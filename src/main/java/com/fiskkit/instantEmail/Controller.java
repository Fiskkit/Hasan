package com.fiskkit.instantEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;

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

	@RequestMapping(value = "/analyze", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> statistics(@RequestBody String text) {
		try {
			text = URLDecoder.decode(text, StandardCharsets.UTF_8.toString()).toLowerCase();
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		Tokenizer<Word> ptbt = PTBTokenizer.factory().getTokenizer(new StringReader(text));
		Map<String, String> ret = new HashMap<>();
		List<Word> words = ptbt.tokenize();

		Integer wordCount = words.size();
		ret.put("wordCount", wordCount.toString());
		HashMultiset<Word> frequencies = HashMultiset.create();

		Double totalLength = 0.0;
		for (Word word : words) {
			totalLength += word.word().length();
			frequencies.add(word);
		}
		ret.put("averageWordLength", new Double(totalLength / wordCount).toString());

		Double commonCount = Math.floor(words.size() % 10);

		logger.info("threshold for commonality " + commonCount.intValue());

		Set<Word> entries = frequencies.elementSet();

		String freqs = Joiner.on(",").join(", ",
				entries.stream().filter(p -> frequencies.count(p) > commonCount).collect(Collectors.toSet()));
		freqs = freqs.replace("[", "").replace("]", "");
		ret.put("mostCommonWords", freqs);
		logger.info("returning " + new Gson().toJson(ret));
		return new ResponseEntity<Map<String, String>>(ret, HttpStatus.OK);
	}

	@RequestMapping(value = "/callback", method = RequestMethod.POST)
	public ResponseEntity<String> chargebeeWebhooks(@RequestParam Map<String, String> params,
			@RequestBody String rawBody) {
		JSONObject json = null;
		try {
			json = new JSONObject(rawBody);
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		String customerId = null;
		try {
			customerId = json.getJSONObject("content").getJSONObject("customer").getString("id");
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		String customerFirstName = null;
		try {
			customerFirstName = json.getJSONObject("content").getJSONObject("customer").getString("first_name");
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		String customerLastName = null;
		try {
			customerLastName = json.getJSONObject("content").getJSONObject("customer").getString("last_name");
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		User user = new User();
		user.setChargebeeId(customerId);
		JSONObject remoteJson;
		try {
			remoteJson = new JSONObject(
					(String) new URL("http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users/").openConnection()
							.getContent());
			JSONArray users = remoteJson.getJSONArray("users");
			for (int i = 0; i != users.length(); i++) {
				JSONObject aUser = users.getJSONObject(i);
				if (aUser.getString("first_name").equals(customerFirstName)
						&& aUser.getString("last_name").equals(customerLastName)) {
					user.setPhpId(Integer.parseInt(aUser.getString("id")));
					repository.save(user);
					return new ResponseEntity<String>("successful", HttpStatus.CREATED);
				}
			}
		} catch (JSONException | IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return new ResponseEntity<String>("failed", HttpStatus.CONFLICT);
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
