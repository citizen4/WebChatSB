package kc87.repository.jpa;

import kc87.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;


@RepositoryRestResource(exported = true)
@SuppressWarnings("unused")
public interface AccountRepository extends JpaRepository<Account, Long> {
   Account findByUsernameIgnoreCase(String username);
}
