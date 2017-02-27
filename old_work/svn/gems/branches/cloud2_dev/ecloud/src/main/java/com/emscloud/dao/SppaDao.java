/**
 * 
 */
package com.emscloud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

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
import com.emscloud.model.CustomerBills;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.types.BillStatus;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.util.DateUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("sppaDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SppaDao {

	@Resource
  SessionFactory sessionFactory;
	
	@Resource
	EmInstanceManager emInstanceManger;
	
	/**
	 * 
	 */
	public SppaDao() {
		// TODO Auto-generated constructor stub
	}
	
	private Connection getDbConnection(String dbName, String replicaServer) {
		
		String dbUser = "postgres";
		String dbPassword = "postgres";
		
		Connection connection = null;		
		if(dbName == null) {
			return null;
		}
		try {
			String conString = "jdbc:postgresql://" + replicaServer + ":" + DatabaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, dbUser, dbPassword);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return connection;
		
	} //end of method getDbConnection
	
	public List<SppaBill> getEmBillsOfCustomerBill(long custBillId) {
		
		List<SppaBill> billList = new ArrayList<SppaBill>();		
		try {
			String hsql = " Select bill from SppaBill bill where bill.customerBill.id = :custBillId order by bill.id desc";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("custBillId", custBillId);
			billList = q.list();
			if (billList != null && !billList.isEmpty()) {
				return billList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return billList;
		
	} //end of method getEmBillsOfCustomerBill
	
	public SppaBill getMonthlyBillData(String dbName, String replicaServerHost, Date fromDate, Date toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		try {
			connection = getDbConnection(dbName, replicaServerHost);
			stmt = connection.createStatement();
			String query = "SELECT SUM(base_power_used) AS baseline_energy, SUM(base_cost) AS base_cost, " +
					"SUM(power_used) AS consumed_energy, SUM(saved_cost) AS saved_cost FROM energy_consumption_daily WHERE capture_at > '" + 
					DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss") + "' AND capture_at <= date '" +
					DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "' + interval '1 day'";
			rs = stmt.executeQuery(query);		
			SppaBill bill = null;
			if(rs.next()) {
				bill = new SppaBill();
				bill.setBaselineEnergy(rs.getBigDecimal("baseline_energy"));
				bill.setBaseCost(rs.getDouble("base_cost"));
				bill.setConsumedEnergy(rs.getBigDecimal("consumed_energy"));
				bill.setSavedCost(rs.getDouble("saved_cost"));
				return bill;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return null;
		
	} //end of method getMonthlyBill
	
	public SppaBill saveOrUpdate(SppaBill bill) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(bill) ;
		return bill ;
		
	} //end of method saveOrUpdate
	
	public CustomerSppaBill saveOrUpdate(CustomerSppaBill bill) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(bill) ;
		return bill ;
		
	} //end of method saveOrUpdate
	
	public List<SppaBill> getLastMonthBillPerCustomer(long custId) {
		
		List<SppaBill> billList = new ArrayList<SppaBill>();
		
		try {
            
            String hsql = " Select bill from SppaBill bill, EmInstance em where bill.emInstance.id = em.id " +
                          " and em.customer.id = :custId order by bill.id desc";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setLong("custId", custId);
            billList = q.list();
            if (billList != null && !billList.isEmpty()) {
            return billList ;
            }
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        		return billList;
		
	} //end of method getLastMonthBillPerCustomer

@SuppressWarnings("unchecked")
	public CustomerBills getAllBillsPerCustomer(Long id,String orderway, int offset, int limit) {
		CustomerBills customerBills = new CustomerBills();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(CustomerSppaBill.class, "custbill").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(CustomerSppaBill.class, "custbill");
	
		oRowCount.add(Restrictions.eq("custbill.customer.id", id));
		oRowCount.add(Restrictions.ne("custbill.billStatus", BillStatus.OBSOLETE.ordinal()));
		oCriteria.add(Restrictions.eq("custbill.customer.id", id));
		oCriteria.add(Restrictions.ne("custbill.billStatus", BillStatus.OBSOLETE.ordinal()));
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("custbill.billEndDate"));
		} 
		else {
			oCriteria.addOrder(Order.asc("custbill.billEndDate"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			customerBills.setTotal(count);
			customerBills.setCustomerSppaBill(oCriteria.list());
			return customerBills;
		}
		return customerBills;	
	}

	/**
	 * load customer sppa bill by id
	 * 
	 * @param customerName Customer Name
	 *            
	 * @return Load Customer details by name
	 */
	@SuppressWarnings("unchecked")
	public CustomerSppaBill loadCustomerSppaBillById(Long billId) {
  
		try {
			CustomerSppaBill sppaBill = null;
			String hsql = "from CustomerSppaBill sppaBill where sppaBill.id = ?";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setParameter(0, billId);
			sppaBill = (CustomerSppaBill)q.uniqueResult(); 
			if (sppaBill != null) {
				return sppaBill;           
			}
    } catch (HibernateException hbe) {
    	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
    }
    return null;
    
	} //end of method loadCustomerSppaBillId
	
	public List<CustomerSppaBill> getEMBillsByCustomerId(long customerId) {
		List<CustomerSppaBill> billList = new ArrayList<CustomerSppaBill>();
		try {
			String hsql = " Select bill from CustomerSppaBill bill where bill.customer.id = :customerId and bill.billStatus!='2' order by bill.id desc limit 1";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("customerId", customerId);
			billList = q.list();
			if (billList != null && !billList.isEmpty()) {
				return billList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return billList;
		
	} //end of method getEmBillsOfCustomerBill
} //end of class SppaDao
