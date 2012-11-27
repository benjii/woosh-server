package com.luminos.woosh.base;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;

/**
 * This test just creates some entities and persists them to the database to ensure that basic DAO operations are working. This suite does not
 * test any particular business functionality, as would typically be the case with integration tests.
 * 
 * @author Ben
 */
public class BasicDaoOperationsIntTest extends AbstractLuminosIntegrationTest {
	
	@Autowired
	private UserDao userDao = null;

	
	@Test
	public void persistUserEntity() throws Exception {
		User user = new User("persist-user", "password", "test@nowhere.com");
		
		userDao.save(user);
		Assert.assertEquals(3, super.countRowsInTable("users"));		// 2 because we already have the authenticated user in the database
	}

	@Test
	public void generateSomeIds() {
		// this is not a test - it just a simple way of generating Hibernate IDs for external scripts.
		
		for (int count = 0; count < 10; count++) {
			User user = new User(UUID.randomUUID().toString(), "password", "test@nowhere.com");
			
			userDao.save(user);
			System.out.println(user.getId());
		}
		
	}
	
}
