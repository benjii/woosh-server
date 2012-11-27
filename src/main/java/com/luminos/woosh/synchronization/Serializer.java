package com.luminos.woosh.synchronization;

/**
 * Implement for custom serialize / deserailize functionaly on @Synchronizable properties.
 * 
 * @author Ben
 */
public interface Serializer {

	/**
	 * Implement to serialize an object to a String.
	 * 
	 * @param value
	 * @return
	 */
	String serialize(Object value);
	
	/**
	 * Implement to de-serialize a String value to an object.
	 * 
	 * @param value
	 * @return
	 */
	Object deserialize(String value);
	
}
