package com.eai.idss.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eai.idss.service.UserService;
import com.eai.idss.vo.UserResponseVO;
import com.mongodb.client.MongoClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.eai.idss.model.User;
import com.eai.idss.repository.UserRepository;


@RestController
@CrossOrigin(origins ={"http://localhost:4200", "http://10.10.10.32:8080"})
public class IDSSUserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	MongoTemplate mongoTemplate;

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method = RequestMethod.GET, value = "/admin/user-list", produces = "application/json")
	public ResponseEntity<UserResponseVO> getMasterData(Pageable pageable) throws IOException {
		UserResponseVO userDetails = new UserResponseVO();
	    try {
			Query query = new Query().with(pageable);
			Query queryCnt = new Query();
			queryCnt.addCriteria(Criteria.where("isActive").is(true));
			userDetails.setTotalRecords(mongoTemplate.count(queryCnt, User.class));

			query.addCriteria(Criteria.where("isActive").is(true));
			List<User> userList = mongoTemplate.find(query, User.class);
			Page<User> imPage = PageableExecutionUtils.getPage(userList, pageable,
					() -> mongoTemplate.count(query, User.class));
			userDetails.setUserList(imPage.toList());

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity("Exception in userDetails", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return new ResponseEntity<UserResponseVO>(userDetails,HttpStatus.OK);
	}
    
    @RequestMapping(method = RequestMethod.PUT, value = "/admin/user", produces = "application/json")
	public ResponseEntity<String> updateUserAttributes(@RequestBody(required=true) User user) throws IOException {
	    ResponseEntity<String> responseEntity = null;
	    try {
	    	User dbUserDetails = userRepository.findByUserName(user.getUserName());
	    	if(null==dbUserDetails)
	    		return new ResponseEntity<String>("User Not Found.", HttpStatus.INTERNAL_SERVER_ERROR);
	    	myCopyProperties(user,dbUserDetails);
 			userRepository.save(dbUserDetails);
 			responseEntity = new ResponseEntity<String>("User attributes updated successfully.", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return responseEntity;
	}
    
    @RequestMapping(method = RequestMethod.PUT, value = "/secure/user/password", produces = "application/json")
	public ResponseEntity<String> resetPassword(@RequestBody(required=true) User user) throws IOException {
	    ResponseEntity<String> responseEntity = null;
	    try {
	    	User dbUserDetails = userRepository.findByUserName(user.getUserName());
	    	if(null==dbUserDetails)
	    		return new ResponseEntity<String>("User Not Found.", HttpStatus.INTERNAL_SERVER_ERROR);
	    	
	    	dbUserDetails.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			userRepository.save(dbUserDetails);
	    		
	    	responseEntity = new ResponseEntity<String>("Password updated successfully.", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return responseEntity;
	}
    
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    // then use Spring BeanUtils to copy and ignore null using our function
    public static void myCopyProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/admin/new-user", produces = "application/json")
	public ResponseEntity<String> onboardUser(@RequestBody(required=true) User user) throws IOException {
	    ResponseEntity<String> responseEntity = null;
	    try {
	    	User userDetails = userRepository.findByUserName(user.getUserName());
	    	if(null!=userDetails) {
	    		return  new ResponseEntity<String>("User Already Exist!", HttpStatus.INTERNAL_SERVER_ERROR);
	    	}
	    	user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    	userRepository.save(user);
	    	
	    	responseEntity = new ResponseEntity<String>("Successfully created user", HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return responseEntity;
	}
    
    @RequestMapping(method = RequestMethod.PUT, value = "/user/complete-registration", produces = "application/json")
	public ResponseEntity<String> createUser(@RequestBody(required=true) User user) throws IOException {
	    ResponseEntity<String> responseEntity = null;
	    try {
	    	User userDetails = userRepository.findByUserName(user.getUserName());
	    	if(null==userDetails) {
	    		return new ResponseEntity<String>("User Not Exist!", HttpStatus.NOT_FOUND);
	    	}
	    	user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    	userRepository.save(user);
	    	
	    	responseEntity = new ResponseEntity<String>("Successfully completed registration.", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			responseEntity = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return responseEntity;
	}
    
    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/user", produces = "application/json")
	public List<String> deleteUser(@RequestParam(required = true) List<String>  userNames) throws IOException {
	    List<String> responseEntity = new ArrayList<>();
	    try {
			List<String> userDeleted = userService.deleteByUserName(userNames);
			responseEntity =userDeleted;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return responseEntity;
	}
    
	@RequestMapping(method = RequestMethod.GET, value = "/secure/user/{userName}", produces = "application/json")
	public ResponseEntity<User> findUserByName(@PathVariable String userName) {
		
		User userDetails = null;
	    try {
	    	userDetails = userRepository.findByUserName(userName);
	    	if(null!=userDetails)
	    		return new ResponseEntity<User>(userDetails,HttpStatus.OK);
	    	else
	    		return new ResponseEntity<User>(new User(),HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new User(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	}
    
}
