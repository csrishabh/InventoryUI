package com.cargo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.cargo.document.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> , UserRepoCustom {

    User findByUsernameIgnoreCase(String email);
    
    @Query(value="{'fullname' : {$regex : ?0 , $options: 'i'}, roles: { $elemMatch: { $in: [?1] } }, enabled: ?2}" , fields="{ fullname: 1, username:1 , mobileNo:1, address:1, gstNo:1 }")
    List<User> findByNameStartingWithAndType(String regexp, String role, boolean enabled);
    
    @Query(value="{'fullname' : {$regex : ?0 , $options: 'i'}}" , fields="{ fullname: 1 }")
    List<User> findByNameStartingWith(String regexp);

}
