/**
 * 
 */
package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.enlightedUrls.EmFixtureUrls;
import com.emscloud.communication.vos.Wds;
import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.types.FacilityType;
import com.sun.jersey.api.client.GenericType;

/**
 * @author sheetal
 * 
 */
@Service("wdsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsManager {
    static final Logger logger = Logger.getLogger("SwitchLogger");

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
	
	public List<Wds> getWdsList(String property, Long pid) {
		List<Wds> result = null;
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
				List<EmInstance> emList = communicationUtils.getEmMap(floor);
				Long emFloorid = facilityEmMappingManager
						.getFacilityEmMappingOnFacilityId(pid)
						.getEmFacilityId();
				List<ResponseWrapper<List<Wds>>> response = glemManager.getAdapter()
						.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getListOfWds
								+ property + "/" + emFloorid.toString() + "/",
								MediaType.APPLICATION_XML,
								new GenericType<List<Wds>>() {
								});
				if (response.get(0).getStatus() == Response.Status.OK
						.getStatusCode()) {
					
					result = response.get(0).getItems();					
					
				} else {
					logger.error("Not able to get Wds from EM:- "
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
   
	public Wds getWdsDetails(Long wdsId,Long pid) {
		Wds result = null;
		Facility floor = getFacility(pid);
		if (FacilityType.getFacilityType(FacilityType.FLOOR) == floor.getType()) {
			List<EmInstance> emList = communicationUtils.getEmMap(floor);
			List<ResponseWrapper<Wds>> response = glemManager.getAdapter()
					.executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getWdsDetails
							+wdsId, MediaType.APPLICATION_XML,Wds.class);
			if (response.get(0).getStatus() == Response.Status.OK
					.getStatusCode()) {
				result = response.get(0).getItems();
			} else {
				logger.error("Not able to get Wds Details from EM:- "
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
	
	public Facility getFacility(long id) {
		return facilityDao.getFacility(id);
	}

}
