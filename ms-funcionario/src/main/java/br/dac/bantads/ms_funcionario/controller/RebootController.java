package br.dac.bantads.ms_funcionario.controller;

import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.service.RebootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reboot")
public class RebootController {

    @Autowired
    private RebootService rebootService;

    @GetMapping
    public List<Funcionario> reboot() {
        return rebootService.reboot();
    }

}