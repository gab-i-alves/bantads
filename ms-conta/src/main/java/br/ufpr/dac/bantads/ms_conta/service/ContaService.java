package br.ufpr.dac.bantads.ms_conta.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.bantads.ms_conta.dto.ContaResumoDTO;
import br.ufpr.dac.bantads.ms_conta.dto.ExtratoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.ExtratoResponseDTO.ItemExtrato;
import br.ufpr.dac.bantads.ms_conta.dto.OperacaoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.SaldoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.TransferenciaResponseDTO;
import br.ufpr.dac.bantads.ms_conta.exceptions.ContaExceptions;
import br.ufpr.dac.bantads.ms_conta.messaging.ContaEvent;
import br.ufpr.dac.bantads.ms_conta.messaging.ContaEventPublisher;
import br.ufpr.dac.bantads.ms_conta.model.Conta;
import br.ufpr.dac.bantads.ms_conta.model.ContaRead;
import br.ufpr.dac.bantads.ms_conta.model.Movimentacao;
import br.ufpr.dac.bantads.ms_conta.model.MovimentacaoRead;
import br.ufpr.dac.bantads.ms_conta.model.TipoMovimentacao;
import br.ufpr.dac.bantads.ms_conta.repository.ContaReadRepository;
import br.ufpr.dac.bantads.ms_conta.repository.ContaRepository;
import br.ufpr.dac.bantads.ms_conta.repository.MovimentacaoReadRepository;
import br.ufpr.dac.bantads.ms_conta.repository.MovimentacaoRepository;

@Service
public class ContaService {

    // repositórios de ESCRITA (schema_conta_cud)
    private final ContaRepository contaRepo;
    private final MovimentacaoRepository movRepo;

    // repositórios de LEITURA (schema_conta_read)
    private final ContaReadRepository contaReadRepo;
    private final MovimentacaoReadRepository movReadRepo;

    // publisher que manda eventos pro RabbitMQ
    private final ContaEventPublisher publisher;

    public ContaService(ContaRepository contaRepo,
                        MovimentacaoRepository movRepo,
                        ContaReadRepository contaReadRepo,
                        MovimentacaoReadRepository movReadRepo,
                        ContaEventPublisher publisher) {
        this.contaRepo = contaRepo;
        this.movRepo = movRepo;
        this.contaReadRepo = contaReadRepo;
        this.movReadRepo = movReadRepo;
        this.publisher = publisher;
    }

    // ==================== LEITURA (usa schema_conta_read) ====================

    // listagem de todas as contas - usado pelo gateway pra montar a tela
    // do admin (R12: lista de gerentes com seus clientes)
    public List<ContaResumoDTO> listarTodas() {
        return contaReadRepo.findAll().stream()
                .map(c -> new ContaResumoDTO(
                        c.getNumero(),
                        c.getClienteCpf(),
                        c.getGerenteCpf(),
                        c.getSaldo(),
                        c.getLimite(),
                        c.getDataCriacao()))
                .toList();
    }

    // R3: consultar saldo pra tela inicial do cliente
    public SaldoResponseDTO consultarSaldo(String numeroConta) {
        ContaRead conta = buscarContaReadOuErro(numeroConta);
        return new SaldoResponseDTO(conta.getClienteCpf(), conta.getNumero(), conta.getSaldo());
    }

    public ExtratoResponseDTO consultarExtrato(String numeroConta, LocalDate inicio, LocalDate fim) {
        ContaRead conta = buscarContaReadOuErro(numeroConta);

        List<MovimentacaoRead> movs;
        if (inicio != null && fim != null) {
            movs = movReadRepo.findByContaAndPeriodo(
                    numeroConta, inicio.atStartOfDay(), fim.atTime(23, 59, 59));
        } else {
            movs = movReadRepo.findByContaNumero(numeroConta);
        }

        List<ItemExtrato> itens = movs.stream()
                .map(m -> new ItemExtrato(
                        m.getDataHora(),
                        m.getTipo().getDescricao(),
                        m.getContaOrigem(),
                        m.getContaDestino(),
                        m.getValor()))
                .toList();

        List<ExtratoResponseDTO.SaldoDiario> saldosDiarios = inicio != null && fim != null
                ? calcularSaldosDiarios(numeroConta, conta.getSaldo(), inicio, fim)
                : List.of();

        return new ExtratoResponseDTO(conta.getNumero(), conta.getSaldo(), itens, saldosDiarios);
    }

    private List<ExtratoResponseDTO.SaldoDiario> calcularSaldosDiarios(
            String numeroConta, BigDecimal saldoAtual, LocalDate inicio, LocalDate fim) {

        List<MovimentacaoRead> todasMovs = movReadRepo.findByContaNumero(numeroConta);

        // uma passada só: movs depois de 'fim' recuam o saldo final;
        // movs dentro de [inicio, fim] acumulam o delta do dia (faixas disjuntas)
        BigDecimal saldoAposFim = saldoAtual;
        Map<LocalDate, BigDecimal> deltasPorDia = new HashMap<>();
        for (MovimentacaoRead m : todasMovs) {
            LocalDate d = m.getDataHora().toLocalDate();
            if (d.isAfter(fim)) {
                saldoAposFim = saldoAposFim.subtract(delta(m, numeroConta));
            } else if (!d.isBefore(inicio)) {
                deltasPorDia.merge(d, delta(m, numeroConta), BigDecimal::add);
            }
        }

        BigDecimal saldoInicial = saldoAposFim;
        for (BigDecimal delta : deltasPorDia.values()) {
            saldoInicial = saldoInicial.subtract(delta);
        }

        List<ExtratoResponseDTO.SaldoDiario> resultado = new ArrayList<>();
        BigDecimal saldoCorrente = saldoInicial;
        LocalDate dia = inicio;
        while (!dia.isAfter(fim)) {
            saldoCorrente = saldoCorrente.add(deltasPorDia.getOrDefault(dia, BigDecimal.ZERO));
            resultado.add(new ExtratoResponseDTO.SaldoDiario(dia, saldoCorrente));
            dia = dia.plusDays(1);
        }
        return resultado;
    }

