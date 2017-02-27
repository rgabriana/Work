/**
 * 
 */
package com.emscloud.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
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
import org.springframework.stereotype.Controller;

import com.emscloud.communication.vos.EMProfile;
import com.emscloud.communication.vos.Fixture;
import com.emscloud.communication.vos.Response;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileHandler;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmProfileMappingManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.service.ProfileSyncManager;
import com.emscloud.vo.EmProfileList;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profile")
public class ProfileService {
	
    @Resource(name = "profileGroupManager")
    private ProfileGroupManager groupManager;
    @Resource(name = "emsAuthContext")
   	private EmsAuthenticationContext emsAuthContext;
    @Resource(name = "profileSyncManager")
    private ProfileSyncManager profileSyncManager;
    @Resource(name="emProfileMappingManager")
    EmProfileMappingManager emProfileMappingManager;
    @Resource(name="emInstanceManager")
    EmInstanceManager emInstanceManager;
    @Resource(name="facilityEmMappingManager")
    FacilityEmMappingManager facilityEmMappingManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public ProfileService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    
    /**
     * Bulk Assign profile to the list of selected fixtures
     * 
     * @param fixtureList
     *            fixture "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
     * @param profilegroupid
     *            Id of the selected profile group to be assigned
     * 
     * @return Response totalRecordUpdated
     */
    @Path("bulkassign/{profilegroupid}/{pid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response bulkProfileAssign(@PathParam("profilegroupid") Long profilegroupid, @PathParam("pid") Long pid,List<Fixture> fixtureList) {
        Response oStatus = new Response();
        oStatus = groupManager.bulkProfileAssign(profilegroupid,pid,fixtureList);
        return oStatus;
    }

    /**
     * Get the list of all the groups in system
     * 
     * @return Groups list
     */
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ProfileGroups> getAllProfiles() {
        return groupManager.loadAllGroups();
    }
    
    /**
	 * Returns fixture count associated with the profilegroup
	 * 
	 * @param pid
	 *            profilegroup Id
	 * @return fixturecount
	 */
	@Path("fixturecount/profilegroups/{pgId}")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	public String getFixtureCountByProfileGroupId(@PathParam("pgId") Long pgId) {
		return groupManager.getFixtureCountByProfileGroupId(pgId);
	}
    
    
    /**
     * Get the list of all the groups in system
     * 
     * @return Groups list
     */
    /*
    @Path("assignlist/{pid}")
    @POST    
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public AssignFixtureList getAllProfiles(@PathParam("pid") Long pid, @FormParam("page") Integer page,@FormParam("selectedfixtures") String selectedfixtures) {    	
    	AssignFixtureList returnList = new AssignFixtureList();
    	int x = AssignFixtureList.DEFAULT_ROWS;
    	
		List oFixtureList =groupManager.loadFixtureList(pid,selectedfixtures,(page - 1) * x,x,returnList);		
		
		List<AssignFixture> assignFixtureList = new ArrayList<AssignFixture>();
		Integer serial ;
		if(page==1)
		{			
			serial = 1;	
		}
		else
		{
			serial = (page *AssignFixtureList.DEFAULT_ROWS) - AssignFixtureList.DEFAULT_ROWS;
			serial++;
		}
		for (Iterator iterator = oFixtureList.iterator(); iterator.hasNext();) {
			
			AssignFixture assignFixture = new AssignFixture();
			Object[] object = (Object[]) iterator.next();
			Long fixtureId = (Long) object[0];
			String fixtureName = (String)object[1];
			Long currentGroupId  =(Long)object[2];
			String templateName =(String)object[3];
			Long templateId = (Long)object[4];
			
			assignFixture.setFixtureId(fixtureId);
			assignFixture.setFixtureName(fixtureName);
			assignFixture.setCurrentGroupId(currentGroupId);
			assignFixture.setTemplate(templateName);	
			assignFixture.setTemplateId(templateId);
			assignFixture.setNumber(serial++);
			if (emsAuthContext.getCurrentUserRoleType() == RoleType.Employee) {
				 Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
				 Long tenantId=0L;
				 if(tenant != null)
				 {
					tenantId = tenant.getId();
					assignFixture.setProfileList(groupManager.loadAllProfileForTenantByTemplateId(templateId,tenantId));
				}else
				{
					assignFixture.setProfileList(groupManager.loadAllProfileTemplateById(templateId,tenantId));
				}
			}
			//assignFixture.setProfileList(groupManager.loadAllProfileTemplateById(templateId));
			assignFixtureList.add(assignFixture);				
		}		
		returnList.setPage(page);
		returnList.setProfileFixtureList(assignFixtureList);
		return returnList;
	}
    */
    @Path("changeassignlist")
    @POST 
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response setSelectedProfiles(List<Fixture> fixtureList) {
    	
    	Response oStatus = new Response();
    	String newGroupName = "";
    	Long newGroupId ;
    	String oldGroupName = "";
    	Long oldGroupId = null ; 
    	Integer countUpdatedFixture = 0;
    	for (Iterator<Fixture> iterator = fixtureList.iterator(); iterator.hasNext();) {
			Fixture newfixture = (Fixture) iterator.next();
			//Fixture oldFixture = fixtureManager.getFixtureById(newfixture.getId());
			
			//oldGroupId = oldFixture.getGroupId();
			ProfileGroups oldGroup = groupManager.getGroupById(oldGroupId);
			oldGroupName = oldGroup.getName();
			
			newGroupId = newfixture.getGroupId();
			ProfileGroups newGroup = groupManager.getGroupById(newGroupId);
			newGroupName = newGroup.getName();
			
			if(!newGroupId.equals(oldGroupId))
			{
				//fixtureManager.changeFixtureProfile(newfixture.getId(), newfixture.getGroupId(), newGroupName, oldGroupName);
				countUpdatedFixture++;
				//System.out.println("Updating fixture"+" New Group Id : "+newGroupId + "Old Groupd Id : "+oldGroupId);
			}
		}
    	
    	oStatus.setMsg(countUpdatedFixture + " Fixture/s Updated");
    	return oStatus;
    }
    
