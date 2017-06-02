package com.fiskkit.instantEmail;

import com.fiskkit.instantEmail.models.EmailPriority;
import org.springframework.data.repository.CrudRepository;

public interface EmailPriorityRepository extends CrudRepository<EmailPriority, Integer> { 
}
