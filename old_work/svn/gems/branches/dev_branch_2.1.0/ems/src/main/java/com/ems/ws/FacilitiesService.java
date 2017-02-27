/**
 * RESTful webservices exposed by GEMS 
 */
package com.ems.ws;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.InventoryDevice;
import com.ems.model.Switch;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.InventoryDeviceService;
import com.ems.service.PlanMapManager;
import com.ems.service.SwitchManager;
import com.ems.service.UserLocationsManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org")
// @RolesAllowed("admin")
public class FacilitiesService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "companyManager")
    private CompanyManager companyManager;
    @Resource(name = "buildingManager")
    private BuildingManager buildingManager;
    @Resource(name = "floorManager")
    private FloorManager floorManager;
    @Resource(name = "planMapManager")
    private PlanMapManager planMapManager;
    @Resource(name = "campusManager")
    private CampusManager campusManager;
    @Resource(name = "areaManager")
    private AreaManager areaManager;
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "switchManager") 
    private SwitchManager switchManager;
    @Resource
    FacilityTreeManager facilityTreeManager;
    
    @Resource(name = "inventoryDeviceService")
    private InventoryDeviceService inventoryDeviceService;
    @Resource(name = "userLocationsManager")
    private UserLocationsManager userLocationsManager; 

    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public FacilitiesService() {
    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    /**
     * Return the first company
     * 
     * @return company
     */
    @Path("company")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Company getCompany() {
        return companyManager.loadCompanyById(1L);
    }

    /**
     * Returns list of all companies
     * 
     * @return company list
     */
    @Path("company/list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Company> getCompanyList() {
        return companyManager.getAllCompanies();
    }

    /**
     * Return campus list
     * 
     * @param companyid
     * @return campus list for the companyid
     */
    @Path("campus/list/{companyid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Campus> getCampusList(@PathParam("companyid") Long companyid) {
        return companyManager.loadCompanyById(companyid).getCampuses();
    }

    /**
     * Returns Building list
     * 
     * @param campusid
     * @return building list for the campusid
     */
    @Path("building/list/{campusid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Building> getBuildingList(@PathParam("campusid") Long campusid) {
        return buildingManager.getAllBuildingsByCampusId(campusid);
    }

    /**
     * @param buildingid
     * @return floor list for the building id.
     */
    @Path("floor/list/{buildingid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Floor> getFloorList(@PathParam("buildingid") Long buildingid) {
        try {
            return floorManager.getAllFloorsByBuildingId(buildingid);
        } catch (SQLException e) {
            m_Logger.error(e.getMessage());
        } catch (IOException e) {
            m_Logger.error(e.getMessage());
        }
        return null;
    }
    
    /**
     * @return floor list for the building id.
     */
    @Path("floor/list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<com.ems.vo.model.Floor> getFloorList() {
      
      ArrayList<com.ems.vo.model.Floor> floorList = new ArrayList<com.ems.vo.model.Floor>();
      try {
	List<Object[]> results = floorManager.getAllFloorsOfCompany();
	if(results == null) {
	  return floorList;
	}
	Iterator<Object[]> resultsIter = results.iterator();
	com.ems.vo.model.Floor floor = null;
	while(resultsIter.hasNext()) {
	  Object[] data = resultsIter.next();
	  floor = new com.ems.vo.model.Floor();
	  floor.setId((BigInteger)data[0]);
	  floor.setName(data[1].toString());
	  floor.setBuildingId((BigInteger)data[2]);
	  floor.setCampusId((BigInteger)data[3]);
	  floor.setCompanyId((BigInteger)data[4]);
	  floor.setDescription(data[5].toString());
	  floor.setFloorPlanUrl(data[6].toString());
	  floorList.add(floor);
	}
      } catch (SQLException e) {
	m_Logger.error(e.getMessage());
      } catch (IOException e) {
	m_Logger.error(e.getMessage());
      }
      return floorList;
      
    } //end of method getFloorList

    /**
     * @param floorid
     * @return area list for the floor id
     */
    @Path("area/list/{floorid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Area> getAreaList(@PathParam("floorid") Long floorid) {
        return areaManager.getAllAreasByFloorId(floorid);
    }

    /**
     * Returns the floor plan image for the selected floor
     * 
     * @param fid
     * @return embedded image in the byte array
     */
    @Path("floor/{fid:.*}")
    @GET
    @Produces("image/jpeg")
    public byte[] getFloorPlan(@PathParam("fid") String fid) {

        if (fid == null || fid.isEmpty())
            fid = "1";
        try {
            Floor oFloor = floorManager.getFloorById(Long.parseLong(fid));
            return oFloor.getByteImage();
        } catch (IndexOutOfBoundsException e1) {
            m_Logger.error(e1.getMessage());
        } catch (NumberFormatException e1) {
            m_Logger.error(e1.getMessage());
        } catch (SQLException e1) {
            m_Logger.error(e1.getMessage());
        } catch (IOException e1) {
            m_Logger.error(e1.getMessage());
        }
        return null;
    }

    /**
     * Returns the floor plan image for the selected area's floor
     * 
     * @param aid
     * @return embedded image in the byte array
     */
    @Path("area/{aid:.*}")
    @GET
    @Produces("image/jpeg")
    public byte[] getFloorPlanFromArea(@PathParam("aid") String aid) {

        if (aid == null || aid.isEmpty())
            aid = "1";
        try {
            Area oArea = areaManager.getAreaById(Long.parseLong(aid));
            if (oArea != null) {
                Floor oFloor = floorManager.getFloorById(oArea.getFloor().getId());
                return oFloor.getByteImage();
            }
        } catch (IndexOutOfBoundsException e1) {
            m_Logger.error(e1.getMessage());
        } catch (NumberFormatException e1) {
            m_Logger.error(e1.getMessage());
        } catch (SQLException e1) {
            m_Logger.error(e1.getMessage());
        } catch (IOException e1) {
            m_Logger.error(e1.getMessage());
        }
        return null;
    }

    /**
     * Assign fixtures to Selected Area
     * 
     * @param aid
     *            Id of the area
     * @param fixtures
     *            list "<fixtures><fixture><id>1</id></fixture></fixtures>
     * @return Response status
     */
    @Path("area/{aid}/assignfixtures")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response assignFixtures(@PathParam("aid") Long aid, List<Fixture> fixtures) {
        m_Logger.debug("Fixtures: " + fixtures.size());
        Response oResponse = new Response();
        Area oArea = areaManager.getAreaUsingId(aid);
        if (oArea != null) {
            Iterator<Fixture> itr = fixtures.iterator();
            while (itr.hasNext()) {
                Fixture fixture = (Fixture) itr.next();
                // Get the fixture from the database
                Fixture oFixture = fixtureManager.getFixtureById(fixture.getId());
                if (oFixture != null) {
                    if (oArea.getFloor().getId().longValue() == oFixture.getFloor().getId().longValue()) {
                        oFixture.setArea(oArea);
                        fixtureManager.updateFixture(oFixture);
                    }
                }
            }
            oResponse.setStatus(1);
        }
        return oResponse;
    }
    
    @Path("area/delete/{aid}")
    @POST
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String deleteArea(@PathParam("aid") Long aid) {
    	List<Fixture> list = (List<Fixture>)fixtureManager.loadFixtureByAreaId(aid);
    	boolean assignedToUser = userLocationsManager.isAreaAssignedToUser(aid);
    	if((list == null || list.size() == 0) && !assignedToUser) {
    		areaManager.deleteArea(aid);
    		userAuditLoggerUtil.log("Delete area: " + aid, UserAuditActionType.Area_Update.getName());
    		return "S";
    	}
    	else {
    		return "F";
    	}
    	
    }
    
    @Path("floor/delete/{fid}")
    @POST
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String deleteFloor(@PathParam("fid") Long fid) {
    	//TODO get only counts instead of object lists
    	List<Fixture> list1 = (List<Fixture>)fixtureManager.loadAllFixtureByFloorId(fid);
    	List<Area> list2 = null;
		list2 = (List<Area>)areaManager.getAllAreasByFloorId(fid);
		List<Gateway> list3 = null;
		list3 = gatewayManager.loadFloorGateways(fid);
		List<Switch> list4 = null;
		list4 = switchManager.loadSwitchByFloorId(fid);
		List<InventoryDevice> list5 = null;
		list5 = inventoryDeviceService.loadInventoryDeviceByFloorId(fid);
		boolean assignedToUser = userLocationsManager.isFloorAssignedToUser(fid);

    	if((list1 == null || list1.size() == 0) 
    			&& (list2 == null || list2.size() == 0)
    			&& (list3 == null || list3.size() == 0)
    			&& (list4 == null || list4.size() == 0)
    			&& (list5 == null || list5.size() == 0)
    			&& !assignedToUser) {
    		floorManager.deleteFloor(fid);
    		userAuditLoggerUtil.log("Delete floor: " + fid, UserAuditActionType.Floor_Update.getName());
    		return "S";
    	}
    	else {
    		return "F";
    	}
    	
    }
    
    
    @Path("building/delete/{bid}")
    @POST
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String deleteBuilding(@PathParam("bid") Long bid) {
    	List<Floor> list = null;
		try {
			list = (List<Floor>)floorManager.getAllFloorsByBuildingId(bid);
		} catch (SQLException e) {
			e.printStackTrace();
			return "I";
		} catch (IOException e) {
			e.printStackTrace();
			return "I";
		}
    	if(list == null || list.size() == 0) {
    		userAuditLoggerUtil.log("Delete building: " + bid, UserAuditActionType.Building_Update.getName());
    		buildingManager.deleteBuilding(bid);
    		return "S";
    	}
    	else {
    		return "F";
    	}
    	
    }
    
    @Path("campus/delete/{cid}")
    @POST
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String deleteCampus(@PathParam("cid") Long cid) {
    	List<Building> list = null;
		list = (List<Building>)buildingManager.getAllBuildingsByCampusId(cid);
		
    	if(list == null || list.size() == 0) {
    		campusManager.deleteCampus(cid);
    		userAuditLoggerUtil.log("Delete campus: " + cid, UserAuditActionType.Campus_Update.getName());
    		return "S";
    	}
    	else {
    		return "F";
    	}
    	
    }
    
    //Service to get the bread crumb for given node
    
    @Path("facilities/nodepath/{nodeType}/{nodeId}")
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public String getNodePath(@PathParam("nodeType") String nodeType, @PathParam("nodeId") Long nodeID) {
        String path=facilityTreeManager.getNodePath(nodeType, nodeID);        
        return path;
    }
    
    
//Service to get the parent node for any child in the tree
    
    @Path("facilities/{nodeType}/{nodeId}")
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public String getNodeparent(@PathParam("nodeType") String nodeType, @PathParam("nodeId") Long nodeID) {
       		if(nodeType.equalsIgnoreCase("area"))
       		{
       			try {
					Area area = areaManager.getAreaById(nodeID) ;
					  return area.getFloor().getId().toString() ;
				} catch (SQLException e) {
					e.printStackTrace();
					return null ; 
				} catch (IOException e) {
					e.printStackTrace();
					 return null ; 
				}
       			
       			
       		}
        return null ; 
    }
    
    @Path("getCurrentTime")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
		return sdf.format(new java.util.Date());
    }
    
}
