package com.cargo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.cargo.document.Challan;

@Repository
public interface ChallanRepo extends MongoRepository<Challan, String>, ChallanRepoCustom{

	@Query("{'invoiceNo' : ?0, isCancelled : ?1}")
	public List<Challan> getAllChallanByInvoice(long invoiceNo, boolean isCancelled);
	
	@Query("{'refId' : ?0}")
	Challan getChallanByRefId(long refId);
}
