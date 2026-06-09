package br.ufpr.dac.bantads.ms_cliente.saga;

import br.ufpr.dac.bantads.ms_cliente.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_cliente.dto.ClienteRequestDTO;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

// Publica eventos START das SAGAs orquestradas pelo ms-saga.
// Vive no ms-cliente porque os triggers HTTP (POST /aprovar, PUT /clientes) moram aqui.
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaPublisher {

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    // R10: gerente aprovou → saga cria auth + conta no ms-conta + dispara e-mail mock
    public void dispararAutocadastro(String cpf) {
        try {
            String payload = mapper.writeValueAsString(Map.of("cpf", cpf));
            rabbit.convertAndSend(RabbitConfig.SAGA_EXCHANGE, RabbitConfig.START_AUTOCADASTRO_ROUTING_KEY, payload);
            log.info("saga start.autocadastro publicada cpf={}", cpf);
        } catch (Exception e) {
            throw new RuntimeException("falha publicando saga.start.autocadastro", e);
        }
    }

    // R4: cliente atualizou perfil → saga recalcula limite no ms-conta
    // payload inclui cpf + salario novo (o resto vai no contexto da saga via ATUALIZAR_CLIENTE)
    public void dispararAlteracaoPerfil(String cpf, ClienteRequestDTO dto) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("cpf", cpf);
            body.put("nome", dto.nome());
            body.put("email", dto.email());
            body.put("telefone", dto.telefone());
            body.put("salario", dto.salario().toPlainString());
            body.put("endereco", dto.endereco());
            String payload = mapper.writeValueAsString(body);
            rabbit.convertAndSend(RabbitConfig.SAGA_EXCHANGE, RabbitConfig.START_ALTERACAO_PERFIL_ROUTING_KEY, payload);
            log.info("saga start.alteracao_perfil publicada cpf={}", cpf);
        } catch (Exception e) {
            throw new RuntimeException("falha publicando saga.start.alteracao_perfil", e);
        }
    }
}
