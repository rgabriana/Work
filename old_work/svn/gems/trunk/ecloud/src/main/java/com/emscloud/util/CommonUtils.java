package com.emscloud.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.emscloud.model.SystemConfiguration;
import com.emscloud.security.exception.EcloudValidationException;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.action.SpringContext;
import com.emscloud.dao.AppInstanceDao;
import com.emscloud.dao.EmInstanceDao;

public class CommonUtils {

	public static Logger logger = Logger.getLogger(CommonUtils.class.getName());
	private static int BASE_FREE_PORT = 32769;
	private static int NEXT_FREE_PORT = 32769;
	private static int MAX_PORT_VALUE = 61000;

	private static int BASE_FREE_PORT_APP = 25000;
	private static int NEXT_FREE_PORT_APP = 25000;
	private static int MAX_PORT_VALUE_APP = 32700;
	
	private static EmInstanceDao emInstanceDao = null;
	private static AppInstanceDao appInstanceDao = null;
	
	private final static Log log = LogFactory.getLog(CommonUtils.class);

	private static final String STRING_TYPE = "java.lang.String";
	private static final String DATE_TYPE = "java.util.Date";

	public static boolean isNull(final Object argumentValue) {
		return (argumentValue == null);
	}

	public static boolean isNullOrEmpty(final String argumentValue) {
		return (isNull(argumentValue) || argumentValue.length() ==0);
	}

	@SuppressWarnings("unchecked")
	public static boolean isNullOrEmpty(Collection argumentValue) {
		return (isNull(argumentValue) || argumentValue.size() == 0);
	}

	public static void checkNull(final String argumentName, final Object argumentValue) {
		if (isNull(argumentValue))
			throw new IllegalArgumentException("The supplied property " + argumentName + " was null");
	}

	public static void checkNullOrEmpty(final String argumentName, final String argumentValue) {
		if (isNullOrEmpty(argumentValue))
			throw new IllegalArgumentException("The supplied String property " + argumentName + " was null or empty");
	}
	
	// this method now selects the next port after incrementing 
	// and checking it instead of random port and we ensure that
	// race condition is taken care of.
	public static synchronized int getRandomPort() {
		return getNextFreePortInternal(1);
	}

	private static int getNextFreePortInternal(int repeat) {
		int port = NEXT_FREE_PORT;
		boolean first = true;
		if(emInstanceDao == null) {
			emInstanceDao = (EmInstanceDao)SpringContext.getBean("emInstanceDao");
		}
		try {
			while(emInstanceDao.checkPortInUseForSSHORWebappByAnyEM(port)) {
				NEXT_FREE_PORT++;
				if(NEXT_FREE_PORT > MAX_PORT_VALUE) {
					if(first) {
						NEXT_FREE_PORT = BASE_FREE_PORT;
						first = false;
					}
					else {
						logger.error("Max Port value reached while trying to assign free port");
						return -1;
					}
				}
				port = NEXT_FREE_PORT;
			}
			ServerSocket server = new ServerSocket(port);
			logger.info("Socket port = " + server.getLocalPort());
			server.close();
		} catch (Exception e) {
			logger.error("Error opening/closing socket on port = " + port , e);
			if(repeat < 5) {
				NEXT_FREE_PORT++;
				if(NEXT_FREE_PORT > MAX_PORT_VALUE) {
					NEXT_FREE_PORT = BASE_FREE_PORT;
				}
				port = getNextFreePortInternal(++repeat);
			}
			else {
				port =  -1;
			}
		}
		return port;
	}

	// this method now selects the next port after incrementing 
	// and checking it instead of random port and we ensure that
	// race condition is taken care of.
	public static synchronized int getRandomPort_App() {
		return getNextFreePortInternal_App(1);
	}

