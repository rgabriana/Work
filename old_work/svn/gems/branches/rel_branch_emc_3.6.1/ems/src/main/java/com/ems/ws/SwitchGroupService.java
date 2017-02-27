package com.ems.ws;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.GemsGroup;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.service.SwitchManager;

@Controller
@Path("/org/switchgroups")
public class SwitchGroupService {
	
	@Resource
    SwitchManager switchManager;
	
	/**
	 * Get list of switch groups
	 * @return switch groups list for the property level
	 * @throws UnsupportedEncodingException 
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor')")
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroup> getSwitchGroups(@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        List<GemsGroup> switchGroups =  switchManager.loadSwitchGroupsByFloor(cookieHandler.getFacilityId());
        return switchGroups;
    }
	
	/**
	 * Return Switch Groups
	 * 
	 * @param property
	 *            (company|building|campus|floor)
	 * @param pid
	 *            property unique identifier
	 * @return Switch Group list  for the selected floor or EM level
	 */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("list/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GemsGroup> getSwitchGroupByFacility(@PathParam("property") String property,@PathParam("pid") Long pid) {
		List<GemsGroup> switchGroups = new ArrayList<GemsGroup>(); 
		switchGroups=switchManager.getSwitchGroupByFacility(property,pid);
		if(switchGroups==null)
		{
			return new ArrayList<GemsGroup>();
		}
	    return switchGroups;
	} //end of method getSwitchScenes   
}
