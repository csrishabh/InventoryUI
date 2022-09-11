package com.cargo.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.cargo.document.Case;

public interface CaseRepo extends MongoRepository<Case,String> ,CaseRepoCustom {

	public List<Case> getCaseByOpdNo(String opdNo);
	
	@Query("{'opdNo' : ?0, version : ?1 , bookingDate : ?2 }")
	public Case getCaseByOpdAndVersionNo(String opdNo, int versionNo, Date bookingDate);
	
	@Query("{'opdNo' : ?0, bookingDate : ?1 }")
	public List<Case> getCaseHistroy(String opdNo , Date bookingDate, Sort sort);
}
