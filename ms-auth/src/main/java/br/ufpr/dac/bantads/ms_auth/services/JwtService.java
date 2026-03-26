package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.models.Account;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long tokenExpiration;

    public String buildToken(Account account) {
        return JWT.create()
                .withSubject(account.getAccountId())
                .withClaim("role", account.getRole().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

}
