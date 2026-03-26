package br.ufpr.dac.bantads.ms_auth.dtos;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private final String tokenType = "bearer";
    private Role role;
    @JsonProperty("account_id")
    private String accountId;
}
