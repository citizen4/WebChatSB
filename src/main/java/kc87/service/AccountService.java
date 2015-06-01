package kc87.service;

import kc87.domain.Account;
import kc87.repository.AccountRepository;
import kc87.util.CustomPasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;

@Service
@SuppressWarnings("unused")
public class AccountService implements UserDetailsService
{
   private static final Logger LOG = LogManager.getLogger(AccountService.class);

   @Autowired
   @Qualifier("inMemory")
   private AccountRepository accountRepository;

   public Account checkAccount(final String username, final String password)
   {

      Account account = accountRepository.findByUsername(username);

      if (account != null) {
         if (CustomPasswordEncoder.isPasswordCorrect(password, account.getPwHash())) {
            LOG.debug("Username and password are correct!");
            return account;
         } else {
            LOG.debug("Wrong password!");
         }
      } else {
         LOG.debug("User '{}' has no account!", username);
      }

      return null;
   }

   public void createAccount(final Account account)
   {
      LOG.debug("Create account for: {}", account.getUsername());
      if (accountRepository.findByUsername(account.getUsername()) == null) {
         account.setPwHash(CustomPasswordEncoder.encryptPassword(account.getPassword()));
         account.setPassword(null);
         accountRepository.save(account);
      } else {
         LOG.debug("User '{}' is already registered!", account.getUsername());
      }
   }

   @PostConstruct
   private void init()
   {
      LOG.debug("Init service");
      createTestAccount();
   }

   @PreDestroy
   private void destroy()
   {
      LOG.debug("Destroy service");
   }

   private void createTestAccount()
   {
      Account testAccount = new Account();
      testAccount.setFirstName("Luke");
      testAccount.setLastName("Skywalker");
      testAccount.setEmail("luke.skywalker@jedi.org");
      testAccount.setUsername("luke");
      testAccount.setPassword("12345678");

      createAccount(testAccount);
   }

   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
   {
      Account account = accountRepository.findByUsername(username);

      if(account != null){
         UserDetails user = new User(account.getUsername(),
                 account.getPwHash(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
         return user;
      }else {
         throw new UsernameNotFoundException("User does not exist!");
      }
   }
}
