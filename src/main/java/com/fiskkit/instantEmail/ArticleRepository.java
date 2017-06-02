package com.fiskkit.instantEmail;

import com.fiskkit.instantEmail.models.Article;
import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article, String> { 
}
