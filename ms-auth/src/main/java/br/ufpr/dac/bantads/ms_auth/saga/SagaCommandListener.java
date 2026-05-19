package br.ufpr.dac.bantads.ms_auth.saga;

import br.ufpr.dac.bantads.ms_auth.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import br.ufpr.dac.bantads.ms_auth.repositories.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    private final RabbitTemplate rabbitTemplate;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @RabbitListener(queues = RabbitConfig.CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    private SagaReply handle(SagaCommand cmd) {
        try {
            return switch (cmd.step()) {
                case "CRIAR_AUTH_GERENTE" -> criarAuth(cmd, Role.GERENTE);
                case "CRIAR_AUTH_CLIENTE" -> new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                case "REMOVER_AUTH_CLIENTE",
                     "REMOVER_AUTH_GERENTE" ->
                        new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    private SagaReply criarAuth(SagaCommand cmd, Role role) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String email = root.path("email").asText(null);
        String senha = root.path("senha").asText(null);

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                    "payload sem email ou senha");
        }

        if (accountRepository.findByEmail(email).isPresent()) {
            log.info("CRIAR_AUTH_{} idempotente: já existe account para {}", role, email);
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"criado\":false}", null);
        }

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(senha));
        account.setRole(role);
        accountRepository.save(account);

        return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"criado\":true}", null);
    }
}
