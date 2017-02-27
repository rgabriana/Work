package com.ems.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.UserSwitchBean;
import com.ems.dao.SwitchDao;
import com.ems.dao.UserDao;
import com.ems.dao.UserSwitchesDao;
import com.ems.model.Role;
import com.ems.model.Switch;
import com.ems.model.User;
import com.ems.model.UserLocations;
import com.ems.model.UserSwitches;
import com.ems.types.RoleType;
import com.ems.util.Constants;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("userManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UserManager {

    @Resource
    private UserDao userDao;

    @Resource
    private UserSwitchesDao userSwitchesDao;

    @Resource
    private SwitchDao switchDao;

    private List<Role> allRoles;
    private List<Role> companyRoles;
    private List<Role> tenantRoles;

    public void initializeRoles() {
        this.allRoles = userDao.getAllRoles();
        this.companyRoles = new ArrayList<Role>();
        this.tenantRoles = new ArrayList<Role>();
        for (Role role : allRoles) {
            switch (role.getRoleType()) {
            case Admin: {
                break;
            }
            case Auditor: {
                companyRoles.add(role);
                break;
            }
            case Employee: {
                companyRoles.add(role);
                tenantRoles.add(role);
                break;
            }
            case FacilitiesAdmin: {
                companyRoles.add(role);
                break;
            }
            case TenantAdmin: {
                tenantRoles.add(role);
                break;
            }
            }
        }

    }

    // private List

    /**
     * save user details.
     * 
     * @param user
     *            com.ems.model.User
     */
    public User save(User user) {
        user.setCreatedOn(new Date());
        return (User) userDao.saveObject(user);
    }

    /**
     * edit user details.
     * 
     * @param user
     *            com.ems.model.User
     */
    public User update(User user) {
        return (User) userDao.saveObject(user);
    }

    /**
     * load user details if user email is given.
     * 
     * @param userId
     *            user email or user id.
     * @return User com.ems.model.User object
     */
    public User loadUserByUserName(String userId) {
        return userDao.loadUserByUserName(userId);
    }

    /**
     * load all pending users. pending users are the users where role is null
     * 
     * @return com.ems.model.User collection
     */
    public List<User> loadAllPendingUsers() {
        return userDao.loadAllPendingUsers();
    }
    
    /**
     * load all users who are having secret_key as absent
     * @return
     */
    public List<User> loadAllUsersHavingNoSecretKey() {
        return userDao.loadAllUsersHavingNoSecretKey();
    }

    /**
     * Load user object if id is given
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.User object
     */
    public User loadUserById(Long id) {
        return (User) userDao.getObject(User.class, id);
    }

    /**
     * load all active users. active users are the users where role is not null
     * 
     * @return com.ems.model.User collection
     */
    public List<User> loadCompanyUsers() {
        return userDao.loadCompanyUsers();
    }

    public List<User> loadTenantUsers(Long tenantId) {
        return userDao.loadTenantUsers(tenantId);
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
        userDao.updateUserDetails(column, value, id);
    }
    
    /**
     * update user details if column name,value and id is given in a new transaction.
     * 
     * @param column
     *            column name of database table
     * @param value
     *            updated value
     * @param id
     *            database id(primary key)
     */
    public void updateUserDetailsInNewTransaction(String column, String value, String id) {
    	userDao.updateUserDetailsInNewTransaction(column, value, id);
    }

    /**
     * Delete user object
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
        userDao.removeObject(User.class, id);
    }

    /**
     * load role object if user id is given
     * 
     * @param name
     *            Role name like Admin,Auditor,Employee
     * @return com.ems.model.Role object
     */
    public Role loadRoleByUserId(Long userId) {
        return userDao.loadRoleByUserId(userId);
    }
    
    
    public Role getRoleByRoleType(RoleType roleType) {
    	return userDao.getRoleByRoleType(roleType);
    }
    
    public List<User> loadUsersByRole(RoleType roleType){
    	return userDao.loadUsersByRole(roleType);
    }

    public User loadBacnetUser(){
    	final List<User> l = userDao.loadUsersByRole(RoleType.Bacnet);
    	if (l != null && l.size() > 0){
    		return l.get(0);
    	}else{
    		return null;
    	}
    }
    
    public List<UserSwitchBean> loadSwitchesbyUserId(Long id) {

        List<UserSwitchBean> switchBeanList = new ArrayList<UserSwitchBean>();
        User user = userDao.loadUserById(id);

        if (user != null) {
            Long locationId = user.getLocationId();
            String locationType = user.getLocationType();
            Long roleid = user.getRole().getId();
            String userName = user.getEmail();
            List<Switch> switches = null;
            if (roleid == 1) {
                // admin will have access to all switches

            } else {

                if (locationType != null && locationId != null) {

                    if (locationType.equalsIgnoreCase("Floor")) {

                        switches = switchDao.loadSwitchByFloorId(locationId);

                    } else if (locationType.equalsIgnoreCase("Building")) {

                        switches = switchDao.loadSwitchByBuildingId(locationId);
                    }

                    if (switches != null) {

                        List<UserSwitches> userswitchList = userSwitchesDao.loadUserSwitchesByUserId(id);
                        HashMap<Long, UserSwitches> usswitchMap = new HashMap<Long, UserSwitches>();

                        if (userswitchList != null) {

                            for (int i = 0; i < userswitchList.size(); i++) {
                                UserSwitches obj = userswitchList.get(i);
                                usswitchMap.put(obj.getSwitchId(), obj);
                            }
                        }

                        for (int i = 0; i < switches.size(); i++) {

                            UserSwitchBean bean = new UserSwitchBean();
                            Switch sw = switches.get(i);
                            bean.setSwitchId(sw.getId());
                            bean.setSwitchName(sw.getName());
                            bean.setUserName(userName);

                            UserSwitches obj = usswitchMap.get(sw.getId());
                            if (obj != null) {
                                bean.setUserId(obj.getUserId());
                                bean.setSelected(true);
                                bean.setId(obj.getId());
                            } else {
                                bean.setSelected(false);
                                bean.setUserId(id);
                            }

                            switchBeanList.add(bean);
                        }
                    }
                }
            }

        }
        return switchBeanList;

    }

    public List<UserSwitches> loadSwitchListbyUserId(Long id) {
        return userSwitchesDao.loadUserSwitchesByUserId(id);
    }

    public List<UserSwitches> loadUserSwitchesByUserIdSwitchId(Long userId, Long switchId) {
        return userSwitchesDao.loadUserSwitchesByUserIdSwitchId(userId, switchId);
    }

    public UserSwitches saveSwitchToUser(UserSwitches us) {
        return (UserSwitches) userSwitchesDao.saveObject(us);
    }

    public UserSwitches addSwitchToUser(Long userId, Long switchId) {

        UserSwitches us = new UserSwitches();
        us.setUserId(userId);
        us.setSwitchId(switchId);
        return (UserSwitches) userSwitchesDao.saveObject(us);

    }

    public void removeSwitchFromUser(Long userId, Long switchId) {

        userSwitchesDao.deleteUserSwitches(userId, switchId);

    }

    public List<Role> loadRolesForTenant() {
        if (this.tenantRoles == null) {
            this.initializeRoles();
        }
        return this.tenantRoles;
    }

    public List<Role> loadRolesForCompany() {
        if (this.companyRoles == null) {
            this.initializeRoles();
        }
        return this.companyRoles;
    }

    public Role getRole(RoleType roleType) {
        if (this.companyRoles == null) {
            this.initializeRoles();
        }

        Role role = null;

        for (Role r : companyRoles) {
            if (r.getRoleType() == roleType) {
                role = r;
                break;
            }
        }

        return role;
    }
    
    public Role getRoleFromRoleList(RoleType roleType){
    	 if (this.allRoles == null) {
             this.initializeRoles();
         }

         Role role = null;

         for (Role r : allRoles) {
             if (r.getRoleType() == roleType) {
                 role = r;
                 break;
             }
         }

         return role;
    }
    /*Added by Nitin to delete user from DB*/
    
    /**
     * load all pending users. pending users are the users where role is null
     * 
     * @return com.ems.model.User collection
     */
    
    public void deleteUser(Long id) {
        userDao.deleteUserById(id);
    }

    public void saveUserLocation(UserLocations location) {
        userDao.saveObject(location);        
    }

	public void deleteUserLocation(UserLocations userLocation) {
		userDao.removeObject(UserLocations.class, userLocation.getId());
		
	}
	
	/*Added by Nitin*/
	
	 /**	 
     * load all active users. active users are the users where role is not null and their role is not 'Auditor'
     * 
     * @return com.ems.model.User collection
     */
    public List<User> loadCompanyUsersExceptAuditor() {
        return userDao.loadCompanyUsersExceptAuditor();
    }
    
    public Set<Long> loadUserSwitchesBySwitchId(Long switchId) {
        return userSwitchesDao.loadUserSwitchesBySwitchId(switchId);
    }
}
