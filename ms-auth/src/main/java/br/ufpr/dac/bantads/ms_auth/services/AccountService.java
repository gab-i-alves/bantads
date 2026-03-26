package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.dtos.LoginRequestDTO;
import br.ufpr.dac.bantads.ms_auth.dtos.LoginResponseDTO;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email or password are incorrect"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Email or password are incorrect");
        }

        String token = jwtService.buildToken(account);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccountId(account.getAccountId());
        response.setRole(account.getRole());
        response.setAccessToken(token);
        return response;
    }

}

