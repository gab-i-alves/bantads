package br.ufpr.dac.bantads.ms_auth.saga;

import br.ufpr.dac.bantads.ms_auth.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import br.ufpr.dac.bantads.ms_auth.services.PasswordHasher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String SENHA_CHARSET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int SENHA_LEN = 8;

    private final RabbitTemplate rabbitTemplate;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom random = new SecureRandom();

    @RabbitListener(queues = RabbitConfig.CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, RabbitConfig.REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    // R20: troca de email/senha de gerente vinda do ms-funcionario (fora da saga).
    // O Account é localizado por email, então o payload traz o email antigo pra
    // achar a conta, mais o email novo e/ou a senha nova. Falha aqui é só logada:
    // a edição do gerente no ms-funcionario já aconteceu.
    @RabbitListener(queues = RabbitConfig.AUTH_UPDATE_CREDENCIAIS_QUEUE)
    public void onUpdateCredenciais(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String emailAntigo = root.path("emailAntigo").asText(null);
            String emailNovo = root.path("email").asText(null);
            String senha = root.path("senha").asText(null);

            if (emailAntigo == null || emailAntigo.isBlank()) {
                log.warn("update credenciais sem emailAntigo, ignorando");
                return;
            }
            Optional<Account> opt = accountRepository.findByEmail(emailAntigo);
            if (opt.isEmpty()) {
                log.warn("update credenciais: nenhuma account para email={}", emailAntigo);
                return;
            }
            Account account = opt.get();
            if (emailNovo != null && !emailNovo.isBlank()) {
                account.setEmail(emailNovo);
            }
            if (senha != null && !senha.isBlank()) {
                // novo salt + hash SHA256+SALT (NF11), mesmo esquema do login/seed.
                String salt = PasswordHasher.gerarSalt();
                account.setSalt(salt);
                account.setPassword(PasswordHasher.hash(senha, salt));
            }
            accountRepository.save(account);
            log.info("R20 credenciais atualizadas: {} -> email={}", emailAntigo, account.getEmail());
        } catch (Exception e) {
            log.warn("falha ao atualizar credenciais (R20): {}", e.getMessage());
        }
    }

    private SagaReply handle(SagaCommand cmd) {
        try {
            return switch (cmd.step()) {
                case "CRIAR_AUTH_GERENTE" -> criarAuthComSenhaInformada(cmd, Role.GERENTE);
                case "CRIAR_AUTH_CLIENTE" -> criarAuthCliente(cmd);
                case "REMOVER_AUTH_CLIENTE",
                     "REMOVER_AUTH_GERENTE" -> removerAuth(cmd);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    // R17: senha do gerente vem do request (admin define ao criar)
    private SagaReply criarAuthComSenhaInformada(SagaCommand cmd, Role role) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String email = root.path("email").asText(null);
        String senha = root.path("senha").asText(null);

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                    "payload sem email ou senha");
        }
        return persistirAuth(cmd, email, senha, role, false);
    }

    // R10: senha aleatória gerada pelo ms-auth; orquestrador propaga pro mock de e-mail
    private SagaReply criarAuthCliente(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String email = root.path("email").asText(null);
        if (email == null || email.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem email");
        }
        String senha = gerarSenha();
        return persistirAuth(cmd, email, senha, Role.CLIENTE, true);
    }

    private SagaReply persistirAuth(SagaCommand cmd, String email, String senhaClara,
                                    Role role, boolean expoeSenhaNoReply) throws Exception {
        Optional<Account> existente = accountRepository.findByEmail(email);
        if (existente.isPresent()) {
            log.info("CRIAR_AUTH_{} idempotente: já existe account para {}", role, email);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("criado", false);
            body.put("email", email);
            return new SagaReply(cmd.sagaId(), cmd.step(), true,
                    objectMapper.writeValueAsString(body), null);
        }

        Account account = new Account();
        account.setEmail(email);
        // salt novo + hash SHA256+SALT (NF11), mesmo esquema do seed e do login.
        String salt = PasswordHasher.gerarSalt();
        account.setSalt(salt);
        account.setPassword(PasswordHasher.hash(senhaClara, salt));
        account.setRole(role);
        accountRepository.save(account);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("criado", true);
        body.put("email", email);
        if (expoeSenhaNoReply) {
            // Senha em claro só vai pro orquestrador montar o mock de e-mail (R10).
            // NÃO persistida fora da memória do RabbitMQ; pra produção precisa de
            // um canal mais seguro (Outbox pattern + serviço de notificação).
            body.put("senhaTemporaria", senhaClara);
        }
        return new SagaReply(cmd.sagaId(), cmd.step(), true,
                objectMapper.writeValueAsString(body), null);
    }

    // Compensação: remove account quando saga falha após criação.
    private SagaReply removerAuth(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload() == null ? "{}" : cmd.payload());
        String email = root.path("email").asText(null);
        if (email == null || email.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removido\":false}", null);
        }
        Optional<Account> opt = accountRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removido\":false}", null);
        }
        accountRepository.delete(opt.get());
        log.info("{}: email={}", cmd.step(), email);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removido\":true}", null);
    }

    private String gerarSenha() {
        StringBuilder sb = new StringBuilder(SENHA_LEN);
        for (int i = 0; i < SENHA_LEN; i++) {
            sb.append(SENHA_CHARSET.charAt(random.nextInt(SENHA_CHARSET.length())));
        }
        return sb.toString();
    }
}
