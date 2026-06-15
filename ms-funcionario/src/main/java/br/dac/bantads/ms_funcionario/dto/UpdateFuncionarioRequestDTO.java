package br.dac.bantads.ms_funcionario.dto;

import lombok.Getter;
import lombok.Setter;

// R20: corpo do PUT /gerentes/{cpf} traz nome, email, telefone e senha opcional.
// O role do gerente nunca é alterado por esta rota, por isso não aparece aqui.
@Getter
@Setter
public class UpdateFuncionarioRequestDTO {
    private String nome;
    private String email;
    // R19: telefone opcional na atualização
    private String telefone;
    // senha opcional: se vier, dispara atualização best-effort no ms-auth
    private String senha;
}
