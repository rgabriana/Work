package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.PlugloadCache;
import com.ems.model.Plugload;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.discovery.DiscoverySO;
import com.ems.service.PlugloadManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.PlugloadList;
import com.ems.ws.util.Response;

@Controller
@Path("/org/plugload")
public class PlugloadService {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	/**
	 * Fetch list of plugloads
	 * 
	 * @return Response -List of plugload in the format
	 *         <Plugloads><Plugload><id>{plugload
	 *         id}</id></Plugload></Plugloads>
	 */
	@Path("list/alternate/filter/{property}/{pid}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public PlugloadList loadPlugloadListWithSpecificAttrs(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@FormParam("page") Integer page, @FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) {
		System.out.println("in plugload service");
		if (bSearch == null) {
			bSearch = false;
		}
		PlugloadList plugloadList = plugloadManager
				.loadPlugloadListWithSpecificAttrs(property, pid, orderby,
						orderway, bSearch, searchField, searchString,
						searchOper, (page - 1) * FixtureList.DEFAULT_ROWS,
						FixtureList.DEFAULT_ROWS);
		plugloadList.setPage(page);
		// PlugloadList plugloadList = plugloadManager.loadAllPlugloads();
		return plugloadList;
	}

	@Path("listAllPlugloads")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public PlugloadList loadAllPlugloads() {
		return plugloadManager.loadAllPlugloads();
	}

	/* Get plugoad by id */
	@Path("getPlugloadById/{plugloadId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Plugload getPlugloadById(@PathParam("plugloadId") long plugloadId) {
		return plugloadManager.getPlugloadById(plugloadId);

	}

	/* Update plugload */
	@Path("updatePlugload")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Plugload updatePlugload(Plugload plugload) {
		Plugload savedPlugload = plugloadManager.getPlugloadById(plugload
				.getId());
		savedPlugload.setName(plugload.getName());
		savedPlugload.setState(plugload.getState());
		savedPlugload.setIsHopper(plugload.getIsHopper());
		savedPlugload.setCurrentState(plugload.getCurrentState());
		return plugloadManager.updatePlugload(savedPlugload);

	}

	/**
	 * Returns plugload list TODO: limit is currently not used, need to fix
	 * this.
	 * 
	 * @param property
	 *            (floor|area)
	 * @param pid
	 *            property unique identifier
	 * @param limit
	 * @return fixture list for the property level
	 */
	@Path("list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Plugload> loadAllPlugLoad(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("limit") String limit) {
		if (property.equalsIgnoreCase("floor")) {
			return plugloadManager.loadPlugloadByFloorId(pid);
		} else if (property.equalsIgnoreCase("area")) {
			return plugloadManager.loadPlugloadByAreaId(pid);
		}
		return null;
	}

	/**
	 * Updates the position of the selected plugloads on the floorplan
	 * 
	 * @param plugloads
	 *            List of selected plugload with their respective x & y
	 *            co-ordinates
	 *            "<plugloads><plugload><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></plugload></plugloads>"
	 * @return Response status
	 */
	@Path("du/updateposition")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateFixturePosition(List<Plugload> Plugloads) {
		Iterator<Plugload> itr = Plugloads.iterator();
		StringBuffer fixtString = new StringBuffer("");
		boolean log = false;
		while (itr.hasNext()) {
			Plugload plugload = (Plugload) itr.next();
			if (plugload.getXaxis() != null && plugload.getYaxis() != null) {
				plugloadManager.updatePosition(plugload.getId(),
						plugload.getXaxis(), plugload.getYaxis(), "");
				fixtString.append(plugloadManager.getPlugloadById(
						plugload.getId()).getName()
						+ "(X:"
						+ plugload.getXaxis()
						+ " Y:"
						+ plugload.getYaxis() + ") ");
				log = true;
			}
		}
		if (log) {
			userAuditLoggerUtil
					.log("Update plugload position for " + fixtString,
							UserAuditActionType.Plugload_Update.getName());
		}
		return new Response();
	}

	/**
	 * return plugload discovery status
	 * 
	 * @return Discovery status as part of response object
	 */
	@Path("getdiscoverystatus")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getDiscoveryStatus() {
		Response oStatus = new Response();
		oStatus.setStatus(plugloadManager.getDiscoveryStatus());
		return oStatus;
	}

