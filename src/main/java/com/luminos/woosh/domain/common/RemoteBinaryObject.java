package com.luminos.woosh.domain.common;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;



/**
 * Represents a remotely stored binary object. For example, a photograph for a photograph form element. The default storage
 * mechanism is S3.
 * 
 * @author Ben
 */
@Entity
public class RemoteBinaryObject {

	@Id
	@GeneratedValue(generator="default")
	@GenericGenerator(name="default", strategy="uuid", parameters={@Parameter(name="separator",value="-")})
	private String id = null;

	@Version
	private Integer version = null;

	private Timestamp lastUpdated = null;
	
	private Boolean deleted = Boolean.FALSE;

	@OneToOne
	private User user = null;
			
	// the identifier used in the remote data store (i.e.: S3)
	private String remoteId = null;

	
	public RemoteBinaryObject() {
	
	}
	
	public RemoteBinaryObject(User user, String remoteId) {
		this.user = user;
		this.remoteId = remoteId;
	}


	/**
	 * Returns the (globally unique) S3 name for the object. 
	 * 
	 * @return
	 */
	public String getS3Name() {
		return this.getUser().getId() + "/" + this.getRemoteId();								
	}


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

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}
	
}
