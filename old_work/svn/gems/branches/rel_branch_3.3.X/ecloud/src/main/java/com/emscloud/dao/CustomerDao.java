package com.emscloud.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerList;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.types.RoleType;

@Repository("customerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CustomerDao {
	static final Logger logger = Logger.getLogger(CustomerDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
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
            String hsql = "from Customer u where upper(u.name)=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, customerName.toUpperCase());
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
	
	@SuppressWarnings("unchecked")
	public CustomerList loadCustomerList(String orderway, int offset, int limit,List<Long> cList) {
		CustomerList customerList = new CustomerList();
		
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		String roleType = emsAuthContext.getCurrentUserRoleType().getName().toString();
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(Customer.class, "customer").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(Customer.class, "customer");
		if(cList!=null && cList.size()>0)
		oCriteria.add(Restrictions.in("id", cList));
		else if((cList == null || cList.size()<=0) && !roleType.equals(RoleType.Admin.toString()))
		{
			return customerList;
		}	
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("customer.name"));
		} 
		else {
			oCriteria.addOrder(Order.asc("customer.name"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			customerList.setTotal(count);
			customerList.setCustomers(oCriteria.list());
			return customerList;
		}
		
		return customerList;	
		
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

	
	 /**
     * 
     * 
     * @return Customer com.emscloud.model.Customer object load only id,name,address,contact details of customer other details
     *         loads as null.
     */
    @SuppressWarnings("unchecked")
    public Customer loadCustomer() {
    	Customer customer = null ;
        ArrayList<Customer> companyList = new ArrayList<Customer>() ;
        companyList = (ArrayList<Customer>) getAllCustomer() ;
        	if(companyList!=null && !companyList.isEmpty()){
        		customer = companyList.get(0);
        	}
      
        return customer ;
    }

    /**
     * 
     * @return the list of all customers
     */
    @SuppressWarnings("unchecked")
    public List<Customer> getAllCustomer() {
        List<Customer> companies = sessionFactory.getCurrentSession().createQuery("from Customer order by id").list();
        return companies;
    }

	public List<Customer> loadUnMappedCustomers(List<Long> ids) {
		// TODO Auto-generated method stub
		List<Customer> results = new ArrayList<Customer>();
		String hsql = null;
		Query q = null;
		try {            
			if(ids.size()<=0 || ids==null)
			{
			hsql = "from Customer order by id";
			q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			}
			else
			{
            hsql = "from Customer where id not in (:ids) order by id";
            q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameterList("ids", ids);
			}            
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

    
}
