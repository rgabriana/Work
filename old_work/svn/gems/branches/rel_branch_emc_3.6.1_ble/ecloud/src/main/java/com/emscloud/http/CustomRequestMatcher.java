package com.emscloud.http;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CustomRequestMatcher implements RequestMatcher {
	
	private final static Logger logger = Logger.getLogger(CustomRequestMatcher.class.getName());
	
	private final String pattern = "/services/org/replicaserver/,/services/org/emtask/,/services/org/upgrades/,/services/org/communicate/,/services/org/communicate/em/cloudsyncstatus/,/services/org/communicate/em/uemsyncstatus/";
	
	private final List<Pattern> list = new ArrayList<Pattern>(); 
	
	public CustomRequestMatcher() { 
        String[] ps = pattern.split(",");
        for(String p : ps) {
        	  Pattern pat = Pattern.compile("^" + p + ".*");
        	  logger.info("pattern added = " + pat.pattern() );
        	  list.add(pat);
        }
    } 

	@Override
	public boolean matches(HttpServletRequest request) {
		String url = request.getRequestURL().toString().replaceFirst(".*/ecloud", "");
		if(url.startsWith("/services/org/")) {
			logger.debug("url = " + url );
			for(Pattern p : list) {
				if(p.matcher(url).matches()) {
					logger.debug("url = " + url + " matched");
					return true;
				}
			}
		}
		return false;
	}

}
