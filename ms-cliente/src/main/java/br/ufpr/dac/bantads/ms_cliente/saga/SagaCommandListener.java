package br.ufpr.dac.bantads.ms_cliente.saga;

import br.ufpr.dac.bantads.ms_cliente.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_cliente.dto.ClienteRequestDTO;
import br.ufpr.dac.bantads.ms_cliente.dto.ClienteResponseDTO;
import br.ufpr.dac.bantads.ms_cliente.dto.RejeitarRequestDTO;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

// participa de SAGAs orquestradas pelo ms-saga: recebe comandos, executa o step
// local e devolve um reply pra mesma exchange numa routing key conhecida pelo orquestrador
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private final RabbitTemplate rabbitTemplate;
    private final ClienteService clienteService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, RabbitConfig.REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    // dispatcher por step — try/catch envolve tudo pra evitar requeue infinito quando
    // o ClienteService levantar exception (ex cliente não encontrado): a saga recebe
    // reply success=false e o orquestrador decide compensar
    private SagaReply handle(SagaCommand cmd) {
        try {
            return switch (cmd.step()) {
                case "CRIAR_CLIENTE" -> criarCliente(cmd);
                case "APROVAR_CLIENTE" -> aprovarCliente(cmd);
                case "REVERTER_APROVACAO" -> reverterAprovacao(cmd);
                case "REJEITAR_CLIENTE" -> rejeitarCliente(cmd);
                case "ATUALIZAR_CLIENTE" -> atualizarCliente(cmd);
                case "REVERTER_ATUALIZACAO_CLIENTE" -> reverterAtualizacao(cmd);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    private SagaReply criarCliente(SagaCommand cmd) throws Exception {
        ClienteRequestDTO dto = objectMapper.readValue(cmd.payload(), ClienteRequestDTO.class);
        ClienteResponseDTO criado = clienteService.criar(dto);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, serializeCliente(criado), null);
    }

    // R10: aprova e devolve dados do cliente (email + salário) pro orquestrador
    // encadear CRIAR_AUTH_CLIENTE e CRIAR_CONTA sem reconsultar
    private SagaReply aprovarCliente(SagaCommand cmd) throws Exception {
        String cpf = readCpf(cmd.payload());
        ClienteResponseDTO aprovado = clienteService.aprovar(cpf);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, serializeCliente(aprovado), null);
    }

    // compensação de APROVAR_CLIENTE: status volta pra PENDENTE quando saga R10 falha
    private SagaReply reverterAprovacao(SagaCommand cmd) throws Exception {
        String cpf = readCpf(cmd.payload());
        clienteService.reverterParaPendente(cpf);
        log.info("REVERTER_APROVACAO: cpf={} status=PENDENTE", cpf);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
    }

    private SagaReply rejeitarCliente(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String cpf = root.path("cpf").asText(null);
        String motivo = root.path("motivo").asText("");
        if (cpf == null || cpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem cpf");
        }
        clienteService.rejeitar(cpf, new RejeitarRequestDTO(motivo));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
    }

    // R4 (passo principal): aplica os dados novos. Orquestrador encadeia RECALCULAR_LIMITE
    // no ms-conta passando o salário novo no contexto. O reply leva tambem o perfil
    // ANTERIOR (campo "anterior") pra que o orquestrador possa compensar com
    // REVERTER_ATUALIZACAO_CLIENTE caso um step posterior da saga falhe.
    private SagaReply atualizarCliente(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String cpf = root.path("cpf").asText(null);
        if (cpf == null || cpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem cpf");
        }
        // snapshot do perfil corrente antes de sobrescrever (fonte da compensacao)
        ClienteResponseDTO anterior = clienteService.buscarPorCpf(cpf);
        ClienteRequestDTO dto = objectMapper.readValue(cmd.payload(), ClienteRequestDTO.class);
        ClienteResponseDTO atualizado = clienteService.atualizar(cpf, dto);

        Map<String, Object> body = clienteToMap(atualizado);
        body.put("anterior", clienteToMap(anterior));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, objectMapper.writeValueAsString(body), null);
    }

    // R4 (compensação): restaura o perfil anterior do cliente. O payload traz os
    // campos a restaurar (nome/email/salario/telefone/endereco) no formato do
    // ClienteRequestDTO, montado pelo orquestrador a partir do snapshot "anterior".
    private SagaReply reverterAtualizacao(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String cpf = root.path("cpf").asText(null);
        if (cpf == null || cpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem cpf");
        }
        ClienteRequestDTO anterior = objectMapper.readValue(cmd.payload(), ClienteRequestDTO.class);
        clienteService.reverterAtualizacao(cpf, anterior);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
    }

    private String readCpf(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload == null ? "{}" : payload);
        String cpf = root.path("cpf").asText(null);
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("payload sem campo cpf: " + payload);
        }
        return cpf;
    }

    private String serializeCliente(ClienteResponseDTO c) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cpf", c.cpf());
        body.put("nome", c.nome());
        body.put("email", c.email());
        body.put("salario", c.salario().toPlainString());
        body.put("status", c.status());
        return objectMapper.writeValueAsString(body);
    }

    // mapa com o perfil completo (inclui telefone/endereco), no formato que tanto o
    // orquestrador quanto o ClienteRequestDTO sabem ler — usado no fluxo R4 pra
    // carregar o snapshot "anterior" e depois remonta-lo na compensacao
    private Map<String, Object> clienteToMap(ClienteResponseDTO c) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cpf", c.cpf());
        body.put("nome", c.nome());
        body.put("email", c.email());
        body.put("telefone", c.telefone());
        body.put("salario", c.salario().toPlainString());
        body.put("status", c.status());

        Map<String, Object> end = new LinkedHashMap<>();
        end.put("logradouro", c.endereco().logradouro());
        end.put("numero", c.endereco().numero());
        end.put("complemento", c.endereco().complemento());
        end.put("cep", c.endereco().cep());
        end.put("cidade", c.endereco().cidade());
        end.put("estado", c.endereco().estado());
        body.put("endereco", end);
        return body;
    }
}
