package br.dac.bantads.ms_funcionario.config;

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

    // routing key dedicada pro PUT /gerentes/{cpf} avisar o ms-auth a trocar a senha.
    // best-effort, fora da saga: ms-auth deve bindar uma fila própria nesta routing key.
    public static final String AUTH_UPDATE_SENHA_ROUTING_KEY = "saga.cmd.auth.update_senha";

    // dead-letter exchange compartilhado (NF8): a mensagem que estoura o retry cai aqui
    // em vez de voltar pra fila original e virar poison message reprocessada pra sempre.
    public static final String DLX = "bantads.dlx";
    public static final String CMD_QUEUE_DLQ = CMD_QUEUE + ".dlq";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // exchange morto (durável) onde o broker reentrega o que o retry rejeitou.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // x-dead-letter-exchange manda a rejeição pro DLX com a routing key da .dlq.
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

    // fila morta de saga.cmd.funcionario, ligada ao DLX pela própria routing key.
    @Bean
    public Queue sagaCmdDlq() {
        return QueueBuilder.durable(CMD_QUEUE_DLQ).build();
    }

    @Bean
    public Binding cmdDlqBinding(Queue sagaCmdDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(sagaCmdDlq).to(deadLetterExchange).with(CMD_QUEUE_DLQ);
    }

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
