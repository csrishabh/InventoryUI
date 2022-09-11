package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cargo.document.Order;

public interface OrderRepo extends MongoRepository<Order, String> {

}
