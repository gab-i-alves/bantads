package br.ufpr.dac.bantads.ms_auth.dtos;

import br.ufpr.dac.bantads.ms_auth.models.Account;

// NF10: o /reboot não pode devolver a entidade Account (vaza o hash da senha e
// detalhes de persistência). Este DTO expõe só o necessário pra confirmar o seed.
public record AccountResumoDTO(String email, String tipo) {

    public static AccountResumoDTO de(Account account) {
        return new AccountResumoDTO(
                account.getEmail(),
                account.getRole() == null ? null : account.getRole().toString());
    }
}
