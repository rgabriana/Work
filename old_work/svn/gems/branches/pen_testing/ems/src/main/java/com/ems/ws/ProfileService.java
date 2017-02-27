/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.Tenant;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.ProfileManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.AssignFixture;
import com.ems.vo.AssignFixtureList;
import com.ems.vo.EMProfile;
import com.ems.ws.util.Response;
import com.ems.ws.util.WebServiceUtils;

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

    @Autowired
	private MessageSource messageSource;
    @Resource
    private SystemConfigurationManager systemConfigurationManager;
    
    @Resource(name = "emsAuthContext")
   	private EmsAuthenticationContext emsAuthContext;
    @Resource
   	private UserManager userManager;
    @Resource(name="profileTemplateManager")
    ProfileTemplateManager profileTemplateManager;
    @Resource(name="metaDataManager")
    MetaDataManager metaDataManager;
    @Resource(name="companyManager")
    CompanyManager companyManager;
    @Resource
    ProfileManager profileManager;
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response assignProfile(@PathParam("currentprofile") String currentProfile,
            @PathParam("originalprofile") String originalProfile, @PathParam("groupid") Long groupId, Fixture fixture) {
        Response oStatus = new Response();
        if (fixture != null) {
        	
        	/*Map<String,Object> nameValMap = new HashMap<String,Object>();
        	nameValMap.put("profileName", currentProfile);
        	nameValMap.put("originalProfile", originalProfile);
        	oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        	if(oStatus!= null && oStatus.getStatus()!=200){
        		return oStatus;
        	}*/
        	
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
     * Bulk Assign new profile to the list of selected fixtures
     * 
     * @param fixtureList
     *            fixture "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
     * @param groupId
     *            Id of the selected group to be assigned
     * @param currentProfile
     *         	  Name of the selected profile
     * @return Response totalRecordUpdated
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("bulkassign/{currentprofile}/{groupid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response bulkProfileAssign(@PathParam("currentprofile") String currentProfile,@PathParam("groupid") Long groupId, List<Fixture> fixtureList) {
        Response oStatus = new Response();
        Long totalRecordUpdated = null;
        String fixtureIdsList="";
        /*oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "profileName", currentProfile);
    	if(oStatus!= null && oStatus.getStatus()!=200){
    		return oStatus;
    	}*/
        List<Long> fixtureIds  = new ArrayList<Long>();
		for (Iterator<Fixture> iterator = fixtureList.iterator(); iterator.hasNext();) {
			Long id = new Long(iterator.next().getId() + "");
			fixtureIdsList+=id;
			 if (iterator.hasNext()) {   
				  fixtureIdsList += ",";    
			  } 
			fixtureIds.add(id);
		}
        if (fixtureIds != null && !fixtureIds.isEmpty()) {
            m_Logger.debug("Assinging profile: '" + currentProfile + "' to (" + fixtureIdsList + ") fixtures");
            totalRecordUpdated = fixtureManager.bulkProfileAssignToFixture(fixtureIdsList, groupId, currentProfile);
        }
        userAuditLoggerUtil.log("Assign profile " + currentProfile + " to fixtures " + fixtureIdsList, UserAuditActionType.Fixture_Profile_Update.getName());
        oStatus.setStatus(totalRecordUpdated.intValue());
        return oStatus;
    }

    /**
     * Get the list of all the groups in system
     * 
     * @return Groups list
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
    @Path("assignlist/{pid}")
    @POST    
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public AssignFixtureList getAllProfiles(@PathParam("pid") Long pid, @FormParam("page") Integer page,@FormParam("selectedfixtures") String selectedfixtures) {    	
    	AssignFixtureList returnList = new AssignFixtureList();
    	int x = AssignFixtureList.DEFAULT_ROWS;
    	Response resp = new Response();
    	
    	/*Map<String,Object> nameValMap = new HashMap<String,Object>();
//    	nameValMap.put("id", pid);
    	nameValMap.put("page", page);
    	nameValMap.put("selectedfixtures", selectedfixtures);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
    	if(resp!= null && resp.getStatus()!=200){
    		m_Logger.error("Validation error"+resp.getMsg());
    		return new AssignFixtureList();
    	}*/
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
    /**
     * Change the Profiles within the given templates list for all selected fixtures. 
     * This service will be called from floorplan assign profile menu option for Employee role user
     *    
     * @param fixtureList
     *            fixture "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
     * @return Response message showing "{countUpdatedFixture} Fixture/s Updated"
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
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
			}
		}
    	
    	oStatus.setMsg(countUpdatedFixture + " Fixture/s Updated");
    	return oStatus;
    }
    /**
     * Delete the profile for the selected id
     * 
     * @param List of groups in the format 
     *          <profilegroups><profilegroup><id>{profile id}</id></profilegroup></profilegroups>
     *           
     * @return Response - If profile deleted successful, response status = successfully deleted profiles count other wise 0
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
    /**
     * Check for duplicate profile name.
     * 
     * @param profilename
     * @return Response object - If Duplicate, response object will return profile name otherwise returns 0
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("duplicatecheck/{profilename}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateProfile(@PathParam("profilename") String profilename) {
		Response oStatus = new Response();
		oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "profileName", profilename);
		if(oStatus!= null && oStatus.getStatus()!=200){
			m_Logger.error("Validation error"+oStatus.getMsg());
    		return oStatus;
    	}
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
    /**
     * Fetch all derived profile (including default 16 profile) to sent to UEM
     * 
     * @return List of EMProfile
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getallderivedemprofiles")
   	@GET
   	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	public List<EMProfile> getAllDerivedEMProfiles() {
        List<EMProfile> groupsList = new ArrayList<EMProfile>();
    	List<Groups> derivedProfile = groupManager.getAllNonDefaultDerivedProfile();
   		Iterator<Groups> itr = derivedProfile.iterator();
   		while (itr.hasNext()) {
   			Groups grp = (Groups) itr.next();
   			EMProfile emProfile = new EMProfile();
   			emProfile.setId(grp.getId());
   			emProfile.setName(grp.getName());
   			emProfile.setProfileNo(grp.getProfileNo());
   			emProfile.setDerivedFromGroup(grp.getDerivedFromGroup() != null ? grp.getDerivedFromGroup().getId() : -1);
   			emProfile.setProfileTemplate(grp.getProfileTemplate() != null ? grp.getProfileTemplate().getId() : -1);
  			ProfileHandler derivedProfileHandler = groupManager.fetchProfileHandlerById(grp.getProfileHandler().getId());
   			emProfile.setProfileHandler(derivedProfileHandler);
   			groupsList.add(emProfile);
   		}
   		return groupsList;
   }
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getEMDerivedProfileHandlerByGroup/{grpId}")
   	@GET
   	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	public ProfileHandler getAllDerivedEMProfileHandler(@PathParam("grpId") Long grpId) {
    	Response oStatus = new Response();
    	ProfileHandler profileHandler = null;
		/*oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "id", grpId);
		if(oStatus!= null && oStatus.getStatus()!=200){
    		return profileHandler;
    	}*/
    	return groupManager.getProfileHandlerById(grpId);
   }
    
    /**
     * Update Profile coming from UEM to EM
     * @param List<EMProfile> - List of EMProfile VO coming from UEM
     * @return Response - Status of Profile Update
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("updateemprofiles")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateEMProfiles(List<EMProfile> emProfiles) {
        Response response = groupManager.updateEMProfiles(emProfiles);
        return response;
    }
    
    /**
     * Save new Profile coming from UEM to EM - This is totally new profile that need to be created at EM and then pushed 
     * to SU if any fixture are associated with it.
     * 
     * @param EMProfile - EMProfile VO coming from UEM
     * @return EMProfile - Saved EMProfile VO
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("pushnewprofiletoem")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EMProfile PushNewProfileToEM(EMProfile emProfile) {
        // Create New Groups Here and Store it into database
        EMProfile reponse = groupManager.PushNewProfileToEM(emProfile);
        return reponse; 
    }
   
    /**
     * Returns next available profile_no in EM groups
     * @return Response - next available max valid Profile number
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getmaxprofileno")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getMaxProfileNo() {
   	 	Long tenantId=null;
   	 	Response res = new Response();
        Short profileNo = groupManager.getMaxProfileNo(tenantId);
        res.setMsg(profileNo.toString());
        return res;
    }
    
    /**
     * Export Profile in xml format
     * 
     * @param GroupId - Groups Id to be exported
     * @return XML - XML Response
     */
    @GET
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("/exportprofile/{groupId}")
    @Produces("application/xml")
    public javax.ws.rs.core.Response exportProfiles(@PathParam("groupId") Long groupId) {
        Groups dbGroup = groupManager.getGroupById(groupId);
        String fileName = dbGroup.getName();//.replaceAll(" ", ".");
        if(dbGroup.isDefaultProfile()==true)
        {
        	fileName+="_Default";
        }
        ProfileHandler ph = null;
        try {
            ph = groupManager.fetchProfileHandlerById(dbGroup.getProfileHandler().getId());
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
     * Export All Profile in in zip file
     * 
     * @param templateId - To get all profiles under given templateid
     * @return ZIP - Zip Document
     */
    @GET
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("/exportallprofile/{templateId}")
    @Produces("application/zip")
    public javax.ws.rs.core.Response exportAllProfiles(@PathParam("templateId") Long templateId) {
        List<Groups> profileList = groupManager.loadAllProfileTemplateById(templateId,0L);
        ProfileTemplate profileTemplate = profileTemplateManager.getProfileTemplateById(templateId);
        String templateFileName = profileTemplate.getName().replaceAll(" ", "_");
        
        String reportDirname = "/tmp/" + profileTemplate.getId() + "_" + System.currentTimeMillis();
        File oDir = new File(reportDirname);
        oDir.mkdirs();
        Iterator<Groups> itr = profileList.iterator();
        while (itr.hasNext()) {
            Groups grp = (Groups) itr.next();
            String profileName = grp.getName().replaceAll(" ", "_"); 
            ProfileHandler ph = null;
          try {
              ph = groupManager.fetchProfileHandlerById(grp.getProfileHandler().getId());
          } catch (Exception e) {
              m_Logger.error(e.getMessage(), e);
          }
          String outPutXMLString = WebServiceUtils.convertModelToString(ph);
          writeReportFile(profileName, "", reportDirname + File.separator + profileName + ".xml", outPutXMLString);
        }
        String strRptName = templateFileName + "_profiles" + ".zip";
        File oRptFile = new File("/tmp/" + strRptName);
        writeZipFile(oDir, strRptName);
        oDir.delete();
        return javax.ws.rs.core.Response
                .ok(oRptFile, "application/zip")
                .header("Content-Disposition",
                        "attachment; filename =" + strRptName).build(); 
    }

    public void writeReportFile(String site, String em, String reportName, String data) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(reportName)));
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            m_Logger.error(e.getMessage(), e);
        }
    }
    private void writeZipFile(File directoryToZip, String sRptName) {

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/" + sRptName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            File[] files = directoryToZip.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            m_Logger.error(e.getMessage(), e);
        } catch (IOException e) {
            m_Logger.error(e.getMessage(), e);
        }
    }

    private void addToZip(File directoryToZip, File file,
            ZipOutputStream zos) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(
                directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        //System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
        file.delete();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private Method used for profile sync testing
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Retrieve profiles handler object for the given id.
     * 
     * @param id - profile handler id
     * @return ProfileHandler object
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("inspect/getProfileHandler/{id}")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ProfileHandler getUpdateProfileHandler(@PathParam("id") Long id )
    {
    	Response oStatus = new Response();
    	ProfileHandler profileHandler = null;
		/*oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "id", id);
		if(oStatus!= null && oStatus.getStatus()!=200){
    		return profileHandler;
    	}*/
    	return groupManager.getProfileHandlerById(id);
    }
}
