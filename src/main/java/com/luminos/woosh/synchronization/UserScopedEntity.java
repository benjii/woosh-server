package com.luminos.woosh.synchronization;

import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
public interface UserScopedEntity {

	/**
	 * 
	 * @return
	 */
	User getOwner();
	
	/**
	 * 
	 * @param clientId
	 */
	void setOwner(User owner);	
	
}
