package com.luminos.woosh.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import com.luminos.woosh.beans.CandidateOffer;
import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.beans.MakeOfferBean;
import com.luminos.woosh.beans.PingResponse;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.services.BeanConverterService;
import com.luminos.woosh.services.WooshServices;
import com.luminos.woosh.util.GeoSpatialUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * This controller handles all Woosh functions including card creation, making offers, and scanning for offers.
 * 
 * @author Ben
 */
@Controller
public class WooshController extends AbstractLuminosController {

	private static final Logger LOGGER = Logger.getLogger(WooshController.class);
	
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	
	@Autowired
	private CardDao cardDao = null;
	
	@Autowired
	private OfferDao offerDao = null;
		
	@Autowired
	private WooshServices wooshServices = null;
	
	@Autowired
	private BeanConverterService beanConverterService = null;
	

	/**
	 * This method simply returns a ping response. Clients can use this method to ensure reachability to Woosh servers.
	 * 
	 * @return
	 */
	@RequestMapping(value="/m/ping", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public PingResponse ping(HttpServletRequest request) {
		User authenticatedUser = super.getUser();

		LOGGER.info("Received ping request from user '" + authenticatedUser.getUsername() + "' from " + request.getRemoteAddr());

		// record that the device ping'd the server (this also gathers other information that is useful the the client)
		return wooshServices.recordPing(authenticatedUser);
	}
	
	/**
	 * Client devices can call this method to say "hello" to the Woosh servers and register client app and device information.
	 * 
	 * @param appVersion
	 * @param deviceType
	 * @param osVersion
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/m/hello", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public String ping(@RequestParam(required=false, value="v") String appVersion,
					   @RequestParam(required=false, value="type") String deviceType,
					   @RequestParam(required=false, value="os") String osVersion,
					   HttpServletRequest request) {
		
		User authenticatedUser = super.getUser();

		LOGGER.info("Received 'hello' from user '" + authenticatedUser.getUsername() + "' from " + request.getRemoteAddr());

		// record that the device ping'd the server
		wooshServices.recordHello(authenticatedUser, appVersion, deviceType, osVersion);
		
		return "{ \"status\": \"OK\", \"server_time\": \"" + SDF.format(Calendar.getInstance().getTime()) + "\" }";
	}

	/**
	 * 
	 * @param card
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/m/card", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt addCard(@RequestBody CardBean card, HttpServletRequest request) {
		User authenticatedUser = super.getUser();

//		LOGGER.info("Creating new card named '" + card.getName() + "' for user: " + authenticatedUser.getUsername());
		LOGGER.info("Creating new card for user: " + authenticatedUser.getUsername());

		// call the create card service
		Receipt newCardReceipt = wooshServices.createCard(authenticatedUser, card);
		
		LOGGER.info("Successfully created and saved card " + newCardReceipt.getId());
		
		// return a receipt to the client
		return newCardReceipt;
	}
	
	/**
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/m/card/{id}", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public CardBean getCard(@PathVariable String id, HttpServletRequest request /*, HttpServletResponse response */) {
		User authenticatedUser = super.getUser();

		// retrieve the card
		Card card = wooshServices.retrieveCard(id, authenticatedUser);

		if (card == null) {
			throw new EntityNotFoundException(id, "Card entity does not exist or was deleted.");
		}
		
		return beanConverterService.convertCard(card);		
	}

	/**
	 * 
	 * @param id
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/m/card/{id}", method=RequestMethod.DELETE)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt deleteCard(@PathVariable String id, HttpServletResponse response) {
		User authenticatedUser = super.getUser();

		// delete the card
		Card deletedCard = wooshServices.deleteCard(id, authenticatedUser);

		// return a receipt to the client
		return new Receipt(deletedCard.getClientId());
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(value="/m/cards", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<CardBean> getCardsForUser(HttpServletRequest request) {
		User authenticatedUser = super.getUser();

		LOGGER.info("Received request for all cards from user '" + authenticatedUser.getUsername() + "' from " + request.getRemoteAddr());

		// find all of the available (active) offers for the user
		List<Card> cards = wooshServices.findAllCards(authenticatedUser);

		return beanConverterService.convertCards(cards);
	}

	/**
	 * 
	 * @param offer
	 * @return
	 */
	@RequestMapping(value="/m/offer", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt makeOffer(@RequestBody MakeOfferBean offer) {

		LOGGER.info("Creating new offer for card '" + offer.getCardId() + "' for user: " + super.getUser().getUsername());

		// create the point geometry
		Geometry offerRegion = GeoSpatialUtils.createPoint(offer.getLatitude(), offer.getLongitude());

		// create the new offer
		Offer newOffer = wooshServices.createOffer(offer.getCardId(), offer.getDuration(), offerRegion,
												   offer.getAutoAccept(), super.getUser());
		
		LOGGER.info("Successfully created offer.");		
		
		// send a receipt to the client
		return new Receipt(newOffer.getClientId());
	}

	/**
	 * 
	 * @param id
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/m/offer/report/{id}", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt reportOffer(@PathVariable String id, HttpServletResponse response) {
		User authenticatedUser = super.getUser();

		// call the service to expire the offer
		Receipt receipt = wooshServices.reportOffer(id, authenticatedUser);
		
		LOGGER.info("Successfully reported offer " + id);		

		return receipt;
	}

	/**
	 * 
	 * @param id
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/m/offer/expire/{id}", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt expireOffer(@PathVariable String id, HttpServletResponse response) {
		User authenticatedUser = super.getUser();

		// call the service to expire the offer
		Receipt receipt = wooshServices.expireOffer(id, authenticatedUser);
		
		LOGGER.info("Successfully expired offer " + id);		

		return receipt;
	}

	/**
	 * 
	 * @param latitude The reported latitude of the scanning device.
	 * @param longitude The reported latitude of the scanning device.
	 * @param accuracy The accuracy at which the device reported that it was able to determine it's location.
	 * @return
	 */
	@RequestMapping(value="/m/offers", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<CandidateOffer> findOffers(@RequestParam Double latitude,
										   @RequestParam Double longitude,
										   @RequestParam(required=false) Integer accuracy) {

		User authenticatedUser = super.getUser();
		
		LOGGER.info("Scanning for offers at location (" + latitude + "," + longitude + ") for user: " + authenticatedUser.getUsername());

		// create the point geometry
		Point location = GeoSpatialUtils.createPoint(latitude, longitude);
		
		// scan for offers for the user
		List<CandidateOffer> availableOffers = wooshServices.findOffers(location, accuracy, super.getUser());
		
		return availableOffers;
	}

	/**
	 * 
	 * @param id
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/m/offer/accept/{id}", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public Receipt acceptOffer(@PathVariable String id, HttpServletResponse response) {
		User authenticatedUser = super.getUser();

		// accept the offer for the user
		Offer acceptedOffer = wooshServices.acceptOffer(id, authenticatedUser);
		
		// return a success receipt to the client
		return new Receipt(acceptedOffer.getClientId());
	}

	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
}
