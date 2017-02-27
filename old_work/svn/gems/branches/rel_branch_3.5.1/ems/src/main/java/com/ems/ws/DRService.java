/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.DRRecord;
import com.ems.model.DRTarget;
import com.ems.model.GroupECRecord;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.DRTargetManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.PricingManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.CommonUtils;
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
	
	@Autowired
	private MessageSource messageSource;
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("group/ec/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GroupECRecord> loadGroupEnergyConsumptionBetweenPeriods(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		Response resp = new Response();  
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("fromDate", fdate);
	    nameValMap.put("toDate", tdate);
	    resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
	    if(resp!= null && resp.getStatus()!=200){
	    	m_Logger.error("Validation error"+resp.getMsg());
	    	return null;
	    }
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		try {
			oFDate = inputFormat.parse(fdate);
			oTDate = inputFormat.parse(tdate);
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	 * sensitivity); 
	 * 
	 * @param groupId
	 *            Group id
	 * @param percentage
	 *            (-100 | 0 | 100)
	 * @param time
	 *            specified time in minutes
	 * @return response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/dim/group/{groupid}/{percentage}/{time}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String dimFixture(String data, @PathParam("groupid") Integer groupId,
			@PathParam("percentage") String percentage,
			@PathParam("time") String time) {
		/*Response resp = new Response();
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("percentage", percentage);
		nameValMap.put("time", time);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return "";
		}*/
		groupManager.dimFixtures(groupId, -1 * Integer.valueOf(percentage).intValue(),
				Integer.valueOf(time));
		userAuditLoggerUtil.log("Dimmed Fixture, Group Id : " + groupId + " %age: " + percentage,UserAuditActionType.DR.getName());
		return "S";
	}
	
	/**
	 * clears dr events from database
	 * 
	 * @return response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
						oStatus.setStatus(0);
						oStatus.setMsg("F");
					e.printStackTrace();
				}
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();

			} catch (IOException e) {
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("fixture/record/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DRRecord> getFixtureECProfileRecords(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Response resp = new Response();  
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("fromDate", fdate);
	    nameValMap.put("toDate", tdate);
	    resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
	    if(resp!= null && resp.getStatus()!=200){
	    	m_Logger.error("Validation error"+resp.getMsg());
	    	return new ArrayList<DRRecord>();
	    }
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
	 * Updates the drTarget
	 * 
	 * @param drTarget
	 * @return Response Status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
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
					current.setTargetReduction(drTargetManager.getPercentRedByDrLevel(dr.getPriceLevel()));
					
					drTargetManager.saveOrUpdateDRTarget(current);
					
					if(prevStartTime.compareTo(dr.getStartTime()) != 0 || prevStartAfter.compareTo(dr.getStartAfter()) != 0)
					{
						Date endDate = new Date();
						endDate.setTime(current.getStartTime().getTime() + current.getDuration() * 1000 + current.getJitter() + 2000);
						
						// Delete the older Quartz job and create a new one
						drTargetManager.deleteScheduledJob(startJobName, current.getDrIdentifier());

						drTargetManager.scheduleOverrideJobs(startJobName, current.getDrIdentifier(), startTriggerName, new Date(current.getStartTime().getTime() + current.getJitter()));
			        	
				        DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
				        
				        drTargetManager.deleteScheduledJob(endJobName, current.getDrIdentifier());
				        
				        drTargetManager.scheduleOverrideJobs(endJobName, current.getDrIdentifier(), endTriggerName, endDate);

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
					dr.setTargetReduction(drTargetManager.getPercentRedByDrLevel(dr.getPriceLevel()));
					dr.setDrStatus(dr.getDrStatus().substring(0, 1).toUpperCase() +
								dr.getDrStatus().substring(1).toLowerCase());
					
					drTargetManager.saveOrUpdateDRTarget(dr);
					
					// Delete the older Quartz job and create a new one
					drTargetManager.deleteScheduledJob(startJobName, dr.getDrIdentifier());

					drTargetManager.scheduleOverrideJobs(startJobName, dr.getDrIdentifier(), startTriggerName, new Date(dr.getStartTime().getTime() + dr.getJitter()));
		        	
					Date endDate = new Date();
					endDate.setTime(dr.getStartTime().getTime() + dr.getDuration() * 1000 + dr.getJitter() + 2000);
			        
			        drTargetManager.deleteScheduledJob(endJobName, dr.getDrIdentifier());
			        
			        drTargetManager.scheduleOverrideJobs(endJobName, dr.getDrIdentifier(), endTriggerName, endDate);
				}
			}
			
			// Cancel the remaining DR events
		   	if(cancelledList != null)
		   	{
				for(DRTarget t:cancelledList)
				{
					if(t.getDrStatus().equals(DRStatusType.Active.getName()) && new Date(t.getStartTime().getTime() + t.getJitter() + t.getDuration()*1000).compareTo(new Date()) <= 0) {
						t.setDrStatus(DRStatusType.Completed.getName());
						drTargetManager.saveOrUpdateDRTarget(t);
						DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
					}
					else {
						if(t.getCancelTime() == null) {
							t.setCancelTime(new Date());
						}
						String status = null;
						if(new Date(t.getCancelTime().getTime() + t.getJitter()).compareTo(new Date()) <= 0) {
							status = t.getDrStatus();
							t.setDrStatus(DRStatusType.Cancelled.getName());
						}
						drTargetManager.saveOrUpdateDRTarget(t);
						
						if(DRStatusType.Active.getName().equals(status) && t.getDuration() != 0) {
							drTargetManager.cancelDR();
						}
						DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	
	@SuppressWarnings("rawtypes")
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("list/manualdr/showAllChecked/{showAllChecked}")
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public DRTargetList loadManualDRListWithSpecificAttrs(@PathParam("showAllChecked") Boolean showAllChecked, 
			                                              @FormParam("page") Integer page,
			                                              @FormParam("sidx") String orderby,
			                                  			  @FormParam("sord") String orderway) 
	{		
		int x = DRTargetList.DEFAULT_ROWS;
		DRTargetList mList = new DRTargetList();
		
		Response resp = new Response();  
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("typeBoolean", showAllChecked);	   
	    resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
	    if(resp!= null && resp.getStatus()!=200){
	    	m_Logger.error("Validation error"+resp.getMsg());
	    	return mList;
	    }
		
		List oddList = drTargetManager.getAllManualDRTargets(((page-1)*x),x,mList,showAllChecked,orderby,orderway);
		
		List<DRTarget> addList = new ArrayList<DRTarget>();
		for (Iterator iterator = oddList.iterator(); iterator.hasNext();) {
			
			DRTarget drTarget = new DRTarget();
			Object[] object = (Object[]) iterator.next();
			Long drId = (Long) object[0];
			String priceLevel = (String)object[1];
			Double pricing  =(Double)object[2];
			Integer duration =(Integer)object[3];
			Date startTime = (Date)object[4];
			String status =(String)object[5];
			String drtype =(String)object[6];
			String description =(String)object[7];
			
			drTarget.setId(drId);
			drTarget.setPriceLevel(priceLevel);
			drTarget.setPricing(pricing);
			drTarget.setDuration(duration);
			drTarget.setStartTime(startTime);			
			drTarget.setDrStatus(status);
			drTarget.setDrType(drtype);
			drTarget.setDescription(description);
			
			addList.add(drTarget);			
		}
		mList.setDrtarget(addList);
		mList.setPage(page);
		return mList;
	}
		
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updateflag/id/{id}/flag/{flag}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateFlag(@PathParam("id") Long id,@PathParam("flag") String flag)
	{		
		Response oResponse = new Response();
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("id", id);
		nameValMap.put("updateFlag", flag);	  		
		oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(oResponse!= null && oResponse.getStatus()!=200){
			m_Logger.error("Validation error"+oResponse.getMsg());
	    	return oResponse;
	    }
		if (flag.equalsIgnoreCase("false")) {
			DRTarget drTarget = drTargetManager.getDRTargetById(id);
			String status = drTarget.getDrStatus();
			drTargetManager.optOutOfDrByIdentifier(drTarget.getDrIdentifier());
			if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
				drTargetManager.cancelDR();
			}
			DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
		}
		return oResponse;		
	}

	/**
	 * cancels dr event
	 * 
	 * @return response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("cancel")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelDR(DRTarget dr) {
		Response oStatus = new Response();
		DRTarget drTarget = drTargetManager.getDRTargetById(dr.getId());
		if(drTarget!=null) {
			String status = drTarget.getDrStatus();
			if(DRStatusType.Cancelled.getName().equals(status) || DRStatusType.Completed.getName().equals(status)) {
				oStatus.setMsg("E");
			}
			else {
				if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
					drTargetManager.cancelDR();
				}
				drTarget.setDrStatus(DRStatusType.Cancelled.getName());
				String startJobName = "DRStartJob" + drTarget.getId();
				String endJobName = "DREndJob" + drTarget.getId();
				try {
					drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
					drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
				String message = "DR Cancelled at "+ new Date(System.currentTimeMillis());
				userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
				oStatus.setMsg("S");
			}
		}
		return oStatus;
	}
	
	/**
	 * cancels multiple DRs
	 * 
	 * @param drtargets
	 *            List of drtargets
	 *            "<dRTargets><drTarget><id>1</id></drTarget></dRTargets>"
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("cancel/multiple")
	@POST	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Response> cancelMultipleDRs(List<DRTarget> drtargets) {
		Iterator<DRTarget> itr = drtargets.iterator();		
		List<Response> responseList = new ArrayList<Response>();
		
		while (itr.hasNext()) {
			Response oStatus = new Response();			
			DRTarget drTarget = drTargetManager.getDRTargetById(((DRTarget) itr.next()).getId());	
			
			if(drTarget!=null) {	
				String status = drTarget.getDrStatus();
				if(DRStatusType.Cancelled.getName().equals(status) || DRStatusType.Completed.getName().equals(status)) {
					oStatus.setMsg("E");
				}
				else {
					if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
						drTargetManager.cancelDR();
					}
					drTarget.setDrStatus(DRStatusType.Cancelled.getName());
					String startJobName = "DRStartJob" + drTarget.getId();
					String endJobName = "DREndJob" + drTarget.getId();
					try {
						drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
						drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
					} catch (SchedulerException e) {
						e.printStackTrace();
					}
					
					DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
					String message = "DR Cancelled at "+ new Date(System.currentTimeMillis());
					userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
					oStatus.setMsg("S");
				}
				responseList.add(oStatus);
			}
		}		
		return responseList;
	}
	
	/**
	 * deletes single dr event
	 * 
	 * @return response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/id/{id}")
	@POST	
	public Response deleteDR(@PathParam("id") long id) {	
		DRTarget drTarget = drTargetManager.getDRTargetById(id);
		if(drTarget!=null) {
			String startJobName = "DRStartJob" + drTarget.getId();
			String endJobName = "DREndJob" + drTarget.getId();
			try {
				drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
				drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			String status = drTarget.getDrStatus();
			if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
				drTargetManager.cancelDR();
			}
			String message = "DR deleted at "+ new Date(System.currentTimeMillis());
			userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
		}
		drTargetManager.deleteDRTarget(id);
		DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
		return new Response();
	}
	
	/**
	 * deletes multiple DRs
	 * 
	 * @param drtargets
	 *            List of drtargets
	 *            "<dRTargets><drTarget><id>1</id></drTarget></dRTargets>"
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/multiple")
	@POST	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Response> deleteMultipleDRs(List<DRTarget> drtargets) {
		m_Logger.debug("DRs: " + drtargets.size());
		
		Iterator<DRTarget> itr = drtargets.iterator();
		List<Response> responseList = new ArrayList<Response>();
		long drId = 0;
		DRTarget drTarget = new DRTarget();
		
		while (itr.hasNext()) {
			Response oResponse = new Response();
			
			drTarget = (DRTarget) itr.next();
			drId = drTarget.getId();
			
			drTarget = drTargetManager.getDRTargetById(drId);
			if(drTarget!=null) {
				String startJobName = "DRStartJob" + drTarget.getId();
				String endJobName = "DREndJob" + drTarget.getId();
				try {
					drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
					drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
				String status = drTarget.getDrStatus();
				if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
					drTargetManager.cancelDR();
				}
				String message = "DR deleted at "+ new Date(System.currentTimeMillis());
				userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
			}
			
			oResponse.setStatus(drTargetManager.deleteDRTarget(drId));
			DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
			oResponse.setMsg(String.valueOf(drId)); // using message as current dr id
			
			responseList.add(oResponse);
		}		
		
		return responseList;
	}
	
	 /**
	 * The API to schedule a DR for a specific period of time from a start time
	 * 
	 * @param drtarget
	 *            "<drTarget><pricelevel></pricelevel><duration></duration><starttime></starttime><dridentifier></dridentifier><pricing></pricing></drTarget>"
	 *    			dr_level: HIGH(1)/MODERATE(2)/LOW(3)
	 *				duration: In minutes for which this DR should be in effect
 	 * 				start_time: time from when DR should be in effect
 	 * 				dr_identifier: unique identifier string to identify a particular DR
 	 * 				price: price to be effective for the energy consumed during the DR (optional)
	 * @return Response status
	 * 			0: sucess
 	 * 			1: failure
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("scheduledr")
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response scheduleDR(DRTarget drTarget) {
		Response res= new Response();
		Integer duration = null;
		Double drPricing = null;
		try {
			Boolean isInValidDrIdentifier =ArgumentUtils.isNullOrEmpty(drTarget.getDrIdentifier());
			Boolean isValidPriceLevel = ArgumentUtils.isInEnum(drTarget.getPriceLevel(), DrLevel.class);
			if(isInValidDrIdentifier ==true)
			{
				res.setMsg("DR Identifier is missing. Please provide unique DR Identifier");
				res.setStatus(1);
				return res;
			}else if(drTarget.getStartTime()==null)
			{
				res.setMsg("Start date is invalid. Please provide valid start time in 'yyyy-MM-dd HH:mm:ss' format");
				res.setStatus(1);
				return res;
			}else if(isValidPriceLevel==false)
			{
				res.setMsg("DR Level is invalid. Please provide valid DR Level in between HIGH/MODERATE/LOW");
				res.setStatus(1);
				return res;
			}else if(drTarget.getDuration()==null)
			{
				res.setMsg("Duration is invalid. Please provide valid duration in minutes");
				res.setStatus(1);
				return res;
			}
			
			if(drTarget.getPricing()!=null && drTarget.getPricing() < 0)
			{
				res.setMsg("DR Pricing is invalid. Please provide valid price");
				res.setStatus(1);
				return res;
			}
			
			//Check if the DR Identifier already exists in DB
			DRTarget drTargetinDB = drTargetManager.getDRTargetByDrIdentifier(drTarget.getDrIdentifier());
			if(drTargetinDB==null)
			{
				drTarget.setDrStatus(DRStatusType.Scheduled.getName());
				drTarget.setDrType(DRType.MANUAL.getName());
				//duration provided in web service post data is in minutes. db value in seconds. hence the conversion
				drTarget.setDuration(drTarget.getDuration()*60);
				if (drTarget.getId() == null) {   
					drTarget.setOptIn(true);
					drTarget.setJitter(0L);
					if(drTarget.getDrType().equals(DRType.MANUAL.getName())) {
						drTarget.setTargetReduction(drTargetManager.getPercentRedByDrLevel(drTarget.getPriceLevel()));
					}
					drTargetManager.saveOrUpdateDRTarget(drTarget);    	            
				}
				String startJobName = "DRStartJob" + drTarget.getId();
				String startTriggerName = "DRStartTr" + drTarget.getId();
				String endJobName = "DREndJob" + drTarget.getId();
				String endTriggerName = "DREndTr" + drTarget.getId();
				Date endDate = new Date();
				endDate.setTime(drTarget.getStartTime().getTime() + drTarget.getDuration() * 1000 + 2000);
				try {
					// Delete the older Quartz job and create a new one
					drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
					drTargetManager.scheduleOverrideJobs(startJobName, drTarget.getDrType(), startTriggerName, new Date(drTarget.getStartTime().getTime()));
				    DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
					drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
					drTargetManager.scheduleOverrideJobs(endJobName, drTarget.getDrType(), endTriggerName, endDate);
				} catch (SchedulerException e) {
					res.setMsg("Error occured while scheduling the service: "+e.getMessage());
					res.setStatus(1);
					e.printStackTrace();
				}
				res.setMsg("DR Scheduled successfully");
			}else
			{
				res.setMsg("DR service with given DR Identifier is already exists. Please use different DR Identifier to schedule new DR Event" );
				res.setStatus(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setMsg("Error occured while processing the service: "+e.getMessage());
			res.setStatus(1);
		}
		return res;
	}
	
	
	/**
	 * cancels dr event
	 * 
	 * @param drtarget
	 *            "<drTarget><dridentifier></dridentifier></drTarget>"
	 *    			dr_identifier: unique identifier string to identify a particular DR
	 * @return Response status
	 * 			0: success
 	 * 			1: failure
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("canceldr")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelDREvent(DRTarget dr) {
		Response oStatus = new Response();
		DRTarget drTarget = drTargetManager.getDRTargetByDrIdentifier(dr.getDrIdentifier());
		if(drTarget!=null) {
			String status = drTarget.getDrStatus();
			if(DRStatusType.Cancelled.getName().equals(status) || DRStatusType.Completed.getName().equals(status)) {
				oStatus.setMsg("E");
			}
			else {
				if (DRStatusType.Active.getName().equals(status) && drTarget.getDuration() != 0) {
					drTargetManager.cancelDR();
				}
				drTarget.setDrStatus(DRStatusType.Cancelled.getName());
				String startJobName = "DRStartJob" + drTarget.getId();
				String endJobName = "DREndJob" + drTarget.getId();
				try {
					drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
					drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
				DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
				String message = "DR Cancelled at "+ new Date(System.currentTimeMillis());
				userAuditLoggerUtil.log(message,UserAuditActionType.DR.getName());
				oStatus.setMsg("DR Cancelled successfully");
			}
		}else
		{
			oStatus.setStatus(1);
			oStatus.setMsg("Dr Identifier could not be found to cancel the dr event");
		}
		return oStatus;
	}
	/**
	 * Get list of all dr event
	 * @return Response List of DrTarget vos
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("listdr")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DRTarget> getAllActiveDrList() {
		return drTargetManager.getAllScheduledManualDRTargets();
	}
	
	
	 /**
	 * Update existing DR
	 * 
	 * @param drtarget
	 *            "<drTarget><pricelevel></pricelevel><duration></duration><starttime></starttime><dridentifier></dridentifier><pricing></pricing></drTarget>"
	 *    			dr_level: HIGH(1)/MODERATE(2)/LOW(3)
	 *				duration: In minutes for which this DR should be in effect
 	 * 				start_time: time from when DR should be in effect
 	 * 				dr_identifier: unique identifier string to identify a particular DR
 	 * 				price: price to be effective for the energy consumed during the DR (optional)
	 * @return Response status
	 * 			0: success
 	 * 			1: failure
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("updatedr")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updatedr(DRTarget drTarget) {
		Response res = new Response();
		
		Integer duration = null;
		Double drPricing = null;
		try {
			Boolean isInValidDrIdentifier =ArgumentUtils.isNullOrEmpty(drTarget.getDrIdentifier());
			Boolean isValidPriceLevel = ArgumentUtils.isInEnum(drTarget.getPriceLevel(), DrLevel.class);
			if(isInValidDrIdentifier ==true)
			{
				res.setMsg("DR Identifier is missing. Please provide unique DR Identifier");
				res.setStatus(1);
				return res;
			}else if(drTarget.getStartTime()==null)
			{
				res.setMsg("Start date is invalid. Please provide valid start time in 'yyyy-MM-dd HH:mm:ss' format");
				res.setStatus(1);
				return res;
			}else if(isValidPriceLevel==false)
			{
				res.setMsg("DR Level is invalid. Please provide valid DR Level in between HIGH/MODERATE/LOW");
				res.setStatus(1);
				return res;
			}else if(drTarget.getDuration()==null)
			{
				res.setMsg("Duration is invalid. Please provide valid duration in minutes");
				res.setStatus(1);
				return res;
			}
			
			if(drTarget.getPricing()!=null && drTarget.getPricing() < 0)
			{
				res.setMsg("DR Pricing is invalid. Please provide valid price");
				res.setStatus(1);
				return res;
			}
			//Check if the DR Identifier already exists in DB
			DRTarget drTargetToSave = drTargetManager.getDRTargetByDrIdentifier(drTarget.getDrIdentifier());
			if(drTargetToSave!=null)
			{
				//duration provided in web service post data is in minutes. db value in seconds. hence the conversion
				drTargetToSave.setDuration(drTarget.getDuration()*60);
				drTargetToSave.setPriceLevel(drTarget.getPriceLevel());
		    	drTargetToSave.setPricing(drTarget.getPricing());
		    	if(drTarget.getStartTime() != null)
		    		drTargetToSave.setStartTime(drTarget.getStartTime());        	
		    	drTargetToSave.setDescription(drTarget.getDescription());
		    	if(drTargetToSave.getDrType().equals(DRType.MANUAL.getName())) {
		    		drTargetToSave.setTargetReduction(drTargetManager.getPercentRedByDrLevel(drTarget.getPriceLevel()));
				}
		    	drTarget = drTargetManager.saveOrUpdateDRTarget(drTargetToSave);   
			
				String startJobName = "DRStartJob" + drTarget.getId();
				String startTriggerName = "DRStartTr" + drTarget.getId();
				String endJobName = "DREndJob" + drTarget.getId();
				String endTriggerName = "DREndTr" + drTarget.getId();
				
				Date endDate = new Date();
				endDate.setTime(drTarget.getStartTime().getTime() + drTarget.getDuration() * 1000 + 2000);
			    try {
			    	// Delete the older Quartz job and create a new one
					drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
					drTargetManager.scheduleOverrideJobs(startJobName, drTarget.getDrType(), startTriggerName, new Date(drTarget.getStartTime().getTime()));
			        DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
					drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
					drTargetManager.scheduleOverrideJobs(endJobName, drTarget.getDrType(), endTriggerName, endDate);
					res.setMsg("DR updated successfully");
				} catch (SchedulerException e) {
					e.printStackTrace();
					res.setStatus(1);
				}
			}
		    else
			{
				res.setMsg("DR service with given DR Identifier does not exists. Please use different DR Identifier to schedule new DR Event" );
				res.setStatus(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setMsg("Error occured while processing the service: "+e.getMessage());
			res.setStatus(1);
		}
		return res;
	}
}
