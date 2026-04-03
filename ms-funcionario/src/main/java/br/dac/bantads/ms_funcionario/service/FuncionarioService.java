package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.dto.CreateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuncionarioService {

    @Autowired
    FuncionarioRepository funcionarioRepository;

    @Transactional
    public FuncionarioResponseDTO create(CreateFuncionarioRequestDTO dto) {
        Funcionario funcionario = new Funcionario(
                null,
                dto.getCpf(),
                dto.getNome(),
                dto.getEmail(),
                dto.getRole()
        );
        funcionarioRepository.save(funcionario);
        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional
    public FuncionarioResponseDTO delete(String cpf) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow();
        funcionarioRepository.delete(funcionario);
        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional
    public FuncionarioResponseDTO update(String cpf, UpdateFuncionarioRequestDTO dto) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow();
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setRole(dto.getRole());

        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional
    public FuncionarioResponseDTO findByCpf(String cpf) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf).orElseThrow();
        return new FuncionarioResponseDTO(funcionario);
    }

    @Transactional
    public List<FuncionarioResponseDTO> findAll() {
        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        return funcionarios.stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }

    @Transactional
    public List<FuncionarioResponseDTO> findByRole(Role role) {
        List<Funcionario> funcionarios = funcionarioRepository.findByRole(role);
        return funcionarios.stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }


}
