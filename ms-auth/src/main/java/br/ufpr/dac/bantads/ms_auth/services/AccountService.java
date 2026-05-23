package br.ufpr.dac.bantads.ms_auth.services;

import br.ufpr.dac.bantads.ms_auth.dtos.LoginRequestDTO;
import br.ufpr.dac.bantads.ms_auth.dtos.LoginResponseDTO;
import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${ms.cliente.url:http://ms-cliente:8081}")
    private String msClienteUrl;

    @Value("${ms.funcionario.url:http://ms-funcionario:8080}")
    private String msFuncionarioUrl;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public LoginResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email or password are incorrect"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Email or password are incorrect");
        }

        String token = jwtService.buildToken(account);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(token);
        response.setTipo(account.getRole());
        response.setUsuario(buscarDadosUsuario(account));
        return response;
    }

    private Map<String, Object> buscarDadosUsuario(Account account) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("email", account.getEmail());
        fallback.put("role", account.getRole().name());

        try {
            String url = account.getRole() == Role.CLIENTE
                    ? msClienteUrl + "/clientes"
                    : msFuncionarioUrl + "/gerentes";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                log.warn("composition login: {} retornou {}", url, resp.statusCode());
                return fallback;
            }

            JsonNode root = mapper.readTree(resp.body());
            if (!root.isArray()) {
                log.warn("composition login: resposta de {} não é array", url);
                return fallback;
            }
            for (JsonNode item : root) {
                if (account.getEmail().equalsIgnoreCase(item.path("email").asText(""))) {
                    return mapper.convertValue(item, Map.class);
                }
            }
            log.warn("composition login: nenhum match pra email={} em {}", account.getEmail(), url);
            return fallback;
        } catch (Exception e) {
            log.warn("composition login falhou: {}", e.getMessage());
            return fallback;
        }
    }
}
