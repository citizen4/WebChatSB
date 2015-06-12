package kc87.config;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "kc87.repository.mongo")
@SuppressWarnings("unused")
public class PersistenceMongoConfig {

   @Bean
   public MongoDbFactory mongoDbFactory() throws Exception {
      MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
      builder.connectTimeout(1000);
      MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost/webchat",builder);
      return new SimpleMongoDbFactory(mongoClientURI);
   }

   @Bean
   public MongoTemplate mongoTemplate() throws Exception {
      return new MongoTemplate(mongoDbFactory());
   }

}
