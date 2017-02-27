package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureGroupDao;
import com.ems.model.Fixture;
import com.ems.model.FixtureGroup;
import com.ems.service.FixtureGroupManager;


/**
 * 
 * @author pankaj kumar chauhan
 *
 */
@Service("fixtureGroupManager")
@Transactional(propagation=Propagation.REQUIRED)
public class FixtureGroupManager{

	@Resource
	private FixtureGroupDao fixtureGroupDao;


	/**
	 * save fixtureGroup details.
	 * @param fixtureGroup  com.ems.model.FixtureGroup
	 */
	public FixtureGroup save(FixtureGroup fixtureGroup)
	{
		if(fixtureGroup.getId()==0)
		{
			fixtureGroup.setId(null);
		}
		return (FixtureGroup)fixtureGroupDao.saveObject(fixtureGroup);
	}

	/**
	 * update fixtureGroup details.
	 * @param fixtureGroup  com.ems.model.FixtureGroup
	 */
	public FixtureGroup update(FixtureGroup fixtureGroup){
		return (FixtureGroup)fixtureGroupDao.saveObject(fixtureGroup);
	}
	
	/**
	 * load all fixture of group
	 * @param id  Group id
	 * @return com.ems.dao.Fixture collection
	 */
	public List<Fixture> loadFixtureByGroupId(Long id){
		return fixtureGroupDao.loadFixtureByGroupId(id);
	}
	
	/**
	 * Delete FixtureGroup details
	 * @param id  database id(primary key)
	 */
	public void delete(Long id){
		fixtureGroupDao.removeObject(FixtureGroup.class, id);
	}

	/**
	 * Loads all fixtureGroups within an area
	 * @param id
	 * @return com.ems.model.FixtureGroup collection
	 */
	public List<FixtureGroup> loadFixtureGroupByAreaId(Long id) {
		return fixtureGroupDao.loadFixtureGroupByAreaId(id);
	}

	/**
	 * Loads all fixtureGroups within a floor
	 * @param id
	 * @return com.ems.model.FixtureGroup collection
	 */
	public List<FixtureGroup> loadFixtureGroupByFloorId(Long id) {
		return fixtureGroupDao.loadFixtureGroupByFloorId(id);
	}

	/**
	 * Loads all fixtureGroups within a group
	 * @param id
	 * @return com.ems.model.FixtureGroup collection
	 */
	public List<FixtureGroup> loadFixtureGroupByGroupId(Long id) {
		return fixtureGroupDao.loadFixtureGroupByGroupId(id);
	}

	/**
	 * Loads all fixtureGroups within a sub-area
	 * @param id
	 * @return com.ems.model.FixtureGroup collection
	 */
	public List<FixtureGroup> loadFixtureGroupBySubareaId(Long id) {
		return fixtureGroupDao.loadFixtureGroupBySubareaId(id);
	}
}
