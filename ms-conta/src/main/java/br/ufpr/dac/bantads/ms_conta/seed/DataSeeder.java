package br.ufpr.dac.bantads.ms_conta.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.bantads.ms_conta.model.Conta;
import br.ufpr.dac.bantads.ms_conta.model.Movimentacao;
import br.ufpr.dac.bantads.ms_conta.model.TipoMovimentacao;
import br.ufpr.dac.bantads.ms_conta.repository.ContaRepository;
import br.ufpr.dac.bantads.ms_conta.repository.MovimentacaoRepository;

// dados pré-cadastrados exigidos na secao 4 do enunciado
// só insere se o banco estiver vazio (evita duplicar no restart)
// reboot() é chamado pelo /reboot pra resetar tudo
@Component
public class DataSeeder implements CommandLineRunner {

    private final ContaRepository contaRepo;
    private final MovimentacaoRepository movRepo;

    public DataSeeder(ContaRepository contaRepo, MovimentacaoRepository movRepo) {
        this.contaRepo = contaRepo;
        this.movRepo = movRepo;
    }

    @Override
    public void run(String... args) {
        if (contaRepo.count() > 0) {
            return;
        }
        seed();
    }

    // /reboot: apaga tudo e re-insere os dados da spec
    @Transactional
    public void reboot() {
        movRepo.deleteAll();
        movRepo.flush();
        contaRepo.deleteAll();
        contaRepo.flush();
        seed();
    }

    private void seed() {
        List<Conta> contas = List.of(
            new Conta(null, "1291", "12912861012", new BigDecimal("800.00"),
                      new BigDecimal("5000.00"), "98574307084",
                      LocalDate.of(2000, 1, 1)),       // Catharyna → Geniéve
            new Conta(null, "0950", "09506382000", new BigDecimal("-10000.00"),
                      new BigDecimal("10000.00"), "64065268052",
                      LocalDate.of(1990, 10, 10)),      // Cleuddônio → Godophredo
            new Conta(null, "8573", "85733854057", new BigDecimal("-1000.00"),
                      new BigDecimal("1500.00"), "23862179060",
                      LocalDate.of(2012, 12, 12)),      // Catianna → Gyândula
            new Conta(null, "5887", "58872160006", new BigDecimal("150000.00"),
                      new BigDecimal("0.00"), "98574307084",
                      LocalDate.of(2022, 2, 22)),       // Cutardo → Geniéve
            new Conta(null, "7617", "76179646090", new BigDecimal("1500.00"),
                      new BigDecimal("0.00"), "64065268052",
                      LocalDate.of(2025, 1, 1))         // Coândrya → Godophredo
        );
        contaRepo.saveAll(contas);

        List<Movimentacao> movs = List.of(
            // Catharyna (1291)
            new Movimentacao(null, LocalDateTime.of(2020, 1, 1, 10, 0),
                TipoMovimentacao.DEPOSITO, "1291", null, new BigDecimal("1000.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 1, 11, 0),
                TipoMovimentacao.DEPOSITO, "1291", null, new BigDecimal("900.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 1, 12, 0),
                TipoMovimentacao.SAQUE, "1291", null, new BigDecimal("550.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 1, 13, 0),
                TipoMovimentacao.SAQUE, "1291", null, new BigDecimal("350.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 10, 15, 0),
                TipoMovimentacao.DEPOSITO, "1291", null, new BigDecimal("2000.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 15, 8, 0),
                TipoMovimentacao.SAQUE, "1291", null, new BigDecimal("500.00")),
            new Movimentacao(null, LocalDateTime.of(2020, 1, 20, 12, 0),
                TipoMovimentacao.TRANSFERENCIA, "1291", "0950", new BigDecimal("1700.00")),
            // Cleuddônio (0950)
            new Movimentacao(null, LocalDateTime.of(2025, 1, 1, 12, 0),
                TipoMovimentacao.DEPOSITO, "0950", null, new BigDecimal("1000.00")),
            new Movimentacao(null, LocalDateTime.of(2025, 1, 2, 10, 0),
                TipoMovimentacao.DEPOSITO, "0950", null, new BigDecimal("5000.00")),
            new Movimentacao(null, LocalDateTime.of(2025, 1, 10, 10, 0),
                TipoMovimentacao.SAQUE, "0950", null, new BigDecimal("200.00")),
            new Movimentacao(null, LocalDateTime.of(2025, 2, 5, 10, 0),
                TipoMovimentacao.DEPOSITO, "0950", null, new BigDecimal("7000.00")),
            // Catianna (8573)
            new Movimentacao(null, LocalDateTime.of(2025, 5, 5, 0, 0),
                TipoMovimentacao.DEPOSITO, "8573", null, new BigDecimal("1000.00")),
            new Movimentacao(null, LocalDateTime.of(2025, 5, 6, 0, 0),
                TipoMovimentacao.SAQUE, "8573", null, new BigDecimal("2000.00")),
            // Cutardo (5887)
            new Movimentacao(null, LocalDateTime.of(2025, 6, 1, 0, 0),
                TipoMovimentacao.DEPOSITO, "5887", null, new BigDecimal("150000.00")),
            // Coândrya (7617)
            new Movimentacao(null, LocalDateTime.of(2025, 7, 1, 0, 0),
                TipoMovimentacao.DEPOSITO, "7617", null, new BigDecimal("1500.00"))
        );
        movRepo.saveAll(movs);

        System.out.println("[ms-conta] Seed: " + contas.size() + " contas e "
                + movs.size() + " movimentacoes inseridas");
    }
}
