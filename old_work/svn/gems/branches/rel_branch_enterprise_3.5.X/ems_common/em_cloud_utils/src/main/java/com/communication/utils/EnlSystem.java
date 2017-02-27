package com.communication.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class EnlSystem {

	static final Logger logger = Logger.getLogger(EnlSystem.class.getName());
	private final static Map<String, String> DEFAULT_ENV_MAP = new HashMap<String, String>();
	static{
		DEFAULT_ENV_MAP.put("ENL_APP_HOME", "/var/lib/tomcat6");
		DEFAULT_ENV_MAP.put("OPT_ENLIGHTED", "/opt/enLighted");
		DEFAULT_ENV_MAP.put("ENLIGHTED_HOME", "/home/enlighted");
	}
	
	public static String getenv(final String envKey){
		final String  sysval= System.getenv(envKey);
		if(!StringUtils.isEmpty(sysval) && !sysval.equals(envKey)){
			return sysval;
		}
		final String defval = DEFAULT_ENV_MAP.get(envKey);
		logger.debug("Sys variable not exists for :"+envKey+": Returning default value:"+defval+":");
		return defval;
	}
}
