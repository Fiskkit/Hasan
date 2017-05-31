package com.fiskkit.instantEmail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by joshuaellinger on 3/25/15.
 * Imported from Email-Notiffy
 */
public class MySQLAccess {

	private static final Logger LOGGER = Logger.getLogger("");

	private static Map<String, String> pictureSettings = new HashMap<String, String>();

	private static Statement statement = null;
	// private PreparedStatement preparedStatement = null;
	private static ResultSet resultSet = null;
	private static Connection CONNECTION;
	
	
	
	
	private final String mysql_name;
	private final String mysql_user;
	private final String mysql_pass;
	private final String mysql_port;
	private final String mysql_db;
	private final String db_type;
	
	
	private MySQLAccess() {

		LOGGER.log(Level.INFO, "Loading MySQL JDBC Driver");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			LOGGER.log(Level.INFO, "Driver found");
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Cannot find MySQL JDBC Driver", e);
		}
		
		this.mysql_name = System.getProperty("MYSQL_NAME");
		this.mysql_user = System.getProperty("MYSQL_USER");
		this.mysql_pass = System.getProperty("MYSQL_PASSWORD_ENC","").trim();
		this.mysql_port = System.getProperty("MYSQL_PORT");
		this.mysql_db = System.getProperty("MYSQL_DB");
		this.db_type = System.getProperty("DB_TYPE");
		
	}
	
	
	
	public static Connection getConnection() throws RuntimeException
	{
		try {
			if (
					(CONNECTION == null) || 
					(CONNECTION.isClosed()) || 
					(! CONNECTION.isValid(1))
			){
				CONNECTION = new MySQLAccess().getConnectionInstance();
				LOGGER.info("MySQL Connection complete:" + CONNECTION);
			}
			
			return CONNECTION;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e.getClass().getName() + ":" + e.getMessage());
		}
	}
	
	// NB should onlyy be called by getConnection()
	private Connection getConnectionInstance() {

		String connectionString = "jdbc:mysql://" + mysql_name + ":" + mysql_port + "/" + mysql_db;
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(connectionString, mysql_user, mysql_pass);
			if (connection == null || connection.isClosed()) {
				LOGGER.log(Level.SEVERE, "Unable to connect.");
				return null;
			}
			LOGGER.log(Level.INFO, "Connected to " + db_type + " db.:" + connection);
			return connection;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot connect to database:" + connectionString, e);
			throw new RuntimeException("Cannot connect to database!");
		}

	}
	
	
	
	
	
	
	
	
	
	
	public static void touchPriorityList(User user) {

		Connection connection = null;
		try {
			connection = getConnection();
			
			statement = connection.createStatement();
			String sql = "" + "UPDATE email_priority " + "SET updated_at = NOW() " + "WHERE user_id = " + user.getId()
					+ ";";
			statement.executeUpdate(sql);
      connection.close();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "", e);
			throw new RuntimeException("Query Failed!");

		}

	}

	public static User getUser(int userId) {

		Connection connection = null;
		// FIXME remove in() clause
		try {
			connection = getConnection();
			
			statement = connection.createStatement();
			String sql = "" + "SELECT users.*, " + "(" + "SELECT COUNT(*) " + "FROM respects r "
					+ "WHERE r.author_id = users.id" + ") AS respect_count " + "FROM users " + " WHERE id = " + userId
					+ " " + " AND id in (19, 43, 46, 355, 382, 385, 386, 387, 388)" + ";";
			resultSet = statement.executeQuery(sql);

			connection.close();
			if (!resultSet.next()) {
				return null;
			}

			return new User(resultSet);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "", e);
			throw new RuntimeException("Query Failed!");

		} finally {

			try {
				if (resultSet != null && statement != null) {
					resultSet.close();
					statement.close();
				}
			} catch (SQLException ignore) {
			}

		}

	}
}
