package br.ufpr.dac.bantads.ms_conta.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.bantads.ms_conta.model.ContaRead;

// repositório de LEITURA — consultas vão pra schema_conta_read
public interface ContaReadRepository extends JpaRepository<ContaRead, Long> {

    Optional<ContaRead> findByNumero(String numero);

    Optional<ContaRead> findByClienteCpf(String clienteCpf);
}
