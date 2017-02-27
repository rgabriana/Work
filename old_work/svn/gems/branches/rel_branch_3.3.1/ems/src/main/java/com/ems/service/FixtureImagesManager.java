package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FixtureImagesDao;
import com.ems.model.FixtureImages;

@Service("fixtureImagesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureImagesManager {

    @Resource
    private FixtureImagesDao fixtureImagesDao;

    public FixtureImages save(FixtureImages fixtureImages) {
        return (FixtureImages) fixtureImagesDao.saveObject(fixtureImages);
    }

    public FixtureImages getFixtureImageById(Long id) {
        return fixtureImagesDao.getFixtureImageById(id);
    }

    public Boolean getFixtureImageById(String fileName) {
        return fixtureImagesDao.getFixtureImageById(fileName);
    }

    public FixtureImages getFixtureImageByName(String name) {
        return fixtureImagesDao.getFixtureImageByName(name);
    }
}
