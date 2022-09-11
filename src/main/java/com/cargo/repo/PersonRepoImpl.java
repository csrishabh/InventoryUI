package com.cargo.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.cargo.document.Person;

public class PersonRepoImpl implements PersonRepoCustom {
	
	 @Autowired 
	 MongoTemplate  mongoTemplate;
	 
	 public List<Person> findByNameStartingWith(String regexp,String type){
		 
		 Criteria regex = Criteria.where("name").regex(regexp, "i").and("type").is(type);
		 return mongoTemplate.find(new Query().addCriteria(regex), Person.class);
	 }
}
