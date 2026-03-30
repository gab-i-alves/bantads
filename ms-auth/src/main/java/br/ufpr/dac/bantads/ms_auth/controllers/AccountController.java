package br.ufpr.dac.bantads.ms_auth.controllers;

import br.ufpr.dac.bantads.ms_auth.dtos.LoginRequestDTO;
import br.ufpr.dac.bantads.ms_auth.dtos.LoginResponseDTO;
import br.ufpr.dac.bantads.ms_auth.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = accountService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
