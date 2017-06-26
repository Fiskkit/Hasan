package com.fiskkit.instantEmail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	private static Logger logger = LoggerFactory.getLogger("com.fiskkit.instantEmail.Application");

	@Value("${local.server.port}")
	static int portNumber;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		logger.error("Application bound to " + portNumber);
	}
}
