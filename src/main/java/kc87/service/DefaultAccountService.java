package kc87.service;

import kc87.domain.Account;
import kc87.repository.jpa.AccountRepository;
import kc87.service.crypto.ScryptPasswordEncoder;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@SuppressWarnings("unused")
public class DefaultAccountService implements AccountService {
   private static final Logger LOG = LogManager.getLogger(DefaultAccountService.class);
   private static final PasswordEncoder PASSWORD_ENCODER = new ScryptPasswordEncoder();

   @Autowired
   private AccountRepository accountRepository;

   @Autowired
   LocalValidatorFactoryBean validatorFactory;

   private AccountValidator accountValidator;

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
   public void createAccount(final Account account) {
      LOG.debug("Create account for: {}", account.getUsername());
      accountRepository.save(account);
   }

   @PostConstruct
   private void init() {
      LOG.debug("Init service");
      accountValidator = new AccountValidator(accountRepository, validatorFactory);
      createTestAccounts();
   }

   @PreDestroy
   private void destroy() {
      LOG.debug("Destroy service");
   }

   public AccountValidator getAccountValidator() {
      return accountValidator;
   }

   public Errors validateAccount(final Account account, final Errors err) {
      Errors errors = (err == null) ? new BeanPropertyBindingResult(account, "Account") : err;
      accountValidator.validate(account, errors);
      return errors;
   }

   public Account prepareAccount(final RegisterFormBean formBean) {
      Account newAccount = new Account();
      newAccount.setFirstName(formBean.getFirstName());
      newAccount.setLastName(formBean.getLastName());
      newAccount.setEmail(formBean.getEmail());
      newAccount.setUsername(formBean.getUsername());
      return prepareAccount(newAccount, formBean.getPassword());
   }

   public Account prepareAccount(final Account account, final String password) {
      Account newAccount = new Account();
      newAccount.setId(account.getId());
      newAccount.setFirstName(account.getFirstName());
      newAccount.setLastName(account.getLastName());
      newAccount.setEmail(account.getEmail());
      newAccount.setUsername(account.getUsername());
      newAccount.setPassword(PASSWORD_ENCODER.encode(password));
      newAccount.setRoles(account.getRoles() == null ? "USER" : account.getRoles());
      return newAccount;
   }

   private void createTestAccounts() {
      Resource accountResource = new ClassPathResource("db/accounts.json");
      Jackson2ResourceReader resourceReader = new Jackson2ResourceReader();
      try {
         Object accounts = resourceReader.readFrom(accountResource, this.getClass().getClassLoader());
         if (accounts instanceof List) {
            for (Object account : (List) accounts) {
               createTestAccount((Account)account);
            }
         } else {
            createTestAccount((Account)accounts);
         }
      } catch (Exception e) {
         LOG.error(e);
      }
   }

   private void createTestAccount(Account account) {
      Account newAccount = prepareAccount(account, account.getPassword());
      Errors errors = validateAccount(newAccount, null);
      if (!errors.hasErrors()) {
         createAccount(newAccount);
      } else {
         for(ObjectError error : errors.getAllErrors()) {
            LOG.warn(error.getDefaultMessage());
         }
      }
   }

   public static class AccountValidator implements Validator {
      private static final Logger LOG = LogManager.getLogger(AccountValidator.class);
      private AccountRepository accountRepository;
      private javax.validation.Validator validator;

      public AccountValidator(final AccountRepository repository,
                              final LocalValidatorFactoryBean validatorFactory) {
         accountRepository = repository;
         validator = validatorFactory.getValidator();
      }

      @Override
      public boolean supports(Class<?> aClass) {
         return Account.class.equals(aClass);
      }

      @Override
      public void validate(Object o, Errors errors) {
         Account account = (Account) o;

         Set<ConstraintViolation<Account>> constraintViolationSet = validator.validate(account);

         if (constraintViolationSet.size() > 0) {
            for (ConstraintViolation<Account> violation : constraintViolationSet) {
               LOG.warn("Violation:" + violation.getMessage() + " / " + violation.getMessageTemplate());
               String errorCode = violation.getMessageTemplate().replace("{", "").replace("}", "");
               errors.rejectValue(violation.getPropertyPath().toString(), errorCode, "Validation error!");
            }
            LOG.warn("Reject: " + account.toString());
            return;
         }

         Account dbAccount = accountRepository.findByUsernameIgnoreCase(account.getUsername());

         if (dbAccount != null && !dbAccount.getId().equals(account.getId())) {
            LOG.warn("Reject: " + account.toString());
            errors.rejectValue("username", "error.username_taken", "Username already taken!");
         }
      }
   }
}
