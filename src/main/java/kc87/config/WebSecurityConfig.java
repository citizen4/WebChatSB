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
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
   private static final Logger LOG = LogManager.getLogger(WebSecurityConfig.class);

   @Autowired
   AccountService accountService;

   @Override
   public void configure(WebSecurity security) {
      // Speed up access of static content
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
      http.authorizeRequests().antMatchers("/").permitAll();
      http.authorizeRequests().antMatchers("/index.*").permitAll();
      http.authorizeRequests().antMatchers("/intern/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/chat").hasRole("USER");
      http.authorizeRequests().antMatchers("/login").anonymous();
      http.authorizeRequests().antMatchers("/register").anonymous();
      //http.authorizeRequests().anyRequest().fullyAuthenticated();
      http.authorizeRequests().anyRequest().permitAll();
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
