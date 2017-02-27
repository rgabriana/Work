/**
 * 
 */
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.Tenant;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.vo.AssignFixture;
import com.ems.vo.AssignFixtureList;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profile")
public class ProfileService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "emsAuthContext")
   	private EmsAuthenticationContext emsAuthContext;
    @Resource
   	private UserManager userManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public ProfileService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Assign new profile to the list of selected fixture(s)
     * 
     * @param currentProfile
     *            Name of the selected profile
     * @param originalProfile
     *            Name of the original profile
     * @param groupId
     *            Id of the selected group to be assigned
     * @param fixture
     *            fixture "<fixture><id>1</id></fixture>"
     * @return Response status
     */
    @Path("assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response assignProfile(@PathParam("currentprofile") String currentProfile,
            @PathParam("originalprofile") String originalProfile, @PathParam("groupid") Long groupId, Fixture fixture) {
        Response oStatus = new Response();
        if (fixture != null) {
            m_Logger.debug("Assinging profile: '" + currentProfile + "' to (" + fixture.getId() + ") fixtures");
            fixtureManager.changeFixtureProfile(fixture.getId(), groupId, currentProfile, originalProfile);
        }
        String fixtureName = "";
        
        //Let's find the name of fixture from cache. Let's put this in try/catch as failure
        //to find the name should not stop decommission.
        try {
            if(FixtureCache.getInstance()
                    .getDevice(fixture.getId()) != null){
            fixtureName = FixtureCache.getInstance()
                    .getDevice(fixture.getId()).getFixtureName();
            }else{
                fixtureName = String.valueOf(fixture.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userAuditLoggerUtil.log("Assign profile " + currentProfile + " to fixture " + fixtureName, UserAuditActionType.Fixture_Profile_Update.getName());
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
    public List<Groups> getAllProfiles() {
        return groupManager.loadAllGroups();
    }
    
    /**
     * Get the list of all the groups in system
     * 
     * @return Groups list
     */
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
    
    @Path("changeassignlist")
    @POST 
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response setSelectedProfiles(List<Fixture> fixtureList) {
    	
    	Response oStatus = new Response();
    	String newGroupName = "";
    	Long newGroupId ;
    	String oldGroupName = "";
    	Long oldGroupId ; 
    	Integer countUpdatedFixture = 0;
    	for (Iterator<Fixture> iterator = fixtureList.iterator(); iterator.hasNext();) {
			Fixture newfixture = (Fixture) iterator.next();
			Fixture oldFixture = fixtureManager.getFixtureById(newfixture.getId());
			
			oldGroupId = oldFixture.getGroupId();
			Groups oldGroup = groupManager.getGroupById(oldGroupId);
			oldGroupName = oldGroup.getName();
			
			newGroupId = newfixture.getGroupId();
			Groups newGroup = groupManager.getGroupById(newGroupId);
			newGroupName = newGroup.getName();
			
			if(!newGroupId.equals(oldGroupId))
			{
				fixtureManager.changeFixtureProfile(newfixture.getId(), newfixture.getGroupId(), newGroupName, oldGroupName);
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
	public Response deleteProfiles(List<Groups> groups) {
		m_Logger.debug("Profiles: " + groups.size());
		Iterator<Groups> itr = groups.iterator();
		Response oResponse = new Response();
		long profileId = 0;
		Groups profile = new Groups();
		while (itr.hasNext()) {
			profile = (Groups) itr.next();
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
		Groups group = groupManager.getGroupByName(profilename);
		if (group!=null && group.getId() > 0) {
			oStatus.setMsg(profilename);
			oStatus.setStatus(0);
			return oStatus;
		}
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
}
