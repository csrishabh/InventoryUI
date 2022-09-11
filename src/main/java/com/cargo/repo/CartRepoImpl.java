package com.cargo.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.cargo.document.Cart;

public class CartRepoImpl implements CartRepoCustom {

	@Autowired
	
	MongoTemplate mongoTemplate;
	
	@Override
	public Cart getCartByUserId(String userId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId));
		return mongoTemplate.findOne(query, Cart.class);
	}

}
