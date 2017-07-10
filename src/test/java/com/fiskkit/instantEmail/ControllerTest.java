package com.fiskkit.instantEmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class ControllerTest {
	@Autowired
	Controller controller;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void isCreatedProperly() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	public void userValidTest() {
		assertThat((restTemplate.getForObject("http://localhost:" + port + "/valid?subscription=1sjs9hvQ5tmUbX2I1Z",
				String.class) == "true"));

	}

	@Test
	public void expiredSubscription() {
		assertThat(restTemplate.getForObject("http://localhost:" + port + "/valid?subscription=cbdemo_dave-sub2",
				String.class) == "false");
	}

	@Test
	public void readabilityOfText() {
		String text = "the quick brown fox jumped over the lazy dog.";
		assertThat(restTemplate.postForObject("http://localhost:" + port + "/readability", text, Double.class) < 1.00);
	}

	@Test
	public void statistics() {
		String text = "the quick brown fox jumped over the lazy dog.";
		@SuppressWarnings("unchecked")
		Map<String, String> stats = restTemplate.postForObject("http://localhost:" + port + "/analyze", text,
				Map.class);
		Set<String> expectedKeys = new HashSet<>();
		expectedKeys.add("wordCount");
		expectedKeys.add("averageWordLength");
		expectedKeys.add("mostCommonWords");
		assertThat(stats.keySet().containsAll(expectedKeys));
	}
}
