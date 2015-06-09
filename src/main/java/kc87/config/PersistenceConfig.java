package kc87.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import kc87.domain.Account;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@SuppressWarnings("unused")
public class PersistenceConfig {

   @Bean
   public PlatformTransactionManager transactionManager() {
      JpaTransactionManager transactionManager = new JpaTransactionManager();
      transactionManager.setDataSource(dataSource());
		return transactionManager;
	}

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
      //factoryBean.setValidationMode(ValidationMode.NONE);

      return factoryBean;
   }

}
