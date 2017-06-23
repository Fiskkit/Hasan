package com.fiskkit.instantEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.Customer;
import com.chargebee.models.PaymentSource;
import com.chargebee.models.Subscription;
import com.chargebee.models.enums.Gateway;
import com.fiskkit.instantEmail.models.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
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

	@RequestMapping(value = "/valid", method = RequestMethod.GET)
	public ResponseEntity<Boolean> getBalance(@RequestParam(name = "subscription") String subscriptionId) {
		Subscription subscription = null;
		try {
			subscription = Subscription.retrieve(subscriptionId).request().subscription();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		Boolean balance = subscription.startDate().after(new Date())
				&& subscription.currentTermEnd().before(new Date());
		return new ResponseEntity<Boolean>(balance, HttpStatus.OK);
	}

	// FIXME should be patch, but spring-boot gives "Request method 'PATCH' not
	// supported" when RequestMethod.PATCH is used here
	@RequestMapping(value = "/balance", method = RequestMethod.POST)
	public ResponseEntity<User> refreshSubscription(@RequestParam(name = "user") String mysqlUserId,
			@RequestParam(name = "email") String email, @RequestBody Map<String, String> billingAddress) {
		Integer phpUser = Integer.parseInt(mysqlUserId);
		User user = repository.findByPhpId(phpUser);

		Result result = null;
		Environment.configure("fiskkit-test.chargebee.com", "test_sClA1cu99xAcdJuoq2OrgK7StavXAFTeKA");
		logger.info("Envionment configured!");
		try {
			URL url = new URL("http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users/" + phpUser);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1) {
				buffer.append(chars, 0, read);
			}
			JsonObject json = new JsonPrimitive(buffer.toString()).getAsJsonObject();
			JsonObject userMap = json.getAsJsonObject("user");
			String firstName = userMap.get("first_name").getAsString();
			String lastName = userMap.get("last_name").getAsString();
			result = Customer.create().firstName(firstName).lastName(lastName).email(email)
					.billingAddressFirstName(firstName).billingAddressLastName(lastName)
					.billingAddressLine1(billingAddress.get("street")).billingAddressCity(billingAddress.get("city"))
					.billingAddressState(billingAddress.get("state")).billingAddressZip(billingAddress.get("zip"))
					.billingAddressCountry("US").request();
			Customer customer = result.customer();
			result = PaymentSource.createCard().customerId(customer.id()).cardFirstName(firstName)
					.cardLastName(lastName).cardGatewayAccountId(Gateway.CHARGEBEE.toString())
					.cardNumber(billingAddress.get("cardNumber"))
					.cardExpiryMonth(Integer.parseInt(billingAddress.get("expirationMonth")))
					.cardExpiryYear(Integer.parseInt(billingAddress.get("expirationYear")))
					.cardCvv(billingAddress.get("cvv")).request();
			user.setPaymentSourceId(result.paymentSource().id());
			user.setChargebeeId(result.customer().id());
			repository.save(user);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug(result.toString());
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
