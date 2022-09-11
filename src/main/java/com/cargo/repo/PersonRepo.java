package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cargo.document.Person;

@Repository
public interface PersonRepo extends MongoRepository<Person, String>, PersonRepoCustom {

	
}
