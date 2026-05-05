package br.ufpr.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// item da listagem de contas - usado pelo gateway pra montar a lista de
// clientes por gerente (R12)
public record ContaResumoDTO(
    String numero,
    String clienteCpf,
    String gerenteCpf,
    BigDecimal saldo,
    BigDecimal limite,
    LocalDate dataCriacao
) {}
