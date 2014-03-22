package com.luminos.woosh.security;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.luminos.woosh.base.AbstractLuminosIntegrationTest;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.security.WooshUserDetailsService;

/**
 * 
 * @author Ben
 */
public class WooshUserDetailsServiceIntTest extends AbstractLuminosIntegrationTest {

	@Autowired
	private WooshUserDetailsService wooshUserDetailsService = null;
	
	@Autowired
	private UserDao userDao = null;
	
	
	@Test
	public void registeredUsersCanLogIn() throws Exception {
		User user = new User("known-user", "password", "test@test.com");
		userDao.save(user);
		
		UserDetails principal = wooshUserDetailsService.loadUserByUsername("known-user");
		Assert.assertNotNull(principal);
	}

	@Test
	public void unregisteredUsersAreDeniedAccess() throws Exception {
		try {
			wooshUserDetailsService.loadUserByUsername("unknown-user");
			Assert.fail("Unregistered user was able to log in.");
		} catch (UsernameNotFoundException ex) {
			// success
		}
	}

}
