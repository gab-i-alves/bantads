package br.dac.bantads.ms_funcionario.saga;

import br.dac.bantads.ms_funcionario.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// participa de SAGAs orquestradas pelo ms-saga: recebe comandos, executa step
// local e devolve reply pra exchange numa routing key conhecida pelo orquestrador.
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    private final RabbitTemplate rabbitTemplate;

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
                case "CONSULTAR_GERENTE_MENOS_CLIENTES",
                     "CONSULTAR_GERENTE_MAIS_CLIENTES",
                     "INSERIR_GERENTE",
                     "REMOVER_GERENTE" ->
                        new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }
}
