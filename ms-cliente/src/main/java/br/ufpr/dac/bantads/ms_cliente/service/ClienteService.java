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
}
