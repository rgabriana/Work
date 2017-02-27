package com.emscloud.dao;

import java.math.BigDecimal;
import java.util.Date;
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

import com.emscloud.model.BillPayments;
import com.emscloud.model.Customer;
import com.emscloud.model.CustomerBillPayment;
import com.emscloud.model.EmInstance;

@Repository("billPaymentDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BillPaymentDao {
	static final Logger logger = Logger.getLogger(BillPaymentDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	@Resource
	CustomerDao customerDao;
	/**
	 * Load list of all payment Received by Client so far
	 * 
	 * @param customerName Customer Name
	 *            
	 * @return Load Customer details by name
	 */
	@SuppressWarnings("unchecked")
	public CustomerBillPayment getAllBillPaymentPerCustomer(Long id,String orderway, int offset, int limit) {
		CustomerBillPayment customerBillPayment = new CustomerBillPayment();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(BillPayments.class, "custbillPmt").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(BillPayments.class, "custbillPmt");
	
		oRowCount.add(Restrictions.eq("custbillPmt.customer.id", id));
		oCriteria.add(Restrictions.eq("custbillPmt.customer.id", id));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("custbillPmt.paymentDate"));
		} 
		else {
			oCriteria.addOrder(Order.asc("custbillPmt.paymentDate"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			customerBillPayment.setTotal(count);
			customerBillPayment.setBillPayments(oCriteria.list());
			return customerBillPayment;
		}
		return customerBillPayment;	
	}
	
	 /**
     * Update the BillPayment for the Customer
     * @param CustomerID
     * @param PaymentAmount
     * @return void
     */
	public void updateCustomerBillPayment(long customerId, BigDecimal paymentAmount) {
		Session session = sessionFactory.getCurrentSession();
		BillPayments billPayments = new BillPayments();
		Customer customer = customerDao.loadCustomerById(customerId);
		billPayments.setCustomer(customer);
		billPayments.setPaymentAmount(paymentAmount.doubleValue());
		billPayments.setPaymentDate(new Date());
		session.saveOrUpdate(billPayments);
	}
	
	public Double getPaymentRcvdFromDate(long customerId, Date date) {
	
		try {
			String hsql = " Select SUM(paymentAmount) from BillPayments billPayments where billPayments.customer.id = :customerId";
			if(date != null) {
				hsql += " AND billPayments.paymentDate > :date";
			}
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("customerId", customerId);
			if(date != null) {
				q.setTimestamp("date", date);
			}
			System.out.println("query - " + q.toString());
			List<Double> doubleList = q.list();
			if (doubleList != null && !doubleList.isEmpty()) {
				System.out.println("size == " + doubleList.size());
				Double payments = doubleList.get(0);
				if(payments != null) {
					return payments;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("returning 0");
		return 0d;	
		
	} //end of method getPaymentRcvdFromDate
	
}
