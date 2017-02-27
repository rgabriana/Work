package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GemsGroupDao;
import com.ems.dao.MotionGroupDao;
import com.ems.model.GemsGroup;
import com.ems.model.MotionGroup;

@Service("motionGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionGroupManager {
	
	@Resource
	private MotionGroupDao motionGroupDao;

    @Resource
    private GemsGroupDao gemsGroupDao;
	
	
	public String getNextGroupNo() {
		return motionGroupDao.getNextGroupNo();
	}
	
	public MotionGroup getMotionGroupById(Long motionGroupId) {
		return (MotionGroup) motionGroupDao.getObject(MotionGroup.class, motionGroupId);
	}
	
	public MotionGroup getMotionGroupByGemsGroupId(Long gemsGroupId) {
        return motionGroupDao.getMotionGroupByGemsGroupId(gemsGroupId);
    }
	
	public MotionGroup saveOrUpdateMotionGroup(MotionGroup motionGroup) {
		return (MotionGroup) motionGroupDao.saveObject(motionGroup);
	}
	
    public void deleteMotionGroup(Long id) {
        MotionGroup motionGroup = getMotionGroupById(id);
        gemsGroupDao.deleteGemsGroup(motionGroup.getGemsGroup().getId());
        motionGroupDao.removeObject(MotionGroup.class, id);
    }

    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByCompany(companyId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByCampus(Long campusId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByCampus(campusId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByBuilding(Long buildingId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByBuilding(buildingId);
        return groups;
    }

    public List<GemsGroup> loadGroupsByFloor(Long floorId) {
        List<GemsGroup> groups = motionGroupDao.loadGroupsByFloor(floorId);
        return groups;
    }

    public List<GemsGroup> loadObsoleteGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = motionGroupDao.loadObsoleteGroupsByCompany(companyId);
        return groups;
    }

}