    private BigDecimal delta(MovimentacaoRead m, String numeroConta) {
        return switch (m.getTipo()) {
            case DEPOSITO -> m.getValor();
            case SAQUE -> m.getValor().negate();
            case TRANSFERENCIA -> numeroConta.equals(m.getContaOrigem())
                    ? m.getValor().negate()
                    : m.getValor();
        };
    }

    // ==================== ESCRITA (usa schema_conta_cud + publica evento) ====================

    // R5: depositar na propria conta
    @Transactional
    public OperacaoResponseDTO depositar(String numeroConta, BigDecimal valor) {
        Conta conta = buscarContaCudOuErro(numeroConta);

        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepo.save(conta);

        LocalDateTime agora = LocalDateTime.now();
        Movimentacao mov = new Movimentacao();
        mov.setDataHora(agora);
        mov.setTipo(TipoMovimentacao.DEPOSITO);
        mov.setContaOrigem(numeroConta);
        mov.setValor(valor);
        movRepo.save(mov);

        // publica evento pra sincronizar o schema de leitura via RabbitMQ
        publisher.publicar(new ContaEvent(
                "DEPOSITO", numeroConta, conta.getSaldo(),
                valor, numeroConta, null, agora));

        return new OperacaoResponseDTO(numeroConta, agora, conta.getSaldo());
    }

    // R6: saque - so permite se saldo + limite >= valor
    @Transactional
    public OperacaoResponseDTO sacar(String numeroConta, BigDecimal valor) {
        Conta conta = buscarContaCudOuErro(numeroConta);

        validarSaldoSuficiente(conta, valor);

        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepo.save(conta);

        LocalDateTime agora = LocalDateTime.now();
        Movimentacao mov = new Movimentacao();
        mov.setDataHora(agora);
        mov.setTipo(TipoMovimentacao.SAQUE);
        mov.setContaOrigem(numeroConta);
        mov.setValor(valor);
        movRepo.save(mov);

        // publica evento pra sincronizar o schema de leitura via RabbitMQ
        publisher.publicar(new ContaEvent(
                "SAQUE", numeroConta, conta.getSaldo(),
                valor, numeroConta, null, agora));

        return new OperacaoResponseDTO(numeroConta, agora, conta.getSaldo());
    }

    // R7: transferencia entre contas
    @Transactional
    public TransferenciaResponseDTO transferir(String numeroOrigem, String numeroDestino, BigDecimal valor) {
        if (numeroOrigem.equals(numeroDestino)) {
            throw new IllegalArgumentException("Não pode transferir pra mesma conta");
        }

        Conta origem = buscarContaCudOuErro(numeroOrigem);
        Conta destino = buscarContaCudOuErro(numeroDestino);

        validarSaldoSuficiente(origem, valor);

        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));
        contaRepo.save(origem);
        contaRepo.save(destino);

        LocalDateTime agora = LocalDateTime.now();
        Movimentacao mov = new Movimentacao();
        mov.setDataHora(agora);
        mov.setTipo(TipoMovimentacao.TRANSFERENCIA);
        mov.setContaOrigem(numeroOrigem);
        mov.setContaDestino(numeroDestino);
        mov.setValor(valor);
        movRepo.save(mov);

        // publica 2 eventos: um pra conta de origem (debito) e outro pra destino (credito)
        publisher.publicar(new ContaEvent(
                "TRANSFERENCIA", numeroOrigem, origem.getSaldo(),
                valor, numeroOrigem, numeroDestino, agora));
        publisher.publicar(new ContaEvent(
                "TRANSFERENCIA", numeroDestino, destino.getSaldo(),
                valor, numeroOrigem, numeroDestino, agora));

        return new TransferenciaResponseDTO(numeroOrigem, agora, numeroDestino, origem.getSaldo(), valor);
    }

    // busca no schema READ (pra consultas)
    private ContaRead buscarContaReadOuErro(String numeroConta) {
        return contaReadRepo.findByNumero(numeroConta)
                .orElseThrow(() -> new ContaExceptions.NotFoundException(numeroConta));
    }

    // busca no schema CUD (pra operações de escrita)
    private Conta buscarContaCudOuErro(String numeroConta) {
        return contaRepo.findByNumero(numeroConta)
                .orElseThrow(() -> new ContaExceptions.NotFoundException(numeroConta));
    }

    // R6/R7: só permite debitar se saldo + limite cobre o valor
    private void validarSaldoSuficiente(Conta conta, BigDecimal valor) {
        BigDecimal disponivel = conta.getSaldo().add(conta.getLimite());
        if (disponivel.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
    }
}
