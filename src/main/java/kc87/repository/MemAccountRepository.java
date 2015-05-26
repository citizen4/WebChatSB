package kc87.repository;


import kc87.domain.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository(value = "inMemory")
@SuppressWarnings("unused")
public class MemAccountRepository implements AccountRepository {

   private static final Logger LOG = LogManager.getLogger(MemAccountRepository.class);
   private static final int MAX_ACCOUNTS = 32;
   private List<Account> repository = new ArrayList<>(MAX_ACCOUNTS);
   private long nextId = 0;

   @Override
   public Account findByUsername(final String username) {
      for(Account account : repository){
         if(account.getUsername().equalsIgnoreCase(username)){
            return account;
         }
      }
      return null;
   }

   @Override
   public Account[] findAll() {
      return repository.toArray(new Account[repository.size()]);
   }

   @Override
   public synchronized void save(Account account) {

      if(nextId > MAX_ACCOUNTS - 1 ){
         throw new RuntimeException("Repository is full");
      }

      account.setId(nextId++);
      repository.add(account);

      LOG.debug("Saved: "+account.toString());
   }
}
