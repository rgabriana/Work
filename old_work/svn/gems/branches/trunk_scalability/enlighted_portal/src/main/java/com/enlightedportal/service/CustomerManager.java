package com.enlightedportal.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedportal.dao.CustomerDao;
import com.enlightedportal.model.Customer;



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

	public void saveOrUpdate(Customer customer) {
		customerDao.saveOrUpdate(customer) ;
		
	}

	public Customer loadCustomerById(Long customerId) {
		// TODO Auto-generated method stub
		return customerDao.loadCustomerById(customerId);
	}

}