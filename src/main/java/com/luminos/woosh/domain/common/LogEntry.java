package com.luminos.woosh.domain.common;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.luminos.woosh.enums.UserAction;

/**
 * 
 * @author Ben
 */
@Entity(name="log")
public class LogEntry {

	@Id
	@GeneratedValue(generator="default")
	@GenericGenerator(name="default", strategy="uuid", parameters={@Parameter(name="separator",value="-")})
	private String id = null;

	@Version
	private Integer version = null;

	// the user that took the logged action
	@OneToOne
	private User user = null;
	
	// the username (simply included because it's easier to query by)
	private String username = null;
	
	@Enumerated(value=EnumType.STRING)
	private UserAction action = null;
	
	// the sequence can be used to link multiple log entries for the same user action
	private String sequence = null;
	
	// the time at which the action was taken
	private Date date = Calendar.getInstance().getTime();

	
	public LogEntry() {
		
	}

	public LogEntry(User user, UserAction action) {
		this.user = user;
		this.username = user.getUsername();
		this.action = action;
	}
	
	
	// below are the set of factory methods for creating various log entry records
	
	public static LogEntry apnsTokenEntry(User user) {
		return new LogEntry(user, UserAction.APNS_TOKEN);
	}

	public static LogEntry pingEntry(User user) {
		return new LogEntry(user, UserAction.PING);
	}

	public static LogEntry loggedInEntry(User user) {
		return new LogEntry(user, UserAction.SIGN_UP);
	}

	// end of factory methods
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserAction getAction() {
		return action;
	}

	public void setAction(UserAction action) {
		this.action = action;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
