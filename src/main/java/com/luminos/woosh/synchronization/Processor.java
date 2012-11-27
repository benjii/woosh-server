package com.luminos.woosh.synchronization;

import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
public interface Processor<T, R> {

	/**
	 * 
	 * @param user The user for which the processing is taking place.
	 * @param entity The object being processed.
	 * @param repository The general-purpose repository for access to the primary database.
	 * @return Any object of type 'R' - usually some logical result of the processing.
	 */
	R process(User user, T entity, SynchronizableDao repository);
	
}
