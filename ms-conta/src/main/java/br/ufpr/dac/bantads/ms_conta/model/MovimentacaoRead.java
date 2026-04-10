package br.ufpr.dac.bantads.ms_conta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// espelho da entidade Movimentacao, mas no schema de LEITURA
// atualizada pelo ContaEventListener quando chega evento do RabbitMQ
@Entity
@Table(name = "movimentacao", schema = "schema_conta_read")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(name = "conta_origem", nullable = false, length = 4)
    private String contaOrigem;

    @Column(name = "conta_destino", length = 4)
    private String contaDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;
}
