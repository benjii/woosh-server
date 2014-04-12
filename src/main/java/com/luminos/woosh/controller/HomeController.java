package com.luminos.woosh.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.ScanDao;

/**
 * 
 * @author Ben
 */
@Controller
public class HomeController extends AbstractLuminosController {

	@Autowired
	private CardDao cardDao = null;
	
	@Autowired
	private OfferDao offerDao = null;

	@Autowired
	private AcceptanceDao acceptanceDao = null;

	@Autowired
	private ScanDao scanDao = null;


	/**
	 * 
	 * @return
	 */
	@RequestMapping(value={ "/home", "/" }, method=RequestMethod.GET)
	public ModelAndView home() {
		
		// the Woosh home page just shows a few system statistics right now
		// but hopefully a little comic soon!
		ModelAndView mav = new ModelAndView("home");

		// TODO refactor into a service
		
		mav.addObject("card_count", cardDao.count());
		mav.addObject("full_card_count", cardDao.countAll());
		mav.addObject("offer_count", offerDao.count());
		mav.addObject("acceptance_count", acceptanceDao.count());
		mav.addObject("scan_count", scanDao.count());
		
		return mav;
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(value={ "/support" }, method=RequestMethod.GET)
	public ModelAndView support() {
		return new ModelAndView("support");
	}

	@RequestMapping(value={ "/eula" }, method=RequestMethod.GET)
	public ModelAndView eula() {
		return new ModelAndView("eula");
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(value={ "/syncsvc" }, method=RequestMethod.GET)
	@Deprecated
	public ModelAndView synchronizationService() {
		return new ModelAndView("syncsvc");
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(value={ "/restapi" }, method=RequestMethod.GET)
	public ModelAndView restfulApi() {
		return new ModelAndView("restapi");
	}

	/**
	 * 
	 * @param err
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public ModelAndView login(@RequestParam(required=false) final String err, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("login");
		mav.addObject("err", err);
		return mav;
	}

}
