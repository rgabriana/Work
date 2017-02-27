package com.ems;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.ems.ws.AbstractEnlightedWSTest;

@Configuration
@ContextConfiguration(locations = { "" +
		"classpath*:META-INF/spring/*.xml",
		"classpath*:enlighted*.xml" })
public abstract class AbstractEnlightedTest extends AbstractJUnit4SpringContextTests {
	
	protected static final Logger logger = Logger
			.getLogger(AbstractEnlightedWSTest.class);

	@Autowired
	protected MessageSource testattachedMessageSource;

	@BeforeClass
	public static void init() {
	}

	@Before
	public void beforeEveryTest(){
	
	}
	
	@Before
	public void setUp() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
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
