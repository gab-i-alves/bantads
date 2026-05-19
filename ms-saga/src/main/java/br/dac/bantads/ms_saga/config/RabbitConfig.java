package br.dac.bantads.ms_saga.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";

    public static final String REPLY_QUEUE = "saga.reply.orchestrator";
    public static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    public static final String START_AUTOCADASTRO_QUEUE = "saga.start.autocadastro";
    public static final String START_AUTOCADASTRO_ROUTING_KEY = "saga.start.autocadastro";

    public static final String START_APROVACAO_QUEUE = "saga.start.aprovacao";
    public static final String START_APROVACAO_ROUTING_KEY = "saga.start.aprovacao";

    public static final String START_INSERCAO_GERENTE_QUEUE = "saga.start.insercao_gerente";
    public static final String START_INSERCAO_GERENTE_ROUTING_KEY = "saga.start.insercao_gerente";

    public static final String CMD_CLIENTE_ROUTING_KEY = "saga.cmd.cliente";
    public static final String CMD_AUTH_ROUTING_KEY = "saga.cmd.auth";
    public static final String CMD_CONTA_ROUTING_KEY = "saga.cmd.conta";
    public static final String CMD_FUNCIONARIO_ROUTING_KEY = "saga.cmd.funcionario";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Queue replyQueue() {
        return new Queue(REPLY_QUEUE, true);
    }

    @Bean
    public Binding replyBinding(Queue replyQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(replyQueue).to(sagaExchange).with(REPLY_ROUTING_KEY);
    }

    @Bean
    public Queue startAutocadastroQueue() {
        return new Queue(START_AUTOCADASTRO_QUEUE, true);
    }

    @Bean
    public Binding startAutocadastroBinding(Queue startAutocadastroQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startAutocadastroQueue).to(sagaExchange).with(START_AUTOCADASTRO_ROUTING_KEY);
    }

    @Bean
    public Queue startAprovacaoQueue() {
        return new Queue(START_APROVACAO_QUEUE, true);
    }

    @Bean
    public Binding startAprovacaoBinding(Queue startAprovacaoQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startAprovacaoQueue).to(sagaExchange).with(START_APROVACAO_ROUTING_KEY);
    }

    @Bean
    public Queue startInsercaoGerenteQueue() {
        return new Queue(START_INSERCAO_GERENTE_QUEUE, true);
    }

    @Bean
    public Binding startInsercaoGerenteBinding(Queue startInsercaoGerenteQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startInsercaoGerenteQueue).to(sagaExchange).with(START_INSERCAO_GERENTE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
