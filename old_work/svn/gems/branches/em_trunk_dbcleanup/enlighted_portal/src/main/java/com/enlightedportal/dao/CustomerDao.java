package com.enlightedportal.dao;

import java.util.ArrayList;
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

import com.enlightedportal.model.Customer;
import com.enlightedportal.model.LicenseDetails;
import com.enlightedportal.model.User;

@Repository("customerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CustomerDao {
	static final Logger logger = Logger.getLogger(CustomerDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	 /**
     * load customer by name
     * 
     * @param customerName Customer Name
     *            
     * @return Load Customer details by name
     */
    @SuppressWarnings("unchecked")
    public Customer loadCustomerByName(String customerName) {
        try {
            List<Customer> results = null;
            String hsql = "from Customer u where u.name=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, customerName);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	Customer customer = (Customer) results.get(0);
                  // user.getRole().getModulePermissions().size();
                   return customer;
               
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

	public List<Customer> loadAllCustomers() {
		List<Customer> results = new ArrayList<Customer>();
		try {
            
            String hsql = "from Customer order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            return results ;
            }
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
	}

	public void saveOrUpdate(Customer customer) {
		sessionFactory.getCurrentSession().saveOrUpdate(customer) ;
		
	}

	public Customer loadCustomerById(Long customerId) {
		 try {
	            List<Customer> results = null;
	            String hsql = "from Customer u where u.id=?";
	            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	            q.setParameter(0, customerId);
	            results = q.list();
	            if (results != null && !results.isEmpty()) {
	            	Customer customer = (Customer) results.get(0);
	                  // user.getRole().getModulePermissions().size();
	                   return customer;
	               
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return null;
	}

}
