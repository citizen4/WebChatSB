package kc87.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "kc87.repository", enableDefaultTransactions = true)
@SuppressWarnings("unused")
public class PersistenceConfig {

   @Bean
   public DataSource dataSource() {
      HikariConfig config = new HikariConfig();
      config.setDriverClassName("org.hsqldb.jdbcDriver");
      config.setJdbcUrl("jdbc:hsqldb:file:/tmp/webchat.db;shutdown=true;hsqldb.write_delay=false;");
      config.setUsername("sa");
      config.setPassword("");

      HikariDataSource dataSource = new HikariDataSource(config);
      dataSource.setMaximumPoolSize(50);
      return dataSource;
   }

   @Bean
   public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
      LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
      Map<String, Object> jpaProperties = new HashMap<>();

      jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
      jpaProperties.put("hibernate.hbm2ddl.auto", "update");
      jpaProperties.put("hibernate.show_sql", true);
      jpaProperties.put("hibernate.format_sql", true);

      factoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
      factoryBean.setDataSource(dataSource());
      factoryBean.setPackagesToScan("kc87.domain");
      factoryBean.setJpaPropertyMap(jpaProperties);
      factoryBean.setValidationMode(ValidationMode.NONE);

      return factoryBean;
   }

   /*
   @Bean
   public Jackson2RepositoryPopulatorFactoryBean repositoryPopulator() {
      Jackson2RepositoryPopulatorFactoryBean factoryBean = new Jackson2RepositoryPopulatorFactoryBean();
      Resource sourceData = new ClassPathResource("db/accounts.json");
      factoryBean.setResources(new Resource[] { sourceData });
      return factoryBean;
   }*/
}
