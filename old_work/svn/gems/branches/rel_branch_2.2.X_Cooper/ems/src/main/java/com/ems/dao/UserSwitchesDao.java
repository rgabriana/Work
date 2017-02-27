package com.ems.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.model.User;
import com.ems.model.UserSwitches;

@Repository("userSwitchesDao")
@Transactional(propagation = Propagation.REQUIRED)
public class UserSwitchesDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(UserSwitchesDao.class.getName());

    public List loadSwitchesbyUserId(Long id) {

        try {

            // Fetch the floor or building assigned to the user
            UserDao userDao = (UserDao) SpringContext.getBean("userDao");

            Long locationId = null;
            String locationType = null;
            if (userDao != null) {
                User user = userDao.loadUserById(id);
                locationId = user.getLocationId();
                locationType = user.getLocationType();

            }

            List results = null;
            String hsql = "select sw.id as switchid,us.id as usid,us.switch_id as usswid,us.user_id as ususerid from switch sw left join  user_switches us on us.switch_id=sw.id ";
            Query q = getSession().createSQLQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {

                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;

    }

    public List loadUserSwitchesByUserId(Long id) {
        try {
            List<User> results = null;
            String hsql = "from UserSwitches u where u.userId=?";
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

    public List<UserSwitches> loadUserSwitchesByUserIdSwitchId(Long userId, Long switchId) {
        try {
            List<UserSwitches> results = null;
            String hsql = "from UserSwitches u where u.userId=? and u.switchId=? ";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            q.setParameter(1, switchId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    public void deleteUserSwitches(Long userId, Long switchId) {
        try {
            String hsql = "delete from UserSwitches u where u.userId=? and u.switchId=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            q.setParameter(1, switchId);
            int status = q.executeUpdate();

        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }

    }
    
    @SuppressWarnings("unchecked")
	public Set<Long> loadUserSwitchesBySwitchId(Long switchId) {
    	Set<Long> userIds = new HashSet<Long>();
        try {
            String hsql = "from UserSwitches u where u.switchId=? ";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, switchId);
            List<UserSwitches> results = q.list();
            if (results != null && !results.isEmpty()) {
                for(UserSwitches userSwitch: results) {
                	userIds.add(userSwitch.getUserId());
                }
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return userIds;
    }
}
