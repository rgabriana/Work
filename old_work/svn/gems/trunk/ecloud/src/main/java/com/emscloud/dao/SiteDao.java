package com.emscloud.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.EmSite;
import com.emscloud.model.Site;
import com.emscloud.model.SiteAnomaly;
import com.emscloud.model.SiteAnomalyList;
import com.emscloud.vo.AggregatedSiteReport;
import com.emscloud.vo.SiteList;
import com.emscloud.vo.SiteReportVo;

@Repository("siteDao")
@Transactional(propagation = Propagation.REQUIRED)
public class SiteDao {
	
	static final Logger logger = Logger.getLogger(SiteDao.class.getName());

	
	@Resource 
	SessionFactory sessionFactory;	

	@SuppressWarnings("unchecked")
	public List<Site> loadAllSites() {
		
		try {
			List<Site> siteList = sessionFactory.getCurrentSession().createCriteria(Site.class).addOrder(Order.asc("name")).list();
			if (siteList != null && !siteList.isEmpty()) {
				return siteList;
	 		} else {
	 			return null;
	 		}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadAllSites

	@SuppressWarnings("unchecked")
	public List<Site> loadSitesByCustomer(long id) {
		
		try {
			List<Site> siteList = sessionFactory.getCurrentSession().createCriteria(Site.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
			if (siteList != null && !siteList.isEmpty()) {
	 			return siteList;
	 		} else {
	 			return null;
	 		}
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadSitesByCustomer
	
	public List<Object[]> loadSiteFacilitiesByCustomer(long id) {
		
		String sql = "SELECT s.id AS siteId, s.name, geo_location, region, f.square_foot, f.id AS facilityId FROM site s, facility f WHERE s.name = f.name" +
				" AND type = 2 AND s.customer_id="+id;
		try {
			Query q = sessionFactory.getCurrentSession().createSQLQuery(sql);
      List<Object[]> siteList = q.list();
      return  siteList;			
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadSiteFacilitiesByCustomer
	
	public Site getSiteByGeoLocation(String geoLoc) {
		
		try {
			Site site = (Site)sessionFactory.getCurrentSession().createCriteria(Site.class).
					add(Restrictions.eq("geoLocation", geoLoc)).uniqueResult();
			return site;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method getsiteByGeoLocation
	
	public Site loadSiteById(long id) {
		
		try {
			Site site = (Site)sessionFactory.getCurrentSession().createCriteria(Site.class).add(Restrictions.eq("id", id)).uniqueResult();
			return site;
		}
		catch (HibernateException hbe) {
			throw SessionFactoryUtils.convertHibernateAccessException(hbe);
		}
		
	} //end of method loadSiteById
	
	public void saveOrUpdate(Site site) {
		sessionFactory.getCurrentSession().saveOrUpdate(site) ;
	} //end of method saveOrUpdate
	
	public List<Long> getSiteEms(long siteId) {
		
		List<Long> emList = new ArrayList<Long>();
		try {
			String hsql = "Select emId from EmSite site where site.siteId = :siteId";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.setLong("siteId", siteId);
			emList = q.list();			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return emList;
		
	} //end of method getSiteEms

	@SuppressWarnings("unchecked")
    public AggregatedSiteReport loadSitesByCustomerWithSpecificAttibute(Long id,String orderby,
            String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
	    AggregatedSiteReport aggregatedSiteReport = new AggregatedSiteReport();
        Criteria oCriteria = null;
        Criteria oRowCount = null;

        oRowCount = sessionFactory.getCurrentSession()
                .createCriteria(Site.class, "site")
                .setProjection(Projections.rowCount());
        oCriteria = sessionFactory.getCurrentSession().createCriteria(
                Site.class, "site");

        oRowCount.add(Restrictions.eq("site.customer.id", id));
        oCriteria.add(Restrictions.eq("site.customer.id", id));
        
        if (orderby != null && !"".equals(orderby)) {
            if (orderby.equals("name")) {
                orderby = "site.name";
            }else if (orderby.equals("geoLocation")) {
                orderby = "site.geoLocation";
            }else 
            {
                orderby = "site.id";
            }
            if ("desc".equals(orderway)){
                oCriteria.addOrder(Order.desc(orderby));
            }else{
                oCriteria.addOrder(Order.asc(orderby));
            }
            
        } else {
            if ("desc".equals(orderway)){
                oCriteria.addOrder(Order.desc("id"));
            }else{
                oCriteria.addOrder(Order.asc("id"));
            }
        }
        
        if (bSearch) {
            if (searchField.equals("name")) {
                oRowCount.add(Restrictions.ilike("site.name", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.name", "%"
                        + searchString + "%"));
            }else if (searchField.equals("geoLocation")) {
                oRowCount.add(Restrictions.ilike("site.geoLocation", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.geoLocation", "%"
                        + searchString + "%"));
            }
        }
        ArrayList<SiteReportVo> dataList = new ArrayList<SiteReportVo>();
        if (limit > 0) {
            oCriteria.setMaxResults(limit).setFirstResult(offset);
        }
        List<Object> output = (List<Object>) oRowCount.list();
        Long count = (Long) output.get(0);
        if (count.compareTo(new Long("0")) > 0) {
            aggregatedSiteReport.setTotal(count);
            List<Site> siteList = oCriteria.list();
            Iterator<Site> siteItr = siteList.iterator();
            while(siteItr.hasNext()) {
                Site siteObj =  siteItr.next();
                SiteReportVo data = new SiteReportVo();
                data.setId(siteObj.getId());
                data.setCustomer(siteObj.getCustomer().getName());
                data.setName(siteObj.getName());
                data.setGeoLocation(siteObj.getGeoLocation());
                dataList.add(data);
            }
            aggregatedSiteReport.setSiteReport(dataList);
            return aggregatedSiteReport;
        }
        return aggregatedSiteReport;
    } //end of method loadSitesByCustomerWithSpeciicAttibute
	
	@SuppressWarnings("unchecked")
    public SiteList getSitesByCustomerWithSpecificAttibute(Long id,String orderby,
            String orderway, Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
        SiteList siteList = new SiteList();
        Criteria oCriteria = null;
        Criteria oRowCount = null;

        oRowCount = sessionFactory.getCurrentSession()
                .createCriteria(Site.class, "site")
                .setProjection(Projections.rowCount());
        oCriteria = sessionFactory.getCurrentSession().createCriteria(
                Site.class, "site");

        oRowCount.add(Restrictions.eq("site.customer.id", id));
        oCriteria.add(Restrictions.eq("site.customer.id", id));
        
        if (orderby != null && !"".equals(orderby)) {
            if (orderby.equals("name")) {
                orderby = "site.name";
            }else if (orderby.equals("geoLocation")) {
                orderby = "site.geoLocation";
            } else if (orderby.equals("region")) {
                orderby = "site.region";
            } else if (orderby.equals("poNumber")) {
                orderby = "site.poNumber";
            }else 
            {
                orderby = "site.id";
            }
            if ("desc".equals(orderway)){
                oCriteria.addOrder(Order.desc(orderby));
            }else{
                oCriteria.addOrder(Order.asc(orderby));
            }
            
        } else {
            if ("desc".equals(orderway)){
                oCriteria.addOrder(Order.desc("id"));
            }else{
                oCriteria.addOrder(Order.asc("id"));
            }
        }
        
        if (bSearch) {
            if (searchField.equals("name")) {
                oRowCount.add(Restrictions.ilike("site.name", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.name", "%"
                        + searchString + "%"));
            }else if (searchField.equals("geoLocation")) {
                oRowCount.add(Restrictions.ilike("site.geoLocation", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.geoLocation", "%"
                        + searchString + "%"));
            }else if (searchField.equals("region")) {
                oRowCount.add(Restrictions.ilike("site.region", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.region", "%"
                        + searchString + "%"));
            }else if (searchField.equals("poNumber")) {
                oRowCount.add(Restrictions.ilike("site.poNumber", "%"
                        + searchString + "%"));
                oCriteria.add(Restrictions.ilike("site.poNumber", "%"
                        + searchString + "%"));
            }
        }
        if (limit > 0) {
            oCriteria.setMaxResults(limit).setFirstResult(offset);
        }
        List<Object> output = (List<Object>) oRowCount.list();
        Long count = (Long) output.get(0);
        if (count.compareTo(new Long("0")) > 0) {
            siteList.setTotal(count);
            List<Site> siteListArr = oCriteria.list();
            siteList.setSitesList(siteListArr);
            return siteList;
        }
        return siteList;
    } //end of method getSitesByCustomerWithSpecificAttibute

    public int deleteSite(Long siteId) {
        Site siteObj = loadSiteById(siteId);
        int status=0;
        try
        {
            Session session = sessionFactory.getCurrentSession();
            session.delete(siteObj);
        }catch(Exception e)
        {
            status=-1;
        }
        return status;
    }
    public List<Object[]> getUnmappedEmInstList(Long customerId) {
        List<Object[]> emList = new ArrayList<Object[]>();
        try {
            String hsql = "Select ei.id,ei.name,ei.macId from EmInstance ei where ei.id not in (select es.emId from EmSite es) and ei.customer.id=:customerId";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setLong("customerId", customerId);
            emList = q.list();          
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return emList;
    }

    public int assignEmToSite(EmSite emSite) {
        int status=0;
        Session session = sessionFactory.getCurrentSession();
        try {
           session.saveOrUpdate(emSite) ;
        }
        catch(Exception e) {
            status=-1;
        }
        return status;        
    }
    public EmSite loadEMSiteByEmIdAndSiteId(long emId,long siteId) {
        
        try {
            EmSite emsite = (EmSite)sessionFactory.getCurrentSession().createCriteria(EmSite.class)
                    .add(Restrictions.eq("siteId", siteId))
                    .add(Restrictions.eq("emId", emId)).uniqueResult();
            return emsite;
        }
        catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        
    } //end of method loadSiteById
    public int unAssignEmToSite(EmSite emSite) {
        int status=0;
        Session session = sessionFactory.getCurrentSession();
        try {
            session.delete(emSite);
        }
        catch(Exception e) {
            status=-1;
        }
        return status;  
    }
    
    public Site getSiteByName(String siteName) {
    	try {
            List<Site> results = null;
            String hsql = "from Site s where upper(s.name)=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, siteName.toUpperCase());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	Site site = (Site) results.get(0);
                return site;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
	} //end of method getsiteByGeoLocation
    
  public void addSiteAnomaly(SiteAnomaly anomaly) {
  	
  	sessionFactory.getCurrentSession().saveOrUpdate(anomaly);
  	
  } //end of method addSiteAnomaly

	public SiteAnomalyList getAllAnamoliesListBySiteId(List<String> geoLocList,Date startDate,Date endDate,
			String orderby, String orderway, Boolean bSearch,
			String searchField, String searchString, String searchOper, int offset,
			int limit) {
		 SiteAnomalyList  siteAnomalyList= new SiteAnomalyList();
	        Criteria oCriteria = null;
	        Criteria oRowCount = null;

	        oRowCount = sessionFactory.getCurrentSession()
	                .createCriteria(SiteAnomaly.class, "siteanomaly")
	                .setProjection(Projections.rowCount());
	        oCriteria = sessionFactory.getCurrentSession().createCriteria(
	        		SiteAnomaly.class, "siteanomaly");	
	        
	        if(geoLocList!=null && geoLocList.size()>0)
	        {
		        oRowCount.add(Restrictions.in("siteanomaly.geoLocation", geoLocList));
		        oCriteria.add(Restrictions.in("siteanomaly.geoLocation", geoLocList));
	        }else
	        {
	        	return siteAnomalyList;
	        }
	        
    		if(startDate != null) {
    			Calendar c = Calendar.getInstance(); 
        		c.setTime(startDate); 
        		c.set(Calendar.HOUR, 0);
        		c.set(Calendar.MINUTE, 0);
        		c.set(Calendar.SECOND, 0);
        		c.set(Calendar.MILLISECOND, 0);
        		Date startDate1 = c.getTime();
    			oRowCount.add(Restrictions.ge("siteanomaly.startDate", startDate1));
    			oCriteria.add(Restrictions.ge("siteanomaly.startDate", startDate1));
    		}
    		if(endDate != null) {
    			Calendar c = Calendar.getInstance(); 
        		c.setTime(endDate); 
        		c.set(Calendar.HOUR, 0);
        		c.set(Calendar.MINUTE, 0);
        		c.set(Calendar.SECOND, 0);
        		c.set(Calendar.MILLISECOND, 0);
        		c.add(Calendar.DATE, 1);
        		Date endDate1 = c.getTime();
    			oCriteria.add(Restrictions.lt("siteanomaly.endDate", endDate1));
    			oRowCount.add(Restrictions.lt("siteanomaly.endDate", endDate1));
    		}
	        if (orderby != null && !"".equals(orderby)) {
	           if (orderby.equals("geoLocation")) {
	                orderby = "siteanomaly.geoLocation";
	            }
	            else if (orderby.equals("reportDate")) {
	                orderby = "siteanomaly.reportDate";
	            }
	            else if (orderby.equals("startDate")) {
	                orderby = "siteanomaly.startDate";
	            }
	            else if (orderby.equals("endDate")) {
	                orderby = "siteanomaly.endDate";
	            }
	            else if (orderby.equals("issue")) {
	                orderby = "siteanomaly.issue";
	            }else 
	            {
	                orderby = "siteanomaly.id";
	            }
	            if ("desc".equals(orderway)){
	                oCriteria.addOrder(Order.desc(orderby));
	            }else{
	                oCriteria.addOrder(Order.asc(orderby));
	            }
	            
	        } else {
	            if ("desc".equals(orderway)){
	                oCriteria.addOrder(Order.desc("id"));
	            }else{
	                oCriteria.addOrder(Order.asc("id"));
	            }
	        }
	        
	        if (bSearch) {
	          if (searchField.equals("geoLocation")) {
	                oRowCount.add(Restrictions.ilike("siteanomaly.geoLocation", "%"
	                        + searchString + "%"));
	                oCriteria.add(Restrictions.ilike("siteanomaly.geoLocation", "%"
	                        + searchString + "%"));
	            }else if (searchField.equals("issue")) {
	                oRowCount.add(Restrictions.ilike("siteanomaly.issue", "%" + searchString +"%"));
					oCriteria.add(Restrictions.ilike("siteanomaly.issue", "%" +searchString +"%"));
	            }else if (searchField.equals("details")) {
	                oRowCount.add(Restrictions.ilike("siteanomaly.details", "%" + searchString +"%"));
					oCriteria.add(Restrictions.ilike("siteanomaly.details", "%" +searchString +"%"));
	            }
	        }
	        
	        if (limit > 0) {
	            oCriteria.setMaxResults(limit).setFirstResult(offset);
	        }
	        List<Object> output = (List<Object>) oRowCount.list();
	        Long count = (Long) output.get(0);
	        if (count.compareTo(new Long("0")) > 0) {
	        	siteAnomalyList.setTotal(count);
	            List<SiteAnomaly> siteListArr = oCriteria.list();
	            siteAnomalyList.setSiteAnomaly(siteListArr);
	            return siteAnomalyList;
	        }
	        return siteAnomalyList;
	}
} //end of class SiteDao