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
import com.emscloud.communication.vos.Gateway;
import com.sun.jersey.api.client.GenericType;

@Service("gatewayManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GatewayManager {

	@Resource
	GlemManager glemManager;
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	@Resource
	CommunicationUtils communicationUtils;
	@Resource
    EmInstanceManager emInstanceManager;
	@Resource
	FacilityDao facilityDao;

	static final Logger logger = Logger.getLogger(GatewayManager.class.getName());

	public List<Gateway> getEMGateways(EmInstance emInstances, Long floorId)
    {
	    List<Gateway> result = null;
	    ResponseWrapper<List<Gateway>> response = glemManager.getAdapter()
                .executeGet(emInstances, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getListOfGateways
                        +"floor/"+floorId, "application/xml",new GenericType<List<Gateway>>(){});
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            result = response.getItems();
        } else {
            logger.error("Not able to get Gateways from EM:- "
                    + response.getEm().getMacId()
                    + " reason :- " + response.getStatus());
        }
        return result;
    }
	
	public List<Gateway> getGatewayList(String property, Long pid) {
		List<Gateway> result = new ArrayList<Gateway>();
		try {
			Facility facility = getFacility(pid);
			List<EmInstance> emList = communicationUtils.getEmMap(facility);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == facility.getType()) {
				FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(pid);
	            if(facilityEMmapping!=null)
	            {
	                Long EmFloorId = facilityEMmapping.getEmFacilityId();
	                result = getEMGateways(emList.get(0),EmFloorId);
	            }
			}
			else
	        {
			    Long EmFloorId=(long) -1;
	            ArrayList<Facility> floorFacilitys = (ArrayList<Facility>) communicationUtils.getFloor(facility);
	            Iterator<Facility> facilityItr = floorFacilitys.iterator();
	            while (facilityItr.hasNext()) {
	                Facility facilityObj = facilityItr.next();
	                FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(facilityObj.getId());
	                if(facilityEMmapping!=null)
	                {
	                    EmFloorId = facilityEMmapping.getEmFacilityId();
	                    EmInstance emInstance = null;
	                    emInstance = emInstanceManager.getEmInstance(facilityEMmapping.getEmId());
	                    if(emInstance != null){
	                    	List<Gateway> tempGatewayList = getEMGateways(emInstance,EmFloorId);
		                    if(tempGatewayList!=null && tempGatewayList.size()>0)
		                    {
		                       result.addAll(tempGatewayList);
		                       // records+=tempEventsAndFaults.getRecords();
		                       //page=tempEventsAndFaults.getPage();
		                    }	                    	
	                    }
	                    
	                }
	            }
	        }
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return result;
	}
	
	public Gateway getGatewayDetails(Long pid,Long gid) {
		Gateway result = null;
		Facility floor = getFacility(pid);
		if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
			List<EmInstance> emList = communicationUtils.getEmMap(floor);
			List<ResponseWrapper<Gateway>> response = glemManager.getAdapter()
					.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getGatewayDetails
							+gid, MediaType.APPLICATION_XML,Gateway.class);
			if (response.get(0).getStatus() == Response.Status.OK
					.getStatusCode()) {
				result = response.get(0).getItems();
			} else {
				logger.error("Not able to get Gateway Detailsfrom EM:- "
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
	
	public com.emscloud.util.Response getGatewayRealTimeStats(Long pid , List<Gateway> gateways) {
		com.emscloud.util.Response result = null;
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(floor);
				List<ResponseWrapper<com.emscloud.util.Response>> response = glemManager.getAdapter()
						.executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getGatewayRealTimeStats, MediaType.APPLICATION_XML,
								MediaType.APPLICATION_XML,com.emscloud.util.Response.class,
								communicationUtils.convertModelListToString(gateways, Gateway.class));
				if (response.get(0).getStatus() == Response.Status.OK
						.getStatusCode()) {
					result = response.get(0).getItems();
				} else {
					logger.error("Getting Realtime Stats of Gateways command failed from EM:- "
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
