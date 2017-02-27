package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Fixture;
import com.ems.model.PlacedFixture;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Repository("placedFixtureDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlacedFixtureDao extends BaseDaoHibernate {	
	
	@SuppressWarnings("unchecked")
	public PlacedFixture getPlacedFixtureById(Long id) {
        List<PlacedFixture> fxList = getSession().createCriteria(PlacedFixture.class)
                .add(Restrictions.eq("id", id)).list();
        if(fxList.size() > 0) {
        	return fxList.get(0);
        }
        else
        	return null;
    }
	
	public void deletePlacedFixtureById(Long id)
	{
		String hsql = "delete from PlacedFixture where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}



	public void addPlacedFixture(PlacedFixture fx) {
		// TODO Auto-generated method stub
		saveObject(fx);
	}

	public void editPlacedFixture(PlacedFixture fx) {
		// TODO Auto-generated method stub
		saveObject(fx);
	}

	public PlacedFixture getPlacedFixtureByMacAddr(String macAddr) {
		Session session = getSession();
		PlacedFixture fixture = (PlacedFixture) session.createCriteria(PlacedFixture.class)
				.add(Restrictions.eq("macAddress", macAddr)).uniqueResult();
		return fixture;
	} // end of method getFixtureByMacAddr

	@SuppressWarnings("unchecked")
	public List<PlacedFixture> getAllPlacedFixtures() {
        List<PlacedFixture> fxList = getSession().createCriteria(PlacedFixture.class)
        							.addOrder(Order.asc("name")).list();
        if (!ArgumentUtils.isNullOrEmpty(fxList)) {
            return fxList;
        } else {
            return null;
        }
    }

	@SuppressWarnings("unchecked")
	public List<PlacedFixture> loadPlacedFixturesByFloorId(Long id) {
		Session session = getSession();
		List<PlacedFixture> fixtureList = session
				.createCriteria(PlacedFixture.class)
				.add(Restrictions.eq("floorId", id)).list();
		if (!ArgumentUtils.isNullOrEmpty(fixtureList)) {
			return fixtureList;
		} else {
			return null;
		}
	}
}