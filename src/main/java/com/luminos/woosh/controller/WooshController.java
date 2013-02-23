package com.luminos.woosh.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

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
import com.luminos.woosh.beans.CardDataBean;
import com.luminos.woosh.beans.OfferBean;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.ScanDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.enums.CardDataType;
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
	public String ping(HttpServletRequest request) {
		LOGGER.info("Received ping from: " + request.getRemoteAddr());
		return "{ \"status\": \"OK\" }";
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
		LOGGER.info("Creating new card named '" + card.getName() + "' for user: " + super.getUser().getUsername());
		
		// create the new card for the user
		// TODO refactor this so that it's included with the services area (so that it is correctly transactionalised)
		Card newCard = new Card(super.getUser(), card.getName());
		cardDao.save(newCard);
		
		// now create the card data and associate it with the card
		if (card.getData() != null) {
			
			for (CardDataBean dataBean : card.getData()) {
				if (dataBean.getType() == CardDataType.BINARY) {
								
					// this is binary data - decode it
					byte[] decodedBinary = DatatypeConverter.parseBase64Binary(dataBean.getValue());
					wooshServices.addBinaryDataToCard(newCard.getClientId(), dataBean.getName(), decodedBinary, super.getUser());

				} else {
					
					// this is a non-binary (string or similar) attachment
					wooshServices.addDataToCard(newCard.getClientId(), dataBean.getName(), dataBean.getValue(), super.getUser());					
					
				}
			}
		}
		
		LOGGER.info("Successfully saved card.");
		
		// return a receipt to the client
		return new Receipt(newCard.getClientId());
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
		Card card = cardDao.findByClientId(id, super.getUser());
		
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
		
		// delete the card
		Card deletedCard = wooshServices.deleteCard(id, super.getUser());

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
	public List<CardBean> getCardsForUser() {
		List<Card> cards = cardDao.findAll(super.getUser());

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
	public Receipt makeOffer(@RequestBody OfferBean offer) {

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
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	@RequestMapping(value="/m/offers", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<CandidateOffer> findOffers(@RequestParam Double latitude, @RequestParam Double longitude) {
		
		User user = super.getUser();
		
		LOGGER.info("Scanning for offers at location (" + latitude + "," + longitude + ") for user: " + user.getUsername());

		// create the point geometry
		Point location = GeoSpatialUtils.createPoint(latitude, longitude);
		
		// scan for offers for the user
		List<CandidateOffer> availableOffers = wooshServices.findOffers(location, super.getUser());
		
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

		// accept the offer for the user
		Offer acceptedOffer = wooshServices.acceptOffer(id, super.getUser());
		
		// return a success receipt to the client
		return new Receipt(acceptedOffer.getClientId());
	}

	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
}
