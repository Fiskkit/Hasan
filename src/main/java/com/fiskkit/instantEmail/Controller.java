package com.fiskkit.instantEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.chargebee.Environment;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.Status;
import com.fiskkit.instantEmail.models.User;

import org.jsoup.Jsoup;
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
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

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

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> newOrg(@RequestParam(name = "id") String organizationUniqueId,
			@RequestParam(name = "subscription") String subscriptionId) {
		User user = new User();
		user.setPhpId(Integer.parseInt(organizationUniqueId));
		user.setChargebeeId(subscriptionId);
		repository.save(user);
		return new ResponseEntity<String>(user.toString(), HttpStatus.CREATED);
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

	@RequestMapping(value = "/phrases", method = RequestMethod.GET)
	public ResponseEntity<List<String>> tokenizeIntoPhrases(@RequestParam(name = "loc") String location) {
		List<String> sentences = new ArrayList<>();
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
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

		text = Jsoup.parse(text).text();
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> phrases = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : phrases) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				sentences.add(token.get(TextAnnotation.class));
			}
		}

		return new ResponseEntity<List<String>>(sentences, HttpStatus.OK);
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
