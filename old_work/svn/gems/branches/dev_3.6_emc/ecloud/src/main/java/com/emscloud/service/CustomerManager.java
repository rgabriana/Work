package com.emscloud.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.CustomerDao;
import com.emscloud.model.Customer;
import com.emscloud.model.CustomerList;
import com.emscloud.vo.Organization;



@Service("customerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CustomerManager {
	
	@Resource
	private CustomerDao customerDao;

	/**
	 * load customer according to specified name
	 * 
	 * @param customerName
	 *            Customer Name. 
	 * @return Customer as per the given customer Name
	 */
	public Customer loadCustomerByName(String customerName) {
		return customerDao.loadCustomerByName(customerName) ;
	}

	public List<Customer> loadallCustomer() {
		
		return customerDao.loadAllCustomers() ;
	}
	
	public List<Customer> loadUnMappedCustomers(List<Long> ids)
	{
		
		return customerDao.loadUnMappedCustomers(ids);
		
	}
	
	public CustomerList loadCustomerList(String orderway, int offset, int limit,List<Long> cList) {
		
		return customerDao.loadCustomerList(orderway, offset, limit,cList) ;
	}

	public void saveOrUpdate(Customer customer) {
		customerDao.saveOrUpdate(customer) ;
		
	}

	public Customer loadCustomerById(Long customerId) {
		// TODO Auto-generated method stub
		return customerDao.loadCustomerById(customerId);
	}
	
	public List<Organization> getAllOrganizations() {
		
		List<Customer> custList = customerDao.loadAllCustomers();
		ArrayList<Organization> orgList = new ArrayList<Organization>();
		for(Customer c:custList) {
			Organization org = new Organization();
			org.setId(c.getId());
			org.setName(c.getName());
			orgList.add(org);
		}
		return orgList;
				
	} //end of method getAllOrganizations

}