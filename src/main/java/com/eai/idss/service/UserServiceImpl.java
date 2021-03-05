package com.eai.idss.service;

import com.eai.idss.model.User;
import com.eai.idss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<String> deleteByUserName(List<String> userNames) {
        try {
           List<String> userDeactivatedList = userNames.stream().map(value -> {
                User dbUserDetails = userRepository.findByUserName(value);
                if(null!=dbUserDetails) {
                    dbUserDetails.setActive(Boolean.FALSE);
                    userRepository.save(dbUserDetails);
                    return "User has been deactivated.";
                }
                return "User Not found";
			}).collect(Collectors.toList());
            return userDeactivatedList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserExitOrNot(String userName){
        User user = userRepository.findByUserName(userName);
        return user;
    }
}
