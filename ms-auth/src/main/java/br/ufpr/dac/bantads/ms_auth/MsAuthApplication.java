package br.ufpr.dac.bantads.ms_auth;

import br.ufpr.dac.bantads.ms_auth.services.RebootService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MsAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthApplication.class, args);
	}

	// NF18: a tabela nasce vazia no boot, então semeia as contas padrão uma vez
	// pra que o login funcione sem depender de uma chamada manual ao /reboot.
	@Bean
	public CommandLineRunner seedAccounts(RebootService rebootService) {
		return args -> rebootService.seedIfEmpty();
	}

}
