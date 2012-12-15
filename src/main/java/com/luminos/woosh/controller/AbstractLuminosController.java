package com.luminos.woosh.controller;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Component
public abstract class AbstractLuminosController {
	
	@Autowired
	private UserDao userDao = null;
	
	
	// the web app version
//	@Value(value="${app.version}")
	private String version = "1.0-beta";
	
	
	@ModelAttribute("version")
	protected String getVersion() {
		return this.version;
	}
	
	@ModelAttribute("user")
	protected User getUser() {
		return ( this.isUserAuthenticated() ? (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
											: userDao.findByUsername("ben.deany") );	// VERY TEMPORARY			
	}
	
//	@ModelAttribute("roles")
//	protected List<String> getRoles() {
//		return ( this.isUserAuthenticated() ? this.getUser().get : null );
//	}

	@ModelAttribute("date")
	protected Date getDate() {
		return Calendar.getInstance().getTime();
	}

	
	private boolean isUserAuthenticated() {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return false;
		} else {
			return SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User;
		}
	}
	
}
