package com.fiskkit.instantEmail;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.Subscription;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiskkit.instantEmail.models.User;

import org.apache.commons.io.IOUtils;
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
	@Autowired(required = false)
	UserRepository repository;

	@RequestMapping(value = "/newuser", method = RequestMethod.POST)
	public ResponseEntity<User> newUser(@RequestParam String phpUserId) {
		User user = new User();
		user.setPhpUserId(Integer.parseInt(phpUserId));
		repository.save(user);
		return new ResponseEntity<User>(repository.findByPhpId(Integer.parseInt(phpUserId)), HttpStatus.CREATED);

	}

	@RequestMapping(value = "/balance", method = RequestMethod.GET)
	public ResponseEntity<String> getBalance(@RequestParam(name = "user") String mysqlUserId) {
		Integer mysqlUser = new Integer(mysqlUserId);
		String USER_URL = "http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users/" + mysqlUser;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> user = null;
		try {
			user = mapper.readValue(IOUtils.toString(new URL(USER_URL).openStream(), "UTF-8"),
					new TypeReference<Map<String, String>>() {
					});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<String>(user.get("balance"), HttpStatus.OK);
	}

	// FIXME should be patch, but spring-boot gives "Request method 'PATCH' not
	// supported" when RequestMethod.PATCH is used here
	@RequestMapping(value = "/balance", method = RequestMethod.POST)
	public ResponseEntity<User> refreshSubscription(@RequestParam(name = "user") String mysqlUserId,
			@RequestParam(name = "amount") BigDecimal amount) {
		Integer phpUser = Integer.parseInt(mysqlUserId);
		User user = repository.findByPhpId(phpUser);
		String USER_URL = "http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users/" + phpUser;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> userFromFiskkit = null;
		try {
			user = mapper.readValue(IOUtils.toString(new URL(USER_URL).openStream(), "UTF-8"),
					new TypeReference<Map<String, String>>() {
					});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Result result = null;
		try {
			Environment.configure(System.getProperty("chargbee.site"), System.getProperty("chargebee.password"));
			logger.info("Envionment configured!");
			result = Subscription.create().id("HwxfyiHNUFzaiWO").planId("starter")
					.customerEmail(userFromFiskkit.get("email")).customerLastName(userFromFiskkit.get("last_name"))
					.customerFirstName(userFromFiskkit.get("first_name")).request();
			logger.info("Subscription requested");

		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug(result.toString());

		BigDecimal newBalance = user.getBalance().subtract(amount);

		user.setBalance(newBalance);
		repository.save(user);
		logger.info("Subscription adjusted for" + user.getPhpUserId());
		return new ResponseEntity<User>(repository.findByPhpId(phpUser), HttpStatus.valueOf(209));
	}

	@Bean
	public User newUser() {
		return new User();
	}

}
