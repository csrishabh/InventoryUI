package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cargo.document.Cart;

public interface CartRepo extends MongoRepository<Cart, String> , CartRepoCustom{
	

}
