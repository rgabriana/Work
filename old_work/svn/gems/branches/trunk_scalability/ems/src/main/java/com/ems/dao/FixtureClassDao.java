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
import com.ems.service.FixtureManager;
import com.ems.utils.ArgumentUtils;

@Repository("fixtureClassDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureClassDao extends BaseDaoHibernate{
	
	
	@Resource(name = "bulbManager")
    private BulbManager bulbManager;
	
	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	
	@Resource
	private FixtureManager fixtureManager;
	
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
		
		List<Fixture> fixtureList = fixtureManager.getFixtureListByFixtureClassId(Long.parseLong(id));
		
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)){
			fixtureManager.updateFixtureClassChanges(Long.parseLong(id), Long.parseLong(ballastId), Long.parseLong(bulbId),
					Integer.parseInt(noOfBallasts), Short.parseShort(voltage), lampNum);
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
	
	@SuppressWarnings("unchecked")
	public Integer getFixtureClassCountByBulbId(Long id) {
		// TODO Auto-generated method stub
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("bulb.id", id)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.size();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
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
	public FixtureClass getFixtureClassByName(String fixtureType) {
		// TODO Auto-generated method stub
		List<FixtureClass> fixtureClassList = getSession().createCriteria(FixtureClass.class)
        .add(Restrictions.eq("name", fixtureType)).list();
		if(fixtureClassList.size() > 0) {
			return fixtureClassList.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getCommissionedFxTypeCount()
    {
        List<Object[]> commiFxTypeList = null;
        String hql = "select count(f.id), f.fixture_class_id,cast(fc.name as varchar),cast(b.display_label as varchar) from fixture f join fixture_class fc on f.fixture_class_id=fc.id  join ballasts b on fc.ballast_id =b.id " +
        		"where f.state=:state group by f.fixture_class_id, cast(fc.name as varchar), cast(b.display_label as varchar) order by f.fixture_class_id;";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        commiFxTypeList = q.list();
        return  commiFxTypeList;
    }
}