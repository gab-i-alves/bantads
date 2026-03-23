package br.ufpr.dac.bantads.ms_cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
    @NotBlank String logradouro,
    @NotBlank String numero,
    String complemento,
    @NotBlank @Size(min = 8, max = 8) String cep,
    @NotBlank String cidade,
    @NotBlank @Size(min = 2, max = 2) String estado
) {}
