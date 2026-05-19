package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.config.RabbitConfig;
import br.dac.bantads.ms_funcionario.dto.CreateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.exceptions.FuncionarioExceptions;
import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FuncionarioService {

    @Autowired
    FuncionarioRepository funcionarioRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public FuncionarioResponseDTO create(CreateFuncionarioRequestDTO dto) {
        if (funcionarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new FuncionarioExceptions.CpfInUseException(dto.getCpf());
        }
        Funcionario funcionario = new Funcionario(
                null,
                dto.getCpf(),
                dto.getNome(),
                dto.getEmail(),
                dto.getRole() != null ? dto.getRole() : Role.GERENTE
        );
        funcionarioRepository.save(funcionario);

        dispararSagaInsercaoGerente(dto);

        return new FuncionarioResponseDTO(funcionario);
    }

    private void dispararSagaInsercaoGerente(CreateFuncionarioRequestDTO dto) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("cpf", dto.getCpf());
        payload.put("nome", dto.getNome());
        payload.put("email", dto.getEmail());
        payload.put("senha", dto.getSenha());

        try {
            String json = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.SAGA_EXCHANGE,
                    RabbitConfig.START_INSERCAO_GERENTE_ROUTING_KEY,
                    json
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("falha ao serializar payload da saga R17", e);
        }
    }

    @Transactional
    public FuncionarioResponseDTO delete(String cpf) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow(
                FuncionarioExceptions.NotFoundException::new
        );
        funcionarioRepository.delete(funcionario);
        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional
    public FuncionarioResponseDTO update(String cpf, UpdateFuncionarioRequestDTO dto) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow(
                FuncionarioExceptions.NotFoundException::new
        );
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setRole(dto.getRole());

        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional(readOnly = true)
    public FuncionarioResponseDTO findByCpf(String cpf) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow(
                FuncionarioExceptions.NotFoundException::new
        );
        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponseDTO> findAll() {
        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        return funcionarios.stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponseDTO> findByRole(Role role) {
        List<Funcionario> funcionarios = funcionarioRepository.findByRole(role);
        return funcionarios.stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }


}
