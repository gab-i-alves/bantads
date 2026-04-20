package br.ufpr.dac.bantads.ms_cliente.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// configura a infraestrutura do RabbitMQ pro ms-cliente
// exchange DIRECT saga.exchange → filas de comando e resposta pra orquestrar as SAGAs
@Configuration
public class RabbitConfig {

    // exchange única pra todas as SAGAs — todos os MSs publicam e consomem daqui
    public static final String SAGA_EXCHANGE = "saga.exchange";

    // fila de comandos: outros MSs mandam comandos pro ms-cliente por aqui
    // ex: "rejeite esse cliente" (vindo do orquestrador de remoção de gerente)
    public static final String CMD_QUEUE = "saga.cmd.cliente";
    public static final String CMD_ROUTING_KEY = "saga.cmd.cliente";

    // fila de respostas: quando o ms-cliente orquestra uma saga (autocadastro, alteração perfil),
    // os outros MSs respondem por aqui
    public static final String RESPONSE_QUEUE = "saga.response.cliente";
    public static final String RESPONSE_ROUTING_KEY = "saga.response.cliente";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // fila de comandos (durável: sobrevive restart do rabbit)
    @Bean
    public Queue sagaCmdQueue() {
        return new Queue(CMD_QUEUE, true);
    }

    // fila de respostas (durável também)
    @Bean
    public Queue sagaResponseQueue() {
        return new Queue(RESPONSE_QUEUE, true);
    }

    // binding: liga exchange → fila de comandos pela routing key
    @Bean
    public Binding cmdBinding(Queue sagaCmdQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaCmdQueue).to(sagaExchange).with(CMD_ROUTING_KEY);
    }

    // binding: liga exchange → fila de respostas pela routing key
    @Bean
    public Binding responseBinding(Queue sagaResponseQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaResponseQueue).to(sagaExchange).with(RESPONSE_ROUTING_KEY);
    }

    // converte objetos Java <-> JSON automaticamente
    // sem isso o rabbit manda bytes binários e fica ilegível no console
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
