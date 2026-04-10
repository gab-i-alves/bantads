package br.ufpr.dac.bantads.ms_conta.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.ufpr.dac.bantads.ms_conta.config.RabbitConfig;

// publica eventos no RabbitMQ após cada operação de escrita
// o ContaService chama publicar() depois de salvar no schema CUD
@Component
public class ContaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ContaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(ContaEvent evento) {
        // manda o evento pro exchange com a routing key
        // o Jackson converte ContaEvent → JSON automaticamente
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ROUTING_KEY,
            evento
        );
        System.out.println("[CQRS] Evento publicado: " + evento.tipoEvento()
                + " conta=" + evento.numeroConta());
    }
}
