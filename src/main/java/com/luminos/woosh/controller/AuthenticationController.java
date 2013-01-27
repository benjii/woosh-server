package com.luminos.woosh.controller;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
		if (StringUtils.isBlank(username) || StringUtils.length(username) <= MINIMUM_USERNAME_LENGTH) {			
			return "{ \"status\": \"INVALID_USERNAME\" }";
		}

		if (StringUtils.isBlank(password) || StringUtils.length(password) <= MINIMUM_PASSWORD_LENGTH) {			
			return "{ \"status\": \"INVALID_PASSWORD\" }";
		}

		try {
			
			// hash the password using MD5
			byte[] passwordBytes = password.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hashedPassword = md.digest(passwordBytes);

			// create the new user
			User newUser = new User(username, new String(hashedPassword), email);
			userDao.save(newUser);
			
		} catch (UnsupportedEncodingException e) {
			LOGGER.info("User failed sign-up - could not decode password (username='" + username + "').");
			return "{ \"status\": \"FAILED\" }";
		} catch (NoSuchAlgorithmException e) {
			LOGGER.info("User failed sign-up - could not create MD5 instance (username='" + username + "').");
			return "{ \"status\": \"FAILED\" }";
		}

		LOGGER.info("User signed up successfully (username='" + username + "').");

		return "{ \"status\": \"OK\" }";
	}
	
}
