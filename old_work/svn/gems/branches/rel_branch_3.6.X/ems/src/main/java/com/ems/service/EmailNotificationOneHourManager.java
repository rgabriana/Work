package com.ems.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.SystemConfiguration;
import com.ems.types.FacilityType;
import com.ems.vo.EmailNotification;

@Service("emailNotificationOneHourManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmailNotificationOneHourManager {
	
	public static final Logger logger = Logger.getLogger("emailNotificationLogger");
	
	@Resource(name = "emailManager")
   	EmailManager emailManager ;
	
	@Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
	
	@Resource(name = "eventsAndFaultManager")
	private EventsAndFaultManager eventAndFaultManager;
	
	@Autowired
	private MessageSource messageSource;
	
	private boolean isRunning = false;

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	
	public void sendOneHourNotificationEmail() {
		try {
			setRunning(true);
			String receipientEmail = "";
			String emailSubject = "Energy Manager Critcal and Major Events Notification";
			String emailMessage = "Please find the Energy Manager Citical and Major Events Notification report as an attachment.";
			
			SystemConfiguration emailNotificationConfiguraiton = systemConfigurationManager.loadConfigByName("email_notification_configuraiton");
			
			if(emailNotificationConfiguraiton!=null)
			{
				EmailNotification emailNotification = new ObjectMapper().readValue(emailNotificationConfiguraiton.getValue(),EmailNotification.class);
				
				if(emailNotification.getEnabled()){
					
						if(emailNotification.getEnableOneHourNotification() != null){
							if(emailNotification.getEnableOneHourNotification()){
								receipientEmail = emailNotification.getEmailList();
								
								String emailNotificationDataCsv = getEventsAndFaultsDataCSV();
								
								if(!"".equalsIgnoreCase(emailNotificationDataCsv)){
									InputStream emailNotificationDataIS = new ByteArrayInputStream(emailNotificationDataCsv.getBytes());
									EmailManager.EmailDTO dto = new EmailManager.EmailDTO();
									dto.setCommaSeperatedRecipientList(receipientEmail);
									dto.setHtmlMessage(emailMessage);
									dto.setHtmlSubject(emailSubject);
									final EmailManager.EmailDTO.AttachmentInfo attachment = new EmailManager.EmailDTO.AttachmentInfo();
									attachment.setFileName("EM_Notification_Critical_Major.csv");
									attachment.setMimeType("text/csv");
									attachment.setFileInputStream(emailNotificationDataIS);
									dto.getAttachments().add(attachment);
									
									emailManager.doSendEmail(dto);
								}
							}
						}
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally {
			setRunning(false);
		}
	}
	
	public String getEventsAndFaultsDataCSV(){
		
		StringBuffer output = new StringBuffer("");
		
		String orderWay = "desc";
		String orderBy  = "eventTime";
		
		//1%23%23%23%23%23Critical%2CMinor%2CInfo%23Bad%20Profile%2CDiscovery%2CDR%20Condition%2CERC%20Commissioning
		//%2CERC%20Discovery%2CFixture%20Configuration%20Upload%23END
		
		/*
		 * (List of objects in order active, search string, group, start date,
		 * end date, severity, event type, org node type, org node id)
		 */
		List<Object> filter = new ArrayList<Object>();
		
		filter.add("1");
		
		filter.add(null);
		
		filter.add(null);
		
		filter.add(null);
		
		filter.add(null);
		
		filter.add(Arrays.asList("Critical", "Major"));
		
		filter.add(null);
		
		filter.add(FacilityType.COMPANY);
		
		Long companyId = 1L;
		
		filter.add(companyId);
		
		
		List<Object> eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
				orderBy, orderWay, filter, "Admin", 0, -1);


		output.append(messageSource.getMessage("eventsAndFault.time", null,
				null)
				+ ","
				+ messageSource.getMessage("eventsAndFault.location", null,
						null)
				+ ","
				+ messageSource.getMessage("eventsAndFault.eventType", null,
						null)
				+ ","
				+ messageSource.getMessage("eventsAndFault.severity", null,
						null)
				+ ","
				+ messageSource.getMessage("eventsAndFault.description", null,
						null)
				+ ","
				+ messageSource.getMessage("eventsAndFault.resolved", null,
						null));
		
		int eventsCount = 0;

		for (int i = 1; i < eventsAndFaults.size(); i++) {
			Object[] each = (Object[]) eventsAndFaults.get(i);
			String desc = (String) each[4];
			desc = (desc!=null && desc!="")? desc.replace(",", ";") : desc;
			String location = (String) each[8];
			location = (location!=null && location!="") ? location.replace(",", "-") :location;
			output.append("\r\n");
			output.append((String) each[1]
					+ ","
					+ (each[7] != null ? location + "->" + (String) each[12] : "")
					+ ","
					+ each[3]
					+ ","
					+ each[2]
					+ ","
					+ desc
					+ ","
					+ ((Boolean) each[5] ? messageSource.getMessage("lov.no",
							null, null) : messageSource.getMessage("lov.yes",
							null, null)));
			eventsCount++;
		}
		
		if(eventsCount > 0){
			return output.toString();
		}else{
			output = new StringBuffer("");
			return output.toString();
		}
	}
	
	public void saveEmailNotificationScheduler(EmailNotification emailNotification){
		
		String emailNotificationJsonString = "";
		SystemConfiguration emailNotificationConfiguraiton = systemConfigurationManager.loadConfigByName("email_notification_configuraiton");
		if(emailNotificationConfiguraiton!=null)
		{
			try {
				ObjectMapper mapper = new ObjectMapper();
				emailNotificationJsonString = mapper.writeValueAsString(emailNotification);
				emailNotificationConfiguraiton.setValue(emailNotificationJsonString);
				systemConfigurationManager.save(emailNotificationConfiguraiton);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
