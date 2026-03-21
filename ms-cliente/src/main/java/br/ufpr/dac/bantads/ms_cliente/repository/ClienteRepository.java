package br.ufpr.dac.bantads.ms_cliente.repository;

import br.ufpr.dac.bantads.ms_cliente.model.Cliente;
import br.ufpr.dac.bantads.ms_cliente.model.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Cliente> findByStatus(StatusCliente status);
}
