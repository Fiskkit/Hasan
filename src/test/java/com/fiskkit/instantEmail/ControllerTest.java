package com.fiskkit.instantEmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.fiskkit.instantEmail.models.User;

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
	public void importUsers() {
		assertThat(restTemplate.postForObject("http://localhost:" + port + "/user", null, Boolean.class))
				.isEqualTo(Boolean.TRUE);
	}

	@Test
	public void updateBalanceTest() {
		assertThat(restTemplate
				.postForObject("http://localhost:" + port + "/balance?user=19&amount=1", null, User.class).getBalance())
						.isGreaterThanOrEqualTo(BigDecimal.ONE);
	}
}
