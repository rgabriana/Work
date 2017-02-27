package com.ems.dao;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Ballast;
import com.ems.model.BallastList;

@Repository("ballastDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastDao extends BaseDaoHibernate{
	
	public void addBallast(Ballast ballast) {
				
		String bulbStr = "bulbs";
		if(ballast.getLampNum() == 1) {
			bulbStr = "bulb";
		}
		
		String displayLabel = ballast.getDisplayLabel().trim();
		
		if(displayLabel == null || "".equals(displayLabel)){
			displayLabel = ballast.getBallastName() + "(" + ballast.getBallastManufacturer() + "," + ballast.getLampType() + "," + ballast.getWattage() + "W," + ballast.getLampNum() + " " + bulbStr + ")";
		}
		
		ballast.setDisplayLabel(displayLabel);
		
		ballast.setVoltPowerMapId(new Long(1)); // default value
		
		saveObject(ballast);
	}
	
	public void editBallast(Ballast ballast) {
		
		String bulbStr = "bulbs";
		if(ballast.getLampNum() == 1) {
			bulbStr = "bulb";
		}
		
		String displayLabel = ballast.getDisplayLabel().trim();
		
		if(displayLabel == null || "".equals(displayLabel)){
			displayLabel = ballast.getBallastName() + "(" + ballast.getBallastManufacturer() + "," + ballast.getLampType() + "," + ballast.getWattage() + "W," + ballast.getLampNum() + " " + bulbStr + ")";
		}
		
		ballast.setDisplayLabel(displayLabel);
		saveObject(ballast);
		
	}
	
	
	@SuppressWarnings("unchecked")
	public BallastList loadBallastList(String orderway, int offset, int limit) {
		BallastList ballastList = new BallastList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(Ballast.class, "ballast").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(Ballast.class, "ballast");
	
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("ballast.ballastName"));
		} 
		else {
			oCriteria.addOrder(Order.asc("ballast.ballastName"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			ballastList.setTotal(count);
			ballastList.setBallasts(oCriteria.list());
			return ballastList;
		}
		
		return ballastList;	
		
	}
	
	public void deleteBallastById(Long id)
	{
		String hsql = "delete from Ballast where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public Ballast getBallastById(Long id) {
        List<Ballast> ballastList = getSession().createCriteria(Ballast.class)
                .add(Restrictions.eq("id", id)).list();
        if(ballastList.size() > 0) {
        	return ballastList.get(0);
        }
        
        return new Ballast();
    }
	
}
