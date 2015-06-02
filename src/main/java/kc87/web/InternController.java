package kc87.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/intern")
public class InternController {
   private static final Logger LOG = LogManager.getLogger(InternController.class);

   @RequestMapping(value = {"", "dashboard"}, method = RequestMethod.GET)
   public String dashboard() {
      return "dashboard";
   }
}
