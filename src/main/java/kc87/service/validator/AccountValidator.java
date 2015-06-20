package kc87.service.validator;

import kc87.domain.Account;
import kc87.repository.generic.AccountRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import javax.validation.ConstraintViolation;
import java.util.Set;


@Component
public class AccountValidator implements Validator {
   private static final Logger LOG = LogManager.getLogger(AccountValidator.class);

   @Autowired
   private AccountRepository accountRepository;

   @Autowired
   private LocalValidatorFactoryBean validatorFactory;

   @Override
   public boolean supports(Class<?> aClass) {
      return Account.class.equals(aClass);
   }

   @Override
   public void validate(Object o, Errors errors) {
      Account account = (Account) o;

      Set<ConstraintViolation<Account>> constraintViolationSet = validatorFactory.getValidator().validate(account);

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
