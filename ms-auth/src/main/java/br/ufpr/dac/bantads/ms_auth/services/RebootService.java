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

    public Account initialize() {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Account fefeAccount = new Account();
        fefeAccount.setAccountId("12912861012"); // cpf placeholder
        fefeAccount.setEmail("cli1@bantads.com.br");
        fefeAccount.setPassword(passwordEncoder.encode("tads"));
        fefeAccount.setRole(Role.CLIENTE);

        accountRepository.insert(fefeAccount);
        return fefeAccount;
    }

}
