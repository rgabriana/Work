package com.emscloud.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmInstanceDao;
import com.emscloud.dao.FacilityDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.api.DetailedProfile;
import com.emscloud.vo.Fixture;

@Service("facilityManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityManager {
	
	@Resource
	private FacilityDao	facilityDao;
	@Resource
	private EmInstanceDao emInstanceDao;
		
	public Facility loadFacilityTreeByCustomer(long custId) {
		
		return facilityDao.loadFacilityTreeByCustomer(custId) ;
		
	} //end of method loadFacilityTreeByCustomer

	public Facility loadFacilityById(long id) {
		
		return facilityDao.loadFacilityById(id) ;
		
	} //end of method loadSiteById
			
	public void saveOrUpdate(Facility facility) {		
		
		facilityDao.saveOrUpdate(facility) ;	
	
	} //end of method saveOrUpdate

	public List<DetailedProfile> getProfilesByFloorId(Long id) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(id);
		List<DetailedProfile> list = new ArrayList<DetailedProfile>();
		if (fems != null) {
			for(FacilityEmMapping fem: fems) {
				EmInstance emInstance = emInstanceDao.loadEmInstanceById(fem.getEmId());
				list.addAll(facilityDao.getProfilesByEmFloorId(emInstance.getDatabaseName(), emInstance.getReplicaServer().getInternalIp(), fem.getEmFacilityId()));
			}
		}
		return list;
		
	}
	
	public long getCustomerLevelId(Long emId, Long levelId, int levelType) {
		
		return facilityDao.getCustomerLevelId(emId, levelId, levelType);
		
	} //end of method getCustomerLevelId
	
	public Long getOrganizationIdOfCustomer(long custId) {
		
		return facilityDao.getOrganizationIdOfCustomer(custId);
		
	} //end of method getOrganizationIdOfCustomer
	
	public byte[] getFloorPlan(Long id) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(id);
		byte[] planMap = null;
		if(fems != null) {
			for(FacilityEmMapping fem: fems) {
				EmInstance emInstance = emInstanceDao.loadEmInstanceById(fem.getEmId());
				planMap = facilityDao.getFloorPlan(emInstance.getDatabaseName(), emInstance.getReplicaServer().getInternalIp(), fem.getEmFacilityId());
				if (planMap != null && planMap.length > 0) {
					break;
				}
			}
		}
		return planMap;
		
	} //end of method getFloorPlan
	
	public List<Fixture> getFixtures(Long floorId) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(floorId);
		if(fems == null) {
			return null;
		}
		ArrayList<Fixture> fixtureList = new ArrayList<Fixture>();
		for(FacilityEmMapping fem:fems) {
			EmInstance emInst = emInstanceDao.loadEmInstanceById(fem.getEmId());
			System.out.println("database name -- " + emInst.getDatabaseName());
			fixtureList.addAll(facilityDao.getFixtures(emInst.getDatabaseName(), 
					emInst.getReplicaServer().getInternalIp(), fem.getEmFacilityId()));
		}
		return fixtureList;
		
	} //end of method getFixtures
		
} //end of class FacilityManager
