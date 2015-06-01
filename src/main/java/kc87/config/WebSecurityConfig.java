package kc87.config;


import kc87.service.AccountService;
import kc87.util.CustomPasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
   private static final Logger LOG = LogManager.getLogger(WebSecurityConfig.class);

   @Autowired
   AccountService accountService;

   @Override
   public void configure(WebSecurity security)
   {
      security.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
   }

   @Override
   protected void configure(HttpSecurity http) throws Exception
   {
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
      http.authorizeRequests().antMatchers("/intern/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/chat").hasRole("USER");
      http.authorizeRequests().antMatchers("/login").hasRole("ANONYMOUS");
      http.authorizeRequests().antMatchers("/register").hasRole("ANONYMOUS");
      http.authorizeRequests().anyRequest().permitAll();
   }

   @Override
   public void configure(AuthenticationManagerBuilder auth) throws Exception
   {
      //auth.userDetailsService(inMemoryUserDetailsManager());
      auth.userDetailsService(accountService)
              .passwordEncoder(new CustomPasswordEncoder());
   }

   @Bean
   public InMemoryUserDetailsManager inMemoryUserDetailsManager()
   {
      UserDetails user;
      GrantedAuthority userAuthority = new SimpleGrantedAuthority("ROLE_USER");
      GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
      List<UserDetails> users = new ArrayList<>();
      user = new User("admin", "master", Arrays.asList(userAuthority, adminAuthority));
      users.add(user);
      user = new User("luke", "12345678", Collections.singletonList(userAuthority));
      users.add(user);
      return new InMemoryUserDetailsManager(users);
   }

   @Bean
   public SessionRegistry sessionRegistry()
   {
      return new SessionRegistryImpl();
   }
}
