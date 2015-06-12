package kc87.web;

import kc87.service.AccountService;
import kc87.service.DefaultAccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {
   private static final Logger LOG = LogManager.getLogger(RegisterController.class);

   @Autowired
   AccountService accountService;

   @RequestMapping(method = RequestMethod.GET)
   public ModelAndView form(final ModelAndView modelView) {
      modelView.setViewName("register");
      modelView.addObject(new RegisterFormBean());
      return modelView;
   }

   @RequestMapping(method = RequestMethod.POST)
   public String handleSubmit(@Valid RegisterFormBean formBean, BindingResult result) {
      if (!result.hasErrors()) {
         LOG.debug(formBean.toString());
         try {
            accountService.createAccount(formBean);
            // After successful registration, login the user automatically
            autoLogin(formBean.getUsername());
            return "redirect:chat";
         } catch (DefaultAccountService.UsernameAlreadyTakenException e) {
            result.rejectValue("username", "error.username_taken", "Username error!");
         }
      }
      return "register";
   }

   private void autoLogin(final String username) {
      try {
         UserDetails user = accountService.loadUserByUsername(username);
         Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (Exception e) {
         LOG.error(e);
      }
   }
}