	private static int getNextFreePortInternal_App(int repeat) {
		int port = NEXT_FREE_PORT_APP;
		boolean first = true;
		if(appInstanceDao == null) {
			appInstanceDao = (AppInstanceDao)SpringContext.getBean("appInstanceDao");
		}
		try {
			while(appInstanceDao.checkPortInUseForSSHORWebappByAnyApp(port)) {
				NEXT_FREE_PORT_APP++;
				if(NEXT_FREE_PORT_APP > MAX_PORT_VALUE_APP) {
					if(first) {
						NEXT_FREE_PORT_APP = BASE_FREE_PORT_APP;
						first = false;
					}
					else {
						logger.error("Max Port value reached while trying to assign free port for App");
						return -1;
					}
				}
				port = NEXT_FREE_PORT_APP;
			}
			ServerSocket server = new ServerSocket(port);
			logger.info("Socket port = " + server.getLocalPort());
			server.close();
		} catch (Exception e) {
			logger.error("Error opening/closing socket on port = " + port , e);
			if(repeat < 5) {
				NEXT_FREE_PORT_APP++;
				if(NEXT_FREE_PORT_APP > MAX_PORT_VALUE_APP) {
					NEXT_FREE_PORT_APP = BASE_FREE_PORT_APP;
				}
				port = getNextFreePortInternal_App(++repeat);
			}
			else {
				port =  -1;
			}
		}
		return port;
	}
	
	public static String getHostName() {
		String hostName = null;
		Runtime run = Runtime.getRuntime();
		BufferedReader buf = null;
		try {
			Process pr = run.exec("hostname");
			buf = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
				hostName = line;
			}
			pr.waitFor();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(buf);
		}

