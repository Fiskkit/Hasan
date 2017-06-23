package com.fiskkit.instantEmail;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V2__moveDBStateToV2 implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    // programmatically change the database state using JdbCTemplate
  }
}
