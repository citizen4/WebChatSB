package kc87.rest;


import kc87.domain.Account;
import kc87.service.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service")
@SuppressWarnings("unused")
public class ServiceController {
   private static final Logger LOG = LogManager.getLogger(ServiceController.class);
   private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");

   @Autowired
   AccountService accountService;

   @RequestMapping(value = "/accounts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
   public Account[] accounts(Authentication authentication) {
      checkPermissions(authentication);
      List<Account> accounts = accountService.allAccounts();
      return accounts.toArray(new Account[accounts.size()]);
   }

   private void checkPermissions(final Authentication authentication) {
      if(authentication != null && authentication.isAuthenticated()) {
         if(authentication.getAuthorities().contains(ADMIN_AUTHORITY)){
            LOG.debug("Access granted!");
            return;
         }
      }
      LOG.debug("Access denied!");
      throw new RestException(HttpStatus.UNAUTHORIZED,null);
   }


   @ExceptionHandler(RestException.class)
   private ResponseEntity<Object> invalidRequest(RestException exception) {
      ResponseEntity<Object> responseEntity;

      switch (exception.getCode()) {
         case UNAUTHORIZED:
            responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            break;
         default:
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            break;
      }

      return responseEntity;
   }


   public static class RestException extends RuntimeException {

      private HttpStatus code;

      public RestException(final HttpStatus code, final String message) {
         super(message);
         this.code = code;
      }

      public HttpStatus getCode() {
         return this.code;
      }
   }

}
