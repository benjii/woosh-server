package com.luminos.woosh.synchronization;

/**
 * All classes annotated as Synchronizable with a writable policy (write-only or read-write) <i>must</i> implement this interface.
 * 
 * The client version property is set by the client and is checked by the server to ensure that only the most recent entity
 * is synchronized with the server-side database.
 * 
 * @author Ben
 */
public interface WritableSynchronizationEntity extends ReadOnlySynchronizationEntity {

	/**
	 * 
	 * @return
	 */
	Integer getClientVersion();	
	
	/**
	 * 
	 * @param clientVersion
	 */
	void setClientVersion(Integer clientVersion);

}
