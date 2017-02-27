package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureImageVersionDao;
import com.ems.model.FixtureImageVersion;

@Service("fixtureImageVersionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureImageVersionManager {

    @Resource
    private FixtureImageVersionDao fixtureImageVersionDao;

    public FixtureImageVersion save(FixtureImageVersion fixtureImageVersion) {
        return (FixtureImageVersion) fixtureImageVersionDao.saveObject(fixtureImageVersion);
    }

    public List<FixtureImageVersion> getAllActiveScheduleUpgrades() {
        return fixtureImageVersionDao.getAllActiveScheduleUpgrades();
    }

    public Long getCurrentImageVersion() {
        return fixtureImageVersionDao.getCurrentImageVersion();
    }

}
