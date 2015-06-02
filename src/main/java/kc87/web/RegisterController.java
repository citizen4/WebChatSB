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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

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
   public String handleSubmit(@Valid Account account, Errors errors) {

      LOG.debug("Errors: " + errors.toString());

      if (!errors.hasErrors()) {
         if(accountService.createAccount(account)) {
            autoLogin(account);
            return "redirect:chat";
         }else{
            errors.rejectValue("username",null,"This username is already taken!");
            LOG.debug("Errors: " + errors.toString());
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
