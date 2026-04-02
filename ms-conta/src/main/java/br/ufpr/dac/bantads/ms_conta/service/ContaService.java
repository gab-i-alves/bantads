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
import br.ufpr.dac.bantads.ms_conta.model.Conta;
import br.ufpr.dac.bantads.ms_conta.model.Movimentacao;
import br.ufpr.dac.bantads.ms_conta.model.TipoMovimentacao;
import br.ufpr.dac.bantads.ms_conta.repository.ContaRepository;
import br.ufpr.dac.bantads.ms_conta.repository.MovimentacaoRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepo;
    private final MovimentacaoRepository movRepo;

    public ContaService(ContaRepository contaRepo, MovimentacaoRepository movRepo) {
        this.contaRepo = contaRepo;
        this.movRepo = movRepo;
    }

    // R3: consultar saldo pra tela inicial do cliente
    public SaldoResponseDTO consultarSaldo(String numeroConta) {
        Conta conta = buscarContaOuErro(numeroConta);
        return new SaldoResponseDTO(conta.getClienteCpf(), conta.getNumero(), conta.getSaldo());
    }

    // R8: consultar extrato - com filtro opcional de data inicio/fim
    public ExtratoResponseDTO consultarExtrato(String numeroConta,
                                                LocalDate inicio, LocalDate fim) {
        Conta conta = buscarContaOuErro(numeroConta);

        List<Movimentacao> movs;
        if (inicio != null && fim != null) {
            movs = movRepo.findByContaAndPeriodo(
                    numeroConta, inicio.atStartOfDay(), fim.atTime(23, 59, 59));
        } else {
            movs = movRepo.findByContaNumero(numeroConta);
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

    // R5: depositar na propria conta
    @Transactional
    public OperacaoResponseDTO depositar(String numeroConta, BigDecimal valor) {
        Conta conta = buscarContaOuErro(numeroConta);

        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepo.save(conta);

        LocalDateTime agora = LocalDateTime.now();
        Movimentacao mov = new Movimentacao();
        mov.setDataHora(agora);
        mov.setTipo(TipoMovimentacao.DEPOSITO);
        mov.setContaOrigem(numeroConta);
        mov.setValor(valor);
        movRepo.save(mov);

        // TODO pendente: publicar evento no rabbitmq pra sync do banco de leitura (cqrs)

        return new OperacaoResponseDTO(numeroConta, agora, conta.getSaldo());
    }

    // R6: saque - so permite se saldo + limite >= valor
    @Transactional
    public OperacaoResponseDTO sacar(String numeroConta, BigDecimal valor) {
        Conta conta = buscarContaOuErro(numeroConta);

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

        // TODO pendente: publicar evento no rabbitmq pra sync do banco de leitura (cqrs)

        return new OperacaoResponseDTO(numeroConta, agora, conta.getSaldo());
    }

    // R7: transferencia entre contas
    @Transactional
    public TransferenciaResponseDTO transferir(String numeroOrigem, String numeroDestino, BigDecimal valor) {
        if (numeroOrigem.equals(numeroDestino)) {
            throw new IllegalArgumentException("Não pode transferir pra mesma conta");
        }

        Conta origem = buscarContaOuErro(numeroOrigem);
        Conta destino = buscarContaOuErro(numeroDestino);

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

        // TODO pendente: publicar evento no rabbitmq pra sync do banco de leitura (cqrs)

        return new TransferenciaResponseDTO(numeroOrigem, agora, numeroDestino, origem.getSaldo(), valor);
    }

    private Conta buscarContaOuErro(String numeroConta) {
        return contaRepo.findByNumero(numeroConta).orElseThrow(() -> new RuntimeException("Conta não encontrada: " + numeroConta));
    }
}
