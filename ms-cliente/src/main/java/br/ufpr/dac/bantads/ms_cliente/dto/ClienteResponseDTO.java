package br.ufpr.dac.bantads.ms_cliente.dto;

import java.math.BigDecimal;

public record ClienteResponseDTO(
    Long id,
    String nome,
    String email,
    String cpf,
    String telefone,
    BigDecimal salario,
    String status,
    EnderecoDTO endereco
) {}
