package com.luminos.woosh.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.util.RedirectUrlBuilder;

/**
 * 
 * @author Ben
 */
public class LuminosAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static Logger LOGGER = Logger.getLogger(LuminosAuthenticationEntryPoint.class);

    private PortMapper portMapper = new PortMapperImpl();

    private PortResolver portResolver = new PortResolverImpl();

    private boolean forceHttps = false;

	private String loginUrl = null;
	

	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String requestUrl = request.getRequestURI();
        String mobileUriContext = request.getContextPath() + "/m/";

        if (StringUtils.startsWith(requestUrl, mobileUriContext)) {
			LOGGER.info("Authorization from device failed. Invalid credentials supplied.");
            response.addHeader("WWW-Authenticate", "Basic realm=\"woosh\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        } 
        else {
            String redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
        }        
	}

	private String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        int serverPort = portResolver.getServerPort(request);
        String scheme = request.getScheme();

        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();

        urlBuilder.setScheme(scheme);
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(serverPort);
        urlBuilder.setContextPath(request.getContextPath());
        urlBuilder.setPathInfo(this.loginUrl);

        if (forceHttps && "http".equals(scheme)) {
            Integer httpsPort = portMapper.lookupHttpsPort(new Integer(serverPort));

            if (httpsPort != null) {
                urlBuilder.setScheme("https");
                urlBuilder.setPort(httpsPort.intValue());
            }
        }

        return urlBuilder.getUrl();
	}


	@Required
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	
}
