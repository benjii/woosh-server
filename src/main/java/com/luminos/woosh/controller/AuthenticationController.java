package com.luminos.woosh.controller;

import org.apache.commons.lang3.StringUtils;
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
import com.luminos.woosh.exception.InvalidInvitationKeyException;
import com.luminos.woosh.exception.MaximumUsersReachedException;
import com.luminos.woosh.exception.UsernameAlreadyInUseException;
import com.luminos.woosh.services.WooshServices;

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
		
	@Autowired
	private WooshServices wooshServices = null;
		
	
	@RequestMapping(value="/m/signup", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public String signup(@RequestParam String username, @RequestParam String password, @RequestParam String invitationKey,
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
		
		// call the sign up service
		try {
			
			User newUser = wooshServices.signup(username, password, email, invitationKey);			
			LOGGER.info("User signed up successfully (username='" + username + "').");
			return "{ \"status\": \"OK\", \"invitationKey\": \"" + newUser.getInvitationalKey() + "\" }";			

		} catch (UsernameAlreadyInUseException e) {
			return "{ \"status\": \"USERNAME_UNAVAILABLE\" }";						
		} catch (InvalidInvitationKeyException e) {
			return "{ \"status\": \"INVALID_INVITATION_KEY\" }";			
		} catch (MaximumUsersReachedException e) {
			return "{ \"status\": \"MAXIMUM_USERS_REACHED\" }";			
		}
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
		User authenticatedUser = super.getUser();
		
		// call the service to record the authenticate of the login
		wooshServices.authenticate(authenticatedUser);
		
		return "{ \"status\": \"OK\", \"invitationKey\": \"" + authenticatedUser.getInvitationalKey() + "\" }";
	}
	
}
