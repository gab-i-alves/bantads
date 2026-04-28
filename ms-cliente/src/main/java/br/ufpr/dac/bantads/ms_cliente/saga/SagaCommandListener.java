package br.ufpr.dac.bantads.ms_cliente.saga;

import br.ufpr.dac.bantads.ms_cliente.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// participa de SAGAs orquestradas pelo ms-saga: recebe comandos, executa o step
// local e devolve um reply pra mesma exchange numa routing key conhecida pelo orquestrador
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    // routing key onde o ms-saga escuta replies (ele decide o nome da fila)
    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    // extrai "cpf" de um JSON simples — evita pull de jackson-databind explicitamente
    private static final Pattern CPF_PATTERN = Pattern.compile("\"cpf\"\\s*:\\s*\"([^\"]+)\"");

    private final RabbitTemplate rabbitTemplate;
    private final ClienteService clienteService;

    @RabbitListener(queues = RabbitConfig.CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    // dispatcher por step — try/catch envolve tudo pra evitar requeue infinito quando
    // o ClienteService levantar exception (ex cliente nao encontrado): a saga recebe
    // um reply success=false e o orquestrador decide compensar
    private SagaReply handle(SagaCommand cmd) {
        try {
            return switch (cmd.step()) {
                case "APROVAR_CLIENTE" -> {
                    clienteService.aprovar(readCpf(cmd.payload()));
                    yield new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                }
                // TODO: plugar criar() e rejeitar() quando os payloads estiverem definidos
                case "CRIAR_CLIENTE", "REJEITAR_CLIENTE" ->
                        new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    // payload e JSON livre — pra steps que precisam so do cpf, le esse campo direto
    private String readCpf(String payload) {
        Matcher m = CPF_PATTERN.matcher(payload == null ? "" : payload);
        if (!m.find()) throw new IllegalArgumentException("payload sem campo cpf: " + payload);
        return m.group(1);
    }
}
