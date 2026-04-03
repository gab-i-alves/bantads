package br.dac.bantads.ms_funcionario.controller;

import br.dac.bantads.ms_funcionario.dto.CreateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;
import br.dac.bantads.ms_funcionario.service.RebootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funcionario")
public class FuncionarioController {

    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping
    public List<FuncionarioResponseDTO> findAll() {
        return funcionarioService.findAll();
    }

    @GetMapping("/{cpf}")
    public FuncionarioResponseDTO findByCpf(@PathVariable String cpf) {
        return funcionarioService.findByCpf(cpf);
    }

    @PostMapping
    public FuncionarioResponseDTO create(@RequestBody CreateFuncionarioRequestDTO dto) {
        return funcionarioService.create(dto);
    }

    @DeleteMapping("/{cpf}")
    public FuncionarioResponseDTO delete(@PathVariable String cpf) {
        return funcionarioService.delete(cpf);
    }

    @PutMapping("/{cpf}")
    public FuncionarioResponseDTO update(@PathVariable String cpf, @RequestBody UpdateFuncionarioRequestDTO dto) {
        return funcionarioService.update(cpf, dto);
    }

    @GetMapping("/role/{role}")
    public List<FuncionarioResponseDTO> findByRole(@PathVariable Role role) {
        return funcionarioService.findByRole(role);
    }



}