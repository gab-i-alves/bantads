package br.ufpr.dac.bantads.ms_cliente.service;

import br.ufpr.dac.bantads.ms_cliente.dto.*;
import br.ufpr.dac.bantads.ms_cliente.model.*;
import br.ufpr.dac.bantads.ms_cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    // R1: cria cliente com status PENDENTE, verifica cpf duplicado
    // TODO pendente: isso vai virar parte da saga de autocadastro via rabbitmq
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

    // R12: lista clientes aprovados (os que ja tem conta)
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return repository.findByStatus(StatusCliente.APROVADO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // R9: lista clientes aguardando aprovacao do gerente
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarPendentes() {
        return repository.findByStatus(StatusCliente.PENDENTE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // R13: busca cliente por cpf
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorCpf(String cpf) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        return toResponse(cliente);
    }

    // R4: altera perfil do cliente (tudo menos cpf)
    // TODO pendente: se salario mudar, ms-conta precisa recalcular limite via saga
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

    // R10: gerente aprova cliente
    // TODO pendente: saga deve criar conta no ms-conta e enviar senha por email
    @Transactional
    public ClienteResponseDTO aprovar(String cpf) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        cliente.setStatus(StatusCliente.APROVADO);
        Cliente salvo = repository.save(cliente);
        return toResponse(salvo);
    }

    // R11: gerente rejeita cliente com motivo
    // motivo nao persiste aqui - vai ser enviado por email via rabbitmq
    @Transactional
    public ClienteResponseDTO rejeitar(String cpf, RejeitarRequestDTO dto) {
        Cliente cliente = repository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
        cliente.setStatus(StatusCliente.REJEITADO);
        Cliente salvo = repository.save(cliente);
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
                endDto
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
