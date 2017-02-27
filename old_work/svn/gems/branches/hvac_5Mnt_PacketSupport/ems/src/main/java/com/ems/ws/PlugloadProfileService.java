package com.ems.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;


import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.server.ServerConstants;
import com.ems.service.MetaDataManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.PlugloadProfileManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.vo.AssignPlugload;
import com.ems.vo.AssignPlugloadList;
import com.ems.ws.util.Response;
import com.ems.ws.util.WebServiceUtils;


@Controller
@Path("/org/plugloadProfile")
public class PlugloadProfileService {
	
	@Resource
	private PlugloadProfileManager plugloadProfileManager;
	@Resource
	private PlugloadGroupManager plugloadGroupManager;
	
	@Resource
	private PlugloadManager plugloadManager;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource
	private MetaDataManager metadataManager;
	
	@Resource(name = "emsAuthContext")
   	private EmsAuthenticationContext emsAuthContext;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	

    /**
     * Assign new profile to the list of selected plugload(s)
     * 
     * @param currentProfile
     *            Name of the selected profile
     * @param originalProfile
     *            Name of the original profile
     * @param groupId
     *            Id of the selected group to be assigned
     * @param plugload
     *            plugload "<plugload><id>1</id></plugload>"
     * @return Response status
     */
	    @Path("assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}")
	    @POST
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public Response assignProfile(@PathParam("currentprofile") String currentProfile,
	            @PathParam("originalprofile") String originalProfile, @PathParam("groupid") Long groupId, Plugload plugload) {
	    	 Response oStatus = new Response();
	    	 if(plugload != null){
	    		 m_Logger.debug("Assinging profile: '" + currentProfile + "' to (" + plugload.getId() + ") plugload");
	    		 plugloadProfileManager.changePlugloadProfile(plugload.getId(), groupId, currentProfile, originalProfile);
	    	 }
	       return oStatus;
	 }
	    
	    /**
	     * Get the list of all the groups in system
	     * 
	     * @return PlugloadGroups list
	     */
	    @Path("listPlugloadProfiles")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public List<PlugloadGroups> getAllProfiles() {
	        return plugloadGroupManager.loadAllPlugloadGroups();
	    }
	    
	    @Path("getPlugloadProfileHandlerById/{plugloadProfileHandlerId}")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public PlugloadProfileHandler getProfileHandlerById(@PathParam("plugloadProfileHandlerId") Long profileHandlerId) {
	        return plugloadGroupManager.fetchPlugloadProfileHandlerById(profileHandlerId);
	    }
	    
	    @Path("getPlugloadProfileById/{plugloadProfileId}")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public PlugloadGroups getPlugloadProfileById(@PathParam("plugloadProfileId") Long profileId) {
	        return plugloadGroupManager.getGroupById(profileId);
	    }
	    
