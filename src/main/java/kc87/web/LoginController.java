package kc87.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping(value = "/login")
public class LoginController {
   private static final Logger LOG = LogManager.getLogger(LoginController.class);

   @RequestMapping(method = RequestMethod.GET)
   public ModelAndView login(@RequestParam(value = "error", required = false) String error,
		                       @RequestParam(value = "logout", required = false) String logout,
                             UsernamePasswordAuthenticationToken token) {

      ModelAndView model = new ModelAndView();
      model.setViewName("login");

      LOG.debug("Login request");

      if(token != null) {
         LOG.debug("Token: " + token.toString());
      }

      if(token != null && token.isAuthenticated()){
         LOG.debug("Already logged in!");
         model.addObject("loggedin","");
      }

      if(error != null){
         LOG.debug("Login error");
         model.addObject("error", "");
      }

      if(logout != null) {
         LOG.debug("Logout");
         model.addObject("logout","");
      }

      return model;
   }
}
