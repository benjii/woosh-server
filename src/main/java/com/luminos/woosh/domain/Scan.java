package com.luminos.woosh.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;
import com.vividsolutions.jts.geom.Point;

/**
 * A scan is an 'up-woosh'. When a user performs a scan then offers within their vicinity are searched for. If
 * any are found then offer candidates are sent.
 * 
 * The scan entity is prioritized in the synchronization queue to ensure that it's processed before offer
 * candidate entities (this is because scans can produce offer candidates, which then need to be picked up
 * to be synchronized down to the client).
 * 
 * @author Ben
 */
@Entity
//@Synchronizable(alias="scans", order=1)
//@OnEntityCreate(processor=OfferScanProcessor.class)
public class Scan implements WritableSynchronizationEntity, UserScopedEntity {

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

	// the user that took the offer
	@OneToOne
	@SynchronizeIgnore
	private User owner = null;

	// the time at which the woosh occurred
	private Timestamp scannedAt = new Timestamp(Calendar.getInstance().getTimeInMillis());

	// the location at which the woosh occured
	@Type(type="org.hibernatespatial.GeometryUserType")
	@Column(nullable=false)
//	@SynchronizationSerializer(serializer=GeometrySerializer.class, reportedSchemaType="WKT String")
	private Point location = null;

	// the list of offers that were made because of this scan
	// many-to-many because one scan can have many offers, and one offer can be scanned for many times
	@ManyToMany
	private List<Offer> offers = null;

//	// the list of (cloned) cards that were offered for this scan
//	// many-to-many because one scan can have cards, and one card can be scanned for many times
//	@ManyToMany
//	private List<Card> cards = null;
	
	
	public Scan() {
	
	}

	public Scan(User owner, Point location) {
		this.owner = owner;
		this.location = location;
	}

	/**
	 * 
	 * @param offer
	 */
	public void addOffer(Offer offer) {
		if (offer == null) {
			throw new IllegalArgumentException("The offer argument must not be NULL.");
		}
		
		if (this.offers == null) {
			this.offers = new ArrayList<Offer>();
		}
		
		this.offers.add(offer);
	}

//	/**
//	 * 
//	 * @param card
//	 */
//	public void addCard(Card card) {
//		if (card == null) {
//			throw new IllegalArgumentException("The card argument must not be NULL.");
//		}
//		
//		if (this.cards == null) {
//			this.cards = new ArrayList<Card>();
//		}
//		
//		this.cards.add(card);
//	}

	
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

	public Timestamp getScannedAt() {
		return scannedAt;
	}

	public void setScannedAt(Timestamp scannedAt) {
		this.scannedAt = scannedAt;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

//	public List<Card> getCards() {
//		return cards;
//	}
//
//	public void setCards(List<Card> cards) {
//		this.cards = cards;
//	}

}
