package com.fiskkit.instantEmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.fiskkit.instantEmail.models")
@EntityScan("com.fiskkit.instantEmail.models")
public class Application {
  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(Config.class);
    SpringApplication.run(Application.class, args);
  }
}
