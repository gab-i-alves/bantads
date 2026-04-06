package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// R7: entrada pra transferencia
public record TransferenciaRequestDTO(
    @NotBlank String destino,
    @NotNull @Positive BigDecimal valor
) {}
