package com.ems.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("groupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GroupDao extends BaseDaoHibernate {

    /**
     * Load company's group
     * 
     * @param id
     *            company id
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadGroupByCompanyId(Long id) {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name) from Groups g where g.company.id = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Load All groups
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadAllGroups() {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId) from Groups g order by g.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    /**
     * Load All groups Except Default Profile
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadAllGroupsExceptDeafult() {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId) from Groups g where name!='Default' order by g.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Groups getGroupById(Long id) {
        List<Groups> groupsList = getSession().createQuery("Select new Groups(id, name) from Groups where id = :id")
                .setLong("id", id).list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    public Groups getGroupByName(String sGroupname) {
        List<Groups> groupsList = getSession()
                .createQuery(
                        "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId) from Groups g where g.name = :name")
                .setString("name", sGroupname).list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    public Groups editName(Groups groups) {
        Session session = getSession();
        session.createQuery("Update Groups set name = :name where id = :id").setString("name", groups.getName())
                .setLong("id", groups.getId()).executeUpdate();
        return getGroupById(groups.getId());
    }

    public void updateGroupProfile(ProfileHandler profileHandler, Long groupId) {
        try {
            Session session = getSession();
            Groups group = (Groups) session.get(Groups.class, groupId);
            group.setProfileHandler(profileHandler);
            session.save("profileHandler", group);
        } catch (HibernateException hbe) {
            hbe.printStackTrace();
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    // Added by Nitin to get the list of profiles based on company

    public TreeNode<FacilityType> loadProfileHierarchy() {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId("0");
        rootNode.setName("Profile");
        rootNode.setNodeType(FacilityType.GROUP);

        List<Groups> groupList = loadAllGroups();
        if (groupList != null) {
            for (Groups group : groupList) {
                TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
                profileNode.setNodeId(group.getId().toString());
                profileNode.setName(group.getName());
                profileNode.setNodeType(FacilityType.GROUP);
                rootNode.addTreeNode(profileNode);
            }
        }
        return rootNode;
    }

    public List<Groups> getDRSensitivity() {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileHandler.drReactivity) from Groups g";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * Returns the dr sensitivity formatted for the webservice layer to access it.
     * @return Group EC records with dr sensitivity field updated.
     */
    public List<GroupECRecord> getDRSensitivityRecords() {
        try {
            List<GroupECRecord> drSensitivityRecords = new ArrayList<GroupECRecord>();
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileHandler.drReactivity) from Groups g";
            Query q = getSession().createQuery(hsql.toString());
            List<Groups> results = q.list();
            Groups oGroup = null;
            if (results != null && !results.isEmpty()) {
                Iterator<Groups> oRecords = results.iterator();
                while (oRecords.hasNext()) {
                    oGroup = oRecords.next();
                    GroupECRecord groupRecord = new GroupECRecord();
                    groupRecord.setI(oGroup.getId().intValue());
                    groupRecord.setName(oGroup.getName());
                    groupRecord.setDrSensitivity((int) oGroup.getProfileHandler().getDrReactivity());
                    drSensitivityRecords.add(groupRecord);
                }
                return drSensitivityRecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public ProfileHandler getProfileHandlerByGroupId(Long id) {
    	
    	Session session = getSession();
        ProfileHandler profileHandler = (ProfileHandler) session.get(ProfileHandler.class, id);
        if(profileHandler!=null)
        	return profileHandler;
        return null;
    }

}
