package com.luminos.woosh.controller;

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
import com.luminos.woosh.beans.CandidateOffer;
import com.luminos.woosh.beans.Receipt;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.ScanDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.exception.RequestProcessingException;
import com.luminos.woosh.services.BeanConverterService;
import com.luminos.woosh.services.WooshServices;
import com.luminos.woosh.util.GeoSpatialUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
	private WooshServices wooshServices = null;
	
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
	public Receipt addCardData(@RequestBody String cardId, @RequestBody String name, @RequestBody String value,
							@RequestBody String type, HttpServletRequest request) {

		LOGGER.info("Adding data to card " + cardId + " (name=" + name + ", value=" + value + ").");

		CardData data = null;
		
		// perform an action depending on what type of data is being attached to the card
		if (StringUtils.equalsIgnoreCase(type, "BIN") && request instanceof DefaultMultipartHttpServletRequest ) {
			
			// this is a binary data attachment (photograph or similar) - call the data service to upload to S3
			MultiValueMap<String, MultipartFile> attachments = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();
			
			// check to see if the binary data was correctly attached to the request
			if (attachments.get(value) == null || attachments.get(value).size() == 0) {
				throw new RequestProcessingException("Could not locate binary attachment with ID " + value + ". Bad request?");
			}
			
			// if so, then extract the attachment from the request
			MultipartFile binary = attachments.get(value).get(0);
			
			// call a service method to upload the binary data to S3, create the card data entity, and attach it to card
			data = wooshServices.addBinaryDataToCard(cardId, name, super.getUser(), binary);
			
		} else {
			
			// this is a non-binary (string or similar) attachment
			data = wooshServices.addDataToCard(cardId, name, value, super.getUser());
			
		}
		
		LOGGER.info("Successfully added card data.");
		
		return new Receipt(data.getClientId());
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
	public Receipt makeOffer(@RequestBody String cardId, @RequestBody Integer duration,
						  @RequestBody Double latitude, @RequestBody Double longitude,
						  @RequestBody Boolean autoAccept) {

		LOGGER.info("Creating new offer for card '" + cardId + "' for user: " + super.getUser().getUsername());

		// create the point geometry
		Geometry offerRegion = GeoSpatialUtils.createPoint(latitude, longitude);

		// create the new offer
		Offer newOffer = wooshServices.createOffer(cardId, offerRegion, autoAccept, super.getUser());
		
		LOGGER.info("Successfully created offer.");		
		
		// send a receipt to the client
		return new Receipt(newOffer.getClientId());
	}

	@RequestMapping(value="/offers", method=RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public List<CandidateOffer> findOffers(@RequestBody Double latitude, @RequestBody Double longitude) {
		
		User user = super.getUser();
		
		LOGGER.info("Scanning for offers at location (" + latitude + "," + longitude + ") for user: " + user.getUsername());

		// create the point geometry
		Point location = GeoSpatialUtils.createPoint(latitude, longitude);
		
		// scan for offers for the user
		List<CandidateOffer> availableOffers = wooshServices.findOffers(location, super.getUser());
		
		return availableOffers;
	}

	@RequestMapping(value="/offer/accept/{id}", method=RequestMethod.GET)
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
