package com.ems.service;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Title24;
import com.ems.util.Constants;

@Service("title24Manager")
@Transactional(propagation = Propagation.REQUIRED)
public class Title24Manager {

	private static Logger syslog = Logger.getLogger("SysLog");

	@Autowired
	private MessageSource messageSource;
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	
	public boolean isTitle24Compliant(){
		final Title24 title24 = loadTitle24Details();
		if (title24 != null && title24.getCompliance() != null){
			return Boolean.parseBoolean(title24.getCompliance().getFlag());
		}
		return false;
	}
	
	public Title24 loadTitle24Details(){
		try {
			final Title24 title24  = (Title24)systemConfigurationManager.loadJSONObjectFromSysConfigByName(Constants.TITLE_24_JSON_KEY,Title24.class);
			return title24;
		} catch (Exception e) {
			syslog.error("Issue in loading JSON object from SysCOnfig against key: "+Constants.TITLE_24_JSON_KEY);
		}
		return null;
	}
	
	public void saveTitle24Details(final Title24 title24) throws JsonGenerationException, JsonMappingException, IOException{
		systemConfigurationManager.updateSysConfigJSON(Constants.TITLE_24_JSON_KEY, title24);
	}
}
