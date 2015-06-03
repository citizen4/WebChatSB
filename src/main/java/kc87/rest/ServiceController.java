package kc87.rest;


import kc87.domain.Account;
import kc87.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
@SuppressWarnings("unused")
public class ServiceController {

   @Autowired
   AccountService accountService;

   @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public Account[] accounts() {
       return accountService.allAccounts();
    }

}
