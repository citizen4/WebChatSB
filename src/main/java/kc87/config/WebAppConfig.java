package kc87.config;

import kc87.web.WsChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.SessionTrackingMode;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;


@Configuration
@SuppressWarnings("unused")
public class WebAppConfig extends WebMvcConfigurerAdapter {
   private static final Logger LOG = LogManager.getLogger(WebAppConfig.class);

   @Value("${webchat.http.port}")
   private int httpPort;

   @Value("${webchat.https.port}")
   private int httpsPort;

   @Value("${webchat.session.timeout}")
   private int sessionTimeout;

   @Value("${webchat.session.cookiename}")
   private String cookieName;


   @Bean
   public WsChatServer wsChatServer() {
      WsChatServer chatServer = new WsChatServer();
      chatServer.setChatSessionTimeout(sessionTimeout);
      return chatServer;
   }

   @Bean
   public ServerEndpointExporter serverEndpointExporter() {
      return new ServerEndpointExporter();
   }

   @Bean
   public ServletListenerRegistrationBean httpSessionEventPublisher() {
      return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
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
      registry.addViewController("/").setViewName("index");
      registry.addViewController("/index.*").setViewName("index");
      registry.addViewController("/login").setViewName("login");
      registry.addViewController("/chat").setViewName("chat");
   }


   @Bean
   public EmbeddedServletContainerFactory servletContainer() {
      JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();

      factory.setSessionTimeout(sessionTimeout, TimeUnit.SECONDS);

      factory.addInitializers(servletContext -> {
         servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
         servletContext.getSessionCookieConfig().setName(cookieName);
      });

      factory.addServerCustomizers(server -> {
         ServerConnector defaultConnector = (ServerConnector) (server.getConnectors())[0];
         defaultConnector.setPort(httpPort);
         addHttpsConnector(server);
         // Disable http server header for all connectors
         for (Connector connector : server.getConnectors()) {
            //LOG.debug(((ServerConnector) connector).getName());
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
               if (connectionFactory instanceof HttpConnectionFactory) {
                  HttpConfiguration httpConfiguration = ((HttpConnectionFactory) connectionFactory)
                          .getHttpConfiguration();
                  httpConfiguration.setSendServerVersion(false);
               }
            }
         }
      });

      return factory;
   }

   private void addHttpConnector(final Server server) {
      ServerConnector serverConnector = new ServerConnector(server);
      serverConnector.setPort(httpPort);
      server.addConnector(serverConnector);
   }

   private void addHttpsConnector(final Server server) {
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePassword("topsecret");
      sslContextFactory.setKeyManagerPassword("topsecret");
      sslContextFactory.setKeyStorePath(WebAppConfig.class.getResource("/keystore.jks").toExternalForm());
      sslContextFactory.setKeyStoreType("JKS");
      sslContextFactory.setNeedClientAuth(false);

      HttpConfiguration httpsConfiguration = new HttpConfiguration();
      httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

      ServerConnector httpsConnector = new ServerConnector(server,
              new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
              new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setPort(httpsPort);
      server.addConnector(httpsConnector);
   }

}
