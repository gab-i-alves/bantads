package br.dac.bantads.ms_funcionario.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// participante das SAGAs orquestradas pelo ms-saga
// usa a mesma exchange direct compartilhada com os outros MSs
@Configuration
public class RabbitConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";

    // routing key da reply que o orquestrador (ms-saga) escuta
    public static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    // fila de comandos endereçados ao ms-funcionario
    public static final String CMD_QUEUE = "saga.cmd.funcionario";
    public static final String CMD_ROUTING_KEY = "saga.cmd.funcionario";

    // routing key usada pelo POST /gerentes pra disparar a saga R17 no ms-saga
    public static final String START_INSERCAO_GERENTE_ROUTING_KEY = "saga.start.insercao_gerente";

    // routing key usada pelo DELETE /gerentes/{cpf} pra disparar a saga R18 no ms-saga
    public static final String START_REMOCAO_GERENTE_ROUTING_KEY = "saga.start.remocao_gerente";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Queue sagaCmdQueue() {
        return new Queue(CMD_QUEUE, true);
    }

    @Bean
    public Binding cmdBinding(Queue sagaCmdQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaCmdQueue).to(sagaExchange).with(CMD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
