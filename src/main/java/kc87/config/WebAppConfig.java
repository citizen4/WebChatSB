package kc87.config;

import kc87.domain.Account;
import kc87.web.WsChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.hibernate.validator.HibernateValidator;
import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.SessionTrackingMode;
import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter {

   private static final Logger LOG = LogManager.getLogger(WebAppConfig.class);


   @Bean
   public EmbeddedServletContainerFactory servletContainer() {
      JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();

      //factory.setSessionTimeout(60, TimeUnit.SECONDS);

      factory.addInitializers(servletContext -> {
         servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
         servletContext.getSessionCookieConfig().setName("SID");
      });

      factory.addServerCustomizers(server -> {
         // Add a HTTP connector in addition to HTTPS
         ServerConnector serverConnector = new ServerConnector(server);
         serverConnector.setPort(8080);
         server.addConnector(serverConnector);
         // Disable http server header for all connectors
         for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
               if (connectionFactory instanceof HttpConnectionFactory) {
                  ((HttpConnectionFactory) connectionFactory).getHttpConfiguration().setSendServerVersion(false);
               }
            }
         }
      });

      return factory;
   }


   @Bean
   public DataSource dataSource() {
      return DataSourceBuilder.create()
              .driverClassName("org.hsqldb.jdbcDriver")
              .url("jdbc:hsqldb:file:/tmp/webchat.db;shutdown=true;hsqldb.write_delay=false;")
              .username("sa")
              .password("")
              .type(JDBCDataSource.class)
              .build();
   }

   @Bean
   public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
           EntityManagerFactoryBuilder builder) {
      return builder
              .dataSource(dataSource())
              .packages(Account.class)
              .persistenceUnit("accounts")
              .properties(jpaProperties())
              .build();
   }

   private Map<String,?> jpaProperties() {
      Map<String,Object> properties = new HashMap<>();
      properties.put("hibernate.dialect","org.hibernate.dialect.HSQLDialect");
      properties.put("hibernate.hbm2ddl.auto","update");
      properties.put("hibernate.show_sql","true");
      properties.put("hibernate.format_sql",true);

      return properties;
   }



   @Bean
   public WsChatServer wsChatServer() {
      return new WsChatServer();
   }

   @Bean
   public ServerEndpointExporter serverEndpointExporter() {
      return new ServerEndpointExporter();
   }

   @Bean
   public ServletListenerRegistrationBean httpSessionEventPublisher() {
      return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
   }


   @Bean
   public MessageSource messageSource() {
      ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
      messageSource.setBasename("classpath:i18n/messages");
      messageSource.setCacheSeconds(1);
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
   }

   @Bean
   public LocalValidatorFactoryBean validator() {
      LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
      bean.setProviderClass(HibernateValidator.class);
      bean.setValidationMessageSource(messageSource());
      return bean;
   }

   @Override
   public Validator getValidator() {
      return validator();
   }

   @Override
   public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/login").setViewName("login");
      registry.addViewController("/chat").setViewName("chat");
   }

}
