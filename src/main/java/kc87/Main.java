package kc87;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Main {

   public static void main(String[] args) {
      SpringApplication springApplication = new SpringApplication(Main.class);
      springApplication.setWebEnvironment(true);
      springApplication.run(args);
   }

   /*
   @Bean
   public DispatcherServlet dispatcherServlet() {
      return new DispatcherServlet();
   }

   @Bean
    public ServletRegistrationBean webServlet() {
      //DispatcherServlet dispatcherServlet = new DispatcherServlet();
      //AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();

      //applicationContext.register(WebAppConfig.class, WebSecurityConfig.class);
      //dispatcherServlet.setApplicationContext(applicationContext);

      ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet(),"/web/*");
      servletRegistrationBean.setName("web");
      return servletRegistrationBean;
   }*/

}
