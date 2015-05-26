package kc87.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/chat")
public class ChatController {
   private static final Logger LOG = LogManager.getLogger(ChatController.class);


   @RequestMapping(method = RequestMethod.GET)
   public String chat(HttpSession session,Model model) {

      if(session == null || session.getAttribute("wsSession") != null){
         return "redirect:/error";
      }


      //LOG.debug(model.toString());
      if(!model.containsAttribute("username")){
         model.addAttribute("username","");
      }

      return "chat";
   }
}
