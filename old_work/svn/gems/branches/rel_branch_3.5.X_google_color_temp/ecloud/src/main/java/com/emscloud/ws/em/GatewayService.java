package com.emscloud.ws.em;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.emscloud.service.GatewayManager;
import com.emscloud.communication.vos.Gateway;
import com.emscloud.util.Response;



@Controller
@Path("/org/gateway/")
public class GatewayService {
	

	@Resource
	GatewayManager gatewayManager;
	
	
	/**
     * Returns gateway list
     * 
     * @param property
     *            (company|campus|building|floor)
     * @param pid
     *            property unique identifier
     * @return gateway list for the property level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Gateway> getGatewayList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        return gatewayManager.getGatewayList(property,pid);
    }
    
    /**
     * Returns gateway Details
     * 
     * @param gid
     *            gateway unique identifier
     * @return gateway details
     */
    @Path("details/{gid}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Gateway getGatewayDetails(@PathParam("gid") Long gid,@PathParam("pid") Long pid) {
        return gatewayManager.getGatewayDetails(pid,gid);
    }
    
    /**
     * Sends a realtime command to selected gateway. Generally over mouse over
     * 
     * @param gateways
     *            List of gateways
     *            "<gateways><gateway><id>1</id><ipaddress>169.254.0.100</ipaddress></gateway></gateways>"
     * @return Response status
     */
    @Path("op/realtime/{pid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getGatewayRealTimeStats(@PathParam("pid") Long pid,List<Gateway> gateways) {
        return gatewayManager.getGatewayRealTimeStats(pid, gateways);
    }

}
