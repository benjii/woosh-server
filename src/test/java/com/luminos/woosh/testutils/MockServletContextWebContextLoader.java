package com.luminos.woosh.testutils;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * 
 * @author Ben
 */
public class MockServletContextWebContextLoader extends AbstractContextLoader {

	public static final ServletContext SERVLET_CONTEXT = new MockServletContext("", new FileSystemResourceLoader());
	
	
	protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
		return new XmlBeanDefinitionReader(context);
	}
	
	public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception {
		final GenericWebApplicationContext webCtx = new GenericWebApplicationContext();
		
		SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webCtx);

		webCtx.setServletContext(SERVLET_CONTEXT);
		createBeanDefinitionReader(webCtx).loadBeanDefinitions(locations);
		
		AnnotationConfigUtils.registerAnnotationConfigProcessors(webCtx);		

		webCtx.refresh();
		webCtx.registerShutdownHook();
		
		return webCtx;
	}
	
	protected String getResourceSuffix() {
		return "-context.xml";
	}
    
}
