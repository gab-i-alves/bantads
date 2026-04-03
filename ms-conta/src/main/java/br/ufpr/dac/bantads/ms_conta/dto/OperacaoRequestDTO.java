package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// R5, R6: entrada pra deposito e saque
public record OperacaoRequestDTO(
    @NotNull @Positive BigDecimal valor
) {}
