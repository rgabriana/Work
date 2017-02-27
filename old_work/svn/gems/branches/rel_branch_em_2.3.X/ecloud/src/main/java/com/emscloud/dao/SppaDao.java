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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.SppaBill;
import com.emscloud.service.EmInstanceManager;
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
	
	public SppaBill saveOrUpdate(SppaBill instance) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(instance) ;
		return instance ;
		
	} //end of method saveOrUpdate
	
	public List<SppaBill> getLastMonthBillPerCustomer(long custId) {
//		
//		Session session = sessionFactory.getCurrentSession();
//		List<SppaBill> billList  = session
//				.createCriteria(SppaBill.class)
//				.createAlias("emInstance", "emInstance", Criteria.LEFT_JOIN)
//				.add(Restrictions.ne("state",
//						ServerConstants.FIXTURE_STATE_DELETED_STR))
//				.add(Restrictions.eq("groupId", id))
//				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		
		
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
	
} //end of class SppaDao
