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
import com.emscloud.vo.Device;
import com.emscloud.vo.Fixture;
import com.emscloud.vo.Gateway;

@Service("facilityManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityManager {
	
	@Resource
	private FacilityDao	facilityDao;
	@Resource
	private EmInstanceDao emInstanceDao;
		
	//private static final Logger logger = Logger.getLogger(FacilityManager.class.getName());
	
	public Facility loadFacilityTreeByCustomer(long custId) {
		
		Long facilityId = getOrganizationIdOfCustomer(custId);
		return facilityDao.loadFacilityById(facilityId) ;
		
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
	
	public byte[] getFloorPlan(String dbName, String replicaIp, Long emFacilityId) {
		
		byte[] planMap = facilityDao.getFloorPlan(dbName, replicaIp, emFacilityId);		
		return planMap;
		
	} //end of method getFloorPlan
	
	public void setFloorPlan(Long floorId, byte[] imageData) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(floorId);		
		if(fems != null) {
			for(FacilityEmMapping fem: fems) {
				EmInstance emInstance = emInstanceDao.loadEmInstanceById(fem.getEmId());
				facilityDao.savePlanMap(emInstance.getDatabaseName(), emInstance.getReplicaServer().getInternalIp(), imageData, 
						fem.getEmFacilityId());
				return;
			}
		}
		
	} //end of method setFloorPlan
	
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
	
	public List<Fixture> getFixtures(String dbName, String replicaIp, Long emFacilityId) {
				
		List<Fixture> fixtureList = facilityDao.getFixtures(dbName, replicaIp, emFacilityId);
		return fixtureList;
		
	} //end of method getFixtures
	
	public List<Gateway> getGateways(String dbName, String replicaIp, Long emFacilityId) {
				
		List<Gateway> gwList = facilityDao.getGateways(dbName, replicaIp, emFacilityId);
		return gwList;
		
	} //end of method getGateways
	
	public List<Gateway> getGateways(Long floorId) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(floorId);
		if(fems == null) {
			return null;
		}
		ArrayList<Gateway> gwList = new ArrayList<Gateway>();
		for(FacilityEmMapping fem:fems) {
			EmInstance emInst = emInstanceDao.loadEmInstanceById(fem.getEmId());
			System.out.println("database name -- " + emInst.getDatabaseName());
			gwList.addAll(facilityDao.getGateways(emInst.getDatabaseName(), 
					emInst.getReplicaServer().getInternalIp(), fem.getEmFacilityId()));
		}
		return gwList;
		
	} //end of method getGateways
	
	public List<Device> getDevices(String dbName, String replicaIp, Long emFacilityId) {
		
		List<Device> deviceList = facilityDao.getDevices(dbName, replicaIp, emFacilityId);
		return deviceList;
		
	} //end of method getDevices
	
	public List<Device> getDevices(Long floorId) {
		
		List<FacilityEmMapping> fems = facilityDao.getEmMappingsByFacilityId(floorId);
		if(fems == null) {
			return null;
		}
		ArrayList<Device> deviceList = new ArrayList<Device>();
		for(FacilityEmMapping fem:fems) {
			EmInstance emInst = emInstanceDao.loadEmInstanceById(fem.getEmId());
			System.out.println("database name -- " + emInst.getDatabaseName());
			deviceList.addAll(facilityDao.getDevices(emInst.getDatabaseName(), 
					emInst.getReplicaServer().getInternalIp(), fem.getEmFacilityId()));
		}
		return deviceList;
		
	} //end of method getDevices
	
	public Facility getFacility(long id) {
		return facilityDao.loadFacilityById(id);
	}
	
	public Facility getFacility(String name) {
		return facilityDao.loadFacilityByName(name);
	}
	
	public List<Facility> getChildFacilitiesByFacilityId(long facilityId){
		return facilityDao.getChildFacilitiesByFacilityId(facilityId);
	}
	
	public List<Facility> getSiblingFacilitiesByFacilityId(long facilityId){
		return facilityDao.getSiblingFacilitiesByFacilityId(facilityId);
	}
	
	public Facility addFacility(Facility facility) {
        return (Facility) facilityDao.saveObject(facility);
    }
	
	public void editFacility(Facility facility) {
		facilityDao.editFacility(facility);
	}
	
	public void deleteFacility(Long id) {
		facilityDao.removeObject(Facility.class, id);
	}
	
	
} //end of class FacilityManager
