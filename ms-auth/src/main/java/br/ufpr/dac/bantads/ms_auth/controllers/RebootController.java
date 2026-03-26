package br.ufpr.dac.bantads.ms_auth.controllers;

import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.services.RebootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reboot")
public class RebootController {

    @Autowired
    private RebootService rebootService;

    @GetMapping
    public Account reboot() {
        return rebootService.initialize();
    }

}
