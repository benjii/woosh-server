package com.luminos.woosh.synchronization;

/**
 * The synchronization policy for entities can the read-only, write-only, or read-write.
 * 
 * @author Ben
 */
public enum Policy {

	// entities with this policy must implement the ReadOnlySynchronizationEntity interface
	READ_ONLY,

	// entities with this policy must implement the WritableSynchronizationEntity interface
	WRITE_ONLY,
	
	// entities with this policy must implement the WritableSynchronizationEntity interface
	READ_WRITE;
	
}
