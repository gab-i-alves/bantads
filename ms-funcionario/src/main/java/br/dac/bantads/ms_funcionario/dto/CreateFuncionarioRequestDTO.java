package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFuncionarioRequestDTO {
    @NotBlank
    private String cpf;
    @NotBlank
    private String nome;
    @NotBlank
    @Email
    private String email;
    private String senha;
    // R19: telefone opcional na criação
    private String telefone;
    private Role role;
}