	   /* @Path("changeplugloadassignlist")
	    @POST 
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public Response setSelectedProfiles(List<Plugload> fixtureList) {
	    return null;
	    }*/
	    
	    
	    /**
	     * Delete the plugload profile for the selected id
	     * 
	     * @param List of plugload groups in the format 
	     *          <PlugloadGroupss><PlugloadGroups><id>{plugload profile id}</id></PlugloadGroups></PlugloadGroupss>
	     *           
	     * @return Response - If plugload profileId deleted successful, response status = successfully deleted profiles count other wise 0
	     */
	    @Path("delete")
		@POST
		@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
		public Response deletePlugloadProfiles(List<PlugloadGroups> groups) {
	    	
	    	m_Logger.debug("Plugload Profiles: " + groups.size());
	    	System.out.println("Plugload groups size"+groups.size());
			Iterator<PlugloadGroups> itr = groups.iterator();
			Response oResponse = new Response();
			long plugloadProfileId = 0;
			PlugloadGroups plugloadProfile = new PlugloadGroups();
			while (itr.hasNext()) {
				System.out.println("inside loop");
				plugloadProfile = (PlugloadGroups) itr.next();
				System.out.println("profile object"+plugloadProfile.getName());
				plugloadProfileId = plugloadProfile.getId();
				int status = 0;
				try {
					status = plugloadGroupManager.deleteProfile(plugloadProfileId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				oResponse.setStatus(status);
				oResponse.setMsg(String.valueOf(plugloadProfileId)); 
			}
			return oResponse;
			
	    	
	    }
	
	    /**
	     * Check for duplicate profile name.
	     * 
	     * @param profilename
	     * @return Response object - If Duplicate, response object will return profile name otherwise returns 0
	     */
	    @Path("duplicatecheck/{profilename}")
		@GET
		@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
		public Response checkDuplicateProfile(@PathParam("profilename") String profilename) {
	    	System.out.println("profile name is "+profilename);
			Response oStatus = new Response();
			PlugloadGroups group = plugloadGroupManager.getPlugloadGroupByName(profilename);
			if (group!=null && group.getId() > 0) {				
				oStatus.setMsg(profilename);
				oStatus.setStatus(0);
				return oStatus;
			}
			oStatus.setMsg("0");
			oStatus.setStatus(0);
			return oStatus;
		}
	    
	    
	    /**
	     * Export Profile in xml format
	     * 
	     * @param GroupId - PlugloadGroups Id to be exported
	     * @return XML - XML Response
	     */
	    @POST
	    @Path("/exportprofile/{groupId}")
	    @Produces("application/xml")
	    public javax.ws.rs.core.Response exportProfiles(@PathParam("groupId") Long groupId) {
	    	
	    	PlugloadGroups dbGroup = plugloadGroupManager.getPlugloadGroupById(groupId);
	        String fileName = dbGroup.getName();//.replaceAll(" ", ".");
	        if(dbGroup.isDefaultProfile()==true)
	        {
	        	fileName+="_Default";
	        }
	        PlugloadProfileHandler ph = null;
	        try {
	            ph = plugloadGroupManager.fetchPlugloadProfileHandlerById(dbGroup.getPlugloadProfileHandler().getId());
	        } catch (Exception e) {
	            m_Logger.error(e.getMessage(), e);
	        }
	        String outPutXMLString = WebServiceUtils.convertModelToString(ph);
	        ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok(outPutXMLString);
	        String outPutFileName = fileName + ".xml";
	        responseBuilder.header("Content-Disposition","attachment; filename=\"" + outPutFileName+"\"");
	     return responseBuilder.build();
	    	
	    }
	    
	    /**
	     * Create a new plugload profile
	     * 
	     * @param PlugloadProfileHandler - PlugloadProfileHandler object with all profile settings values
	     * @param weekdaysString - weekday configuration string i.e true,true,true,true,false,false for Mon-Fri weekdays and Sat,Sun weekend
	     * @return XML - XML Response
	     */
	    @Path("createPlugloadProfile/{param}")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public PlugloadProfileHandler createPlugloadProfiles(PlugloadProfileHandler ph/*,String param*/,@PathParam("param") String weekdaysString){
	    	
	    	return plugloadProfileManager.createPlugloadProfiles(ph,weekdaysString);
	    }
	    
	    /**
	     * Save plugloadProfileHandler object
	     * 
	     * @param PlugloadProfileHandler - plugloadprofilehandler object to be saved
	     * @return Response object - return the saved object in xml format
	     */	    
	    @Path("saveProfileHandler")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public PlugloadProfileHandler savePlugloadProfileHandler(PlugloadProfileHandler plugloadProfileHandler) {
	    	return plugloadProfileManager.savePlugloadProfileHandler(plugloadProfileHandler);
	    }
	    
	    /**
	     * Save plugloadProfileConfiguration object
	     * 
	     * @param plugloadProfileConfiguration - plugloadProfileConfiguration object to be saved
	     * @return Response object - return the saved object in xml format
	     */	 
	    @Path("saveProfileConfiguration")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public PlugloadProfileConfiguration saveProfileConfiguration(PlugloadProfileConfiguration profileConfiguration) {
	    	return plugloadProfileManager.saveProfileConfiguration(profileConfiguration);
	    }
	    
	    /**
	     * Save plugloadProfile object
	     * 
	     * @param plugloadProfile - plugloadProfile object to be saved
	     * @return Response object - return the saved object in xml format
	     */	 
	    @Path("saveProfile")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public void saveProfile(PlugloadProfile profile) {
	    	plugloadProfileManager.saveProfile(profile);
	    }
	    
	    
	    /**
	     * Save plugloadGroups object
	     * 
	     * @param plugloadGroups - plugloadGroups object to be saved
	     * @return Response object - return the saved object in xml format
	     */	
	    @Path("savePlugloadGroup")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public void savePlugloadGroup(PlugloadGroups plugloadGroups) {
	    	metadataManager.saveOrUpdatePlugloadGroup(plugloadGroups);
	    }
	    
	    /**
	     * Save plugloadGroups object
	     * 
	     * @param weekdayString - weekday configuration string from Mon-Sun
	     * @param profileConfiguration - plugloadProfileConfiguration to be mapped against a weekdayPlugload object
	     * @return Response object - return the saved object in xml format
	     */	
	    @Path("saveWeekDays/{weekday}")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public void saveWeekDays(@PathParam("weekday") String weekdayString ,PlugloadProfileConfiguration profileConfiguration) {
	    	//System.out.println("Hello");
	    	plugloadProfileManager.saveWeekDays(weekdayString, profileConfiguration, true);
	    }
	    
	   
	    @Path("testGrp")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })	    
	    public void test() {	    	
	    	System.out.println("max profile no is "+plugloadGroupManager.getMaxPlugloadProfileNo(null));
	    }
	    
