package com.ems.ws;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;

import com.ems.AbstractEnlightedTest;
import com.ems.communication.ClientHelper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public abstract class AbstractEnlightedWSTest extends AbstractEnlightedTest {

	 static {

	        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()

	            {   @Override

	        public boolean verify(String hostname, SSLSession arg1) {
	            	return true;

	              }
	               });
	         }
	protected String HOST_ADDR = "";
	
	private HTTPBasicAuthFilter httpAuth = null;

	protected String EMAIL_SEND_WS_PATH = HOST_ADDR+"/ems/services/org/email/v1/send";

	protected MockHttpServletRequest req;
	protected MockHttpServletResponse res;
	protected ServletContext servletContext;
	protected MockServletConfig servletConfig;
	protected static final Logger logger = Logger
			.getLogger(AbstractEnlightedWSTest.class);

	@Before
	public void beforeEveryTest(){
		super.beforeEveryTest();
		HOST_ADDR = getMessage("webservice.server");
		EMAIL_SEND_WS_PATH = HOST_ADDR + EMAIL_SEND_WS_PATH;
		System.setProperty("path.prefix", "src/main/webapp");
	}
	protected String getMessage(final String key) {
		return testattachedMessageSource.getMessage(key, null, null, null);
	}

	protected Client getClient() {
		Client client = ClientHelper.createClient();
		httpAuth = new HTTPBasicAuthFilter(getMessage("em.login.username"), getMessage("em.login.password"));
		client.addFilter(httpAuth);
		return client;
	}
	
	/**
	 * Use this when testing /api/ API webservices
	 * @return
	 */
	protected Client getClientWithoutAuth() {
		Client client = ClientHelper.createClient();
		//client.addFilter(httpAuth);
		return client;
	}
	
	protected Builder getWebResource(final String url) {
		return getWebResource(url, null);
	}

	protected Builder getWebResource(final String url, String mediaType) {
		Client client = getClient();
		WebResource webResource = client.resource(url);
		Builder b = null;
		if (StringUtils.isEmpty(mediaType)) {
			webResource.accept(MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_XML);
			b = webResource.type(MediaType.WILDCARD);
		} else {
			webResource.accept(mediaType);
			b = webResource.type(mediaType);
		}


		//b.header(FilterConstants.USER_ATTRIBUTE, "admin");
		//b.header(FilterConstants.TOKEN, getToken());
		return b;
	}

	/**
	 * Use this when testing /api/ API webservices
	 * @param url
	 * @return
	 */
	protected Builder getWebResourceWithoutAuth(final String url) {
		return getWebResourceWithoutAuth(url, null);
	}

	/**
	 * Use this when testing /api/ API webservices
	 * @param url
	 * @param mediaType
	 * @return
	 */
	protected Builder getWebResourceWithoutAuth(final String url, String mediaType) {
		Client client = getClientWithoutAuth();
		WebResource webResource = client.resource(url);
		Builder b = null;
		if (StringUtils.isEmpty(mediaType)) {
			webResource.accept(MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_XML);
			b = webResource.type(MediaType.WILDCARD);
		} else {
			webResource.accept(mediaType);
			b = webResource.type(mediaType);
		}


		//b.header(FilterConstants.USER_ATTRIBUTE, "admin");
		//b.header(FilterConstants.TOKEN, getToken());
		return b;
	}

	@Before
	public void setUp() {
		try {
			super.setUp();
			req = new MockHttpServletRequest();
			res = new MockHttpServletResponse();
			final String servletName = "click servlet";
			final String key = "initKey";
			final String value = "initValue";
			servletContext = new MockServletContext();
			req.setParameter("Uname", "admin");
			req.setParameter("password", "#Admin123");
			// servletConfig = new MockServletConfig(servletContext,
			// LoginServlet.class.getSimpleName());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
