package com.luminos.woosh.dao.hibernate;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.ConfigurationDao;
import com.luminos.woosh.domain.common.Configuration;

/**
 * 
 * @author Ben
 */
@Repository
public class ConfigurationDaoHibernateImpl extends GenericWooshDaoHibernateImpl<Configuration> implements ConfigurationDao {

	@Override
	public Configuration findByKey(String key) {
		return (Configuration) getSession().createCriteria(Configuration.class, "c")
										   .add(Restrictions.eq("key", key))
										   .uniqueResult();		
	}

}