	    @Path("saveDefaultPlugloadGroups")
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public void saveDefaultPlugloadGroups() {
	    	PlugloadProfileHandler plugloadProfileHandler = plugloadProfileManager.createPlugloadProfile("default.",
                    ServerConstants.DEFAULT_PLUGLOAD_PROFILE_GID,true);
	    	plugloadProfileManager.saveDefaultPlugloadGroups(plugloadProfileHandler);
	    }
	    
	    //
	    
	    /**
	     * Bulk Assign new plugload profile to the list of selected plugloads
	     * 
	     * @param plugloadList
	     *            plugload "<plugloads><plugload><id>1</id></plugload><plugload><id>2</id></plugload></plugloads>"
	     * @param profileGroupid
	     *            Id of the selected plugload group to be assigned
	     * @param currentPlugloadProfile
	     *         	  Name of the selected plugload profile
	     * @return Response totalRecordUpdated
	     */
	    @Path("bulkassign/{currentPlugloadProfile}/{plugloadGroupid}")
	    @POST
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public Response bulkPlugloadProfileAssign(@PathParam("currentPlugloadProfile") String currentPlugloadProfile,@PathParam("plugloadGroupid") Long plugloadGroupid, List<Plugload> plugloadList) {
	        Response oStatus = new Response();
	        Long totalRecordUpdated = null;
	        String plugloadIdsList="";
	        List<Long> plugloadIds  = new ArrayList<Long>();
			for (Iterator<Plugload> iterator = plugloadList.iterator(); iterator.hasNext();) {
				Long id = new Long(iterator.next().getId() + "");
				plugloadIdsList+=id;
				 if (iterator.hasNext()) {   
					 plugloadIdsList += ",";    
				  } 
				 plugloadIds.add(id);
			}
	        if (plugloadIds != null && !plugloadIds.isEmpty()) {
	            m_Logger.debug("Assinging Plugload profile: '" + currentPlugloadProfile + "' to (" + plugloadIdsList + ") plugloads");
	            totalRecordUpdated = plugloadManager.bulkProfileAssignToPlugload(plugloadIdsList, plugloadGroupid, currentPlugloadProfile);
	        }
	        userAuditLoggerUtil.log("Assign Plugload profile " + currentPlugloadProfile + " to plugloads " + plugloadIdsList, UserAuditActionType.Plugload_Profile_Update.getName());
	        oStatus.setStatus(totalRecordUpdated.intValue());
	        return oStatus;
	    }
	    
