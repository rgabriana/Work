package com.emscloud.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.emscloud.model.EmInstance;
import com.emscloud.service.EmInstanceManager;

@Controller
@Path("/org/eminstance")
public class EmInstanceService {
	@Resource
	EmInstanceManager		emInstanceManager;

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    EmInstanceService()
	{
		
	}

	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceList(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadallEmInstances();
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		return instances ;
    	}
    	return null ;
    }

	@Path("listbycutomerid/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByCustomerId(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadEmInstancesByCustomerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		return instances ;
    	}
    	return null ;
    }

	@Path("delete/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
}
