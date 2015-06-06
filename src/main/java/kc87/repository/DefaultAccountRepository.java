package kc87.repository;


import kc87.domain.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

@Repository(value = "default")
@SuppressWarnings("unused")
public class DefaultAccountRepository implements AccountRepository {
   private static final Logger LOG = LogManager.getLogger(DefaultAccountRepository.class);

   @PersistenceUnit
   private EntityManagerFactory entityManagerFactory;


   @Override
   public Account findByUsername(String username) {
      return null;
   }

   @Override
   public Account[] findAll() {
      return new Account[0];
   }

   @Override
   public void save(Account account) {

      Account newAccount = new Account();

      newAccount.setFirstName(account.getFirstName());
      newAccount.setLastName(account.getLastName());
      newAccount.setEmail(account.getEmail());
      newAccount.setRoles(account.getRoles());
      newAccount.setUsername(account.getUsername());
      newAccount.setPwHash(account.getPwHash());

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      EntityTransaction transaction = entityManager.getTransaction();

      transaction.begin();
      entityManager.persist(newAccount);
      transaction.commit();
      entityManager.close();
   }


}
