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
            new Funcionario(null, "Geniéve", "98574307084", "ger1@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "Godophredo", "64065268052", "ger2@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "Gyândula", "23862179060", "ger3@bantads.com.br", Role.GERENTE),
            new Funcionario(null, "Adamântio", "40501740066", "ger4@bantads.com.br", Role.ADMINISTRADOR)
        );

        funcionarioRepository.saveAll(funcionarios);

        return funcionarios;

    }
}
