package com.ems.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GemsGroupDao;
import com.ems.dao.MotionBitsSchedulerDao;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.MotionBitsScheduler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.SchedulerManager;
import com.ems.types.UserAuditActionType;

@Service("motionBitsConfigManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionBitsConfigManager {
	
	@Resource
	private MotionBitsSchedulerDao motionBitsSchedulerDao;
    @Resource
    private GemsGroupDao gemsGroupDao;
    @Resource
    private FixtureManager fixtureManager;
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	GemsGroupManager	gemsGroupManager;

	static String startJobSuffix = "StartJob";
	static String startTriggerSuffix = "StartTrigger";
	static String endJobSuffix = "EndJob";
	static String endTriggerSuffix = "EndTrigger";
	
  
    public MotionBitsScheduler saveMotionBitsSchedule(Long gid, MotionBitsScheduler mbSchedule) {
    	
    	if(gid != 0)
    	{
	        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
	        mbSchedule.setMotionBitGroup(group);
    	}        
		return (MotionBitsScheduler)motionBitsSchedulerDao.saveObject(mbSchedule);
    	
    
	}
	
	public MotionBitsScheduler loadMotionBitsScheduleById(Long id) {
		return (MotionBitsScheduler)motionBitsSchedulerDao.loadMotionBitsScheduleById(id);
	}
	
	public boolean deleteMotionBitsScheduleById(Long id) {

		boolean ret = deleteMotionBitsSchedulerJob(id);
		
		motionBitsSchedulerDao.removeObject(MotionBitsScheduler.class, id);
		
		return ret;
	}
	
	public boolean stopMotionBitsScheduleById(Long id) {
		
		try {
			MotionBitsSchedulerJob stopTask = new MotionBitsSchedulerJob(id, 0L);
			stopTask.execute(null);
			
			return true;
		}
		catch(JobExecutionException ex)
		{
			ex.printStackTrace();
			
			return false;
		}
	}

	public List<MotionBitsScheduler> loadAllMotionBitsSchedules() {
		return motionBitsSchedulerDao.loadAllMotionBitsSchedules();
	}
	
	public boolean deleteMotionBitsSchedulerJob(Long id)
	{
		MotionBitsScheduler schedule = loadMotionBitsScheduleById(id);
        String startJobName = schedule.getName() + startJobSuffix;
        String endJobName = schedule.getName() + endJobSuffix;
        
        try {
		
			if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(startJobName, schedule.getName())) == false)
				System.out.println("Failed to delete Quartz job" + startJobName);
	
			// Run the stopMotionBit job and then delete it
			SchedulerManager.getInstance().getScheduler().triggerJob(new JobKey(endJobName, schedule.getName()));
			if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(endJobName, schedule.getName())) == false)
				System.out.println("Failed to delete Quartz job" + endJobName);
			
			return true;
        }
        catch(SchedulerException ex)
        {
        	ex.printStackTrace();
        	return false;
        }
	}
	
	
	public String validateSchedule(String name, String startTime, String endTime, List<Fixture> fixtures, Long id) {
		String status = "";
		String fixtureNames = "";
		
		if(fixtures.isEmpty())
			return status;
		
		for (Fixture fixture : fixtures) {
			List<MotionBitsScheduler> listSchedules = loadAllMotionBitsSchedules();
			
			if(listSchedules == null || listSchedules.isEmpty())
				return status;
			
			for(MotionBitsScheduler schedule:listSchedules) {
				
				if(id.longValue() != 0L && id.longValue() == schedule.getId().longValue())
					continue;
						
				if(name.equals(schedule.getName()) == true)
				{
					status = "Another configuration with the same name already exists. Please enter a different name";
					return status;
				}
				
				List<GemsGroupFixture> listGroupFixture = gemsGroupDao.getGemsGroupFixtureByGroup(schedule.getMotionBitGroup().getId());
				
				if(listGroupFixture == null || listGroupFixture.isEmpty())
					continue;
				
				for(GemsGroupFixture groupFixture:listGroupFixture)
				{
					if(groupFixture.getFixture().getId().longValue() == fixture.getId().longValue())
					{
						String startHour = startTime.substring(0, startTime.indexOf(':'));
						String startMinute = startTime.substring(startTime.indexOf(':')+1);
						
				        Calendar startCal = Calendar.getInstance();
				        startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
				        startCal.set(Calendar.MINUTE, Integer.parseInt(startMinute));

				        String endHour = endTime.substring(0, endTime.indexOf(':'));
						String endMinute = endTime.substring(endTime.indexOf(':')+1);
						
				        Calendar endCal = Calendar.getInstance();
				        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
				        endCal.set(Calendar.MINUTE, Integer.parseInt(endMinute));
				        
						String schStartHour = schedule.getCaptureStart().substring(0, schedule.getCaptureStart().indexOf(':'));
						String schStartMinute = schedule.getCaptureStart().substring(schedule.getCaptureStart().indexOf(':')+1);
						
				        Calendar schStartCal = Calendar.getInstance();
				        schStartCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schStartHour));
				        schStartCal.set(Calendar.MINUTE, Integer.parseInt(schStartMinute));

				        String schEndHour = schedule.getCaptureEnd().substring(0, schedule.getCaptureEnd().indexOf(':'));
						String schEndMinute = schedule.getCaptureEnd().substring(schedule.getCaptureEnd().indexOf(':')+1);
						
				        Calendar schEndCal = Calendar.getInstance();
				        schEndCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schEndHour));
				        schEndCal.set(Calendar.MINUTE, Integer.parseInt(schEndMinute));			
				        
				        if((schStartCal.getTimeInMillis() <= startCal.getTimeInMillis() && startCal.getTimeInMillis() <= schEndCal.getTimeInMillis()) || 
				        		(schStartCal.getTimeInMillis() <= endCal.getTimeInMillis() && endCal.getTimeInMillis() <= schEndCal.getTimeInMillis()))
				        {
				        	fixtureNames += fixture.getFixtureName();
				        	fixtureNames += ",";
				        }
					}
				}
			}
		}
		
		if(fixtureNames != "")
			status = "Failed to create/save the configuration: Fixtures " + fixtureNames + " are part of another configuration for the same time interval.";
		return status;
	}
	
	public boolean addMotionBitsSchedulerJob(MotionBitsScheduler schedule)
	{
		try
		{
			String startTime = schedule.getCaptureStart();
			
			String startHour = startTime.substring(0, startTime.indexOf(':'));
			String startMinute = startTime.substring(startTime.indexOf(':')+1);
			Boolean isStartDateIncremented = false ;
			
			Calendar startCal = Calendar.getInstance();
	        
	        // If hour to start is less than the current hour then start tomorrow
	        if((Integer.parseInt(startHour) < startCal.get(Calendar.HOUR_OF_DAY)) || ((Integer.parseInt(startHour) == startCal.get(Calendar.HOUR_OF_DAY)) && Integer.parseInt(startMinute) < startCal.get(Calendar.MINUTE)))
	        	{
	        		startCal.set(Calendar.DAY_OF_MONTH, startCal.get(Calendar.DAY_OF_MONTH)+1);
	        		isStartDateIncremented = true;
	        	}
	        
	        startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
	        startCal.set(Calendar.MINUTE, Integer.parseInt(startMinute));
	
	        String startJobName = schedule.getName() + startJobSuffix;
	        String startTriggerName = schedule.getName() + startTriggerSuffix;
	        
	        // Create quartz job
	        JobDetail startJob = newJob(MotionBitsSchedulerJob.class)
	                .withIdentity(startJobName, schedule.getName())
	                .usingJobData("start", 1L)
	                .usingJobData("id", schedule.getId())
	                .build();
	        
	        // Create Quartz trigger
	        SimpleTrigger startTrigger = (SimpleTrigger) newTrigger() 
	                .withIdentity(startTriggerName, schedule.getName())
	                .startAt(startCal.getTime())
	                .withSchedule(simpleSchedule()
	                		.withIntervalInHours(24)
	                		.repeatForever())
	                .build();
	
	        Date startDate = SchedulerManager.getInstance().getScheduler().scheduleJob(startJob, startTrigger);
	        
			String endTime = schedule.getCaptureEnd();
	
			String endHour = endTime.substring(0, endTime.indexOf(':'));
			String endMinute = endTime.substring(endTime.indexOf(':')+1);
			
	        Calendar endCal = Calendar.getInstance();
	        
	       
	        if(isStartDateIncremented)
	        { 
	        	// if start date is incremented we need to increment the date of the end date to by 1 
	        	endCal.set(Calendar.DAY_OF_MONTH, endCal.get(Calendar.DAY_OF_MONTH)+1);
	        } else if((Integer.parseInt(endHour) < endCal.get(Calendar.HOUR_OF_DAY)) || ((Integer.parseInt(endHour) == endCal.get(Calendar.HOUR_OF_DAY)) && Integer.parseInt(endMinute) < endCal.get(Calendar.MINUTE)))
	        {	 // If hour to start is less than the current hour then start tomorrow
	        	endCal.set(Calendar.DAY_OF_MONTH, endCal.get(Calendar.DAY_OF_MONTH)+1);       
	        } 
	        
	        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
	        endCal.set(Calendar.MINUTE, Integer.parseInt(endMinute));
			
	        String endJobName = schedule.getName() + endJobSuffix;
	        String endTriggerName = schedule.getName() + endTriggerSuffix;
	        
	        // Create quartz job
	        JobDetail endJob = newJob(MotionBitsSchedulerJob.class)
	                .withIdentity(endJobName, schedule.getName())
	                .usingJobData("id", schedule.getId())
	                .usingJobData("start", 0L)
	                .build();
	
	        // Create Quartz trigger
	        SimpleTrigger endTrigger = (SimpleTrigger) newTrigger() 
	                .withIdentity(endTriggerName, schedule.getName())
	                .startAt(endCal.getTime())
	                .withSchedule(simpleSchedule()
	                		.withIntervalInHours(24)
	                		.repeatForever())
	                .build();
	
	        Date endDate = SchedulerManager.getInstance().getScheduler().scheduleJob(endJob, endTrigger);
	        
	        userAuditLoggerUtil.log("Scheduled motion bits configuration tasks for: " + schedule.getName(), UserAuditActionType.Motion_Bits_update.getName());
	        
	        return true;
		}
		catch(SchedulerException ex)
		{
			ex.printStackTrace();
			
			return false;
		}
	}

	public void createMotionBitsSchedulerJobs()
	{
		List<MotionBitsScheduler> listSchedules = loadAllMotionBitsSchedules();
        if (listSchedules != null) {
    		for(MotionBitsScheduler schedule: listSchedules) {
    			addMotionBitsSchedulerJob(schedule);
    		}
        }
	}

}
