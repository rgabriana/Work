/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.cache.PlugloadCache;
import com.ems.hvac.utils.JsonUtil;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.Building;
import com.ems.model.Bulb;
import com.ems.model.Campus;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureCalibrationMap;
import com.ems.model.FixtureClass;
import com.ems.model.FixtureLampCalibration;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Groups;
import com.ems.model.PlacedFixture;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BuildingManager;
import com.ems.service.BulbManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureCalibrationManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.GroupManager;
import com.ems.service.LocatorDeviceManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.PlacedFixtureManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.TemperatureType;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.FixtureDetails;
import com.ems.vo.model.BulbVO;
import com.ems.vo.model.FixtureLampStatusVO;
import com.ems.vo.model.FixtureList;
import com.ems.vo.model.FixtureOutageVO;
import com.ems.vo.model.FixtureVoltPowerList;
import com.ems.vo.model.PlacementInfoVO;
import com.ems.vo.model.SensorConfig;
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
	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	@Resource(name = "gatewayManager")
	private GatewayManager gatewayManager;
	@Resource
	SwitchManager switchManager;
	@Resource(name = "gemsGroupManager")
	private GemsGroupManager gemsGroupManager;
	@Resource
	CampusManager campusManager;
	@Resource
	BuildingManager buildingManager;
	@Resource
	FloorManager floorManager;
    @Resource
    FixtureCalibrationManager fixtureCalibrationManager;
	@Resource
	BulbManager	bulbManager;
	@Resource
	BallastManager	ballastManager;
	@Resource(name = "placedFixtureManager")
	private PlacedFixtureManager placedFixtureManager;
	@Resource
	GroupManager groupManager;
	@Resource
	PlugloadGroupManager plugloadGroupManager;
	@Resource(name = "fixtureClassManager")
	private FixtureClassManager fixtureClassManager;
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	@Resource
	private LocatorDeviceManager locatorDeviceManager;
    @Resource
    FacilityTreeManager facilityTreeManager;
	@Resource
	private EventsAndFaultManager eventsAndFaultManager;
	@Resource
	private CompanyManager companyManager;
	
	@Resource(name = "motionGroupManager")
	private MotionGroupManager motionGroupManager;
	
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureList(@PathParam("property") String property,
			@PathParam("pid") Long pid, @PathParam("limit") String limit) {
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("fixProperty", property);
   // 	nameValMap.put("limit", limit);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
	@Path("location/list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureLocationList(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("limit") String limit) {
		ArrayList<Fixture> fixtLocationList = new ArrayList<Fixture>();
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("fixProperty", property);
    	//nameValMap.put("limit", limit);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return fixtLocationList;
    	}
		
		
		List results = null;
		if (property.equalsIgnoreCase("floor")) {
			results = fixtureManager.loadFixtureLocationsByFloorId(pid);
		}
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
				if("area".equalsIgnoreCase(property) && dataArr[6] == null){
					fxLocation.setAreaId(0L);					
				}else{
					fxLocation.setAreaId((Long) dataArr[6]);
				}
								
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
		
		FixtureList oFixtureList = new FixtureList();
		if (bSearch == null) {
			bSearch = false;
		}
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("fixServiceProperty", property);    	
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return oFixtureList;
    	}
		oFixtureList = fixtureManager.loadFixtureList(property,
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
		Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("fixServiceProperty", property);
    	
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new FixtureList();
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("count/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureCount(@PathParam("property") String property,
			@PathParam("pid") Long pid) {
		Response oResponse = new Response();
        oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "fixProperty", property);
        if(oResponse!= null && oResponse.getStatus()!=200){
        	m_Logger.error("Validation error"+oResponse.getMsg());
    		return oResponse;
    	}
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
		
		oResponse.setMsg(String.valueOf(fixtureManager.getFixtureCount(
				property, pid)));
		return oResponse;
	}
	
	/**
	 * Returns fixture count
	 * 
	 * @param templateId
	 *            profile template Id
	 *            
	 * @return Response status with msg used for sending fixture count
	*/
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("count/profiletemplate/{templateId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response loadFixtureCountByTemplateId(@PathParam("templateId") Long templateId){
		Response oResponse = new Response();
		oResponse.setMsg(String.valueOf(fixtureManager.getFixtureCountByTemplateId(templateId)));
		return oResponse;
	}

	/**
	 * Returns Fixture Details
	 * 
	 * @param fid
	 *            fixture unique identifier
	 * @return fixture details
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
		Response oResponse = new Response();
       /* Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("mode", mode);
        nameValMap.put("time", time);
        nameValMap.put("fixPercentage", percentage);
		oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oResponse!= null && oResponse.getStatus()!=200){
    		return oResponse;
    	}*/
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
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
		/*Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("modetype", modetype);
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oStatus!= null && oStatus.getStatus()!=200){
    		return oStatus;
    	}*/
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Bacnet')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("op/motionbit/bitlevel/{bitlevel}/frequency/{frequency}/action/{action}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response triggerMotionBits(@PathParam("bitlevel") int bitlevel, @PathParam("frequency") int frequency,
            @PathParam("action") int motion_detection_interval, List<Fixture> fixtures) {
        m_Logger.debug("Fixtures: " + fixtures.size());
        if (fixtures.size() > 0) {
        	Response oStatus = new Response();
    		/*Map<String,Object> nameValMap = new HashMap<String, Object>();
            nameValMap.put("bitlevel", bitlevel);
            nameValMap.put("frequency", frequency);
            nameValMap.put("motion_detection_interval", motion_detection_interval);
            oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
            if(oStatus!= null && oStatus.getStatus()!=200){
        		return oStatus;
        	}*/
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	 * initiate the Placed fixture commissioning process 
	 * 
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("commissionplacedfixtures/gateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response commissionPlacedFixtures(@PathParam("gatewayId") Long gatewayId) {
		Response response = new Response();
		int status = fixtureManager.commissionPlacedFixtures(gatewayId);
		response.setStatus(status);
		userAuditLoggerUtil.log("Commission placed fixtures started using gateway: "
				+ gatewayManager.loadGateway(gatewayId).getGatewayName(),
				UserAuditActionType.Fixture_Commission.getName());
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	 * used to exit placed fixture commissioning process
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("exitplacedfixturecommission/gateway/{gatewayId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response exitPlacedFixtureCommission(@PathParam("gatewayId") Long gatewayId) {
		Response response = new Response();
		int status = fixtureManager.exitPlacedFixtureCommissioning(gatewayId);
		response.setStatus(status);
		userAuditLoggerUtil.log("Exit Placed Fixture comission for gateway "
				+ gatewayManager.loadGateway(gatewayId).getGatewayName(),
				UserAuditActionType.Placed_Fixture_Commission.getName());
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
		savedFixture.setFixtureClassId(fixture.getFixtureClassId());

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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("validatefixture/gatewayId/{gatewayId}/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response validatefixture(@PathParam("gatewayId") Long gatewayId,
			@PathParam("fixtureId") Long fixtureId) {
		fixtureManager.validateFixture(fixtureId, gatewayId);
		return new Response();
	}

	/**
	 * Validates the selected fixture by gateway as a part of fixture
	 * commissioning process
	 * 
	 * @param gatewayId
	 *            Gateway unique identifier
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("validateplacedfixture/gatewayId/{gatewayId}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response validateplacedfixture(@PathParam("gatewayId") Long gatewayId,
			 List<Fixture> fxList) {
		
    	Gateway gw = gatewayManager.loadGateway(gatewayId);
    	if (gw != null) {
			m_Logger.debug("Updating, Selected gateway: " + gw.getId() + " "
					+ gw.getMacAddress() + " {fx: " + fxList.size() + "}");
			
	        // PLACED sensors are not "discovered" and so not associated with a gateway. 
	        // But to use the CommandScheduler to commission them with a gateway, need to set secGwid
			fixtureManager.updateFixtureSecGw(fxList, gw);

			m_Logger.debug("Processing, Selected gateway: " + gw.getId() + " "
					+ gw.getMacAddress() + " {fx: " + fxList.size() + "}");
			
			for(Fixture fixture : fxList)
			{
				Fixture fxObj = fixtureManager.getFixtureById(fixture.getId());
				
				if(fxObj == null)
					continue;
				
		        if(fxObj.getState().equals(ServerConstants.FIXTURE_STATE_PLACED_STR))
		        {
					fixtureManager.validateFixture(fixture.getId(), gatewayId);
		        }
			}
    	}
		return new Response();
	}

	/**
	 * returns commission status of selected fixtures
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Commission status as part of response object
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	 * returns commission status of selected fixtures
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Commission status as part of response object
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getplacedfixturecommissionstatus/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getPlacedFixtureCommissionStatusString(
			@PathParam("fixtureId") int fixtureId) {
		Response oStatus = new Response();
		oStatus.setMsg(fixtureManager.getCommissionStatus(fixtureId));
		oStatus.setStatus(fixtureId);
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("changeprofile/fixtureId/{fixtureId}/groupId/{groupId}/currentProfile/{currentProfile}/originalProfile/{originalProfile}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response changeProfile(@PathParam("fixtureId") long fixtureId,
			@PathParam("groupId") long groupId,
			@PathParam("currentProfile") String currentProfile,
			@PathParam("originalProfile") String originalProfile) {
		Response oStatus = new Response();
		/*Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("profileName", currentProfile);
        nameValMap.put("originalProfile", originalProfile);
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oStatus!= null && oStatus.getStatus()!=200){
    		return oStatus;
    	}*/
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
			forgetFixtureCurve(fixtureId);
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
//			gemsGroupManager.deleteGemsGroupsFromFixture(fixtureId);
			oResponse.setStatus(fixtureManager
					.deleteFixtureWithoutAck(fixtureId));
			oResponse.setMsg(String.valueOf(fixtureId)); // using message as
															// current fixture
															// id
			forgetFixtureCurve(fixtureId);
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
	 * Force delete fixtures  
	 * @param fixtures
	 *            List of fixtures
	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("forcedelete")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response forceDeleteFixtures(List<Fixture> fixtures) {
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
						
			oResponse.setStatus(fixtureManager
					.forceDeleteFixture(fixtureId));
			oResponse.setMsg(String.valueOf(fixtureId)); // using message as
															// current fixture
															// id
			forgetFixtureCurve(fixtureId);
			break;
		}
		if(oResponse.getStatus() == 1)
			userAuditLoggerUtil.log(
				"Decommission, force delete, fixture "
						+ fixtureName + "(Status = Success)", UserAuditActionType.Fixture_Commission.getName());
		else
			userAuditLoggerUtil.log(
					"Decommission, force delete, fixture "
							+ fixtureName + "(Status = Failure)", UserAuditActionType.Fixture_Commission.getName());

		return oResponse;
	}

	/**
	 * Reboots the sensor alternate currently application 1
	 * 
	 * @param fixture
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
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
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("list/report/fixtureOutage/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureOutageVO> getFixtureOutageList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		Response resp = new Response();
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return new ArrayList<FixtureOutageVO>();
    	}
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/all/discovered")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteAllDiscoveredFixtures() {
		
		Response oResponse = new Response();
		int deviceCount = 0;
		List<Fixture> fixtures = fixtureManager.loadFixtureByState(ServerConstants.FIXTURE_STATE_DISCOVER_STR);		
		if(fixtures != null) {
			Iterator<Fixture> itr = fixtures.iterator();
			long fixtureId = 0;
			Fixture fixture = new Fixture();
			String fixtureName = "";			
			while (itr.hasNext()) {
				fixture = (Fixture) itr.next();
				fixtureId = fixture.getId();
				//Let's find the name of fixture from cache. Let's put this in try/catch as failure
				//to find the name should not stop decommission.
				try {
					if(FixtureCache.getInstance().getDevice(fixture.getId()) != null) {
						fixtureName = FixtureCache.getInstance().getDevice(fixture.getId()).getFixtureName();
					} else{
						fixtureName = String.valueOf(fixtureId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fixtureManager.deleteFixture(fixtureId);
				deviceCount++;
				userAuditLoggerUtil.log("Deleted fixture " + fixtureName + "(Status = " + oResponse.getStatus() + ")",
						UserAuditActionType.Fixture_Discovered_Delete.getName());											
			}			
		} 
		
		List<Plugload> plugloads = plugloadManager.loadPlugloadsByState(ServerConstants.PLUGLOAD_STATE_DISCOVER_STR);		
		if(plugloads != null) {
			Iterator<Plugload> itr = plugloads.iterator();
			long plugloadId = 0;
			Plugload plugload = new Plugload();
			String plugloadName = "";			
			while (itr.hasNext()) {
				plugload = (Plugload) itr.next();
				plugloadId = plugload.getId();
				//Let's find the name of plugload from cache. Let's put this in try/catch as failure
				//to find the name should not stop decommission.
				try {
					if(PlugloadCache.getInstance().getCachedPlugload(plugload.getId()) != null) {
						plugloadName = PlugloadCache.getInstance().getCachedPlugload(plugload.getId()).getName();
					} else{
						plugloadName = String.valueOf(plugloadId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				plugloadManager.deletePlugload(plugloadId);
				deviceCount++;
				userAuditLoggerUtil.log("Deleted plugload " + plugloadName + "(Status = " + oResponse.getStatus() + ")",
						UserAuditActionType.Plugload_Discovered_Delete.getName());											
			}			
		}		
		
		if(deviceCount == 0) {
			oResponse.setStatus(0);
			oResponse.setMsg(String.valueOf(0));
			return oResponse ;
		} else {
			oResponse.setStatus(1);
			oResponse.setMsg(String.valueOf(deviceCount));
			return oResponse;
		}
		
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete/all/placed")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteAllPlacedFixtures() {
		
		Response oResponse = new Response();
		int deviceCount = 0;
		
		List<Fixture> fixtures = fixtureManager.loadFixtureByState(ServerConstants.FIXTURE_STATE_PLACED_STR);
		if(fixtures!=null) {
			Iterator<Fixture> itr = fixtures.iterator();		
			long fixtureId = 0;
			Fixture fixture = new Fixture();
			String fixtureName = "";			
			while (itr.hasNext()) {
				fixture = (Fixture) itr.next();
				fixtureId = fixture.getId();
				//Let's find the name of fixture from cache. Let's put this in try/catch as failure
				//to find the name should not stop decommission.
				try {
					if(FixtureCache.getInstance().getDevice(fixture.getId()) != null){
						fixtureName = FixtureCache.getInstance().getDevice(fixture.getId()).getFixtureName();
					}else{
						fixtureName = String.valueOf(fixtureId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fixtureManager.deleteFixture(fixtureId);
				deviceCount++;		
				userAuditLoggerUtil.log("Deleted fixture " + fixtureName + "(Status = " + oResponse.getStatus() + ")",
						UserAuditActionType.Placed_Fixture_Delete.getName());		
			}			
		}
			
		List<Plugload> plugloads = plugloadManager.loadPlugloadsByState(ServerConstants.PLUGLOAD_STATE_PLACED_STR);		
		if(plugloads != null) {
			Iterator<Plugload> itr = plugloads.iterator();
			long plugloadId = 0;
			Plugload plugload = new Plugload();
			String plugloadName = "";			
			while (itr.hasNext()) {
				plugload = (Plugload) itr.next();
				plugloadId = plugload.getId();
				//Let's find the name of plugload from cache. Let's put this in try/catch as failure
				//to find the name should not stop decommission.
				try {
					if(PlugloadCache.getInstance().getCachedPlugload(plugload.getId()) != null) {
						plugloadName = PlugloadCache.getInstance().getCachedPlugload(plugload.getId()).getName();
					} else{
						plugloadName = String.valueOf(plugloadId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				plugloadManager.deletePlugload(plugloadId);
				deviceCount++;
				userAuditLoggerUtil.log("Deleted plugload " + plugloadName + "(Status = " + oResponse.getStatus() + ")",
						UserAuditActionType.Plugload_Discovered_Delete.getName());											
			}			
		}		
		
		if(deviceCount == 0) {
			oResponse.setStatus(0);
			oResponse.setMsg(String.valueOf(0));
			return oResponse ;
		} else {
			oResponse.setStatus(1);
			oResponse.setMsg(String.valueOf(deviceCount));
			return oResponse;
		}	
		
	}
	
	
	/**
	 * Service to do a rma replacement
	 * @param fromFixtureId - Fixture which needs to be replaced
	 * @param toFixtureId - New Fixture
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("rma/{from}/{to}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rmaFixture(@PathParam("from")Long fromFixtureId, @PathParam("to")Long toFixtureId) {
	    FixtureCache.getInstance().invalidateDeviceCache(fromFixtureId);
        FixtureCache.getInstance().invalidateDeviceCache(toFixtureId);
        Fixture oldFixture = fixtureManager.getFixtureById(fromFixtureId);
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
        
        // Send the hopper command
        m_Logger.info("Restoring hopper settings from old fixture to new fixture...");
        System.out.println("Restoring hopper settings from old fixture to new fixture...");
        fixtureManager.enablePushProfileAndGlobalPushProfile(fromFixtureId, true, true);
        if (oldFixture.getIsHopper() == 0) {
            fixtureManager.enableHopper(fromFixtureId, false);
        } else {
            fixtureManager.enableHopper(fromFixtureId, true);
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("ballast/list")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Ballast> getBallastList() {
	
		List<Ballast> ballastList = fixtureManager.getAllBallasts();		
		return ballastList;
		
	} //end of method getBallastList
	
	/**
     * Returns bulbs list
     *  
     * @return bulbs list
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("bulbs/list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BulbVO> getBulbsVOList() {
    
        List<BulbVO> bulbsList = fixtureManager.getAllBulbVO();
        return bulbsList;
        
    } //end of method getBulbsList

    /**
  	 * Sends a manual calibration command to selected fixtures.
  	 * 
  	 * @param fixtures
  	 *            List of fixtures
  	 *            "<fixtures><fixture><id>1</id></fixture></fixtures>"
  	 * @return Response status
  	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
  	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
  	@Path("op/setAmbientThreshold/{isAuto}/{thresholdValue}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response setFixturesAmbientThresholdValue(List<Fixture> fixtures,@PathParam("isAuto") boolean isAuto,@PathParam("thresholdValue") int thresholdValue) {
        
  	  m_Logger.debug("Fixtures: " + fixtures.size());
      StringBuilder fixtString = new StringBuilder("");
      Response oStatus = new Response();
//      Map<String,Object> nameValMap = new HashMap<String, Object>();
//      nameValMap.put("isAuto", isAuto);
//      nameValMap.put("thresholdValue", thresholdValue);
//      oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
//      if(oStatus!= null && oStatus.getStatus()!=200){
//    	  return oStatus;
//      }
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
      fixtureManager.setFixturesAmbientThresholdValue(fixtureList,isAuto,thresholdValue);
              
      userAuditLoggerUtil.log("Setting fixtures Ambient Threshold value" + fixtString,
              UserAuditActionType.Fixture_Calibration.getName());
      
        return new Response();
    }
  	
  		/**
	 * Send enable or disable hopper command to set of fixtures.
	 * 
	 * @param enable
	 * @param fixtures
	 * @return response status of this service call. (The hooper command status
	 *         is async and needs to be verified via the database or via FX
	 *         stats)
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/hopper/{enabled}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enabledisableHopper(@PathParam("enabled") Boolean enable,
			List<Fixture> fixtures) {
		m_Logger.debug("Fixtures: " + fixtures.size());
		StringBuilder fixtString = new StringBuilder("");
		Response oStatus = new Response();
	   /* oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "isAuto", enable);
	    if(oStatus!= null && oStatus.getStatus()!=200){
	    	return oStatus;
	    }*/
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


	/**
	 * Making placed sensors hopper requires the current gateway's context. So make sure that the 
	 * current gateway is now the actual gateway for these fixtures to proceed.
	 * 
	 * NOTE: Expectation here is that PM stat / Fx Stat / nodeboot info / Discovery for none of these placed fixtures is going to be coming in as these
	 * auto update the gateway id for the fixture, which send the data for the fixture.
	 * @param enable
	 * @param gwid
	 * @param fixtures
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/hopper/{enabled}/gw/{gwid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enabledisablePlacedHopperByGwId(@PathParam("enabled") Boolean enable,
			@PathParam("gwid") Long gwid,
			List<Fixture> fixtures) {
		Response oResponse = new Response();
		/*oResponse = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "isAuto", enable);
	    if(oResponse!= null && oResponse.getStatus()!=200){
	    	return oResponse;
	    }*/
    	Gateway gw = gatewayManager.loadGateway(gwid);
    	if (gw != null) {
			m_Logger.debug("Updating, Selected gateway: " + gw.getId() + " "
					+ gw.getMacAddress() + " {fx: " + fixtures.size() + "}");
			
			fixtureManager.updateFixtureSecGw(fixtures, gw);

			m_Logger.debug("Processing, Selected gateway: " + gw.getId() + " "
					+ gw.getMacAddress() + " {fx: " + fixtures.size() + "}");
			
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
    	}else {
    		oResponse.setStatus(1);
    	}
		return oResponse;

	}



	/**
	 * returns Hopper status of the fixture
	 * 
	 * @param fixtureId
	 *            Fixture unique identifier
	 * @return Hopper status as part of response object
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("getfixturehopperstatus/fixtureId/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureHopperStatus(
			@PathParam("fixtureId") int fixtureId) {
		Response oStatus = new Response();
		oStatus.setMsg(fixtureManager.getFixtureHopperStatus(fixtureId).toString());
		oStatus.setStatus(fixtureId);
		return oStatus;
	}

		/**
	 * Send enable or disable hopper command to set of fixtures.
	 * 
	 * @param enable
	 * @param fixtures
	 * @return response status of this service call. (The hooper command status
	 *         is async and needs to be verified via the database or via FX
	 *         stats)
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/fixturetype/{fxtype}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response setFixtureType(@PathParam("fxtype") Integer fxType, List<Fixture> fixtures) {
		Response resp = new Response();

		if(fixtures == null)
			return resp;
		/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "thresholdValue", fxType);
	    if(resp!= null && resp.getStatus()!=200){
	    	return resp;
	    }*/
		//Derive all fixture having su version >2.0 and CU version > 32
        List<Fixture> calibratedFixtures = new ArrayList<Fixture>();
		for(Fixture fx:fixtures )
		{
		    if(ServerUtil.isNewCU(fx) && fxType==1)
		    {
		        calibratedFixtures.add(fx);
		    }
			fixtureManager.updateFixtureType(fx.getId(), fxType);
		}
		if(fxType==1)
		{
		    getFixturesBaseline(calibratedFixtures,(byte) 1);
		}
		return resp;
	}


   /* *
   	 * Sends Fixture placement information
   	 * 
   	 * @param fixturePlacementInfo
   	 *            List of fixturePlacementInfo
   	 * @return Response status
   	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
   	@Path("send/placementinfo")
   	@POST
   	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   	public Response setPlacementInfo(PlacementInfoVO placementInfo) {
   		Boolean success = true ;
   		Response response = new Response() ;
   		String error = null ;
   		int status=200;
   		String message="success";   		
   		if(!companyManager.getCompany().getName().equals(placementInfo.getCompanyName()))
   		{
   			error = "error";
   			status = 600;
   			response.setMsg(error);
   			response.setStatus(status);
   			return response;   			
   		}
   		 try {
		   	    String location = "";
		   		// Get the facility ids
		        Campus campus = campusManager.getCampusByName(placementInfo.getCampusName());
		        if(campus == null)
		        {
		   			response.setMsg("error");
		   			response.setStatus(601);
		   			return response;   			
		        }
		        long campusId = campus.getId();
		        Building building = buildingManager.getBuildingByNameAndCampusId(placementInfo.getBldgName(), campusId);
		        if(building == null)
		        {
		   			response.setMsg("error");
		   			response.setStatus(602);
		   			return response;   			
		        }
		        long buildingId = building.getId();
		   		Floor floor = floorManager.getFloorByNameAndBuildingId(placementInfo.getFloorName(), buildingId);
		   		if(floor == null) {
		   			response.setMsg("error");
		   			response.setStatus(603);
		   			return response;   			
		   		}
		        location = floor.getName();
		        location = building.getName() + "->" + location;
		        location = campus.getName() + "->" + location;
		   		
		   		List<SensorConfig> configList = placementInfo.getSensorConfigList();
		   		m_Logger.debug("configInfos: " + configList.size());
		   		
		   		Iterator<SensorConfig> iterator = configList.iterator() ;
		   		while(iterator.hasNext())
		   		{
		   			SensorConfig info = iterator.next() ;
		   			if(info.getType().equalsIgnoreCase(String.valueOf(SensorConfig.typeUnmanaged)))
		   			{
		   		       locatorDeviceManager.addLocatorDevice(info.getName(), "Unmanaged_fixture", 0L,floor.getId().toString(), info.getX().toString(), info.getY().toString()); 
		   		       continue;
		   			}
		   			if(info.getType().equalsIgnoreCase(String.valueOf(SensorConfig.typePlugload))) {
		   				Plugload plugload = plugloadManager.getPlugloadBySnapAddress(info.getMac());
		   				if(plugload != null && plugload.getState().equals(ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR)) {
		   					m_Logger.error("Plugload with this macAddress is already commissioned. macAddress" + info.getMac());
			   				eventsAndFaultManager.addEvent("Placed Plugload with this macAddress is already commissioned. macAddress: " + info.getMac(), EventsAndFault.PLACED_PLUGLOAD_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
			   				continue;
		   				}
		   				if(plugload == null) {
		   					plugload = new Plugload();
		   				} else {
		   					if(m_Logger.isInfoEnabled()) {
		   						m_Logger.info("Plugload with this macAddress is already present. Updating it's information " + 
		   								info.getMac());
		   					}
		   				}	   				
		
					    plugload.setLastOccupancySeen(0);				   
					    plugload.setAvgVolts(0.0f);
					    plugload.setVoltage((short)277);		// Update the voltage TODO
					    plugload.setXaxis(info.getX().intValue());
					    plugload.setYaxis(info.getY().intValue());
					    plugload.setCurrentState(ServerConstants.CURR_STATE_AUTO_STR);
					    plugload.setUpgradeStatus("");
					    plugload.setGroupsSyncPending(false);					    
					    
					    // ServerConstants.DEFAULT_PROFILE is the default
					    Long groupDefaultId = 1L;
					    PlugloadGroups oGroups = plugloadGroupManager.getPlugloadGroupByName(ServerConstants.DEFAULT_PROFILE);
					    if (oGroups != null) {
					      groupDefaultId = oGroups.getId();
					    }
					    plugload.setCurrentProfile(ServerConstants.DEFAULT_PROFILE);
					    plugload.setOriginalProfileFrom(ServerConstants.DEFAULT_PROFILE);
					    plugload.setActive(true);
					    plugload.setLastConnectivityAt(new Date());
					    plugload.setLastStatsRcvdTime(new Date());
					    plugload.setGroupId(groupDefaultId);
					     
					    plugload.setMacAddress(info.getMac());
					    plugload.setSnapAddress(info.getMac());
					    
					    plugload.setFloor(floor);      
					    plugload.setBuildingId(buildingId);
					    plugload.setCampusId(campusId);
					    plugload.setLocation(location);

					    plugload.setVersion(info.getVersion());
					    plugload.setModelNo(info.getModelNo());
					    plugload.setIsHopper(info.getIsHopper().intValue());
					  	plugload.setHlaSerialNo(info.getSerialNo());
					  	String plugloadName = "Plugload" + ServerUtil.generateName(info.getMac());
					    plugload.setName(plugloadName);
					    
					    plugload.setArea(null);
					    
					    plugload.setCuVersion("40");
					    plugload.setGateway(null);
					    plugload.setSecGwId(null);
					    plugload.setState(ServerConstants.PLUGLOAD_STATE_PLACED_STR);
					    plugload.setVersionSynced(0);
					    plugload.setCommissionStatus(ServerConstants.COMMISSION_STATUS_UNKNOWN);
					    
					    plugloadManager.save(plugload);
					    PlugloadCache.getInstance().invalidateDeviceCache(plugload.getSnapAddress());
			   			userAuditLoggerUtil.log(" plugload " + info.getMac() + " placement info updated ", 
			   	   				UserAuditActionType.Plugload_Update.getName());
			   			success = true ;		   				
		   				continue;
		   			}
		   			Fixture fixture = fixtureManager.getFixtureByMacAddr(info.getMac());
		   			if(fixture != null && fixture.getState().equals(ServerConstants.FIXTURE_STATE_COMMISSIONED_STR))
		   			{
		   				m_Logger.error("Fixture with this macAddress is already commissioned. macAddress" + info.getMac());
		   				eventsAndFaultManager.addEvent("Placed Fixture with this macAddress is already commissioned. macAddress: " + info.getMac(), EventsAndFault.PLACED_FIXTURE_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
		   				continue;
		   			}
					if(fixture == null)
						fixture = new Fixture();
					else
		   				m_Logger.error("Fixture with this macAddress is already present. Updating it's information " + info.getMac());
						
					FixtureClass fxClass = fixtureClassManager.getFixtureClassByName(info.getFixtureType());
					if(fxClass != null){
					fixture.setFixtureClassId(fxClass.getId());				// Set the fixture class id
		   			Ballast ballast = ballastManager.getBallastById(fxClass.getBallast().getId());
		   			if(ballast != null) {
		   				fixture.setBallast(ballast);
		   			}
		   			Bulb bulb = bulbManager.getBulbById(fxClass.getBulb().getId());
		   			if(bulb != null) {
					    fixture.setBulb(bulb);
		   			}
				    
					fixture.setNoOfFixtures(fxClass.getNoOfBallasts());			// Update number of ballast
				    fixture.setLastOccupancySeen(0);
				    fixture.setNoOfBulbs(ballast.getLampNum());
				    fixture.setBulbWattage(ballast.getWattage());
				    fixture.setWattage(ballast.getWattage() * ballast.getLampNum());
				    fixture.setBulbManufacturer(bulb.getManufacturer());    
				    fixture.setBallastManufacturer(ballast.getBallastManufacturer());
				    fixture.setBaselinePower(new BigDecimal(0));
				    fixture.setDimmerControl(0);
				    fixture.setVoltage(fxClass.getVoltage().shortValue());		// Update the voltage
				    fixture.setXaxis(info.getX().intValue());
				    fixture.setYaxis(info.getY().intValue());
				    fixture.setCurrentState(ServerConstants.CURR_STATE_AUTO_STR);
				    fixture.setUpgradeStatus("");
				    fixture.setGroupsSyncPending(false);
				    fixture.setGroupsChecksum(0);
				    
				    // ServerConstants.DEFAULT_PROFILE is the default
				    Long groupDefaultId = 1L;
				    Groups oGroups = groupManager.getGroupByName(ServerConstants.DEFAULT_PROFILE);
				    if (oGroups != null) {
				      groupDefaultId = oGroups.getId();
				    }
				    fixture.setCurrentProfile(ServerConstants.DEFAULT_PROFILE);
				    fixture.setOriginalProfileFrom(ServerConstants.DEFAULT_PROFILE);   
				    
				    fixture.setBulbLife((double)100);
				    fixture.setActive(true);
				    fixture.setLastConnectivityAt(new Date());
				    fixture.setLastStatsRcvdTime(new Date());
				    fixture.setGroupId(groupDefaultId);
				     
				    fixture.setMacAddress(info.getMac());
				    fixture.setSnapAddress(info.getMac());
				    fixture.setCommType(ServerConstants.COMM_TYPE_ZIGBEE);
				    
				    fixture.setFloor(floor);      
				    fixture.setBuildingId(buildingId);
				    fixture.setCampusId(campusId);
				    fixture.setLocation(location);

			      	fixture.setVersion(info.getVersion());
			      	fixture.setModelNo(info.getModelNo());
			      	fixture.setIsHopper(info.getIsHopper().intValue());
				  	fixture.setHlaSerialNo(info.getSerialNo());
				  	String fixtureName = "Sensor" + ServerUtil.generateName(info.getMac());
				    fixture.setSensorId(fixtureName);
				    fixture.setFixtureName(fixtureName);
				    
				    fixture.setArea(null);
				    fixture.setSubArea(null);
				     
		/*		    fixture.setChannel((int)pkt[i++]);
			      	fixture.setBootLoaderVersion(bootLoaderVer);
			      	fixture.setCuVersion(null);
				  	fixture.setFirmwareVersion(app1Ver);*/
				  
			      	//TODO need to add cuVersion to the SensorConfig XML and if it is null in the XML, default to "40" which is not used
					//that makes it new CU
			      	fixture.setCuVersion("40");
				    fixture.setGateway(null);
				    fixture.setSecGwId(null);
				    fixture.setState(ServerConstants.FIXTURE_STATE_PLACED_STR);
				    fixture.setVersionSynced(0);
				    fixture.setCommissionStatus(ServerConstants.COMMISSION_STATUS_UNKNOWN);
				    
				    fixtureManager.save(fixture);
					FixtureCache.getInstance().invalidateDeviceCache(fixture.getSnapAddress());
		   			userAuditLoggerUtil.log(" fixtures " + info.getMac() + " placement info updated ", 
		   	   				UserAuditActionType.Fixture_Update.getName());
		   			success = true ;
					}
					else
					{
						status = 300;
						message = "error";
						//log the fixture type mentioned does not exist in the em for said fixture.
						m_Logger.error("Fixture Type " + info.getFixtureType() + " does not exist on Em , Ignored placement of fixture : "+info.getMac());
					}
		   		}
   		 }
   		 catch (Exception e)
   		 {
   			m_Logger.error(e.getMessage(), e) ;
   			e.printStackTrace() ;
   			error = e.getMessage() ;
   			success = false ;
   			status = 500;
   			message = "error";
   		 }
   		response.setMsg(message);
   		response.setStatus(status);
   		return response;   		
   	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("configuration/list/floor/{fid}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SensorConfig> getPlacedSensorConfigList(@PathParam("fid") Long id) {
		
		List<PlacedFixture> plFixtures = placedFixtureManager.loadFixtureByFloorId(id);
		
		List<SensorConfig> configList = new ArrayList<SensorConfig>();
		
		if(plFixtures != null) {
			for(PlacedFixture fx : plFixtures)
			{
				SensorConfig cfg = new SensorConfig();
				cfg.setBallastName(fx.getBallast().getBallastName());
				cfg.setBulbName(fx.getBulb().getBulbName());
				cfg.setMac(fx.getMacAddress());
				cfg.setNoOfBallasts(fx.getNoOfFixtures().longValue());
				cfg.setVoltage((long)fx.getVoltage());
				cfg.setX(fx.getXaxis().longValue());
				cfg.setY(fx.getYaxis().longValue());
				
				configList.add(cfg);
			}
		}
		
		return configList;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("placed/list/floor/{fid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getPlacedFixtureList(@PathParam("fid") Long id) {
		
		List<Fixture> fixtures = fixtureManager.loadPlacedFixtureByFloorId(id);

		return fixtures;
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("placedandcommissioned/list/{campusName}/{bldgName}/{floorName}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getPlacedAndCommissionedFixtureList(@PathParam("campusName")String campusName, @PathParam("bldgName")String bldgName, @PathParam("floorName")String floorName) throws SQLException, IOException {
		
		List<Fixture> fixtures = new ArrayList<Fixture>();
		
    	campusName = URLDecoder.decode(campusName, "UTF-8");
    	bldgName = URLDecoder.decode(bldgName, "UTF-8");
    	floorName = URLDecoder.decode(floorName, "UTF-8");
    	
    	Response oStatus = new Response();
        Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("campusName", campusName);
        nameValMap.put("buildingName", bldgName);
        nameValMap.put("floorName", floorName);
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(oStatus!= null && oStatus.getStatus()!=200){
        	m_Logger.error("Validation error"+oStatus.getMsg());
      	  return fixtures;
        }
    	
    	Campus campus = campusManager.getCampusByName(campusName);
        if(campus == null)
        {
        	return fixtures;
        }
        long campusId = campus.getId();
        Building building = buildingManager.getBuildingByNameAndCampusId(bldgName, campusId);
        if(building == null)
        {
        	return fixtures;
        }
        long buildingId = building.getId();
   		Floor floor = floorManager.getFloorByNameAndBuildingId(floorName, buildingId);
   		if(floor == null) {
        	return fixtures;
   		}

		fixtures = fixtureManager.loadPlacedAndCommissionedFixtureByFloorId(floor.getId());
		
		return fixtures;
	}

	/**
     * Upload / save caliberation information for the given fixture
     * 
     * @param flc
     * @return response status of saving to DB operation
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("op/savecalibration")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveCalibrationInfo(FixtureLampCalibration flc) {
        Response oResponse = new Response();
        oResponse.setStatus(DeviceServiceImpl.getInstance().saveCalibrationInfo(flc));
        return oResponse;
    }

    /**
     * 
     * @param fixtureId
     * @return
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getcalibration/id/{fixtureId}")
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureLampCalibration getCalibrationInfo(@PathParam("fixtureId") Long fixtureId) {
        return fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
    }
    /**
	 * Fetch LampOut Fixtures list
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/lampout/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureLampStatusVO> getLampOutStatusFixtureList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		Response oStatus = new Response();
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(oStatus!= null && oStatus.getStatus()!=200){
        	m_Logger.error("Validation error"+oStatus.getMsg());
      	  return new ArrayList<FixtureLampStatusVO>();
        }
		return fixtureManager.getLampOutStatusFixtureList(property, pid);
	}
	
	/**
	 * Fetch LampOut Fixtures list
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/fixtureout/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureLampStatusVO> getFixtureOutStatusFixtureList(@PathParam("property") String property, @PathParam("pid") Long pid) {
		Response oStatus = new Response();
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(oStatus!= null && oStatus.getStatus()!=200){
        	m_Logger.error("Validation error"+oStatus.getMsg());
      	  return new ArrayList<FixtureLampStatusVO>();
        }
		return fixtureManager.getFixtureOutStatusFixtureList(property, pid);
	}
	
	/**
	 * Fetch LampOut Fixtures list
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/calibrated/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureLampStatusVO> getCalibratedFixtureList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		Response oStatus = new Response();
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "property", property);
        if(oStatus!= null && oStatus.getStatus()!=200){
        	m_Logger.error("Validation error"+oStatus.getMsg());
      	  return new ArrayList<FixtureLampStatusVO>();
        }
		return fixtureManager.getCalibratedFixtureList(property, pid);
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("op/getbaseline/calibration/action/{calibration}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getFixturesBaseline(List<Fixture> fixtures, @PathParam("calibration") Byte calibration) {
        m_Logger.debug("Fixtures: " + fixtures.size());
        /*Response oStatus = new Response();
        oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "calibration", calibration);
        if(oStatus!= null && oStatus.getStatus()!=200){
      	  return oStatus;
        }*/
        return fixtureManager.getFixturesBaseline(fixtures,calibration);
       
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getDimLevels")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getDimLevels() {
		return JsonUtil.getJSONString(fixtureManager.getDimLevels());
	}
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getFixtureOutages")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getFixtureOutages() {
		return JsonUtil.getJSONString(fixtureManager.getFixtureOutages());
	}
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getLastOccupancy")
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM})
	public String getLastOccupancy() {
		return JsonUtil.getJSONString(fixtureManager.getLastOccupancy());
	}
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
    @Path("getRealTimeStats")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public String getRealTimeStats() {
    	return JsonUtil.getJSONString(fixtureManager.getRealTimeStats());
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("fixtureCalibrationMap/updateEnabledFlag/")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public com.ems.ws.util.Response updateFixtureCalibrationMap(FixtureVoltPowerList fixtureVoltPowerList) {
		com.ems.ws.util.Response resp = new com.ems.ws.util.Response();
		List<FixtureCalibrationMap> fixtureCalibrationMapList = null;
		List<Fixture> fixtureIdList = null;
		StringBuilder fixtString = new StringBuilder("");
		if(fixtureVoltPowerList!=null)
		{
			fixtureCalibrationMapList = fixtureVoltPowerList.getFixtureCalibrationMap();
			fixtureIdList = fixtureVoltPowerList.getFixture();
		}
		if(fixtureIdList!=null && !fixtureIdList.isEmpty())
		{
			Iterator<Fixture> fixtIdItr = fixtureIdList.iterator();
			while (fixtIdItr.hasNext()) {
				Fixture currFixture = fixtIdItr.next();
				Long fixtureId = currFixture.getId();
				fixtString.append(currFixture.getFixtureName() + ",");
				if(fixtureCalibrationMapList != null && fixtureCalibrationMapList.size() > 0) {
					fixtureCalibrationManager.updateFixtureCalibrationMap(fixtureCalibrationMapList,fixtureId);
				}
			}
		}
		userAuditLoggerUtil.log("Enable/Disable Volt Participation in Lamp Outage for fixtures " + fixtString, UserAuditActionType.Enable__Disable_Volt_Participation_In_LORP.getName());
		resp.setMsg("S");
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/voltpowermap/{fixtureId}/{type}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<FixtureCalibrationMap> loadFixtureVoltPowerList(@PathParam("fixtureId") Long fixtureId,@PathParam("type") String type) {	
		List<FixtureCalibrationMap> fixtureVoltPower = fixtureCalibrationManager.getAllFixtureVoltPowersFromId(fixtureId);
		ArrayList<FixtureCalibrationMap> fixtVoltPowerList = new ArrayList<FixtureCalibrationMap>();
		if (fixtureVoltPower != null && !fixtureVoltPower.isEmpty()) {
			Iterator<FixtureCalibrationMap> fixtVoltpowerIt = fixtureVoltPower.iterator();
			while (fixtVoltpowerIt.hasNext()) {
				FixtureCalibrationMap dataArr = fixtVoltpowerIt.next();
				FixtureCalibrationMap fxVoltPower = new FixtureCalibrationMap();
				fxVoltPower.setId(dataArr.getId());
				fxVoltPower.setVolt(dataArr.getVolt());
				fxVoltPower.setPower(dataArr.getPower());
				fxVoltPower.setLux(dataArr.getLux());
				if(type.equals("single"))
				{
					fxVoltPower.setEnabled(dataArr.getEnabled());
				}else
				{
					fxVoltPower.setEnabled(true);
				}
				fixtVoltPowerList.add(fxVoltPower);
			}
		}
		return fixtVoltPowerList;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("setDimLevel/{percentage}/{time}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response setDimLevel(@PathParam("percentage") int percentage, @PathParam("time") int time, List<Fixture> fixturesList) {
		Response resp = new Response();
		if(fixturesList != null && fixturesList.size() > 0) {
			int[] fixtureIds = new int[fixturesList.size()];
			int cnt = 0;
			for(Fixture fixt: fixturesList) {
				Fixture f = fixtureManager.getFixtureByMacAddr(fixt.getMacAddress());
				fixtureIds[cnt++] = Integer.parseInt(f.getId().toString());
			}
			DeviceServiceImpl deviceServiceImpl =  DeviceServiceImpl.getInstance();
			deviceServiceImpl.absoluteDimFixtures(fixtureIds, percentage, time);
			ServerUtil.sleep(2);
			deviceServiceImpl.getCurrentState(fixtureIds);
			ServerUtil.sleepMilli(50);
			deviceServiceImpl.getCurrentState(fixtureIds);
		}
		resp.setMsg("S");
		return resp;
	}
	
	/**
	 * Get the Lamp Out Feature Status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("lampoutagefeature/status/")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getLampoutageFeatureStatus() {
		Response oResponse = new Response();
		SystemConfiguration bulbConfigurationEnableConfig = systemConfigurationManager.loadConfigByName("bulbconfiguration.enable");
        if (bulbConfigurationEnableConfig != null && "false".equalsIgnoreCase(bulbConfigurationEnableConfig.getValue())) {
        	  oResponse.setStatus(-1);
        }
        return oResponse;
	}
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/getexportdata")
	@POST
	@Produces("application/csv")
	public javax.ws.rs.core.Response getFixtureCSVListById(
			@FormParam("fixtureidfixturecsv") Long fixtureId)
			throws UnsupportedEncodingException, ParseException {
		
		StringBuffer output = new StringBuffer("");		
		List<FixtureCalibrationMap> fixtureVoltPower = fixtureCalibrationManager.getAllFixtureVoltPowersFromId(fixtureId);
		FixtureLampCalibration fxLc = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
		Fixture fixture = fixtureManager.getFixtureById(fixtureId);
		NumberFormat nf = NumberFormat.getInstance();
       	nf.setMaximumFractionDigits(2);
		String ballastDisplayLabel = fixture.getBallast().getBallastName();
		output.append("#ballast type,"+ ballastDisplayLabel.trim()+"\r\n");
		output.append("#no. lamps,"+ fixture.getBallast().getLampNum().toString().trim()+"\r\n");
		output.append("#lamp manufacturer,"+ fixture.getBallast().getBallastManufacturer().toString().trim()+"\r\n");
		output.append("#lamp type,"+ fixture.getBallast().getLampType()+"\r\n");
		output.append("#lamp wattage,"+ fixture.getBallast().getWattage().toString().trim()+"\r\n");
		output.append("#line voltage,"+ fixture.getVoltage().toString().trim()+"\r\n");
		output.append("#MAC address,"+ ServerUtil.generateMACAddress(fixture.getMacAddress().toString().trim())+"\r\n");
		output.append("#characterized,"+ fxLc.getCaptureAt().toString()+"\r\n");
		output.append("#warm-up,"+ fxLc.getWarmupTime().toString().trim()+"\r\n");
		output.append("#stabilization,"+ fxLc.getStabilizationTime().toString().trim()+"\r\n");
		output.append("Light level"+","+"Power"+","+"Include\r\n");
		for (Iterator iterator = fixtureVoltPower.iterator(); iterator
				.hasNext();) {
			FixtureCalibrationMap fixtureCalibrationMap = (FixtureCalibrationMap) iterator
					.next();
			int enabled = 0;
			if(fixtureCalibrationMap.getEnabled().equals(true))
			{
				enabled =1;
			}
			output.append(fixtureCalibrationMap.getVolt()*10+","+nf.format(fixtureCalibrationMap.getPower())+","+enabled);
			
			output.append("\r\n");			
		}		
		String fileName = ballastDisplayLabel.trim()+".csv";
		return javax.ws.rs.core.Response
		.ok(output.toString(), "text/csv")
		.header("Content-Disposition",
				"attachment;filename=\""+fileName+"\"")
		.build();
	}
	
	public void forgetFixtureCurve(Long fixtureId) {
		com.ems.ws.util.Response resp = new com.ems.ws.util.Response();
		Fixture fixture = fixtureManager.getFixtureById(fixtureId);
		// Set User fixture curve to false, this will ensure that the Download Fixture curve won't download the curve for this fixture.
		fixtureManager.setUseFxCurveFlag(fixtureId, false);
		// Clearing outstating alarms for the fixture (related to outages)
		eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
		eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
		// Cleanup the fixture curve tables.
		fixtureCalibrationManager.deleteFixtureCurve(fixtureId);
		// Invalidate cache
		FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
		userAuditLoggerUtil.log("Deleted Fixture Curve for fixture " + fixture.getFixtureName(), UserAuditActionType.Forget_Fixture.getName());
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("forgetAllFixtureCurve")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public com.ems.ws.util.Response forgetAllFixtureCurve(List<Fixture> fixturesList) {
		com.ems.ws.util.Response resp = new com.ems.ws.util.Response();
		StringBuilder fixtString = new StringBuilder("");
		if(fixturesList != null && fixturesList.size() > 0) {
			for(Fixture fixt: fixturesList) {
				Long fixtureId = fixt.getId();
				Fixture fixture = fixtureManager.getFixtureById(fixtureId);
				fixtString.append(fixture.getFixtureName() + ",");
				// Set User fixture curve to false, this will ensure that the Download Fixture curve won't download the curve for this fixture.
				fixtureManager.setUseFxCurveFlag(fixtureId, false);
				// Clearing outstating alarms for the fixture (related to outages)
				eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
				eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
				// Cleanup the fixture curve tables.
				fixtureCalibrationManager.deleteFixtureCurve(fixtureId);
				// Invalidate cache
				FixtureCache.getInstance().invalidateDeviceCache(fixtureId);
				userAuditLoggerUtil.log("Deleted Fixture Curve for fixtures " + fixtString, UserAuditActionType.Forget_Fixture.getName());
			}
		}
		resp.setMsg("S");
		return resp;
	}
	
	
	/**
	 * Returns Fixture Details Object
	 * 
	 * @param fixtureId
	 *            fixture unique identifier
	 * @return fixture details Object
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("detailsobject/{fixtureId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public FixtureDetails getFixtureDetailsObject(@PathParam("fixtureId") Long fixtureId) {
		
		List<FixtureClass> fixtureclasses = fixtureClassManager.loadAllFixtureClasses();
		
		FixtureDetails fixtureDetails = new FixtureDetails();
		
		Fixture fixture = fixtureManager.getFixtureById(fixtureId);
		
		SystemConfiguration temperatureConfig = systemConfigurationManager.loadConfigByName("temperature_unit");
		String dbTemp = "";
		String temperatureunit= "";
		if (temperatureConfig != null) {
			dbTemp = temperatureConfig.getValue();
			if (dbTemp.equalsIgnoreCase(TemperatureType.F.getName())) {
				temperatureunit = TemperatureType.F.getName();
			} else if (dbTemp.equalsIgnoreCase(TemperatureType.C.getName())) {
				temperatureunit = TemperatureType.C.getName();
			}
		}
						
		Long originalProfileFromId = null;
        
        String OriginalProfileFromName = fixture.getOriginalProfileFrom();
        
        if(OriginalProfileFromName.indexOf("_"+ServerConstants.DEFAULT_PROFILE) != -1 && !OriginalProfileFromName.equals(ServerConstants.DEFAULT_PROFILE))
        {
        	originalProfileFromId = groupManager.getGroupByName(OriginalProfileFromName.split("_")[0]).getId();
        }else{
        	originalProfileFromId = groupManager.getGroupByName(OriginalProfileFromName).getId();
        }
        
        
        Long currentProfileId = fixture.getGroupId();
        
        List<String> groupList = new ArrayList<String>();
        List<GemsGroupFixture> listGrFix = gemsGroupManager.getAllGroupsOfFixture(fixture);
        if(listGrFix != null)
        {
	        for (GemsGroupFixture grpFix : listGrFix) {
	        	if(motionGroupManager.getMotionGroupByGemsGroupId(grpFix.getGroup().getId()) != null)
	        	{
	        		String name = grpFix.getGroup().getGroupName() + "[M]";
	        		groupList.add(name);
	        	}
	        	else if(switchManager.getSwitchByGemsGroupId(grpFix.getGroup().getId()) != null)
	        	{
	        		String name = grpFix.getGroup().getGroupName() + "[S]";
	        		groupList.add(name);
	        	}
	        }
	    }
        
        FixtureLampCalibration flc = fixtureCalibrationManager.getFixtureCalibrationMapByFixtureId(fixtureId);
        List<BallastVoltPower> voltPowerList = fixtureManager.getBallastVoltPowerCurve(fixture.getBallast().getId().longValue(),fixture.getVoltage());
        String characterizationStatus = "";
        String fixtureStatus = "";
        if(flc!=null){
        	characterizationStatus = FixtureLampStatusVO.INDIVIDUAL;
        }else if(voltPowerList!=null && voltPowerList.size() >0){
        	characterizationStatus = FixtureLampStatusVO.GENERIC_FROM_BALLAST;
        }else{
        	characterizationStatus  = FixtureLampStatusVO.UNCHARACTERISED;
        }
        
        FixtureLampStatusVO fixtLampStatusVo= fixtureManager.getOutageTypeByFixtureId(fixtureId);
        if(fixtLampStatusVo.getFixtureStatus()!=null && fixtLampStatusVo.getFixtureStatus().equalsIgnoreCase(FixtureLampStatusVO.FIXTURE_OUT)){
        	fixtureStatus = FixtureLampStatusVO.FIXTURE_OUT;
        }else if(fixtLampStatusVo.getFixtureStatus()!=null && fixtLampStatusVo.getFixtureStatus().equalsIgnoreCase(FixtureLampStatusVO.LAMP_OUT)){
        	fixtureStatus = FixtureLampStatusVO.LAMP_OUT;
        }else{
        	fixtureStatus = FixtureLampStatusVO.OPERATIONAL;
        }
        
        
        fixtureDetails.setFixture(fixture);
        fixtureDetails.setTemperatureunit(temperatureunit);
        fixtureDetails.setFixtureclasses(fixtureclasses);
        fixtureDetails.setOriginalProfileFromId(originalProfileFromId);
        fixtureDetails.setCurrentProfileId(currentProfileId);
        fixtureDetails.setGroupList(groupList);
        fixtureDetails.setCharacterizationStatus(characterizationStatus);
        fixtureDetails.setFixtureStatus(fixtureStatus);
        
		return fixtureDetails;
	}
	
	/**
	 * Returns Fixture List
	 * 
	 * @param fixtures
	 *            List of fixture's
	 * @return fixture List 
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("fixturesListByFixtureIds")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixturesListByFixtureIds(List<Fixture> fixtures) {
		List<Long> fixArrayList = new ArrayList<Long>();
		Iterator<Fixture> itr = fixtures.iterator();
		while (itr.hasNext()) {
			Fixture fixture = (Fixture) itr.next();
			fixArrayList.add(fixture.getId());
		}
		return fixtureManager.getFixturesListByFixtureIds(fixArrayList);
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
	@Path("reboot/{fixtureId}")
	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response rebootFixture(@PathParam("fixtureId") Long fixtureId) {   	 	
  
		Response res = new Response();
		fixtureManager.rebootFixture(fixtureId);
		return res;
  
	} //end of method rebootFixture
	
 }