		return hostName;
	}

	public static void reloadApache() {
		Runtime run = Runtime.getRuntime();
		BufferedReader buf =  null;
		try {
			Process pr = run.exec("sudo /etc/init.d/apache2 reload");
			buf = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
				System.out.println(line);
				logger.info(line);
			}
			pr.waitFor();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(buf);
		}

	}

	/**
	 * Convenience method to get the application's URL based on request
	 * variables.
	 */
	public static String getAppURL(HttpServletRequest request) {
		StringBuffer url = new StringBuffer();
		int port = request.getServerPort();
		if (port < 0) {
			port = 80; // Work around java.net.URL bug
		}
		String scheme = request.getScheme();
		url.append(scheme);
		url.append("://");
		url.append(request.getServerName());
		if ((scheme.equals("http") && (port != 80))
				|| (scheme.equals("https") && (port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(request.getContextPath());
		return url.toString();
	}
	 /**
     * Unmarshal XML to Wrapper and return List value.
     */
	@SuppressWarnings("unchecked")
	public static <T> List<T> unmarshal(Unmarshaller unmarshaller,
			Class<T> clazz, String xml) throws JAXBException {
		 StreamSource xmlSource = new StreamSource(new StringReader (xml));
		Wrapper<T> wrapper = (Wrapper<T>) unmarshaller.unmarshal(xmlSource,Wrapper.class).getValue();
		return wrapper.getItems();
	}
	 /**
     * Wrap List in Wrapper, then leverage JAXBElement to supply root element
     * information.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String marshal(Marshaller marshaller, List<?> list, String name)
            throws JAXBException {
    	StringWriter sw = new StringWriter();
        QName qName = new QName(name);
        Wrapper wrapper = new Wrapper(list);
        JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
                Wrapper.class, wrapper);
        marshaller.marshal(jaxbElement, sw);
        return sw.toString() ;
    }
    
	public static String getSaltBasedDigest(String key, String secret,
			String salt, String algo) throws Exception  {
		if (isNullOrEmpty(key)
				|| isNullOrEmpty(secret)
				|| isNullOrEmpty(salt)
				|| isNullOrEmpty(algo)) {
			throw new NullPointerException(
					"All parameters required to create digest.");
		}
		MessageDigest md =null;
		try{
		 md = MessageDigest.getInstance(algo);
		}catch(NoSuchAlgorithmException e){
			logger.warn("No Such algorithm exception. falling over to default algo for creating digest which is SHA-1", e);
			 md = MessageDigest.getInstance("SHA-1");
		}
		md.reset();
		byte[] keyByte = (key+secret+salt).getBytes(Charset.forName("utf-8")); 
		md.update(keyByte);
		byte[] digest = md.digest();
		return getByteString(digest);
	}
	
	private static String getByteString(byte[] bytes) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = bytes.length;
		for (int i = 0; i < noOfBytes; i++) {
			 oBuffer.append(String.format("%02x", bytes[i]));
		}
		return oBuffer.toString();
	}

	public static String getMetaDataServer(SystemConfigurationManager systemConfigurationManager) {
		
		String ip = "localhost";
		try {
			if (systemConfigurationManager == null) {
				return ip;
			}
			SystemConfiguration metaDataServer = systemConfigurationManager.loadConfigByName("MetaDataServer.IP");
			if (metaDataServer == null || metaDataServer.getValue() == null || metaDataServer.getValue().isEmpty()) {
				return ip;
			}
			ip = metaDataServer.getValue();					
		} catch (Exception e) {			
		}
		return ip;
		
	} //end of method getMetaDataServer
	
	public static void sleep(int sec) {
	    try {
	      Thread.sleep(sec * 1000);
	    }
	    catch(Exception ex) {
	    	logger.error("Error:Sleeping the thread", ex);
	    }
	} //end of method sleep
	
	 public static void readStreamOfProcess(final Process pr){
		 readStreamInThread(pr.getInputStream(),false, pr);
		 readStreamInThread(pr.getErrorStream(),true, pr);
	 }
	 private static boolean isProcessRunning(Process process) {
		    try {
		        process.exitValue();
		        return false;
		    } catch (Exception e) {
		        return true;
		    }
		}
	 private static void readStreamInThread(final InputStream stream, final boolean isErrorStream, final Process pr) {

	        new Thread() {
	            public void run() {
	                BufferedReader br = null;
	                try {
	                    sleep(1);
	                    br = new BufferedReader(new InputStreamReader(stream));
	                    String line = "";
	                    StringTokenizer st = null;
	                    while (true) {
	                        line = br.readLine();
	                        if (line == null) {
	                            break;
	                        }else{
	                        	if(isErrorStream){
	                        		logger.error("WARNING: Error Observed in the Process Error Stream: "+ line);
	                        	}else{
	                        		//logger.error("INFO: INFO: "+ line);
	                        	}
	                        }
	                    }
	                } catch (Exception e) {
	                	logger.error("ERROR: Reading inputstream",e);
	                } finally {
	                    if (br != null) {
	                        try {
	                            br.close();
	                        } catch (Exception e) {
	                        	logger.error("ERROR: During closing BufferedReader:",e);
	                        }
	                    }
	                }
	            }
	        }.start();

	    } // end of method readErrorStream
		
		/**
		 * Checks if the param on request/session passed to this method from Controller/service is in the allowed range configured in the message resource.
		 * 
		 *  e.g.dhcpSettingStatus is passed as paramName then there will be following keys configured in the message resources.
		 *  	dhcpSettingStatus.required=true							(By default it is false i.e. not mandatory)
		 *  	dhcpSettingStatus.allowed.values.flag=false				(By default it will be true if not present)
		 *  	dhcpSettingStatus.allowed.values=true,false
		 *  	dhcpSettingStatus.case-sensitive.flag=true
		 *  	dhcpSettingStatus.type=java.lang.String
		 *      dhcpSettingStatus.pattern=(?!.*[@#\\-_!?^<>~$%&*()+=\\[\\]\\{\\}.,'\"|:;/\\\\]).* 				(If any one of the chars is found in the value then this will return false irrespective of length of string)
		 *  
		 *  
		 *  PATTERN Matching example description
		 *  1. CHECK EQUALITY		
		 *  ((?\=.*\\d)(?\=.*[a-z])(?\=.*[A-Z])(?\=.*[@\#?\!$%&\\-_]).{8,255}) 
		 *			Description
		 *			
		 *			(					# Start of group
		 *			  (?=.*\d)					#   must contains one digit from 0-9
		 *			  (?=.*[a-z])				#   must contains one lowercase characters
		 *			  (?=.*[A-Z])				#   must contains one uppercase characters
		 *			  (?=.*[@\#?\!$%&\\-_])		#   must contains one special symbols in the list "@#?!$%&\-_"
		 *			  .							#   match anything with previous condition checking
		 *			  {8,255}					#   length at least 8 characters and maximum of 255	
		 *			)					# End of group 
		 *		QuestionMark(?) means apply the assertion condition, meaningless by itself, always work with other combination
		 *  
		 *  2. CHECK NEgATION
		 *  (?!.*[@#\\-_!?^<>~$%&*()+=\\[\\]\\{\\}.,'\"|:;/\\\\]).{8,255}
		 *  		Description
		 *  		(
		 *  			?!.*[@#\\-_!?]			# If found any one these then will return false
		 *  			.{8,255}				# length at least 8 characters and maximum of 255	
		 *  		)
		 *  
		 *  
		 *  
		 *  
		 *  By default dhcpSettingStatus.case-sensitive.flag is false and dhcpSettingStatus.type is java.lang.String if it is not configured.
		 *  dhcpSettingStatus.type should be an object having one public constructor taking value as string and needs to be having toString() method giving the same string value passed in the constructor
		 *  If dhcpSettingStatus.allowed.values is not configured then the method will return true.
		 *  Multiple comma separated allowed values can be configured in dhcpSettingStatus.allowed.values 
		 *  
		 *  If the paramNameVal passed is not among the allowed ones then it will throw the IllegalArgumentException
		 *  
		 * @param messageSource
		 * @param paramName - parameter name on request/session or whose value is to be checked from the messageresource. 
		 * @param paramNameVal - actual value passed in the paramName. This values should be among the allowed values
		 * @return true if any of the value passed in arg is null OR true/false depending on the keys configured in the messageResource.
		 */
		public static Response isParamValueAllowed(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final String paramName, final Object paramNameVal){
			final Response r = new Response();
			r.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
			try{
				
				//check the system property flag.ecloud.apply.validation in system_configuration. If it is true then only execute else return OK
				SystemConfiguration validationFlagConfig = systemConfigurationManager.loadConfigByName("flag.ecloud.apply.validation");
				if (validationFlagConfig != null) {
					final String isApplyValidationStr = validationFlagConfig.getValue();
					final boolean isApplyValidation = StringUtils.isEmpty(isApplyValidationStr)?true:isApplyValidationStr.trim().equalsIgnoreCase("true")?true:false;
					if(!isApplyValidation){
						return r;
					}
				}
				final String isReqStr = messageSource.getMessage(paramName+".required", null, "", LocaleContextHolder.getLocale());
				final boolean isRequired = StringUtils.isEmpty(isReqStr)?false:isReqStr.trim().equalsIgnoreCase("true")?true:false;
				final String typeStr = messageSource.getMessage(paramName+".type", null, "",LocaleContextHolder.getLocale());
				final String type = StringUtils.isEmpty(typeStr)?STRING_TYPE : typeStr.trim();
				final boolean isString = type.equals(STRING_TYPE);
				final boolean isDate = type.equals(DATE_TYPE);
				final String patternStr = messageSource.getMessage(paramName+".pattern", null, "", LocaleContextHolder.getLocale());
				
				if(isRequired && ((paramNameVal == null) || (isString && StringUtils.isEmpty(paramNameVal.toString().trim()))) ){
					r.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
					r.setMsg(" Value is required for "+paramName);
					return r;
				}
				if(StringUtils.isEmpty(paramName) || messageSource == null || paramNameVal == null){
					return r;
				}
				
				
				//Handle specific to date check and return
				if(isDate){
					try {
						final SimpleDateFormat format = new SimpleDateFormat(patternStr);
						format.parse(paramNameVal.toString());
					} catch (Exception e) {
						r.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						r.setMsg(" Value "+paramNameVal+" is not matching with the pattern "+patternStr+" for param "+ paramName );
						return r;
					}
					
				}else{
					final String allowedValFlagStr = messageSource.getMessage(paramName+".allowed.values.flag", null, "", LocaleContextHolder.getLocale());
					final boolean isAllowedValsToCheck = StringUtils.isEmpty(allowedValFlagStr)?true:allowedValFlagStr.trim().equalsIgnoreCase("true")?true:false;
					final String allowedValStr = messageSource.getMessage(paramName+".allowed.values", null, "", LocaleContextHolder.getLocale());
					if(isAllowedValsToCheck && StringUtils.isEmpty(allowedValStr)){
						log.error("Allowed values not specified for "+paramName);
						return r;
					}
					final String flagCaseStr = messageSource.getMessage(paramName+".case-sensitive.flag", null, "",LocaleContextHolder.getLocale());
					
					final boolean flagCase = StringUtils.isEmpty(flagCaseStr)?false:flagCaseStr.trim().equalsIgnoreCase("true") || 
									flagCaseStr.trim().equalsIgnoreCase("y") || flagCaseStr.trim().equalsIgnoreCase("yes") ? true : false;
					final Class<?> cls = Class.forName(type);
					if(paramNameVal.getClass() != cls){
						throw new IllegalArgumentException(" class name configured in "+ cls +" but value to passed is of class "+ paramNameVal.getClass());
					}
					if(isAllowedValsToCheck){
						final List<String> allowedVals = Arrays.asList((allowedValStr.split(",")));
						boolean isMatched;
						for(final String val : allowedVals){
							final Object o = cls.getConstructor(String.class).newInstance(val);
							isMatched = flagCase ? String.valueOf(o).trim().equals(String.valueOf(paramNameVal).trim()) : String.valueOf(o).trim().equalsIgnoreCase(String.valueOf(paramNameVal).trim());
							if(isMatched){
								return r;
							}
						}
						r.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						r.setMsg("Actual Value passed for param "+ paramName +" is "+ String.valueOf(paramNameVal)+" .But it is not from the allowed ones: "+ allowedValStr);
						return r;
					}
					//Check if special characters are allowed in the paramNameVal if its type is String
					if(isString){
						if(!StringUtils.isEmpty(patternStr)){
							final Pattern pattern = Pattern.compile(patternStr.trim());
							final Matcher matcher = pattern.matcher(String.valueOf(paramNameVal).trim());
							if(isRequired && StringUtils.isEmpty(String.valueOf(paramNameVal).trim())){
								r.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
								r.setMsg(" Value which is null/empty is not adhering to the paramName "+paramName+"");
								return r;
							}
							if (!matcher.matches()) {
								//Raise flag as input value is not as per pattern specified in the config
								r.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
								r.setMsg(" Value "+paramNameVal+" is not adhering to the configured pattern "+patternStr+" for paramName " +paramName+"");
								return r;
							}else{
								return r;
							}
						}
					}
				}
			}catch(Exception e){
				log.error("*************************Error occured in checking the param value of "+ paramName,e);
				r.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
				r.setMsg("Error occured in checking the param value of "+ paramName);
			}
			return r;
		}
		
		/***
		 * Throws exception directly if values is not matched 
		 * 
		 * @param messageSource
		 * @param systemConfigurationManager
		 * @param paramName
		 * @param paramNameVal
		 * @return
		 * @throws EcloudValidationException
		 */
		public static Response isParamValueAllowedAndThrowException(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final String paramName, final Object paramNameVal) throws EcloudValidationException{
			final Response r = isParamValueAllowed(messageSource, systemConfigurationManager, paramName, paramNameVal);
			if(r==null || r.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()){
				throw new EcloudValidationException(r.getMsg());
			}
			return r;
		}
		
		/**
		 * Take map of parameter name vs value ans pass all params to check in one go
		 * @param messageSource
		 * @param systemConfigurationManager
		 * @param nameValMap
		 * @return
		 */
		public static Response isParamValueAllowed(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final Map<String, Object> nameValMap){
			final Set<String> keys = nameValMap.keySet();
			Response r = null;
			for(final String key : keys){
				r = isParamValueAllowed(messageSource, systemConfigurationManager, key, nameValMap.get(key));
				if(r==null || r.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()){
					return r;
				}
			}
			return r;
		}
		
		public static Response isParamValueAllowedAndThrowException(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final Map<String, Object> nameValMap) throws EcloudValidationException{
			final Response r = isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
			if(r==null || r.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()){
				throw new EcloudValidationException(r.getMsg());
			}
			return r;
		}

}
