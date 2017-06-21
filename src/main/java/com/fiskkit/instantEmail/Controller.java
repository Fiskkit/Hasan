package com.fiskkit.instantEmail;

import java.io.IOException;
import java.math.BigDecimal;

// import com.chargebee.Environment;
// import com.chargebee.Result;
// import com.chargebee.models.Subscription;
import com.fiskkit.instantEmail.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@RequestMapping(value = "/balance", method = RequestMethod.GET)
	public ResponseEntity<Double> getBalance(@RequestParam(name = "user") String userId) {
		User user = repository.findOne(Long.parseLong(userId));
		return new ResponseEntity<Double>(user.getBalance().doubleValue(), HttpStatus.OK);
	}

	// FIXME should be patch, but spring-boot gives "Request method 'PATCH' not
	// supported" when RequestMethod.PATCH is used here
	@RequestMapping(value = "/balance", method = RequestMethod.POST)
	public ResponseEntity<User> refreshSubscription(@RequestParam(name = "user") String mysqlUserId,
			@RequestParam(name = "email") String email, @RequestParam(name = "first") String firstName,
			@RequestParam(name = "last") String lastName, @RequestParam(name = "amount") BigDecimal amount) {
		Integer phpUser = Integer.parseInt(mysqlUserId);
		User user = repository.findByPhpId(phpUser);
    /*
     // TODO get chargebee configured.
		Result result = null;
		Environment.configure("fiskkit-test.chargebee.com", "test_sClA1cu99xAcdJuoq2OrgK7StavXAFTeKA");
		logger.info("Envionment configured!");
		try {
			result = Subscription.create().id("HwxfyiHNUFzaiWO").planId("starter").customerEmail(email)
					.customerLastName(lastName).customerFirstName(firstName).request();
			logger.info("Subscription received -- " + result.toString());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug(result.toString());
    */

		BigDecimal newBalance = user.getBalance().add(amount);

		user.setBalance(newBalance);
		repository.save(user);
		logger.info("Balance adjusted for " + user.getPhpId() + " to "+newBalance.toString());
		return new ResponseEntity<User>(repository.findByPhpId(phpUser), HttpStatus.CREATED);
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
