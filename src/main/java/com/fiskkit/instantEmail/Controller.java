package com.fiskkit.instantEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.chargebee.Environment;
import com.chargebee.models.Subscription;
import com.fiskkit.instantEmail.models.User;
import com.fiskkit.instantEmail.models.mysql.Users;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

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
		logger.info("susbscription id requested: " + subscriptionId);
		try {
			Subscription.retrieve(subscriptionId).request().subscription().status();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();

			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.FAILED_DEPENDENCY);
		}

		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> newOrg(@RequestParam(name = "id") String organizationUniqueId,
			@RequestParam(name = "subscription") String subscriptionId) {
		User user = new User();
		user.setPhpId(Integer.parseInt(organizationUniqueId));
		user.setChargebeeId(subscriptionId);
		repository.save(user);
		return new ResponseEntity<String>(user.toString(), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/callback", method = RequestMethod.POST)
	public ResponseEntity<String> chargebeeWebhooks(@RequestParam Map<String, String> params,
			@RequestBody Map<String, Map<String, Map<String, String>>> body) {
		String customerId = body.get("content").get("customer").get("id");
		String customerFirstName = body.get("content").get("customer").get("first_name");
		String customerLastName = body.get("content").get("customer").get("last_name");
		User user = new User();
		user.setChargebeeId(customerId);
		Type targetClassType = new TypeToken<List<Users>>() {
		}.getType();
		Gson gson = new Gson();
		List<Users> users = null;
		try {
			users = gson.fromJson((String) new URL("http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users/")
					.openConnection().getContent(), targetClassType);
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Users userFromFiskkit : users) {
			if (userFromFiskkit.getFirst_name().equals(customerFirstName)
					&& userFromFiskkit.getLast_name().equals(customerLastName)) {
				user.setPhpId(userFromFiskkit.getUserId());
				repository.save(user);
				return new ResponseEntity<String>("null", HttpStatus.OK);
			}
		}
		return new ResponseEntity<String>("error", HttpStatus.EXPECTATION_FAILED);

	}

	@RequestMapping(value = "/readability", method = RequestMethod.POST)
	public ResponseEntity<Double> readability(@RequestBody String text) {
		Double ADJUSTMENT = 3.6365, score = 0.0, DIFFICULT_WORD_THRESHOLD = 0.05;
		String[] wordsInText = text.split("[\\W]");
		HashSet<String> words = (HashSet<String>) Arrays.stream(wordsInText).collect(Collectors.toSet());
		HashSet<String> simpleWords = new HashSet<String>();
		BufferedReader simpleList = null;
		try {
			simpleList = new BufferedReader(new InputStreamReader(
					new URL("http://countwordsworth.com/download/DaleChallEasyWordList.txt").openStream()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		String word;
		try {
			while ((word = simpleList.readLine()) != null) {
				simpleWords.add(word);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		words.retainAll(simpleWords);
		int countsSimpleWords = words.size();
		float pctSimple = countsSimpleWords / wordsInText.length;
		if (pctSimple > DIFFICULT_WORD_THRESHOLD) {
			score = score + ADJUSTMENT;
		}
		return new ResponseEntity<Double>(score, HttpStatus.OK);
	}

	@RequestMapping(value = "/entities", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Set<String>>> getEntities(@RequestParam(name = "loc") String location) {
		BufferedReader contents = null;
		try {
			contents = new BufferedReader(new InputStreamReader(new URL(location).openStream()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		String text = null, line = null;
		try {
			while ((line = contents.readLine()) != null) {
				text = text + line + "\n";
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		Map<String, Set<String>> map = new HashMap<>();
		String serializedClassifier = this.getClass()
				.getResource("edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz").toString();

		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		List<List<CoreLabel>> classify = classifier.classify(text);
		for (List<CoreLabel> coreLabels : classify) {
			for (CoreLabel coreLabel : coreLabels) {

				String word = coreLabel.word();
				String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
				if (!"O".equals(category)) {
					if (map.containsKey(category)) {
						// key is already their just insert
						map.get(category).add(word);
					} else {
						LinkedHashSet<String> temp = new LinkedHashSet<String>();
						temp.add(word);
						map.put(category, temp);
					}
				}

			}

		}
		return new ResponseEntity<Map<String, Set<String>>>(map, HttpStatus.OK);
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
