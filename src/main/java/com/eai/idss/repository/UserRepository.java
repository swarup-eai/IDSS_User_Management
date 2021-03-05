package com.eai.idss.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.eai.idss.model.User;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUserName(String name);
    
    long deleteByUserName(String name);

    Page<User> findByIsActive(boolean isActive, Pageable page);



}