	    @Path("assignlist/{pid}")
	    @POST    
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public AssignPlugloadList getAllPlugloadProfiles(@PathParam("pid") Long pid, @FormParam("page") Integer page,@FormParam("selectedplugloads") String selectedplugloads) {    	
	    	AssignPlugloadList returnList = new AssignPlugloadList();
	    	int x = AssignPlugloadList.DEFAULT_ROWS;
	    	
			List oPlugloadList =plugloadGroupManager.loadPlugloadList(pid,selectedplugloads,(page - 1) * x,x,returnList);		
			
			List<AssignPlugload> assignPlugloadList = new ArrayList<AssignPlugload>();
			Integer serial ;
			if(page==1)
			{			
				serial = 1;	
			}
			else
			{
				serial = (page *AssignPlugloadList.DEFAULT_ROWS) - AssignPlugloadList.DEFAULT_ROWS;
				serial++;
			}
			for (Iterator iterator = oPlugloadList.iterator(); iterator.hasNext();) {
				
				AssignPlugload assignPlugload = new AssignPlugload();
				Object[] object = (Object[]) iterator.next();
				Long plugloadId = (Long) object[0];
				String plugloadName = (String)object[1];
				Long currentGroupId  =(Long)object[2];
				String templateName =(String)object[3];
				Long templateId = (Long)object[4];
				
				assignPlugload.setPlugloadId(plugloadId);
				assignPlugload.setPlugloadName(plugloadName);
				assignPlugload.setCurrentPlugloadGroupId(currentGroupId);
				assignPlugload.setTemplate(templateName);	
				assignPlugload.setTemplateId(templateId);
				assignPlugload.setNumber(serial++);
				if (emsAuthContext.getCurrentUserRoleType() == RoleType.Employee) {
					 assignPlugload.setProfileList(plugloadGroupManager.loadAllProfileTemplateById(templateId, 0L));
				}
				assignPlugloadList.add(assignPlugload);				
			}		
			returnList.setPage(page);
			returnList.setProfilePlugloadList(assignPlugloadList);
			return returnList;
		}
	    
	    
	    /**
	     * Change the Plugload Profiles within the given templates list for all selected plugloads. 
	     * This service will be called from floorplan assign plugload profile menu option for Employee role user
	     *    
	     * @param plugloadList
	     *            plugload "<plugloads><plugload><id>1</id></plugload><plugload><id>2</id></plugload></plugloads>"
	     * @return Response message showing "{countUpdatedPlugload} Plugload/s Updated"
	     */
	    @Path("changeassignlist")
	    @POST 
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public Response setSelectedPlugloadProfiles(List<Plugload> plugloadList) {
	    	
	    	Response oStatus = new Response();
	    	String newGroupName = "";
	    	Long newGroupId ;
	    	String oldGroupName = "";
	    	Long oldGroupId ; 
	    	Integer countUpdatedPlugload = 0;
	    	for (Iterator<Plugload> iterator = plugloadList.iterator(); iterator.hasNext();) {
				Plugload newplugload = (Plugload) iterator.next();
				Plugload oldPlugload = plugloadManager.getPlugloadById(newplugload.getId());
				
				oldGroupId = oldPlugload.getGroupId();
				PlugloadGroups oldGroup = plugloadGroupManager.getGroupById(oldGroupId);
				oldGroupName = oldGroup.getName();
				
				newGroupId = newplugload.getGroupId();
				PlugloadGroups newGroup = plugloadGroupManager.getGroupById(newGroupId);
				newGroupName = newGroup.getName();
				
				if(!newGroupId.equals(oldGroupId))
				{
					plugloadManager.changePlugloadProfile(newplugload.getId(), newplugload.getGroupId(), newGroupName, oldGroupName);
					countUpdatedPlugload++;
				}
			}
	    	
	    	oStatus.setMsg(countUpdatedPlugload + " Plugload/s Updated");
	    	return oStatus;
	    }

}
