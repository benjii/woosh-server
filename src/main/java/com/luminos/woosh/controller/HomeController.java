package com.luminos.woosh.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Ben
 */
@Controller
public class HomeController extends AbstractLuminosController {

	@RequestMapping(value={ "/home", "/" }, method=RequestMethod.GET)
	@Secured({ "ROLE_USER" })
	public ModelAndView home() {
		return new ModelAndView("home");
	}

	@RequestMapping(value="/login", method=RequestMethod.GET)
	public ModelAndView login(@RequestParam(required=false) final String err, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("login");
		mav.addObject("err", err);
		return mav;
	}

}
