package kc87.repository;


import kc87.domain.Account;
import kc87.service.crypto.ScryptPasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@SuppressWarnings("unused")
public class AccountEventHandler {
   private static final Logger LOG = LogManager.getLogger(AccountEventHandler.class);
   private static final PasswordEncoder PASSWORD_ENCODER = new ScryptPasswordEncoder();

   @HandleBeforeCreate
   public void handleAccountCreate(Account account) {
      LOG.debug("HandleBeforeCreate");
      final String password = account.getPassword();
      account.setPassword(PASSWORD_ENCODER.encode(password));
   }

   @HandleBeforeSave
   public void handleAccountSave(Account account) {
      LOG.debug("HandleBeforeSave");
   }
}
