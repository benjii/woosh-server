package com.luminos.woosh.testutils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * 
 * @author Ben
 */
public class LuminosDefaultMultipartHttpServletRequest extends DefaultMultipartHttpServletRequest {

	/**
	 * 
	 * @param request
	 */
	public LuminosDefaultMultipartHttpServletRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> files) {
		super(request);
		super.setMultipartFiles(files);
	}
	
	

}
