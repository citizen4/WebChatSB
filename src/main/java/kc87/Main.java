package kc87;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {

   public static void main(String[] args)
   {
      SpringApplication springApplication = new SpringApplication(Main.class);
      springApplication.setWebEnvironment(true);
      springApplication.run(args);
   }
}