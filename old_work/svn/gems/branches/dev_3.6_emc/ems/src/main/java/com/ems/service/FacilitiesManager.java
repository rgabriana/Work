package com.ems.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Floor;
import com.ems.model.PlanMap;
import com.ems.ws.util.Response;

@Service("facilitiesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilitiesManager {
	
	@Resource(name = "companyManager")
    private CompanyManager companyManager;
    @Resource(name = "buildingManager")
    private BuildingManager buildingManager;
    @Resource(name = "floorManager")
    private FloorManager floorManager;
    @Resource(name = "campusManager")
    private CampusManager campusManager;
    @Resource
    private PlanMapManager planMapManager;
    
    private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	public Response setFloorPlan(String companyName, String campusName,String bldgName, String floorName, String imageURL,byte[] imageData) throws UnsupportedEncodingException {
		Response resp = new Response();
    	
    	companyName = URLDecoder.decode(companyName, "UTF-8");
    	campusName = URLDecoder.decode(campusName, "UTF-8");
    	bldgName = URLDecoder.decode(bldgName, "UTF-8");
    	floorName = URLDecoder.decode(floorName, "UTF-8");
    	
    	try {
       		if(!companyManager.getCompany().getName().equals(companyName))
       		{
	        	resp.setMsg("error");
	        	resp.setStatus(600);
	   			return resp;   			
       		}
       		Campus campus = campusManager.getCampusByName(campusName);
	        if(campus == null)
	        {
	        	resp.setMsg("error");
	        	resp.setStatus(601);
	   			return resp;   			
	        }
	        long campusId = campus.getId();
	        Building building = buildingManager.getBuildingByNameAndCampusId(bldgName, campusId);
	        if(building == null)
	        {
	        	resp.setMsg("error");
	        	resp.setStatus(602);
	   			return resp;   			
	        }
	        long buildingId = building.getId();
	   		Floor floor = floorManager.getFloorByNameAndBuildingId(floorName, buildingId);
	   		if(floor == null) {
	        	resp.setMsg("error");
	        	resp.setStatus(603);
	   			return resp;   			
	   		}
 			PlanMap planMap = new PlanMap();
			planMap.setPlan(imageData);
			planMapManager.save(planMap);
			floor.setPlanMap(planMap);
			floor.setFloorPlanUrl(imageURL);
			
			floorManager.update(floor);
			resp.setStatus(200);		// Done for EmConfig , if image upload runs successfully , must keep this statement at end.
            return resp;
        } catch (IndexOutOfBoundsException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
            m_Logger.error(e1.getMessage());
        } catch (NumberFormatException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	m_Logger.error(e1.getMessage());
        } catch (SQLException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	m_Logger.error(e1.getMessage());
        } catch (IOException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	m_Logger.error(e1.getMessage());
        } catch(Exception e1) {		// Added for any other exceptions as well.
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	m_Logger.error(e1.getMessage());
        }
        return resp;
	}
}
