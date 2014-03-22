package com.luminos.woosh.testutils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * 
 * @author Ben
 */
public class WooshDefaultMultipartHttpServletRequest extends DefaultMultipartHttpServletRequest {

	/**
	 * 
	 * @param request
	 */
	public WooshDefaultMultipartHttpServletRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> files) {
		super(request);
		super.setMultipartFiles(files);
	}
	
	

}
