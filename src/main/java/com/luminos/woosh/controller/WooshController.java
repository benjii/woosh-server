package com.luminos.woosh.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.beans.OfferBean;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.RemoteBinaryObjectDao;
import com.luminos.woosh.dao.ScanDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.exception.RequestProcessingException;
import com.luminos.woosh.services.BeanConverterService;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;
import com.luminos.woosh.util.GeoSpatialUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO secure this controller - at the moment everything is available to anonymous users (public)
 * TODO for the more complex multi-step methods refactor into a (transactionalised) service class
 * 
 * @author Ben
 */
@Controller
public class WooshController extends AbstractLuminosController {

	private static final Logger LOGGER = Logger.getLogger(WooshController.class);
	
	
	@Autowired
	private CardDao cardDao = null;
	
	@Autowired
	private CardDataDao cardDataDao = null;

	@Autowired
	private OfferDao offerDao = null;

	@Autowired
	private ScanDao scanDao = null;

	@Autowired
	private AcceptanceDao acceptanceDao = null;

	@Autowired
	private UserDao userDao = null;

	@Autowired
	private RemoteBinaryObjectDao remoteBinaryObjectDao = null;
	
	@Autowired
	private CloudServiceProxy cloudServiceProxy = null;
	
	@Autowired
	private BeanConverterService beanConverterService = null;
	
	
	@RequestMapping(value="/card", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt addCard(@RequestBody String name) {
		LOGGER.info("Creating new card named '" + name + "' for user: " + super.getUser().getUsername());
		
		// create the new card for the user
		Card newCard = new Card(super.getUser(), name);
		cardDao.save(newCard);
		
		LOGGER.info("Successfully saved card.");
		
		// return a receipt to the client
		return new Receipt(newCard.getClientId());
	}

	@RequestMapping(value="/card/data", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public void addCardData(@RequestBody String cardId, @RequestBody String name, @RequestBody String value,
							@RequestBody String type, HttpServletRequest request) {

		// TODO refactor into (transactionalised) service class

		Card card = cardDao.findByClientId(cardId);
		CardData data = null;

		// if we can't find the card then fault
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}
		
		LOGGER.info("Adding data to card '" + card.getName() + "' for user: " + super.getUser().getUsername());
		
		if (StringUtils.equalsIgnoreCase(type, "BIN") && request instanceof DefaultMultipartHttpServletRequest ) {
			
			// this is a binary card data item - the 'data' parameter is the ID of the attached multi-part file
			MultiValueMap<String, MultipartFile> attachments = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();			
			
			try {
				byte[] binaryItem = attachments.get(value).get(0).getBytes();
				RemoteBinaryObject rbo = new RemoteBinaryObject(super.getUser(), value);
				
				// upload to S3
				cloudServiceProxy.upload(rbo, binaryItem);
				
				// save the remote pointer to the local database
				remoteBinaryObjectDao.save(rbo);
				
				// create the data item
				data = new CardData(super.getUser(), name, rbo, card);
				
			} catch (IOException e) {
				throw new RequestProcessingException("Could not locate attachment with ID: " + value + ". Bad request?");
			}
			
			
		} else {			
			
			// this is a non-binary card - simply attach the data and save the card
			data = new CardData(super.getUser(), name, value, card);
			cardDataDao.save(data);
			
		}

		// save the card
		card.addData(data);
		cardDao.save(card);
		
		LOGGER.info("Successfully added card data.");
	}

	@RequestMapping(value="/card/{id}", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public CardBean getCard(@PathVariable String id, HttpServletResponse response) {
		Card card = cardDao.findByClientId(id, super.getUser());
		
		if (card == null) {
			throw new EntityNotFoundException(id, "Card entity does not exist or was deleted.");
		}
		
		return beanConverterService.convertCard(card);		
	}

	@RequestMapping(value="/cards", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<CardBean> getCardsForUser() {
		List<Card> cards = cardDao.findAll(super.getUser());

		return beanConverterService.convertCards(cards);
	}

	@RequestMapping(value="/offer", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public void makeOffer(@RequestBody String cardId, @RequestBody Integer duration,
						  @RequestBody Double latitude, @RequestBody Double longitude,
						  @RequestBody Boolean autoAccept) {

		// TODO refactor into (transactionalised) service class
		
		LOGGER.info("Creating new offer for card '" + cardId + "' for user: " + super.getUser().getUsername());
		
		// look up the card
		Card card = cardDao.findByClientId(cardId, super.getUser());
		
		if (card == null) {
			throw new EntityNotFoundException(cardId, "Card entity does not exist or was deleted.");
		}
		
		// create the point geometry
		Geometry offerRegion = GeoSpatialUtils.createPoint(latitude, longitude);
		
		// create and save the offer
		Offer offer = new Offer(super.getUser(), card, offerRegion, autoAccept);
		offerDao.save(offer);
		
		LOGGER.info("Successfully created offer.");		
	}

	@RequestMapping(value="/offers", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<OfferBean> findOffers(@RequestBody Double latitude, @RequestBody Double longitude) {
		
		// TODO refactor into (transactionalised) service class

		User user = super.getUser();
		
		LOGGER.info("Scanning for offers at location (" + latitude + "," + longitude + ") for user: " + user.getUsername());

		// record the location and time of the offer scan (we don't do anything with this data, it's just for historical purposes)
		Scan scan = new Scan(user, GeoSpatialUtils.createPoint(latitude, longitude));
		scanDao.save(scan);
		
		// scan for offers
		List<Offer> availableOffers = offerDao.findOffersWithinRange(scan);

		LOGGER.info("Found " + availableOffers.size() + " offers for user " + user.getUsername() + " at location (" + latitude + "," + longitude + ")");

		// convert each of the offers into beans and return to the user
		List<OfferBean> beans = new ArrayList<OfferBean>();
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
			
			// recard all of this against the user
			user.addCard(cardForOffer);
			user.addAcceptance(acceptance);
			user.addScan(scan);
			
			// now convert the offer and card to beans
			CardBean cardForOfferBean = beanConverterService.convertCard(cardForOffer);
			OfferBean bean = new OfferBean(offer.getClientId(), cardForOfferBean);

			beans.add(bean);
		}
		
		return beans;
	}

	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
}
