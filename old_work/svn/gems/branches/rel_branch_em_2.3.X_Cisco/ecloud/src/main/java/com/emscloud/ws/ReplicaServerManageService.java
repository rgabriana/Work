package com.emscloud.ws;

import java.util.List;

import javax.annotation.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.model.ReplicaServer;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.ReplicaServerManager;

@Controller
@Path("/org/replicaservermanage")
public class ReplicaServerManageService {
	Logger logger = Logger.getLogger(ReplicaServerManageService.class) ;
	@Resource
	ReplicaServerManager	replicaServerManager;
	@Resource
	EmInstanceManager emInstanceManager;
	@Resource
	EmStateManager emStateManager;
	
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ReplicaServer> getReplicaServerList() {
    	List<ReplicaServer> replicaServers = replicaServerManager.getAllReplicaServers();
    	
    	if(replicaServers != null && !replicaServers.isEmpty())
    	{
    		return replicaServers ;
    	}
    	return null ;
    }
	
	@Path("delete/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteReplicaServer(@PathParam("id") Long id) {
		
		replicaServerManager.delete(id);
		
		return;
    }
	
	@Path("duplicate/add/{name}/{ip}/{internalIp}/{uid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String checkDuplicateValues(@PathParam("name") String name,@PathParam("ip") String ip,@PathParam("internalIp") String internalIp,@PathParam("uid") String uid) {
    	String duplicateField = "none";
    	if (replicaServerManager.getReplicaServersbyName(name) != null){
    		duplicateField = "name";
    	}else if (replicaServerManager.getReplicaServersbyIp(ip) != null){
    		duplicateField = "ip";
    	}else if (replicaServerManager.getReplicaServersbyInternalIp(internalIp) != null){
    		duplicateField = "internalIp";
    	}else if (replicaServerManager.getReplicaServersbyUid(uid) != null){
    		duplicateField = "uid";
    	}
    	return duplicateField ;
    }
	
	
	@Path("duplicate/edit/{id}/{name}/{ip}/{internalIp}/{uid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String checkDuplicateEditValues(@PathParam("id") Long id,@PathParam("name") String name,@PathParam("ip") String ip,@PathParam("internalIp") String internalIp,@PathParam("uid") String uid) {
    	String duplicateField = "none";
    	
    	ReplicaServer replicaServer = replicaServerManager.getReplicaServersbyId(id);
    	if(!replicaServer.getName().equals(name)){
    		if(replicaServerManager.getReplicaServersbyName(name) != null){
    			duplicateField = "name";
    			return duplicateField ;
    		}
    	}
    	
    	if(!replicaServer.getIp().equals(ip)){
    		if(replicaServerManager.getReplicaServersbyIp(ip) != null){
    			duplicateField = "ip";
    			return duplicateField ;
    		}
    	}
    	
    	if(!replicaServer.getInternalIp().equals(internalIp)){
    		if(replicaServerManager.getReplicaServersbyInternalIp(internalIp) != null){
    			duplicateField = "internalIp";
    			return duplicateField ;
    		}
    	}
    	
    	if(!replicaServer.getUid().equals(uid)){
    		if(replicaServerManager.getReplicaServersbyUid(uid) != null){
    			duplicateField = "uid";
    			return duplicateField ;
    		}
    	}
    	
    	return duplicateField ;
    }
	
}
