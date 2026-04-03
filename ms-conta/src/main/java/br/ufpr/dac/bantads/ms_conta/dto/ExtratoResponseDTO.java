package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// R8: extrato com movimentacoes
public record ExtratoResponseDTO(
    String conta,
    BigDecimal saldo,
    List<ItemExtrato> movimentacoes
) {
    public record ItemExtrato(
        LocalDateTime data,
        String tipo,
        String origem,
        String destino,
        BigDecimal valor
    ) {}
}
