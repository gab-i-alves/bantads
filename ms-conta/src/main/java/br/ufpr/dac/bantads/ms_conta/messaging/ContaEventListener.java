package br.ufpr.dac.bantads.ms_conta.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.bantads.ms_conta.config.RabbitConfig;
import br.ufpr.dac.bantads.ms_conta.model.ContaRead;
import br.ufpr.dac.bantads.ms_conta.model.MovimentacaoRead;
import br.ufpr.dac.bantads.ms_conta.model.TipoMovimentacao;
import br.ufpr.dac.bantads.ms_conta.repository.ContaReadRepository;
import br.ufpr.dac.bantads.ms_conta.repository.MovimentacaoReadRepository;
import lombok.extern.slf4j.Slf4j;

// listener que consome eventos da fila conta.sync
// e atualiza o schema de LEITURA (schema_conta_read)
//
// fluxo: ContaService grava no CUD → publisher manda evento → este listener atualiza o Read
@Slf4j
@Component
public class ContaEventListener {

    private final ContaReadRepository contaReadRepo;
    private final MovimentacaoReadRepository movReadRepo;

    public ContaEventListener(ContaReadRepository contaReadRepo,
                              MovimentacaoReadRepository movReadRepo) {
        this.contaReadRepo = contaReadRepo;
        this.movReadRepo = movReadRepo;
    }

    // @RabbitListener: o Spring chama esse método automaticamente
    // toda vez que uma mensagem chega na fila conta.sync
    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void onSync(ContaEvent evento) {
        log.info("[CQRS] Evento recebido: {} conta={}", evento.tipoEvento(), evento.numeroConta());

        // atualiza o saldo da conta no schema de leitura
        ContaRead conta = contaReadRepo.findByNumero(evento.numeroConta())
                .orElse(null);

        if (conta == null) {
            // se a conta nao existe no read ainda, ignora
            // (acontece se o seed nao populou o read — mas nao deveria)
            log.warn("[CQRS] conta {} nao encontrada no schema_conta_read", evento.numeroConta());
            return;
        }

        conta.setSaldo(evento.saldoAtual());
        contaReadRepo.save(conta);

        boolean isDestino = evento.numeroConta().equals(evento.contaDestino());
        if (!isDestino) {
            MovimentacaoRead mov = new MovimentacaoRead();
            mov.setDataHora(evento.dataHora());
            mov.setTipo(TipoMovimentacao.valueOf(evento.tipoEvento()));
            mov.setContaOrigem(evento.contaOrigem());
            mov.setContaDestino(evento.contaDestino());
            mov.setValor(evento.valorMovimentacao());
            movReadRepo.save(mov);
        }

        log.info("[CQRS] Schema de leitura atualizado: conta={} saldo={}", evento.numeroConta(), evento.saldoAtual());
    }
}
