package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RebootService {

    @Autowired
    private AccountRepository accountRepository;

    public List<Account> initialize() {
        accountRepository.deleteAll();
        return seed();
    }

    // o seed em si fica isolado pra que o boot (NF18) e o /reboot insiram
    // exatamente as mesmas contas sem duplicar a lista de credenciais.
    public List<Account> seed() {
        List<Account> accounts = List.of(
            createAccount("cli1@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli2@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli3@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli4@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli5@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("ger1@bantads.com.br", "tads", Role.GERENTE),
            createAccount("ger2@bantads.com.br", "tads", Role.GERENTE),
            createAccount("ger3@bantads.com.br", "tads", Role.GERENTE),
            createAccount("adm1@bantads.com.br", "tads", Role.ADMINISTRADOR)
        );
        accountRepository.insert(accounts);
        return accounts;
    }

    // popula as contas no boot só quando a coleção está vazia; o /reboot continua
    // sendo o caminho que zera e recria, então aqui não apaga nada pra não duplicar.
    public void seedIfEmpty() {
        if (accountRepository.count() == 0) {
            seed();
        }
    }

    private Account createAccount(String email, String password, Role role) {
        Account account = new Account();
        account.setEmail(email);
        // gera salt novo e grava hash SHA256+SALT (NF11); reseed regrava password+salt.
        String salt = PasswordHasher.gerarSalt();
        account.setSalt(salt);
        account.setPassword(PasswordHasher.hash(password, salt));
        account.setRole(role);
        return account;
    }

}
