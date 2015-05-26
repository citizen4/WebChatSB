package kc87.service;

import kc87.domain.Account;
import kc87.repository.AccountRepository;
import kc87.util.PasswordCrypto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@SuppressWarnings("unused")
public class AccountService
{
   private static final Logger LOG = LogManager.getLogger(AccountService.class);

   @Autowired
   @Qualifier("inMemory")
   private AccountRepository accountRepository;

   public Account checkAccount(final String username, final String password) {

      Account account = accountRepository.findByUsername(username);

      if(account != null) {
         if(PasswordCrypto.isPasswordCorrect(password, account.getPwHash())) {
            LOG.debug("Username and password are correct!");
            return account;
         }else{
            LOG.debug("Wrong password!");
         }
      }else {
         LOG.debug("User '{}' has no account!",username);
      }

      return null;
   }

   public void createAccount(final Account account) {
      LOG.debug("Create account for: {}",account.getUsername());
      if(accountRepository.findByUsername(account.getUsername()) == null) {
         account.setPwHash(PasswordCrypto.encryptPassword(account.getPassword()));
         accountRepository.save(account);
      }else {
         LOG.debug("User '{}' is already registered!",account.getUsername());
      }
   }

   @PostConstruct
   private void init() {
      LOG.debug("Init service");
      createTestAccount();
   }

   @PreDestroy
   private void destroy() {
      LOG.debug("Destroy service");
   }

   private void createTestAccount() {
      Account testAccount = new Account();
      testAccount.setFirstName("Luke");
      testAccount.setLastName("Skywalker");
      testAccount.setEmail("luke.skywalker@jedi.org");
      testAccount.setUsername("luke");
      testAccount.setPassword("12345678");

      createAccount(testAccount);
   }

}
