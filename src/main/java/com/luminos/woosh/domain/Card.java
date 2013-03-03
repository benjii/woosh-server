package com.luminos.woosh.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.domain.processor.NewCardProcessor;
import com.luminos.woosh.enums.ShareMethod;
import com.luminos.woosh.synchronization.OnEntityCreate;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;

/**
 * In Woosh users will trades 'cards' and this entity represents those cards. A card can contain arbitrary data (text,
 * photos, audio, etc). However, client-side apps will typically mould their user interfaces so as to produce certain
 * types of cards.
 * 
 * Cards can be 'wooshed' - that is, offered, taken, or traded.
 * 
 * One card can be offered as many times as the user wishes.
 * 
 * TODO Add redemptions - cards can be redeemed (e.g.: using a coupon in a store). Redemptions may have conditions.
 * TODO Add rewards - cards can store rewards (e.g.: getting a set of coffee stamps - to redeem for a product or service).
 * 
 * @author Ben
 */
@Entity
@Synchronizable(alias="cards", order=2)
@OnEntityCreate(processor=NewCardProcessor.class)
public class Card implements WritableSynchronizationEntity, UserScopedEntity {

	public static final Integer UNLIMITED_ACCEPTS = Integer.MAX_VALUE;

	public static final Integer DEFAULT_REDEMPTIONS = 1;

	public static final Integer DEFAULT_MAXIMUM_HOPS = 1;

	
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

	@OneToOne
	@SynchronizeIgnore
	private User owner = null;
	
	private String name = null;
	
	// card offers can have a maximum number of candidate acceptances
	private Integer maximumAccepts = UNLIMITED_ACCEPTS;
	
	// once accepted an offer can have a maximum number of redemptions
	private Integer maximumRedemptions = DEFAULT_REDEMPTIONS;
	
	// cards can have a maximum number of 'hops' (that is, the maximum number of times that an order can be 'forwarded')
	private Integer maximumHops = DEFAULT_MAXIMUM_HOPS;

	@OneToOne
	private Offer lastOffer = null;
	
	// the method by which the card is shared between users
	@Enumerated(value=EnumType.STRING)
	private ShareMethod shareMethod = ShareMethod.CLONE;
	
	// when an offer is made it is cloned, and the same is done for the card for that offer
	// if this is a cloned offer then this is the original
	@OneToOne
	private Card originalCard = null;
	
	// the list of offers that have been based on this card
	@OneToMany
	@Cascade(value=CascadeType.ALL)
	private List<Offer> offers = null;
	
	@OneToMany
	@Cascade(value=CascadeType.ALL)
	private List<CardData> data = null;
	
	
	public Card() {
		
	}

	public Card(User owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	
	public Card(User owner, String name, Integer maximumAccepts, Integer maximumRedemptions, Integer maximumHops, Card originalCard) {
		this.owner = owner;
		this.name = name;
		this.maximumAccepts = maximumAccepts;
		this.maximumRedemptions = maximumRedemptions;
		this.maximumHops = maximumHops;
		this.originalCard = originalCard;
	}

	
	/**
	 * When offers are made a clone of the offered card is made (specifically the 'face' of the card)
	 * 
	 * @param user
	 * @return
	 */
	public Card clone(User user) {
		Card card = new Card(user, this.name, this.maximumAccepts, this.maximumRedemptions, this.maximumHops, this);
		
		if (this.data != null) {
			for (CardData datum : this.getData())  {
				card.addData(datum.clone(card, user));
			}			
		}

		return card;
	}
	
	/**
	 * Determines if a card is currently active (on offer).
	 * 
	 * @return True if the card is on offer, false otherwise.
	 */
	public boolean isActive() {
		if (this.lastOffer == null) {
			return false;
		}
//		if (this.lastOfferStart == null || this.lastOfferEnd == null) {
//			return false;
//		}

		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
//		return now.after(this.lastOfferStart) && now.before(this.lastOfferEnd);
		return now.after(lastOffer.getOfferStart()) && now.before(lastOffer.getOfferEnd());

	}
	
	/**
	 * TODO add a name check to ensure that all card data names are unique
	 * 
	 * @param datum
	 */
	public void addData(CardData datum) {
		if (datum == null) {
			throw new IllegalArgumentException("The datum argument must not be NULL.");
		}
		
		if (this.data == null) {
			this.data = new ArrayList<CardData>();
		}
		
		this.data.add(datum);
	}

	/**
	 * 
	 * @param datum
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaximumAccepts() {
		return maximumAccepts;
	}

	public void setMaximumAccepts(Integer maximumAccepts) {
		this.maximumAccepts = maximumAccepts;
	}

	public Integer getMaximumRedemptions() {
		return maximumRedemptions;
	}

	public void setMaximumRedemptions(Integer maximumRedemptions) {
		this.maximumRedemptions = maximumRedemptions;
	}

	public Integer getMaximumHops() {
		return maximumHops;
	}

	public void setMaximumHops(Integer maximumHops) {
		this.maximumHops = maximumHops;
	}

	public Offer getLastOffer() {
		return lastOffer;
	}

	public void setLastOffer(Offer lastOffer) {
		this.lastOffer = lastOffer;
	}

	public ShareMethod getShareMethod() {
		return shareMethod;
	}

	public void setShareMethod(ShareMethod shareMethod) {
		this.shareMethod = shareMethod;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	public List<CardData> getData() {
		return data;
	}

	public void setData(List<CardData> data) {
		this.data = data;
	}

	public Card getOriginalCard() {
		return originalCard;
	}

	public void setOriginalCard(Card originalCard) {
		this.originalCard = originalCard;
	}
	
}
