package kc87.config;

import kc87.service.AccountService;
import kc87.util.CustomPasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
   private static final Logger LOG = LogManager.getLogger(WebSecurityConfig.class);

   @Autowired
   AccountService accountService;

   @Override
   public void configure(WebSecurity security) {
      security.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico");
   }

   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http.sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
              .sessionFixation()
              .changeSessionId()
              .maximumSessions(2)
              .maxSessionsPreventsLogin(false)
              .expiredUrl("/login?expired")
              .sessionRegistry(sessionRegistry());
      http.formLogin()
              .loginPage("/login")
              .failureUrl("/login?error")
              .defaultSuccessUrl("/chat");
      http.logout()
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout")
              .deleteCookies("SID")
              .invalidateHttpSession(true);
      http.httpBasic().disable();
      http.csrf().disable();
      // Access to the service endpoint is handled by the REST controller itself
      http.authorizeRequests().antMatchers("/", "/index.*","/service/**").permitAll();
      http.authorizeRequests().antMatchers("/intern/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/chat").hasRole("USER");
      http.authorizeRequests().antMatchers("/login", "/register").anonymous();
      http.authorizeRequests().anyRequest().fullyAuthenticated();
   }

   @Override
   public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(accountService).passwordEncoder(new CustomPasswordEncoder());
   }

   @Bean
   public SessionRegistry sessionRegistry() {
      return new SessionRegistryImpl();
   }

}
