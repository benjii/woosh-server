package com.luminos.woosh.dao;

import com.luminos.woosh.domain.common.Configuration;

/**
 * 
 * @author Ben
 */
public interface ConfigurationDao extends GenericWooshDao<Configuration> {

	/**
	 * 
	 * @param key
	 * @return
	 */
	Configuration findByKey(String key);
	
}
