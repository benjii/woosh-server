package com.luminos.woosh.synchronization.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.luminos.woosh.base.AbstractLuminosIntegrationTest;
import com.luminos.woosh.synchronization.service.SynchronizationService;

/**
 * 
 * @author Ben
 */
public class SynchronizationServiceIntTest extends AbstractLuminosIntegrationTest {
	
	@Autowired
	private SynchronizationService synchronizationService = null;
	
	
	@Test
	public void canDiscoverAnnotatedClasses() {
		assertEquals(5, synchronizationService.listSynchronizableClasses().size());
	}

}
