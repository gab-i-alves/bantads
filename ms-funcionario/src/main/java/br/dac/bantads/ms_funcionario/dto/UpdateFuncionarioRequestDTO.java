package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFuncionarioRequestDTO {
    private String nome;
    private String email;
    private Role role;
}
