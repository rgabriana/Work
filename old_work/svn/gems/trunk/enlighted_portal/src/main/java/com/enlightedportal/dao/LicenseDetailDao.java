package com.enlightedportal.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedportal.model.LicenseDetails;

@Repository("licenseDetailDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class LicenseDetailDao  {
	
static final Logger logger = Logger.getLogger(LicenseDetailDao.class.getName());
	
    @Resource
    SessionFactory sessionFactory;
    
    /**
     * load License key details for one customer 
     * 
     * @param customerId
     *            
     * @return License details acquored by that customer. 
     */
    @SuppressWarnings("unchecked")
    public List<LicenseDetails> loadLicenseDetailsByCustomerId(Long customerId) {
        try {
            List<LicenseDetails> results = null;
            String hsql = "from LicenseDetails u where u.customerId=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, customerId);
            results = q.list();
            if (results != null && !results.isEmpty()) {
               
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

	public void saveOrUpdate(LicenseDetails licenseDetails) {
		sessionFactory.getCurrentSession().saveOrUpdate(licenseDetails) ;
		
	}

	public List<LicenseDetails> loadAllLicenseDetails() {
		  try {
	            List<LicenseDetails> results = null;
	            String hsql = "from LicenseDetails order by id";
	            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	            results = q.list();
	            if (results != null && !results.isEmpty()) {
	               
	                return results;
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return null;
	}

	public byte[] loadApiKeyWRTMac(String mac) {
		  try {
	            List<LicenseDetails> results = null;
	            String hsql = "from LicenseDetails u where u.macId=?";
	            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	            q.setParameter(0, mac);
	            results = q.list();
	            if (results != null && !results.isEmpty()) {
	               
	                return results.get(0).getApiKey();
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return null;
		
	}



}
