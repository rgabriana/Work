/**
 * 
 */
package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.Gateway;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.GatewayInfo;
import com.ems.server.ServerMain;
import com.ems.service.GatewayManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/gateway")
public class GatewayService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public GatewayService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Returns gateway list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @return gateway list for the property level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Gateway> getGatewayList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("company")) {
            return gatewayManager.loadAllGatewaysWithActiveSensors();
        } else if (property.equalsIgnoreCase("campus")) {
            return gatewayManager.loadCampusGatewaysWithActiveSensors(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return gatewayManager.loadBuildingGatewaysWithActiveSensors(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return gatewayManager.loadFloorGatewaysWithActiveSensors(pid);
        } else if (property.equalsIgnoreCase("area")) {
            // TODO: Need to update gateway model to support area, for now send
            // floor data
            return gatewayManager.loadFloorGatewaysWithActiveSensors(pid);
        }
        return null;
    }

    /**
     * Returns Commisioned gateway list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @return gateway list for the property level
     */
    @Path("commissioned/list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Gateway> getCommissionedGatewayList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("company")) {
            return gatewayManager.loadAllCommissionedGateways();
        } else if (property.equalsIgnoreCase("campus")) {
            return gatewayManager.loadCommissionedCampusGateways(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return gatewayManager.loadCommissionedBuildingGateways(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return gatewayManager.loadCommissionedFloorGateways(pid);
        } else if (property.equalsIgnoreCase("area")) {
            // TODO: Need to update gateway model to support area, for now send
            // floor data
            return gatewayManager.loadCommissionedFloorGateways(pid);
        }
        return null;
    }
    /**
     * Returns gateway Details
     * 
     * @param gid
     *            gateway unique identifier
     * @return gateway details
     */
    @Path("details/{gid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Gateway getGatewayDetails(@PathParam("gid") Long gid) {
        return gatewayManager.loadGatewayWithActiveSensors(gid);
    }

    /**
     * Commission's gateway
     * 
     * @param gateway
     * @return response
     */
    @Path("commission")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response commissionGateway(Gateway gateway) {
        Response response = new Response();
        if (!gatewayManager.commission(gateway)) {
            response.setStatus(1);
            response.setMsg("Failed to commission gateway");
        }
        return response;
    }
    
    /**
     * Performs gateway's post commissioning steps 
     * @param gateway
     * @return Response status
     */
    @Path("postcommission")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response performAfterCommissionOperations(Gateway gateway) {
        Response response = new Response();
        if (!gatewayManager.performAfterCommissionSteps(gateway)) {
            response.setStatus(1);
            response.setMsg("Gateway commissioned successfully, but failed to perform post commissioning steps!");
        }
        return response;
    }

    /**
     * Checks whether the given gateway is commissioned.
     * 
     * @param gid
     *            gateway unique identifier
     * @return Response status
     */
    @Path("iscommissioned/{gid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response isCommissioned(@PathParam("gid") Long gid) {
        Response response = new Response();
        if (!gatewayManager.isCommissioned(gid)) {
            response.setStatus(1);
        }
        return response;
    }

    /**
     * Decommission's gateway from GEMS
     * 
     * @param gateway
     *            "<gateway><id>1</id></gateway>"
     * @return Response status, 1 indicates success.
     */
    @Path("decommission")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deCommissionGateway(Gateway gateway) {
        Response response = new Response();
        String gatewayName = "";   
        //Let's find the name of gateway from cache. LEt's put this in try/catch as failure
        //to find the name should not stop decommission.
        try{
        GatewayInfo cachedGatewayInfo = ServerMain.getInstance().getGatewayInfo(gateway.getId());
        if(cachedGatewayInfo != null){
        	if(cachedGatewayInfo.getGw() != null){
        		 gatewayName = cachedGatewayInfo.getGw().getGatewayName(); 
        	}
        }
        }catch(Exception e){
        	e.printStackTrace();
        }
        response.setStatus(gatewayManager.deleteGateway(gateway.getId()));
        userAuditLoggerUtil.log("Decommission gateway "+ gatewayName, UserAuditActionType.Gateway_Commission.getName());
        return response;
    }

    /**
     * Sends a realtime command to selected gateway. Generally over mouse over
     * 
     * @param gateways
     *            List of gateways
     *            "<gateways><gateway><id>1</id><ipaddress>169.254.0.100</ipaddress></gateway></gateways>"
     * @return Response status
     */
    @Path("op/realtime")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getGatewayRealTimeStats(List<Gateway> gateways) {
        m_Logger.debug("Gateway: " + gateways.size());
        Iterator<Gateway> itr = gateways.iterator();
        while (itr.hasNext()) {
            Gateway gateway = (Gateway) itr.next();
            gatewayManager.getRealtimeStatsByGWId(gateway.getId());
        }
        return new Response();
    }

    /**
     * Updates the position of the selected fixtures on the floorplan
     * 
     * @param gateways
     *            List of selected gateway with their respective x & y co-ordinates
     *            "<gateways><gateway><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></gateway></gateways>"
     * @return Response status
     */
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGatewayPosition(List<Gateway> gateways) {
        m_Logger.debug("Gateways: " + gateways.size());
        Iterator<Gateway> itr = gateways.iterator();
        StringBuffer gatewayNames = new StringBuffer("");
        boolean log = false;
        while (itr.hasNext()) {
            Gateway gateway = (Gateway) itr.next();
            if (gateway.getXaxis() != null && gateway.getYaxis() != null) {
                gatewayManager.updateGatewayPosition(gateway);
                gatewayNames.append(gatewayManager.loadGateway(gateway.getId()).getGatewayName() + "(X:" + gateway.getXaxis() + " Y:" + gateway.getYaxis() + ") ");
                log = true;
            }
        }
        if (log) {
        	userAuditLoggerUtil.log("Update gateway positions for " + gatewayNames, UserAuditActionType.Gateway_Update.getName());
        }
        return new Response();
    }

    /**
     * Updates the noOfSensors field of selected gateway
     * 
     * @param gatewayId
     *            gateway unique identifier
     * @param noOfSensors
     *            updated value of sensors
     * @return Response status
     */
    @Path("du/updatenoofsensors/gateway/{gatewayId}/noofsensors/{noOfSensors}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGatewaynoOfSensors(@PathParam("gatewayId") Long gatewayId,
            @PathParam("noOfSensors") int noOfSensors) {
        Gateway gw = gatewayManager.loadGateway(gatewayId);
        gw.setNoOfSensors(noOfSensors);
        gatewayManager.updateFields(gw);
        return new Response();
    }

}
