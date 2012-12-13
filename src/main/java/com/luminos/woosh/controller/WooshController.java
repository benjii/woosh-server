package com.luminos.woosh.controller;

import org.apache.commons.lang.StringUtils;
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
import com.luminos.woosh.beans.CardDataBean;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.enums.CardDataType;
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
	private CloudServiceProxy s3proxy = null;
	
	
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
	public CardBean getCard(@PathVariable String id) {
		Card card = cardDao.findByClientId(id/*, super.getUser()*/);
		CardBean cardBean = new CardBean(card);
		
		if (card.getData() != null) {
			for (CardData data : card.getData()) {
				CardDataBean dataBean = null;
				
				if ( StringUtils.isNotBlank(data.getData()) ) {
					dataBean = new CardDataBean(data.getName(), data.getData(), CardDataType.TEXT);
				} else {
					dataBean = new CardDataBean(data.getName(), s3proxy.createSignedUrl(data.getBinaryData()), CardDataType.BINARY);					
				}

				cardBean.addDatum(dataBean);
			}			
		}
		
		return cardBean;	
	}

}
