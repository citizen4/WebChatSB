package kc87.config;


import kc87.domain.Account;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.MediaType;

@Configuration
public class RestConfig extends RepositoryRestMvcConfiguration {

   @Override
   public RepositoryRestConfiguration config() {
      RepositoryRestConfiguration restConfiguration = super.config();
      restConfiguration.setBasePath("/service");
      restConfiguration.setDefaultMediaType(MediaType.APPLICATION_JSON);
      restConfiguration.useHalAsDefaultJsonMediaType(true);
      restConfiguration.exposeIdsFor(Account.class);
      return restConfiguration;
   }
}
