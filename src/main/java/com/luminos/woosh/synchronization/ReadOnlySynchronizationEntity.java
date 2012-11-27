package com.luminos.woosh.synchronization;

import java.sql.Timestamp;

/**
 * All classes annotated as Synchronizable with a policy of read-only <i>must</i> implement this interface.
 * 
 * The 'lastUpdated' property refers to the last time that the instance of the synchronizable entity was
 * updated <i>on the server</i> (instances of a read-only entity will never be updated by the client, and if
 * they are those updates are ignored).
 * 
 * @author Ben
 */
public interface ReadOnlySynchronizationEntity {
	
	/**
	 * 
	 * @return
	 */
	String getClientId();
	
	/**
	 * 
	 * @param clientId
	 */
	void setClientId(String clientId);
	
	/**
	 * 
	 * @return
	 */
	Boolean getDeleted();

	/**
	 * 
	 * @param deleted
	 */
	void setDeleted(Boolean deleted);

	/**
	 * 
	 * @return
	 */
	Timestamp getLastUpdated();

	/**
	 * 
	 * @param lastUpdated
	 */
	void setLastUpdated(Timestamp lastUpdated);
	
}
