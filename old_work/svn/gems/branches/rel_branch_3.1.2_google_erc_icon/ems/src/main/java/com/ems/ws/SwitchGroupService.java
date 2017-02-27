package com.ems.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GemsGroup> getSwitchGroups(@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(URLDecoder.decode(cookie, "UTF-8"));
        List<GemsGroup> switchGroups =  switchManager.loadSwitchGroupsByFloor(cookieHandler.getFacilityId());
        return switchGroups;
    }
	
}
