package kc87.service;

import kc87.domain.Account;
import kc87.web.RegisterFormBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.Errors;

import java.util.List;


public interface AccountService extends UserDetailsService {
   List<Account> allAccounts();

   void createAccount(final Account account);

   Account prepareAccount(final Account account, final String password);

   Account prepareAccount(final RegisterFormBean formBean);

   Errors validateAccount(final Account account, final Errors errors);

   DefaultAccountService.AccountValidator getAccountValidator();
}
