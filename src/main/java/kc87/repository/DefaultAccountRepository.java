package kc87.repository;

import kc87.domain.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository(value = "default")
@SuppressWarnings("unused")
public class DefaultAccountRepository implements AccountRepository {
   private static final Logger LOG = LogManager.getLogger(DefaultAccountRepository.class);

   @PersistenceContext
   private EntityManager entityManager;

   @Override
   public Account findByUsername(final String username) {
      List<Account> resultList;

      resultList = entityManager.createQuery("SELECT a FROM Account a WHERE LOWER(a.username) = :username", Account.class)
               .setParameter("username", username.toLowerCase())
               .getResultList();

      return (resultList.size() == 0) ? null : resultList.get(0);
   }

   @Override
   public Account[] findAll() {
      List<Account> result;
      result = entityManager.createQuery("SELECT a FROM Account a" , Account.class).getResultList();
      return result.toArray(new Account[result.size()]);
   }

   @Override
   @Transactional
   public void save(final Account account) {
      Account newAccount = new Account();

      newAccount.setFirstName(account.getFirstName());
      newAccount.setLastName(account.getLastName());
      newAccount.setEmail(account.getEmail());
      newAccount.setRoles(account.getRoles());
      newAccount.setUsername(account.getUsername());
      newAccount.setPassword(account.getPassword());

      entityManager.persist(newAccount);
   }
}
