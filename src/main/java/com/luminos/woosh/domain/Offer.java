package com.luminos.woosh.domain;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.domain.serializer.GeometrySerializer;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.SynchronizationSerializer;
import com.luminos.woosh.synchronization.SynchronizeChildCollection;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author Ben
 */
@Entity
@Synchronizable(alias="offers")
public class Offer implements WritableSynchronizationEntity, UserScopedEntity {
	
	private static final Integer DEFAULT_OFFER_DURATION = 10000;		// milliseconds

	
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

	// the user that made this offer
	@OneToOne
	@SynchronizeIgnore
	private User owner = null;
	
	// the (original) card being offered (the cloned card is on the acceptance entity)
	@OneToOne
	@Cascade(value=CascadeType.ALL)
	@SynchronizeChildCollection(method="addOffer")
	private Card card = null;
	
	// the maximum number of 'takes' for this offer (takes are only allowed for the duration of the offer)
	private Integer maximumAccepts = Card.UNLIMITED_ACCEPTS;
		
	// a card can have a maximum hop count (the maximum number of times that this offer can be forwarded)
	// this value represents the number of remaining hops in the chain
	private Integer remainingHops = 1;
	
	// the time at which the offer started
	private Timestamp offerStart = new Timestamp(Calendar.getInstance().getTimeInMillis());
	
	// the time at which the offer ends (by default this is the offer start time + the default duration)
	private Timestamp offerEnd = null;
	
	// an offer has a region within which it is application
	// the default is a circle around the user at the devices maximum location resolution (on iOS devices
	// this is approximately 2-3 metres)
	@Type(type="org.hibernatespatial.GeometryUserType")
	@Column(nullable=false)
	@SynchronizationSerializer(serializer=GeometrySerializer.class, reportedSchemaType="WKT String")
	private Geometry offerRegion = null;
	
	// true if this offer is auto-accepted by the scanner
	private Boolean autoAccept = Boolean.TRUE;
	
	
	public Offer() {
		
	}

	public Offer(User owner, Card card, Geometry offerRegion) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, DEFAULT_OFFER_DURATION);
		
		this.owner = owner;
		this.card = card;
		this.offerEnd = new Timestamp(c.getTimeInMillis());
		this.offerRegion = offerRegion;
	}

	public Offer(User owner, Card card, Geometry offerRegion, Boolean autoAccept) {
		this(owner, card, offerRegion);
		this.autoAccept = autoAccept;
	}

	public Offer(User owner, Card card, Integer offerDuration, Geometry offerRegion) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, offerDuration);

		this.owner = owner;
		this.card = card;
		this.offerEnd = new Timestamp(c.getTimeInMillis());
		this.offerRegion = offerRegion;
	}

	public Offer(User owner, Card card, Integer offerDuration, Geometry offerRegion, Boolean autoAccept) {
		this(owner, card, offerDuration, offerRegion);
		this.autoAccept = autoAccept;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Offer other = (Offer) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
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

	public Integer getMaximumAccepts() {
		return maximumAccepts;
	}

	public void setMaximumAccepts(Integer maximumAccepts) {
		this.maximumAccepts = maximumAccepts;
	}

	public Integer getRemainingHops() {
		return remainingHops;
	}

	public void setRemainingHops(Integer remainingHops) {
		this.remainingHops = remainingHops;
	}

	public Timestamp getOfferStart() {
		return offerStart;
	}

	public void setOfferStart(Timestamp offerStart) {
		this.offerStart = offerStart;
	}

	public Timestamp getOfferEnd() {
		return offerEnd;
	}

	public void setOfferEnd(Timestamp offerEnd) {
		this.offerEnd = offerEnd;
	}

	public Boolean getAutoAccept() {
		return autoAccept;
	}

	public void setAutoAccept(Boolean autoAccept) {
		this.autoAccept = autoAccept;
	}

	public Geometry getOfferRegion() {
		return offerRegion;
	}

	public void setOfferRegion(Geometry offerRegion) {
		this.offerRegion = offerRegion;
	}
	
}
