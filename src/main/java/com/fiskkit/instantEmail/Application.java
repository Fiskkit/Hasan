package com.fiskkit.instantEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = { "com.fiskkit.instantEmail.models" })
@EntityScan(basePackages = { "com.fiskkit.instantEmail.models" })
public class Application {
	private static Logger logger = LoggerFactory.getLogger("Application");

	private static String readUrl(String urlString) {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1) {
				buffer.append(chars, 0, read);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		logger.info("begin import from PHP");
		importFromPhp();
		logger.info("finished import from PHP");
	}

	public static int importFromPhp() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(
					"jdbc:postgresql://mydbinstance.cwblf8lajcuh.us-west-1.rds.amazonaws.com/Fiskkit_Instant_Email",
					"root", "SLvX92gBJqK7ykX4yJtQvV");
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		PreparedStatement prepped = null;
		try {
			prepped = conn.prepareStatement(
					"INSERT INTO users (id, added_at, php_id, balance) VALUES (nextval('hibernate_sequence'),now(), ?,?)");
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		String json = readUrl(
				"http://fiskkit-dev-2014-11.elasticbeanstalk.com/api/v1/users?limit=" + Integer.MAX_VALUE);
		JSONTokener tokener = new JSONTokener(json);
		JSONObject obj = null;
		try {
			obj = new JSONObject(tokener);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		JSONArray remoteUsers = null;
		try {
			remoteUsers = obj.getJSONArray("users");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int ret = 1;
		for (int userIdx = 0; userIdx != remoteUsers.length(); userIdx++) {
			JSONObject userObj = null;
			try {
				userObj = remoteUsers.getJSONObject(userIdx);
				prepped.setInt(1, userObj.getInt("id"));
				prepped.setBigDecimal(2, BigDecimal.ZERO);
				if (prepped.execute()) {
					ret++;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				System.exit(-1 * e.hashCode());
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1 * e.hashCode());
			}
		}

		try {
			conn.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
