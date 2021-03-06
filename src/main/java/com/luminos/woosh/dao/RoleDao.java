package com.luminos.woosh.dao;

import com.luminos.woosh.domain.common.Role;

/**
 * 
 * @author Ben
 */
public interface RoleDao extends GenericWooshDao<Role> {

	/**
	 * 
	 * @param authority
	 * @return
	 */
	Role findByAuthority(String authority);
	
}
