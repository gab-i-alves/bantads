package br.ufpr.dac.bantads.ms_auth.config;

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

    // dead-letter exchange compartilhado (NF8): comando que estoura o retry cai aqui
    // em vez de voltar pra fila e virar poison message reprocessada pra sempre.
    public static final String DLX = "bantads.dlx";
    public static final String CMD_QUEUE_DLQ = CMD_QUEUE + ".dlq";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // exchange morto (durável). Mesmo nome/tipo/durabilidade dos outros MSs, então
    // declarar aqui também é idempotente.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue sagaCmdQueue() {
        return QueueBuilder.durable(CMD_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", CMD_QUEUE_DLQ)
                .build();
    }

    @Bean
    public Binding cmdBinding(Queue sagaCmdQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaCmdQueue).to(sagaExchange).with(CMD_ROUTING_KEY);
    }

    @Bean
    public Queue sagaCmdDlq() {
        return QueueBuilder.durable(CMD_QUEUE_DLQ).build();
    }

    @Bean
    public Binding sagaCmdDlqBinding(Queue sagaCmdDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(sagaCmdDlq).to(deadLetterExchange).with(CMD_QUEUE_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // factory de listener com retry limitado (NF8). Como nenhum @RabbitListener fixa
    // containerFactory, o Boot injeta este bean por nome. setDefaultRequeueRejected(false):
    // a mensagem rejeitada não volta pra fila, vai pro DLX. Retry stateless 3 tentativas.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .configureRetryPolicy(policy -> policy.maxRetries(2))
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
