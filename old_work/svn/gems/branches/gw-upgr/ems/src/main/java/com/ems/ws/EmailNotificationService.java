package com.ems.ws;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.stereotype.Controller;

import com.ems.server.SchedulerManager;
import com.ems.service.EmailNotificationManager;
import com.ems.service.EmailNotificationOneHourSchedulerJob;
import com.ems.service.EmailNotificationSchedulerJob;
import com.ems.service.SystemConfigurationManager;
import com.ems.vo.EmailNotification;
import com.ems.ws.util.Response;

@Controller
@Path("/org/emailnotification")
public class EmailNotificationService {
	
	private static final Logger m_Logger = Logger.getLogger("emailNotificationLogger");
	
	@Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
	
	@Resource(name = "emailNotificationManager")
	EmailNotificationManager emailNotificationManager;
	    
    JobDetail emailNotificationSchedulerJob;
    
    JobDetail emailNotificationOneHourSchedulerJob;
	
	@Path("saveEmailNotificationScheduler")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveEmailNotificationScheduler(EmailNotification emailNotification){
		Response response = new Response();
		
		String cronstatement = "";
		
		if(emailNotification.getEnabled()){
			
			SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
		    SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
		    Date date = null;
			try {
				if(!"".equalsIgnoreCase(emailNotification.getTime())){
					date = parseFormat.parse(emailNotification.getTime());
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    String[] timeStringArray = displayFormat.format(date).split(":");
			
			String timeHH = timeStringArray[0];
			
			String timeMM = timeStringArray[1];
			
			cronstatement = "0" + " " + timeMM + " " + timeHH + " ? * " + emailNotification.getWeeklyRecurrence();
		}
		
		emailNotificationManager.saveEmailNotificationScheduler(emailNotification);
		
		
		String emailNotificationSchedulerJobName = "emailNotificationSchedulerJob";
		String emailNotificationSchedulerTriggerName = "emailNotificationSchedulerJobTrigger";
		//String cronEmailNotificationSchedulerDefault = "0 30 16 ? * MON,TUE,WED,THU,FRI,SAT,SUN";
		
		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(emailNotificationSchedulerJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(emailNotificationSchedulerJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					m_Logger.debug("Failed to delete Quartz job" + emailNotificationSchedulerJobName);
			}
		}catch (Exception e) {
			m_Logger.error(e.getMessage(), e);
		}
		
		if(emailNotification.getEnabled()){
			try {
					
				// create job
				emailNotificationSchedulerJob = newJob(EmailNotificationSchedulerJob.class)
						.withIdentity(
								emailNotificationSchedulerJobName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName()).build();
				// create trigger
				CronTrigger emailNotificationSchedulerJobTrigger = (CronTrigger) newTrigger()
						.withIdentity(
								emailNotificationSchedulerTriggerName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName())
						.withSchedule(
								CronScheduleBuilder.cronSchedule(cronstatement))
						.startNow().build();

				// schedule job
				SchedulerManager.getInstance().getScheduler()
						.scheduleJob(emailNotificationSchedulerJob, emailNotificationSchedulerJobTrigger);
				
			} catch (Exception e) {
				m_Logger.error(e.getMessage(), e);
			}
			
		}
		
		
		String emailNotificationOneHourSchedulerJobName = "emailNotificationOneHourSchedulerJob";
		String emailNotificationOneHourSchedulerTriggerName = "emailNotificationOneHourSchedulerJobTrigger";
		String cronOneHourstatement = "0 0/60 * * * ?";
		
		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(emailNotificationOneHourSchedulerJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(emailNotificationOneHourSchedulerJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					m_Logger.debug("Failed to delete Quartz job" + emailNotificationOneHourSchedulerJobName);
			}
		}catch (Exception e) {
			m_Logger.error(e.getMessage(), e);
		}
		
		if(emailNotification.getEnabled()){
			if(emailNotification.getEnableOneHourNotification() != null){
				if(emailNotification.getEnableOneHourNotification()){
					try {
						
						// create job
						emailNotificationOneHourSchedulerJob = newJob(EmailNotificationOneHourSchedulerJob.class)
								.withIdentity(
										emailNotificationOneHourSchedulerJobName,
										SchedulerManager.getInstance().getScheduler()
												.getSchedulerName()).build();
						// create trigger
						CronTrigger emailNotificationOneHourSchedulerJobTrigger = (CronTrigger) newTrigger()
								.withIdentity(
										emailNotificationOneHourSchedulerTriggerName,
										SchedulerManager.getInstance().getScheduler()
												.getSchedulerName())
								.withSchedule(
										CronScheduleBuilder.cronSchedule(cronOneHourstatement))
								.startNow().build();

						// schedule job
						SchedulerManager.getInstance().getScheduler()
								.scheduleJob(emailNotificationOneHourSchedulerJob, emailNotificationOneHourSchedulerJobTrigger);
						
					} catch (Exception e) {
						m_Logger.error(e.getMessage(), e);
					}
				}
			}
		}
		
		return response;
		
	}

}
