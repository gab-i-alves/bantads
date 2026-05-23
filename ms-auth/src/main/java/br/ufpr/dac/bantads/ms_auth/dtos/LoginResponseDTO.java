package br.ufpr.dac.bantads.ms_auth.dtos;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class LoginResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private final String tokenType = "bearer";

    private Role tipo;

    private Map<String, Object> usuario;
}
