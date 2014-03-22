package com.luminos.woosh.testutils;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * 
 * @author Ben
 */
public class WooshTestSchema {

	static {
		Configuration config = new AnnotationConfiguration().configure("test-hibernate.cfg.xml");
        HibernateUtil.setSessionFactory(config.buildSessionFactory());
	}
		
}
