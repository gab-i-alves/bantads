package br.dac.bantads.ms_funcionario.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // R19: telefone do gerente. Nullable porque o seed legado não tinha o campo
    // e clientes antigos podem não enviá-lo.
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

}