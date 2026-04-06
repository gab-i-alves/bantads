package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionarioResponseDTO {
    private String cpf;
    private String nome;
    private String email;
    private Role role;

    public FuncionarioResponseDTO(Funcionario funcionario) {
        this.cpf = funcionario.getCpf();
        this.nome = funcionario.getNome();
        this.email = funcionario.getEmail();
        this.role = funcionario.getRole();
    }
}