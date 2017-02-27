package com.emcloudinstance.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.OccupancyDao;
import com.emcloudinstance.vo.OccSyncVo;

@Service("occupancyManager")
@Transactional(propagation = Propagation.REQUIRED)
public class OccupancyManager {
    static final Logger logger = Logger.getLogger(OccupancyManager.class.getName());
    @Resource
    private OccupancyDao occupancyDao;

    public List<OccSyncVo> load30MinOccupancyData(Date oFDate, String mac, String emTimeZone, List<Long> emFacIds) {
        return occupancyDao.load30MinOccupancyData(oFDate, mac, emTimeZone, emFacIds);
    }

    public Date loadLatestOccDataDate(String mac, String emTimeZone) {
        return occupancyDao.loadLatestDataDate(mac, emTimeZone);
    }

    public Date loadFirstOccDataDate(String mac, String emTimeZone) {
        return occupancyDao.loadFirstDataDate(mac, emTimeZone);
    }

}
