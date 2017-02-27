package com.emscloud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmFacilityDao;
import com.emscloud.model.EmFacility;
import com.emscloud.types.FacilityType;




@Service("emFacilityManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmFacilityManager {
	
	static final Logger logger = Logger.getLogger(EmFacilityManager.class.getName());
	
	@Resource
	EmFacilityDao emFacilityDao;
	
	public EmFacility saveOrUpdate(EmFacility emFacility) {
		return (EmFacility)emFacilityDao.saveObject(emFacility);
	}
	
	public Map<FacilityType, Map<Long, EmFacility>> getEmFacilityByEmId(Long emId) {
		Map<FacilityType, Map<Long, EmFacility>> map = new HashMap<FacilityType, Map<Long,EmFacility>>();
		List<EmFacility> emFacilities = emFacilityDao.getEMFacilitiesByEmId(emId);
		
		map.put(FacilityType.ORGANIZATION, new HashMap<Long, EmFacility>());
		map.put(FacilityType.CAMPUS, new HashMap<Long, EmFacility>());
		map.put(FacilityType.BUILDING, new HashMap<Long, EmFacility>());
		map.put(FacilityType.FLOOR, new HashMap<Long, EmFacility>());
		
		if(emFacilities != null && emFacilities.size() > 0) {
			for(EmFacility emFacility : emFacilities) {
				logger.debug("CurrentEmFacility:::" + emFacility.getEmId() + "::::" + emFacility.getType() + "::::" + emFacility.getEmFacilityId() +"::::" + emFacility.getEmFacilityName());
				switch (emFacility.getType()) {
					case ORGANIZATION: {
						map.get(FacilityType.ORGANIZATION).put(emFacility.getEmFacilityId(), emFacility);
						break;
					}
					case CAMPUS: {
						map.get(FacilityType.CAMPUS).put(emFacility.getEmFacilityId(), emFacility);
						break;
					}
					case BUILDING: {
						map.get(FacilityType.BUILDING).put(emFacility.getEmFacilityId(), emFacility);
						break;
					}
					case FLOOR: {
						map.get(FacilityType.FLOOR).put(emFacility.getEmFacilityId(), emFacility);
						break;
					}
					default: {
						break;
					}
				}
			}
		}
		return map;
	}
	
	public List<EmFacility> getEMFacilitiesByFacilityType(FacilityType type, Long emId){
		return emFacilityDao.getEMFacilitiesByFacilityType(type, emId);
	}

	public void deleteEmFacilityByEmId(Long id) {
		emFacilityDao.deleteEmFacilityByEmId(id);
	}

}
