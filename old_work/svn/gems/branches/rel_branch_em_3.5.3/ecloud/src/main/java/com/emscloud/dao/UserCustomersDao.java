package com.emscloud.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.Customer;
import com.emscloud.model.UserCustomers;

@Repository("userCustomersDao")
@Transactional(propagation = Propagation.REQUIRED)
public class UserCustomersDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(UserCustomersDao.class
			.getName());

	@Resource
	SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public List<UserCustomers> loadUserCustomersByUserId(Long userId) {
		// TODO Auto-generated method stub

		Criteria rowCount = getSession().createCriteria(UserCustomers.class)
				.add(Restrictions.eq("user.id", userId));
		List<UserCustomers> list = rowCount.list();
		return list;

		/*
		 * try { List<UserCustomers> results = null; String hsql =
		 * "from UserCustomers uc where uc.id=?"; Query q =
		 * sessionFactory.getCurrentSession().createQuery(hsql.toString());
		 * q.setParameter(0, userId); results = q.list(); if (results != null &&
		 * !results.isEmpty()) { return results; } } catch (HibernateException
		 * hbe) { throw
		 * SessionFactoryUtils.convertHibernateAccessException(hbe); }
		 */

	}

	public void deleteOldAssignments(Long userid) {
		// TODO Auto-generated method stub
		String hsql = null;
		Query q = null;
		try {
			hsql = "delete from UserCustomers uc where uc.user.id=?";
			q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setParameter(0, userid);
			q.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return;
	}

	public void removeByUserAndCustomerId(Long userid, Long customerKey) {
		// TODO Auto-generated method stub
		String hsql = null;
		Query q = null;
		try {
			hsql = "delete from UserCustomers uc where uc.user.id=? and uc.customer.id=?";
			q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setParameter(0, userid);
			q.setParameter(1, customerKey);
			q.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return;
	}

}
