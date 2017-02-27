package com.ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.GroupDao;
import com.ems.dao.ProfileDao;
import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("groupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GroupManager {

    // Added by Nitin
    Map<Long, TreeNode<FacilityType>> profileTreeMap = new HashMap<Long, TreeNode<FacilityType>>();

    @Resource
    private GroupDao groupDao;

    @Resource
    private ProfileDao profileDao;

    /**
     * save group details.
     * 
     * @param group
     *            com.ems.model.Group
     */
    public Groups save(Groups group) {
        return (Groups) groupDao.saveObject(group);
    }

    /**
     * update group details.
     * 
     * @param group
     *            com.ems.model.Group
     */
    public Groups update(Groups group) {
        return (Groups) groupDao.saveObject(group);
    }

    /**
     * Load company's group
     * 
     * @param id
     *            company id
     * @return com.ems.model.Group collection
     */
    public List<Groups> loadGroupByCompanyId(Long id) {
        return groupDao.loadGroupByCompanyId(id);
    }

    /**
     * Load all group
     * 
     * @return com.ems.model.Group collection
     */
    public List<Groups> loadAllGroups() {
        return groupDao.loadAllGroups();
    }

    /**
     * Delete Groups details
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
        groupDao.removeObject(Groups.class, id);
    }

    public Groups getGroupById(Long id) {
        return groupDao.getGroupById(id);
    }

    public Groups getGroupByName(String sGroupname) {
        return groupDao.getGroupByName(sGroupname);
    }

    public Groups editName(Groups groups) {
        return groupDao.editName(groups);
    }

    public void updateGroupProfile(ProfileHandler profileHandler, Long groupId) {
        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        ProfileHandler copyProfileHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            groupDao.updateGroupProfile(copyProfileHandler, groupId);
        } else {
            copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        }
        DeviceServiceImpl.getInstance().updateGroupProfile(copyProfileHandler, groupId);
    }

    public void updateAdvanceGroupProfile(ProfileHandler profileHandler, Long groupId) {
        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        ProfileHandler copyProfileHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            groupDao.updateGroupProfile(copyProfileHandler, groupId);
        } else {
            copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        }
        DeviceServiceImpl.getInstance().updateAdvanceGroupProfile(copyProfileHandler, groupId);
    }

    // Added by Nitin
    public TreeNode<FacilityType> loadProfileHierarchy() {

        if (profileTreeMap.containsKey(0L)) {
            return profileTreeMap.get(0L);
        }

        TreeNode<FacilityType> profileHierachy = groupDao.loadProfileHierarchy();
        profileTreeMap.put(0L, profileHierachy);
        return profileHierachy;
    }

    public List<Groups> getDRSensitivity() {
        return groupDao.getDRSensitivity();
    }

    public List<GroupECRecord> getDRSensitivityRecords() {
        return groupDao.getDRSensitivityRecords();
    }

    public void dimFixtures(int groupId, int percentage, int time) {
        DeviceServiceImpl.getInstance().dimFixturesByGroup(groupId, percentage, time);
    }

}
