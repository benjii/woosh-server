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

import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.SynchronizeChildCollection;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;

/**
 * Card data are key-value pairs representing data items on a woosh card.
 * 
 * @author Ben
 */
@Entity
//@Synchronizable(alias="carddata")
public class CardData implements WritableSynchronizationEntity, UserScopedEntity {

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
	
	// importantly, when this card data is cloned this object is NOT cloned (that is, the data is immutable)
	@OneToOne
	private RemoteBinaryObject binaryData = null;
	
	// as above - we need to re-model this at some point to be a pointer to the data, not the data itself
	// this is so we can clone the 'face' of the card (changing ownership, essentially) without cloning the
	// data itself.
	private String data = null;
	
	// the card that holds this card data
	@OneToOne
	@SynchronizeChildCollection(method="addData")
	private Card card = null;


	public CardData() {
	
	}

	public CardData(User owner, String name, String data, Card card) {
		this.owner = owner;
		this.name = name;
		this.data = data;
		this.card = card;
	}

	public CardData(User owner, String name, RemoteBinaryObject data, Card card) {
		this.owner = owner;
		this.name = name;
		this.binaryData = data;
		this.card = card;
	}

	public CardData(User owner, String name, String data, RemoteBinaryObject binaryData, Card card) {
		this.owner = owner;
		this.name = name;
		this.data = data;
		this.binaryData = binaryData;
		this.card = card;
	}


	/**
	 * 
	 * @param card
	 * @param user
	 * @return
	 */
	public CardData clone(Card card, User user) {
		return new CardData(user, this.name, this.data, this.binaryData, card);
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

	public RemoteBinaryObject getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(RemoteBinaryObject binaryData) {
		this.binaryData = binaryData;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}
	
}
