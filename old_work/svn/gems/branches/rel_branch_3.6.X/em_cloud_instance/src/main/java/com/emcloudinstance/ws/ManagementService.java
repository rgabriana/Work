package com.emcloudinstance.ws;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.emcloudinstance.util.DatabaseUtil;

@Component
@Path("/org/manage")
public class ManagementService {
	
	@Resource 
	DatabaseUtil databaseUtil;

	
	@Path("createDatabase")
	@POST
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public void createDatabase(String name) {
		databaseUtil.createDatabase(name);
	}
	
	
}
