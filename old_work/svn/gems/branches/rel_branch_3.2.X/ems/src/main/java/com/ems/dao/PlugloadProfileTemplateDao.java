package com.ems.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.PlugloadProfileTemplate;
import com.ems.utils.ArgumentUtils;

@Repository("plugloadProfileTemplateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadProfileTemplateDao extends BaseDaoHibernate {
	
	/**
     * Load company's PlugloadProfileTemplate
     * 
     * @param id
     *            company id
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    @SuppressWarnings("unchecked")
    public List<PlugloadProfileTemplate> loadPlugloadTemplateByCompanyId(Long id) {
        try {
            List<PlugloadProfileTemplate> results = null;
            String hsql = "Select new PlugloadProfileTemplate(pt.id,pt.name) from PlugloadProfileTemplate pt where pt.company.id = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public int getPlugloadProfileTemplateCountByName(String name)
    {    	
    	try {
            List<PlugloadProfileTemplate> results = null;
            String hsql = "Select new PlugloadProfileTemplate(pt.id,pt.name) from PlugloadProfileTemplate pt where name = ?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results.size();
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return 0;	
    }
    
    @SuppressWarnings("unchecked")
	public PlugloadProfileTemplate getPlugloadProfileTemplateByName(String plugloadProfileTemplateName) {
        List<PlugloadProfileTemplate> plugloadProfileTemplateList = getSession()
                .createQuery("Select new PlugloadProfileTemplate(pt.id,pt.name) from PlugloadProfileTemplate pt where pt.name = :name")
                .setString("name", plugloadProfileTemplateName).list();
        if (!ArgumentUtils.isNullOrEmpty(plugloadProfileTemplateList)) {
            return plugloadProfileTemplateList.get(0);
        }
        return null;
    }
	
	@SuppressWarnings("unchecked")
	public PlugloadProfileTemplate getPlugloadProfileTemplateById(Long id) {
		 List<PlugloadProfileTemplate> plugloadProfileTemplateList = getSession().createQuery("Select new PlugloadProfileTemplate(pt.id,pt.name) from PlugloadProfileTemplate pt where id = :id")
         .setLong("id", id).list();
		 if (!ArgumentUtils.isNullOrEmpty(plugloadProfileTemplateList)) {
		     return plugloadProfileTemplateList.get(0);
		 }
		 return null;
    }
	
	/**
     * Load All PlugloadProfileTemplate
     * 
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    @SuppressWarnings("unchecked")
    public List<PlugloadProfileTemplate> loadAllPlugloadProfileTemplate() {
    	List<PlugloadProfileTemplate> results= null;
    	try {
    		results = getSession().createQuery("from PlugloadProfileTemplate order by id").list();
    		//Fill the Plugload Count for the Templates.
		   	List<Object> plugloadCountList =  getPlugloadCountForPlugloadProfileTemplate();
		    if (results != null && !results.isEmpty())
		    {
	              Iterator<PlugloadProfileTemplate> it = results.iterator();
	              while (it.hasNext()) {
	            	  PlugloadProfileTemplate rowResult = it.next();
	                  if(plugloadCountList!=null && !plugloadCountList.isEmpty())
	                  {
		                  for (Iterator<Object> plugloadInterator = plugloadCountList.iterator(); plugloadInterator.hasNext();)
							{
		                	  	Object[] plugloadobject = (Object[]) plugloadInterator.next();
				    			Long templateId = (Long) plugloadobject[0];
				    			if(rowResult.getId().compareTo(templateId) == 0) {
			    					rowResult.setPlugloadCount((Long) plugloadobject[1]);
			    					break;
			    				}
							}
	                  }
	              }
		    }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    	return results;
    }
    
    @SuppressWarnings("unchecked")
	public List<Object> getPlugloadCountForPlugloadProfileTemplate() {
		List<Object> plugloadCountForPlugloadProfileTemplate = new ArrayList<Object>();
		try {
			Session s = getSession();
			String hsql = "select plpt.id as plugloadProfileTemplateId,count(pg.id) as plugloadCount from Plugload pg,PlugloadGroups plg,PlugloadProfileTemplate plpt where pg.state!='DELETED' and pg.groupId=plg.id AND plg.plugloadProfileTemplate.id=plpt.id group by plpt.id";
			Query query = s.createQuery(hsql);
			plugloadCountForPlugloadProfileTemplate = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plugloadCountForPlugloadProfileTemplate;
	}
    
    /**
     * Load All Derived PlugloadProfileTemplate (Template no > 1)
     * 
     * @return com.ems.model.PlugloadProfileTemplate collection
     */
    @SuppressWarnings("unchecked")
    public List<PlugloadProfileTemplate> loadAllDerivedPlugloadProfileTemplate() {
        List<PlugloadProfileTemplate> results= null;
        try {
            results = getSession().createQuery("from PlugloadProfileTemplate where id >1 order by id").list();
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
    }
    
    public PlugloadProfileTemplate editName(PlugloadProfileTemplate plugloadProfileTemplate) {
        Session session = getSession();
        session.createQuery("Update PlugloadProfileTemplate set name = :name where id = :id").setString("name", plugloadProfileTemplate.getName())
                .setLong("id", plugloadProfileTemplate.getId()).executeUpdate();
        return getPlugloadProfileTemplateById(plugloadProfileTemplate.getId());
    }


}
