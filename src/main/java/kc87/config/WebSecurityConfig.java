package kc87.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;


@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

   private static final Logger LOG = LogManager.getLogger(WebSecurityConfig.class);

   @Override
   protected void configure(HttpSecurity http) throws Exception {
      //http.sessionManagement().enableSessionUrlRewriting(true);
      http.sessionManagement()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .sessionRegistry(sessionRegistry());
      http.formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/chat");
      http.logout()
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true);
      http.httpBasic().disable();
      http.csrf().disable();
      http.authorizeRequests().antMatchers("/intern/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/chat/**").hasRole("USER");
      //http.authorizeRequests().antMatchers("/login").hasRole("ANONYMOUS");
      http.authorizeRequests().anyRequest().permitAll();
   }

   @Override
   public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.inMemoryAuthentication().withUser("admin").password("master").roles("USER", "ADMIN");
      auth.inMemoryAuthentication().withUser("luke").password("12345678").roles("USER");
   }


   @Bean
   public SessionRegistry sessionRegistry() {
      return new SessionRegistryImpl();
   }

      /*
      @Bean
      public static ServletListenerRegistrationBean httpSessionEventPublisher() {
         return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
      }*/

}
