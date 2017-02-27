package com.emscloud.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.BaseDaoHibernate;
import com.emscloud.model.Users;
import com.emscloud.vo.UsersList;

@Repository("userDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class UserDao extends BaseDaoHibernate{

	static final Logger logger = Logger.getLogger(UserDao.class.getName());
	
    @Resource
    SessionFactory sessionFactory;
    
    public void saveOrUpdate(Users user) {
		sessionFactory.getCurrentSession().saveOrUpdate(user);		
	}

    @Transactional(propagation = Propagation.REQUIRED,readOnly=false)
    public Users getUserByEmail(String userName) {
    	Users user = null;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(Users.class);
    	criteria.add(Restrictions.eq("email", userName));
    	user = (Users)criteria.uniqueResult();
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
    public Users loadUserByUserName(String userId) {
        try {
            List<Users> results = null;
            String hsql = "from Users u where u.email=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                Users user = (Users) results.get(0);
               // user.getRole().getModulePermissions().size();
                return user;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Users loadUserByUserId(Long userId) {
        try {
            List<Users> results = null;
            String hsql = "from Users u where u.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, userId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                Users user = (Users) results.get(0);
               // user.getRole().getModulePermissions().size();
                return user;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

	@SuppressWarnings("unchecked")
	public UsersList loadAllUsers(String orderway, String orderby,int offset, int limit) {
		
		UsersList usersList = new UsersList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(Users.class, "users")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				Users.class, "users");
		

		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		
		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("email")) {
				orderby = "users.email";
			} else if (orderby.equals("firstname")) {
				orderby = "users.firstName";
			} else if (orderby.equals("lastname")) {
				orderby = "users.lastName";
			} else if (orderby.equals("roletype")) {
				orderby = "users.roleType";
			} else if (orderby.equals("status")) {
				orderby = "users.status";
			} else {
				orderby = "users.id";
			}
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("id"));
			}else{
				oCriteria.addOrder(Order.asc("id"));
			}
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			usersList.setTotal(count);
			usersList.setUsers(oCriteria.list());
			return usersList;
		}

		return usersList;
	}

}
