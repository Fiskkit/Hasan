package com.fiskkit.instantEmail;

import java.io.IOException;

import com.chargebee.Environment;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.Status;
import com.fiskkit.instantEmail.models.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class Controller {
	private static Logger logger = LoggerFactory.getLogger(Controller.class);

	@Autowired
	UserRepository repository;

	@Value("${chargebee.applicationEnvironment}")
	String chargebeeEnvironment;

	@Value("${chargebee.applicationSecret}")
	String chargebeeSecret;

	@RequestMapping(value = "/valid", method = RequestMethod.GET)
	public ResponseEntity<Boolean> getBalance(@RequestParam(name = "subscription") String subscriptionId) {
		Environment.configure(chargebeeEnvironment, chargebeeSecret);
		Status status = null;
		logger.info("susbscription id requested: " + subscriptionId);
		try {
			status = Subscription.retrieve(subscriptionId).request().subscription().status();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		return new ResponseEntity<Boolean>(status == Status.ACTIVE, HttpStatus.OK);
	}

	@Bean
	public User user() {
		return new User();
	}

	@Bean
	public UserRepository getRepo() {
		return repository;
	}
}
