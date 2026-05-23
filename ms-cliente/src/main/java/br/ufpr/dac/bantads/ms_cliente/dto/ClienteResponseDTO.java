package br.ufpr.dac.bantads.ms_cliente.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClienteResponseDTO(
    Long id,
    String nome,
    String email,
    String cpf,
    String telefone,
    BigDecimal salario,
    String status,
    EnderecoDTO endereco,
    LocalDateTime dataDecisao,
    String motivoRejeicao
) {}
