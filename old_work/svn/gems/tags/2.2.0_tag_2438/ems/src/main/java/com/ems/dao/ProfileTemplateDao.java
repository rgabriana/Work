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

import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author Sharad k Mahajan
 * 
 */
@Repository("profileTemplateDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileTemplateDao extends BaseDaoHibernate {

    /**
     * Load company's template
     * 
     * @param id
     *            company id
     * @return com.ems.model.ProfileTemplate collection
     */
    @SuppressWarnings("unchecked")
    public List<ProfileTemplate> loadTemplateByCompanyId(Long id) {
        try {
            List<ProfileTemplate> results = null;
            String hsql = "Select new ProfileTemplate(pt.id,pt.name) from ProfileTemplate pt where pt.company.id = ?";
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

    /**
     * Load All ProfileTemplate
     * 
     * @return com.ems.model.ProfileTemplate collection
     */
    @SuppressWarnings("unchecked")
    public List<ProfileTemplate> loadAllProfileTemplate() {
    	List<ProfileTemplate> results= null;
    	try {
    		results = getSession().createQuery("from ProfileTemplate order by id").list();
    		//Fill the fixture Count for the Templates.
		   	List<Object> fixtureCountList =  getFixtureCountForTemplate();
		    if (results != null && !results.isEmpty())
		    {
	              Iterator<ProfileTemplate> it = results.iterator();
	              while (it.hasNext()) {
	                  ProfileTemplate rowResult = it.next();
	                  if(fixtureCountList!=null && !fixtureCountList.isEmpty())
	                  {
		                  for (Iterator<Object> fixInterator = fixtureCountList.iterator(); fixInterator.hasNext();)
							{
		                	  	Object[] fixobject = (Object[]) fixInterator.next();
				    			Long templateId = (Long) fixobject[0];
				    			if(rowResult.getId().compareTo(templateId) == 0) {
			    					rowResult.setFixtureCount((Long) fixobject[1]);
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
    public ProfileTemplate getProfileTemplateById(Long id) {
        List<ProfileTemplate> groupsList = getSession().createQuery("Select new ProfileTemplate(pt.id,pt.name) from ProfileTemplate pt where id = :id")
                .setLong("id", id).list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public ProfileTemplate getProfileTemplateByName(String profileTemplateName) {
        List<ProfileTemplate> groupsList = getSession()
                .createQuery("Select new ProfileTemplate(pt.id,pt.name) from ProfileTemplate pt where g.name = :name")
                .setString("name", profileTemplateName).list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    public ProfileTemplate editName(ProfileTemplate profileTemplate) {
        Session session = getSession();
        session.createQuery("Update ProfileTemplate set name = :name where id = :id").setString("name", profileTemplate.getName())
                .setLong("id", profileTemplate.getId()).executeUpdate();
        return getProfileTemplateById(profileTemplate.getId());
    }

    public void updateGroupProfile(ProfileHandler profileHandler, Long groupId) {
        try {
            Session session = getSession();
            Groups group = (Groups) session.get(Groups.class, groupId);
            group.setProfileHandler(profileHandler);
            session.save("profileHandler", group);
        } catch (HibernateException hbe) {
            hbe.printStackTrace();
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    @SuppressWarnings("unchecked")
	public List<Object> getFixtureCountForTemplate() {
		List<Object> fixtureCountForTemplate = new ArrayList<Object>();
		try {
			Session s = getSession();
			String hsql = "select t.id as templateId,count(f.id) as fixtureCount from Fixture f,Groups g,ProfileTemplate t where f.groupId=g.id AND g.profileTemplate.id=t.id group by t.id";
			Query query = s.createQuery(hsql);
			fixtureCountForTemplate = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fixtureCountForTemplate;
	}
    
    public void updateTemplateVisibility(Long templateId, boolean visibility) {
		Session session = getSession();		
		session.createQuery("Update ProfileTemplate set displayTemplate = :displayTemplate where id = :id")
				.setBoolean("displayTemplate", visibility)
				.setLong("id", templateId).executeUpdate();
	}
    
    
    @SuppressWarnings("unchecked")
	public List<Object> getFixtureCountForProfileTemplate(Long profileId) {
		try {
			Session s = getSession();
			String hsql = "SELECT p.id,count(f.id) from Fixture f, ProfileTemplate p where f.groupId=:profileId group by p.id";
			Query query = s.createQuery(hsql);
			query.setParameter("profileId",profileId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
