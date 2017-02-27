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

import com.ems.model.Switch;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.SwitchManager;
import com.ems.vo.model.SwitchDetail;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/switch")
public class SwitchService {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "switchManager")
    private SwitchManager switchManager;
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public SwitchService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Return switch list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @return Switch list for the selected org level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Switch> getSwitchList(@PathParam("property") String property, @PathParam("pid") Long pid) {
        if (property.equalsIgnoreCase("company")) {
            return switchManager.loadAllSwitches();
        } else if (property.equalsIgnoreCase("campus")) {
            return switchManager.loadSwitchByCampusId(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return switchManager.loadSwitchByBuildingId(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return switchManager.loadSwitchByFloorId(pid);
        } else if (property.equalsIgnoreCase("area")) {
            // TODO: Need to update switch model to support area, for now send
            // floor data
            return switchManager.loadSwitchByFloorId(pid);
        }
        return null;
    }

    /**
     * Updates the position of the selected switches on the floorplan
     * 
     * @param switches
     *            List of selected switches with their respective x & y co-ordinates
     *            "<switches><switch><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></switch></switches>"
     * @return Response status
     */
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSwitchPosition(List<Switch> switches) {
        m_Logger.debug("Switches: " + switches.size());
        Iterator<Switch> itr = switches.iterator();
        while (itr.hasNext()) {
            Switch oSwitch = (Switch) itr.next();
            if (oSwitch.getXaxis() != null && oSwitch.getYaxis() != null)
                switchManager.updatePositionById(oSwitch);
        }
        userAuditLoggerUtil.log("Update Switch Positions");
        return new Response();
    }

    /**
     * Return switch list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @param name
     *            switch name
     * @return Switch for the selected name and org level
     */
    @Path("details/{property}/{pid}/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Switch loadSwitchByNameandProperty(@PathParam("property") String property, @PathParam("pid") Long pid,
            @PathParam("name") String name) {
        if (property.equalsIgnoreCase("floor")) {
            return switchManager.loadSwitchByNameandFloorId(name, pid);
        }
        return null;
    }

    /**
     * Return switch list by User ID
     * 
     * @param property
     *            (UserID)
     * @param UId
     *            user ID
     * @param name
     *            switch name
     * @return Switch for the Given users
     */
    @Path("list/user/{uId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<SwitchDetail> getSwitchesbyUser(@PathParam("uId") String uId) {
        return switchManager.loadSwitchDetailsByUserId(uId);

    }

    /**
     * Delete Switch
     * 
     * @param id
     *            Switch unique identifier
     * @return Response status
     */
    @Path("delete/{id}")
    @GET
    public Response deleteSwitch(@PathParam("id") long id) {
        switchManager.deleteSwitch(id);
        userAuditLoggerUtil.log("Delete Switch with id: " + id);
        return new Response();
    }

    /**
     * Dim fixtures based on switch and sceneid, mostly using the mode as 102 and using scene light level for individual
     * fixtures
     * 
     * @param switchId
     *            switch identifier
     * @param sceneId
     *            scene identifier
     * @param percentage
     *            percentage (0 (full off) | 100 (full on) | 101 (auto) | 102 (pick scene light levels for individual
     *            fixtures))
     * @param time
     *            duration in "minutes"
     * @return Response status
     */
    @Path("op/dim/switch/{switchid}/scene/{sceneid}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixtureBySwitchAndSceneId(@PathParam("switchid") Long switchId,
            @PathParam("sceneid") Long sceneId, @PathParam("percentage") Integer percentage,
            @PathParam("time") Integer time) {
        m_Logger.debug("Switch: " + switchId + ", Scene: " + sceneId + ", Percentage: " + percentage + ", Time: "
                + time);
        DeviceServiceImpl.getInstance().dimFixturesBySceneOfSwitch(switchId, sceneId, percentage, time);
        userAuditLoggerUtil.log("Dim Fixture with switch Id: "+switchId + " %: " + percentage);
        return new Response();
    }

    /**
     * Dims fixtures based on switch id, used mostly for the slider functionality and auto mode
     * 
     * @param switchId
     *            switch identifier
     * @param percentage
     *            percentage (0 (full off) | 100 (full on) | 101 (auto))
     * @param time
     *            duration in "minutes"
     * @return Response status
     */
    @Path("op/dim/switch/{switchid}/{percentage}/{time}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response dimFixtureBySwitch(@PathParam("switchid") Long switchId,
            @PathParam("percentage") Integer percentage, @PathParam("time") Integer time) {
        m_Logger.debug("Switch: " + switchId + ", Percentage: " + percentage + ", Time: " + time);
        DeviceServiceImpl.getInstance().dimFixturesBySwitch(switchId, percentage, time);
        userAuditLoggerUtil.log("Dim Fixture with switch Id: "+switchId + " %: " + percentage);
        return new Response();
    }

}
