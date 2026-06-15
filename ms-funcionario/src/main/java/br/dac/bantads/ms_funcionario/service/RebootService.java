package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
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

    // NF10: não expõe a entidade JPA; devolve DTOs.
    public List<FuncionarioResponseDTO> reboot() {
        funcionarioRepository.deleteAll();
        List<Funcionario> funcionarios = funcionarioRepository.saveAll(seedFuncionarios());
        return funcionarios.stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }

    // NF18: popula o seed no boot só se a tabela estiver vazia, pra não duplicar
    // nem sobrescrever dados de um banco já em uso.
    public void seedIfEmpty() {
        if (funcionarioRepository.count() == 0) {
            funcionarioRepository.saveAll(seedFuncionarios());
        }
    }

    // Lista canônica de seed reutilizada pelo /reboot e pelo boot, pra não divergirem.
    private List<Funcionario> seedFuncionarios() {
        return List.of(
            new Funcionario(null, "98574307084", "Geniéve", "ger1@bantads.com.br", "(41) 99999-0001", Role.GERENTE),
            new Funcionario(null, "64065268052", "Godophredo", "ger2@bantads.com.br", "(41) 99999-0002", Role.GERENTE),
            new Funcionario(null, "23862179060", "Gyândula", "ger3@bantads.com.br", "(41) 99999-0003", Role.GERENTE),
            new Funcionario(null, "40501740066", "Adamântio", "adm1@bantads.com.br", "(41) 99999-0004", Role.ADMINISTRADOR)
        );
    }
}
