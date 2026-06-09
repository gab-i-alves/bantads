package br.ufpr.dac.bantads.ms_auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// participante das SAGAs orquestradas pelo ms-saga
// usa a mesma exchange direct compartilhada com os outros MSs (ms-cliente já cria)
@Configuration
public class RabbitConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";

    // fila de comandos endereçados ao ms-auth (ex: CRIAR_AUTH_CLIENTE pós-aprovação)
    public static final String CMD_QUEUE = "saga.cmd.auth";
    public static final String CMD_ROUTING_KEY = "saga.cmd.auth";

    // routing key onde o ms-saga escuta replies (ele decide o nome da fila)
    public static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

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
