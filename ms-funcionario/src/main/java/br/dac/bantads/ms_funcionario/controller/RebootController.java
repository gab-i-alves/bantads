package br.dac.bantads.ms_funcionario.controller;

import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
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

    // NF10: devolve DTOs, não a entidade JPA.
    @GetMapping
    public List<FuncionarioResponseDTO> reboot() {
        return rebootService.reboot();
    }

}
