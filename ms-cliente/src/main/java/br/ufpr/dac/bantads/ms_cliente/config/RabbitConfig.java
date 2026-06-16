package br.ufpr.dac.bantads.ms_cliente.config;

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

// configura a infraestrutura do RabbitMQ pro ms-cliente
// exchange DIRECT saga.exchange -> filas de comando e resposta pra orquestrar as SAGAs
@Configuration
public class RabbitConfig {

    // exchange única pra todas as SAGAs - todos os MSs publicam e consomem daqui
    public static final String SAGA_EXCHANGE = "saga.exchange";

    // fila de comandos: outros MSs mandam comandos pro ms-cliente por aqui
    // ex: "rejeite esse cliente" (vindo do orquestrador de remoção de gerente)
    public static final String CMD_QUEUE = "saga.cmd.cliente";
    public static final String CMD_ROUTING_KEY = "saga.cmd.cliente";

    // fila de respostas: quando o ms-cliente orquestra uma saga (autocadastro, alteração perfil),
    // os outros MSs respondem por aqui
    public static final String RESPONSE_QUEUE = "saga.response.cliente";
    public static final String RESPONSE_ROUTING_KEY = "saga.response.cliente";

    // routing key onde o ms-saga escuta replies (ele decide o nome da fila)
    public static final String REPLY_ROUTING_KEY = "saga.reply.orchestrator";

    // routing keys das filas de start do ms-saga (mesma exchange única "saga.exchange")
    public static final String START_AUTOCADASTRO_ROUTING_KEY = "saga.start.autocadastro";
    public static final String START_ALTERACAO_PERFIL_ROUTING_KEY = "saga.start.alteracao_perfil";

    public static final String AUTH_UPDATE_CREDENCIAIS_ROUTING_KEY = "saga.cmd.auth.update_credenciais";

    // dead-letter exchange compartilhado (NF8): a mensagem que estoura o retry cai aqui
    // em vez de voltar pra fila original e virar poison message reprocessada pra sempre.
    public static final String DLX = "bantads.dlx";
    public static final String CMD_QUEUE_DLQ = CMD_QUEUE + ".dlq";
    public static final String RESPONSE_QUEUE_DLQ = RESPONSE_QUEUE + ".dlq";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // exchange morto (durável) onde o broker reentrega o que o retry rejeitou.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // fila de comandos (durável: sobrevive restart do rabbit).
    // x-dead-letter-exchange manda a rejeição pro DLX com a routing key da .dlq.
    @Bean
    public Queue sagaCmdQueue() {
        return QueueBuilder.durable(CMD_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", CMD_QUEUE_DLQ)
                .build();
    }

    // fila de respostas (durável também)
    @Bean
    public Queue sagaResponseQueue() {
        return QueueBuilder.durable(RESPONSE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RESPONSE_QUEUE_DLQ)
                .build();
    }

    // binding: liga exchange -> fila de comandos pela routing key
    @Bean
    public Binding cmdBinding(Queue sagaCmdQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaCmdQueue).to(sagaExchange).with(CMD_ROUTING_KEY);
    }

    // binding: liga exchange -> fila de respostas pela routing key
    @Bean
    public Binding responseBinding(Queue sagaResponseQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(sagaResponseQueue).to(sagaExchange).with(RESPONSE_ROUTING_KEY);
    }

    // filas mortas, ligadas ao DLX pela própria routing key.
    @Bean
    public Queue sagaCmdDlq() {
        return QueueBuilder.durable(CMD_QUEUE_DLQ).build();
    }

    @Bean
    public Binding cmdDlqBinding(Queue sagaCmdDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(sagaCmdDlq).to(deadLetterExchange).with(CMD_QUEUE_DLQ);
    }

    @Bean
    public Queue sagaResponseDlq() {
        return QueueBuilder.durable(RESPONSE_QUEUE_DLQ).build();
    }

    @Bean
    public Binding responseDlqBinding(Queue sagaResponseDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(sagaResponseDlq).to(deadLetterExchange).with(RESPONSE_QUEUE_DLQ);
    }

    // converte objetos Java <-> JSON automaticamente
    // sem isso o rabbit manda bytes binários e fica ilegível no console
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
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
