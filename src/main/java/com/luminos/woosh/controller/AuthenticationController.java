package com.luminos.woosh.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.security.Md5PasswordEncoder;

/**
 * This controller is responsible for user management.
 * 
 * @author Ben
 */
@Controller
public class AuthenticationController extends AbstractLuminosController {

	private static final Logger LOGGER = Logger.getLogger(AuthenticationController.class);
	
	private static final Integer MINIMUM_USERNAME_LENGTH = 4;

	private static final Integer MINIMUM_PASSWORD_LENGTH = 6;

	
	@Autowired
	private UserDao userDao = null;
	
	
	@RequestMapping(value="/m/signup", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public String signup(@RequestParam String username, @RequestParam String password,
						 @RequestParam(required=false) String email) {

		LOGGER.info("User requested sign-up (username='" + username + "').");

		// perform some up-front checks
		
		// check that the username was specified and is of sufficient length
		if (StringUtils.isBlank(username) || StringUtils.length(username) <= MINIMUM_USERNAME_LENGTH) {			
			return "{ \"status\": \"INVALID_USERNAME\" }";
		}

		// check that the password was specified and is of sufficient length
		if (StringUtils.isBlank(password) || StringUtils.length(password) <= MINIMUM_PASSWORD_LENGTH) {			
			return "{ \"status\": \"INVALID_PASSWORD\" }";
		}

		// check that the username is not already taken
		User existingUser = userDao.findByUsername(username);
		if (existingUser != null) {
			return "{ \"status\": \"USERNAME_UNAVAILABLE\" }";			
		}
		
		// if everything checks out then continue
		
		// create the new user
		User newUser = new User(username, Md5PasswordEncoder.hashPassword(password), email);
		userDao.save(newUser);

		LOGGER.info("User signed up successfully (username='" + username + "').");

		return "{ \"status\": \"OK\" }";
	}


	/**
	 * Clients can call this method to test user supplied authentication credentials.
	 * 
	 * @return
	 */
	@RequestMapping(value="/m/authenticate", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public String authenticate() {
		return "{ \"status\": \"OK\" }";
	}
	
}
