package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExtratoResponseDTO(
    String conta,
    BigDecimal saldo,
    List<ItemExtrato> movimentacoes,
    List<SaldoDiario> saldosDiarios
) {
    public record ItemExtrato(
        LocalDateTime data,
        String tipo,
        String origem,
        String destino,
        BigDecimal valor
    ) {}

    public record SaldoDiario(
        LocalDate data,
        BigDecimal saldo
    ) {}
}
