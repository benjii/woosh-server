package com.luminos.woosh.domain;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;

/**
 * An acceptance is an accepted offer from the offer candidates that have been presented to the user. We record
 * the offer that was accepted, and time it was accepted, etc. We also track redemption counts here.
 * 
 * In addition, of the offer is multi-hop then new offers can be cloned from this accepted offer.
 * 
 * @author Ben
 */
@Entity
@Synchronizable(alias="acceptances")
public class Acceptance implements WritableSynchronizationEntity, UserScopedEntity {

	@Id
	@GeneratedValue(generator="default")
	@GenericGenerator(name="default", strategy="uuid", parameters={@Parameter(name="separator",value="-")})
	@SynchronizeIgnore
	private String id = null;

	@Version
	@SynchronizeIgnore
	private Integer version = null;

	private String clientId = UUID.randomUUID().toString();
	
	private Integer clientVersion = -1;

	private Timestamp lastUpdated = null;
	
	private Boolean deleted = Boolean.FALSE;

	// the user that the offer was made to
	@OneToOne
	@SynchronizeIgnore
	private User owner = null;

	// the (cloned from the original) card that may be accepted
	@OneToOne
	private Card card = null;

	// the offer that may be accepted
	@OneToOne
	private Offer offer = null;
	
	// true if this offer is accepted
	private Boolean accepted = Boolean.FALSE;

	// the time that the offer was was accepted (if it has been)
	private Timestamp acceptedAt = null;
	
	
	public Acceptance() {
		
	}
	
	public Acceptance(User owner, Card card, Offer offer) {
		this.owner = owner;
		this.card = card;
		this.offer = offer;
	}

	public Acceptance(User owner, Card card, Offer offer, Boolean accepted,	Timestamp acceptedAt) {
		this.owner = owner;
		this.card = card;
		this.offer = offer;
		this.accepted = accepted;
		this.acceptedAt = acceptedAt;
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Integer getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(Integer clientVersion) {
		this.clientVersion = clientVersion;
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

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	public Boolean getAccepted() {
		return accepted;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}

	public Timestamp getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(Timestamp acceptedAt) {
		this.acceptedAt = acceptedAt;
	}
	
}
