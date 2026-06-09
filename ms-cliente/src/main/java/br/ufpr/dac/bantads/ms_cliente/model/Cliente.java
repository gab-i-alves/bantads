package br.ufpr.dac.bantads.ms_cliente.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente", schema = "schema_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salario;

    // pendente, aprovado ou rejeitado - controla fluxo de aprovacao (R1, R10, R11)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCliente status;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;

    // relacao 1:1 - cada cliente tem seu proprio endereco
    // cascade: salvar/deletar cliente salva/deleta endereco junto
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id", nullable = false, unique = true)
    private Endereco endereco;
}
