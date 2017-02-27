package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmProfileMappingDao;
import com.emscloud.model.EmProfileMapping;

@Service("emProfileMappingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmProfileMappingManager {

	static final Logger logger = Logger
			.getLogger(EmProfileMappingManager.class.getName());
	
	@Resource
	private EmProfileMappingDao emProfileMappingDao;

	public EmProfileMapping getEmProfileMapping(long id) {

		return emProfileMappingDao.getEmProfileMapping(id);
	}

	public List<EmProfileMapping> getEmProfileMappingListOnUemProfileId(long uemProfileId) {
		return emProfileMappingDao.getEmProfileMappingListOnUemProfileId(uemProfileId);
	}
	
	 public EmProfileMapping getEmProfileMappingByUemProfileIdAndEMId(Long emInstanceId, Long profileId){
		 return emProfileMappingDao.getEmProfileMappingByUemProfileIdAndEMId(profileId,emInstanceId);
	 }

	public List<EmProfileMapping> getEmProfileMappingByEmId(long emId) {
		return emProfileMappingDao.getEmProfileMappingByEmId(emId);
	}
	
    public EmProfileMapping saveOrUpdate(EmProfileMapping emProfileMapping) {
        return emProfileMappingDao.saveOrUpdate(emProfileMapping);
    }
    
    public EmProfileMapping getEmTemplateMappingByEmProfileNoAndEMId(Short emProfileNo, Long emInstanceId) {
        return emProfileMappingDao.getEmTemplateMappingByEmProfileNoAndEMId(emProfileNo,emInstanceId);
    }
    public EmProfileMapping getEmTemplateMappingByEmGroupIdAndEMId(Long emGroupId, Long emInstanceId) {
        return emProfileMappingDao.getEmTemplateMappingByEmGroupIdAndEMId(emGroupId,emInstanceId);
    }
    public List<EmProfileMapping> getListOfDirtyProfilesInUEM() {
        return emProfileMappingDao.getListOfDirtyProfilesInUEM();
    }
    public EmProfileMapping getEMProfileMappingByUEMProfileId(Long uemProfileId) {
        return emProfileMappingDao.getEMProfileMappingByUEMProfileId(uemProfileId);
    }
}
