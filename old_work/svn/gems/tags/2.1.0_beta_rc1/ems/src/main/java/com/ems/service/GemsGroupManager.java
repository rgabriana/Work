package com.ems.service;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GemsGroupDao;
import com.ems.model.Fixture;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupType;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.GGroupType;

/**
 * @author Shilpa Chalasani
 * 
 */
@Service("gemsGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsGroupManager {
    static final Logger logger = Logger.getLogger("GemsGroupLogger");

    @Resource
    private GemsGroupDao gemsGroupDao;

    public List<GemsGroupType> getGroupTypeList() {
        return gemsGroupDao.getGroupTypeList();
    }

    public void saveGemsGroupType(GemsGroupType gemsGroupType) {
        gemsGroupDao.saveGemsGroupType(gemsGroupType);
    }

    public List<GemsGroup> loadGroupsByCompany(Long companyId) {
        List<GemsGroup> groups = gemsGroupDao.loadGroupsByCompany(companyId);
        return groups;
    }

    public GemsGroup loadGroupsByGroupName(String groupName) {
        return gemsGroupDao.loadGroupsByGroupName(groupName);
    }

    public GemsGroup loadGemsGroup(Long id) {
        return gemsGroupDao.loadGemsGroup(id);
    }

    public GemsGroup createNewGroup(GemsGroup gemsGroup) {
        return gemsGroupDao.createNewGroup(gemsGroup);
    }

    public void deleteGroup(GemsGroup gemsGroup) {
        gemsGroupDao.deleteGroup(gemsGroup);
    }

    public void saveGemsGroupFixtures(Long gid, List<Fixture> fixtures) {
        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
        int groupNo = group.getType().getGroupNo();
        // Currently only motion group is supported
        int type = GGroupType.MotionGroup.getId();

        Iterator<Fixture> itr = fixtures.iterator();
        int[] fixtureIdList = new int[fixtures.size()];
        int count = 0;
        while (itr.hasNext()) {
            Fixture fixture = (Fixture) itr.next();

            GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(gid, fixture.getId());
            if (groupFixture == null) {
                fixtureIdList[count] = fixture.getId().intValue();
                groupFixture = new GemsGroupFixture();
                groupFixture.setFixture(fixture);
                groupFixture.setGroup(group);
                gemsGroupDao.saveGemsGroupFixtures(groupFixture);
                count++;
            }
        }
        DeviceServiceImpl.getInstance().sendSUGroupCommand(fixtureIdList, ServerConstants.SU_CMD_JOIN_GRP,
                (byte) type, groupNo);
    }

    public void deleteGemsGroupFixtures(Long gid, List<Fixture> fixtures) {
        GemsGroup group = gemsGroupDao.loadGemsGroup(gid);
        int groupNo = group.getType().getGroupNo();

        Iterator<Fixture> itr = fixtures.iterator();
        int[] fixtureIdList = new int[fixtures.size()];
        int count = 0;
        while (itr.hasNext()) {
            Fixture fixture = (Fixture) itr.next();
            GemsGroupFixture groupFixture = gemsGroupDao.getGemsGroupFixture(gid, fixture.getId());
            if (groupFixture != null) {
                fixtureIdList[count] = fixture.getId().intValue();
                gemsGroupDao.deleteGemsGroupFixtures(groupFixture);
                count++;
            }
        }
        // Currently only motion group is supported
        int type = GGroupType.MotionGroup.getId();

        DeviceServiceImpl.getInstance().sendSUGroupCommand(fixtureIdList, ServerConstants.SU_CMD_LEAVE_GRP,
                (byte) type, groupNo);
    }

    public void resetAllGroupOnFixtures(List<Fixture> fixtures) {
        Iterator<Fixture> itr = fixtures.iterator();
        int[] fixtureIdList = new int[fixtures.size()];
        int count = 0;
        while (itr.hasNext()) {
            Fixture fixture = (Fixture) itr.next();
            fixtureIdList[count] = fixture.getId().intValue();
            count++;
            List<GemsGroupFixture> groupFixtureList = gemsGroupDao.getGemsGroupFixtureByFixture(fixture.getId());
            if (groupFixtureList != null) {
                Iterator<GemsGroupFixture> oItr = groupFixtureList.iterator();
                while(oItr.hasNext()) {
                    gemsGroupDao.deleteGemsGroupFixtures(oItr.next());
                }
            }
        }
        // Currently only motion group is supported
        int type = GGroupType.MotionGroup.getId();
        DeviceServiceImpl.getInstance().sendSUGroupCommand(fixtureIdList, ServerConstants.SU_CMD_REQ_REST_GRP,
                (byte) type, 0);
    }

    public GemsGroupFixture getGemsGroupFixture(Long groupId, Long fixtureid) {
        return gemsGroupDao.getGemsGroupFixture(groupId, fixtureid);
    }

    public List<GemsGroupFixture> getGemsGroupFixtureByGroup(Long groupId) {
        return gemsGroupDao.getGemsGroupFixtureByGroup(groupId);
    }
}
