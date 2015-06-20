package kc87.config;

import kc87.service.AccountService;
import kc87.service.crypto.ScryptPasswordEncoder;
import kc87.service.crypto.SimplePasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
   private static final Logger LOG = LogManager.getLogger(WebSecurityConfig.class);
   private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");

   @Value("${webchat.session.cookiename}")
   private String cookieName;

   @Autowired
   AccountService accountService;

   @Bean
   public SessionRegistry sessionRegistry() {
      return new SessionRegistryImpl();
   }

   @Override
   public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(accountService).passwordEncoder(new ScryptPasswordEncoder());
   }

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
              .successHandler((request, response, authentication) -> {
                 String successUrl = "/chat";
                 RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
                 if (authentication.getAuthorities().contains(ADMIN_AUTHORITY)) {
                    successUrl = "/intern";
                 }
                 redirectStrategy.sendRedirect(request, response, successUrl);
              });
      //.defaultSuccessUrl("/chat");
      http.logout()
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout")
              .deleteCookies(cookieName)
              .invalidateHttpSession(true);

      http.httpBasic().disable();
      http.csrf().disable();

      http.exceptionHandling().accessDeniedHandler(this::handleServiceAccess);
      http.exceptionHandling().authenticationEntryPoint(this::handleServiceAccess);

      http.authorizeRequests().antMatchers("/service/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/", "/index.*").permitAll();
      http.authorizeRequests().antMatchers("/intern/**").hasRole("ADMIN");
      http.authorizeRequests().antMatchers("/chat").hasRole("USER");
      http.authorizeRequests().antMatchers("/login", "/register").anonymous();
      http.authorizeRequests().anyRequest().fullyAuthenticated();
   }

   private void handleServiceAccess(HttpServletRequest request, HttpServletResponse response,
                                    final RuntimeException accessException) throws IOException {
      RequestMatcher serviceMatcher = new AntPathRequestMatcher("/service/**");
      LOG.debug("handleServiceAccess: " + accessException.toString());

      int httpResponseStatus = (accessException instanceof AccessDeniedException) ?
             HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;

      if(serviceMatcher.matches(request)) {
         // No error page for service requests
         response.setStatus(httpResponseStatus);
      }else {
         // Show error page
         response.sendError(httpResponseStatus);
      }
   }

}
