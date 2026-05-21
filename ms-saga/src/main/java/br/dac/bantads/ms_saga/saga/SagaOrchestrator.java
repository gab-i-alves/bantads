package br.dac.bantads.ms_saga.saga;

import br.dac.bantads.ms_saga.config.RabbitConfig;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper = new ObjectMapper();

    // estado in-memory: simples e suficiente pro escopo do projeto.
    // Pra produção: tabela saga_log no Postgres (sobrevive restart, permite recovery).
    private final Map<String, SagaState> sagas = new ConcurrentHashMap<>();

    // =========================================================================
    // STARTERS — cada SAGA tem uma fila de entrada própria
    // =========================================================================

    @RabbitListener(queues = RabbitConfig.START_AUTOCADASTRO_QUEUE)
    public void onStartAutocadastro(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga AUTOCADASTRO iniciada sagaId={} payload={}", sagaId, payload);

        sagas.put(sagaId, SagaState.start("AUTOCADASTRO", payload));
        // R10: cliente já existe (PENDENTE). Aprovação é o primeiro passo da saga.
        avancarStep(sagaId, "APROVAR_CLIENTE", RabbitConfig.CMD_CLIENTE_ROUTING_KEY, payload);
    }

    @RabbitListener(queues = RabbitConfig.START_APROVACAO_QUEUE)
    public void onStartAprovacao(String payload) {
        // alias da AUTOCADASTRO — alguns frontends podem chamar essa fila;
        // mantido só por compat. O fluxo real é o mesmo.
        onStartAutocadastro(payload);
    }

    @RabbitListener(queues = RabbitConfig.START_INSERCAO_GERENTE_QUEUE)
    public void onStartInsercaoGerente(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga INSERCAO_GERENTE iniciada sagaId={} payload={}", sagaId, payload);

        sagas.put(sagaId, SagaState.start("INSERCAO_GERENTE", payload));
        avancarStep(sagaId, "CONSULTAR_GERENTE_MAIS_CONTAS",
                RabbitConfig.CMD_CONTA_ROUTING_KEY, payload);
    }

    @RabbitListener(queues = RabbitConfig.START_ALTERACAO_PERFIL_QUEUE)
    public void onStartAlteracaoPerfil(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga ALTERACAO_PERFIL iniciada sagaId={} payload={}", sagaId, payload);

        sagas.put(sagaId, SagaState.start("ALTERACAO_PERFIL", payload));
        avancarStep(sagaId, "ATUALIZAR_CLIENTE", RabbitConfig.CMD_CLIENTE_ROUTING_KEY, payload);
    }

    @RabbitListener(queues = RabbitConfig.START_REMOCAO_GERENTE_QUEUE)
    public void onStartRemocaoGerente(String payload) {
        String sagaId = UUID.randomUUID().toString();
        log.info("saga REMOCAO_GERENTE iniciada sagaId={} payload={}", sagaId, payload);

        sagas.put(sagaId, SagaState.start("REMOCAO_GERENTE", payload));
        // R18: gate antes de tudo — não deixa remover último gerente.
        avancarStep(sagaId, "VERIFICAR_ULTIMO_GERENTE",
                RabbitConfig.CMD_FUNCIONARIO_ROUTING_KEY, payload);
    }

    // =========================================================================
    // REPLY ROUTER
    // =========================================================================

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
            log.warn("step {} falhou em saga {} ({}) — compensando", reply.step(),
                    reply.sagaId(), state.getType());
            state.setStatus("FAILED");
            compensar(reply.sagaId(), state, reply.step(), reply.error());
            return;
        }

        // sucesso: marca step e dispara próximo
        state.getStepsCompleted().add(reply.step());

        switch (state.getType()) {
            case "AUTOCADASTRO" -> avancarAutocadastro(reply, state);
            case "INSERCAO_GERENTE" -> avancarInsercaoGerente(reply, state);
            case "ALTERACAO_PERFIL" -> avancarAlteracaoPerfil(reply, state);
            case "REMOCAO_GERENTE" -> avancarRemocaoGerente(reply, state);
            default -> log.warn("tipo de saga desconhecido: {}", state.getType());
        }
    }

    // =========================================================================
    // R10 — Autocadastro (aprovação): APROVAR → CONSULTAR_GER → BUSCAR_DADOS → AUTH → CONTA
    // =========================================================================

    private void avancarAutocadastro(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "APROVAR_CLIENTE" -> {
                // payload do reply: {cpf,nome,email,salario,status}
                state.getContext().put("cliente", reply.payload());
                // payload vazio aqui — ms-conta varre todas as contas
                avancarStep(sagaId, "CONSULTAR_GERENTE_MENOS_CONTAS",
                        RabbitConfig.CMD_CONTA_ROUTING_KEY, "{}");
            }
            case "CONSULTAR_GERENTE_MENOS_CONTAS" -> {
                state.getContext().put("gerenteEscolhido", reply.payload());
                // próximo step só precisa do cpf do gerente — passa o próprio reply
                avancarStep(sagaId, "BUSCAR_DADOS_GERENTE",
                        RabbitConfig.CMD_FUNCIONARIO_ROUTING_KEY, reply.payload());
            }
            case "BUSCAR_DADOS_GERENTE" -> {
                state.getContext().put("gerenteDados", reply.payload());
                // CRIAR_AUTH_CLIENTE recebe email do cliente (do startPayload do step APROVAR)
                String emailCliente = readString(state.getContext().get("cliente"), "email");
                avancarStep(sagaId, "CRIAR_AUTH_CLIENTE",
                        RabbitConfig.CMD_AUTH_ROUTING_KEY,
                        json(Map.of("email", emailCliente)));
            }
            case "CRIAR_AUTH_CLIENTE" -> {
                state.getContext().put("authCliente", reply.payload());
                // CRIAR_CONTA: cpf cliente, salario, gerenteCpf
                String cpf = readString(state.getContext().get("cliente"), "cpf");
                String salario = readString(state.getContext().get("cliente"), "salario");
                String gerenteCpf = readString(state.getContext().get("gerenteEscolhido"), "gerenteCpf");
                avancarStep(sagaId, "CRIAR_CONTA", RabbitConfig.CMD_CONTA_ROUTING_KEY,
                        json(Map.of("clienteCpf", cpf, "salario", salario, "gerenteCpf", gerenteCpf)));
            }
            case "CRIAR_CONTA" -> {
                state.setStatus("COMPLETED");
                // R10 final: dispara mock de envio de e-mail com a senha gerada
                String email = readString(state.getContext().get("cliente"), "email");
                String senha = readString(state.getContext().get("authCliente"), "senhaTemporaria");
                String numero = readString(reply.payload(), "numero");
                log.info("[MOCK EMAIL] R10 cadastro aprovado → para={} | conta={} | senha={}",
                        email, numero, senha);
                log.info("saga AUTOCADASTRO {} concluída (cliente={}, conta={})",
                        sagaId, email, numero);
            }
            default -> log.warn("step desconhecido em AUTOCADASTRO: {}", reply.step());
        }
    }

    // Compensação AUTOCADASTRO — ordem reversa do que já completou
    private void compensarAutocadastro(String sagaId, SagaState state, String stepFalhou) {
        // REMOVER_CONTA: só se CRIAR_CONTA já tinha completado (não é o caso quando falha nele mesmo)
        if (state.getStepsCompleted().contains("CRIAR_CONTA")) {
            String cpf = readString(state.getContext().get("cliente"), "cpf");
            avancarStep(sagaId, "REMOVER_CONTA", RabbitConfig.CMD_CONTA_ROUTING_KEY,
                    json(Map.of("clienteCpf", cpf)));
        }
        if (state.getStepsCompleted().contains("CRIAR_AUTH_CLIENTE")) {
            String email = readString(state.getContext().get("cliente"), "email");
            avancarStep(sagaId, "REMOVER_AUTH_CLIENTE", RabbitConfig.CMD_AUTH_ROUTING_KEY,
                    json(Map.of("email", email)));
        }
        if (state.getStepsCompleted().contains("APROVAR_CLIENTE")) {
            String cpf = readString(state.getContext().get("cliente"), "cpf");
            avancarStep(sagaId, "REVERTER_APROVACAO", RabbitConfig.CMD_CLIENTE_ROUTING_KEY,
                    json(Map.of("cpf", cpf)));
        }
    }

    // =========================================================================
    // R17 — Inserção de Gerente (já implementado, mantido)
    // =========================================================================

    private void avancarInsercaoGerente(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "CONSULTAR_GERENTE_MAIS_CONTAS" -> {
                state.getContext().put("gerenteMaisContas", reply.payload());
                avancarStep(sagaId, "CRIAR_AUTH_GERENTE", RabbitConfig.CMD_AUTH_ROUTING_KEY,
                        state.getStartPayload());
            }
            case "CRIAR_AUTH_GERENTE" ->
                    avancarStep(sagaId, "REATRIBUIR_CONTA", RabbitConfig.CMD_CONTA_ROUTING_KEY,
                            montarPayloadReatribuir(state));
            case "REATRIBUIR_CONTA" -> {
                state.setStatus("COMPLETED");
                log.info("saga INSERCAO_GERENTE {} concluída", sagaId);
            }
            default -> log.warn("step desconhecido em INSERCAO_GERENTE: {}", reply.step());
        }
    }

    private String montarPayloadReatribuir(SagaState state) {
        return "{\"gerenteNovo\":" + state.getStartPayload()
                + ",\"gerenteOrigem\":" + state.getContext().get("gerenteMaisContas") + "}";
    }

    // =========================================================================
    // R4 — Alteração de Perfil: ATUALIZAR_CLIENTE → RECALCULAR_LIMITE
    // =========================================================================

    private void avancarAlteracaoPerfil(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "ATUALIZAR_CLIENTE" -> {
                state.getContext().put("cliente", reply.payload());
                String cpf = readString(reply.payload(), "cpf");
                String salario = readString(reply.payload(), "salario");
                avancarStep(sagaId, "RECALCULAR_LIMITE", RabbitConfig.CMD_CONTA_ROUTING_KEY,
                        json(Map.of("clienteCpf", cpf, "salario", salario)));
            }
            case "RECALCULAR_LIMITE" -> {
                state.setStatus("COMPLETED");
                log.info("saga ALTERACAO_PERFIL {} concluída", sagaId);
            }
            default -> log.warn("step desconhecido em ALTERACAO_PERFIL: {}", reply.step());
        }
    }

    // =========================================================================
    // R18 — Remoção de Gerente: VERIFICAR → CONSULTAR_DESTINO → REATRIBUIR → REMOVER → REMOVER_AUTH
    // =========================================================================

    private void avancarRemocaoGerente(SagaReply reply, SagaState state) {
        String sagaId = reply.sagaId();
        switch (reply.step()) {
            case "VERIFICAR_ULTIMO_GERENTE" -> {
                // payload do start: {cpf:"..."} — esse cpf vai ser excluído da consulta de destino
                String cpfRemovido = readString(state.getStartPayload(), "cpf");
                state.getContext().put("cpfRemovido", cpfRemovido);
                avancarStep(sagaId, "CONSULTAR_GERENTE_MENOS_CONTAS",
                        RabbitConfig.CMD_CONTA_ROUTING_KEY,
                        json(Map.of("excluirCpf", cpfRemovido)));
            }
            case "CONSULTAR_GERENTE_MENOS_CONTAS" -> {
                state.getContext().put("gerenteDestino", reply.payload());
                String cpfRemovido = (String) state.getContext().get("cpfRemovido");
                ObjectNode payload = mapper.createObjectNode();
                payload.put("gerenteRemovido", cpfRemovido);
                try {
                    payload.set("gerenteDestino", mapper.readTree(reply.payload()));
                } catch (Exception e) {
                    log.warn("falha parse gerenteDestino: {}", e.getMessage());
                }
                avancarStep(sagaId, "REATRIBUIR_TODAS_CONTAS",
                        RabbitConfig.CMD_CONTA_ROUTING_KEY, payload.toString());
            }
            case "REATRIBUIR_TODAS_CONTAS" -> {
                String cpfRemovido = (String) state.getContext().get("cpfRemovido");
                avancarStep(sagaId, "REMOVER_GERENTE",
                        RabbitConfig.CMD_FUNCIONARIO_ROUTING_KEY,
                        json(Map.of("cpf", cpfRemovido)));
            }
            case "REMOVER_GERENTE" -> {
                state.getContext().put("gerenteRemovido", reply.payload());
                String email = readString(reply.payload(), "email");
                avancarStep(sagaId, "REMOVER_AUTH_GERENTE",
                        RabbitConfig.CMD_AUTH_ROUTING_KEY,
                        json(Map.of("email", email)));
            }
            case "REMOVER_AUTH_GERENTE" -> {
                state.setStatus("COMPLETED");
                log.info("saga REMOCAO_GERENTE {} concluída", sagaId);
            }
            default -> log.warn("step desconhecido em REMOCAO_GERENTE: {}", reply.step());
        }
    }

    // =========================================================================
    // COMPENSAÇÃO — dispatcher por tipo de saga
    // =========================================================================

    private void compensar(String sagaId, SagaState state, String stepFalhou, String erro) {
        switch (state.getType()) {
            case "AUTOCADASTRO" -> compensarAutocadastro(sagaId, state, stepFalhou);
            // R17, R4, R18: compensação no projeto acadêmico é só log — esses fluxos
            // não introduzem efeito colateral persistido antes do step que falha
            // (R17 só persiste auth+reatribuição no final; R4 atualiza cliente mas
            // recalcular é idempotente; R18 falha antes de modificar se VERIFICAR falhar)
            default -> log.warn("saga {} ({}) falhou em {} sem compensação automática: {}",
                    sagaId, state.getType(), stepFalhou, erro);
        }
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private void avancarStep(String sagaId, String step, String routingKey, String payload) {
        SagaState state = sagas.get(sagaId);
        state.setCurrentStep(step);

        SagaCommand cmd = new SagaCommand(sagaId, state.getType(), step, payload);
        rabbit.convertAndSend(RabbitConfig.SAGA_EXCHANGE, routingKey, cmd);
        log.info("saga cmd → sagaId={} step={} routingKey={}", sagaId, step, routingKey);
    }

    private String json(Map<String, ?> body) {
        try {
            return mapper.writeValueAsString(new LinkedHashMap<>(body));
        } catch (Exception e) {
            throw new RuntimeException("falha serializando payload da saga", e);
        }
    }

    // Lê um campo string de um payload JSON (ou de um Object guardado no context que
    // pode ser String JSON). Retorna "" se não encontrar — caller decide se isso é erro.
    private String readString(Object source, String field) {
        if (source == null) return "";
        try {
            JsonNode root = source instanceof String s
                    ? mapper.readTree(s)
                    : mapper.valueToTree(source);
            return root.path(field).asText("");
        } catch (Exception e) {
            log.warn("falha lendo campo {} de payload: {}", field, e.getMessage());
            return "";
        }
    }
}
