package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SubAreaDao;
import com.ems.model.SubArea;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("subAreaManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SubAreaManager {

    @Resource
    private SubAreaDao subAreaDao;

    /**
     * save SubArea details.
     * 
     * @param subArea
     *            com.ems.model.SubArea
     */
    public SubArea save(SubArea subArea) {
        return (SubArea) subAreaDao.saveObject(subArea);
    }

    /**
     * update SubArea details.
     * 
     * @param subArea
     *            com.ems.model.SubArea
     */
    public SubArea update(SubArea subArea) {
        return (SubArea) subAreaDao.saveObject(subArea);
    }

    /**
     * Load SubArea
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.SubArea collection
     */
    public List<SubArea> loadSubAreaByAreaId(Long id) {
        return subAreaDao.loadSubAreaByAreaId(id);
    }

    /**
     * Delete SubArea details
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteSubArea(Long id) {
        subAreaDao.removeObject(SubArea.class, id);
    }

    /**
     * load sub area details if id is given.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.SubArea
     */
    public SubArea getSubAreaById(Long id) {
        return (SubArea) subAreaDao.getObject(SubArea.class, id);
    }

    /**
     * load subarea details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.SubArea
     */
    public SubArea loadSubArea(Long id) {
        return subAreaDao.loadSubArea(id);
    }
}
