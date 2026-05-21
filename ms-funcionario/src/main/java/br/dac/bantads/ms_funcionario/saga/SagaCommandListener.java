package br.dac.bantads.ms_funcionario.saga;

import br.dac.bantads.ms_funcionario.config.RabbitConfig;
import br.dac.bantads.ms_funcionario.model.Funcionario;
import br.dac.bantads.ms_funcionario.model.Role;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// participa de SAGAs orquestradas pelo ms-saga: recebe comandos, executa step
// local e devolve reply pra exchange numa routing key conhecida pelo orquestrador.
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    private final RabbitTemplate rabbitTemplate;
    private final FuncionarioRepository funcionarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                case "BUSCAR_DADOS_GERENTE" -> buscarDadosGerente(cmd);
                case "VERIFICAR_ULTIMO_GERENTE" -> verificarUltimoGerente(cmd);
                case "REMOVER_GERENTE" -> removerGerente(cmd);
                case "INSERIR_GERENTE" ->
                        // ms-funcionario.create() já persistiu o registro síncrono antes
                        // de publicar saga.start.insercao_gerente — aqui é só ack
                        new SagaReply(cmd.sagaId(), cmd.step(), true, null, null);
                default ->
                        new SagaReply(cmd.sagaId(), cmd.step(), false, null, "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    // Lê "gerenteCpf" do payload (vem do step CONSULTAR_GERENTE_MENOS_CONTAS) ou
    // "cpf" diretamente. Retorna nome+email do gerente pro orquestrador propagar.
    private SagaReply buscarDadosGerente(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload() == null ? "{}" : cmd.payload());
        String cpf = root.path("gerenteCpf").asText(null);
        if (cpf == null || cpf.isBlank()) cpf = root.path("cpf").asText(null);

        if (cpf == null || cpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem cpf");
        }

        Optional<Funcionario> opt = funcionarioRepository.findByCpf(cpf);
        if (opt.isEmpty()) {
            log.info("BUSCAR_DADOS_GERENTE: cpf {} não encontrado, devolve dados vazios", cpf);
            return new SagaReply(cmd.sagaId(), cmd.step(), true,
                    objectMapper.writeValueAsString(Map.of("gerenteCpf", cpf, "nome", "", "email", "")),
                    null);
        }
        Funcionario f = opt.get();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gerenteCpf", f.getCpf());
        body.put("nome", f.getNome());
        body.put("email", f.getEmail());
        return new SagaReply(cmd.sagaId(), cmd.step(), true, objectMapper.writeValueAsString(body), null);
    }

    // R18: SAGA não pode remover o último gerente. Falha se count(GERENTE) <= 1.
    private SagaReply verificarUltimoGerente(SagaCommand cmd) throws Exception {
        long total = funcionarioRepository.findByRole(Role.GERENTE).size();
        if (total <= 1) {
            log.info("VERIFICAR_ULTIMO_GERENTE bloqueia: total atual = {}", total);
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                    "não é possível remover o último gerente");
        }
        return new SagaReply(cmd.sagaId(), cmd.step(), true,
                objectMapper.writeValueAsString(Map.of("totalGerentes", total)), null);
    }

    // R18: remove fisicamente o gerente. Pré-condição: contas já foram reatribuídas.
    @Transactional
    public SagaReply removerGerente(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload() == null ? "{}" : cmd.payload());
        String cpf = root.path("cpf").asText(null);
        if (cpf == null || cpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem cpf");
        }
        Optional<Funcionario> opt = funcionarioRepository.findByCpf(cpf);
        if (opt.isEmpty()) {
            // idempotência: se já removido, sucesso
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removido\":false}", null);
        }
        Funcionario f = opt.get();
        if (f.getRole() != Role.GERENTE) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                    "cpf " + cpf + " não é GERENTE: role=" + f.getRole());
        }
        String email = f.getEmail();
        funcionarioRepository.delete(f);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("removido", true);
        body.put("cpf", cpf);
        body.put("email", email);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, objectMapper.writeValueAsString(body), null);
    }
}
