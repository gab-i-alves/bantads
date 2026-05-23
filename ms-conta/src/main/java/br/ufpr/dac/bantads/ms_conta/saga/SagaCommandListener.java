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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandListener {

    private static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";
    private static final BigDecimal LIMITE_MIN_SALARIO = new BigDecimal("2000");
    private static final BigDecimal DOIS = new BigDecimal("2");

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
                case "CONSULTAR_GERENTE_MAIS_CONTAS" -> consultarGerentePorContagem(cmd, true);
                case "CONSULTAR_GERENTE_MENOS_CONTAS" -> consultarGerentePorContagem(cmd, false);
                case "REATRIBUIR_CONTA" -> reatribuirConta(cmd);
                case "REATRIBUIR_TODAS_CONTAS" -> reatribuirTodasContas(cmd);
                case "CRIAR_CONTA" -> criarConta(cmd);
                case "REMOVER_CONTA" -> removerConta(cmd);
                case "RECALCULAR_LIMITE" -> recalcularLimite(cmd);
                default -> new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                        "step desconhecido: " + cmd.step());
            };
        } catch (Exception e) {
            log.warn("saga step falhou: sagaId={} step={} err={}", cmd.sagaId(), cmd.step(), e.getMessage());
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, e.getMessage());
        }
    }

    // ---------- Consultas ----------

    // Aceita campo opcional "excluirCpf" no payload pra ignorar um gerente
    // (usado em R18 — não pode escolher o gerente que está sendo removido)
    private SagaReply consultarGerentePorContagem(SagaCommand cmd, boolean mais) throws Exception {
        String excluirCpf = null;
        if (cmd.payload() != null && !cmd.payload().isBlank()) {
            JsonNode root = objectMapper.readTree(cmd.payload());
            excluirCpf = root.path("excluirCpf").asText(null);
        }
        String gerenteCpf = gerenteCpfPorContagem(mais, excluirCpf);
        String payload = objectMapper.writeValueAsString(
                Map.of("gerenteCpf", gerenteCpf == null ? "" : gerenteCpf));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }

    private String gerenteCpfPorContagem(boolean mais, String excluirCpf) {
        List<Conta> contasFiltradas = contaRepository.findAll().stream()
                .filter(c -> excluirCpf == null || !excluirCpf.equals(c.getGerenteCpf()))
                .toList();
        if (contasFiltradas.isEmpty()) return null;

        Map<String, Long> contagem = contasFiltradas.stream()
                .collect(Collectors.groupingBy(Conta::getGerenteCpf, Collectors.counting()));

        if (!mais) {
            return contagem.entrySet().stream()
                    .min(Comparator.comparing(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        long maxContas = contagem.values().stream().max(Long::compare).orElse(0L);
        List<String> candidatos = contagem.entrySet().stream()
                .filter(e -> e.getValue() == maxContas)
                .map(Map.Entry::getKey)
                .toList();
        if (candidatos.size() == 1) return candidatos.get(0);

        Map<String, BigDecimal> saldoPositivoPorGerente = contasFiltradas.stream()
                .filter(c -> candidatos.contains(c.getGerenteCpf()))
                .filter(c -> c.getSaldo().signum() >= 0)
                .collect(Collectors.groupingBy(
                        Conta::getGerenteCpf,
                        Collectors.reducing(BigDecimal.ZERO, Conta::getSaldo, BigDecimal::add)));

        return candidatos.stream()
                .min(Comparator.comparing(cpf -> saldoPositivoPorGerente.getOrDefault(cpf, BigDecimal.ZERO)))
                .orElse(candidatos.get(0));
    }

    // ---------- Reatribuição ----------

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

    // R18: move TODAS as contas do gerente removido pro novo gerente.
    // Retorna lista de números reatribuídos pra compensação eventual.
    @Transactional
    public SagaReply reatribuirTodasContas(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String cpfRemovido = root.path("gerenteRemovido").asText(null);
        String cpfDestino = root.path("gerenteDestino").path("gerenteCpf").asText(null);

        if (cpfRemovido == null || cpfRemovido.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem gerenteRemovido");
        }
        if (cpfDestino == null || cpfDestino.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem gerenteDestino");
        }

        List<Conta> contas = contaRepository.findByGerenteCpf(cpfRemovido);
        for (Conta c : contas) {
            c.setGerenteCpf(cpfDestino);
            contaRepository.save(c);
            contaReadRepository.findByNumero(c.getNumero()).ifPresent(cr -> {
                cr.setGerenteCpf(cpfDestino);
                contaReadRepository.save(cr);
            });
        }
        List<String> numeros = contas.stream().map(Conta::getNumero).toList();
        String payload = objectMapper.writeValueAsString(Map.of(
                "reatribuidas", numeros.size(),
                "numeros", numeros,
                "de", cpfRemovido,
                "para", cpfDestino
        ));
        log.info("REATRIBUIR_TODAS_CONTAS: {} contas {} → {}", numeros.size(), cpfRemovido, cpfDestino);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }

    // ---------- Criação / Remoção ----------

    // R10 (passo final do autocadastro): cria conta com saldo=0,
    // limite = salário/2 se salário ≥ 2000, senão 0.
    @Transactional
    public SagaReply criarConta(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String clienteCpf = root.path("clienteCpf").asText(null);
        String gerenteCpf = root.path("gerenteCpf").asText(null);
        BigDecimal salario = root.has("salario")
                ? new BigDecimal(root.get("salario").asText())
                : BigDecimal.ZERO;

        if (clienteCpf == null || clienteCpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem clienteCpf");
        }
        if (gerenteCpf == null || gerenteCpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null, "payload sem gerenteCpf");
        }

        // idempotência: se já existe conta pra esse cpf, retorna ok
        var existente = contaRepository.findByClienteCpf(clienteCpf);
        if (existente.isPresent()) {
            log.info("CRIAR_CONTA idempotente: já existe conta {} pra cpf {}", existente.get().getNumero(), clienteCpf);
            String payload = objectMapper.writeValueAsString(Map.of(
                    "criada", false,
                    "numero", existente.get().getNumero()));
            return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
        }

        BigDecimal limite = salario.compareTo(LIMITE_MIN_SALARIO) >= 0
                ? salario.divide(DOIS, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Conta conta = new Conta();
        conta.setNumero(gerarNumeroConta());
        conta.setClienteCpf(clienteCpf);
        conta.setSaldo(BigDecimal.ZERO);
        conta.setLimite(limite);
        conta.setGerenteCpf(gerenteCpf);
        conta.setDataCriacao(LocalDate.now());
        contaRepository.save(conta);

        // espelha no read-side direto — CRIAR_CONTA não é evento de movimentação
        ContaRead cr = new ContaRead();
        cr.setNumero(conta.getNumero());
        cr.setClienteCpf(conta.getClienteCpf());
        cr.setSaldo(conta.getSaldo());
        cr.setLimite(conta.getLimite());
        cr.setGerenteCpf(conta.getGerenteCpf());
        cr.setDataCriacao(conta.getDataCriacao());
        contaReadRepository.save(cr);

        log.info("CRIAR_CONTA: numero={} cpf={} gerente={} limite={}",
                conta.getNumero(), clienteCpf, gerenteCpf, limite);

        String payload = objectMapper.writeValueAsString(Map.of(
                "criada", true,
                "numero", conta.getNumero(),
                "limite", limite.toPlainString()));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }

    // Compensação de CRIAR_CONTA — usado quando a saga falha após criar conta.
    @Transactional
    public SagaReply removerConta(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String clienteCpf = root.path("clienteCpf").asText(null);
        if (clienteCpf == null || clienteCpf.isBlank()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removida\":false}", null);
        }
        var opt = contaRepository.findByClienteCpf(clienteCpf);
        if (opt.isEmpty()) {
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removida\":false}", null);
        }
        Conta c = opt.get();
        contaRepository.delete(c);
        contaReadRepository.findByNumero(c.getNumero()).ifPresent(contaReadRepository::delete);
        log.info("REMOVER_CONTA: numero={} cpf={}", c.getNumero(), clienteCpf);
        return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"removida\":true}", null);
    }

    // R4: cliente alterou salário → recalcula limite.
    // Regra: limite novo = salário ≥ 2000 ? salário/2 : 0
    //        mas se saldo < 0 → limite mínimo = |saldo| (não pode reduzir limite a ponto de exigir cobrir saldo negativo)
    @Transactional
    public SagaReply recalcularLimite(SagaCommand cmd) throws Exception {
        JsonNode root = objectMapper.readTree(cmd.payload());
        String clienteCpf = root.path("clienteCpf").asText(null);
        BigDecimal novoSalario = root.has("salario")
                ? new BigDecimal(root.get("salario").asText())
                : null;

        if (clienteCpf == null || clienteCpf.isBlank() || novoSalario == null) {
            return new SagaReply(cmd.sagaId(), cmd.step(), false, null,
                    "payload sem clienteCpf ou salario");
        }

        var opt = contaRepository.findByClienteCpf(clienteCpf);
        if (opt.isEmpty()) {
            log.info("RECALCULAR_LIMITE no-op: cliente {} sem conta ainda", clienteCpf);
            return new SagaReply(cmd.sagaId(), cmd.step(), true, "{\"atualizada\":false}", null);
        }
        Conta conta = opt.get();

        BigDecimal limiteCalculado = novoSalario.compareTo(LIMITE_MIN_SALARIO) >= 0
                ? novoSalario.divide(DOIS, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // saldo negativo dita limite mínimo (não rebaixa abaixo de |saldo|)
        BigDecimal saldo = conta.getSaldo();
        BigDecimal limiteFinal = saldo.signum() < 0 && saldo.abs().compareTo(limiteCalculado) > 0
                ? saldo.abs()
                : limiteCalculado;

        conta.setLimite(limiteFinal);
        contaRepository.save(conta);
        contaReadRepository.findByNumero(conta.getNumero()).ifPresent(cr -> {
            cr.setLimite(limiteFinal);
            contaReadRepository.save(cr);
        });

        log.info("RECALCULAR_LIMITE: cpf={} salario={} limiteCalc={} saldo={} limiteFinal={}",
                clienteCpf, novoSalario, limiteCalculado, saldo, limiteFinal);

        String payload = objectMapper.writeValueAsString(Map.of(
                "atualizada", true,
                "numero", conta.getNumero(),
                "limite", limiteFinal.toPlainString()));
        return new SagaReply(cmd.sagaId(), cmd.step(), true, payload, null);
    }

    // gera número de conta 4 dígitos com retry pra evitar colisão
    private String gerarNumeroConta() {
        for (int i = 0; i < 100; i++) {
            String numero = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10_000));
            if (contaRepository.findByNumero(numero).isEmpty()) {
                return numero;
            }
        }
        throw new IllegalStateException("não foi possível gerar número de conta único após 100 tentativas");
    }
}
