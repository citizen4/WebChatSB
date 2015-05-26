package kc87.service;

import kc87.domain.Account;
import kc87.repository.AccountRepository;
import kc87.util.PasswordCrypto;
import kc87.web.WsChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Service
@SuppressWarnings("unused")
public class SessionService implements ApplicationListener<ApplicationEvent>
{
   private static final Logger LOG = LogManager.getLogger(SessionService.class);
   private static List<Consumer<String>> callbacks = new LinkedList<>();

   @Override
   public void onApplicationEvent(ApplicationEvent event) {

      if (event instanceof HttpSessionCreatedEvent) {
         HttpSessionCreatedEvent sessionCreated = (HttpSessionCreatedEvent) event;
         HttpSession session = sessionCreated.getSession();
         LOG.debug("HTTP session created: " + session.toString());
         return;
      }

      if (event instanceof HttpSessionDestroyedEvent) {
         HttpSessionDestroyedEvent sessionDestroyed = (HttpSessionDestroyedEvent) event;
         for(SecurityContext context : sessionDestroyed.getSecurityContexts()) {
            User user = (User) context.getAuthentication().getPrincipal();
            LOG.debug("HTTP session destroyed for user: "+ user.getUsername());
            for(Consumer<String> consumer : callbacks) {
               consumer.accept(user.getUsername());
            }
         }
      }

      /*
      if (event instanceof AuthenticationSuccessEvent) {
         AuthenticationSuccessEvent authenticationSuccess = (AuthenticationSuccessEvent) event;
         LOG.debug("Authentication successful: " + authenticationSuccess.toString());
      }*/

   }


   public synchronized void addOnSessionDestroyedListener(Consumer<String> callback) {
      callbacks.add(callback);
   }

   public synchronized void removeOnSessionDestroyedListener(Consumer<String> callback) {
      callbacks.remove(callback);
   }

}
