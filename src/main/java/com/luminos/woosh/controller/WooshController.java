package com.luminos.woosh.controller;

import java.io.IOException;
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
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.RemoteBinaryObjectDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.exception.RequestProcessingException;
import com.luminos.woosh.services.BeanConverterService;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;

/**
 * TODO secure this controller - at the moment everything is available to anonymous users (public)
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
	private RemoteBinaryObjectDao remoteBinaryObjectDao = null;
	
	@Autowired
	private CloudServiceProxy cloudServiceProxy = null;
	
	@Autowired
	private BeanConverterService beanConverterService = null;
	
	
	@RequestMapping(value="/card", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public void addCard(@RequestBody String name) {
		LOGGER.info("Creating new card named '" + name + "' for user: " + super.getUser().getUsername());
		
		// create the new card for the user
		Card newCard = new Card(super.getUser(), name);
		cardDao.save(newCard);
		
		LOGGER.info("Successfully saved card.");		
	}

	@RequestMapping(value="/card/data", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	@ResponseBody
	public void addCardData(@RequestBody String cardId, @RequestBody String name, @RequestBody String value,
							@RequestBody String type, HttpServletRequest request) {

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

	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
}
