/**
 * 
 */
package com.ems.ws;

import java.util.ArrayList;
import java.util.Date;
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

import com.ems.cache.FixtureCache;
import com.ems.model.Ballast;
import com.ems.model.Fixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.FixtureOutageVO;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/fixture")
public class FixtureService {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "gatewayManager")
	private GatewayManager gatewayManager;

	@Resource
	SwitchManager switchManager;
	@Resource(name = "gemsGroupManager")
	private GemsGroupManager gemsGroupManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public FixtureService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Returns fixture list TODO: limit is currently not used, need to fix this.
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|gateway|secondarygateway|
	 *            group)
	 * @param pid
	 *            property unique identifier
	 * @param limit
	 * @return fixture list for the property level
	 */
	@Path("list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureList(@PathParam("property") String property,
			@PathParam("pid") Long pid, @PathParam("limit") String limit) {
		if (property.equalsIgnoreCase("company")) {
			return fixtureManager.loadAllFixtures();
		} else if (property.equalsIgnoreCase("campus")) {
			return fixtureManager.loadFixtureByCampusId(pid);
		} else if (property.equalsIgnoreCase("building")) {
			return fixtureManager.loadFixtureByBuildingId(pid);
		} else if (property.equalsIgnoreCase("floor")) {
			return fixtureManager.loadFixtureByFloorId(pid);
		} else if (property.equalsIgnoreCase("area")) {
			return fixtureManager.loadFixtureByAreaId(pid);
		} else if (property.equalsIgnoreCase("gateway")) {
			return fixtureManager.loadAllFixtureByGatewayId(pid);
		} else if (property.equalsIgnoreCase("secondarygateway")) {
			return fixtureManager.loadAllFixtureBySecondaryGatewayId(pid);
		} else if (property.equalsIgnoreCase("group")) {
			return fixtureManager.loadFixtureByGroupId(pid);
		}
		return null;
	}

	/**
	 * Returns fixture location list TODO: limit is currently not used, need to
	 * fix this.
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|gateway|secondarygateway|
	 *            group)
	 * @param pid
	 *            property unique identifier
	 * @param limit
	 * @return fixture list for the property level
	 */
	@Path("location/list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureLocationList(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("limit") String limit) {

		List results = null;
		if (property.equalsIgnoreCase("floor")) {
			results = fixtureManager.loadFixtureLocationsByFloorId(pid);
		}
		ArrayList<Fixture> fixtLocationList = new ArrayList<Fixture>();
		if (results != null && !results.isEmpty()) {
			Iterator fixtLocationIt = results.iterator();
			while (fixtLocationIt.hasNext()) {
				Object[] dataArr = (Object[]) fixtLocationIt.next();
				Fixture fxLocation = new Fixture();
				fxLocation.setId((Long) dataArr[0]);
				fxLocation.setFixtureName(dataArr[1].toString());
				fxLocation.setXaxis((Integer) dataArr[2]);
				fxLocation.setYaxis((Integer) dataArr[3]);
				fxLocation.setGroupId((Long) dataArr[4]);
				fxLocation.setMacAddress(dataArr[5].toString());
				fixtLocationList.add(fxLocation);
			}
		}
		return fixtLocationList;

	}

	/**
	 * Fetch fixture list with pagination support at organization level
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|group)
	 * @param pid
	 *            property unique identifier
	 * @param page
	 *            page no.
	 * @param orderby
	 *            sort by columnname
	 * @param orderway
	 *            (asc or desc)
	 * @param bSearch
	 *            (true or false)
	 * @param searchField
	 *            search by column field
	 * @param searchString
	 *            search pattern
	 * @param searchOper
	 *            (eq: equals, cn: like)
	 * @return filtered Fixture list
	 */
	@Path("list/filter/{property}/{pid}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixtureList getFixtureList(@PathParam("property") String property,
			@PathParam("pid") Long pid, @FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) {
		if (bSearch == null) {
			bSearch = false;
		}
		FixtureList oFixtureList = fixtureManager.loadFixtureList(property,
				pid, orderby, orderway, bSearch, searchField, searchString,
				searchOper, (page - 1) * FixtureList.DEFAULT_ROWS,
				FixtureList.DEFAULT_ROWS);
		oFixtureList.setPage(page);
		return oFixtureList;
	}
	
	
	/**
	 * Fetch fixture list (only selected attributes) with pagination support at organization level
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|group)
	 * @param pid
	 *            property unique identifier
	 * @param page
	 *            page no.
	 * @param orderby
	 *            sort by columnname
	 * @param orderway
	 *            (asc or desc)
	 * @param bSearch
	 *            (true or false)
	 * @param searchField
	 *            search by column field
	 * @param searchString
	 *            search pattern
	 * @param searchOper
	 *            (eq: equals, cn: like)
	 * @return filtered Fixture list
	 */
	@Path("list/alternate/filter/{property}/{pid}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixtureList loadFixtureListWithSpecificAttrs(@PathParam("property") String property,
			@PathParam("pid") Long pid, @FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) {
		if (bSearch == null) {
			bSearch = false;
		}
		FixtureList oFixtureList = fixtureManager.loadFixtureListWithSpecificAttrs(property,
				pid, orderby, orderway, bSearch, searchField, searchString,
				searchOper, (page - 1) * FixtureList.DEFAULT_ROWS,
				FixtureList.DEFAULT_ROWS);
		oFixtureList.setPage(page);
		return oFixtureList;
	}

	/**
	 * Returns fixture count
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|gateway|secondarygateway|
	 *            group)
	 * @param pid
	 *            property unique identifier
	 * @return Response status with msg used for sending fixture count
	 */
	@Path("count/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureCount(@PathParam("property") String property,
			@PathParam("pid") Long pid) {
		if (property.equalsIgnoreCase("campus")) {
			property = "campusId";
		} else if (property.equalsIgnoreCase("building")) {
			property = "buildingId";
		} else if (property.equalsIgnoreCase("floor")) {
			property = "floor.id";
		} else if (property.equalsIgnoreCase("area")) {
			property = "area.id";
		} else if (property.equalsIgnoreCase("gateway")) {
			property = "gateway.id";
		} else if (property.equalsIgnoreCase("secondarygateway")) {
			property = "secGwId";
		} else if (property.equalsIgnoreCase("group")) {
			property = "groupId";
		}
		Response oResponse = new Response();
		oResponse.setMsg(String.valueOf(fixtureManager.getFixtureCount(
				property, pid)));
		return oResponse;
	}

	/**
	 * Returns Fixture Details
	 * 
	 * @param fid
	 *            fixture unique identifier
	 * @return fixture details
	 */
	@Path("details/{fid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Fixture getFixtureDetails(@PathParam("fid") Long fid) {
		return fixtureManager.getFixtureById(fid);
	}

	/**
	 * Allows selected set of fixture to be dimmed or brighted from the
	 * floorplan
	 * 
	 * @param mode
	 *            (rel | abs)
	 * @param percentage
	 *            {(-100 | 0 | 100) for rel} AND {(0 | 100) for abs}
	 * @param time
	 *            minutes
	 * @param fixtures
	 *            list of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return response
	 */
	@Path("op/dim/{mode}/{percentage}/{time}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response dimFixture(@PathParam("mode") String mode,
			@PathParam("percentage") String percentage,
			@PathParam("time") String time, List<Fixture> fixtures) {
		m_Logger.debug("Percentage: " + percentage + ", Time: " + time
				+ ", Fixtures: " + fixtures.size());
		StringBuffer fixtString = new StringBuffer("");
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
			
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
			    if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
				fixtString.append(FixtureCache.getInstance()
						.getDevice(fixture.getId()).getFixtureName() + ",");
			    }else{
			        if(fixture.getId() != null){
			            fixtString.append(fixture.getId() + ","); 
			        }
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		if (mode.equalsIgnoreCase("REL"))
			fixtureManager.dimFixtures(fixtureList,
					Integer.parseInt(percentage), Integer.parseInt(time));
		else if (mode.equalsIgnoreCase("ABS"))
			fixtureManager.absoluteDimFixtures(fixtureList,
					Integer.parseInt(percentage), Integer.parseInt(time));
		userAuditLoggerUtil.log("Dimming fixtures " + fixtString + " to "
			+ percentage + "% in " + mode + " mode for " + time
			+ " minutes", UserAuditActionType.Fixture_Dimming.getName());
		return new Response();
	}

	/**
	 * Place the selected fixtures in specified mode.
	 * 
	 * @param modetype
	 *            String {AUTO|BASELINE|BYPASS}
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("op/mode/{modetype}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response applyModeToFixtures(@PathParam("modetype") String modetype,
			List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		StringBuilder fixtString = new StringBuilder("");
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Response oStatus = new Response();
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
			
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
			    if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
	                fixtString.append(FixtureCache.getInstance()
	                        .getDevice(fixture.getId()).getFixtureName() + ",");
	                }else{
	                    if(fixture.getId() != null){
	                        fixtString.append(fixture.getId() + ","); 
	                    }
	                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		if (modetype.equalsIgnoreCase("AUTO"))
			fixtureManager.auto(fixtureList);
		else if (modetype.equalsIgnoreCase("BASELINE")) {
			fixtureManager.baseline(fixtureList);
		} else if (modetype.equalsIgnoreCase("BYPASS")) {
			fixtureManager.bypass(fixtureList);
		} else {
			oStatus.setStatus(1);
			oStatus.setMsg("Undefined mode type");
		}
		userAuditLoggerUtil.log("Setting Mode of  fixtures " + fixtString
				+ " at  " + modetype,
				UserAuditActionType.Fixture_Mode_Change.getName());
		return oStatus;
	}

	/**
	 * Sends a realtime command to selected fixtures.
	 * 
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("op/realtime")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureRealTimeStats(List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			// TODO: Do I need to sleep between sending commands?
			fixtureManager.getCurrentDetails(fixture.getId());
		}
		return new Response();
	}

    /**
     * 
     * @param fixtures
     *            List of fixtures "<fixtures><fixture><id>1</id></fixture></fixtures>"
     * @param bitlevel
     *            configure bit level, 1 bit or 2 bit.
     * @param frequency
     *            send the motion bits information from SU to EM at 1 min or 5 min
     * @param motion_detection_duration
     *            1 default, 0 to switch the motion off
     * @return Response status
     */
    @Path("op/motionbit/bitlevel/{bitlevel}/frequency/{frequency}/action/{action}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response triggerMotionBits(@PathParam("bitlevel") int bitlevel, @PathParam("frequency") int frequency,
            @PathParam("action") int motion_detection_interval, List<Fixture> fixtures) {
        m_Logger.debug("Fixtures: " + fixtures.size());
        if (fixtures.size() > 0) {
            int[] fixtureList = new int[fixtures.size()];
            int count = 0;
            Iterator<Fixture> itr = fixtures.iterator();
            while (itr.hasNext()) {
                Fixture fixture = (Fixture) itr.next();
                fixtureList[count++] = fixture.getId().intValue();
            }
            if (motion_detection_interval > 1)
                motion_detection_interval = 1;
            
            if (motion_detection_interval < 0)
                motion_detection_interval = 0;
                
            fixtureManager.triggerMotionBits(fixtureList, (byte) bitlevel, (byte) frequency,
                    (byte) motion_detection_interval, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+15*60*1000));
        }
        return new Response();
    }
	/**
     * Sends the following commands to the sensors a) Put the selected sensors in auto mode first b) Then puts them back
     * in validation mode
     * 
     * @param fixtures
     * @return Response status
     */
    @Path("op/unstrobe/gateway/{gatewayId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response operationUnstrobe(@PathParam("gatewayId") Long gatewayId, List<Fixture> fixtures) {
        Response oResponse = new Response();
        fixtureManager.unStrobeFixture(fixtures, gatewayId);
        return oResponse;
    }

    /**
     * Identify fixture by taking it into dimming cycle before putting it back to validation mode.
     * 
     * @param fixtureId
     * @return Response status
     */
    @Path("op/identify/fixture/{fixtureId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response operationIdentify(@PathParam("fixtureId") Long fixtureId) {
        Response oResponse = new Response();
        fixtureManager.identifyFixture(fixtureId);
        return oResponse;
    }


	/**
	 * Updates the position of the selected fixtures on the floorplan
	 * 
	 * @param fixtures
	 *            List of selected fixture with their respective x & y
	 *            co-ordinates
	 *            "<fixtures><fixture><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("du/updateposition")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateFixturePosition(List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		Iterator<Fixture> itr = fixtures.iterator();
		StringBuffer fixtString = new StringBuffer("");
		boolean log = false;
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();

			// FIXME: Last param state is not required;
			if (fixture.getXaxis() != null && fixture.getYaxis() != null) {
				fixtureManager.updatePosition(fixture.getId(),
						fixture.getXaxis(), fixture.getYaxis(), "");
				fixtString.append(fixtureManager
						.getFixtureById(fixture.getId()).getFixtureName()
						+ "(X:"
						+ fixture.getXaxis()
						+ " Y:"
						+ fixture.getYaxis() + ") ");
				log = true;
			}

		}
		if (log) {
			userAuditLoggerUtil.log(
					"Update fixture position for " + fixtString,
					UserAuditActionType.Fixture_Update.getName());
		}
		return new Response();
	}

	/**
	 * return fixture discovery status
	 * 
	 * @return Discovery status as part of response object
	 */
	@Path("getdiscoverystatus")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getDiscoveryStatus() {
		Response oStatus = new Response();
		oStatus.setStatus(fixtureManager.getDiscoveryStatus());
		return oStatus;
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
		oStatus.setStatus(fixtureManager.getCommissioningStatus());
		return oStatus;
	}

	/**
	 * starts fixture discovery process for selected floor and gateway
	 * 
	 * @param floorId
	 *            Floor unique identifier
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return
	 */
	@Path("startnetworkdiscovery/floor/{floorId}/gateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response startNetworkDiscovery(@PathParam("floorId") Long floorId,
			@PathParam("gatewayId") Long gatewayId) {
		Response oStatus = new Response();
		oStatus.setStatus(fixtureManager.startNetworkDiscovery(floorId,
				gatewayId));
		userAuditLoggerUtil.log("Start network discovery for gateway "
				+ gatewayManager.loadGateway(gatewayId).getGatewayName(),
				UserAuditActionType.Fixture_Discovery.getName());
		return oStatus;
	}

	/**
	 * returns number of associated fixture to the selected gateway
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return No. of fixtures as a part of response
	 */
	@Path("getcountbygateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCountByGateway(@PathParam("gatewayId") Long gatewayId) {
		Response oStatus = new Response();
		List<Fixture> fxList = fixtureManager
				.loadAllFixtureByGatewayId(gatewayId);
		oStatus.setStatus((fxList != null ? fxList.size() : 0));
		return oStatus;
	}

	/**
	 * cancels fixture discovery process
	 * 
	 * @return Response status
	 */
	@Path("cancelnetworkdiscovery")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelNetworkDiscovery() {
		fixtureManager.cancelNetworkDiscovery();
		userAuditLoggerUtil.log("Cancel network discovery",
				UserAuditActionType.Fixture_Discovery.getName());
		return new Response();
	}

	/**
	 * initiate the fixture commissioning process for all fixtures associated
	 * with the selected floor and gateway
	 * 
	 * @param floorId
	 *            Floor unique identifier
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @param type
	 *            Commission type
	 * @return Response status
	 */
	@Path("commissionfixtures/floor/{floorId}/gateway/{gatewayId}/type/{type}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response commissionFixtures(@PathParam("floorId") Long floorId,
			@PathParam("gatewayId") Long gatewayId, @PathParam("type") int type) {
		Response response = new Response();
		int status = fixtureManager
				.commissionFixtures(gatewayId, floorId, type);
		response.setStatus(status);
		return response;
	}

	/**
	 * initiate the fixture commissioning process for selected fixture
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @param type
	 *            Commission type
	 * @return Response status
	 */
	@Path("commissionfixtures/fixtureId/{fixtureId}/type/{type}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response commissionFixture(@PathParam("fixtureId") Long fixtureId,
			@PathParam("type") int type) {
		Response response = new Response();
		int status = fixtureManager.commissionFixture(fixtureId, type);
		response.setStatus(status);
		userAuditLoggerUtil.log("Commission fixture: "
				+ fixtureManager.getFixtureById(fixtureId).getFixtureName(),
				UserAuditActionType.Fixture_Commission.getName());
		return response;
	}

	/**
	 * used to exit fixture commissioning process
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
		int status = fixtureManager.exitCommissioning(gatewayId);
		response.setStatus(status);
		userAuditLoggerUtil.log("Exit comission for gateway "
				+ gatewayManager.loadGateway(gatewayId).getGatewayName(),
				UserAuditActionType.Fixture_Commission.getName());
		return response;
	}

	/**
	 * updates fixture during fixture commissioning process updates the fields
	 * of fixtures which are editable in the fixture commissioning form
	 * 
	 * @param fixture
	 *            <fixture>
	 *            <id>166</id><noofbulbs>1</noofbulbs><currentprofile>Warehouse
	 *            </currentprofile><name>Sensor000446 </name>
	 *            <description></description><notes></notes>
	 *            <ballast><id>9</id><name></name><lampnum></lampnum></ballast>
	 *            <bulb><id>3</id><name></name></bulb>
	 *            <nooffixtures>1</nooffixtures> <voltage>277</voltage>
	 *            </fixture>
	 * 
	 * @return Response status
	 */
	@Path("updateduringcommission")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateDuringCommission(Fixture fixture) {
		Response response = new Response();

		// Get the fixture from the database
		Fixture savedFixture = fixtureManager.getFixtureById(fixture.getId());

		// Let's populate the values on the fixture
		savedFixture.setFixtureName(fixture.getFixtureName());
		//savedFixture.setIsHopper(fixture.getIsHopper());

		savedFixture.setBallast(fixtureManager.loadBallast(fixture.getBallast()
				.getId()));
		savedFixture
				.setBulb(fixtureManager.loadBulb(fixture.getBulb().getId()));
		savedFixture.setNoOfBulbs(fixture.getNoOfBulbs());
		savedFixture.setCurrentProfile(fixture.getCurrentProfile());
		savedFixture.setNoOfFixtures(fixture.getNoOfFixtures());
		savedFixture.setVoltage(fixture.getVoltage());
		savedFixture.setNoOfFixtures(fixture.getNoOfFixtures());
		savedFixture.setDescription(fixture.getDescription());
		savedFixture.setNotes(fixture.getNotes());

		fixtureManager.updateFixture(savedFixture, true);
		userAuditLoggerUtil.log("Update during commission for fixture "
				+ fixture.getFixtureName(),
				UserAuditActionType.Fixture_Commission.getName());

		return response;
	}

	/**
	 * Validates the selected fixture as a part of fixture commissioning process
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Response status
	 */
	@Path("validatefixture/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response validatefixture(@PathParam("fixtureId") Long fixtureId) {
		fixtureManager.validateFixture(fixtureId);
		return new Response();
	}

	/**
	 * Validates the selected fixture by gateway as a part of fixture
	 * commissioning process
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Response status
	 */
	@Path("validatefixture/gatewayId/{gatewayId}/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response validatefixture(@PathParam("gatewayId") Long gatewayId,
			@PathParam("fixtureId") Long fixtureId) {
		fixtureManager.validateFixture(fixtureId, gatewayId);
		return new Response();
	}

	/**
	 * returns commission status of selected fixtures
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Commission status as part of response object
	 */
	@Path("getcommissionstatus/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCommissionStatusString(
			@PathParam("fixtureId") long fixtureId) {
		Response oStatus = new Response();
		oStatus.setMsg(fixtureManager.getCommissionStatus(fixtureId));
		return oStatus;
	}

	/**
	 * Change profile of selected fixture
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @param groupId
	 *            Profile (Group) unique identifier
	 * @param currentProfile
	 *            New profile name
	 * @param originalProfile
	 *            Old profile name
	 * @return Response status
	 */
	@Path("changeprofile/fixtureId/{fixtureId}/groupId/{groupId}/currentProfile/{currentProfile}/originalProfile/{originalProfile}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response changeProfile(@PathParam("fixtureId") long fixtureId,
			@PathParam("groupId") long groupId,
			@PathParam("currentProfile") String currentProfile,
			@PathParam("originalProfile") String originalProfile) {
		fixtureManager.changeFixtureProfile(fixtureId, groupId, currentProfile,
				originalProfile);
		userAuditLoggerUtil.log("Change profile for fixture "
				+ fixtureManager.getFixtureById(fixtureId).getFixtureName(),
				UserAuditActionType.Fixture_Profile_Update.getName());
		return new Response();
	}

	/**
	 * The UI will call the function for one fixture at a time, till the time a
	 * robust mechanism of delete status for all is worked out.
	 * 
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("decommission")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deCommissionFixtures(List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		Iterator<Fixture> itr = fixtures.iterator();
		Response oResponse = new Response();
		long fixtureId = 0;
		Fixture fixture = new Fixture();
		String fixtureName = "";
		while (itr.hasNext()) {
			fixture = (Fixture) itr.next();
			fixtureId = fixture.getId();
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
			    if(FixtureCache.getInstance()
                        .getDevice(fixture.getId()) != null){
				fixtureName = FixtureCache.getInstance()
						.getDevice(fixture.getId()).getFixtureName();
			    }else{
			        fixtureName = String.valueOf(fixtureId);
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
			oResponse.setStatus(fixtureManager.deleteFixture(fixtureId));
			oResponse.setMsg(String.valueOf(fixtureId)); // using message as
															// current fixture
															// id
			break;
		}

		if(oResponse.getStatus() == 1)
			userAuditLoggerUtil.log("Decommission fixture " + fixtureName
				+ "(Status = Success)",
				UserAuditActionType.Fixture_Commission.getName());
		else
			userAuditLoggerUtil.log("Decommission fixture " + fixtureName
					+ "(Status = Failure)",
					UserAuditActionType.Fixture_Commission.getName());
		return oResponse;
	}

	/**
	 * The UI will call the function for one fixture at a time, till the time a
	 * robust mechanism of delete status for all is worked out.
	 * 
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("decommissionwithoutack")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deCommissionFixturesWithoutAck(List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		Iterator<Fixture> itr = fixtures.iterator();
		Response oResponse = new Response();
		long fixtureId = 0;
		Fixture fixture = new Fixture();
		String fixtureName = "";
		while (itr.hasNext()) {
			fixture = (Fixture) itr.next();
			fixtureId = fixture.getId();

			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
	        //to find the name should not stop decommission.
			try {
			    if(FixtureCache.getInstance()
                        .getDevice(fixture.getId()) != null){
                fixtureName = FixtureCache.getInstance()
                        .getDevice(fixture.getId()).getFixtureName();
                }else{
                    fixtureName = String.valueOf(fixtureId);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Disassociate the fixture from switch fixture
			//switchManager.deleteSwitchFixturesForFixture(fixtureId);
			gemsGroupManager.deleteGemsGroupsFromFixture(fixtureId);
			oResponse.setStatus(fixtureManager
					.deleteFixtureWithoutAck(fixtureId));
			oResponse.setMsg(String.valueOf(fixtureId)); // using message as
															// current fixture
															// id
			break;
		}
		if(oResponse.getStatus() == 1)
			userAuditLoggerUtil.log(
				"Decommission, without ack, fixture "
						+ fixtureName + "(Status = Success)", UserAuditActionType.Fixture_Commission.getName());
		else
			userAuditLoggerUtil.log(
					"Decommission, without ack, fixture "
							+ fixtureName + "(Status = Failure)", UserAuditActionType.Fixture_Commission.getName());

		return oResponse;
	}

	/**
	 * Reboots the sensor alternate currently application 1
	 * 
	 * @param fixture
	 * @return Response status
	 */
	@Path("switchrunningimage")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response SwitchRunningImage(Fixture fixture) {
		m_Logger.debug(fixture.getId() + " switch Fixture image");
		fixtureManager.restoreImage(fixture.getId());
		Response oResponse = new Response();
		oResponse.setMsg("Scheduled");
		userAuditLoggerUtil.log(
				"Switch running image for fixture " + fixture.getFixtureName(),
				UserAuditActionType.Fixture_Image_Upgrade.getName());
		return oResponse;
	}
	
	@Path("checkswitchrunningimagestatus")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response CheckSwitchRunningImageStatus(Fixture f) {
		m_Logger.debug(f.getId() + " Check status of switching of Fixture image");
		Fixture fixture = fixtureManager.getFixtureById(f.getId());		
		Response oResponse = new Response();
		oResponse.setMsg(fixture.getCurrApp().toString());
		return oResponse;
	}

	/**
	 * Fetch fixture for which fixture out events have been raised.
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|gateway|secondarygateway|
	 *            group)
	 * @param pid
	 *            property unique identifier
	 * @return Fixture list
	 */
	@Path("list/report/fo/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureOutFixtureList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		return fixtureManager.getFixtureOutFixtureList(property, pid);
	}

	/**
	 * Fetch fixture Outage list
	 */
	@Path("list/report/fixtureOutage/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureOutageVO> getFixtureOutageList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		return fixtureManager.getFixtureOutageList(property, pid);
	}
	/**
	 * The UI will call the function for one fixture at a time, till the time a
	 * robust mechanism of delete status for all is worked out.
	 * 
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("delete/all/discovered")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteAllDiscoveredFixtures() {
		List<Fixture> fixtures=null ;
		Response oResponse = new Response();
		fixtures = fixtureManager.loadFixtureByState(ServerConstants.FIXTURE_STATE_DISCOVER_STR);
		if(fixtures!=null)
		{
		Iterator<Fixture> itr = fixtures.iterator();
		
		long fixtureId = 0;
		Fixture fixture = new Fixture();
		String fixtureName = "";
		int deleteFixtureCount =0 ;
		while (itr.hasNext()) {
			fixture = (Fixture) itr.next();
			fixtureId = fixture.getId();
			 //Let's find the name of fixture from cache. Let's put this in try/catch as failure
            //to find the name should not stop decommission.
            try {
                if(FixtureCache.getInstance()
                        .getDevice(fixture.getId()) != null){
                fixtureName = FixtureCache.getInstance()
                        .getDevice(fixture.getId()).getFixtureName();
                }else{
                    fixtureName = String.valueOf(fixtureId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
			fixtureManager.deleteFixture(fixtureId);
			deleteFixtureCount++;
			
			
			userAuditLoggerUtil.log("Deleted fixture " + fixtureName
					+ "(Status = " + oResponse.getStatus() + ")",
					UserAuditActionType.Fixture_Discovered_Delete.getName());											
			
		}
		oResponse.setStatus(1);
		oResponse.setMsg(String.valueOf(deleteFixtureCount));
		
		return oResponse;
		}
		else
		{
			oResponse.setStatus(0);
			oResponse.setMsg(String.valueOf(0));
			return oResponse ;
		}
	}
	
	
	/**
	 * Service to do a rma replacement
	 * @param fromFixtureId - Fixture which needs to be replaced
	 * @param toFixtureId - New Fixture
	 * @return
	 */
	@Path("rma/{from}/{to}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rmaFixture(@PathParam("from")Long fromFixtureId, @PathParam("to")Long toFixtureId) {
	    FixtureCache.getInstance().invalidateDeviceCache(fromFixtureId);
        FixtureCache.getInstance().invalidateDeviceCache(toFixtureId);
        boolean status = fixtureManager.rmaFixture(fromFixtureId, toFixtureId);
        
        Response oResponse = new Response();
        
        if(status){
            oResponse.setStatus(1);
            oResponse.setMsg("RMA successful");
        }else{
            oResponse.setStatus(0);
            oResponse.setMsg("RMA unsuccessful");
        }
        
      //Let's find the name of fixture from cache. Let's put this in try/catch as failure
        //to find the name should not stop decommission.
        String fromFixtureName = "";
        String toFixtureName = "";
        try {
            fromFixtureName = fixtureManager.getFixtureById(fromFixtureId).getFixtureName();
            if(fromFixtureName == null){
                fromFixtureName = String.valueOf(fromFixtureId);
            }
            toFixtureName = fixtureManager.getFixtureById(fromFixtureId).getFixtureName();
            if(toFixtureName != null){
               toFixtureName = String.valueOf(toFixtureId);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        userAuditLoggerUtil.log("RMA fixture " + fromFixtureName
                + " to " + toFixtureName,
                UserAuditActionType.FIxture_RMA.getName());
        
        return oResponse;
        
    }
	
	/**
	 * Returns ballast list
	 *  
	 * @return ballast list
	 */
	@Path("ballast/list")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Ballast> getBallastList() {
	
		List<Ballast> ballastList = fixtureManager.getAllBallasts();
		return ballastList;
		
	} //end of method getBallastList
	
	/**
	 * Sends a manual calibration command to selected fixtures.
	 * 
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@Path("op/manualCalib")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response calibrateFixtures(List<Fixture> fixtures) {
		
		m_Logger.debug("Fixtures: " + fixtures.size());
		StringBuilder fixtString = new StringBuilder("");
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;		
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();
			
			//Let's find the name of fixture from cache. Let's put this in try/catch as failure
			//to find the name should not stop decommission.
			try {
				if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
					fixtString.append(FixtureCache.getInstance().getDevice(fixture.getId()).getFixtureName() + ",");
				}else{
					if(fixture.getId() != null){
						fixtString.append(fixture.getId() + ","); 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}		
		fixtureManager.calibrateFixtures(fixtureList);		
		userAuditLoggerUtil.log("Calibrating fixtures " + fixtString, 
				UserAuditActionType.Fixture_Calibration.getName());
		return new Response();
		
	} //end of method calibrateFixtures
	
	/**
	 * Send enable or disable hopper command to set of fixtures.
	 * 
	 * @param enable
	 * @param fixtures
	 * @return response status of this service call. (The hooper command status
	 *         is async and needs to be verified via the database or via FX
	 *         stats)
	 */
	@Path("op/hopper/{enabled}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enabledisableHopper(@PathParam("enabled") Boolean enable,
			List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		StringBuilder fixtString = new StringBuilder("");
		int[] fixtureList = new int[fixtures.size()];
		int count = 0;
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixtureList[count++] = fixture.getId().intValue();

			try {
				if (FixtureCache.getInstance().getDevice(fixture.getId()) != null) {
					fixtString.append(FixtureCache.getInstance()
							.getDevice(fixture.getId()).getFixtureName()
							+ ",");
				} else {
					if (fixture.getId() != null) {
						fixtString.append(fixture.getId() + ",");
					}
				}
			} catch (Exception e) {
				m_Logger.warn(e);
			}

		}
		DeviceServiceImpl.getInstance().enabledisableHoppers(fixtureList,
				enable);
		userAuditLoggerUtil.log("Hopper enabled (" + enable + "): " + fixtString,
				UserAuditActionType.Fixture_Update.getName());
		return new Response();

	}
}
