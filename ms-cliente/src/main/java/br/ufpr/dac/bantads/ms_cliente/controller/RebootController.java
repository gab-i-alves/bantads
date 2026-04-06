package br.ufpr.dac.bantads.ms_cliente.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.dac.bantads.ms_cliente.seed.DataSeeder;
import lombok.RequiredArgsConstructor;

// /reboot: reseta o banco pro estado inicial da spec (secao 4)
@RestController
@RequiredArgsConstructor
public class RebootController {

    private final DataSeeder dataSeeder;

    @GetMapping("/reboot")
    public ResponseEntity<String> reboot() {
        dataSeeder.reboot();
        return ResponseEntity.ok("ms-cliente resetado com dados da spec");
    }
}
