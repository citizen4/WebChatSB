package kc87.web;

import kc87.domain.Account;
import kc87.service.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.validation.groups.Default;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {
   private static final Logger LOG = LogManager.getLogger(RegisterController.class);

   @Autowired
   AccountService accountService;

   @RequestMapping(method = RequestMethod.GET)
   public String form(final Model model) {
      model.addAttribute("account", new Account());
      return "register";
   }

   @RequestMapping(method = RequestMethod.POST)
   public String handleSubmit(@Validated(value = {Default.class,RegisterFormValidationGroup.class})
                                 Account account, Errors errors) {
      if (!errors.hasErrors()) {
         try {
            final String password = account.getPassword();
            account.setPassword(null);
            accountService.createAccount(account, password);
            // After successful registration, login the user automatically
            autoLogin(account);
            return "redirect:chat";
         } catch (AccountService.UsernameAlreadyTakenException e) {
            errors.rejectValue("username", "error.username_taken", "Username error!");
         }
      }
      return "register";
   }

   private void autoLogin(final Account account) {
      try {
         UserDetails user = accountService.loadUserByUsername(account.getUsername());
         Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (Exception e) {
         LOG.error(e);
      }
   }
}
