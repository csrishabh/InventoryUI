package com.cargo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cargo.document.Manifest;

@Repository
public interface ManifestRepo extends MongoRepository<Manifest, String> , ManifestRepoCustom{

}
