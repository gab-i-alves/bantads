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

// listener que consome eventos da fila conta.sync
// e atualiza o schema de LEITURA (schema_conta_read)
//
// fluxo: ContaService grava no CUD → publisher manda evento → este listener atualiza o Read
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
        System.out.println("[CQRS] Evento recebido: " + evento.tipoEvento()
                + " conta=" + evento.numeroConta());

        // atualiza o saldo da conta no schema de leitura
        ContaRead conta = contaReadRepo.findByNumero(evento.numeroConta())
                .orElse(null);

        if (conta == null) {
            // se a conta nao existe no read ainda, ignora
            // (acontece se o seed nao populou o read — mas nao deveria)
            System.out.println("[CQRS] WARN: conta " + evento.numeroConta()
                    + " nao encontrada no schema_conta_read");
            return;
        }

        // atualiza saldo com o valor que ja veio calculado do service
        conta.setSaldo(evento.saldoAtual());
        contaReadRepo.save(conta);

        // cria a movimentação no schema de leitura
        MovimentacaoRead mov = new MovimentacaoRead();
        mov.setDataHora(evento.dataHora());
        mov.setTipo(TipoMovimentacao.valueOf(evento.tipoEvento()));
        mov.setContaOrigem(evento.contaOrigem());
        mov.setContaDestino(evento.contaDestino());
        mov.setValor(evento.valorMovimentacao());
        movReadRepo.save(mov);

        System.out.println("[CQRS] Schema de leitura atualizado: conta="
                + evento.numeroConta() + " saldo=" + evento.saldoAtual());
    }
}
