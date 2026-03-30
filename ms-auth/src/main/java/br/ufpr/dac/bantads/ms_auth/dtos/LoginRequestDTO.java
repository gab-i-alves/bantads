package br.ufpr.dac.bantads.ms_auth.dtos;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String email;
    private String password;
}
