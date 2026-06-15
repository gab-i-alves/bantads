package br.ufpr.dac.bantads.ms_conta.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// configura a infraestrutura do RabbitMQ pro CQRS
// exchange DIRECT -> fila conta.sync -> listener atualiza schema de leitura
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "conta.exchange";
    public static final String QUEUE = "conta.sync";
    public static final String ROUTING_KEY = "conta.sync";

    // infra das SAGAs orquestradas pelo ms-saga (R1, R10, R17, R18 etc.)
    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String SAGA_CMD_QUEUE = "saga.cmd.conta";
    public static final String SAGA_CMD_ROUTING_KEY = "saga.cmd.conta";
    public static final String SAGA_REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    // dead-letter exchange compartilhado (NF8): mensagem que estoura o retry cai aqui
    // em vez de voltar pra fila original e virar poison message reprocessada pra sempre.
    public static final String DLX = "bantads.dlx";
    // fila morta por consumidor, com routing key igual ao nome dela.
    public static final String QUEUE_DLQ = QUEUE + ".dlq";
    public static final String SAGA_CMD_QUEUE_DLQ = SAGA_CMD_QUEUE + ".dlq";

    // exchange tipo DIRECT: manda mensagem pra fila que bater a routing key
    @Bean
    public DirectExchange contaExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // exchange morto (durável) onde o broker reentrega o que o retry rejeitou.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // fila durável: sobrevive restart do RabbitMQ.
    // x-dead-letter-exchange faz o broker mandar a rejeição pro DLX com a routing key da .dlq.
    @Bean
    public Queue contaSyncQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    // binding: liga exchange -> fila pela routing key
    @Bean
    public Binding binding(Queue contaSyncQueue, DirectExchange contaExchange) {
        return BindingBuilder.bind(contaSyncQueue).to(contaExchange).with(ROUTING_KEY);
    }

    // fila morta de conta.sync, ligada ao DLX pela própria routing key.
    @Bean
    public Queue contaSyncDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding contaSyncDlqBinding(Queue contaSyncDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(contaSyncDlq).to(deadLetterExchange).with(QUEUE_DLQ);
    }

    // converte objetos Java <-> JSON automaticamente (em vez de bytes binarios)
    // importante: na defesa o prof vai olhar o console do RabbitMQ e ver JSON legivel
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Queue sagaCmdQueue() {
        return QueueBuilder.durable(SAGA_CMD_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", SAGA_CMD_QUEUE_DLQ)
                .build();
    }

    @Bean
    public Binding sagaCmdBinding(Queue sagaCmdQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaCmdQueue).to(sagaExchange).with(SAGA_CMD_ROUTING_KEY);
    }

    @Bean
    public Queue sagaCmdDlq() {
        return QueueBuilder.durable(SAGA_CMD_QUEUE_DLQ).build();
    }

    @Bean
    public Binding sagaCmdDlqBinding(Queue sagaCmdDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(sagaCmdDlq).to(deadLetterExchange).with(SAGA_CMD_QUEUE_DLQ);
    }

    // factory de listener com retry limitado (NF8). sobrescreve o default do Spring Boot.
    // como nenhum @RabbitListener fixa containerFactory, o Boot injeta este bean por nome.
    // setDefaultRequeueRejected(false): a mensagem rejeitada NÃO volta pra fila, vai pro DLX.
    // retry stateless 3 tentativas; ao esgotar, RejectAndDontRequeueRecoverer rejeita sem requeue.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                // maxRetries(2) = 2 reentregas além da 1a tentativa, total de 3 execuções
                .configureRetryPolicy(policy -> policy.maxRetries(2))
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
