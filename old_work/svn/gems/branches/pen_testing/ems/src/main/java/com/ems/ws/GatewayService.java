/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.Gateway;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.GatewayInfo;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.service.GatewayManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.WdsManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
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
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;
    private static int REINIT_DELAY = 10 * 1000;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");
    private static Logger syslog = Logger.getLogger("SysLog");

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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Gateway> getGatewayList(@PathParam("property") String property, @PathParam("pid") Long pid) {
    	Response oResponse = new Response();
        oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(oResponse!= null && oResponse.getStatus()!=200){
        	m_Logger.error("Validation error"+oResponse.getMsg());
    		return null;
    	}
    	if (property.equalsIgnoreCase("company")) {
            return gatewayManager.loadAllGatewaysWithActiveSensors();
        } else if (property.equalsIgnoreCase("campus")) {
            return gatewayManager.loadCampusGatewaysWithActiveSensors(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return gatewayManager.loadBuildingGatewaysWithActiveSensors(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return gatewayManager.loadFloorGatewaysWithActiveSensors(pid,"fp");
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("commissioned/list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Gateway> getCommissionedGatewayList(@PathParam("property") String property, @PathParam("pid") Long pid) {
    	Response oResponse = new Response();
        oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(oResponse!= null && oResponse.getStatus()!=200){
        	m_Logger.error("Validation error"+oResponse.getMsg());
    		return null;
    	}
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("commission")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response commissionGateway(Gateway gateway) {
        Response response = new Response();
        if (!gatewayManager.commission(gateway)) {
            response.setStatus(1);
            response.setMsg("Failed to commission gateway, please retry.");
        }
        return response;
    }
    
    /**
     * Performs gateway's post commissioning steps 
     * @param gateway
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("decommission")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deCommissionGateway(Gateway gateway) {
        Response response = new Response();
        Boolean isComissioned = gatewayManager.isCommissioned(gateway.getId());
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
        
        // Delete the WDSs which are discovered but not commissioned. Because if one of them is associated with this gateway then
        // the gateway will fail to delete.
        wdsManager.deleteDiscoverdWds();
        
       int status = gatewayManager.deleteGateway(gateway.getId()) ;
        response.setStatus(status);
        if(status==1)
        {
        	if(isComissioned)
        	{
        		userAuditLoggerUtil.log("Gateway "+ gatewayName + " decommissioned and deleted", UserAuditActionType.Gateway_Commission.getName());
        	}else
        	{
        		userAuditLoggerUtil.log("Uncommissioned Gateway "+ gatewayName + " deleted", UserAuditActionType.Gateway_Commission.getName());
        	}
        }
        else
        {
        	m_Logger.error("Error Deleteing gateway" + gatewayName ) ;
        }
        return response;
    }
    
    /**
	 * Service to do RMA replacement
	 * @param fromGatewayId - Gateway which needs to be replaced
	 * @param toGatewayId - New Gateway
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("rma/{from}/{to}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rmaGateway(@PathParam("from")Long fromGatewayId, @PathParam("to")Long toGatewayId) {
		boolean commissionStatus = gatewayManager.rmaGateway(fromGatewayId, toGatewayId);
        //cleanup
		int deleteStatus = 0;
		boolean boolDeleteStatus = false;
		
		if(commissionStatus)
		{
			gatewayManager.invalidateFixtureCache(toGatewayId);	
			
			Gateway gw = gatewayManager.loadGateway(fromGatewayId);
			deleteStatus = deCommissionGateway(gw).getStatus();			
		}
		
		if(deleteStatus == 1)
			boolDeleteStatus = true;
			
        Response oResponse = new Response();
                
        if(commissionStatus && boolDeleteStatus){
            oResponse.setStatus(0);
            oResponse.setMsg("RMA successful");
        }else{
            oResponse.setStatus(1);
            oResponse.setMsg("RMA unsuccessful");
        }      
        return oResponse;        
    }

    /**
     * Sends a realtime command to selected gateway. Generally over mouse over
     * 
     * @param gateways
     *            List of gateways
     *            "<gateways><gateway><id>1</id><ipaddress>169.254.0.100</ipaddress></gateway></gateways>"
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Bacnet')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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

    /**
     * Adds a gateway
     * 
     * @param macAddress
     *            macAddress of a Gateway
     * @param ipAddress
     *            ip address of a Gateway
     * @param floorId
     *            floorId at which to add a gateway
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("add/macaddr/{macAddr}/ipaddr/{ipAddr}/floorid/{floorId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addGateway(@PathParam("macAddr") String macAddr,
            @PathParam("ipAddr") String ipAddr, @PathParam("floorId") String floorId) {
    	
    	Response resp = new Response();
    	/*Map<String, Object> nameValMap = new HashMap<String, Object>();
    	nameValMap.put("mac", macAddr);
    	nameValMap.put("ip", ipAddr);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
    	int nResult = gatewayManager.addGateway(macAddr, ipAddr, floorId);
    	
    	if(nResult != 0)
    	{
    		resp.setStatus(nResult);
        	userAuditLoggerUtil.log("Failed to add the gateway with IP address " + ipAddr + " and MAC address " + macAddr, UserAuditActionType.Gateway_Add.getName());
    	}
    	else
        	userAuditLoggerUtil.log("Successfully added the gateway with IP address " + ipAddr + " and MAC address " + macAddr, UserAuditActionType.Gateway_Add.getName());

    	return resp;
    }
    
    /**
     * Send GatewayInfo request
     * 
     * @param macAddress
     *            macAddress of a Gateway
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("sendgatewayinfo/ipaddr/{ipAddr}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addGateway(@PathParam("ipAddr") String ipAddr) {
    	
    	Response resp = new Response();
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "ip", ipAddr);
        /*if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return resp;
    	}*/
    	gatewayManager.getRealtimeStats(ipAddr);
    	
    	return resp;
    }
    
    /**
     * Updates user provided installed no of wds
     * @param gatewayId
     * @param noOfWds
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("du/updatenoofwds/gateway/{gatewayId}/noofwds/{noOfWds}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGatewaynoOfWds(@PathParam("gatewayId") Long gatewayId,
            @PathParam("noOfWds") int noOfWds) {
        Response resp = new Response();
    	Gateway gw = gatewayManager.loadGateway(gatewayId);
       /* resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "page", noOfWds);
        if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
        gw.setNoOfWds(noOfWds);
        gatewayManager.updateFields(gw);
        return new Response();
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("duplicatecheck/{gatewayname}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicateGateway(
			@PathParam("gatewayname") String gatewayname) {
		Response oStatus = new Response();
		/*oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "gatewayname", gatewayname);
		if(oStatus!=null && oStatus.getStatus()!=200){
			return oStatus;
		}*/
		if (gatewayManager.getGatewayCountByName(gatewayname) > 0) {
			oStatus.setMsg(gatewayname);
			oStatus.setStatus(0);
			return oStatus;
		}

		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}

	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
    @Path("reboot/{gwid}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rebootGatewayList(@PathParam("gwid") Long gatewayId) {
		Response oStatus = new Response();
        Gateway gw = gatewayManager.loadGateway(gatewayId);
        if (gw != null) {
        	String sMsg = "Rebooting gateway with IP address " + gw.getIpAddress() + " and MAC address " + gw.getMacAddress();
        	userAuditLoggerUtil.log(sMsg,  UserAuditActionType.Gateway_Update.getName());
        	m_Logger.info(sMsg);
        	GatewayImpl.getInstance().rebootGatewaySystem(gw);
        }else {
        	oStatus.setStatus(1);
        }
		return oStatus;
    }
    
    /**
     * Updates the noOfPlugloads field of selected gateway
     * 
     * @param gatewayId
     *            gateway unique identifier
     * @param noOfSensors
     *            updated value of sensors
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("du/updatenoofplugloads/gateway/{gatewayId}/noofplugloads/{noOfPlugload}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGatewaynoOfPlugloads(@PathParam("gatewayId") Long gatewayId,
            @PathParam("noOfPlugload") int noOfPlugload) {
    	Response oStatus = new Response();
    	/*oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "typeNumber", noOfPlugload);
		if(oStatus!=null && oStatus.getStatus()!=200){
			m_Logger.error("Validation error"+oStatus.getMsg());
			return oStatus;
		}*/
    	Gateway gw = gatewayManager.loadGateway(gatewayId);
        gw.setNoOfPlugloads(noOfPlugload);
        gatewayManager.updateFields(gw);
        return new Response();
    }
    
    /**
     * Get the total discovered gateway present for the floor level
     * 
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getdiscoveredgatewaycount/floor/{floorId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getDiscoveredGatewayCount(@PathParam("floorId") Long floorId) {
    	Response res = new Response();
        List<Gateway> gw = gatewayManager.loadUnCommissionedGatewaysByFloorId(floorId);
        if(gw!=null && gw.size()>0)
        	res.setStatus(gw.size());
        return res;
    }

	
	/**
	 * Updates the ip and restarted the SSL connection against that gw and updates the gw cache from DHCP OFF to ON scenario after applying network settings
	 * Bug EM-443
	 * @param macAddr
	 * @param ipAddr
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("updateGatewayIP/macaddr/{macAddr}/ipaddr/{ipAddr}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public javax.ws.rs.core.Response updateGatewayIP(@PathParam("macAddr") String macAddr,
            @PathParam("ipAddr") String ipAddr){
		
		//Get the Gateway from macAddress.
		//If gateway ip is different then only update and restart the SSL session manager
		try{
			final Gateway gw = gatewayManager.loadGatewayByMacAddress(macAddr);
			if(gw == null){
				final String msg = "Gateway does not exists against the mac "+macAddr;
				syslog.error(msg);
				return javax.ws.rs.core.Response.notModified(msg).build();
			}
			Response resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "ip", ipAddr);
			if(resp.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()){
				//Restart SSL against this gw
				final Timer reinitiateTask = new Timer("Gateway IP Reinitiate Task", true);
				final ReinitiateConnectionTask osync = new ReinitiateConnectionTask(gw);
				reinitiateTask.schedule(osync, REINIT_DELAY);
				syslog.info("***SUCCESS*****Job scheduled to Reinitiate SSL Session with GW "+ipAddr+" db ip is "+ gw.getIpAddress());
			}else{
				//DO nothing a ip is invalid
				final String msg = "IP "+ipAddr+" passed is invalid for GW having mac "+macAddr;
				syslog.error(msg);
				return javax.ws.rs.core.Response.notModified(msg).build();
			}
		}catch(Exception e){
			syslog.error("***Exception occured while updating gateway ip****",e);
			return javax.ws.rs.core.Response.serverError()
					.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		return javax.ws.rs.core.Response.ok().build();
	}
	
	private class ReinitiateConnectionTask extends TimerTask {
		final Gateway gateway;
		public ReinitiateConnectionTask(final Gateway gw){
			this.gateway = gw;
		}
		@Override
		public void run() {
  		  // remove it from gwMap cache
  		  	GatewayImpl.getInstance().updateGatewayInfo(gateway);
	        SSLSessionManager.getInstance().removeSSLConnection(gateway.getId());
		}
	}
	
}
