package com.luminos.woosh.synchronization.service;

import com.luminos.woosh.domain.common.RemoteBinaryObject;


/**
 * 
 * @author Ben
 */
public interface CloudServiceProxy {

	/**
	 * 
	 * @param metadata
	 * @param data
	 */
	void upload(RemoteBinaryObject metadata, byte[] data);

	/**
	 * 
	 * @param object
	 * @return
	 */
	byte[] get(RemoteBinaryObject metadata);
	
	/**
	 * 
	 * @param metadata
	 */
	void delete(RemoteBinaryObject metadata);

	/**
	 * 
	 * @param metadata
	 * @return
	 */
	String createSignedUrl(RemoteBinaryObject metadata);

	/**
	 * 
	 * @param metadata
	 * @param minutes
	 * @return
	 */
	String createSignedUrl(RemoteBinaryObject metadata, Integer minutes);

}
