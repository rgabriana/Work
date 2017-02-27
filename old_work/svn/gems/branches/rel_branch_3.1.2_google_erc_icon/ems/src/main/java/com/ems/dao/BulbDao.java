package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Bulb;
import com.ems.model.BulbList;
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
        List<Bulb> ballastList = getSession().createCriteria(Bulb.class)
        							.addOrder(Order.asc("bulbName")).list();
        if (!ArgumentUtils.isNullOrEmpty(ballastList)) {
            return ballastList;
        } else {
            return null;
        }
    }
}
