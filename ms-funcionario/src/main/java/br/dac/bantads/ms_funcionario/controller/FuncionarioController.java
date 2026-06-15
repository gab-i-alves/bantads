package br.dac.bantads.ms_funcionario.controller;

import br.dac.bantads.ms_funcionario.dto.CreateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerentes")
public class FuncionarioController {

    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping
    public ResponseEntity<List<FuncionarioResponseDTO>> findAll() {
        return ResponseEntity.ok(funcionarioService.findAll());
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<FuncionarioResponseDTO> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(funcionarioService.findByCpf(cpf));
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> create(@Valid @RequestBody CreateFuncionarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.create(dto));
    }

    // R18: dispara SAGA. Retorna 202 ACCEPTED com os dados do gerente que está sendo
    // removido — o delete físico, reatribuição de contas e remoção de auth acontecem
    // assincronamente. Front-end deve fazer refresh da listagem após a chamada.
    @DeleteMapping("/{cpf}")
    public ResponseEntity<FuncionarioResponseDTO> delete(@PathVariable String cpf) {
        return ResponseEntity.accepted().body(funcionarioService.iniciarRemocao(cpf));
    }

    @PutMapping("/{cpf}")
    public ResponseEntity<FuncionarioResponseDTO> update(@PathVariable String cpf, @Valid @RequestBody UpdateFuncionarioRequestDTO dto) {
        return ResponseEntity.ok(funcionarioService.update(cpf, dto));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<FuncionarioResponseDTO>> findByRole(@PathVariable Role role) {
        return ResponseEntity.ok(funcionarioService.findByRole(role));
    }



}
