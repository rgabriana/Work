package com.ems.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.ems.model.SystemConfiguration;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.SystemConfigurationManager;
import com.ems.ws.util.Response;


public class CommonUtils {
	private final static Log log = LogFactory.getLog(CommonUtils.class);

	private static final String STRING_TYPE = "java.lang.String";
	private static final String DATE_TYPE = "java.util.Date";
	
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
			
			//check the system property flag.ems.apply.validation in system_configuration. If it is true then only execute else return OK
			SystemConfiguration validationFlagConfig = systemConfigurationManager.loadConfigByName("flag.ems.apply.validation");
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
							r.setMsg(" Value "+paramNameVal+" is not adhering to the configured pattern "+patternStr+"");
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
	 * @throws EmsValidationException
	 */
	public static Response isParamValueAllowedAndThrowException(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final String paramName, final Object paramNameVal) throws EmsValidationException{
		final Response r = isParamValueAllowed(messageSource, systemConfigurationManager, paramName, paramNameVal);
		if(r==null || r.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()){
			throw new EmsValidationException(r.getMsg());
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
	
	public static Response isParamValueAllowedAndThrowException(final MessageSource messageSource, final SystemConfigurationManager systemConfigurationManager, final Map<String, Object> nameValMap) throws EmsValidationException{
		final Response r = isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(r==null || r.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()){
			throw new EmsValidationException(r.getMsg());
		}
		return r;
	}
}
