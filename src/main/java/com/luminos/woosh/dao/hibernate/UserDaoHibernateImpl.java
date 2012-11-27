package com.luminos.woosh.dao.hibernate;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Repository
public class UserDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<User> implements UserDao {

	/**
	 * We override the standard findById(...) method for the User entity to ensure that we
	 * eagerly load the 'authorities' property
	 */
	@Override
	public User findById(String id) {
		return (User) getSession().createCriteria(User.class, "u")
		  						  .setFetchMode("authorities", FetchMode.JOIN)
//		  						  .createAlias("cards", "c", CriteriaSpecification.LEFT_JOIN)
//		  						  .createAlias("acceptances", "a", CriteriaSpecification.LEFT_JOIN)
//		  						  .createAlias("scans", "s", CriteriaSpecification.LEFT_JOIN)
		  						  .add(Restrictions.eq("id", id))
		  						  .uniqueResult();
	}

	public User findByUsername(String username) {
		return (User) getSession().createCriteria(User.class, "u")
		  						  .setFetchMode("authorities", FetchMode.JOIN)
//		  						  .createAlias("cards", "c", CriteriaSpecification.LEFT_JOIN)
//		  						  .createAlias("acceptances", "a", CriteriaSpecification.LEFT_JOIN)
//		  						  .createAlias("scans", "s", CriteriaSpecification.LEFT_JOIN)
		  						  .add(Restrictions.eq("u.username", username).ignoreCase())
		  						  .uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAll() {
		return (List<User>) getSession().createCriteria(User.class)
		 								.list();
	}

	@Override
	public Integer count() {
		return (Integer) getSession().createCriteria(User.class)
		 							 .setProjection(Projections.count("id"))
		 							 .uniqueResult();
	}

	public User findByInvitationalKey(String invitationalKey) {
		return (User) getSession().createCriteria(User.class, "u")
		  						  .setFetchMode("authorities", FetchMode.JOIN)
		  						  .add(Restrictions.eq("u.invitationalKey", invitationalKey).ignoreCase())
		  						  .uniqueResult();
	}	
	
}
