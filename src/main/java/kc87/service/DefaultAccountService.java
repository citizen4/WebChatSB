package kc87.service;

import kc87.domain.Account;
import kc87.repository.jpa.AccountRepository;
import kc87.service.crypto.ScryptPasswordEncoder;
import kc87.service.crypto.SimplePasswordEncoder;
import kc87.web.RegisterFormBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.Jackson2ResourceReader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("unused")
public class DefaultAccountService implements AccountService {
   private static final Logger LOG = LogManager.getLogger(DefaultAccountService.class);
   private static final PasswordEncoder PASSWORD_ENCODER = new ScryptPasswordEncoder();

   @Autowired
   //@Qualifier("inMemory")
   private AccountRepository accountRepository;


   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Account account = accountRepository.findByUsernameIgnoreCase(username);

      if (account != null) {
         List<GrantedAuthority> authorities = new ArrayList<>();
         for (String role : account.getRoles().split(",")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
         }
         return new User(account.getUsername(), account.getPassword(), authorities);
      } else {
         throw new UsernameNotFoundException("User does not exist!");
      }
   }

   @Override
   public List<Account> allAccounts() {
      return accountRepository.findAll();
   }

   @Override
   public void createAccount(final Account account, final String password) {
      LOG.debug("Create account for: {}", account.getUsername());
      if (accountRepository.findByUsernameIgnoreCase(account.getUsername()) == null) {
         Account newAccount = new Account();
         newAccount.setFirstName(account.getFirstName());
         newAccount.setLastName(account.getLastName());
         newAccount.setEmail(account.getEmail());
         newAccount.setUsername(account.getUsername());
         newAccount.setPassword(PASSWORD_ENCODER.encode(password));
         newAccount.setRoles(account.getRoles() == null ? "USER" : account.getRoles());
         accountRepository.save(newAccount);
      } else {
         LOG.debug("User '{}' is already registered!", account.getUsername());
         throw new UsernameAlreadyTakenException(account.getUsername());
      }
   }

   @Override
   public void createAccount(final RegisterFormBean formBean) {
      Account newAccount = new Account();
      newAccount.setFirstName(formBean.getFirstName());
      newAccount.setLastName(formBean.getLastName());
      newAccount.setEmail(formBean.getEmail());
      newAccount.setUsername(formBean.getUsername());
      createAccount(newAccount, formBean.getPassword());
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
      Resource accountResource = new ClassPathResource("db/accounts.json");
      Jackson2ResourceReader resourceReader = new Jackson2ResourceReader();

      try {
         Object accounts = resourceReader.readFrom(accountResource, this.getClass().getClassLoader());
         if (accounts instanceof List) {
            for (Object account : (List) accounts) {
               try {
                  createAccount((Account) account, ((Account) account).getPassword());
               } catch (UsernameAlreadyTakenException e) {
                  LOG.warn(e);
                  // XXX
                  return;
               }
            }
         } else {
            try {
               createAccount((Account) accounts, ((Account) accounts).getPassword());
            } catch (UsernameAlreadyTakenException e) {
               LOG.warn(e);
            }
         }
      } catch (Exception e) {
         LOG.error(e);
      }
   }

   public static class UsernameAlreadyTakenException extends RuntimeException {
      public UsernameAlreadyTakenException(final String username) {
         super("Username '" + username + "' already taken!");
      }
   }

}
