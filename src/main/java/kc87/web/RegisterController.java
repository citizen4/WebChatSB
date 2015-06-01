package kc87.web;

import kc87.domain.Account;

import kc87.service.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

@Controller
@RequestMapping(value = "/register")
public class RegisterController
{
   private static final Logger LOG = LogManager.getLogger(RegisterController.class);

   @Autowired
   AccountService accountService;

   @Autowired
   private InMemoryUserDetailsManager userDetailsManager;

   @RequestMapping(method = RequestMethod.GET)
   public String form(final Model model)
   {
      model.addAttribute("account", new Account());
      return "register";
   }

   @RequestMapping(method = RequestMethod.POST)
   public String handleSubmit(HttpServletRequest request, @Valid Account account, Errors errors)
   {

      if (!errors.hasErrors()) {
         accountService.createAccount(account);
         //addInMemoryUser(account);
         autoLogin(request, account);
         return "redirect:chat";
      }

      return "register";
   }


   private void autoLogin(final HttpServletRequest request, final Account account)
   {
      try {
         SecurityContext securityContext = SecurityContextHolder.getContext();
         UsernamePasswordAuthenticationToken token =
                 new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword());
         token.setDetails(new WebAuthenticationDetails(request));
         DaoAuthenticationProvider authenticator = new DaoAuthenticationProvider();
         //authenticator.setUserDetailsService(userDetailsManager);
         authenticator.setUserDetailsService(accountService);
         Authentication authentication = authenticator.authenticate(token);
         securityContext.setAuthentication(authentication);
      } catch (Exception e) {
         LOG.error(e);
      }
      // Create a new session and add the security context.
      //HttpSession session = request.getSession(true);
      //session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
   }


   private void addInMemoryUser(final Account account)
   {
      if (!userDetailsManager.userExists(account.getUsername())) {
         UserDetails newUser = new User(account.getUsername(),
                 account.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
         userDetailsManager.createUser(newUser);
      }
   }

}
