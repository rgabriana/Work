package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ProfileTemplateDao;
import com.ems.model.ProfileTemplate;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Service("profileTemplateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileTemplateManager {


    @Resource
    private GroupManager groupManager;

    @Resource
    private ProfileTemplateDao profileTemplateDao;

    /**
     * save ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    @CacheEvict(value="group_id" , allEntries = true)
    public ProfileTemplate save(ProfileTemplate profileTemplate) {
        return (ProfileTemplate) profileTemplateDao.saveObject(profileTemplate);
    }

    /**
     * update ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    @CacheEvict(value="group_id" , allEntries = true)
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
    @CacheEvict(value="group_id" , allEntries = true)
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
    
    public int getProfileTemplateCountByName(String name)
    {
    	int count=0;
    	try{
    		count = profileTemplateDao.getProfileTemplateCountByName(name);
    		return count;
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return 0;
    }

    public ProfileTemplate getProfileTemplateById(Long id) {
        return profileTemplateDao.getProfileTemplateById(id);
    }

    public Long getGroupByProfileAndTenantDetails(byte profileno, long tenantid) {
    	return groupManager.getGroupByProfileAndTenantDetails(profileno, tenantid);
    }
    
    public ProfileTemplate getGroupByName(String profileTemplateName) {
        return profileTemplateDao.getProfileTemplateByName(profileTemplateName);
    }

    @CacheEvict(value="group_id" , allEntries = true)
    public ProfileTemplate editName(ProfileTemplate profileTemplate) {
        return profileTemplateDao.editName(profileTemplate);
    }
    public List<ProfileTemplate>loadAllDerivedProfileTemplate()
    {
        return profileTemplateDao.loadAllDerivedProfileTemplate();
    }
    
    @CacheEvict(value="group_id" , allEntries = true)
    public void updateTemplateVisibility(Long templateId, boolean visibility) {
    	profileTemplateDao.updateTemplateVisibility(templateId, visibility);
    }
    
    public List<Object> getFixtureCountForProfileTemplate(Long profileId) {
    	return profileTemplateDao.getFixtureCountForProfileTemplate(profileId);
    }
}
