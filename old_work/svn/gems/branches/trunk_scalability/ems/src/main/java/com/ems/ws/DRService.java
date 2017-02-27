/**
 * 
 */
package com.ems.ws;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Controller;

import com.ems.model.DRRecord;
import com.ems.model.DRTarget;
import com.ems.model.GroupECRecord;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.SchedulerManager;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.ADRSchedulerJob;
import com.ems.service.DRTargetManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.PricingManager;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.DRStatus;
import com.ems.vo.model.DRTargetList;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/dr")
public class DRService {
	private static Logger m_Logger = Logger.getLogger("DemandResponse");
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "pricingManager")
	private PricingManager pricingManager;
	@Resource(name = "drTargetManager")
	private DRTargetManager drTargetManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;

	public DRService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Fetches the power used per Group with the range of time
	 * 
	 * @param fdate
	 *            older date (format: yyyyMMddHHmmss)
	 * @param tdate
	 *            new date (format: yyyyMMddHHmmss)
	 * @return GroupECRecord list
	 */
	@Path("group/ec/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GroupECRecord> loadGroupEnergyConsumptionBetweenPeriods(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		try {
			oFDate = inputFormat.parse(fdate);
			oTDate = inputFormat.parse(tdate);
			// FIXME: based on respective period.
			return energyConsumptionManager.loadGroupEnergyConsumptionBetweenPeriods(oFDate, oTDate);
		} catch (ParseException pe) {
			m_Logger.warn(pe.getMessage());
		}
		return null;
	}

	/**
	 * Returns the DR reactivity value for each group in the system
	 * 
	 * @return GroupECRecord list
	 */
	@Path("group/sensitivity")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GroupECRecord> getGroupDRSensitivity() {
		return groupManager.getDRSensitivityRecords();
	}

	/**
	 * Fetches the current pricing in the day
	 * 
	 * @return Response object with msg containing the pricing information
	 */
	@Path("pricing/current")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCurrentPricing() {
		Response response = new Response();
		// Msg used to send the pricing for the current period.
		response.setMsg(String.valueOf(pricingManager.getPrice(new Date())));
		return response;
	}

	/**
	 * Dims the fixtures for specified time in the respective group, with the
	 * computed percentage based on the weight (derived from formula based on dr
	 * sensitivity); TODO: Optimize the command send
	 * 
	 * @param groupId
	 *            Group id
	 * @param percentage
	 *            (-100 | 0 | 100)
	 * @param time
	 *            specified time in minutes
	 * @return response status
	 */
	@Path("op/dim/group/{groupid}/{percentage}/{time}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String dimFixture(String data, @PathParam("groupid") Integer groupId,
			@PathParam("percentage") String percentage,
			@PathParam("time") String time) {
		groupManager.dimFixtures(groupId, -1 * Integer.valueOf(percentage).intValue(),
				Integer.valueOf(time));
		//TODO uncomment following line once audit logger code is fixed. 
		userAuditLoggerUtil.log("Dimmed Fixture, Group Id : " + groupId + " %age: " + percentage,UserAuditActionType.DR.getName());
		return "S";
	}

	/**
	 * Updates the drTarget
	 * 
	 * @param drTarget
	 * @return Response Status
	 */
	@Path("update")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateDR(DRTarget dr) {
		Response oStatus = new Response();
		DRTarget dr1 = drTargetManager.getDRTargetById(dr.getId());
		dr1.setDuration(dr.getDuration()*60);
		dr1.setPricing(dr.getPricing());
		String currentEnabled = dr1.getEnabled();
		if ("Yes".equals(currentEnabled)) {
			oStatus.setMsg("R");
		} else if (dr.getEnabled().equals("No")) {
			drTargetManager.saveOrUpdateDRTarget(dr1);
			oStatus.setMsg("S");
		} else if (dr.getEnabled().equals("Yes")) {
			drTargetManager.saveOrUpdateDRTarget(dr1);
		}
		oStatus.setStatus(1);
		userAuditLoggerUtil.log("DR Target - "+dr1.getPriceLevel()+" (ID = "+dr1.getId()+") - Updated",UserAuditActionType.DR.getName());
		return oStatus;
	}

	/**
	 * cancels dr event
	 * 
	 * @return response status
	 */
	@Path("cancel")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelDR(DRTarget dr) {
		Response oStatus = new Response();
		DRTarget drTarget = drTargetManager.getDRTargetById(dr.getId());
		if(drTarget!=null) {
		
			boolean isEnabled = (DRTarget.ENABLED).equals(drTarget.getEnabled());
			drTarget.setEnabled(DRTarget.DISABLED);
			drTargetManager.saveOrUpdateDRTarget(drTarget);
			oStatus.setStatus(1);
			if(isEnabled) {
				drTarget.setDrStatus(DRStatusType.Cancelled.getName());
				drTargetManager.cancelDR();
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);
				String message = "DR Cancelled at "+ new Date(System.currentTimeMillis());
				userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
				oStatus.setMsg("S");
			}else
			{
				//IF DR is already finished or cancelled... UI will show message saying that "DR already finished/cancelled. Please refresh the page."
				int drTimeRem = -1;
				drTimeRem = DeviceServiceImpl.getInstance().getDRTimeRemaining();
				if(drTimeRem==0)
				{
					oStatus.setMsg("E");
				}
			}
		}else {
			oStatus.setMsg("U");
		}
		return oStatus;
	}
	
	/**
	 * clears dr events from database
	 * 
	 * @return response status
	 */
	@Path("cleandrevents")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cleanDrEvents(){
		Response oStatus = new Response();
		String filePath = "/opt/enLighted/adr/adr_cleanup.sh";
		File drCleanUpFile = new File(filePath);
		
        if(drCleanUpFile.exists()) {
        	try {
        		Runtime rt = Runtime.getRuntime();
        		Process proc;
				proc = rt.exec(new String[]{"/bin/bash", filePath});
				try {
					if (proc.waitFor() == 0){
						oStatus.setStatus(1);
						oStatus.setMsg("S");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
						oStatus.setStatus(0);
						oStatus.setMsg("F");
					e.printStackTrace();
				}
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				oStatus.setStatus(0);
				oStatus.setMsg("F");
				e.printStackTrace();
			}
        }
        return oStatus;
	}
	
		
	/**
	 * Fetches the avg power and recent power used by fixture along with the fixture details.
	 * 
	 * @param fdate
	 *            older date (format: yyyyMMddHHmmss)
	 * @param tdate
	 *            new date (format: yyyyMMddHHmmss)
	 * @return DRRecord list
	 */
	@Path("fixture/record/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DRRecord> getFixtureECProfileRecords(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		List<DRRecord> out = new ArrayList<DRRecord>();
		try {
			oFDate = inputFormat.parse(fdate);
			oTDate = inputFormat.parse(tdate);
			Map<Integer, Object[]> map1 = fixtureManager.getRecentFixtureDetails();
			Map<Integer, Double> map2 = energyConsumptionManager.getFixtureECOverPeriod(oFDate, oTDate);
			Map<Integer, Double> map3 = energyConsumptionManager.getRecentFixtureEC();
			if (map1 != null && !map1.isEmpty()) {
				for(Integer i: map1.keySet()) {
					Object[] object = map1.get(i);
					DRRecord drrecord = new DRRecord(((BigInteger)object[0]).intValue(),
							object[1] == null ? new Integer(0) : (Integer)object[1], 
							object[2] == null ? "Auto" : object[2].toString(), 
							((Short)object[3]).intValue(),
							((BigInteger)object[4]).intValue(),
							((BigInteger)object[5]).intValue(),
							(map2.get(i) == null ? 0D : map2.get(i)),
							(map3.get(i) == null ? 0D : map3.get(i)));
					out.add(drrecord);
				}
			}
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		return out;
	}
	
	/**
	 * Start a DR with level (High/Medium/Low, duration and optional price)
	 * 
	 * @return response status
	 */
	@Path("start/level/{level}/duration/{duration}/type/{type}/starttime/{starttime}/drindentifier/{dridentifier}/status/{status}/{price:.*}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response startDRWithLevel(@PathParam("level") String level,
			@PathParam("duration") Integer duration,
			@PathParam("type") String type,
			@PathParam("starttime") String starttime,
			@PathParam("dridentifier") String dridentifier,
			@PathParam("status") String status,
			@PathParam("price") String price) {
		Response oStatus = new Response();
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar oCalendar = Calendar.getInstance();
        DrLevel drlevel;
        if(duration.intValue()>0 && ArgumentUtils.isInEnum(level.toUpperCase(),DrLevel.class) && ArgumentUtils.isInEnum(type.toUpperCase(),DRType.class))
        {
    	   drlevel = DrLevel.valueOf(level.toUpperCase());
        }else
        {
        	oStatus.setMsg("U");
        	oStatus.setStatus(1);
    		return oStatus;
        }
        Date drStartTime =null;
        try {
        	if("0".equals(starttime)) {
        		drStartTime = new Date();
        	}
        	else {
        		drStartTime = inputFormat.parse(starttime);
        	}
        } catch (ParseException pe) {
        	drStartTime = new Date(System.currentTimeMillis());
        }
        DRTarget drTarget = drTargetManager.getDRTargetByPriceLevelAndDrType(drlevel, type);
        
    	DRStatus drStatus =drTargetManager.getCurrentDRProcessRunning();
		try {
	        if(type.toUpperCase().equals(DRType.MANUAL.getName().toUpperCase()))
	        {
	        	if(drStatus.getStatus().equals(true))
				{
					oStatus.setMsg("E");
					oStatus.setStatus(1);
					return oStatus;
				}
		        oCalendar.setTime(drStartTime);
		       //Update Existing ManualDR Event in the DB and FIRE StartDR()
				if(drTarget!=null)
				{
					drTarget.setDrStatus(DRStatusType.Active.getName());
					drTarget.setStartTime(drStartTime);
					drTarget.setDuration(duration*60);
					//drTarget.setEnabled(DRTarget.ENABLED);
					if(!price.equals(""))
					{
						drTarget.setPricing(Double.parseDouble(price));
					}
					drTargetManager.saveOrUpdateDRTarget(drTarget);
				}
		   		
				String endJobName = "ManualDREndJob" + drlevel.getName();
		   		String endTriggerName = "ManualDREndTr" + drlevel.getName();
		   		
		   		Date endDate = new Date();
				endDate.setTime(drTarget.getStartTime().getTime() + drTarget.getDuration() * 1000 + 2000);

				// Delete the older Quartz job and create a new one
	        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(endJobName))) {
	        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(endJobName)) == false)
	        			System.out.println("Failed to delete Quartz job" + endJobName);
	        	}

	        	// Create quartz job
		        JobDetail endJob = newJob(ADRSchedulerJob.class)
		                .withIdentity(endJobName)
		                .build();
		        
		        // Create Quartz trigger
		        SimpleTrigger endTrigger = (SimpleTrigger) newTrigger() 
		                .withIdentity(endTriggerName)
		                .startAt(endDate)
		                .withSchedule(simpleSchedule()
		                		.withRepeatCount(0))
		                .build();
		        SchedulerManager.getInstance().getScheduler().scheduleJob(endJob, endTrigger);
	        }
	        
			DeviceServiceImpl deviceImpl = DeviceServiceImpl.getInstance();
			//Converting Duration into Seconds as web service consumes duration in minutes
			deviceImpl.sendUTCTimeOnAllGateways(true);
			
		} catch (Exception e) {
			drTarget.setEnabled(DRTarget.DISABLED);
			drTarget.setDrStatus(DRStatusType.Cancelled.getName());
			e.printStackTrace();
			oStatus.setMsg("Failed to Start DR Service due to some internal error");
        	oStatus.setStatus(-1);
    		return oStatus;
		}
		String message = "DR Started. Initiate type:  " + type + ", Level: " + drlevel + ", Duration:" +duration +" mins "+ ", StartTime :" + drStartTime;
		userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
		oStatus.setStatus(1);
		oStatus.setMsg("S");
		return oStatus;
	}
	
	/**
	 * Updates the drTarget
	 * 
	 * @param drTarget
	 * @return Response Status
	 */
	@Path("updateADRTargets")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DRTarget> updateADRTargets(List<DRTarget> drtargets) {
		List<DRTarget> retList = new ArrayList<DRTarget>();
		List<DRTarget> cancelledList = drTargetManager.getAllScheduledADRTargets();

    	DateFormat dateFormat = DateFormat.getDateTimeInstance( 
   			 DateFormat.LONG, DateFormat.LONG );
    	Random random = new Random();
	   	TimeZone zone = dateFormat.getTimeZone();
	   	Date d = new Date();
	   	Integer offset = zone.getOffset(d.getTime());

	   	try 
	   	{
		   	for(DRTarget dr: drtargets) {
	
		   		// Convert the date to local
		   		Date localDate = new Date();
		   		localDate.setTime(dr.getStartTime().getTime() + offset);
		   		dr.setStartTime(localDate);
	
		   		String startJobName = "ADRStartJob" + dr.getDrIdentifier() + "_" + dr.getUid().toString();
		   		String startTriggerName = "ADRStartTr" + dr.getDrIdentifier() + "_" + dr.getUid().toString();
		   		String endJobName = "ADREndJob" + dr.getDrIdentifier() + "_" + dr.getUid().toString();
		   		String endTriggerName = "ADREndTr" + dr.getDrIdentifier() + "_" + dr.getUid().toString();
		   		
				DRTarget current = drTargetManager.getDRTargetByDRIdentifierAndUid(dr.getDrIdentifier(), dr.getUid());
				if(current != null)
				{
					if(current.getOptIn() == false)
					{
						retList.add(current);
						continue;
					}
					
					Date prevStartTime = current.getStartTime();
					Long prevStartAfter = current.getStartAfter();
					
					// update the current target
					current.setDuration(dr.getDuration());
					current.setPriceLevel(dr.getPriceLevel().toUpperCase());
					current.setStartTime(dr.getStartTime());
					current.setPricing(dr.getPricing());
					//Priority 0 means lowest priority thread hence setting Max value so that I will be in last while getting current Active DR Event
					if(dr.getPriority() == null || dr.getPriority()==0)
					{
						current.setPriority(Integer.MAX_VALUE);
					}else
					{
						current.setPriority(dr.getPriority());
					}
					if(!current.getDrStatus().equals(DRStatusType.Completed.getName()) && !current.getDrStatus().equals(DRStatusType.Active.getName())) {
						current.setDrStatus(dr.getDrStatus().substring(0, 1).toUpperCase() +
								dr.getDrStatus().substring(1).toLowerCase());
					}
					current.setUid(dr.getUid());
					if(current.getStartAfter().compareTo(dr.getStartAfter()) != 0) {
						current.setStartAfter(dr.getStartAfter());
						if(dr.getStartAfter().compareTo(0L) == 0) {
							current.setJitter(0L);
						}
						else {
							if(current.getUid() != 0) {
								DRTarget firstTarget = drTargetManager.getFirstDRTargetByIdentifier(dr.getDrIdentifier());
								current.setJitter(firstTarget.getJitter());
							}
							else {
								current.setJitter((random.nextInt(dr.getStartAfter().intValue())) * 1000L);
							}
						}
					}
					current.setTargetReduction(getPercentRedByDrLevel(dr.getPriceLevel()));
					
					drTargetManager.saveOrUpdateDRTarget(current);
					
					if(prevStartTime.compareTo(dr.getStartTime()) != 0 || prevStartAfter.compareTo(dr.getStartAfter()) != 0)
					{
						Date endDate = new Date();
						endDate.setTime(current.getStartTime().getTime() + current.getDuration() * 1000 + current.getJitter() + 2000);
						
						// Delete the older Quartz job and create a new one
			        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(startJobName, current.getDrIdentifier()))) {
			        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(startJobName, current.getDrIdentifier())) == false)
			        			System.out.println("Failed to delete Quartz job" + startJobName);
			        	}

			        	// Create quartz job
				        JobDetail startJob = newJob(ADRSchedulerJob.class)
				                .withIdentity(startJobName, current.getDrIdentifier())
				                .build();
				        
				        // Create Quartz trigger
				        SimpleTrigger startTrigger = (SimpleTrigger) newTrigger() 
				                .withIdentity(startTriggerName, current.getDrIdentifier())
				                .startAt(new Date(current.getStartTime().getTime() + current.getJitter()))
				                .withSchedule(simpleSchedule()
				                		.withRepeatCount(0))
				                .build();
				
				        SchedulerManager.getInstance().getScheduler().scheduleJob(startJob, startTrigger);
				        DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);
				        
						// Delete the older Quartz job and create a new one
			        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(endJobName, current.getDrIdentifier()))) {
			        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(endJobName, current.getDrIdentifier())) == false)
			        			System.out.println("Failed to delete Quartz job" + endJobName);
			        	}

			        	// Create quartz job
				        JobDetail endJob = newJob(ADRSchedulerJob.class)
				                .withIdentity(endJobName, current.getDrIdentifier())
				                .build();
				        
				        // Create Quartz trigger
				        SimpleTrigger endTrigger = (SimpleTrigger) newTrigger() 
				                .withIdentity(endTriggerName, current.getDrIdentifier())
				                .startAt(endDate)
				                .withSchedule(simpleSchedule()
				                		.withRepeatCount(0))
				                .build();
				
				        SchedulerManager.getInstance().getScheduler().scheduleJob(endJob, endTrigger);
					}
					
					
					// Remove the event from the cancelled list
					int index = 0;
					boolean notCancel = false;
					for(DRTarget t:cancelledList) {
						if(t.getDrIdentifier().equals(current.getDrIdentifier()) && t.getUid().compareTo(current.getUid()) == 0) {
							notCancel = true;
							break;
						}
						index++;
					}
					if(notCancel) {
						cancelledList.remove(index);
					}
				}
				else
				{
					//Priority 0 means lowest priority thread hence setting Max value so that I will be in last while getting current Active DR Event
					if(dr.getPriority() == null || dr.getPriority()==0 )
					{
						dr.setPriority(Integer.MAX_VALUE);
					}
					dr.setDrType(DRType.OADR.getName().toUpperCase());
					dr.setOptIn(true);
					dr.setEnabled(DRTarget.DISABLED);
					if(dr.getStartAfter().compareTo(0L) == 0) {
						dr.setJitter(0L);
					}
					else {
						if(dr.getUid() != 0) {
							DRTarget firstTarget = drTargetManager.getFirstDRTargetByIdentifier(dr.getDrIdentifier());
							dr.setJitter(firstTarget.getJitter());
						}
						else {
							dr.setJitter((random.nextInt(dr.getStartAfter().intValue())) * 1000L);
						}
					}
					dr.setTargetReduction(getPercentRedByDrLevel(dr.getPriceLevel()));
					dr.setDrStatus(dr.getDrStatus().substring(0, 1).toUpperCase() +
								dr.getDrStatus().substring(1).toLowerCase());
					
					drTargetManager.saveOrUpdateDRTarget(dr);
					
					// Create quartz job
			        JobDetail startJob = newJob(ADRSchedulerJob.class)
			                .withIdentity(startJobName, dr.getDrIdentifier())
			                .build();
			        
			        // Create Quartz trigger
			        SimpleTrigger startTrigger = (SimpleTrigger) newTrigger() 
			                .withIdentity(startTriggerName, dr.getDrIdentifier())
			                .startAt(new Date(dr.getStartTime().getTime() + dr.getJitter()))
			                .withSchedule(simpleSchedule()
			                		.withRepeatCount(0))
			                .build();
			        SchedulerManager.getInstance().getScheduler().scheduleJob(startJob, startTrigger);

			        Date endDate = new Date();
					endDate.setTime(dr.getStartTime().getTime() + dr.getDuration() * 1000 + dr.getJitter() + 2000);

					// Delete the older Quartz job and create a new one
		        	if(SchedulerManager.getInstance().getScheduler().checkExists(new JobKey(endJobName, dr.getDrIdentifier()))) {
		        		if(SchedulerManager.getInstance().getScheduler().deleteJob(new JobKey(endJobName, dr.getDrIdentifier())) == false)
		        			System.out.println("Failed to delete Quartz job" + endJobName);
		        	}

		        	// Create quartz job
			        JobDetail endJob = newJob(ADRSchedulerJob.class)
			                .withIdentity(endJobName, dr.getDrIdentifier())
			                .build();
			        
			        // Create Quartz trigger
			        SimpleTrigger endTrigger = (SimpleTrigger) newTrigger() 
			                .withIdentity(endTriggerName, dr.getDrIdentifier())
			                .startAt(endDate)
			                .withSchedule(simpleSchedule()
			                		.withRepeatCount(0))
			                .build();
			        SchedulerManager.getInstance().getScheduler().scheduleJob(endJob, endTrigger);
				}
			}
			
			// Cancel the remaining DR events
		   	if(cancelledList != null)
		   	{
				for(DRTarget t:cancelledList)
				{
					if(t.getDrStatus().equals(DRStatusType.Active.getName()) && new Date(t.getStartTime().getTime() + t.getJitter() + t.getDuration()*1000).compareTo(new Date()) <= 0) {
						t.setEnabled(DRTarget.DISABLED);
						t.setDrStatus(DRStatusType.Completed.getName());
						drTargetManager.saveOrUpdateDRTarget(t);
						DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);
					}
					else {
						boolean isEnabled = false;
						if(t.getCancelTime() == null) {
							t.setCancelTime(new Date());
						}
						if(new Date(t.getCancelTime().getTime() + t.getJitter()).compareTo(new Date()) <= 0) {
							isEnabled = (DRTarget.ENABLED).equals(t.getEnabled());
							t.setEnabled(DRTarget.DISABLED);
							t.setDrStatus(DRStatusType.Cancelled.getName());
						}
						drTargetManager.saveOrUpdateDRTarget(t);
						if(isEnabled) {
							if(t.getDuration() != 0) {
								drTargetManager.cancelDR();
							}
							DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);
						}
					}
				}
		   	}
		}
		catch(SchedulerException ex) {
			ex.printStackTrace();
			return retList;
		}
		return retList;
	}
	
	@SuppressWarnings("rawtypes")
	@Path("list/alternate/filter")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public DRTargetList loadADRListWithSpecificAttrs(@FormParam("page") Integer page) {
		int x = DRTargetList.DEFAULT_ROWS;
		DRTargetList mList = new DRTargetList();
		//mList.setRecords(12L);
		//mList.setTotal(2L);
		List oddList = drTargetManager.getAllDRTargets(((page-1)*x),x,mList);
		
		List<DRTarget> addList = new ArrayList<DRTarget>();
		for (Iterator iterator = oddList.iterator(); iterator.hasNext();) {
			
			DRTarget drTarget = new DRTarget();
			Object[] object = (Object[]) iterator.next();
			Long drId = (Long) object[0];
			String priceLevel = (String)object[1];
			Double pricing  =(Double)object[2];
			Integer duration =(Integer)object[3];
			Date startTime = (Date)object[4];
			
			Integer priority =(Integer)object[5];
			String drIdentifier =(String)object[6];
			String status =(String)object[7];
			Boolean optIn = (Boolean)object[8];
			Long jitter = (Long)object[9];
			
			
			drTarget.setId(drId);
			drTarget.setPriceLevel(priceLevel);
			drTarget.setPricing(pricing);
			drTarget.setDuration(duration);
			drTarget.setStartTime(startTime);
			drTarget.setPriority(priority);
			drTarget.setDrIdentifier(drIdentifier);
			drTarget.setDrStatus(status);
			drTarget.setOptIn(optIn);
			drTarget.setJitter(jitter);
			
			addList.add(drTarget);			
		}
		mList.setDrtarget(addList);
		mList.setPage(page);
		return mList;
	}
	
	@SuppressWarnings("rawtypes")
	@Path("list/alternate/cancom")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public DRTargetList loadADRListWithCancelAndComplete(@FormParam("page") Integer page) {
		int x = DRTargetList.DEFAULT_ROWS;
		DRTargetList mList = new DRTargetList();
		//mList.setRecords(12L);
		//mList.setTotal(2L);
		List oddList = drTargetManager.getAllDRTargetsCanceledOrCompleted(((page-1)*x),x,mList);
		
		List<DRTarget> addList = new ArrayList<DRTarget>();
		for (Iterator iterator = oddList.iterator(); iterator.hasNext();) {
			
			DRTarget drTarget = new DRTarget();
			Object[] object = (Object[]) iterator.next();
			Long drId = (Long) object[0];
			String priceLevel = (String)object[1];
			Double pricing  =(Double)object[2];
			Integer duration =(Integer)object[3];
			Date startTime = (Date)object[4];		
			
			
			Integer priority =(Integer)object[5];
			String drIdentifier =(String)object[6];
			String status =(String)object[7];
			Boolean optIn = (Boolean)object[8];
			Long jitter = (Long)object[9];

			drTarget.setId(drId);
			drTarget.setPriceLevel(priceLevel);
			drTarget.setPricing(pricing);
			drTarget.setDuration(duration);
			drTarget.setStartTime(startTime);
			drTarget.setPriority(priority);
			drTarget.setDrIdentifier(drIdentifier);
			drTarget.setDrStatus(status);
			drTarget.setOptIn(optIn);
			drTarget.setJitter(jitter);
			
			addList.add(drTarget);			
		}
		mList.setDrtarget(addList);
		mList.setPage(page);
		return mList;
	}
	
	@Path("updateflag/id/{id}/flag/{flag}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateFlag(@PathParam("id") Long id,@PathParam("flag") String flag)
	{		
		Response oResponse = new Response();
		if (flag.equalsIgnoreCase("false")) {
			DRTarget drTarget = drTargetManager.getEnabledDRTargetByDrEvent(id);
			boolean isEnabled = drTarget.getEnabled().equals(DRTarget.ENABLED);
			
			drTargetManager.optOutOfDrByIdentifier(drTarget.getDrIdentifier());
			if(isEnabled)
			{
				if (drTarget.getDuration() != 0) {
					drTargetManager.cancelDR();
				}
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways(true);
			}
		}
		return oResponse;		
	}
	
	private int getPercentRedByDrLevel(String drLevel) {
		drLevel = drLevel.toUpperCase();
		if ("LOW".equals(drLevel)) {
			return 10;
		}
		if ("MODERATE".equals(drLevel)) {
			return 25;
		}
		if ("HIGH".equals(drLevel)) {
			return 50;
		}
		if ("SPECIAL".equals(drLevel)) {
			return 40;
		}
		return 0;
	}
}
