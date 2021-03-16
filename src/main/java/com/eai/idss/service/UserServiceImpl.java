package com.eai.idss.service;

import com.eai.idss.model.User;
import com.eai.idss.repository.UserRepository;
import com.eai.idss.util.SendEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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

    @Override
    public String updateForgotPassword(String userId) {
        try {
         User user = userRepository.findByUserName(userId);
         if(null !=user){
             int pwd =  (int)(Math.floor(100000 + Math.random() * 900000));
             String password = "Mpcb@"+pwd;
             user.setPassword(bCryptPasswordEncoder.encode(password));
             userRepository.save(user);
             SendEmail.sendmail(userId, password);
             return password;
         }
         return "false";
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
