package com.luminos.woosh.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luminos.woosh.beans.CandidateOffer;
import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.RemoteBinaryObjectDao;
import com.luminos.woosh.dao.RoleDao;
import com.luminos.woosh.dao.ScanDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.Role;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.security.Md5PasswordEncoder;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author Ben
 */
@Service
public class WooshServices {
	
	private static final Logger LOGGER = Logger.getLogger(WooshServices.class);
	
	
	@Autowired
	private CardDao cardDao = null;

	@Autowired
	private CardDataDao cardDataDao = null;	
	
	@Autowired
	private OfferDao offerDao = null;
	
	@Autowired
	private AcceptanceDao acceptanceDao = null;
	
	@Autowired
	private ScanDao scanDao = null;
	
	@Autowired
	private RoleDao roleDao = null;
	
	@Autowired
	private UserDao userDao = null;
	
	@Autowired
	private RemoteBinaryObjectDao remoteBinaryObjectDao = null;
	
	@Autowired
	private CloudServiceProxy cloudServiceProxy = null;

	@Autowired
	private BeanConverterService beanConverterService = null;

	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param email
	 */
	@Transactional
	public User signup(String username, String password, String email) {
		
		// if everything checks out then continue
		Role standardUserRole = roleDao.findByAuthority("ROLE_USER");
		
		// create the new user
		User newUser = new User(username, Md5PasswordEncoder.hashPassword(password), email);
		userDao.save(newUser);

		// grant the standard user role to the new user
		newUser.addAuthority(standardUserRole);
		userDao.save(newUser);	
		
		return newUser;
	}
	
	/**
	 * 
	 * @param cardId
	 * @param name
	 * @param user
	 * @param binary
	 * @return
	 */
	@Transactional
	public CardData addBinaryDataToCard(String cardId, String name, String binaryId, byte[] binary, User user) {
		Card card = cardDao.findByClientId(cardId);
		CardData data = null;

		// if we can't find the card then fault
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}

		// create the remote binary object pointer to store locally
		RemoteBinaryObject rbo = new RemoteBinaryObject(user, binaryId /*UUID.randomUUID().toString()*/);
			
		// upload to S3
		cloudServiceProxy.upload(rbo, binary);
			
		// save the remote pointer to the local database
		remoteBinaryObjectDao.save(rbo);
			
		// create the data item
		data = new CardData(user, name, rbo, card);

		// save the card
		card.addData(data);
		cardDao.save(card);
		
		return data;
	}
	
	/**
	 * 
	 * @param cardId
	 * @param name
	 * @param value
	 * @param user
	 * @return
	 */
	@Transactional
	public CardData addDataToCard(String cardId, String name, String value, User user) {
		Card card = cardDao.findByClientId(cardId);
		CardData data = null;

		// if we can't find the card then fault
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}
		
		// this is a non-binary card - simply attach the data and save the card
		data = new CardData(user, name, value, card);
		cardDataDao.save(data);

		// save the card
		card.addData(data);
		cardDao.save(card);
		
		return data;
	}
	
	/**
	 * 
	 * @param cardId
	 * @param user
	 * @return
	 */
	@Transactional
	public Card deleteCard(String cardId, User user) {
		Card card = cardDao.findByClientId(cardId, user);
		
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}

		// to delete a card we mark it, and all of it's data elements, as deleted
		// we also remove all associated binary data from S3, if needed
		card.setDeleted(Boolean.TRUE);
		cardDao.save(card);
		
		// tag each card data element as deleted and remove any binary data if needed
		if (card.getData() != null && card.getData().size() > 0) {
			
			for (CardData data : card.getData()) {
			
				// flag the data element as deleted
				data.setDeleted(Boolean.TRUE);
				
				// remove binary data if needed
				RemoteBinaryObject remoteBinary  = data.getBinaryData();
				if (remoteBinary != null) {
					cloudServiceProxy.delete(remoteBinary);
					remoteBinary.setDeleted(Boolean.TRUE);
					remoteBinaryObjectDao.save(remoteBinary);
				}

				// flush to the database
				cardDataDao.save(data);
			}
		}

		return card;
	}
	
	/**
	 * 
	 * @param cardId
	 * @param location
	 * @param autoAccept
	 * @param user
	 * @return
	 */
	@Transactional
	public Offer createOffer(String cardId, Integer duration, Geometry location, Boolean autoAccept, User user) {
		Card card = cardDao.findByClientId(cardId, user);
		
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}
				
		// create and save the offer
		Offer offer = new Offer(user, card, duration, location, autoAccept);
		offerDao.save(offer);
		
		// record the last offer time on the card
		card.setLastOffer(offer);
		card.addOffer(offer);

		cardDao.save(card);
		
		return offer;
	}
	
	@Transactional
	public List<CandidateOffer> findOffers(Point location, User user) {
		
		// record the location and time of the offer scan (we don't do anything with this data, it's just for historical purposes)
		Scan scan = new Scan(user, location);
		scanDao.save(scan);
		
		// scan for offers
		List<Offer> availableOffers = offerDao.findOffersWithinRange(scan);
		List<CandidateOffer> beans = new ArrayList<CandidateOffer>();

		LOGGER.info("Found " + availableOffers.size() + " offers for user " + user.getUsername() + " at location (" + location.getX() + "," + location.getY() + ")");

		// process each of the available offers
		for (Offer offer : availableOffers) {
			
			// for every offer we;
			//  a) clone the offered card;
			//  b) record an acceptance on the card (if it is auto-accept);
			//  c) create an offer bean (with the bean version of the cloned card);
			//  d) return the full list to the client
			
			// clone the card
			Card cardForOffer = offer.getCard().clone(user);
			cardDao.save(cardForOffer);
			
			// create the relevant acceptance entity
			Acceptance acceptance = null;
			if (offer.getAutoAccept()) {
				acceptance = new Acceptance(user, cardForOffer, offer, Boolean.TRUE, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			} else {
				acceptance = new Acceptance(user, cardForOffer, offer);				
			}
			acceptanceDao.save(acceptance);
			
			// record the offered card and the offer itself on the scan
			scan.addCard(cardForOffer);
			scan.addOffer(offer);
			scanDao.save(scan);
			
			User copyOfUser = userDao.findByUsername(user.getUsername());
			
			// record all of this against the user
			copyOfUser.addCard(cardForOffer);
			copyOfUser.addAcceptance(acceptance);
			copyOfUser.addScan(scan);
			
			userDao.save(copyOfUser);

			// now convert the offer and card to beans
			CardBean cardForOfferBean = beanConverterService.convertCard(cardForOffer);
			CandidateOffer bean = new CandidateOffer(offer.getClientId(), cardForOfferBean);

			beans.add(bean);
		}
		
		return beans;
	}
	
	/**
	 * 
	 * @param offerId
	 * @param user
	 * @return
	 */
	@Transactional
	public Offer acceptOffer(String offerId, User user) {
		Offer offer = offerDao.findByClientId(offerId);
		
		if (offer == null) {
			throw new EntityNotFoundException(offerId, "Offer entity does not exist or was deleted.");
		}
		
		// update the offer to be accepted
		Acceptance acceptance = acceptanceDao.findForOffer(offer, user);
		acceptance.setAccepted(Boolean.TRUE);
		acceptance.setAcceptedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		acceptanceDao.save(acceptance);
		
		return offer;
	}
	
}
