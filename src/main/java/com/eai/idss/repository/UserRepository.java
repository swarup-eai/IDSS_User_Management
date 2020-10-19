package com.eai.idss.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.eai.idss.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUserName(String name);
    
    long deleteByUserName(String name);

}
