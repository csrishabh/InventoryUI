package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cargo.document.Transction;

@Repository
public interface TransctionRepo extends MongoRepository<Transction, String> ,TransctionRepoCustom {
}
