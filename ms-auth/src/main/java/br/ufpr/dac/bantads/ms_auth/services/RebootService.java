package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RebootService {

    @Autowired
    private AccountRepository accountRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<Account> initialize() {

        accountRepository.deleteAll();

        List<Account> accounts = List.of(
            createAccount("cli1@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli2@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli3@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli4@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("cli5@bantads.com.br", "tads", Role.CLIENTE),
            createAccount("ger1@bantads.com.br", "tads", Role.GERENTE),
            createAccount("ger2@bantads.com.br", "tads", Role.GERENTE),
            createAccount("ger3@bantads.com.br", "tads", Role.GERENTE),
            createAccount("ger4@bantads.com.br", "tads", Role.ADMINISTRADOR)
        );
        accountRepository.insert(accounts);
        return accounts;
    }

    private Account createAccount(String email, String password, Role role) {
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setRole(role);
        return account;
    }

}
