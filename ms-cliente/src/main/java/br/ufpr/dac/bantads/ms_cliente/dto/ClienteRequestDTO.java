package br.ufpr.dac.bantads.ms_cliente.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ClienteRequestDTO(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank String cpf,
    @NotBlank String telefone,
    @NotNull @Positive BigDecimal salario,
    @Valid @NotNull EnderecoDTO endereco
) {}
