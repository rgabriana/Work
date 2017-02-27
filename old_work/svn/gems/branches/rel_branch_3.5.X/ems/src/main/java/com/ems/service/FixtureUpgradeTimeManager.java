package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureUpgradeTimeDao;
import com.ems.model.FixtureUpgradeTime;

@Service("fixtureUpgradeTimeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureUpgradeTimeManager {

    @Resource
    private FixtureUpgradeTimeDao fixtureUpgradeTimeDao;

    public FixtureUpgradeTime getFixtureUpgradeTimeById(Long id) {
        return fixtureUpgradeTimeDao.getFixtureUpgradeTimeById(id);
    }

}
