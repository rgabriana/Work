package com.emsdashboard.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.BaseDaoHibernate;
import com.emsdashboard.model.User;

@Repository("userDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class UserDao extends BaseDaoHibernate{

	static final Logger logger = Logger.getLogger(UserDao.class.getName());
	
    @Resource
    SessionFactory sessionFactory;

    @Transactional(propagation = Propagation.REQUIRED,readOnly=false)
    public User getUserByEmail(String userName) {
    	User user = null;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(User.class);
    	criteria.add(Restrictions.eq("email", userName));
    	user = (User)criteria.uniqueResult();
    	return user;
    }

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
               // user.getRole().getModulePermissions().size();
                return user;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
