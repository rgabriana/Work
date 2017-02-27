package com.emscloud.ws;

import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
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

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.util.UTCConverter;

@Controller
@Path("/org/eminstance")
public class EmInstanceService {
	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	EmStatsManager		emStatsManager;
	
	@Resource
	EmStateManager emStateManager;

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
	
	@Path("listEmInstancesByReplicaServerId/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByReplicaServerId(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadEmInstanceByReplicaServerId(id);
    	
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
			@FormParam("sord") String orderway) throws ParseException {
    	EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
    	String timeZone = emInstance.getTimeZone();
    	
    	EmStatsList oEmStatsList = emInstanceManager.loadEmStatsListByEmInstanceId(id,orderway, (page - 1) * EmStatsList.DEFAULT_ROWS,
				EmStatsList.DEFAULT_ROWS);
		oEmStatsList.setPage(page);
		List<EmStats> emstatList = oEmStatsList.getEmStats();
		if(emstatList !=null){
			for(EmStats emStats :emstatList){
				emStats.setUtcCaptureAt(UTCConverter.getUTCTimestamp(emStats.getCaptureAt(), timeZone));
			}
			
			
		}
		oEmStatsList.setEmStats(emstatList);
		return oEmStatsList;
    }
	
	
	@Path("loademinstbycustomerid/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceListByCustomerId(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	//EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
    	//String timeZone = emInstance.getTimeZone();
    	
    	EmInstanceList oEmInstList = emInstanceManager.loadEmInstancesListByCustomerId(id, orderway, (page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oEmInstList.setPage(page);
		List<EmInstance> emInstList = oEmInstList.getEmInsts();
		if(emInstList !=null && !emInstList.isEmpty()){
			for(EmInstance emInstance :emInstList){
				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));
				EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setLastConnectivityAt(latestEmStats.getCaptureAt());
    				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(latestEmStats.getCaptureAt(), emInstance.getTimeZone()));
        			emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
    			}else{
    				emInstance.setLastConnectivityAt(null);
        		}
    			EmState emState = emStateManager.loadLastEmStatsByEmInstanceId(emInstance.getId());
    			if (emState != null){
    				if (emState.getEmStatus() == EmStatus.CALL_HOME){
    					emInstance.setSyncConnectivity("NA");
    				}else{
	    					if (emState.getDatabaseState() != DatabaseState.SYNC_READY){
	    						emInstance.setSyncConnectivity(emState.getDatabaseState().getName());
	    					}
	    					else{
	    						if(emState.getLog() == null){
	    							emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName());
	    						}else{
	    							emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName()+" "+emState.getLog());
	    						}
	    					}
    				}
    				
        		}else{
        			emInstance.setSyncConnectivity("NA");
        		}
    			
			}
		}
		else {
			emInstList = new ArrayList<EmInstance>();
		}
		oEmInstList.setEmInsts(emInstList);
		return oEmInstList;
    }
	
	@Path("listbycutomerid/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByCustomerId(@PathParam("id") Long id) throws ParseException {
    	List<EmInstance> instances = emInstanceManager.loadEmInstancesByCustomerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		for(EmInstance emInstance : instances){
    			EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setLastConnectivityAt(latestEmStats.getCaptureAt());
    				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(latestEmStats.getCaptureAt(), "UTC"));
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
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceUnRegList(@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	
    	EmInstanceList oEmInstList = emInstanceManager.loadUnRegEmInstances(orderway, (page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oEmInstList.setPage(page);
		List<EmInstance> emInstList = oEmInstList.getEmInsts();
		if(emInstList !=null && !emInstList.isEmpty()){
			for(EmInstance emInstance :emInstList){
				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTime(emInstance.getLastConnectivityAt()));
			}
		}
		else {
			emInstList = new ArrayList<EmInstance>();
		}
		oEmInstList.setEmInsts(emInstList);
		return oEmInstList;
    }

	@Path("delete/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
	
	
	@Path("deleteUnRegEmInstance/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteUnRegEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
}
