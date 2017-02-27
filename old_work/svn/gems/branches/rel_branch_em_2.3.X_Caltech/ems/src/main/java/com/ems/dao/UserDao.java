package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Floor;
import com.ems.model.Role;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.model.UserLocations;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("userDao")
@Transactional(propagation = Propagation.REQUIRED)
public class UserDao extends BaseDaoHibernate {

    /**
     * load user details if user email is given.
     * 
     * @param userId
     *            user email or user id.
     * @return User com.ems.model.User object
     */
    @SuppressWarnings("unchecked")
    public User loadUserByUserName(String userId) {
        try {
            List<User> results = null;
            String hsql = "from User u where u.email=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                User user = (User) results.get(0);
                user.getRole().getModulePermissions().size();
                return user;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load user details if id is given.
     * 
     * @param id
     * @return User com.ems.model.User object
     */
    @SuppressWarnings("unchecked")
    public User loadUserById(Long id) {
        try {
            List<User> results = null;
            String hsql = "from User u where u.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                User user = (User) results.get(0);
                if (user.getRole().getModulePermissions() != null) {
                    user.getRole().getModulePermissions().size();
                }
                return user;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load role object if user id is given
     * 
     * @param name
     *            Role name like Admin,Auditor,Employee
     * @return com.ems.model.Role object
     */
    @SuppressWarnings("unchecked")
    public Role loadRoleByUserId(Long userId) {
        try {
            List<Role> results = null;
            String hsql = "Select r from Role r, User u where u.role.id = r.id and u.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (Role) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /**
     * load all pending users. pending users are the users where role is null
     * 
     * @return com.ems.model.User collection
     */
    @SuppressWarnings("unchecked")
    public List<User> loadAllPendingUsers() {
        try {
            List<User> results = null;
            String hsql = "from User u where u.role is null";
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
     * load all active users. active users are the users where role is not null
     * 
     * @return com.ems.model.User collection
     */
    @SuppressWarnings("unchecked")
    public List<User> loadCompanyUsers() {
        try {
            List<User> results = null;
            String hsql = "from User u where u.role is not null and u.email != 'admin' and u.tenant is null order by email asc";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {

                for (int i = 0; i < results.size(); i++) {
                    User user = results.get(i);
                    Long locationId = user.getLocationId();
                    String locationType = user.getLocationType();
                    String location = getLocation(locationId, locationType);
                    user.setLocation(location);
                }
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    public List<User> loadTenantUsers(Long tenantId) {
        List<User> results = null;
        Tenant tenant = (Tenant) getSession().load(Tenant.class, tenantId);
        String hsql = "from User u where u.role is not null and u.email != 'admin' and u.tenant = :tenant"
                + " order by email asc";
        Query q = getSession().createQuery(hsql);
        q.setParameter("tenant", tenant);
        results = q.list();
        return results;
    }

    private String getLocation(Long locationId, String locationType) {

        String location = "";
        if (locationType != null) {

            if (locationType.equalsIgnoreCase("floor")) {

                FloorDao floordao = (FloorDao) SpringContext.getBean("floorDao");
                if (floordao != null) {

                    Floor floor = floordao.getFloorById(locationId);
                    if (floor != null)
                        location = floor.getName();
                }

            } else if (locationType.equalsIgnoreCase("building")) {

                BuildingDao buildingdao = (BuildingDao) SpringContext.getBean("buildingDao");
                if (buildingdao != null) {

                    Building building = buildingdao.getBuildingById(locationId);
                    if (building != null)
                        location = building.getName();
                }

            } else if (locationType.equalsIgnoreCase("campus")) {

                CampusDao campusdao = (CampusDao) SpringContext.getBean("campusDao");
                if (campusdao != null) {

                    Campus campus = campusdao.loadCampusById(locationId);
                    if (campus != null)
                        location = campus.getName();
                }

            }
        }
        return location;
    }

    /**
     * update user details if column name,value and id is given.
     * 
     * @param column
     *            column name of database table
     * @param value
     *            updated value
     * @param id
     *            database id(primary key)
     */
    public void updateUserDetails(String column, String value, String id) {
        String hql = "update User set " + column + " = :newValue where id = :id";
        Query query = getSession().createQuery(hql);
        if (!"role_id".equals(column) && !"approved_location_id".equals(column) && !"location_id".equals(column)) {
            query.setString("newValue", value);
        } else {
            query.setLong("newValue", new Long(value));
        }
        query.setLong("id", new Long(id));
        query.executeUpdate();
    }

    public List<Role> getAllRoles() {
        String hql = "Select r from Role r";
        Query query = getSession().createQuery(hql);
        List<Role> roles = query.list();
        return roles;
    }

    /* Added by Nitin to delete user by id */

    /**
     * delete user details if id is given.
     * 
     * @param id
     * @return User com.ems.model.User object
     */

    public void deleteUserById(Long id) {
        try {

            // Remove all user switches
            String hqluserswitches = "delete from UserSwitches where userId = :id";
            Query queryuserswitches = getSession().createQuery(hqluserswitches);
            queryuserswitches.setLong("id", new Long(id));
            queryuserswitches.executeUpdate();

            // Set the user id to null in emsuser audit table
            String hqlemsuseraudit = "update EmsUserAudit set user = null where user.id = :id";
            Query queryemsuseraudit = getSession().createQuery(hqlemsuseraudit);
            queryemsuseraudit.setLong("id", new Long(id));
            queryemsuseraudit.executeUpdate();

            // update 'firmware_upgrade' for userid.
            String hqlfirmware = "update FirmwareUpgrade set user = null where user.id = :id";
            Query queryhqlfirmware = getSession().createQuery(hqlfirmware);
            queryhqlfirmware.setLong("id", new Long(id));
            queryhqlfirmware.executeUpdate();

            // Remove all user locations - This is still not used.
            String hqluserlocations = "delete from  UserLocations where user.id = :id";
            Query queryuserLocations = getSession().createQuery(hqluserlocations);
            queryuserLocations.setLong("id", new Long(id));
            queryuserLocations.executeUpdate();

            // Now delete the user
            String hqluser = "delete from User where id = :id";
            Query queryuser = getSession().createQuery(hqluser);
            queryuser.setLong("id", new Long(id));
            queryuser.executeUpdate();

        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }

    }

    public void deleteUserLocation(UserLocations userLocation) {
        Session session = this.getSession();
        session.delete(userLocation);
    }
    
    /**
     * Added by Nitin
     * load all active users. active users are the users where role is not null and their role is not 'Auditor'
     * 
     * @return com.ems.model.User collection
     */
    @SuppressWarnings("unchecked")
    public List<User> loadCompanyUsersExceptAuditor() {
        try {
            List<User> results = null;
            String hsql = "from User u where u.role is not null and u.role not in (select id from Role where roleType = 'Auditor') and u.email != 'admin' and u.tenant is null order by email asc";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {

                for (int i = 0; i < results.size(); i++) {
                    User user = results.get(i);
                    Long locationId = user.getLocationId();
                    String locationType = user.getLocationType();
                    String location = getLocation(locationId, locationType);
                    user.setLocation(location);
                }
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
}
