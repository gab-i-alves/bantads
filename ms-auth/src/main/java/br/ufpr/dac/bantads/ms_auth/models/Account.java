package br.ufpr.dac.bantads.ms_auth.models;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "accounts")
public class Account {
    @Id
    private String accountId;
    private String password;
    private String email;
    private Role role;
}
