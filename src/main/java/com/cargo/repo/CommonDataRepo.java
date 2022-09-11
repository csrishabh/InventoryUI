package com.cargo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.cargo.document.CommonData;

@Repository
public interface CommonDataRepo extends MongoRepository<CommonData, String> {
	
	
	@Query(value="{'name' : {$regex : ?0 , $options: 'i'}, typeId: ?1 ,isDisabled: ?2}" , fields="{ name: 1, value:1 }")
    List<CommonData> findByNameStartingWithAndType(String regexp, int typeId , boolean isDisabled);
}
