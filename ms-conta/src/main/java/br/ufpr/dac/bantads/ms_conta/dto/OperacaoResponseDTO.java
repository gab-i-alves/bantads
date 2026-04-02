package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// R5, R6: resposta de deposito e saque
public record OperacaoResponseDTO(
    String conta,
    LocalDateTime data,
    BigDecimal saldo
) {}
