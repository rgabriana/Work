package com.ems.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.BallastVoltPower;
import com.ems.utils.ArgumentUtils;

@Repository("ballastVoltPowerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastVoltPowerDao extends BaseDaoHibernate{
	
	
	@SuppressWarnings("unchecked")
	public List<BallastVoltPower> getAllBallastVoltPowersFromId(
			Long ballastId,Double inputVolt) {
		List<BallastVoltPower> ballastVoltPowers = new ArrayList<BallastVoltPower>();
		ballastVoltPowers = getSession().createCriteria(BallastVoltPower.class)
							.add(Restrictions.eq("ballastId", ballastId))
							.add(Restrictions.eq("inputVolt",inputVolt))
							.addOrder(Order.asc("volt"))
							.list();
		return ballastVoltPowers;
	}

	public void add(Long ballastId, Double voltage, Double volt, Double power,Long voltPowerMapId,Boolean enableFlag) {
		BallastVoltPower ballastVoltPower = new BallastVoltPower();
		ballastVoltPower.setVoltPowerMapId(voltPowerMapId);
		ballastVoltPower.setVolt(volt);
		ballastVoltPower.setInputVolt(voltage);
		ballastVoltPower.setPower(power);
		ballastVoltPower.setBallastId(ballastId);
		ballastVoltPower.setEnabled(enableFlag);
		//getSession().saveOrUpdate(ballastVoltPower);
		saveObject(ballastVoltPower);
	}

	public void updatePower(Long ballastId, Double voltage, Double volt, Double power) {
		try {
            String hql = "update BallastVoltPower set " + "power" + " = :newValue where ballastId = :ballastId and inputVolt =:inputVolt and volt =:volt";
            Query query = getSession().createQuery(hql);
            query.setLong("ballastId", ballastId);
            query.setDouble("newValue", power);
            query.setDouble("inputVolt", voltage);
            query.setDouble("volt", volt);
            query.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateEnable(Long ballastId, Double voltage, Double volt, Boolean enableFlag) {
		try {
            String hql = "update BallastVoltPower set " + "enabled" + " = :newValue where ballastId = :ballastId and inputVolt =:inputVolt and volt =:volt";
            Query query = getSession().createQuery(hql);
            query.setLong("ballastId", ballastId);
            query.setBoolean("newValue", enableFlag);
            query.setDouble("inputVolt", voltage);
            query.setDouble("volt", volt);
            query.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<Long> getAllBallastVoltPowers() {
		
		List<Long> ballastVoltPowers = new ArrayList<Long>();		
		ballastVoltPowers = getSession().createCriteria(BallastVoltPower.class)		
		.setProjection(Projections.distinct(Projections.property("ballastId")))
		.list();			
		return ballastVoltPowers;
	}
	
	@SuppressWarnings("unchecked")
	public List<Double> getVoltageLevelsByBallastId(Long ballastId)
	{		
		List<Double> mVoltageLevels = new ArrayList<Double>();
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property(""));
		mVoltageLevels = getSession().createCriteria(BallastVoltPower.class,"ballastvoltpower")
													.setProjection(Projections.distinct(Projections.property("ballastvoltpower.inputVolt")))
													.add(Restrictions.eq("ballastvoltpower.ballastId", ballastId))													
													.list();		
		return mVoltageLevels;		
	}
	
	public void updateBallastVoltPowerMap(List<BallastVoltPower> ballastVoltPowerMap) {
		for(BallastVoltPower bvp: ballastVoltPowerMap) {
			Session session = getSession();
			BallastVoltPower ballastVoltPower = (BallastVoltPower) session.createCriteria(BallastVoltPower.class)
					.add(Restrictions.eq("voltPowerMapId", bvp.getVoltPowerMapId()))
					.add(Restrictions.eq("volt", bvp.getVolt()))
					.uniqueResult();
			if(bvp.getEnabled() != null && !ballastVoltPower.getEnabled().equals(bvp.getEnabled())) {
				ballastVoltPower.setEnabled(bvp.getEnabled());
				session.saveOrUpdate(ballastVoltPower);
			}
		}
	}
	
	public void deleteBallastCurve(Long ballastId,Double inputVoltage) {
		String hsql = "delete from BallastVoltPower where ballastId=? and inputVolt=?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, ballastId);
        q.setParameter(1, inputVoltage);
		q.executeUpdate();
	}
	
	
	public long getMaxVoltPowerMapId() {
	  long maxVoltPwrId = 2;
	  try {
	    Session s = getSession();
	    String hsql = "SELECT MAX(voltPowerMapId) from BallastVoltPower";
	    Query query = s.createQuery(hsql);
	    maxVoltPwrId =  (Long)query.uniqueResult();
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return maxVoltPwrId;
	}
	
	
	@SuppressWarnings("unchecked")
	public void addVoltPowerMap(long voltMapId, HashMap<Double, Double> curveMap) {

		Query query = getSession()
				.createQuery(
						"Update BallastVoltPower set power = :power"
								+ " where volt = :volt and voltPowerMapId= :voltPowerMapId");

		Session session = getSession();
		List<BallastVoltPower> curveList = session
				.createCriteria(BallastVoltPower.class)
				.add(Restrictions.eq("voltPowerMapId", voltMapId)).list();
		Iterator<BallastVoltPower> curveValIter = curveList.iterator();
		BallastVoltPower bVoltPwr = null;
		ArrayList<Double> existingList = new ArrayList<Double>();
		while (curveValIter.hasNext()) {
			bVoltPwr = curveValIter.next();
			bVoltPwr.setPower(curveMap.get(bVoltPwr.getVolt()));
			query.setLong("voltPowerMapId", voltMapId);
			query.setDouble("volt", bVoltPwr.getVolt());
			query.setDouble("power", bVoltPwr.getPower());
			query.executeUpdate();
			existingList.add(bVoltPwr.getVolt());
		}

		Iterator<Double> iter = curveMap.keySet().iterator();
		Double volt = null;
		while (iter.hasNext()) {
			volt = iter.next();
			if (existingList.contains(volt)) {
				continue;
			}
			bVoltPwr = new BallastVoltPower();
			bVoltPwr.setBallastId(0L);
			bVoltPwr.setPower(curveMap.get(volt));
			bVoltPwr.setVolt(volt);
			bVoltPwr.setVoltPowerMapId(voltMapId);
			session.save(bVoltPwr);
		}
		existingList = null;
		curveList = null;

	} // end of method addVoltPowerMap
	
	@SuppressWarnings("unchecked")
	public List<BallastVoltPower> getBallastVoltPowerByVoltPowerMapId(Long voltPowerMapId) {
		List<BallastVoltPower> ballastVoltPowers = null;
		ballastVoltPowers = getSession()
				.createCriteria(BallastVoltPower.class)
				.add(Restrictions.eq("voltPowerMapId", voltPowerMapId))
				.list();
		if (ArgumentUtils.isNullOrEmpty(ballastVoltPowers)) {
			ballastVoltPowers = getSession()
					.createCriteria(BallastVoltPower.class)
					.add(Restrictions.eq("voltPowerMapId", 1L)).list();
		}
		return ballastVoltPowers;
	}
}
