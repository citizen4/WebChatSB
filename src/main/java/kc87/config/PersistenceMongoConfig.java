package kc87.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@Configuration
@EnableMongoRepositories(basePackages = {
         "kc87.repository.mongo"
        //,"kc87.repository.generic"
})
@SuppressWarnings("unused")
public class PersistenceMongoConfig {

   @Value("${webchat.mongodb.url}")
   private String connectionUrl;

   @Bean
   public MongoDbFactory mongoDbFactory() throws Exception {
      MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
      builder.connectTimeout(1000);
      MongoClientURI mongoClientURI = new MongoClientURI(connectionUrl, builder);
      return new SimpleMongoDbFactory(mongoClientURI);
   }

   @Bean
   public MongoTemplate mongoTemplate() throws Exception {
      return new MongoTemplate(mongoDbFactory());
   }
}
