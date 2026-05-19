package br.dac.bantads.ms_saga.saga;

import br.dac.bantads.ms_saga.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final RabbitTemplate rabbit;

    private final Map<String, SagaState> sagas = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitConfig.START_AUTOCADASTRO_QUEUE)
    public void onStartAutocadastro(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga AUTOCADASTRO iniciada sagaId={} payload={}", sagaId, payload);

        SagaState state = SagaState.start("AUTOCADASTRO", payload);
        sagas.put(sagaId, state);

        avancarStep(sagaId, "CONSULTAR_GERENTE_MENOS_CONTAS", RabbitConfig.CMD_CONTA_ROUTING_KEY, payload);
    }

    @RabbitListener(queues = RabbitConfig.START_APROVACAO_QUEUE)
    public void onStartAprovacao(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga APROVACAO iniciada sagaId={} payload={}", sagaId, payload);
    }

    @RabbitListener(queues = RabbitConfig.START_INSERCAO_GERENTE_QUEUE)
    public void onStartInsercaoGerente(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga INSERCAO_GERENTE iniciada sagaId={} payload={}", sagaId, payload);

        SagaState state = SagaState.start("INSERCAO_GERENTE", payload);
        sagas.put(sagaId, state);

        avancarStep(sagaId, "CONSULTAR_GERENTE_MAIS_CONTAS", RabbitConfig.CMD_CONTA_ROUTING_KEY, payload);
    }

    @RabbitListener(queues = RabbitConfig.REPLY_QUEUE)
    public void onReply(SagaReply reply) {
        log.info("saga reply ← sagaId={} step={} success={} error={}",
                reply.sagaId(), reply.step(), reply.success(), reply.error());

        SagaState state = sagas.get(reply.sagaId());
        if (state == null) {
            log.warn("reply para sagaId desconhecido: {}", reply.sagaId());
            return;
        }

        if (!reply.success()) {
            log.warn("step {} falhou — saga {} entra em compensacao", reply.step(), reply.sagaId());
            state.setStatus("FAILED");
            return;
        }

        state.getStepsCompleted().add(reply.step());

        switch (state.getType()) {
            case "AUTOCADASTRO" -> avancarAutocadastro(reply, state);
            case "INSERCAO_GERENTE" -> avancarInsercaoGerente(reply, state);
            default -> log.warn("tipo de saga desconhecido: {}", state.getType());
        }
    }

    private void avancarInsercaoGerente(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "CONSULTAR_GERENTE_MAIS_CONTAS" -> {
                state.getContext().put("gerenteMaisContas", reply.payload());
                avancarStep(sagaId, "CRIAR_AUTH_GERENTE", RabbitConfig.CMD_AUTH_ROUTING_KEY, state.getStartPayload());
            }
            case "CRIAR_AUTH_GERENTE" ->
                    avancarStep(sagaId, "REATRIBUIR_CONTA", RabbitConfig.CMD_CONTA_ROUTING_KEY,
                            montarPayloadReatribuir(state));
            case "REATRIBUIR_CONTA" -> {
                state.setStatus("COMPLETED");
                log.info("saga INSERCAO_GERENTE {} concluida", sagaId);
            }
            default -> log.warn("step desconhecido em INSERCAO_GERENTE: {}", reply.step());
        }
    }

    private String montarPayloadReatribuir(SagaState state) {
        return "{\"gerenteNovo\":" + state.getStartPayload()
                + ",\"gerenteOrigem\":" + state.getContext().get("gerenteMaisContas") + "}";
    }

    private void avancarAutocadastro(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "CONSULTAR_GERENTE_MENOS_CONTAS" -> {
                state.getContext().put("replyConsultaGerente", reply.payload());
                avancarStep(sagaId, "BUSCAR_DADOS_GERENTE", RabbitConfig.CMD_FUNCIONARIO_ROUTING_KEY, reply.payload());
            }
            case "BUSCAR_DADOS_GERENTE" -> {
                state.getContext().put("dadosGerente", reply.payload());
                avancarStep(sagaId, "VINCULAR_GERENTE", RabbitConfig.CMD_CLIENTE_ROUTING_KEY,
                        montarPayloadVincular(state));
            }
            case "VINCULAR_GERENTE" ->
                    avancarStep(sagaId, "CRIAR_AUTH_CLIENTE", RabbitConfig.CMD_AUTH_ROUTING_KEY, state.getStartPayload());
            case "CRIAR_AUTH_CLIENTE" -> {
                state.setStatus("COMPLETED");
                log.info("saga AUTOCADASTRO {} concluida", sagaId);
            }
            default -> log.warn("step desconhecido em AUTOCADASTRO: {}", reply.step());
        }
    }

    private String montarPayloadVincular(SagaState state) {
        return "{\"cliente\":" + state.getStartPayload()
                + ",\"gerente\":" + state.getContext().get("replyConsultaGerente") + "}";
    }

    private void avancarStep(String sagaId, String step, String routingKey, String payload) {
        SagaState state = sagas.get(sagaId);
        state.setCurrentStep(step);

        SagaCommand cmd = new SagaCommand(sagaId, state.getType(), step, payload);
        rabbit.convertAndSend(RabbitConfig.SAGA_EXCHANGE, routingKey, cmd);
        log.info("saga cmd → sagaId={} step={} routingKey={}", sagaId, step, routingKey);
    }
}
