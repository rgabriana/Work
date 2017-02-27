package com.ems.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeJobList;
import com.ems.utils.ArgumentUtils;


@Repository("imageUpgradeJobDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ImageUpgradeJobDao extends BaseDaoHibernate {
    
	@SuppressWarnings("unchecked")
	public List<ImageUpgradeDBJob> loadAllImageUpgradeJobs() {
		
		Session session = getSession();
		return session.createCriteria(ImageUpgradeDBJob.class).list();		
		
	}
	
	
	@SuppressWarnings("unchecked")
	public ImageUpgradeDBJob loadImageUpgradeJobById( Long Id) {
		
		Session session = getSession();
		List<ImageUpgradeDBJob> imageUpgradeDBJobList = session.createCriteria(ImageUpgradeDBJob.class).add(Restrictions.eq("id", Id)).list();
		
		if (!ArgumentUtils.isNullOrEmpty(imageUpgradeDBJobList)) {
	      return imageUpgradeDBJobList.get(0);
	    } else {
	      return null;
	    }
		
	}
	
	@SuppressWarnings("unchecked")
	public ImageUpgradeJobList loadImageUpgradeJobList(String orderby,String orderway, Boolean bSearch, String searchField, String searchString, int offset, int limit) {
		ImageUpgradeJobList imageUpgradeJobList = new ImageUpgradeJobList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession()
					.createCriteria(ImageUpgradeDBJob.class, "imageupgradedbjob")
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());
		
		oCriteria = sessionFactory.getCurrentSession()
					.createCriteria(ImageUpgradeDBJob.class, "imageupgradedbjob")
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		if (orderby != null && !"".equals(orderby)) {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("scheduledTime"));
			}else{
				oCriteria.addOrder(Order.asc("scheduledTime"));
			}
		}
		
		if (bSearch) {
			if (searchField.equals("jobName")) {
				oRowCount.add(Restrictions.ilike("imageupgradedbjob.jobName", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("imageupgradedbjob.jobName", "%"
						+ searchString + "%"));
			}else if (searchField.equals("deviceType")) {
				oRowCount.add(Restrictions.ilike("imageupgradedbjob.deviceType", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("imageupgradedbjob.deviceType", "%"
						+ searchString + "%"));
			}else if (searchField.equals("imageName")) {
				oRowCount.add(Restrictions.ilike("imageupgradedbjob.imageName", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("imageupgradedbjob.imageName", "%"
						+ searchString + "%"));
			}else if (searchField.equals("status")) {
				oRowCount.add(Restrictions.ilike("imageupgradedbjob.status", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.ilike("imageupgradedbjob.status", "%"
						+ searchString + "%"));
			}
		}
	
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			imageUpgradeJobList.setTotal(count);
			imageUpgradeJobList.setImageUpgradeJobs(oCriteria.list());
			return imageUpgradeJobList;
		}
		
		return imageUpgradeJobList;	
	}

	
} 