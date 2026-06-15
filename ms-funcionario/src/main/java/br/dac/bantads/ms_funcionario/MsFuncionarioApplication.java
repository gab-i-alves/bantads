package br.dac.bantads.ms_funcionario;

import br.dac.bantads.ms_funcionario.service.RebootService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MsFuncionarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsFuncionarioApplication.class, args);
	}

	// NF18: garante o seed dos 3 gerentes + 1 admin no boot, mas só com a tabela
	// vazia (RebootService.seedIfEmpty guarda o count()==0).
	@Bean
	public CommandLineRunner seedFuncionarios(RebootService rebootService) {
		return args -> rebootService.seedIfEmpty();
	}

}
