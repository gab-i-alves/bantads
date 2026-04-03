package br.ufpr.dac.bantads.ms_conta.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.bantads.ms_conta.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumero(String numero);

    Optional<Conta> findByClienteCpf(String clienteCpf);

    List<Conta> findByGerenteCpf(String gerenteCpf);

    // usado pra achar o gerente com menos clientes (R10, R17)
    long countByGerenteCpf(String gerenteCpf);
}
