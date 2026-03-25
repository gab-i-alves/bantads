package br.ufpr.dac.bantads.ms_auth.repositories;

import br.ufpr.dac.bantads.ms_auth.enums.Role;
import br.ufpr.dac.bantads.ms_auth.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    List<Account> findByRole(Role role);
}
