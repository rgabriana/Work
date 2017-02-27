package com.ems.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.PlanMap;
import com.ems.vo.model.BuildingInfo;
import com.ems.vo.model.CampusInfo;
import com.ems.vo.model.FloorInfo;
import com.ems.vo.model.OrganizationInfo;
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
    
    private static final Logger logger = Logger.getLogger("WSLogger");
	
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
            logger.error(e1.getMessage());
        } catch (NumberFormatException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	logger.error(e1.getMessage());
        } catch (SQLException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	logger.error(e1.getMessage());
        } catch (IOException e1) {
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	logger.error(e1.getMessage());
        } catch(Exception e1) {		// Added for any other exceptions as well.
        	resp.setStatus(400);
        	resp.setMsg(e1.getMessage());
        	logger.error(e1.getMessage());
        }
        return resp;
	}
	
	public Response updateOrgFacility(OrganizationInfo orgInfo) {
		
 		Response resp = new Response();
 		int status=200;
 		String message="success";  
 		//for the first time when uuid is not present name has to match
 		Company company = companyManager.getCompany();
 		if(company.getUid() == null) {
 			//name has to match as uuid is not set
 			if(!company.getName().equals(orgInfo.getName().trim())) {
 				status = 600;
 				message = "Wrong Org Name in orgFacility.xml file " + orgInfo.getName();
 				resp.setMsg(message);
 				resp.setStatus(status);
 				logger.error(message);
 				return resp;   		
 			} else {
 				//name is same, set the uuid
 				company.setUid(orgInfo.getUid());
 			}
 		} else {
 			//update the name if not same
 			if(!company.getName().equals(orgInfo.getName().trim())) {
 				company.setName(orgInfo.getName().trim());
 			}
 		}
 		//process the campuses
 		List<CampusInfo> campusList = orgInfo.getCampusInfoList();
 		Iterator<CampusInfo> campusIter = campusList.iterator();
 		while(campusIter.hasNext()) {
 			CampusInfo campus = campusIter.next();
 			if(logger.isDebugEnabled()) {
 				logger.debug("loogking for campus -- " + campus.getName());
 			}
 			boolean campusExists = false;
 			Campus dbCampus = campusManager.getCampusByUid(campus.getUid());
 			if(dbCampus == null) {
 				//there is no campus with this uid
 				dbCampus = campusManager.getCampusByName(campus.getName().trim());
 				if(dbCampus == null) {
 					//no campus with name also so create it
 					dbCampus = new Campus();
 					dbCampus.setUid(campus.getUid());
 					dbCampus.setName(campus.getName().trim());
 					dbCampus.setLocation(campus.getLocation().trim());
 					dbCampus.setZipcode(campus.getZipCode());
 					dbCampus.setVisible(false);
 					campusManager.addCampus(dbCampus);
 					dbCampus = campusManager.save(dbCampus);
 					if(logger.isDebugEnabled()) {
 						logger.debug("id of new campus -- " + dbCampus.getId());
 					}
 				} else {
 					//update the uid
 					campusExists = true;
 					dbCampus.setUid(campus.getUid());
 					dbCampus = campusManager.update(dbCampus);
 				}
 			} else {
 				//campus exists with this uid, update the name
 				campusExists = true;
 				if(!dbCampus.getName().equals(campus.getName().trim())) {
 					dbCampus.setName(campus.getName().trim());
 					dbCampus = campusManager.update(dbCampus);
 				}
 			}
 			
 			//process the buildings
 			List<BuildingInfo> buildingList = campus.getBuildingInfoList();
 			Iterator<BuildingInfo> buildingIter = buildingList.iterator();
 			while(buildingIter.hasNext()) {
 				BuildingInfo building = buildingIter.next();
 				if(logger.isDebugEnabled()) {
 					logger.debug("looking for building -- " + building.getName());
 				}
 				boolean buildingExists = false;
 				Building dbBuilding = buildingManager.getBuildingByUid(building.getUid());
 				if(dbBuilding == null) {
 					//there is no building with this uid
 					if(campusExists) {
 						dbBuilding = buildingManager.getBuildingByNameAndCampusId(building.getName().trim(), dbCampus.getId());
 						if(dbBuilding == null) {
 							//no building exists with name inside the campus so create it
 							dbBuilding = new Building();
 							dbBuilding.setUid(building.getUid());
 							dbBuilding.setName(building.getName().trim());
 							dbBuilding.setLatitude(building.getLatitude());
 							dbBuilding.setLongitude(building.getLongitude());
 							dbBuilding.setCampus(dbCampus);
 							dbBuilding.setVisible(false);
 							try {
 								dbBuilding = buildingManager.save(dbBuilding);
 							}
 							catch(Exception e) {
 								message = "Could not create building - " + building.getName().trim();
 								logger.error(message, e);
 							}
 						} else {
 							buildingExists = true;
 							//update uid
 							dbBuilding.setUid(building.getUid());
 							dbBuilding = buildingManager.update(dbBuilding);
 						}
 					} else {
 						//there is no building, no campus, create one
 						dbBuilding = new Building();
 						dbBuilding.setUid(building.getUid());
 						dbBuilding.setName(building.getName().trim());
 						dbBuilding.setLatitude(building.getLatitude());
 						dbBuilding.setLongitude(building.getLongitude());
 						dbBuilding.setCampus(dbCampus);
 						dbBuilding.setVisible(false);
 						try {
 							dbBuilding = buildingManager.save(dbBuilding);
 						}
 						catch(Exception e) {
 							message = "Could not create building - " + building.getName().trim();
 							logger.error(message, e);
 						}
 					}
 				} else {
 					buildingExists = true;
 					//building exists with uid, update name
 					if(!dbBuilding.getName().equals(building.getName().trim())) {
 						dbBuilding.setName(building.getName().trim());
 						dbBuilding = buildingManager.update(dbBuilding);
 					}
 				}
 				//process floors
 				if(logger.isDebugEnabled()) {
 					logger.debug("processing for building -- " + building.getName().trim() + " in db as " + dbBuilding.getId());
 				}
 				List<FloorInfo> floorList = building.getFloorInfoList();
 				Iterator<FloorInfo> floorIter = floorList.iterator();
 				while(floorIter.hasNext()) {
 					FloorInfo floor = floorIter.next();
 					Floor dbFloor = floorManager.getFloorByUid(floor.getUid());
 					if(logger.isDebugEnabled()) {
 						logger.debug("looking for floor " + floor.getName());
 					}
 					if(dbFloor == null) {
 						//there is no floor with this uid, 
 						if(buildingExists) {
 							try {
 								dbFloor = floorManager.getFloorByNameAndBuildingId(floor.getName().trim(), dbBuilding.getId());
 							}
 							catch(Exception e) {
 								message = "problem in retrieving the floor " + dbFloor.getName().trim();
 								logger.error(message, e);
 							}
 							if(dbFloor == null) {
 								//there is no floor inside this building, create one
 								dbFloor = new Floor();
 								dbFloor.setUid(floor.getUid());
 								dbFloor.setName(floor.getName().trim());
 								dbFloor.setBuilding(dbBuilding);
 								dbFloor.setDescription("Floor created from EmConfig file");
 								dbFloor.setVisible(false);
 								try {
 									dbFloor = floorManager.save(dbFloor);
 								}
 								catch(Exception e) {
 									message = "Could not create the floor " + floor.getName().trim();
 									logger.error(message, e);
 								}
 							} else {
 								//floor exists, update the uid
 								dbFloor.setUid(floor.getUid());
 								try {
 									dbFloor = floorManager.update(dbFloor);
 								}
 								catch(Exception e) {
 									message = "Could not update the floor " + floor.getName().trim();
 									logger.error(message, e);
 								}
 							}
 						} else {
 							//building does not exists, so create floor
 							dbFloor = new Floor();
 							dbFloor.setUid(floor.getUid());
 							dbFloor.setName(floor.getName().trim());
 							if(logger.isDebugEnabled()) {
 								logger.debug("adding floor to building -- " + dbBuilding.getId());
 							}
 							dbFloor.setBuilding(dbBuilding);
 							dbFloor.setDescription("Floor created from EmcConfig file");
 							dbFloor.setVisible(false);
 							try {
 								dbFloor = floorManager.save(dbFloor);
 							}
 							catch(Exception e) {
 								message = "Could not create the floor " + floor.getName().trim();
 								logger.error(message, e);
 							}
 						}
 					} else {
 						//floor exists with uid, update name
 						if(logger.isDebugEnabled()) {
 							logger.debug("floor exists with uid - " + dbFloor.getId());
 						}
 						if(!dbFloor.getName().equals(floor.getName().trim())) {
 							dbFloor.setName(floor.getName());
 							try {
 								dbFloor = floorManager.update(dbFloor);
 							}
 							catch(Exception e) {
 								message = "Could not update floor name " + floor.getName().trim();
 								logger.error(message, e);
 							}
 						}
 					}	
 				}
 			}
 		}
 		if(logger.isDebugEnabled()) {
 			logger.debug("1 done with facility import");
 		}
 		return resp;
 		
 	}
	
}
