package com.luminos.woosh.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.luminos.woosh.beans.CardBean;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.exception.EntityNotFoundException;
import com.luminos.woosh.services.BeanConverterService;

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
		
}
