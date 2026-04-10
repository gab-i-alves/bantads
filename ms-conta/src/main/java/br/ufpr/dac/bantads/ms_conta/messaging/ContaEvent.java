package br.ufpr.dac.bantads.ms_conta.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// evento publicado no RabbitMQ após cada operação de escrita
// o listener usa esses dados pra atualizar o schema de leitura
//
// tipoEvento: DEPOSITO, SAQUE, TRANSFERENCIA ou REBOOT
// numeroConta: conta que foi alterada
// saldoAtual: saldo da conta APOS a operação
// valorMovimentacao: quanto foi movimentado
// contaDestino: preenchido só em transferências
// dataHora: quando a operação aconteceu
public record ContaEvent(
    String tipoEvento,
    String numeroConta,
    BigDecimal saldoAtual,
    BigDecimal valorMovimentacao,
    String contaOrigem,
    String contaDestino,
    LocalDateTime dataHora
) {}
