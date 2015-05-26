package kc87.repository;


import kc87.domain.Account;

public interface AccountRepository {

   Account findByUsername(String username);
   Account[] findAll();
   void save(Account account);
}
