package kc87.web;

import kc87.domain.Account;

import kc87.service.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.util.Locale;

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
   public String handleSubmit(RedirectAttributes attrs, @Valid Account account, Errors errors) {

      if(!errors.hasErrors()) {
         accountService.createAccount(account);
         attrs.addFlashAttribute("username", account.getUsername());
         return "redirect:chat";
      }

      return "register";
   }
}
