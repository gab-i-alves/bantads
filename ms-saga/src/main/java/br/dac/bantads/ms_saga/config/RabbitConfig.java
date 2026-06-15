package br.dac.bantads.ms_saga.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
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

    public static final String START_ALTERACAO_PERFIL_QUEUE = "saga.start.alteracao_perfil";
    public static final String START_ALTERACAO_PERFIL_ROUTING_KEY = "saga.start.alteracao_perfil";

    public static final String START_REMOCAO_GERENTE_QUEUE = "saga.start.remocao_gerente";
    public static final String START_REMOCAO_GERENTE_ROUTING_KEY = "saga.start.remocao_gerente";

    public static final String CMD_CLIENTE_ROUTING_KEY = "saga.cmd.cliente";
    public static final String CMD_AUTH_ROUTING_KEY = "saga.cmd.auth";
    public static final String CMD_CONTA_ROUTING_KEY = "saga.cmd.conta";
    public static final String CMD_FUNCIONARIO_ROUTING_KEY = "saga.cmd.funcionario";

    // dead-letter exchange compartilhado (NF8): a mensagem que estoura o retry cai aqui
    // em vez de voltar pra fila original e virar poison message reprocessada pra sempre.
    public static final String DLX = "bantads.dlx";
    public static final String REPLY_QUEUE_DLQ = REPLY_QUEUE + ".dlq";
    public static final String START_AUTOCADASTRO_QUEUE_DLQ = START_AUTOCADASTRO_QUEUE + ".dlq";
    public static final String START_APROVACAO_QUEUE_DLQ = START_APROVACAO_QUEUE + ".dlq";
    public static final String START_INSERCAO_GERENTE_QUEUE_DLQ = START_INSERCAO_GERENTE_QUEUE + ".dlq";
    public static final String START_ALTERACAO_PERFIL_QUEUE_DLQ = START_ALTERACAO_PERFIL_QUEUE + ".dlq";
    public static final String START_REMOCAO_GERENTE_QUEUE_DLQ = START_REMOCAO_GERENTE_QUEUE + ".dlq";

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(SAGA_EXCHANGE);
    }

    // exchange morto (durável) onde o broker reentrega o que o retry rejeitou.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // helper: fila durável de consumo com dead-letter pro DLX, routing key = nome.dlq.
    private static Queue consumerQueue(String name, String dlqRoutingKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue replyQueue() {
        return consumerQueue(REPLY_QUEUE, REPLY_QUEUE_DLQ);
    }

    @Bean
    public Binding replyBinding(Queue replyQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(replyQueue).to(sagaExchange).with(REPLY_ROUTING_KEY);
    }

    @Bean
    public Queue startAutocadastroQueue() {
        return consumerQueue(START_AUTOCADASTRO_QUEUE, START_AUTOCADASTRO_QUEUE_DLQ);
    }

    @Bean
    public Binding startAutocadastroBinding(Queue startAutocadastroQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startAutocadastroQueue).to(sagaExchange).with(START_AUTOCADASTRO_ROUTING_KEY);
    }

    @Bean
    public Queue startAprovacaoQueue() {
        return consumerQueue(START_APROVACAO_QUEUE, START_APROVACAO_QUEUE_DLQ);
    }

    @Bean
    public Binding startAprovacaoBinding(Queue startAprovacaoQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startAprovacaoQueue).to(sagaExchange).with(START_APROVACAO_ROUTING_KEY);
    }

    @Bean
    public Queue startInsercaoGerenteQueue() {
        return consumerQueue(START_INSERCAO_GERENTE_QUEUE, START_INSERCAO_GERENTE_QUEUE_DLQ);
    }

    @Bean
    public Binding startInsercaoGerenteBinding(Queue startInsercaoGerenteQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startInsercaoGerenteQueue).to(sagaExchange).with(START_INSERCAO_GERENTE_ROUTING_KEY);
    }

    @Bean
    public Queue startAlteracaoPerfilQueue() {
        return consumerQueue(START_ALTERACAO_PERFIL_QUEUE, START_ALTERACAO_PERFIL_QUEUE_DLQ);
    }

    @Bean
    public Binding startAlteracaoPerfilBinding(Queue startAlteracaoPerfilQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startAlteracaoPerfilQueue).to(sagaExchange).with(START_ALTERACAO_PERFIL_ROUTING_KEY);
    }

    @Bean
    public Queue startRemocaoGerenteQueue() {
        return consumerQueue(START_REMOCAO_GERENTE_QUEUE, START_REMOCAO_GERENTE_QUEUE_DLQ);
    }

    @Bean
    public Binding startRemocaoGerenteBinding(Queue startRemocaoGerenteQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(startRemocaoGerenteQueue).to(sagaExchange).with(START_REMOCAO_GERENTE_ROUTING_KEY);
    }

    // helper: fila morta durável ligada ao DLX pela própria routing key.
    private static Binding dlqBinding(String dlqName, DirectExchange dlx) {
        return BindingBuilder.bind(QueueBuilder.durable(dlqName).build()).to(dlx).with(dlqName);
    }

    @Bean
    public Queue replyDlq() {
        return QueueBuilder.durable(REPLY_QUEUE_DLQ).build();
    }

    @Bean
    public Binding replyDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(REPLY_QUEUE_DLQ, deadLetterExchange);
    }

    @Bean
    public Queue startAutocadastroDlq() {
        return QueueBuilder.durable(START_AUTOCADASTRO_QUEUE_DLQ).build();
    }

    @Bean
    public Binding startAutocadastroDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(START_AUTOCADASTRO_QUEUE_DLQ, deadLetterExchange);
    }

    @Bean
    public Queue startAprovacaoDlq() {
        return QueueBuilder.durable(START_APROVACAO_QUEUE_DLQ).build();
    }

    @Bean
    public Binding startAprovacaoDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(START_APROVACAO_QUEUE_DLQ, deadLetterExchange);
    }

    @Bean
    public Queue startInsercaoGerenteDlq() {
        return QueueBuilder.durable(START_INSERCAO_GERENTE_QUEUE_DLQ).build();
    }

    @Bean
    public Binding startInsercaoGerenteDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(START_INSERCAO_GERENTE_QUEUE_DLQ, deadLetterExchange);
    }

    @Bean
    public Queue startAlteracaoPerfilDlq() {
        return QueueBuilder.durable(START_ALTERACAO_PERFIL_QUEUE_DLQ).build();
    }

    @Bean
    public Binding startAlteracaoPerfilDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(START_ALTERACAO_PERFIL_QUEUE_DLQ, deadLetterExchange);
    }

    @Bean
    public Queue startRemocaoGerenteDlq() {
        return QueueBuilder.durable(START_REMOCAO_GERENTE_QUEUE_DLQ).build();
    }

    @Bean
    public Binding startRemocaoGerenteDlqBinding(DirectExchange deadLetterExchange) {
        return dlqBinding(START_REMOCAO_GERENTE_QUEUE_DLQ, deadLetterExchange);
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
