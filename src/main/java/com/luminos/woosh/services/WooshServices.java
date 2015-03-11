package com.luminos.woosh.services;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luminos.woosh.beans.CandidateOffer;
import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.beans.CardDataBean;
import com.luminos.woosh.beans.PingResponse;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.ConfigurationDao;
import com.luminos.woosh.dao.LogEntryDao;
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
import com.luminos.woosh.domain.common.Configuration;
import com.luminos.woosh.domain.common.LogEntry;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.Role;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.enums.CardDataType;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.exception.InvalidInvitationKeyException;
import com.luminos.woosh.exception.MaximumUsersReachedException;
import com.luminos.woosh.exception.UsernameAlreadyInUseException;
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
	
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static final String USER_LIMIT_KEY = "USER_LIMIT";

	private static final String MOTD_KEY = "MOTD";

	private static final String RADIUS_IN_METRES_KEY = "SCAN_RADIUS_IN_METRES";

	private static final Integer DEFAULT_SCAN_RADIUS = 300;			// the maximum scan radius - use only in an emergency

	private static final Integer MAX_USER_DISTANCE = 5;				// the maximum distance, in metres, that two users are considered in
																	// proximity to each other

	private static final Integer OPTIMAL_DEVICE_ACCURACY = 5;		// if a device reports a lower value than this for its location accuracy
																	// then attempt no further compensation

	private static final Integer MAXIMUM_DEVICE_INACCURACY = 200;	// if the device reports over this value as its location accuracy then
																	// consider the device as "un-Wooshable"

	private static final Integer UNLIMITED_USERS = -1;				// value to indicate that the invitation mechanism is no longer requred

	
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
	private LogEntryDao logEntryDao = null;
	
	@Autowired
	private ConfigurationDao configurationDao = null;
	
	@Autowired
	private RemoteBinaryObjectDao remoteBinaryObjectDao = null;
	
	@Autowired
	private PushNotificationService pushNotificationService = null;	
	
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
	public User signup(String username, String password, String email, String invitationKey) {
		
		// the very first thing that we do is to see if we have any more sign-up slots left
		Configuration userLimitConfig = configurationDao.findByKey(USER_LIMIT_KEY);
		Integer userCount = userDao.count();
		Integer userLimit = Integer.valueOf(userLimitConfig.getValue());
		
		if ( userCount >= userLimit && userLimit != UNLIMITED_USERS ) {
			throw new MaximumUsersReachedException();
		}
		
		// if there are slots remaining then perform additional checks
		
		// check that the username is not already taken
		User existingUser = userDao.findByUsername(username);
		if (existingUser != null) {
			throw new UsernameAlreadyInUseException();
		}
		
		// check that the invitation key exists
		User invitedBy = userDao.findByInvitationalKey(invitationKey);
		if (invitedBy == null) {
			throw new InvalidInvitationKeyException();
		}

		LOGGER.info("User '" + username + "' was invited by '" + invitedBy.getUsername() + "'.");

		// if everything checks out then continue
		Role standardUserRole = roleDao.findByAuthority("ROLE_USER");
		
		// create the new user
		User newUser = new User(username, Md5PasswordEncoder.hashPassword(password), email);
		userDao.save(newUser);

		// grant the standard user role to the new user
		newUser.addAuthority(standardUserRole);
		userDao.save(newUser);
		
		// record the action in the database log
		logEntryDao.save(LogEntry.signedUpEntry(newUser));
		
		return newUser;
	}

	/**
	 * 
	 * @param authenticatedUser
	 */
	@Transactional
	public PingResponse recordPing(User authenticatedUser) {
		PingResponse pingResponse = new PingResponse();
				
		// determine how many slots we have left
		Configuration userLimitConfig = configurationDao.findByKey(USER_LIMIT_KEY);
		Integer userCount = userDao.count();
		Integer userLimit = Integer.valueOf(userLimitConfig.getValue());

		// get the MOTD
		Configuration motd = configurationDao.findByKey(MOTD_KEY);
		
		// construct the ping response object
		pingResponse.setStatus("OK");
		pingResponse.setServerTime(SDF.format(Calendar.getInstance().getTime()));
		pingResponse.setTotalSlotsAvailable(userLimit);
		pingResponse.setRemainingUserSlots(userLimit - userCount);
		pingResponse.setMotd(motd.getValue());
		
		// log the action to the database for audit purposes
		logEntryDao.save(LogEntry.pingEntry());
		
		return pingResponse;
	}	

	/**
	 * 
	 * @param user
	 */
	@Transactional
	public void recordLoginTime(User user) {
		
		// refresh the user and record the last logged in time
		User refreshedUser = userDao.findById(user.getId());
		refreshedUser.setLastLogin( new Timestamp(Calendar.getInstance().getTimeInMillis() ));
		userDao.save(refreshedUser);
	}

	/**
	 * 
	 * @param user
	 * @param apnsToken
	 */
	@Transactional
	public void updateApnsToken(User user, String apnsToken) {
		LOGGER.info("User '" + user.getUsername() + "' submitted their APNS token (" + apnsToken + ")");

		User refreshedUser = userDao.findById(user.getId());
		refreshedUser.setApnsToken(apnsToken);
		userDao.save(refreshedUser);

		// log the action to the database for audit purposes
		logEntryDao.save(LogEntry.apnsTokenEntry(refreshedUser));	
	}

	/**
	 * 
	 * @param user
	 * @param appVersion
	 * @param deviceType
	 * @param osVersion
	 */
	@Transactional
	public void recordHello(User user, String appVersion, String deviceType, String osVersion) {

		LOGGER.info("User '" + user.getUsername() + "' said hello (app version=" + appVersion + ", device type=" + deviceType + ", OS version=" + osVersion + ")");
		
		// refresh the user and record the last logged in time
		User refreshedUser = userDao.findById(user.getId());
		refreshedUser.setAppVersion(appVersion);
		refreshedUser.setDeviceType(deviceType);
		refreshedUser.setOsVersion(osVersion);
		refreshedUser.setLastLogin( new Timestamp(Calendar.getInstance().getTimeInMillis() ));
		userDao.save(refreshedUser);		

		// log the action to the database for audit purposes
		logEntryDao.save(LogEntry.clientHelloEntry(refreshedUser));		
	}

	/**
	 * 
	 * @param authenticatedUser
	 */
	@Transactional
	public void authenticate(User authenticatedUser) {
		this.recordLoginTime(authenticatedUser);
		
		// log the fact that the user authenticated successfully
		LOGGER.info("User '" + authenticatedUser.getUsername() + "' authenticated successfully.");

		// log the action to the database for audit purposes
		logEntryDao.save(LogEntry.loggedInEntry(authenticatedUser));		
	}
	
	/**
	 * 
	 * @param user
	 * @param card
	 * @return
	 */
	@Transactional
	public Receipt createCard(User user, CardBean card) {

		// create the new card for the user
		Card newCard = new Card(user /*, card.getName() */);
		cardDao.save(newCard);
		
		// now create the card data and associate it with the card
		if (card.getData() != null) {
			
			for (CardDataBean dataBean : card.getData()) {
				if (dataBean.getType() == CardDataType.BINARY) {
								
					// this is binary data - decode it
					byte[] decodedBinary = DatatypeConverter.parseBase64Binary(dataBean.getValue());
					this.addBinaryDataToCard(newCard.getClientId(), 
											 dataBean.getName(), 
											 dataBean.getBinaryId(),
											 decodedBinary,
											 user);

				} else {
					
					// this is a non-binary (string or similar) attachment
					this.addDataToCard(newCard.getClientId(), 
									   dataBean.getName(), 
									   dataBean.getValue(), 
									   user);					
					
				}
			}
		}

		// log the action to the database for audit purposes
		logEntryDao.save(LogEntry.createCardEntry(user));		

		// return a receipt for the new card
		return new Receipt(newCard.getClientId());
	}
		
	/**
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@Transactional
	public Card retrieveCard(String id, User user) {
		
		// retrieve the card
		Card card = cardDao.findByClientId(id, user);

		// log the action to the database for audit purposes
		// TODO log if we can't find the card?
		if (card != null) {
			logEntryDao.save(LogEntry.retrieveCardEntry(user));			
		}
	
		return card;
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
		
		// TODO log if we can't find the card?
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

		// record the action in the database log
		logEntryDao.save(LogEntry.deleteCardEntry(user));			

		return card;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	@Transactional
	public List<Card> findAllCards(User user) {
		List<Card> cards = cardDao.findAllOrderedByOfferStart(user);
		
		// record the action in the database log
		logEntryDao.save(LogEntry.retrieveAllCardsEntry(user));			

		return cards;
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
		
		// record the action in the database log
		logEntryDao.save(LogEntry.makeOfferEntry(user));			

		return offer;
	}
	
	/**
	 * 
	 * @param location
	 * @param user
	 * @return
	 */
	@Transactional
	public List<CandidateOffer> findOffers(Point location, Integer accuracy, User user) {
		
		// record the location and time of the offer scan (we don't do anything with this data, it's just for historical purposes)
		Scan scan = new Scan(user, location);
		scan.setReportedAccuracy(accuracy);
		scanDao.save(scan);

		// when determining the scan radius we;
		//  1. use the 'accuracy' parameter, if present
		//  2. use the database-configured value, if present
		//	3. use the default scan radius
		
		Integer scanRadius = DEFAULT_SCAN_RADIUS;
		if ( accuracy != null && accuracy >= OPTIMAL_DEVICE_ACCURACY ) {
			
			// if the location accuracy reported by the device is too high, reduce it to an acceptable value
			// this protects Woosh from scanning an area that is too large whilst maintaining a reasonable chance
			// of the user finding the offer(s) that they except / want
			if ( accuracy >= MAXIMUM_DEVICE_INACCURACY ) {
				accuracy = MAXIMUM_DEVICE_INACCURACY;
			}
			
			// when a device reports it's location accuracy for a scan we assume that the offering device(s)
			// were at the same accuracy (the 'worst-case' scenario that we consider)
			// therefore, to 'guarantee' picking up offers in the area the scan radius is
			//		2x + y
			// where;
			//		x = accuracy reported by the scanning device
			//		y = the maximum distance two users can be apart to be considered 'proximal' to each other
			
			scanRadius = (2 * accuracy) + MAX_USER_DISTANCE;
		
			LOGGER.info("User '" + user.getUsername() + "' device provided location accuracy and is scanning for offers at an accuracy of " + scanRadius + " metres.");

		} else {
		
			Configuration databaseConfiguredRadius = configurationDao.findByKey(RADIUS_IN_METRES_KEY);

			if ( databaseConfiguredRadius != null ) {
				scanRadius = new Integer(databaseConfiguredRadius.getValue());
				LOGGER.info("User '" + user.getUsername() + "' device did not provide location accuracy and is scanning for offers at the default database-configured accuracy of " + scanRadius + " metres.");
			} else {
				scanRadius = DEFAULT_SCAN_RADIUS;
				LOGGER.info("User '" + user.getUsername() + "' device did not provide location accuracy and is scanning for offers at the default system accuracy of " + scanRadius + " metres.");
			}
			
		}
		
		// scan for offers
		List<Offer> availableOffers = offerDao.findOffersWithinRange(scan, scanRadius);
		List<CandidateOffer> beans = new ArrayList<CandidateOffer>();

		// record some additional scan data
		scan.setScanRadius(scanRadius);
		scan.setNumberOfOffersFound(availableOffers.size());
		scanDao.save(scan);

		LOGGER.info("Found " + availableOffers.size() + " offers for user " + user.getUsername() + " at location (" + location.getX() + "," + location.getY() + ")");

		// process each of the available offers
		for (Offer offer : availableOffers) {
			
			// for every offer we create a candidate offer
	
			// record the offered card (and the offer itself) on the scan
			scan.addOffer(offer);
			scanDao.save(scan);
			
			User copyOfUser = userDao.findByUsername(user.getUsername());
			
			// record all of this against the user
			copyOfUser.addCard(offer.getCard());
			copyOfUser.addScan(scan);
			
			userDao.save(copyOfUser);

			CardBean cardBeingOffered = beanConverterService.convertCard(offer.getCard());
			CandidateOffer bean = new CandidateOffer(offer.getClientId(), cardBeingOffered);

			beans.add(bean);
		}
		
		// record the action in the database log
		logEntryDao.save(LogEntry.findActiveOffersEntry(user));			

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
		
		// when an offer is accepted we;
		//	1. clone the card that was offered
		//	2. record the acceptance of that offer

		Card originalCard = offer.getCard();

		// clone the card
		Card clonedCard = originalCard.clone(user, offer);
		cardDao.save(clonedCard);

		// record the acceptance
		Acceptance acceptance = new Acceptance(user, clonedCard, offer, Boolean.TRUE, new Timestamp(Calendar.getInstance().getTimeInMillis()));
		acceptanceDao.save(acceptance);
		
		// record the acceptance on the original card
		originalCard.addAcceptance(acceptance);
		cardDao.save(originalCard);
		
		User refreshedUser = userDao.findByUsername(user.getUsername());
		
		// record all of this against the user
		refreshedUser.addCard(clonedCard);
		refreshedUser.addAcceptance(acceptance);

		// send a push notification to the offer owner that their offer was accepted
		pushNotificationService.alert(offer.getOwner(), "Your offer was accepted by " + user.getUsername());
		
		// record the action in the database log
		logEntryDao.save(LogEntry.acceptOfferEntry(user));			

		return offer;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public Receipt expireOffer(String id, User user) {
		Offer offerToExpire = offerDao.findByClientId(id);

		// check to see if this offer is already expired
		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());		
		
		// if we are then note it in the log, but otherwise proceed normally
		if ( offerToExpire.getOfferEnd().before(now) ) {
			LOGGER.info("Offer '" + offerToExpire.getId() + "' is already expired, but we'll expire it anyway.");
		}
		
		// to expire an offer we simply move the offer end time to be right now
		offerToExpire.setOfferEnd(now);
		offerDao.save(offerToExpire);
		
		// send a push notification to confirm to the user that their offer expired
		Point offerLocation = (Point) offerToExpire.getOfferRegion();
		pushNotificationService.alert(user, "An offer that you made at (" + offerLocation.getX() + "," + offerLocation.getY() + ") has now expired.");
		
		// record the action in the database log
		logEntryDao.save(LogEntry.expireOfferEntry(user));			

		return new Receipt(offerToExpire.getClientId());
	}

	/**
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@Transactional
	public Receipt reportOffer(String id, User user) {
		Offer offerToReport = offerDao.findByClientId(id);
		
		// when an offer is reported we want to flag the original card as well as the card clone that
		// was created when the objecting user accepted the card
		Card originalCardToReport = offerToReport.getCard();
				
		Acceptance acceptance = acceptanceDao.findForOffer(offerToReport, user);
		Card clonedCardToReport = acceptance.getCard();
		
		// when an offer is reported we expire and delete the offer
		offerToReport.setOfferEnd(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		offerToReport.setDeleted(Boolean.TRUE);
		offerDao.save(offerToReport);
		
		// also delete the cards
		originalCardToReport.setDeleted(Boolean.TRUE);
		cardDao.save(originalCardToReport);

		clonedCardToReport.setDeleted(Boolean.TRUE);
		cardDao.save(clonedCardToReport);

		// record the action in the database log
		logEntryDao.save(LogEntry.reportOfferEntry(user));			

		return new Receipt(offerToReport.getClientId());
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
	private CardData addBinaryDataToCard(String cardId, String name, String binaryId, byte[] binary, User user) {
		Card card = cardDao.findByClientId(cardId);
		CardData data = null;

		// if we can't find the card then fault
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}

		// create the remote binary object pointer to store locally
		RemoteBinaryObject rbo = new RemoteBinaryObject(user, binaryId);
			
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
	private CardData addDataToCard(String cardId, String name, String value, User user) {
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
	
}
