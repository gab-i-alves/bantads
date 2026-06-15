package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.models.Account;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long tokenExpiration;

    public String buildToken(Account account) {
        return buildToken(account, null);
    }

    public String buildToken(Account account, String cpf) {
        // o gateway extrai o cpf do JWT pra autorizar chamadas downstream;
        // quando o cpf não pôde ser resolvido no login, a claim é omitida em vez
        // de gravar valor vazio pra não confundir quem lê o token.
        // jti único por token: dois logins no mesmo segundo gerariam o mesmo JWT
        // (iat/exp em segundos), e aí o logout de um invalidaria o outro no gateway.
        var builder = JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(account.getAccountId())
                .withClaim("role", account.getRole().toString())
                .withClaim("email", account.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration));

        if (cpf != null && !cpf.isBlank()) {
            builder.withClaim("cpf", cpf);
        }

        return builder.sign(Algorithm.HMAC256(secret));
    }

}
