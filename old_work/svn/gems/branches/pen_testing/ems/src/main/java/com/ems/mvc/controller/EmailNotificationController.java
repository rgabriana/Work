package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ems.model.EventType;
import com.ems.model.EventsAndFault;
import com.ems.model.SystemConfiguration;
import com.ems.service.MetaDataManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.util.Constants;
import com.ems.vo.EmailNotification;



@Controller
@RequestMapping("/emailNotification")
public class EmailNotificationController {
	
	@Resource(name = "metaDataManager")
    private MetaDataManager metaDataManager;
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	
	
	@PreAuthorize("hasAnyRole('Admin')")
	@RequestMapping(value = "/management.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String manageEmailNotification(Model model)
	{
		List<EventType> eventList = metaDataManager.getEventTypes();
   	 
   	 	SystemConfiguration bulbConfigurationEnableConfig = systemConfigurationManager.loadConfigByName("bulbconfiguration.enable");
        if (bulbConfigurationEnableConfig != null && "false".equalsIgnoreCase(bulbConfigurationEnableConfig.getValue())) {
       	 if (eventList != null && eventList.size() > 0) {
    			for (Iterator<EventType> iterator = eventList.iterator(); iterator.hasNext();) {
    				EventType bulbCOnfigurationObj = (EventType) iterator.next();
    				if(bulbCOnfigurationObj.getType().equalsIgnoreCase(EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR))
    				{
    					eventList.remove(bulbCOnfigurationObj);
    					break;
    				}
    			}
    		}
        }
        model.addAttribute("events", eventList);
		List<String> eventSeverities = new ArrayList<String>();
		eventSeverities.add(Constants.SEVERITY_CRITICAL);
		eventSeverities.add(Constants.SEVERITY_MAJOR);
		eventSeverities.add(Constants.SEVERITY_MINOR);
		eventSeverities.add(Constants.SEVERITY_WARNING);
		eventSeverities.add(Constants.SEVERITY_INFORMATIONAL);
		model.addAttribute("severities", eventSeverities);
		
		SystemConfiguration emailNotificationConfiguraiton = systemConfigurationManager.loadConfigByName("email_notification_configuraiton");
		
		if(emailNotificationConfiguraiton!=null)
		{
			EmailNotification emailNotification = null;
			try {
				if(!"".equals(emailNotificationConfiguraiton.getValue())){
					emailNotification = new ObjectMapper().readValue(emailNotificationConfiguraiton.getValue(),EmailNotification.class);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(emailNotification != null){
			
			if(emailNotification.getEnabled()) {
				
				model.addAttribute( "emailNotificationSchedulerEnable", "true");
				
				if(!"".equalsIgnoreCase(emailNotification.getEmailList())){
					model.addAttribute( "emailNotificationSchedulerEmail", emailNotification.getEmailList());
				}
			
				if(!"".equals(emailNotification.getTime())) {
					model.addAttribute( "emailNotificationReportTime", emailNotification.getTime());
				}else{
					model.addAttribute( "emailNotificationReportTime", "");
				}
				
				if(!"".equals(emailNotification.getWeeklyRecurrence())) {
					model.addAttribute( "emailNotificationRecurrence", emailNotification.getWeeklyRecurrence());
				}else{
					model.addAttribute( "emailNotificationRecurrence", "");
				}
				
				if(!"".equals(emailNotification.getSeverityList())) {
					model.addAttribute( "emailNotificationSeverityList", emailNotification.getSeverityList());
				}else{
					model.addAttribute( "emailNotificationSeverityList", "");
				}
				
				if(!"".equals(emailNotification.getEventTypeList())) {
					model.addAttribute( "emailNotificationEventTypeList", emailNotification.getEventTypeList());
				}else{
					model.addAttribute( "emailNotificationEventTypeList", "");
				}
				
				if(emailNotification.getEnableOneHourNotification() != null){
					if(emailNotification.getEnableOneHourNotification()) {
						model.addAttribute("enableOneHourNotification", "true");
					}else{
						model.addAttribute("enableOneHourNotification", "false");
					}
				}else{
					model.addAttribute("enableOneHourNotification", "false");
				}
							
			}else{
				model.addAttribute( "emailNotificationSchedulerEnable", "false");
			}
			
			}
		}
		
		return "emailNotification/management";
	}
	
}