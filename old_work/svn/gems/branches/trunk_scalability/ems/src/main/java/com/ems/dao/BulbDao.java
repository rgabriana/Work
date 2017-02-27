package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
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

import com.ems.model.Bulb;
import com.ems.model.BulbList;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Repository("bulbDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BulbDao extends BaseDaoHibernate {	
	
	@SuppressWarnings("unchecked")
	public Bulb getBulbById(Long id) {
        List<Bulb> bulbList = getSession().createCriteria(Bulb.class)
                .add(Restrictions.eq("id", id)).list();
        if(bulbList.size() > 0) {
        	return bulbList.get(0);
        }
        
        return new Bulb();
    }
	
	public void deleteBulbById(Long id)
	{
		String hsql = "delete from Bulb where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public BulbList loadBulbList(String orderway, int offset, int limit) {
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		BulbList bulbList = new BulbList();
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(
				Bulb.class, "bulb").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				Bulb.class, "bulb");

		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("bulb.bulbName"));
		} else {
			oCriteria.addOrder(Order.asc("bulb.bulbName"));
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			bulbList.setRecords(count);
			int totalpages = (int) (Math.ceil(count / new Double(BulbList.DEFAULT_ROWS)));
			bulbList.setTotal(totalpages);
			bulbList.setBulbs(oCriteria.list());
			return bulbList;
		}
		return bulbList;
		}

	public Bulb addBulb(Bulb bulb) {
		// TODO Auto-generated method stub
		return(Bulb)saveObject(bulb);
	}

	public void editBulb(Bulb bulb) {
		// TODO Auto-generated method stub
		saveObject(bulb);
	}

	@SuppressWarnings("unchecked")
	public Bulb getBulbByName(String bulbName) {
		// TODO Auto-generated method stub
		List<Bulb> bulbList = getSession().createCriteria(Bulb.class)
        .add(Restrictions.eq("bulbName", bulbName)).list();
		if(bulbList.size() > 0) {
			return bulbList.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Bulb> getAllBulbs() {
        List<Bulb> bulbList = getSession().createCriteria(Bulb.class)
        							.addOrder(Order.asc("bulbName")).list();
        if (!ArgumentUtils.isNullOrEmpty(bulbList)) {
            return bulbList;
        } else {
            return null;
        }
    }
	@SuppressWarnings("unchecked")
	public List<Object[]> getBulbsCountByBulbName()
	{
	    List<Object[]> bulbList = null;
	    //Old Query returning counts for each ballast
	    //String hql = "select cast(b.bulb_name as varchar), cast(manufacturer as varchar),count(b.id) from bulbs b where cast(manufacturer as varchar) in (select distinct manufacturer from bulbs) group by cast(manufacturer as varchar), cast(b.bulb_name as varchar) order by cast(manufacturer as varchar)";
	    String hql ="select cast(bb.bulb_name as varchar),cast(bb.manufacturer as varchar), sum(f.no_of_bulbs * f.no_of_fixtures) as bulbCount, count(f.id) as fxcount from fixture f join bulbs bb on bb.id=f.bulb_id where f.state=:state group by cast(bb.bulb_name as varchar),cast(bb.manufacturer as varchar) order by cast(bb.manufacturer as varchar)";
	    Session s = getSession();
	    Query q = s.createSQLQuery(hql);
	    q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
	    bulbList = q.list();
	    List<Object[]> result = new ArrayList<Object[]>();
        if (bulbList != null && !bulbList.isEmpty()) {
            Iterator<Object[]> iterator = bulbList.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Object[] bulbObj = new Object[4];
                bulbObj[0] =  (String) itrObject[0];
                bulbObj[1] =  (String) itrObject[1];
                bulbObj[2] = ((BigInteger) itrObject[2]).longValue();
                bulbObj[3] = ((BigInteger) itrObject[3]).longValue();
                result.add(bulbObj);
            }
        }
        return bulbList;
	}
}
