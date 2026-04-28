package br.ufpr.dac.bantads.ms_cliente.saga;

import br.ufpr.dac.bantads.ms_cliente.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// participa de SAGAs orquestradas pelo ms-saga: recebe comandos, executa o step
// local e devolve um reply pra mesma exchange numa routing key conhecida pelo orquestrador
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    // routing key onde o ms-saga escuta replies (ele decide o nome da fila)
    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    // dispatcher por step — quando os steps reais forem implementados,
    // cada case chama o ClienteService correspondente (criar, aprovar, etc)
    private SagaReply handle(SagaCommand cmd) {
        return switch (cmd.step()) {
            // TODO: chamar clienteService.criar/aprovar/rejeitar quando o orquestrador estiver pronto
            case "CRIAR_CLIENTE", "APROVAR_CLIENTE", "REJEITAR_CLIENTE" ->
                    new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
            default ->
                    new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
        };
    }
}
