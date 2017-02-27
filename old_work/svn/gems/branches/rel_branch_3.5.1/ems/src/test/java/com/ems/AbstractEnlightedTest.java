package com.ems;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

import com.ems.communication.ClientHelper;
import com.ems.model.User;
import com.ems.mvc.controller.ApplicationEntryPointController;
import com.ems.mvc.controller.LoginController;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.ws.AbstractEnlightedWSTest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@Configuration
@ContextConfiguration(locations = { "" +
		"classpath*:META-INF/spring/*.xml",
		"classpath*:enlighted*.xml" })
public abstract class AbstractEnlightedTest extends AbstractJUnit4SpringContextTests {
	
	protected static final Logger logger = Logger
			.getLogger(AbstractEnlightedWSTest.class);

	protected MockHttpServletRequest request;
	protected MockHttpServletResponse response;
	protected AnnotationMethodHandlerAdapter handlerAdapter;
	
	@Resource(name = "userManager")
	protected UserManager userManager;
	@Resource(name = "systemConfigurationManager")
	protected SystemConfigurationManager systemConfigurationManager;
	
//	@Autowired
//	private ApplicationContext applicationContext;
	
//	private ApplicationContext applicationContext;
//	public ApplicationContext getApplicationContext() {
//	    return applicationContext;
//	}
//	@Autowired
//	public void setApplicationContext(ApplicationContext applicationContext) {
//	    this.applicationContext = applicationContext;
//	}
	
	protected ApplicationEntryPointController applicationEntryPointController;
	
	public ApplicationEntryPointController getApplicationEntryPointController() {
		return applicationEntryPointController;
	}

	@Autowired
	public void setApplicationEntryPointController(
			ApplicationEntryPointController applicationEntryPointController) {
		this.applicationEntryPointController = applicationEntryPointController;
	}
	
    protected LoginController loginController;
	

	public LoginController getLoginController() {
		return loginController;
	}

	@Autowired
	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	@Autowired
	protected MessageSource testattachedMessageSource;

	@BeforeClass
	public static void init() {
	}

	@Before
	public void beforeEveryTest(){
	
	}
	
	protected HTTPBasicAuthFilter httpAuth = null;
	protected Client getClient() {
		Client client = ClientHelper.createClient();
		httpAuth = new HTTPBasicAuthFilter(getMessage("em.login.username"), getMessage("em.login.password"));
		client.addFilter(httpAuth);
		return client;
	}
	
	
	@Before
	public void setUp() {
		try {
			request = new MockHttpServletRequest();
		    response = new MockHttpServletResponse();
		    final User user = userManager.loadUserByUserName("admin");
		    EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
					user);
		    Authentication authenticated = new UsernamePasswordAuthenticationToken(
					authenticatedUser, null,
					authenticatedUser.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(
					authenticated);
			
//		    request.setMethod("GET");
//		    request.setServerName("localhost");
//		    request.setRemoteAddr("192.168.137.222");
//		    request.setScheme("https");
//		    request.setRequestURI("/wsaction.action");
//		    request.setContentType("application/xml");
//		    final String content = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" +
//		             "<root><request><messageType>1</messageType><body><loginRequest>" +
//		             "<userName>admin</userName><password>admin</password></loginRequest></body>" +
//		             "</request></root>";
//		    request.setContent(content.getBytes());
//		    request.setParameter("body", content);
//		    request.setAttribute("body", content);
//		    (new AnnotationMethodHandlerAdapter()).handle(request, response, loginController);
		    
		    //Assert.assertNotNull(applicationContext.getBean(HandlerAdapter.class));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		// setup the jndi context and the datasource
		try {
			// Create initial context
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					"org.apache.xbean.spring.jndi.SpringInitialContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
			InitialContext ic = new InitialContext();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	protected String getMessage(final String key) {
		return testattachedMessageSource.getMessage(key, null, null, null);
	}


	private Object retrieveEnumFromString(final Object inObj,
			final String methodName, final String enumStringVal, final Class cls)
			throws IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		Method method = inObj.getClass().getMethod(methodName, cls);
		Object childObjs = method.invoke(inObj, enumStringVal);
		if (!"valueOf".equals(methodName)) {
			// method = childObjs.getClass().getMethod(methodName, null);
			// childObjs = method.invoke(childObjs);
		}
		return childObjs;
	}

	private Object retrieveValueFromField(final Object inObj, final Field field)
			throws IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		final String fieldName = field.getName();
		final String capitol = Character.toString(fieldName.charAt(0))
				.toUpperCase();
		final String newString = CoreConstants.GET + capitol
				+ fieldName.substring(1, fieldName.length());
		final Method method = inObj.getClass().getMethod(newString, null);
		final Object childObjs = method.invoke(inObj, null);
		return childObjs;
	}

	private void setValueInField(final Object inObj, final Field field,
			final Object val) throws IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		final String fieldName = field.getName();
		final String capitol = Character.toString(fieldName.charAt(0))
				.toUpperCase();
		final String newString = CoreConstants.SET + capitol
				+ fieldName.substring(1, fieldName.length());
		final Method method = inObj.getClass().getMethod(newString,
				val.getClass());
		final Object childObjs = method.invoke(inObj, val);
	}

	public static Date convertDateToTimeZone(final String dateSource,
			final String zoneTarget) {
		try {
			final TimeZone timeZoneTarget = TimeZone.getTimeZone(zoneTarget);
			final DateFormat formatterSource = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss Z");
			final Date d = formatterSource.parse(dateSource);
			// logger.debug("Source:"+formatterSource.format(d));
			formatterSource.setTimeZone(timeZoneTarget);
			// logger.debug("Target:"+formatterSource.format(d));
			return d;
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}
}
