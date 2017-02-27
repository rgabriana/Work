package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmProfileTemplateMappingDao;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.EmTemplateMapping;

@Service("emProfileTemplateMappingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmProfileTemplateMappingManager {

	static final Logger logger = Logger.getLogger(FacilityEmMappingManager.class.getName());
	@Resource
	private EmProfileTemplateMappingDao emProfileTemplateMappingDao;

	public EmTemplateMapping getEmTemplateMappingByEmId(long id) {
        return emProfileTemplateMappingDao.getEmTemplateMappingByEmId(id);
    }
	public EmTemplateMapping saveOrUpdate(EmTemplateMapping emTemplateMapping) {
        return emProfileTemplateMappingDao.saveOrUpdate(emTemplateMapping);
    }
    public EmTemplateMapping getEmTemplateMappingByEmTemplateIdAndEMId(Long emTemplateId,Long emInstanceId)  {
        return emProfileTemplateMappingDao.getEmTemplateMappingByEmTemplateIdAndEMId(emTemplateId,emInstanceId);
    }
    public List<EmTemplateMapping> getProfileTemplateMappingListOnUemProfileTemplateId(long uemTemplateId) {
		return emProfileTemplateMappingDao.getProfileTemplateMappingListOnUemProfileTemplateId(uemTemplateId);
	}
    public EmTemplateMapping getEmTemplateMappingByUEMTemplateIdAndEMId(Long uemTemplateId,Long emInstanceId)  {
        return emProfileTemplateMappingDao.getEmTemplateMappingByUEMTemplateIdAndEMId(uemTemplateId,emInstanceId);
    }
}
