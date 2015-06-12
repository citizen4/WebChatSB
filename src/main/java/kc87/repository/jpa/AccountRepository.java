package kc87.repository.jpa;

import kc87.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface AccountRepository extends JpaRepository<Account, Long> {
   Account findByUsernameIgnoreCase(String username);
}
