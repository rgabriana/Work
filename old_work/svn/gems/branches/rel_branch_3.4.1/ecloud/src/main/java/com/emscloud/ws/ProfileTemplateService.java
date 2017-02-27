/**
 * 
 */
package com.emscloud.ws;

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

import com.emscloud.model.ProfileTemplate;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.service.ProfileTemplateManager;
import com.emscloud.vo.EmTemplateList;
import com.emscloud.communication.vos.Response;
/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profiletemplate")
public class ProfileTemplateService {

	@Resource(name = "profileGroupManager")
	private ProfileGroupManager groupManager;
	
	@Resource(name = "profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	

	public ProfileTemplateService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	@Path("duplicatecheck/{templatename}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateProfile(@PathParam("templatename") String templatename) {
		Response oStatus = new Response();
		if (profileTemplateManager
				.getProfileTemplateCountByName(templatename) > 0) {
			oStatus.setMsg(templatename);
			oStatus.setStatus(0);
			return oStatus;
					}
		
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
	/**
     * Returns profiles count associated with the templates
     * 
     * @param profiles
     * @param pid
     *            property unique identifier
     * @return Response status with msg used for sending profiles count
     */
    @Path("count/profiles/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getProfilesCount(@PathParam("pid") Long pid) {
        Response oResponse = new Response();
        oResponse.setMsg(String.valueOf(groupManager.getProfilesCount(pid)));
        return oResponse;
    }
    
    /**
     * Delete Template
     * 
     * @param id
     *            template unique identifier
     * @return Response status
     */
    @Path("delete")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteTemplate(List<ProfileTemplate> profileTemplate) {
        
        m_Logger.debug("Templates: " + profileTemplate.size());
        Iterator<ProfileTemplate> itr = profileTemplate.iterator();
        Response oResponse = new Response();
        long templateId = 0;
        ProfileTemplate template = new ProfileTemplate();
        while (itr.hasNext()) {
            template = (ProfileTemplate) itr.next();
            templateId = template.getId();
            int status = 0;
            try {                   
                status = profileTemplateManager.delete(templateId);
            } catch (Exception e) {
                e.printStackTrace();
                //userAuditLoggerUtil.log("Exception while deleting Profile Template: " + template.getName(), UserAuditActionType.Profile_Update.getName());
            }
            //userAuditLoggerUtil.log("Deleted Profile Template: " + template.getName(), UserAuditActionType.Profile_Update.getName());
            oResponse.setStatus(status);
            oResponse.setMsg(String.valueOf(templateId)); 
        }
        return oResponse;
        
    }
    /**
     * Get the List of all derived EM Templates
     * 
     * @return List<EmTemplateList> - List of EmTemplateList VO present on EM
     */
    @Path("getAllDerivedEMTemplates")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmTemplateList> getAllDerivedProfileTemplate() {
        Long emId=(long) 0;
        return profileTemplateManager.getAllDerivedProfileTemplate(emId);
    }
    
    /**
	 * Returns fixture count associated with the profiletemplate
	 * 
	 * @param pid
	 *            profiletemplate Id
	 * @return fixturecount
	 */
	@Path("fixturecount/profiletemplate/{ptId}")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	public String getFixtureCountByProfileTemplateId(@PathParam("ptId") Long ptId) {
		return profileTemplateManager.getFixtureCountByProfileTemplateId(ptId);
	}
	
	/**
     * Edit EM's Template Name
     * 
     * @param List of Templates
     *            
     * @return Response status
     */
    @Path("editEmTemplate")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editEmTemplate(List<ProfileTemplate> profileTemplate) {
        Response oResponse = new Response();
        oResponse= profileTemplateManager.editEmTemplate(profileTemplate);
        return oResponse;
    }
}
