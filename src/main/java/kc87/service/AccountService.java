package kc87.service;

import kc87.domain.Account;
import kc87.web.RegisterFormBean;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;


public interface AccountService extends UserDetailsService {
   List<Account> allAccounts();
   void createAccount(final Account account, final String password);
   void createAccount(final RegisterFormBean formBean);
}