	@Path("discoverPlugloads/{floorId}/{gwId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response discoverPlugloads(@PathParam("floorId") Long floorId,
			@PathParam("gwId") Long gwId) {

		DiscoverySO.getInstance().startNetworkDiscovery(floorId, gwId,
				ServerConstants.DEVICE_PLUGLOAD);
		return new Response();

	} // end of method discoverPlugLoads

	/**
	 * return plugload commission status
	 * 
	 * @return Commission status as part of response object
	 */
	@Path("getcommissionstatus")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCommissionStatus() {
		Response oStatus = new Response();
		oStatus.setStatus(plugloadManager.getCommissioningStatus());
		return oStatus;
	}

	@Path("startplugloadcommissioning/floor/{floorId}/gateway/{gatewayId}/type/{type}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response startPlugloadCommissioning(
			@PathParam("floorId") Long floorId,
			@PathParam("gatewayId") Long gatewayId, @PathParam("type") int type) {
		Response response = new Response();
		int status = plugloadManager.startPlugloadCommissionProcess(gatewayId,
				floorId, type);
		response.setStatus(status);
		return response;
	}

	@Path("commissionplugload/plugloadid/{plugloadid}/gateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response commissionPlugload(
			@PathParam("plugloadid") Long plugloadId,
			@PathParam("gatewayId") Long gatewayId) {
		Response response = new Response();
		int status = plugloadManager.commissionPlugload(plugloadId, gatewayId);
		response.setStatus(status);
		return response;
	}

	/**
	 * returns number of associated plugload to the selected gateway
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return No. of plugloads as a part of response
	 */
	@Path("getcountbygateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCountByGateway(@PathParam("gatewayId") Long gatewayId) {
		Response oStatus = new Response();
		List<Plugload> plugloadList = plugloadManager
				.loadAllPlugloadsByGatewayId(gatewayId);
		oStatus.setStatus((plugloadList != null ? plugloadList.size() : 0));
		return oStatus;
	}

	/**
	 * cancels plugload discovery process
	 * 
	 * @return Response status
	 */
	@Path("cancelnetworkdiscovery")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelNetworkDiscovery() {
		plugloadManager.cancelNetworkDiscovery();
		userAuditLoggerUtil.log("Cancel network discovery",
				UserAuditActionType.Plugload_Discovery.getName());
		return new Response();
	}

	/**
	 * The UI will call the function for one plugload at a time, till the time a
	 * robust mechanism of delete status for all is worked out.
	 * 
	 * @param plugloads
	 *            List of plugloads
	 *            "<plugloads><plugload><id>1</id></plugload></plugloads>"
	 * @return Response status
	 */
	@Path("decommission")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deCommissionFixtures(List<Plugload> plugloads) {
		m_Logger.debug("Fixtures: " + plugloads.size());
		Iterator<Plugload> itr = plugloads.iterator();
		Response oResponse = new Response();
		long plugloadId = 0;
		Plugload plugload = new Plugload();
		String plugloadName = "";
		while (itr.hasNext()) {
			plugload = (Plugload) itr.next();
			plugloadId = plugload.getId();
			// Let's find the name of plugload from cache. Let's put this in
			// try/catch as failure
			// to find the name should not stop decommission.
			try {
				if (PlugloadCache.getInstance().getDevice(plugload.getId()) != null) {
					plugloadName = PlugloadCache.getInstance()
							.getDevice(plugload.getId()).getPlugload()
							.getName();
				} else {
					plugloadName = String.valueOf(plugloadId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			oResponse.setStatus(plugloadManager.deletePlugload(plugloadId));
			oResponse.setMsg(String.valueOf(plugloadId)); // using message as
															// current fixture
															// id
			break;
		}

		if (oResponse.getStatus() == 1)
			userAuditLoggerUtil.log("Decommission plugload " + plugloadName
					+ "(Status = Success)",
					UserAuditActionType.Plugload_Commission.getName());
		else
			userAuditLoggerUtil.log("Decommission fixture " + plugloadName
					+ "(Status = Failure)",
					UserAuditActionType.Plugload_Commission.getName());
		return oResponse;
	}
}
