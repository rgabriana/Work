package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.annotation.InvalidateFacilityTreeCache;
import com.emscloud.dao.FacilityEmMappingDao;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FacilityEmMappingList;
import com.emscloud.ws.Response;



@Service("facilityEmMappingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityEmMappingManager {

	static final Logger logger = Logger
			.getLogger(FacilityEmMappingManager.class.getName());
	@Resource
	private FacilityEmMappingDao facilityEmMappingDao;
	

	public FacilityEmMapping getFacilityEmMapping(long id) {

		return facilityEmMappingDao.getFacilityEmMapping(id);
	}

	public FacilityEmMapping getFacilityEmMappingOnFacilityId(long id) {
		return facilityEmMappingDao.getFacilityEmMappingOnFacilityId(id);
	}
	
	 public FacilityEmMapping getFacilityEmMappingOnEmFloorId(Long emInstanceId, Long floorId){
		 return facilityEmMappingDao.getFacilityEmMappingOnEmFloorId(emInstanceId, floorId);
	 }

	public List<FacilityEmMapping> getFacilityEmMappingOnEmId(long emId) {
		return facilityEmMappingDao.getFacilityEmMappingOnEmId(emId);
	}
	
	
	public List<FacilityEmMapping> getAllMappedFaciltyList() {
		return facilityEmMappingDao.getAllMappedFaciltyList();
	}
	
	public FacilityEmMappingList loadFacilityEmMappingListByCustomerId(String orderby,String orderway, 
			Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit,Long customerId){
		return facilityEmMappingDao.loadFacilityEmMappingListByCustomerId(orderby, orderway, bSearch, searchField, searchString, searchOper, offset, limit,customerId);
	}
	
	public List<Long> getDistictMappedEMIdList()
	{
	    return facilityEmMappingDao.getDistictMappedEMIdList();
	}
	
	@InvalidateFacilityTreeCache
	public void deleteFacilityEmMapping(long id) {
	   facilityEmMappingDao.deleteFacilityEmMapping(id);
	}
	
	@InvalidateFacilityTreeCache
	public Response saveEmMapping(Long emInstId,Long emFacilityId,String emFacilityPath,Long cloudFacilityId,Long customerId) {
		Response resp = new Response();
		facilityEmMappingDao.saveEmMapping(emInstId, emFacilityId, emFacilityPath,cloudFacilityId,customerId);
		return resp;
	}
	
}
