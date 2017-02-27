package com.emscloud.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.enlightedUrls.EmFixtureUrls;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.types.FacilityType;
import com.emscloud.communication.vos.Fixture;
import com.emscloud.vo.FixtureDetails;
import com.emscloud.vo.FixtureList;
import com.sun.jersey.api.client.GenericType;

@Service("fixtureManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureManager {

	@Resource
	GlemManager glemManager;
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	@Resource
	CommunicationUtils communicationUtils;
	/*
	@Resource
	EmProfileMappingManager emProfileMappingManager;
	
	@Resource
	ProfileGroupManager profileGroupManager;
	*/
	@Resource
	FacilityDao facilityDao;
	@Resource
    EmInstanceManager emInstanceManager;
	static final Logger logger = Logger.getLogger(FixtureManager.class
			.getName());

	public List<Fixture> getFixtureList(String property, Long pid) {
		List<Fixture> result = null;
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(floor);
				Long emFloorid = facilityEmMappingManager
						.getFacilityEmMappingOnFacilityId(pid)
						.getEmFacilityId();
				List<ResponseWrapper<List<Fixture>>> response = glemManager.getAdapter()
						.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getListOfFixture
								+ property + "/" + emFloorid.toString() + "/",
								MediaType.APPLICATION_XML,
								new GenericType<List<Fixture>>() {
								});
				if (response.get(0).getStatus() == Response.Status.OK
						.getStatusCode()) {
					
					result = response.get(0).getItems();
					/*
					EmProfileMapping emProfileMapping = null;
					Long uemProfileid = null;
					for(Fixture fixture : result){
						if(fixture.getGroupId() > 16){
							emProfileMapping = emProfileMappingManager.getEmTemplateMappingByEmGroupIdAndEMId(fixture.getGroupId(), emList.get(0).getId());
							if(emProfileMapping != null){
								uemProfileid = emProfileMapping.getUemProfileId();
								fixture.setCurrentProfile(profileGroupManager.getDisplayProfileName(uemProfileid));
							}
						}
					}*/
					
				} else {
					logger.error("Not able to get Fixtures from EM:- "
							+ response.get(0).getEm().getMacId()
							+ " reason :- " + response.get(0).getStatus());
				}
			} else {
				logger.error("Floor Id passed is not corespond to type "
						+ FacilityType.FLOOR.getName() + " but to "
						+ floor.getType().toString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return result;
	}
	
	public FixtureList getEMFixtures(EmInstance emInstances, Long floorId,String data,String property)
    {
       FixtureList result = null;
       ResponseWrapper<FixtureList> response = glemManager.getAdapter()
               .executePost(emInstances,  glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getAlternateListOfFixture
                       + property + "/" + floorId.toString(), MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, FixtureList.class, data);
       
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            result = response.getItems();
        } else {
            logger.error("Not able to get Fixtures from EM:- "
                    + response.getEm().getMacId()
                    + " reason :- " + response.getStatus());
        }
        return result;
    }
	public FixtureList loadFixtureListWithSpecificAttrs(String data,String property, Long pid) {
	    FixtureList result = null;
        int DEFAULT_ROWS =100;
        Long EmFloorId=(long) -1;
        int page = 0;
        long total = 0, records = 0;
        int limit=20;
        try {
            Facility facility = getFacility(pid);
            if (FacilityType.getFacilityType(FacilityType.FLOOR) == facility.getType()) {
                
                List<EmInstance> emList = communicationUtils.getEmMap(facility);
                FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(pid);
                if(facilityEMmapping!=null)
                {
                    EmFloorId = facilityEMmapping.getEmFacilityId();
                    result = getEMFixtures(emList.get(0),EmFloorId, data, property);
                }
                
            } else {
                ArrayList<Facility> floorFacilitys = (ArrayList<Facility>) communicationUtils.getFloor(facility);
                limit = DEFAULT_ROWS/floorFacilitys.size();
                Iterator<Facility> facilityItr = floorFacilitys.iterator();
                result = new FixtureList();
                ArrayList<Fixture> oriFxList = new ArrayList<Fixture>();
                while (facilityItr.hasNext()) {
                    Facility facilityObj = facilityItr.next();
                    FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(facilityObj.getId());
                    if(facilityEMmapping!=null)
                    {
                        EmFloorId = facilityEMmapping.getEmFacilityId();
                        EmInstance emInstance = emInstanceManager.getEmInstance(facilityEMmapping.getEmId());
                        FixtureList tempFixtureList = getEMFixtures(emInstance,EmFloorId, data, "floor");
                        if(tempFixtureList!=null && tempFixtureList.getFixture()!=null && tempFixtureList.getFixture().size()>0)
                        {
                           oriFxList.addAll(tempFixtureList.getFixture());
                           records+=tempFixtureList.getRecords();
                        }
                    }
                }
                result.setFixture(oriFxList);
                result.setTotal(records);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }
	
	public com.emscloud.util.Response dimFixtures(String mode,String percentage,String time, Long pid , List<Fixture> fixtures) {
		com.emscloud.util.Response result = null;
		Facility floor = getFacility(pid);
		if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
			List<EmInstance> emList = communicationUtils.getEmMap(floor);
			List<ResponseWrapper<com.emscloud.util.Response>> response = glemManager.getAdapter()
					.executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.dimFixture
							+mode+"/"+percentage+"/"+time+"/",MediaType.APPLICATION_XML,MediaType.APPLICATION_XML,com.emscloud.util.Response.class,communicationUtils.convertModelListToString(fixtures,Fixture.class));
			if (response.get(0).getStatus() == Response.Status.OK
					.getStatusCode()) {
				result = response.get(0).getItems();
			} else {
				logger.error("Dimming Fixtures command failed from EM:- "
						+ response.get(0).getEm().getMacId()
						+ " reason :- " + response.get(0).getStatus());
			}
		} else {
			logger.error("Floor Id passed is not corespond to type "
					+ FacilityType.FLOOR.getName() + " but to "
					+ floor.getType().toString());
		}
		return result;
	}
	
	public com.emscloud.util.Response applyModeToFixtures(String modetype, Long pid , List<Fixture> fixtures) {
		com.emscloud.util.Response result = null;
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(floor);
				List<ResponseWrapper<com.emscloud.util.Response>> response = glemManager.getAdapter()
						.executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.applyModeToFixture
								+ modetype + "/", MediaType.APPLICATION_XML,
								MediaType.APPLICATION_XML,
								com.emscloud.util.Response.class,
								communicationUtils.convertModelListToString(
										fixtures, Fixture.class));
				if (response.get(0).getStatus() == Response.Status.OK
						.getStatusCode()) {
					result = response.get(0).getItems();
				} else {
					logger.error("Setting Mode of Fixtures to " + modetype
							+ " command failed from EM:- "
							+ response.get(0).getEm().getMacId()
							+ " reason :- " + response.get(0).getStatus());
				}
			} else {
				logger.error("Floor Id passed is not corespond to type "
						+ FacilityType.FLOOR.getName() + " but to "
						+ floor.getType().toString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return result;
	}
	
	public Fixture getFixtureDetails(Long fid,Long pid) {
		Fixture result = null;
		Facility floor = getFacility(pid);
		if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
			List<EmInstance> emList = communicationUtils.getEmMap(floor);
			List<ResponseWrapper<Fixture>> response = glemManager.getAdapter()
					.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getFixtureDetails
							+fid, MediaType.APPLICATION_XML,Fixture.class);
			if (response.get(0).getStatus() == Response.Status.OK
					.getStatusCode()) {
				result = response.get(0).getItems();
			} else {
				logger.error("Not able to get Fixture Details from EM:- "
						+ response.get(0).getEm().getMacId()
						+ " reason :- " + response.get(0).getStatus());
			}
		} else {
			logger.error("Floor Id passed is not corespond to type "
					+ FacilityType.FLOOR.getName() + " but to "
					+ floor.getType().toString());
		}
		return result;
	}
	
	public FixtureDetails getFixtureObjectDetails(Long fixtureId,Long pid) {
		FixtureDetails result = null;
		Facility floor = getFacility(pid);
		if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
			List<EmInstance> emList = communicationUtils.getEmMap(floor);
			List<ResponseWrapper<FixtureDetails>> response = glemManager.getAdapter()
					.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getFixtureObjectDetails
							+fixtureId, MediaType.APPLICATION_XML,FixtureDetails.class);
			if (response.get(0).getStatus() == Response.Status.OK
					.getStatusCode()) {
				result = response.get(0).getItems();
			} else {
				logger.error("Not able to get Fixture Object Details from EM:- "
						+ response.get(0).getEm().getMacId()
						+ " reason :- " + response.get(0).getStatus());
			}
		} else {
			logger.error("Floor Id passed is not corespond to type "
					+ FacilityType.FLOOR.getName() + " but to "
					+ floor.getType().toString());
		}
		return result;
	}
	
	public com.emscloud.util.Response getFixtureRealTimeStats(Long pid , List<Fixture> fixtures) {
		com.emscloud.util.Response result = null;
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(floor);
				List<ResponseWrapper<com.emscloud.util.Response>> response = glemManager.getAdapter()
						.executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getFixtureRealTimeStats, MediaType.APPLICATION_XML,
								MediaType.APPLICATION_XML,
								com.emscloud.util.Response.class,
								communicationUtils.convertModelListToString(fixtures, Fixture.class));
				if (response.get(0).getStatus() == Response.Status.OK
						.getStatusCode()) {
					result = response.get(0).getItems();
				} else {
					logger.error("Getting Realtime Stats of Fixtures command failed from EM:- "
							+ response.get(0).getEm().getMacId()
							+ " reason :- " + response.get(0).getStatus());
				}
			} else {
				logger.error("Floor Id passed is not corespond to type "
						+ FacilityType.FLOOR.getName() + " but to "
						+ floor.getType().toString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return result;
	}

	public Facility getFacility(long id) {
		return facilityDao.getFacility(id);
	}
	
}
