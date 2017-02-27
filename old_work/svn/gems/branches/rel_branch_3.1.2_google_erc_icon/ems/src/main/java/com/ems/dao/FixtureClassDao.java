package com.ems.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Fixture;
import com.ems.model.FixtureClass;
import com.ems.model.FixtureClassList;
import com.ems.server.ServerConstants;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.utils.ArgumentUtils;

@Repository("fixtureClassDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureClassDao extends BaseDaoHibernate{
	
	
	@Resource(name = "bulbManager")
    private BulbManager bulbManager;
	
	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	
	@SuppressWarnings("unchecked")
	public FixtureClassList loadFixtureClassList(String orderway, int offset, int limit) {
		FixtureClassList fixtureClassList = new FixtureClassList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession()
					.createCriteria(FixtureClass.class, "fixtureclass")
					.createAlias("ballast", "bal",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("bal", FetchMode.JOIN)
					.createAlias("bulb", "bul",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("bul", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());
		
		oCriteria = sessionFactory.getCurrentSession()
					.createCriteria(FixtureClass.class, "fixtureclass")
					.createAlias("ballast", "bal",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("bal", FetchMode.JOIN)
					.createAlias("bulb", "bul",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("bul", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("fixtureclass.name"));
		} 
		else {
			oCriteria.addOrder(Order.asc("fixtureclass.name"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			fixtureClassList.setTotal(count);
			fixtureClassList.setFixtureclasses(oCriteria.list());
			return fixtureClassList;
		}
		
		return fixtureClassList;	
		
	}
    
	@SuppressWarnings("unchecked")
	public List<FixtureClass> loadAllFixtureClasses() {
    	
    	 List<FixtureClass> fixtureClassList = 	getSession()
    	 										.createCriteria(FixtureClass.class)
    	 									   	.createAlias("ballast", "bal",
														CriteriaSpecification.LEFT_JOIN)
												.setFetchMode("bal", FetchMode.JOIN)
												.createAlias("bulb", "bul",
														CriteriaSpecification.LEFT_JOIN)
												.setFetchMode("bul", FetchMode.JOIN)
												.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    	 									   	.addOrder(Order.asc("name")).list();
    	 if (!ArgumentUtils.isNullOrEmpty(fixtureClassList)) {
 			return fixtureClassList;
 		} else {
 			return null;
 		}
    }
    
	public FixtureClass addFixtureClass(String name,String noOfBallasts,String voltage ,String ballastId,String bulbId){
		FixtureClass fixtureClass = new FixtureClass();
		fixtureClass.setBallast(ballastManager.getBallastById(Long.parseLong(ballastId)));
		fixtureClass.setBulb(bulbManager.getBulbById(Long.parseLong(bulbId)));
		fixtureClass.setName(name);
		fixtureClass.setNoOfBallasts(Integer.parseInt(noOfBallasts));
		fixtureClass.setVoltage(Integer.parseInt(voltage));
		return (FixtureClass) saveObject(fixtureClass);
	}
	
	@SuppressWarnings("unchecked")
	public FixtureClass getFixtureClass(String name,String noOfBallasts,String voltage ,String ballastId,String bulbId){
		List<FixtureClass> fixtureClassList = new ArrayList<FixtureClass>();
		
		fixtureClassList = getSession().createCriteria(FixtureClass.class)
        			.add(Restrictions.eq("name", name)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureClassList)) {
            return fixtureClassList.get(0);
        } else {
        	fixtureClassList = getSession().createCriteria(FixtureClass.class)
			.add(Restrictions.eq("noOfBallasts", Integer.parseInt(noOfBallasts)))
			.add(Restrictions.eq("voltage", Integer.parseInt(voltage)))
			.add(Restrictions.eq("ballast.id", Long.parseLong(ballastId)))
			.add(Restrictions.eq("bulb.id", Long.parseLong(bulbId)))
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
        	if (!ArgumentUtils.isNullOrEmpty(fixtureClassList)) {
                return fixtureClassList.get(0);
            }else{
            	return null;
            }
        }
	}
	
	public void editFixtureClass(String id,String name,String noOfBallasts,String voltage ,String ballastId,String bulbId){
		
		Integer lampNum = ballastManager.getBallastById(Long.parseLong(ballastId)).getLampNum();
		
		List<Fixture> fixtureList = getFixtureListByFixtureClassId(Long.parseLong(id));
		
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)){
			Query query = getSession().createQuery(
	                "Update Fixture set noOfFixtures = :noOfFixtures , voltage = :voltage , noOfBulbs = :noOfBulbs, bulb = :bulbId , ballast = :ballastId where fixtureClassId = :fixtureClassId");
			query.setLong("fixtureClassId", Long.parseLong(id));
			query.setLong("ballastId", Long.parseLong(ballastId));
	        query.setLong("bulbId", Long.parseLong(bulbId));
	        query.setInteger("noOfFixtures", Integer.parseInt(noOfBallasts));
	        query.setInteger("voltage", Short.parseShort(voltage));
	        query.setInteger("noOfBulbs", lampNum);
	        query.executeUpdate();
		}
		
		FixtureClass fixtureClass = getFixtureClassById(Long.parseLong(id));
		fixtureClass.setBallast(ballastManager.getBallastById(Long.parseLong(ballastId)));
		fixtureClass.setBulb(bulbManager.getBulbById(Long.parseLong(bulbId)));
		fixtureClass.setName(name);
		fixtureClass.setNoOfBallasts(Integer.parseInt(noOfBallasts));
		fixtureClass.setVoltage(Integer.parseInt(voltage));
		saveObject(fixtureClass);
	}
	
	public void deleteFixtureClassById(Long id)
	{
		String hsql = "delete from FixtureClass where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public FixtureClass getFixtureClassById(Long Id){
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("id", Id)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.get(0);
		}
		
		return new FixtureClass(); 
	}
	
	public Integer getFixtureClassCountByBulbId(Long id) {
		// TODO Auto-generated method stub
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("bulb.id", id)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.size();
		}
		return 0;
	}

	public Integer getFixtureClassCountByBallastId(Long id) {
		// TODO Auto-generated method stub
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("ballast.id", id)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.size();
		}
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public Integer getFixtureCountByFixtureClassId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.eq("fixtureClassId", id)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList.size();
		} else {
			return Integer.valueOf(0);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Fixture> getFixtureListByFixtureClassId(Long id) {
		Session session = getSession();
		List<Fixture> fixtureList = session
				.createCriteria(Fixture.class)
				.add(Restrictions.ne("state",
						ServerConstants.FIXTURE_STATE_DELETED_STR))
				.add(Restrictions.eq("fixtureClassId", id)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
		
	}

	@SuppressWarnings("unchecked")
	public FixtureClass getFixtureClassByName(String fixtureType) {
		// TODO Auto-generated method stub
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("name", fixtureType)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.get(0);
		}
		return null;
	}

	public Long bulkFixtureTypessignToFixture(String fixtureIdsList,Long currentFixturetypeId) {
		int totalRecordUpdated = 0;
		try {
			if(fixtureIdsList!=null && fixtureIdsList.length()>0)
			{
				String hsql1 = "update Fixture set fixture_class_id=:currentfixturetypeid where id in ("+fixtureIdsList+")";
				Query query1 = getSession().createSQLQuery(hsql1);
				query1.setLong("currentfixturetypeid", currentFixturetypeId);
				totalRecordUpdated=  query1.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) totalRecordUpdated;
	}
}