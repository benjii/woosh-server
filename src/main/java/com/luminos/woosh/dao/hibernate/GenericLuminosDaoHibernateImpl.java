package com.luminos.woosh.dao.hibernate;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.GenericLuminosDao;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 *
 * @param <T>
 */
@Repository
public class GenericLuminosDaoHibernateImpl<T> implements GenericLuminosDao<T> {

	protected static final Junction RECORD_IS_NOT_DELETED = Restrictions.disjunction().add(Restrictions.eq("deleted", false))
																					  .add(Restrictions.isNull("deleted"));
	
	@Resource(name="sessionFactory")
	private SessionFactory sessionFactory = null;
	
	
	public final void save(T entity) {
		getSession().saveOrUpdate(entity);
		getSession().flush();
	}

	public final void save(T... entities) {
		for (T entity : entities) {
			getSession().saveOrUpdate(entity);
			getSession().flush();
		}
	}

	public final void refresh(T entity) {
		getSession().refresh(entity);
		getSession().flush();
	}

	public void evict(T entity) {
		getSession().evict(entity);
		getSession().flush();
	}

	public void clear() {
		getSession().clear();
	}

	public void flush() {
		getSession().flush();
	}

	@SuppressWarnings("unchecked")
	public T findById(String id) {
		return (T) getSession().createCriteria(this.getGenericClass())
							   .add(Restrictions.eq("id", id))
							   .uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public T findByClientId(String id) {
		return (T) getSession().createCriteria(this.getGenericClass())
		   					   .add(Restrictions.eq("clientId", id))
		   					   .uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return (List<T>) getSession().createCriteria(this.getGenericClass())
									 .add(RECORD_IS_NOT_DELETED)
									 .list();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(User user) {
		return (List<T>) getSession().createCriteria(this.getGenericClass())
									 .add(RECORD_IS_NOT_DELETED)
									 .add(Restrictions.eq("owner", user))
									 .list();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(User user, Order order) {
		return (List<T>) getSession().createCriteria(this.getGenericClass())
									 .add(RECORD_IS_NOT_DELETED)
									 .add(Restrictions.eq("owner", user))
									 .addOrder(order)
									 .list();
	}

	public Integer count() {
		return (Integer) getSession().createCriteria(this.getGenericClass())
		 							 .setProjection(Projections.count("id"))
									 .add(RECORD_IS_NOT_DELETED)
		 							 .uniqueResult();
	}

	public Integer countAll() {
		return (Integer) getSession().createCriteria(this.getGenericClass())
		 							 .setProjection(Projections.count("id"))
		 							 .uniqueResult();
	}

	public Boolean exists(String id) {
		return this.findById(id) != null;
	}

	public void disableFilter(String name) {
		getSession().enableFilter(name);
	}

	public void enableFilter(String name) {
		getSession().disableFilter(name);
	}


	@SuppressWarnings("unchecked")
	private Class<T> getGenericClass() {
		ParameterizedType parameterizedType = (ParameterizedType) super.getClass().getGenericSuperclass();
		Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
		return clazz;
	}

	
	// For backwards compatibility only - specifically just for the UserDao implementation, which is used by Spring Security (which we have not migrated
	// forward yet to full auto-wire status). All other DAO's have their session factory auto-wired via the @Resource declaration above.
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	// open a new session
	public Session openSession() {
		return sessionFactory.openSession();
	}

	// utility method to get the current session
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

}
