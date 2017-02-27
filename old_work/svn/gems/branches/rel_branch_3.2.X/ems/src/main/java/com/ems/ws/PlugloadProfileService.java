package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
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
import com.ems.server.ServerConstants;
import com.ems.service.MetaDataManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadProfileManager;
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
	private MetaDataManager metadataManager;
	
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

}
