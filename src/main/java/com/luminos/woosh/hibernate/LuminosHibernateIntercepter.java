package com.luminos.woosh.hibernate;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

/**
 * 
 * @author Ben
 */
public class LuminosHibernateIntercepter extends EmptyInterceptor {

	private static final long serialVersionUID = -340808067313423939L;

	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		setValue(currentState, propertyNames, "lastUpdated", new Timestamp(new Date().getTime()));
		return true;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		setValue(state, propertyNames, "lastUpdated", new Timestamp(new Date().getTime()));
		return true;
	}
	
	private void setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value) {
		int propertyIndex = -1;
		
		for (int count = 0; count < propertyNames.length; count++) {
			if (propertyNames[count].equals(propertyToSet)) {
				propertyIndex = count;
				break;
			}
		}
		
		if (propertyIndex != -1) {
			currentState[propertyIndex] = value;
		}
	}

}
