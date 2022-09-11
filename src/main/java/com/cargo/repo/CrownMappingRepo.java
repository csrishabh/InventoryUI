package com.cargo.repo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.cargo.document.CROWNTYPE;
import com.cargo.document.CrownMapping;

@Repository
public interface CrownMappingRepo extends MongoRepository<CrownMapping, String>, CrownMappingRepoCustom {

	@Query("{'crown' : ?0, 'vendor.$id': ?1, 'version' : ?2 }")
	public CrownMapping getCrownMappingByTypeAndVendor(CROWNTYPE type, ObjectId	 vendorId, int version);
}
