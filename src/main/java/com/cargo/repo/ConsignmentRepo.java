package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cargo.document.Consignment;

public interface ConsignmentRepo extends MongoRepository<Consignment, String> , ConsignmentRepoCustom {

	
}
