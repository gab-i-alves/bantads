package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;

// R3: saldo na tela inicial do cliente
public record SaldoResponseDTO(
    String cliente,
    String conta,
    BigDecimal saldo
) {}
