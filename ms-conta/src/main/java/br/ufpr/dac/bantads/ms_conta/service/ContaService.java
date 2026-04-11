package br.ufpr.dac.bantads.ms_conta.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.bantads.ms_conta.dto.ExtratoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.ExtratoResponseDTO.ItemExtrato;
import br.ufpr.dac.bantads.ms_conta.dto.OperacaoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.SaldoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.TransferenciaResponseDTO;
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

    // R3: consultar saldo pra tela inicial do cliente
    public SaldoResponseDTO consultarSaldo(String numeroConta) {
        ContaRead conta = contaReadRepo.findByNumero(numeroConta)
                            .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + numeroConta));
        return new SaldoResponseDTO(conta.getClienteCpf(), conta.getNumero(), conta.getSaldo());
    }

    // R8: consultar extrato - com filtro opcional de data inicio/fim
    public ExtratoResponseDTO consultarExtrato(String numeroConta, LocalDate inicio, LocalDate fim) {
        ContaRead conta = contaReadRepo.findByNumero(numeroConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + numeroConta));

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

        return new ExtratoResponseDTO(conta.getNumero(), conta.getSaldo(), itens);
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

        BigDecimal disponivel = conta.getSaldo().add(conta.getLimite());
        if (disponivel.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

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

        BigDecimal disponivel = origem.getSaldo().add(origem.getLimite());
        if (disponivel.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

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

    // busca no schema CUD (pra operações de escrita)
    private Conta buscarContaCudOuErro(String numeroConta) {
        return contaRepo.findByNumero(numeroConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + numeroConta));
    }
}
