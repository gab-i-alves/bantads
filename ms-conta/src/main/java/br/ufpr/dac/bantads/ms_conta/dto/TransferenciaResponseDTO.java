package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// R7: resposta da transferencia
public record TransferenciaResponseDTO(
    String conta,
    LocalDateTime data,
    String destino,
    BigDecimal saldo,
    BigDecimal valor
) {}
