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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.emscloud.model.CustomerBills;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.types.BillStatus;
import com.emscloud.types.SiteAnomalyType;
import com.emscloud.util.DatabaseUtil;
import com.emscloud.util.DateUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Repository("sppaDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SppaDao {

	static final Logger logger = Logger.getLogger(SppaDao.class.getName());
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
	
	public List<SppaBill> getLongTermRemainingSiteBills(long custBillId, int noOfDays) {
		
		List<SppaBill> billList = new ArrayList<SppaBill>();		
		try {
			String hsql = " SELECT bill FROM SppaBill bill WHERE bill.customerBill.id = :custBillId AND bill.blockTermRemaining > :noOfDays " +
					" ORDER BY bill.id desc";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("custBillId", custBillId);
			q.setLong("noOfDays", noOfDays);
			billList = q.list();
			if (billList != null && !billList.isEmpty()) {
				return billList ;
			}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		return billList;
		
	} //end of method getLongTermRemainingSiteBills
	
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
				logger.info("baseline energy : "+ bill.getBaselineEnergy() +"\n base_cost "+bill.getBaseCost() + "\n consumed_energy"+bill.getConsumedEnergy() + "\n saved_cost "+ bill.getSavedCost());
				return bill;
			}
			if(bill==null)
			{
				logger.debug("getMonthlyBillData() returned null while fetching energyconsumption data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		logger.debug("Connection Failed to Established to database "+dbName);
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
			String hsql = "from CustomerSppaBill sppaBill where sppaBill.id = :billId";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("billId", billId);
			sppaBill = (CustomerSppaBill)q.uniqueResult(); 
			if (sppaBill != null) {
				return sppaBill;           
			}
    } catch (HibernateException hbe) {
    	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
    }
    return null;
    
	} //end of method loadCustomerSppaBillId
	
	public List<CustomerSppaBill> getLastCustomerBill(long customerId) {
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
		
	} //end of method getLastCustomerBill
	
	private static String baselineAnomalyQuery = "SELECT ballast_name, baseline_load from ballasts where (baseline_load is null or " +
			" baseline_load < 30) AND id IN (SELECT DISTINCT ballast_id FROM fixture WHERE state = 'COMMISSIONED')";
	
	public Map<String, Object> validateSiteEm(String dbName, String replicaIp, Date fromDate, Date toDate) {
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		HashMap<String, Object> siteAnomalyMap = new HashMap<String, Object>();
		try {
			connection = getDbConnection(dbName, replicaIp);
			stmt = connection.createStatement();
			
			// baseline anomaly
			rs = stmt.executeQuery(baselineAnomalyQuery);
			StringBuffer sb = null;
			while(rs.next()) {
				if(sb == null) {
					sb = new StringBuffer();
				} else {
					sb.append(", ");
				}
				sb.append(rs.getString("ballast_name"));
				sb.append("(");
				sb.append(rs.getDouble("baseline_load"));
				sb.append(")");				
			}
			if(sb != null) {
				siteAnomalyMap.put(SiteAnomalyType.BaselineLoad.getName(), sb.toString());
			}
			
			// burn hours anomaly
			HashMap<String, Long> burnHourMap = new HashMap<String, Long>();
			String query = "SELECT no_of_rows, no_of_sensors FROM (SELECT count(*) AS no_of_rows FROM energy_consumption WHERE capture_at > " +
					"to_date('" + DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "', 'yyyy-MM-dd HH24:mi:ss') -INTERVAL '30 days' AND " +
					" capture_at <= '" + DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "' AND zero_bucket != 1) AS sq1, (SELECT count(*) " +
					" AS no_of_sensors FROM fixture WHERE state = 'COMMISSIONED') AS sq2";
			System.out.println("query --- " + query);
			rs = stmt.executeQuery(query);
			if(rs.next()) {
				burnHourMap.put("noOfEcRows", rs.getLong(1));
				burnHourMap.put("noOfSensors", rs.getLong(2));				
			}
			siteAnomalyMap.put(SiteAnomalyType.BurnHour.getName(), burnHourMap);
			
			//profile anomaly
			query = "SELECT avg_volts, count(DISTINCT fixture_id) FROM energy_consumption WHERE capture_at > '" + 
					DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss") + "' AND capture_at <= '" + 
					DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "' AND avg_volts > 50 AND min_volts = avg_volts AND avg_volts = max_volts" +
					" GROUP BY avg_volts";
			rs=stmt.executeQuery(query);
			sb = null;
			while(rs.next()) {
				if(sb == null) {
					sb = new StringBuffer();
				} else {
					sb.append(", ");
				}
				sb.append(rs.getInt("avg_volts"));
				sb.append("(");
				sb.append(rs.getLong(2));
				sb.append(")");				
			}
			if(sb != null) {
				siteAnomalyMap.put(SiteAnomalyType.Profile.getName(), sb.toString());
			}
			
			//consumption anomaly			
			query = "SELECT count(DISTINCT fixture_id) FROM energy_consumption ec WHERE capture_at> '" +
					DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss") + "' AND capture_at <= '" + 
					DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "' AND power_used > (SELECT b.wattage * lamp_num * ballast_factor * " +
					"no_of_fixtures FROM ballasts b, fixture f WHERE f.id = fixture_id and f.ballast_id = b.id)";
					
			rs=stmt.executeQuery(query);
			if(rs.next()) {
				if(rs.getInt(1) > 0) {
					siteAnomalyMap.put(SiteAnomalyType.Consumption.getName(), rs.getInt(1));
				}				
			}
			
			String connectivityStr = null;
			//connectivity anomaly
			//query = "SELECT count(*) FROM fixture WHERE state = 'COMMISSIONED' AND last_stats_rcvd_time < now() - INTERVAL '1 day'";
			
			query = "SELECT no_of_days, no_of_sensors FROM (SELECT (now()::date - last_stats_rcvd_time::date) AS no_of_days, count(*) AS " +
					"no_of_sensors FROM fixture WHERE state = 'COMMISSIONED' GROUP BY no_of_days HAVING count(*) > 0) AS sq WHERE no_of_days > 1 " +
					"ORDER BY no_of_days";
			
			rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				if(connectivityStr != null) {
					connectivityStr += ", ";
				} else {
					connectivityStr = "";
				}
				connectivityStr +=  rs.getInt("no_of_days") + "(" + rs.getInt("no_of_sensors") + ")";				
			}
			if(connectivityStr != null) {
				connectivityStr = "SU[" + connectivityStr + "]";
			}
			
			query =  "SELECT count(*) FROM gateway WHERE commissioned = true AND last_connectivity_at < now() - INTERVAL '1 day'";
			rs = stmt.executeQuery(query);
			if(rs.next()) {
				if(rs.getInt(1) > 0) {
					if(connectivityStr != null) {
						connectivityStr += ", ";
					} else {
						connectivityStr = "";
					}
					connectivityStr += "GW[" + rs.getInt(1) + "]";					
				}
			}
			if(connectivityStr != null) {
				siteAnomalyMap.put(SiteAnomalyType.Connectivity.getName(), connectivityStr);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return siteAnomalyMap;
		
	} //end of method validateSiteEm
		
} //end of class SppaDao
