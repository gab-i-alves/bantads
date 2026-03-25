package br.ufpr.dac.bantads.ms_auth.dtos;

import br.ufpr.dac.bantads.ms_auth.enums.Role;

public class LoginResponseDTO {
    private String accessToken;
    private final String tokenType = "bearer";
    private Role role;
    private String accountId;
}
