package com.luminos.woosh.dao;

import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
public interface UserDao extends GenericWooshDao<User> {

	/**
	 * 
	 * @param username
	 * @return
	 */
	User findByUsername(String username);

	/**
	 * 
	 * @param invitationalKey
	 * @return
	 */
	User findByInvitationalKey(String invitationalKey);
	
}
