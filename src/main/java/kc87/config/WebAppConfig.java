package kc87.config;

import kc87.web.WsChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

   @Autowired
   WebChatProperties webChatProperties;

   @Bean
   public WsChatServer wsChatServer() {
      WsChatServer chatServer = new WsChatServer();
      chatServer.setChatSessionTimeout(webChatProperties.getSessionTimeout());
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
      registry.addViewController("/intern").setViewName("dashboard");
   }


   @Bean
   public EmbeddedServletContainerFactory servletContainer() {
      JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();

      LOG.debug(webChatProperties.getKeystoreFile());

      factory.setSessionTimeout(webChatProperties.getSessionTimeout(), TimeUnit.SECONDS);

      factory.addInitializers(servletContext -> {
         servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
         servletContext.getSessionCookieConfig().setName(webChatProperties.getSessionCookieName());
      });

      factory.addServerCustomizers(server -> {
         if(webChatProperties.isHttpLogging()) {
            setupRequestLogging(server);
         }
         ServerConnector defaultConnector = (ServerConnector) (server.getConnectors())[0];
         defaultConnector.setPort(webChatProperties.getHttpPort());
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

   private void setupRequestLogging(final Server server) {
      HandlerCollection handlers = new HandlerCollection();

      for (Handler handler : server.getHandlers()) {
         handlers.addHandler(handler);
      }

      RequestLogHandler requestLogHandler = new RequestLogHandler();

      NCSARequestLog requestLog = new NCSARequestLog(webChatProperties.getHttpLogfile());
      requestLog.setRetainDays(30);
      requestLog.setAppend(true);
      requestLog.setExtended(false);
      requestLog.setLogTimeZone("CET");
      requestLogHandler.setRequestLog(requestLog);

      handlers.addHandler(requestLogHandler);
      server.setHandler(handlers);
   }

   private void addHttpConnector(final Server server) {
      ServerConnector serverConnector = new ServerConnector(server);
      serverConnector.setPort(webChatProperties.getHttpPort());
      server.addConnector(serverConnector);
   }

   private void addHttpsConnector(final Server server) {
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePassword(webChatProperties.getKeystorePassword());
      sslContextFactory.setKeyManagerPassword(webChatProperties.getKeymanagerPassword());
      sslContextFactory.setKeyStorePath(WebAppConfig.class.getResource(webChatProperties.getKeystoreFile()).toExternalForm());
      sslContextFactory.setKeyStoreType(webChatProperties.getKeystoreType());
      sslContextFactory.setNeedClientAuth(false);

      HttpConfiguration httpsConfiguration = new HttpConfiguration();
      httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

      ServerConnector httpsConnector = new ServerConnector(server,
              new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
              new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setPort(webChatProperties.getHttpsPort());
      server.addConnector(httpsConnector);
   }
}
