package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;


import com.ems.model.GemsGroupPlugload;
import com.ems.mvc.util.UserAuditLoggerUtil;

import com.ems.service.GemsPlugloadGroupManager;


@Controller
@Path("/org/gemsgroupplugload")
public class GemsGroupPlugloadService {
	static final Logger logger = Logger.getLogger(GemsGroupService.class
			.getName());

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "gemsPlugloadGroupManager")
	private GemsPlugloadGroupManager gemsPlugloadGroupManager;


	public GemsGroupPlugloadService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Returns plugloads list
	 * 
	 * @param groupid
	 *            gems group unique identifier
	 * @return plugload list for the gems group
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/{groupId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GemsGroupPlugload> getGroupPlugloadList(
			@PathParam("groupId") Long groupId) {
		return gemsPlugloadGroupManager.getGemsGroupPlugloadByGroup(groupId);
	}
}
