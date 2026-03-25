package br.ufpr.dac.bantads.ms_auth.controllers;

import br.ufpr.dac.bantads.ms_auth.dtos.LoginRequestDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AccountController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        return new ResponseEntity(HttpStatusCode.valueOf(200));
    }
}
