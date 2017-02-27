package com.emscloud.ws;

import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.EmInstance;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStatsManager;

@Controller
@Path("/org/eminstance")
public class EmInstanceService {
	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	EmStatsManager		emStatsManager;

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

	@Path("listemstats/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmStats> getEmStatsByEmInstanceId(@PathParam("id") Long id) {
    	List<EmStats> emStatsList = emInstanceManager.loadEmStatsByEmInstanceId(id,0,10);
    	
    	if(emStatsList != null && !emStatsList.isEmpty())
    	{
    		return emStatsList ;
    	}
    	return null ;
    }
	
	@Path("listemstats/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmStats> getEmStatsByEmInstanceId(@PathParam("id") Long id,
    											  @FormParam("page") Integer page,
    											  @FormParam("sidx") String orderby,
    											  @FormParam("sord") String orderway) {
    	List<EmStats> emStatsList = emInstanceManager.loadEmStatsByEmInstanceId(id,page*10,10);
    	
    	if(emStatsList != null && !emStatsList.isEmpty())
    	{
    		return emStatsList ;
    	}
    	return null ;
    }
	
	@Path("loademstats/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmStatsList loadEmStatsListWithSpecificAttrs(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) {
    	
		EmStatsList oEmStatsList = emInstanceManager.loadEmStatsListByEmInstanceId(id,orderway, (page - 1) * EmStatsList.DEFAULT_ROWS,
				EmStatsList.DEFAULT_ROWS);
		if (oEmStatsList != null){
			oEmStatsList.setPage(page);
		}
		return oEmStatsList;
    }
	
	@Path("listbycutomerid/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByCustomerId(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadEmInstancesByCustomerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		for(EmInstance emInstance : instances){
    			EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setLastConnectivityAt(latestEmStats.getCaptureAt());
        			emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
    			}else{
    				emInstance.setLastConnectivityAt(null);
        		}
    			
    		}
    		
    		return instances ;
    	}
    	return null ;
    }
	
	@Path("listUnRegEmInstance")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getUnRegEmInstanceList() {
    	List<EmInstance> instances = emInstanceManager.loadUnRegEmInstances();
    	return instances;
    }

	@Path("delete/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
}
