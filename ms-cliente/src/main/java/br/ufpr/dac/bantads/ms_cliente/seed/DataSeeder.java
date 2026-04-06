package br.ufpr.dac.bantads.ms_cliente.seed;

import br.ufpr.dac.bantads.ms_cliente.model.*;
import br.ufpr.dac.bantads.ms_cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ClienteRepository repository;

    // dados pré-cadastrados exigidos na secao 4 do enunciado
    // só insere se o banco estiver vazio (evita duplicar no restart)
    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Banco ja possui dados, seed ignorado.");
            return;
        }
        seed();
    }

    // /reboot: apaga tudo e re-insere os dados da spec
    @Transactional
    public void reboot() {
        repository.deleteAll();
        repository.flush();
        seed();
        log.info("Reboot: dados resetados pro estado inicial da spec.");
    }

    private void seed() {
        List<Cliente> clientes = List.of(
            Cliente.builder()
                .nome("Catharyna").email("cli1@bantads.com.br").cpf("12912861012")
                .telefone("(41) 99901-1001").salario(new BigDecimal("10000.00"))
                .status(StatusCliente.APROVADO)
                .endereco(Endereco.builder()
                    .logradouro("Rua XV de Novembro").numero("100")
                    .complemento("Apto 501").cep("80020310")
                    .cidade("Curitiba").estado("PR").build())
                .build(),
            Cliente.builder()
                .nome("Cleuddônio").email("cli2@bantads.com.br").cpf("09506382000")
                .telefone("(41) 99902-2002").salario(new BigDecimal("20000.00"))
                .status(StatusCliente.APROVADO)
                .endereco(Endereco.builder()
                    .logradouro("Av. Sete de Setembro").numero("2775")
                    .complemento("Sala 302").cep("80230010")
                    .cidade("Curitiba").estado("PR").build())
                .build(),
            Cliente.builder()
                .nome("Catianna").email("cli3@bantads.com.br").cpf("85733854057")
                .telefone("(41) 99903-3003").salario(new BigDecimal("3000.00"))
                .status(StatusCliente.APROVADO)
                .endereco(Endereco.builder()
                    .logradouro("Rua Marechal Deodoro").numero("630")
                    .complemento(null).cep("80010912")
                    .cidade("Curitiba").estado("PR").build())
                .build(),
            Cliente.builder()
                .nome("Cutardo").email("cli4@bantads.com.br").cpf("58872160006")
                .telefone("(41) 99904-4004").salario(new BigDecimal("500.00"))
                .status(StatusCliente.APROVADO)
                .endereco(Endereco.builder()
                    .logradouro("Av. República Argentina").numero("1228")
                    .complemento("Bloco B").cep("80620010")
                    .cidade("Curitiba").estado("PR").build())
                .build(),
            Cliente.builder()
                .nome("Coândrya").email("cli5@bantads.com.br").cpf("76179646090")
                .telefone("(41) 99905-5005").salario(new BigDecimal("1500.00"))
                .status(StatusCliente.APROVADO)
                .endereco(Endereco.builder()
                    .logradouro("Rua Comendador Araújo").numero("435")
                    .complemento("Apto 12").cep("80420000")
                    .cidade("Curitiba").estado("PR").build())
                .build()
        );

        repository.saveAll(clientes);
        log.info("Seed: {} clientes inseridos.", clientes.size());
    }

}
