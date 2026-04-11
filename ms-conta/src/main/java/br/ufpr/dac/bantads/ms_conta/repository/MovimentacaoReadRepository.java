package br.ufpr.dac.bantads.ms_conta.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.ufpr.dac.bantads.ms_conta.model.MovimentacaoRead;

// repositório de LEITURA — consultas vão pra schema_conta_read
public interface MovimentacaoReadRepository extends JpaRepository<MovimentacaoRead, Long> {

    // R8: extrato com filtro de data
    @Query("SELECT m FROM MovimentacaoRead m " +
           "WHERE (m.contaOrigem = ?1 OR m.contaDestino = ?1) " +
           "AND m.dataHora BETWEEN ?2 AND ?3 " +
           "ORDER BY m.dataHora ASC")
    List<MovimentacaoRead> findByContaAndPeriodo(
            String numeroConta, LocalDateTime inicio, LocalDateTime fim);

    // R8: extrato completo (sem filtro de data)
    @Query("SELECT m FROM MovimentacaoRead m " +
           "WHERE m.contaOrigem = ?1 OR m.contaDestino = ?1 " +
           "ORDER BY m.dataHora ASC")
    List<MovimentacaoRead> findByContaNumero(String numeroConta);
}
