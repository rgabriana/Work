package com.ems.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.Company;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupType;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.GemsGroupManager;
import com.ems.ws.util.Response;

/**
 * @author Shilpa Chalasani
 * 
 */
@Controller
@Path("/org/gemsgroups")
public class GemsGroupService {
    static final Logger logger = Logger.getLogger(GemsGroupService.class.getName());
    
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "gemsGroupManager")
    private GemsGroupManager gemsGroupManager;
    @Resource(name = "companyManager")
    private CompanyManager companyManager;

    public GemsGroupService() {
    }

    /**
     * Returns groups list
     * 
     * @param pid
     *            property unique identifier
     * @return fixture list for the property level
     */
    @Path("grouptypelist")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroupType> getGroupTypes() {
        return gemsGroupManager.getGroupTypeList();
    }

    /**
     * Created new group for an organization
     * 
     * @param GemsGroupType
     *            xml or json notation <gemsGroupType><id>1</id> <name>Motion Group</name>
     *            <groupnumber>10001</groupnumber> </gemsGroupType>
     * 
     * @return Response status
     */
    @Path("op/creategrouptype")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createNewGroupType(GemsGroupType gemsGroupType) {
        gemsGroupManager.saveGemsGroupType(gemsGroupType);
        userAuditLoggerUtil.log("Create Group: " + gemsGroupType.getGroupTypeName());
        return new Response();
    }

    /**
     * Returns groups list
     * 
     * @param pid
     *            property unique identifier
     * @return fixture list for the property level
     */
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroup> getGroupsList() {
        return gemsGroupManager.loadGroupsByCompany(1L);
    }

    /**
     * returns GemsGroup object for the given group name
     * @param groupName
     * 
     * @return GemsGroup object
     */
    @Path("loadbyname/{groupName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public GemsGroup getGroupsByName(@PathParam("groupName")String groupName) {
        return gemsGroupManager.loadGroupsByGroupName(groupName);
    }
    
    /**
     * Create new group for an organization
     * 
     * @param gemsGroup
     *              <gemsGroup><id></id><name>Hall</name><type><id>1</id></type></gemsGroup>
     * @return Response status
     */
    @Path("op/creategroup")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createNewGroup(GemsGroup gemsGroup) {
        Company oCompany = companyManager.loadCompany();
        gemsGroup.setCompany(oCompany);
        GemsGroup oGemsGroup = gemsGroupManager.createNewGroup(gemsGroup);
        
        Response oResponse = new Response();
        oResponse.setMsg(oGemsGroup.getId().toString());
        userAuditLoggerUtil.log("Create Group: " + gemsGroup.getGroupName());
        return oResponse;
    }

    /**
     * Delete a group
     * 
     * @param gid
     *            Group ID that needs to be deleted
     * @return Response status
     */
    @Path("op/deletegroup/{gid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteGroup(@PathParam("gid") Long gid) {
        GemsGroup group = gemsGroupManager.loadGemsGroup(gid);
        gemsGroupManager.deleteGroup(group);
        userAuditLoggerUtil.log("Delete Group: " + group.getGroupName());
        return new Response();
    }
}
