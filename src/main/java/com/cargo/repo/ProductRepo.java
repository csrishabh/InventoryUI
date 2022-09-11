package com.cargo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.cargo.document.Product;

public interface ProductRepo extends MongoRepository<Product, String>, ProductRepoCustom{
	
	List<Product> findByNameIgnoreCase(String name);	
	
	@Query(value="{'name' : {$regex : ?0 , $options: 'i'}, enabled: ?1 }")
    List<Product> findByNameStartingWith(String regexp , boolean enabled);
	
	@Query(value="{'name' : {$regex : ?0 , $options: 'i'}}")
    List<Product> findByNameStartingWith(String regexp);
}
