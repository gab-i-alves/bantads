package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RebootService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<Funcionario> reboot() {

        funcionarioRepository.deleteAll();
        List<Funcionario> funcionarios = List.of(
            new Funcionario(null, "98574307084", "Geniéve", "ger1@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "64065268052", "Godophredo", "ger2@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "23862179060", "Gyândula", "ger3@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "40501740066", "Adamântio",  "ger4@bantads.com.br", Role.ADMINISTRADOR)
        );

        funcionarioRepository.saveAll(funcionarios);

        return funcionarios;

    }
}
