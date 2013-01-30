package com.luminos.woosh.dao.hibernate;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.RoleDao;
import com.luminos.woosh.domain.common.Role;

/**
 * 
 * @author Ben
 */
@Repository
public class RoleDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Role> implements RoleDao {
	
	public Role findByAuthority(String authority) {
		return (Role) getSession().createCriteria(Role.class)
								  .add(Restrictions.eq("authority", authority))
								  .uniqueResult();
	}

}
