package com.luminos.woosh.security;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;

/**
 * Looks up a user by username from the default datastore.
 * 
 * @author Ben
 */
@Service
public class WooshUserDetailsService implements UserDetailsService, BeanFactoryAware {

	private static Logger LOGGER = Logger.getLogger(WooshUserDetailsService.class);
	
	private UserDao userDao = null;

	
//	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		LOGGER.info("Looking up user '" + username + "'.");
		
		User user = userDao.findByUsername(username);
				
		if (user == null) {
			LOGGER.info("User '" + username + "' was not found. No authorization.");
			throw new UsernameNotFoundException("The user '" + username + "' is not registered.");
		} else if ( !user.isEnabled() ) {
			LOGGER.info("User '" + username + "' was found but the account has expired. No authorization.");
			throw new UsernameNotFoundException("The user '" + user.getUsername() + "' account has expired.");
		}

		LOGGER.info("User '" + username + "' was found. Providing principal to the security system.");

		return user;
	}


	// TODO: forward migrate the user details service so that we don't need to do this step (ie: refactor so that the 'userDao' property
	// can be auto-injected
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		// the scheduler is injected manually because it is an 'old style' non-annotated bean
		this.userDao = (UserDao) beanFactory.getBean("userDao");
	}
	
}
