package com.fiskkit.instantEmail;

import com.fiskkit.instantEmail.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Integer> {
}
