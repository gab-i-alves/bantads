package br.ufpr.dac.bantads.ms_cliente.service;

import br.ufpr.dac.bantads.ms_cliente.dto.*;
import br.ufpr.dac.bantads.ms_cliente.model.*;
import br.ufpr.dac.bantads.ms_cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository repository;

    // R1: cria cliente com status PENDENTE, verifica cpf duplicado (passo sincrono;
    // a saga de autocadastro so dispara na aprovacao R10, via ClienteController)
    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        // R1: rejeita cpf invalido antes de qualquer persistencia. O teste de
        // integracao gera cpfs validos por mod-11, entao cpf valido passa direto.
        if (!cpfValido(dto.cpf())) {
            throw new CpfInvalidoException(dto.cpf());
        }
        if (repository.existsByCpf(dto.cpf())) {
            throw new ClienteJaCadastradoException(dto.cpf());
        }

        Endereco endereco = Endereco.builder()
                .logradouro(dto.endereco().logradouro())
                .numero(dto.endereco().numero())
                .complemento(dto.endereco().complemento())
                .cep(dto.endereco().cep())
                .cidade(dto.endereco().cidade())
                .estado(dto.endereco().estado())
                .build();

        Cliente cliente = Cliente.builder()
                .nome(dto.nome())
                .email(dto.email())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .salario(dto.salario())
                .status(StatusCliente.PENDENTE)
                .endereco(endereco)
                .build();

        Cliente salvo = repository.save(cliente);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos(String busca) {
        return repository.findByStatus(StatusCliente.APROVADO)
                .stream()
                .filter(c -> matchBusca(c, busca))
                .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarPendentes() {
        return repository.findByStatus(StatusCliente.PENDENTE)
                .stream()
                .sorted(Comparator.comparing(Cliente::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    private boolean matchBusca(Cliente c, String busca) {
        if (busca == null || busca.isBlank()) return true;
        String q = busca.toLowerCase();
        return c.getCpf().contains(q) || c.getNome().toLowerCase().contains(q);
    }

    // R13: busca cliente por cpf
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorCpf(String cpf) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        return toResponse(cliente);
    }

    // R4: altera perfil do cliente (tudo menos cpf); o recalculo de limite no
    // ms-conta e disparado pela saga de alteracao de perfil no ClienteController
    @Transactional
    public ClienteResponseDTO atualizar(String cpf, ClienteRequestDTO dto) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));

        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setSalario(dto.salario());

        Endereco endereco = cliente.getEndereco();
        endereco.setLogradouro(dto.endereco().logradouro());
        endereco.setNumero(dto.endereco().numero());
        endereco.setComplemento(dto.endereco().complemento());
        endereco.setCep(dto.endereco().cep());
        endereco.setCidade(dto.endereco().cidade());
        endereco.setEstado(dto.endereco().estado());

        Cliente salvo = repository.save(cliente);
        return toResponse(salvo);
    }

    @Transactional
    public ClienteResponseDTO aprovar(String cpf) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        cliente.setStatus(StatusCliente.APROVADO);
        cliente.setDataDecisao(java.time.LocalDateTime.now());
        cliente.setMotivoRejeicao(null);
        Cliente salvo = repository.save(cliente);
        return toResponse(salvo);
    }

    @Transactional
    public void reverterParaPendente(String cpf) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        cliente.setStatus(StatusCliente.PENDENTE);
        cliente.setDataDecisao(null);
        repository.save(cliente);
        // R1: a saga R10 falhou apos a aprovacao e o cliente voltou pra PENDENTE.
        // Mesma logica do mock de rejeicao (R11): o cliente precisa ser avisado.
        log.info("[MOCK EMAIL] R1 falha no cadastro -> para={} | motivo=falha ao concluir abertura de conta",
                cliente.getEmail());
    }

    // R4 (compensacao): restaura o perfil anterior do cliente a partir dos dados
    // que o orquestrador capturou antes do ATUALIZAR_CLIENTE. Nao mexe em status
    // nem cpf (cpf e imutavel; status nao faz parte da alteracao de perfil).
    @Transactional
    public ClienteResponseDTO reverterAtualizacao(String cpf, ClienteRequestDTO anterior) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));

        cliente.setNome(anterior.nome());
        cliente.setEmail(anterior.email());
        cliente.setTelefone(anterior.telefone());
        cliente.setSalario(anterior.salario());

        Endereco endereco = cliente.getEndereco();
        endereco.setLogradouro(anterior.endereco().logradouro());
        endereco.setNumero(anterior.endereco().numero());
        endereco.setComplemento(anterior.endereco().complemento());
        endereco.setCep(anterior.endereco().cep());
        endereco.setCidade(anterior.endereco().cidade());
        endereco.setEstado(anterior.endereco().estado());

        Cliente salvo = repository.save(cliente);
        log.info("REVERTER_ATUALIZACAO_CLIENTE: cpf={} perfil restaurado", cpf);
        return toResponse(salvo);
    }

    @Transactional
    public ClienteResponseDTO rejeitar(String cpf, RejeitarRequestDTO dto) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        cliente.setStatus(StatusCliente.REJEITADO);
        cliente.setDataDecisao(java.time.LocalDateTime.now());
        cliente.setMotivoRejeicao(dto != null ? dto.motivo() : null);
        Cliente salvo = repository.save(cliente);
        log.info("[MOCK EMAIL] R11 rejeicao -> para={} | motivo={}", cliente.getEmail(), cliente.getMotivoRejeicao());
        return toResponse(salvo);
    }

    private ClienteResponseDTO toResponse(Cliente c) {
        EnderecoDTO endDto = new EnderecoDTO(
                c.getEndereco().getLogradouro(),
                c.getEndereco().getNumero(),
                c.getEndereco().getComplemento(),
                c.getEndereco().getCep(),
                c.getEndereco().getCidade(),
                c.getEndereco().getEstado()
        );
        return new ClienteResponseDTO(
                c.getId(),
                c.getNome(),
                c.getEmail(),
                c.getCpf(),
                c.getTelefone(),
                c.getSalario(),
                c.getStatus().name(),
                endDto,
                c.getDataDecisao(),
                c.getMotivoRejeicao()
        );
    }

    // --- validacao ---

    // Valida cpf por digito verificador (mod-11). Aceita com ou sem mascara
    // (pontos/traco sao ignorados). Rejeita tamanho != 11 e todos os digitos
    // iguais (ex 11111111111), que passam na conta mas nao sao cpfs reais.
    static boolean cpfValido(String cpf) {
        if (cpf == null) return false;
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) return false;
        if (digitos.chars().distinct().count() == 1) return false;

        int[] n = new int[11];
        for (int i = 0; i < 11; i++) {
            n[i] = digitos.charAt(i) - '0';
        }
        if (digitoVerificador(n, 9, 10) != n[9]) return false;
        return digitoVerificador(n, 10, 11) == n[10];
    }

    // soma ponderada dos primeiros `qtd` digitos com pesos decrescentes a partir
    // de `pesoInicial`; resto < 2 vira 0 (regra do mod-11 do cpf)
    private static int digitoVerificador(int[] n, int qtd, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) {
            soma += n[i] * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    // --- exceptions ---

    public static class ClienteNaoEncontradoException extends RuntimeException {
        public ClienteNaoEncontradoException(String cpf) {
            super("cliente nao encontrado: " + cpf);
        }
    }

    public static class ClienteJaCadastradoException extends RuntimeException {
        public ClienteJaCadastradoException(String cpf) {
            super("cliente ja cadastrado ou aguardando aprovacao: " + cpf);
        }
    }

    // estende ClienteJaCadastradoException pra reaproveitar o ExceptionHandler ja
    // existente (HTTP 400/409 no ClienteController). O caminho REST de R1 ja barra
    // cpf invalido por bean-validation (@CpfValido) antes de chegar aqui; este check
    // e a rede de seguranca pro caminho da saga, que nao passa por @Valid.
    public static class CpfInvalidoException extends ClienteJaCadastradoException {
        public CpfInvalidoException(String cpf) {
            super(cpf);
        }
    }
}
