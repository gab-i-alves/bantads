package br.ufpr.dac.bantads.ms_conta.saga;

import br.ufpr.dac.bantads.ms_conta.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_conta.model.Conta;
import br.ufpr.dac.bantads.ms_conta.model.ContaRead;
import br.ufpr.dac.bantads.ms_conta.repository.ContaReadRepository;
import br.ufpr.dac.bantads.ms_conta.repository.ContaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    private final RabbitTemplate rabbitTemplate;
    private final ContaRepository contaRepository;
    private final ContaReadRepository contaReadRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.SAGA_CMD_QUEUE)
    public void onCommand(SagaCommand cmd) {
        log.info("saga cmd ← sagaId={} type={} step={}", cmd.sagaId(), cmd.sagaType(), cmd.step());
        SagaReply reply = handle(cmd);
        rabbitTemplate.convertAndSend(RabbitConfig.SAGA_EXCHANGE, REPLY_ROUTING_KEY, reply);
        log.info("saga reply → sagaId={} step={} success={}", reply.sagaId(), reply.step(), reply.success());
    }

    private SagaReply handle(SagaCommand cmd) {
        try {
            return switch (cmd.step()) {
                case "CONSULTAR_GERENTE_MAIS_CONTAS" -> consultarGerenteMaisContas(cmd);
                case "CONSULTAR_GERENTE_MENOS_CONTAS" -> consultarGerenteMenosContas(cmd);
                case "REATRIBUIR_CONTA" -> reatribuirConta(cmd);
                default -> new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                        "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    private SagaReply consultarGerenteMaisContas(SagaCommand cmd) throws Exception {
        return reply(cmd, gerenteCpfPorContagem(true));
    }

    private SagaReply consultarGerenteMenosContas(SagaCommand cmd) throws Exception {
        return reply(cmd, gerenteCpfPorContagem(false));
    }

    private SagaReply reply(SagaCommand cmd, String gerenteCpf) throws Exception {
        String payload = objectMapper.writeValueAsString(
                Map.of("gerenteCpf", gerenteCpf == null ? "" : gerenteCpf));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }

    // mais=true -> gerente com mais contas; mais=false -> com menos
    private String gerenteCpfPorContagem(boolean mais) {
        Map<String, Long> contagem = contaRepository.findAll().stream()
                .collect(Collectors.groupingBy(Conta::getGerenteCpf, Collectors.counting()));
        if (contagem.isEmpty()) {
            return null;
        }
        Comparator<Map.Entry<String, Long>> cmp = Comparator.comparing(Map.Entry::getValue);
        return contagem.entrySet().stream()
                .sorted(mais ? cmp.reversed() : cmp)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Transactional
    public SagaReply reatribuirConta(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String cpfNovo = root.path("gerenteNovo").path("cpf").asText(null);
        String cpfOrigem = root.path("gerenteOrigem").path("gerenteCpf").asText(null);

        if (cpfNovo == null || cpfNovo.isBlank() || cpfOrigem == null || cpfOrigem.isBlank()) {
            log.info("REATRIBUIR_CONTA no-op: cpfNovo={} cpfOrigem={}", cpfNovo, cpfOrigem);
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"reatribuida\":false}", null);
        }

        List<Conta> contasOrigem = contaRepository.findByGerenteCpf(cpfOrigem);
        if (contasOrigem.size() <= 1) {
            // R17: se gerente origem tem só 1 conta, novo gerente fica sem contas
            log.info("REATRIBUIR_CONTA no-op: origem {} tem {} conta(s)", cpfOrigem, contasOrigem.size());
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"reatribuida\":false}", null);
        }

        Conta alvo = contasOrigem.get(0);
        alvo.setGerenteCpf(cpfNovo);
        contaRepository.save(alvo);

        // espelha no read-side direto (caminho normal do CQRS seria via evento; saga é fluxo paralelo)
        contaReadRepository.findByNumero(alvo.getNumero()).ifPresent(cr -> {
            cr.setGerenteCpf(cpfNovo);
            contaReadRepository.save(cr);
        });

        String payload = objectMapper.writeValueAsString(Map.of(
                "reatribuida", true,
                "numero", alvo.getNumero(),
                "de", cpfOrigem,
                "para", cpfNovo
        ));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }
}
