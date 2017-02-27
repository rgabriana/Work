package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.PlugloadProfileTemplateDao;
import com.ems.model.PlugloadProfileTemplate;

@Service("plugloadProfileTemplateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadProfileTemplateManager {
	
	@Resource
	PlugloadProfileTemplateDao  plugloadProfileTemplateDao;
	
	public PlugloadProfileTemplate getPlugloadProfileTemplateById(Long id){
		return plugloadProfileTemplateDao.getPlugloadProfileTemplateById(id);
	}
	
	/**
     * save PlugloadProfileTemplate details.
     * 
     * @param PlugloadProfileTemplate
     *            com.ems.model.PlugloadProfileTemplate
     */
    public PlugloadProfileTemplate save(PlugloadProfileTemplate plugloadProfileTemplate) {
        return (PlugloadProfileTemplate) plugloadProfileTemplateDao.saveObject(plugloadProfileTemplate);
    }

    /**
     * update PlugloadProfileTemplate details.
     * 
     * @param PlugloadProfileTemplate
     *            com.ems.model.PlugloadProfileTemplate
     */
    public PlugloadProfileTemplate update(PlugloadProfileTemplate plugloadProfileTemplate) {
        return (PlugloadProfileTemplate) plugloadProfileTemplateDao.saveObject(plugloadProfileTemplate);
    }

    /**
     * Load company's PlugloadProfileTemplate
     * 
     * @param id
     *            company id
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    public List<PlugloadProfileTemplate> loadPlugloadTemplateByCompanyId(Long id) {
        return plugloadProfileTemplateDao.loadPlugloadTemplateByCompanyId(id);
    }

    /**
     * Load All Derived PlugloadProfileTemplate (Template no > 1)
     * 
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    public List<PlugloadProfileTemplate> loadAllDerivedPlugloadProfileTemplate() {
    	return plugloadProfileTemplateDao.loadAllDerivedPlugloadProfileTemplate();
    }
    
    /**
     * Load all PlugloadProfileTemplate
     * 
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    public List<PlugloadProfileTemplate> loadAllPlugloadProfileTemplate() {
        return plugloadProfileTemplateDao.loadAllPlugloadProfileTemplate();
    }
    
    /**
     * Delete PlugloadProfileTemplate details
     * 
     * @param id
     *            database id(primary key)
     * @return 
     */
    public int delete(Long id) {
    	int status=1;
		try
		{
			plugloadProfileTemplateDao.removeObject(PlugloadProfileTemplate.class, id);
		} catch (Exception e) {
			status=0;
			e.printStackTrace();
		}
		return status;
    	
    }
    
    public int getPlugloadProfileTemplateCountByName(String name)
    {
    	int count=0;
    	try{
    		count = plugloadProfileTemplateDao.getPlugloadProfileTemplateCountByName(name);
    		return count;
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return 0;
    }

    public PlugloadProfileTemplate getPlugloadProfileTemplateByName(String plugloadProfileTemplate) {
        return plugloadProfileTemplateDao.getPlugloadProfileTemplateByName(plugloadProfileTemplate);
    }

    public PlugloadProfileTemplate editName(PlugloadProfileTemplate plugloadProfileTemplate) {
        return plugloadProfileTemplateDao.editName(plugloadProfileTemplate);
    }
    
}
