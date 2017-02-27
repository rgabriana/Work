/**
 * 
 */
package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.DRUsers;
import com.ems.utils.ArgumentUtils;

/**
 * @author yogesh
 * 
 */
@Repository("drUserDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DRUserDao extends BaseDaoHibernate {

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.DRUserDao#loadAllDRUsers()
     */
    @SuppressWarnings("unchecked")
	public List<DRUsers> loadAllDRUsers() {
        try {
            List<DRUsers> drUsers = getSession().createCriteria(DRUsers.class).addOrder(Order.asc("id")).list();
            if (!ArgumentUtils.isNullOrEmpty(drUsers)) {
                return drUsers;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

}
