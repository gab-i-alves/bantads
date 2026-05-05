package br.ufpr.dac.bantads.ms_cliente.saga;

import br.ufpr.dac.bantads.ms_cliente.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_cliente.dto.ClienteRequestDTO;
import br.ufpr.dac.bantads.ms_cliente.dto.ClienteResponseDTO;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService.ClienteJaCadastradoException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SagaCommandListenerTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Mock ClienteService clienteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SagaCommandListener listener;

    @BeforeEach
    void setUp() {
        listener = new SagaCommandListener(rabbitTemplate, clienteService, objectMapper);
    }

    // ====================== CRIAR_CLIENTE ======================

    @Test
    void criarCliente_payloadValido_chamaServiceERespondeSucesso() {
        String payload = """
                {
                  "nome": "Catharyna",
                  "email": "cat@test.com",
                  "cpf": "11111111111",
                  "telefone": "41999999999",
                  "salario": 5000.00,
                  "endereco": {
                    "logradouro": "Rua A",
                    "numero": "100",
                    "complemento": null,
                    "cep": "80000000",
                    "cidade": "Curitiba",
                    "estado": "PR"
                  }
                }
                """;
        SagaCommand cmd = new SagaCommand("saga-1", "AUTOCADASTRO", "CRIAR_CLIENTE", payload);
        when(clienteService.criar(any(ClienteRequestDTO.class)))
                .thenReturn(stubResponse("11111111111"));

        listener.onCommand(cmd);

        verify(clienteService).criar(any(ClienteRequestDTO.class));
        SagaReply reply = captureReply();
        assertThat(reply.sagaId()).isEqualTo("saga-1");
        assertThat(reply.step()).isEqualTo("CRIAR_CLIENTE");
        assertThat(reply.success()).isTrue();
        assertThat(reply.error()).isNull();
    }

    @Test
    void criarCliente_cpfDuplicado_naoPersisteERespondeFalha() {
        String payload = payloadCompleto("11111111111");
        SagaCommand cmd = new SagaCommand("saga-2", "AUTOCADASTRO", "CRIAR_CLIENTE", payload);
        when(clienteService.criar(any(ClienteRequestDTO.class)))
                .thenThrow(new ClienteJaCadastradoException("11111111111"));

        listener.onCommand(cmd);

        SagaReply reply = captureReply();
        assertThat(reply.success()).isFalse();
        assertThat(reply.error()).contains("11111111111");
    }

    @Test
    void criarCliente_payloadMalFormado_naoChamaServiceERespondeFalha() {
        SagaCommand cmd = new SagaCommand("saga-3", "AUTOCADASTRO", "CRIAR_CLIENTE", "{json invalido");

        listener.onCommand(cmd);

        verify(clienteService, never()).criar(any());
        SagaReply reply = captureReply();
        assertThat(reply.success()).isFalse();
        assertThat(reply.error()).isNotBlank();
    }

    // ====================== helpers ======================

    private SagaReply captureReply() {
        ArgumentCaptor<SagaReply> cap = ArgumentCaptor.forClass(SagaReply.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.SAGA_EXCHANGE),
                eq("saga.reply.orchestrator"),
                cap.capture());
        return cap.getValue();
    }

    private String payloadCompleto(String cpf) {
        return """
                {
                  "nome": "Teste",
                  "email": "t@t.com",
                  "cpf": "%s",
                  "telefone": "41900000000",
                  "salario": 3000.00,
                  "endereco": {
                    "logradouro": "Rua X", "numero": "1", "complemento": null,
                    "cep": "80000000", "cidade": "Curitiba", "estado": "PR"
                  }
                }
                """.formatted(cpf);
    }

    private ClienteResponseDTO stubResponse(String cpf) {
        return new ClienteResponseDTO(1L, "Teste", "t@t.com", cpf, "41900000000",
                java.math.BigDecimal.ZERO, "PENDENTE", null);
    }
}