    @Path("delete")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteProfiles(List<ProfileGroups> groups) {
		m_Logger.debug("Profiles: " + groups.size());
		Iterator<ProfileGroups> itr = groups.iterator();
		Response oResponse = new Response();
		long profileId = 0;
		ProfileGroups profile = new ProfileGroups();
		while (itr.hasNext()) {
			profile = (ProfileGroups) itr.next();
			profileId = profile.getId();
			int status = 0;
			try {
				status = groupManager.deleteProfile(profileId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			oResponse.setStatus(status);
			oResponse.setMsg(String.valueOf(profileId)); 
		}
		return oResponse;
	}
    
    @Path("duplicatecheck/{profilename}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateProfile(@PathParam("profilename") String profilename) {
		Response oStatus = new Response();
		ProfileGroups group = groupManager.getGroupByName(profilename);
		if (group!=null && group.getId() > 0) {
			oStatus.setMsg(profilename);
			oStatus.setStatus(0);
			return oStatus;
		}
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
    
    @Path("checkprofileassociation/{groupId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkProfileAssociation(@PathParam("groupId") Long groupId) {
		Response oStatus = new Response();
		String emIdStr="";
		List<EmProfileMapping>  emProfileMappingList= emProfileMappingManager.getEMProfileMappingByUEMProfileId(groupId);
        if(emProfileMappingList!=null && emProfileMappingList.size()>0)
        {
        	Iterator<EmProfileMapping> itr= emProfileMappingList.iterator();
        	while(itr.hasNext())
        	{
        		EmProfileMapping emProfileMapping = itr.next();
        		EmInstance emInstance = emInstanceManager.getEmInstance(emProfileMapping.getEmId());
        		if(emInstance!=null)
        		emIdStr+=emInstance.getName()+",";
        	}
        	emIdStr = emIdStr.substring(0, emIdStr.lastIndexOf(","));
        	oStatus.setMsg(emIdStr);
			oStatus.setStatus(-1);
			return oStatus;
        }
		oStatus.setMsg(emIdStr);
		oStatus.setStatus(0);
		return oStatus;
	}
    
    @Path("PushNewProfileToEM/groupId/{groupId}/floorId/{floorId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EMProfile PushNewProfileToEM(@PathParam("groupId") Long groupId,@PathParam("floorId") Long floorId)
    {
        EMProfile eMProfile = profileSyncManager.PushNewProfileToEM(groupId,floorId);
        return eMProfile; 
    }
   
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private Methods for UEM Profile Management
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the List of all derived EM Profiles
     * 
     * @return List<EmProfileList> - List of EmProfileList VO present on EM
     */
    @Path("getAllDerivedEMProfiles")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmProfileList> getAllDerivedEMProfiles() {
        Long emId=(long) 0;
        //Download For ALL Mapped EM Instances
        return groupManager.getAllDerivedEMProfiles(emId);
    }
    /**
     * Download the Profiles and Templates
     * 
     * @return Response
     */
    @Path("downloadEMProfileToUEM")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response downloadEMProfileToUEM() {
        Response response = new Response();
        //Download For ALL Mapped EM Instances
        Long emId=(long) 0;
        profileSyncManager.downloadDerivedEMTemplatesToUEM(emId);
        profileSyncManager.downloadDerivedEMProfilesToUEM(emId);
        return response;
    }
    /**
     * Initiate the profile sync to EM
     * 
     * @return Response
     */
    @Path("initiateProfileSyncToEm")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response initiateProfileSyncToEm()
    {
        Response r =   profileSyncManager.syncProfileGroupsToEM();
        return r;
    }
    /**
     * Get the UEM profile Handler By Id
     * 
     * @return ProfileHandler - Profile Handler object will be returned
     */
    @Path("getProfileHandlerFromUEM/{id}")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ProfileHandler getUpdateProfileHandler(@PathParam("id") Long id )
    {
        return groupManager.fetchProfileHandlerById(id);
    }
    
    /**
     * Create dummy profiles - Used for Load Testing
     * 
     * @return Response - total profiles created
     */
    @Path("createCustomGroups/from/{fromCount}/to/{toCount}/template/{templateId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createCustomGroups(@PathParam("fromCount") int fromCount,@PathParam("toCount") int toCount,@PathParam("templateId") Long templateId) {
        Response response = new Response();
        Long currentTime = System.currentTimeMillis();
        int updatedCount = profileSyncManager.createCustomGroups(fromCount, toCount, templateId);
        Long endTime = System.currentTimeMillis();
        Long totalTimeTaken = (endTime - currentTime)/(1000 * 60);
        response.setStatus(updatedCount);
        response.setMsg("Profiles Created in " + totalTimeTaken + " mins");
        return response;
    }
}
