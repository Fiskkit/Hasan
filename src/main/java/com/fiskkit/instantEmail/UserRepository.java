package com.fiskkit.instantEmail;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.fiskkit.instantEmail.models.User;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByPhpId(@Param("userId") Integer mysqlUser);	
}
