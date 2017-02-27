package com.ems.ws;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.Wds;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.wds.WDSImpl;
import com.ems.service.GatewayManager;
import com.ems.service.SwitchManager;
import com.ems.service.WdsManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

@Controller
@Path("/org/wds")
public class WdsService {
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;
    
    @Resource(name = "switchManager")
    private SwitchManager switchManager;
    
    public WdsService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    /**
     * Return wds list
     * 
     * @param property
     *            (company|campus|building|floor|area|secondarygateway|switch)
     * @param pid
     *            property unique identifier
     * @return Wds list for the selected org level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getWdsList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("company")) {
            return wdsManager.loadAllWds();
        } else if (property.equalsIgnoreCase("campus")) {
            return wdsManager.loadWdsByCampusId(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return wdsManager.loadWdsByBuildingId(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return wdsManager.loadWdsByFloorId(pid);
        } else if (property.equalsIgnoreCase("area")) {
            return wdsManager.loadWdsByAreaId(pid);
        } else if (property.equalsIgnoreCase("secondarygateway")) {
            return wdsManager.loadAllCommissionedWdsByGatewayId(pid);
        } else if (property.equalsIgnoreCase("switch")) {
            return wdsManager.loadCommissionedWdsBySwitchId(pid);
        }
        return null;
    }


    /**
     * Only discovered WDS by gateway id support for displaying them in the commissioning window.
     * @param property
     * @param pid
     * @return of WDSs
     */
    @Path("list/discovered/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getDiscoveredWdsList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("secondarygateway")) {
            return wdsManager.getUnCommissionedWDSList(pid);
        }
        return null;
    }

    /**
     * Updates the position of the selected wds on the floorplan
     * 
     * @param wds
     *            List of selected wds with their respective x & y co-ordinates
     *            "<wdses><wds><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></wds></wdses>"
     * @return Response status
     */
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateWdsPosition(List<Wds> wdses) {
        m_Logger.debug("Wds: " + wdses.size());
        Iterator<Wds> itr = wdses.iterator();
        StringBuffer wdsNames = new StringBuffer("");
        boolean log = false;
        while (itr.hasNext()) {
            Wds oWds = (Wds) itr.next();
            if (oWds.getXaxis() != null && oWds.getYaxis() != null) {
                wdsManager.updatePositionById(oWds);
                wdsNames.append(wdsManager.getWdsSwitchById(oWds.getId()).getName() + "(X:" + oWds.getXaxis() + " Y:" + oWds.getYaxis() + ") ");
                log = true;
            }
        }
        if(log) {
            userAuditLoggerUtil.log("Update EWS positions for " + wdsNames, UserAuditActionType.Switch_Update.getName());
        }
        return new Response();
    }

    /**
     * return wds discovery status
     * 
     * @return Discovery status as part of response object
     */
    @Path("getdiscoverystatus")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getDiscoveryStatus() {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.getDiscoveryStatus());
        return oStatus;
    }

    /**
     * Returns WDS Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return fixture details
     */
    @Path("details/{wdsId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Wds getFixtureDetails(@PathParam("wdsId") Long wdsId) {
        return wdsManager.getWdsSwitchById(wdsId);
    }
    
    /**
     * Initiates the commission process
     * @param floorId
     * @param gatewayId
     * @return Response status
     */
    @Path("startcommission/floor/{floorId}/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response commission(@PathParam("floorId") Long floorId,
            @PathParam("gatewayId") Long gatewayId) {
        Response response = new Response();
        int status = wdsManager.commissionWds(floorId, gatewayId);
        response.setStatus(status);
        return response;
    }
    
    /**
     * updates Wds during Wds commissioning process updates the fields
     * of Wds which are editable in the Wds commissioning form
     * 
     * @param Wds
     *          <switches>
                <Wds>
                <id>1227</id>
                <name>WDS002</name>
                <floorid>10</floorid>
                <campusid>4</campusid>
                <xaxis>0</xaxis>
                <yaxis>0</yaxis>
                <dimmercontrol>0</dimmercontrol>
                <activecontrol>1</activecontrol>
                <switchtype>Real</switchtype>
                <state>DISCOVERED</state>
                <gatewayid>308</gatewayid>
                </Wds>
                </switches>
     * 
     * @return Response status
     */
    @Path("updateduringcommission")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateDuringCommission(Wds wdsSwitch) {
        Response response = new Response();
        // Get the fixture from the database
        Wds savedWds = wdsManager.getWdsSwitchById(wdsSwitch.getId());
        // Let's populate the values on the fixture
        savedWds.setName(wdsSwitch.getName());
        savedWds.setXaxis(wdsSwitch.getXaxis());
        savedWds.setYaxis(wdsSwitch.getYaxis());
        wdsManager.update(savedWds);
        //TODO: change audit component
        userAuditLoggerUtil.log("Update during commission for EWS "
                + wdsSwitch.getName(),
                UserAuditActionType.Fixture_Commission.getName());

        return response;
    }
    
    /**
     * 
     * @param wdsSwitch
     * @return
     */
    @Path("startcommission")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response startCommission(Wds wdsSwitch) {
        Response response = new Response();
        // Get the fixture from the database
        Wds savedWds = wdsManager.getWdsSwitchById(wdsSwitch.getId());
        int status = WDSImpl.getInstance().setWdsWirelessParams(savedWds.getId(), savedWds.getGatewayId());
        response.setStatus(status);
        if (status == 0) {
            userAuditLoggerUtil.log("start commission for EWS " + wdsSwitch.getName(),
                    UserAuditActionType.Fixture_Commission.getName());
        }

        return response;
    }
    
    /**
     * return fixture commission status
     * 
     * @return Commission status as part of response object
     */
    @Path("getcommissionstatus")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCommissionStatus() {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.getCommissioningStatus());
        return oStatus;
    }
    
    /**
     * starts wds discovery process for selected floor and gateway
     * 
     * @param floorId
     *            Floor unique identifier
     * @param gatewayId
     *            Gateway unique identifier
     * @return
     */
    @Path("startWdsnetworkdiscovery/floor/{floorId}/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response startWDSNetworkDiscovery(@PathParam("floorId") Long floorId,
            @PathParam("gatewayId") Long gatewayId) {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.startNetworkDiscovery(floorId,
                gatewayId));
        userAuditLoggerUtil.log("Start EWS network discovery for gateway "
                + gatewayManager.loadGateway(gatewayId).getGatewayName(),
                UserAuditActionType.EWS_Discovery.getName());
        return oStatus;
    }

    /**
     * cancels wds discovery process
     * 
     * @return Response status
     */
    @Path("cancelwdsnetworkdiscovery")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response cancelWdsNetworkDiscovery() {
        wdsManager.cancelNetworkDiscovery();
        userAuditLoggerUtil.log("Cancel EWS network discovery",
                UserAuditActionType.EWS_Discovery.getName());
        return new Response();
    }
    
    /**
     * used to exit WDS commissioning process
     * 
     * @param gatewayId
     *            Gateway unique identifier
     * @return Response status
     */
    @Path("exitcommission/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response exitCommission(@PathParam("gatewayId") Long gatewayId) {
        Response response = new Response();
        int status = wdsManager.exitCommissioning(gatewayId);
        response.setStatus(status);
        userAuditLoggerUtil.log("Exit EWS comission for gateway "
                + gatewayManager.loadGateway(gatewayId).getGatewayName(),
                UserAuditActionType.EWS_Commission.getName());
        return response;
    }
    
    /**
     * returns commission status of selected fixtures
     * 
     * @param wdsId
     *            Fixture unique identifier
     * @return Commission status as part of response object
     */
    @Path("getcommissionstatus/wds/{wdsId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCommissionStatusString(
            @PathParam("wdsId") long wdsId) {
        Response oStatus = new Response();
        oStatus.setMsg(wdsManager.getCommissioningStatus(wdsId));
        return oStatus;
    }

    /**
     * return count of wds associated with the gateway
     * @param gatewayId
     * @return Response status
     */
    @Path("getwdscountbygateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getWdsCountByGateway(@PathParam("gatewayId") Long gatewayId) {
        Response oStatus = new Response();
        List<Wds> oList = wdsManager.getUnCommissionedWDSList(gatewayId);
        oStatus.setStatus((oList != null ? oList.size() : 0));
        return oStatus;
    }
    
    @Path("updateSwitchWds/{switchId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSwitchWds(@PathParam("switchId") Long switchId, List<Wds> wdsList) {
    	Switch sw = switchManager.getSwitchById(switchId);
    	Long gemsGroupId = sw.getGemsGroup().getId();
    	SwitchGroup swGrp = switchManager.getSwitchGroupByGemsGroupId(gemsGroupId);
    	List<Wds> existingWds = wdsManager.loadCommissionedWdsBySwitchId(switchId);
    	Set<Long> existingWdsIds = new HashSet<Long>();
    	if(existingWds != null &&  existingWds.size() > 0) {
    		for (Wds each: existingWds) {
    			existingWdsIds.add(each.getId());
    		}
    	}
    	
    	if(wdsList != null && wdsList.size() > 0) {
    		for(Wds each: wdsList) {
    			if(existingWdsIds.contains(each.getId())) {
    				existingWdsIds.remove(each.getId());
    			}
    			else {
    				Wds updateWds = wdsManager.loadWdsById(each.getId());
    				updateWds.setWdsSwitch(sw);
    				updateWds.setSwitchGroup(swGrp);
    				updateWds.setAssociationState(ServerConstants.WDS_STATE_ASSOCIATED);
    				wdsManager.update(updateWds);
    			}
    		}
    	}
    	
    	for(Long wdsId: existingWdsIds) {
    		Wds updateWds = wdsManager.loadWdsById(wdsId);
			updateWds.setAssociationState(ServerConstants.WDS_STATE_NOT_ASSOCIATED);
			wdsManager.update(updateWds);
    	}
    	
        Response response = new Response();
        response.setMsg("S");
        return response;
    }
    
    @Path("deleteWds/{wdsId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteWds(@PathParam("wdsId") Long wdsId) {
        Response response = new Response();
        Wds oWds = wdsManager.getWdsSwitchById(wdsId);
        if (oWds != null) {
            if (oWds.getWdsSwitch() != null) {
                response.setStatus(1);
            } else {
                wdsManager.deleteWds(wdsId);
            }
        }
        return response;
    }
}
