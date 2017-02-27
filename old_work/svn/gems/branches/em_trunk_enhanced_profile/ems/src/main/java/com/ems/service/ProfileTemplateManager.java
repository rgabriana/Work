package com.ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GroupDao;
import com.ems.dao.ProfileDao;
import com.ems.dao.ProfileTemplateDao;
import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Service("profileTemplateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileTemplateManager {


    @Resource
    private GroupDao groupDao;

    @Resource
    private ProfileTemplateDao profileTemplateDao;

    /**
     * save ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    public ProfileTemplate save(ProfileTemplate profileTemplate) {
        return (ProfileTemplate) profileTemplateDao.saveObject(profileTemplate);
    }

    /**
     * update ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    public ProfileTemplate update(ProfileTemplate profileTemplate) {
        return (ProfileTemplate) profileTemplateDao.saveObject(profileTemplate);
    }

    /**
     * Load company's ProfileTemplate
     * 
     * @param id
     *            company id
     * @return com.ems.model.ProfileTemplate collection
     */
    public List<ProfileTemplate> loadTemplateByCompanyId(Long id) {
        return profileTemplateDao.loadTemplateByCompanyId(id);
    }

    /**
     * Load all ProfileTemplate
     * 
     * @return com.ems.model.ProfileTemplate collection
     */
    public List<ProfileTemplate> loadAllProfileTemplate() {
        return profileTemplateDao.loadAllProfileTemplate();
    }
    
    /**
     * Delete Groups details
     * 
     * @param id
     *            database id(primary key)
     * @return 
     */
    public int delete(Long id) {
    	int status=1;
		try
		{
			profileTemplateDao.removeObject(ProfileTemplate.class, id);
		} catch (Exception e) {
			status=0;
			e.printStackTrace();
		}
		return status;
    	
    }

    public ProfileTemplate getProfileTemplateById(Long id) {
        return profileTemplateDao.getProfileTemplateById(id);
    }

    public Long getGroupByProfileAndTenantDetails(byte profileno, long tenantid) {
    	return groupDao.getGroupByProfileAndTenantDetails(profileno, tenantid);
    }
    
    public ProfileTemplate getGroupByName(String profileTemplateName) {
        return profileTemplateDao.getProfileTemplateByName(profileTemplateName);
    }

    public ProfileTemplate editName(ProfileTemplate profileTemplate) {
        return profileTemplateDao.editName(profileTemplate);
    }
}
