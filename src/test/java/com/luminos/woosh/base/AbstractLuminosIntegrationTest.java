package com.luminos.woosh.base;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.testutils.MockServletContextWebContextLoader;
import com.luminos.woosh.testutils.WooshDefaultMultipartHttpServletRequest;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
//import org.junit.Before;

/**
 * The abstract super class of all Frontyr integration tests.
 * 
 * @author Ben
 */
@ContextConfiguration(locations = { "classpath:woosh-servlet.xml",
								    "classpath:woosh-application-ds.xml",
								    "classpath:woosh-spring-security.xml" },
		  			  loader = MockServletContextWebContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore("Ignored so that Maven does not attempt to run this class as a test suite.")
public abstract class AbstractLuminosIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private UserDao userDao = null;
	
	
	protected static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
	
	// the authenticated user in the session
	protected static User OFFERING_USER = null;
	
	protected static User END_USER = null;

	
	// not run (until the database is installed)
	static {
		// this ensures that the correct schema is available in the test database
		new AnnotationConfiguration().configure("test-hibernate.cfg.xml").buildSessionFactory();
	}
	

	// not run (until the database is installed)
	@Before
	public void setup() {
		User offeringUser = new User("offering-user", "password", "test@test.com");
		userDao.save(offeringUser);

		User endUser = new User("end-user", "password", "end-user@test.com");
		userDao.save(endUser);

		OFFERING_USER = offeringUser;
		END_USER = endUser;

		// set up a fake authentication context
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(END_USER, null));
	}

	
	protected MockHttpServletRequest getRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest(MockServletContextWebContextLoader.SERVLET_CONTEXT);
		
		request.setMethod("GET");
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, super.applicationContext);
		
        return request;
    }

	protected DefaultMultipartHttpServletRequest getMultipartRequest(MultiValueMap<String, MultipartFile> files) {
		DefaultMultipartHttpServletRequest request = new WooshDefaultMultipartHttpServletRequest(this.getRequest(), files);		
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, super.applicationContext);
		
        return request;
    }

	protected MockHttpServletResponse getResponse() {
		return new MockHttpServletResponse();
	}
	
	protected MockHttpSession getSession() {
		return new MockHttpSession(MockServletContextWebContextLoader.SERVLET_CONTEXT);
	}

	protected String resolveAndProcessView(ModelAndView mav) {
		
		try {
			// because we are operating outside of a real web application context we load templates using a relative file path (rather than a classloader)
			// therefore, we get the FreeMarker configuration, swap out the template loader (to an instance of FileTemplateLoader) and then use the configuration
			// instance to get the actual template)
			ServletContext ctx = MockServletContextWebContextLoader.SERVLET_CONTEXT;
			
			Configuration cfg = ((FreeMarkerConfigurer) applicationContext.getBean("freemarkerConfig")).getConfiguration();

			File baseDir = new File(ctx.getRealPath("src/main/webapp/WEB-INF/pages/"));
			cfg.setTemplateLoader(new FileTemplateLoader(baseDir));

			Template template = cfg.getTemplate(mav.getViewName() + ".f");
			StringWriter writer = new StringWriter();

			// process the template (renders the view)
			template.process(mav.getModelMap(), writer);

			return writer.getBuffer().toString();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (TemplateException ex) {
			throw new RuntimeException(ex);
		}
	}


	protected void assertIsValidJson(String jsonString) {
		try {
			// attempt to parse the JSON - if no exception thrown then we don't fail
			JSON_OBJECT_MAPPER.readValue(jsonString, JsonNode.class);
		} catch (JsonMappingException ex) {
			fail("Error during processing of JSON response: " + ex.toString());
		} catch (JsonParseException ex) {
			fail("Error during processing of JSON response: " + ex.toString());
		} catch (IOException ex) {
			fail("Error during processing of JSON response: " + ex.toString());
		}
	}

	protected void assertJsonNodeHasValue(String jsonString, String path, String value) {
		try {
			JsonNode json = JSON_OBJECT_MAPPER.readValue(jsonString, JsonNode.class);
			JsonNode jsonNode = this.findJsonNode(json, path);
		
			if (jsonNode.isMissingNode()) {
				fail("The node at path '" + path + "' was not found in the JSON response.");
			}
			if (!jsonNode.getValueAsText().equalsIgnoreCase(value))  {
				fail("The node at path '" + path + "' was found but it did not have the correct value (expected=" + value + ", actual=" + jsonNode.getValueAsText() + ").");
			}		
		} catch (JsonMappingException ex) {
			throw new RuntimeException(ex);
		} catch (JsonParseException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}

	protected void assertJsonNodeChildCount(String jsonString, String path, Integer expectedChildCount) {
		try {
			JsonNode json = JSON_OBJECT_MAPPER.readValue(jsonString, JsonNode.class);
			JsonNode jsonNode = this.findJsonNode(json, path);
		
			if (jsonNode.isMissingNode()) {
				fail("The node at path '" + path + "' was not found in the JSON response.");
			}
			if (!jsonNode.isArray()) {
				fail("The node at path '" + path + "' was found but is was not an array node and therefore does not have children.");
			}
			if (!(jsonNode.size() == expectedChildCount)) {
				fail("The node at path '" + path + "' was found but it did not have the correct number of children (expected=" + expectedChildCount + ", actual=" + jsonNode.size() + ").");
			}
		} catch (JsonMappingException ex) {
			throw new RuntimeException(ex);
		} catch (JsonParseException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}

	// this method supports basic JSON path notation
	// we support '.' and '[x]' notations currently
	private JsonNode findJsonNode(JsonNode jsonNode, String path) {
		String[] pathComps = StringUtils.split(path, ".");
		JsonNode result = jsonNode;

		// traverse the JSON tree
		for (String comp : pathComps) {			
			if (StringUtils.endsWith(comp, "]")) {
				String nodeName = StringUtils.substringBefore(comp, "[");
				String index = StringUtils.substringBetween(comp, "[", "]");

				result = result.path(nodeName).path(Integer.parseInt(index));
			} else {
				result = result.path(comp);
			}
		}
		return result;
	}

}
