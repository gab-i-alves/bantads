package br.ufpr.dac.bantads.ms_conta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.dac.bantads.ms_conta.seed.DataSeeder;


@RestController
public class RebootController {

    private final DataSeeder dataSeeder;

    public RebootController(DataSeeder dataSeeder) {
        this.dataSeeder = dataSeeder;
    }

    @GetMapping("/reboot")
    public ResponseEntity<String> reboot() {
        dataSeeder.reboot();
        return ResponseEntity.ok("ms-conta resetado com dados da spec");
    }
}
