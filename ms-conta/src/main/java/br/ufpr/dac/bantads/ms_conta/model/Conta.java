package br.ufpr.dac.bantads.ms_conta.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conta")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 4)
    private String numero;

    @Column(name = "cliente_cpf", nullable = false, length = 11)
    private String clienteCpf;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limite;

    @Column(name = "gerente_cpf", nullable = false, length = 11)
    private String gerenteCpf;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;
}
