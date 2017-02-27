package com.ems.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.PlugloadProfileTemplate;
import com.ems.model.WeekdayPlugload;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.ArgumentUtils;

@Repository("plugloadGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadGroupDao  extends BaseDaoHibernate{
	
	@Resource
	PlugloadDao plugloadDao;
	
	@Resource
	PlugloadProfileTemplateDao plugloadProfileTemplateDao;
	
	private int  minProfileNo = 17;
	 
	 private int MaxProfileNo = 255;

	@SuppressWarnings("unchecked")
	public List<PlugloadGroups> loadAllProfileTemplateById(long templateId,
			Long tenantId) {
        List<PlugloadGroups> resultList = null;
        List<PlugloadGroups> groupsList = new ArrayList<PlugloadGroups>();
        String hsql=null;
        try {
        	if(tenantId==0)
        		hsql = "from PlugloadGroups g where g.displayProfile='true' and g.plugloadProfileTemplate.id = ? order by name";
        	else
        		hsql = "from PlugloadGroups g where g.displayProfile='true' and g.plugloadProfileTemplate.id = ?  and g.tenant.id=? order by name";
	        Query q = getSession().createQuery(hsql.toString());
	        if(tenantId==0)
	        {
	        	q.setParameter(0, templateId);
	        }else
	        {
	        	q.setParameter(0, templateId);
	        	q.setParameter(1, tenantId);
	        }
	        resultList = q.list();	        
	        
	      //Fill the Plugload Count for the profiles.
        	List<Object> plugloadCountList =  getAllPlugloadCountForProfile();        	
		    if (resultList != null && !resultList.isEmpty())
		    {
	              Iterator<PlugloadGroups> it = resultList.iterator();
	              PlugloadGroups group;
	              PlugloadGroups rowResult;
	              while (it.hasNext()) {
	            	  rowResult = it.next();
	            	  group = new PlugloadGroups();
	                  group.setCompany(rowResult.getCompany());
	                  group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
	                  group.setDefaultProfile(rowResult.isDefaultProfile());
	                  group.setDisplayProfile(rowResult.isDisplayProfile());
	                  group.setId(rowResult.getId());
	                  group.setPlugloadProfileHandler(rowResult.getPlugloadProfileHandler());
	                  group.setProfileNo(rowResult.getProfileNo());
	                  group.setPlugloadProfileTemplate(rowResult.getPlugloadProfileTemplate());
	                  group.setTenant(rowResult.getTenant());
	                  //if(rowResult.isDefaultProfile()==true)
	                //  {
	                //	  group.setName(rowResult.getName()+"_Default");
	                 // }else
	                 // {
	                	  group.setName(rowResult.getName());
	                //  }
	                  if(plugloadCountList!=null && !plugloadCountList.isEmpty())
	                  {
		                  for (Iterator<Object> plugloadInterator = plugloadCountList.iterator(); plugloadInterator.hasNext();)
							{
		                	  	Object[] plugloadobject = (Object[]) plugloadInterator.next();
		                	  	Long profileId = (Long) plugloadobject[0];
				    			if(rowResult.getId().compareTo(profileId) == 0) {
				    				group.setPlugloadCount((Long) plugloadobject[1]);
			    					break;
			    				}
							}
	                  }
	                  groupsList.add(group);
	              }

		    }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
        	System.out.println("group list is "+groupsList.size());
            return groupsList;
        }
        return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getAllPlugloadCountForProfile() {
		try {
			Session s = getSession();
			String hsql = "SELECT pg.id,count(pl.id) from Plugload pl, PlugloadGroups pg where pl.state!='DELETED' and pl.groupId=pg.id group by pg.id";
			Query query = s.createQuery(hsql);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getPlugloadCountForPlugloadProfile(Long plugloadProfileId) {
		try {
			Session s = getSession();
			String hsql = "SELECT pg.id,count(pl.id) from Plugload pl, PlugloadGroups pg where pl.state!='DELETED' and pl.groupId=:plugloadProfileId group by pg.id";
			Query query = s.createQuery(hsql);
			query.setParameter("plugloadProfileId",plugloadProfileId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public PlugloadGroups getGroupById(Long id) {
		List<PlugloadGroups> groupsList = null;
        String hsql = "from PlugloadGroups g where g.id = ? order by name";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, id);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
	}
	
	@SuppressWarnings("unchecked")
    public List<PlugloadGroups> loadAllPlugloadGroups() {
    	  try {
    		  	List<PlugloadGroups> groupsList = new ArrayList<PlugloadGroups>();
		    	List<PlugloadGroups> resultList =null;
		    	
		        String hsql = "from PlugloadGroups g where g.displayProfile='true' and g.profileNo>0 order by g.name";
		        Query q = getSession().createQuery(hsql.toString());
		        resultList = q.list();
		        
		        if (resultList != null && !resultList.isEmpty())
			    {
		            Iterator<PlugloadGroups> it = resultList.iterator();
		            while (it.hasNext()) {
		            	PlugloadGroups rowResult = it.next();
		            	PlugloadGroups group = new PlugloadGroups();
			            group.setCompany(rowResult.getCompany());
			            group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
			            group.setDefaultProfile(rowResult.isDefaultProfile());
			            group.setDisplayProfile(rowResult.isDisplayProfile());
			            group.setId(rowResult.getId());
			            group.setPlugloadProfileHandler(rowResult.getPlugloadProfileHandler());
			            group.setProfileNo(rowResult.getProfileNo());
			            group.setPlugloadProfileTemplate(rowResult.getPlugloadProfileTemplate());
			            group.setTenant(rowResult.getTenant());
			            if(rowResult.isDefaultProfile()==true && rowResult.getId()!=1)
			            {
			          	  group.setName(rowResult.getName()+"_Default");
			            }else
			            {
			          	  group.setName(rowResult.getName());
			            }
			            groupsList.add(group);
			      }
			    }
            
	        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
	            return groupsList;
	        }
    	  } catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
	
	/**
     * Load All Plugload groups Except Default Profile
     * 
     * @return com.ems.model.PlugloadGroups collection
     */
    @SuppressWarnings("unchecked")
    public List<PlugloadGroups> loadAllPlugloadGroupsExceptDeafult() {
        try {
        	List<PlugloadGroups> groupsList = new ArrayList<PlugloadGroups>();
            List<PlugloadGroups> results = null;
            //String hsql = "Select g from PlugloadGroups g where g.name!='Default' and g.displayProfile='true' and g.profileNo>0 order by g.name";
            String hsql = "Select g from PlugloadGroups g where g.displayProfile='true' and g.profileNo>0 order by g.name";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty())
		    {
	            Iterator<PlugloadGroups> it = results.iterator();
	            while (it.hasNext()) {
	            	PlugloadGroups rowResult = it.next();
	            	PlugloadGroups group = new PlugloadGroups();
		            group.setCompany(rowResult.getCompany());
		            group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
		            group.setDefaultProfile(rowResult.isDefaultProfile());
		            group.setDisplayProfile(rowResult.isDisplayProfile());
		            group.setId(rowResult.getId());
		            group.setPlugloadProfileHandler(rowResult.getPlugloadProfileHandler());
		            group.setProfileNo(rowResult.getProfileNo());
		            group.setPlugloadProfileTemplate(rowResult.getPlugloadProfileTemplate());
		            group.setTenant(rowResult.getTenant());
		            //if(rowResult.isDefaultProfile()==true && rowResult.getId()!=1)
		            //{
		          	  //group.setName(rowResult.getName()+"_Default");
		           // }else
		           // {
		          	  group.setName(rowResult.getName());
		           // }
		            groupsList.add(group);
		      }
		    }
            if (groupsList != null && !groupsList.isEmpty()) {
                return groupsList;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
	
	@SuppressWarnings("unchecked")
	public List<PlugloadGroups> loadAllDerivedProfile(Long groupId) {
		List<PlugloadGroups> derivedProfileList = null;
        String hsql = "from PlugloadGroups g where g.derivedFromGroup.id=? order by g.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, groupId);
        derivedProfileList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(derivedProfileList)) {
            return derivedProfileList;
        }
        return null;
	}

	public List<WeekdayPlugload> loadAllWeekByProfileConfigurationId(Long profileConfigurationId) {
		List<WeekdayPlugload> weekList = null;
        String hsql = "from WeekdayPlugload w where w.plugloadProfileConfiguration.id=? order by w.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, profileConfigurationId);
        weekList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(weekList)) {
            return weekList;
        }
        return null;
	}
	
	@SuppressWarnings("unchecked")
	public PlugloadGroups getPlugloadGroupByName(String sGroupname) {

        List<PlugloadGroups> groupsList = null;
        sGroupname = sGroupname.toUpperCase();
        String hsql = "from PlugloadGroups g where upper(g.name) = ?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, sGroupname);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

	public PlugloadGroups getPlugloadGroupById(Long id) {
		
		List<PlugloadGroups> groupsList = null;
        String hsql = "from PlugloadGroups g where g.id = ? order by name";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, id);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
	}
	
	public Short getMaxPlugloadProfileNo(Long tenantId) {
		Integer profileNo = 0;
		  try {
		    Session s = getSession();
		    String hsql=null;
		    Query query =null;
		   if(tenantId==null)  //Regular USER
		    {
		    	hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join Plugload_Groups g on g.profile_no = pr_no where g.profile_no is NULL limit 1";
		    	query  = s.createSQLQuery(hsql);
		    }else   //Tenant USER
		    {
		    	hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join Plugload_Groups g on g.profile_no = pr_no and g.tenant_id ="+ tenantId +" where g.profile_no is NULL limit 1";
		    	query  = s.createSQLQuery(hsql);
		    }
		   
		    profileNo =  (Integer) query.uniqueResult();
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		  if(profileNo!=null)
		  {
			  return profileNo.shortValue();
		  }else
		  {
			  return 0;
		  }
		  
	}
	
	public PlugloadGroups editName(PlugloadGroups groups) {
        Session session = getSession();
        session.createQuery("Update PlugloadGroups set name = :name where id = :id").setString("name", groups.getName())
                .setLong("id", groups.getId()).executeUpdate();
        return getGroupById(groups.getId());
    }
	
	  public PlugloadProfileHandler fetchProfileHandlerById(Long id) {
		  PlugloadProfileHandler profileHandler = null;
	        try {
	            getSession().clear();
	            profileHandler = (PlugloadProfileHandler) getSession().createCriteria(PlugloadProfileHandler.class)
	                    .setFetchMode("morningProfile", FetchMode.JOIN).setFetchMode("dayProfile", FetchMode.JOIN)
	                    .setFetchMode("eveningProfile", FetchMode.JOIN).setFetchMode("nightProfile", FetchMode.JOIN)
	                    .setFetchMode("morningProfileWeekEnd", FetchMode.JOIN)
	                    .setFetchMode("dayProfileWeekEnd", FetchMode.JOIN)
	                    .setFetchMode("eveningProfileWeekEnd", FetchMode.JOIN)
	                    .setFetchMode("nightProfileWeekEnd", FetchMode.JOIN)
	                    .setFetchMode("morningProfileHoliday", FetchMode.JOIN)
	                    .setFetchMode("dayProfileHoliday", FetchMode.JOIN)
	                    .setFetchMode("eveningProfileHoliday", FetchMode.JOIN)
	                    .setFetchMode("nightProfileHoliday", FetchMode.JOIN)
	                    .setFetchMode("plugloadProfileConfiguration", FetchMode.JOIN).add(Restrictions.idEq(id)).uniqueResult();
	        } catch (HibernateException hbe) {
	           
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return profileHandler;
	    }
	  
	  public class ProfileComparator implements Comparator<PlugloadGroups>{
	        @Override
	        public int compare(PlugloadGroups pg1, PlugloadGroups pg2) {
	            return pg1.getName().toLowerCase().compareTo(pg2.getName().toLowerCase());
	        }
	  }
	    
	  public class TemplateComparator implements Comparator<PlugloadProfileTemplate>{
		    @Override
		    public int compare(PlugloadProfileTemplate pgt1, PlugloadProfileTemplate pgt2) {
		        return pgt1.getName().toLowerCase().compareTo(pgt2.getName().toLowerCase());
		    }
	  }
	  
	  
	  public TreeNode<FacilityType> loadPlugloadProfileHierarchy(boolean visibilityCheck) {

	        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	        rootNode.setNodeId("0");
	        rootNode.setName("PlugloadTemplate");
	        rootNode.setNodeType(FacilityType.PLUGLOADTEMPLATE);

	      //Get Plugload Profile Templates
	        List<PlugloadProfileTemplate> plugloadProfileTemplateList = plugloadProfileTemplateDao.loadAllPlugloadProfileTemplate();
	        Collections.sort(plugloadProfileTemplateList, new TemplateComparator());
	        //int displayCount = 0;
	        if (plugloadProfileTemplateList != null) {
	            for (PlugloadProfileTemplate plugloadProfileTemplate : plugloadProfileTemplateList) {
	            	if (plugloadProfileTemplate.isDisplayTemplate()==visibilityCheck)
	            	{
		            	TreeNode<FacilityType> plugloadTemplateNode = new TreeNode<FacilityType>();
		            	plugloadTemplateNode.setNodeId(plugloadProfileTemplate.getId().toString());
		            	plugloadTemplateNode.setName(plugloadProfileTemplate.getName());
		            	plugloadTemplateNode.setNodeType(FacilityType.PLUGLOADTEMPLATE);
		                List<Plugload> plugLoadList = plugloadDao.loadPlugloadByProfileTemplateId(plugloadProfileTemplate.getId());
		                if(plugLoadList != null)
		                plugloadTemplateNode.setCount(plugLoadList.size());  
		             
		               // displayCount = 0;
		                
		                //Get Plugload Profiles
		                List<PlugloadGroups> plugloadGroupsList = plugloadProfileTemplate.getPlugloadProfilesList(plugloadProfileTemplate.getPlugloadProfiles());
		                Collections.sort(plugloadGroupsList, new ProfileComparator());
		                for (PlugloadGroups plugloadGroups : plugloadGroupsList) {
		                	//if ((plugloadGroups.isDisplayProfile()==visibilityCheck) && (!plugloadGroups.getName().equalsIgnoreCase("default"))) {
	                		if ((plugloadGroups.isDisplayProfile()==visibilityCheck)) {
			                	 TreeNode<FacilityType> plugloadProfileNode = new TreeNode<FacilityType>();
			                	 plugloadProfileNode.setNodeId(plugloadGroups.getId().toString());
			                	 //if(plugloadGroups.isDefaultProfile()==true)
			                	// {
			                	//	 plugloadProfileNode.setName(plugloadGroups.getName()+"_Default");
			                	// }else
			                	// {
			                		 plugloadProfileNode.setName(plugloadGroups.getName());
			                	// }
			                	 plugloadProfileNode.setNodeType(FacilityType.PLUGLOADGROUP);
			                	 plugloadProfileNode.setSelected(true);
			                	 plugLoadList = plugloadDao.loadPlugloadByPlugloadGroupId(plugloadGroups.getId());
			                	 if(plugLoadList != null)
			                	 plugloadProfileNode.setCount(plugLoadList.size());
			                	 plugloadTemplateNode.setSelected(true);
			                	 plugloadTemplateNode.addTreeNode(plugloadProfileNode);
		                	}
		                }
		                //if (!plugloadTemplateNode.getName().equalsIgnoreCase("default"))
		                rootNode.addTreeNode(plugloadTemplateNode);
	            	}
	            }
	        }
	        return rootNode;
	    }
	  
	  
	  	// This tree will return all plugload profiles node with out any visibility check
	    public TreeNode<FacilityType> loadFilterPlugloadProfileHierarchy() {

	        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	        rootNode.setNodeId("0");
	        rootNode.setName("PlugloadTemplate");
	        rootNode.setNodeType(FacilityType.PLUGLOADTEMPLATE);

	        //Get Profile Templates
	        List<PlugloadProfileTemplate> plugloadProfileTemplateList = plugloadProfileTemplateDao.loadAllPlugloadProfileTemplate();
	        Collections.sort(plugloadProfileTemplateList, new TemplateComparator());
	        //int displayCount = 0;
	        if (plugloadProfileTemplateList != null) {
	            for (PlugloadProfileTemplate plugloadProfileTemplate : plugloadProfileTemplateList) {
	            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
	                templateNode.setNodeId(plugloadProfileTemplate.getId().toString());
	                templateNode.setName(plugloadProfileTemplate.getName());
	                templateNode.setNodeType(FacilityType.PLUGLOADTEMPLATE);
	                if(plugloadProfileTemplate.isDisplayTemplate())
	              	  templateNode.setSelected(true);
	                else
	              	  templateNode.setSelected(false);
	                
	                List<Plugload> plugloadList = plugloadDao.loadPlugloadByProfileTemplateId(plugloadProfileTemplate.getId());
	                if(plugloadList != null)
	                templateNode.setCount(plugloadList.size());
	                
	               // displayCount = 0;
	                
	                //Get Profiles
	                List<PlugloadGroups> plugloadGroupsList = plugloadProfileTemplate.getPlugloadProfilesList(plugloadProfileTemplate.getPlugloadProfiles());
	                Collections.sort(plugloadGroupsList, new ProfileComparator());
	                for (PlugloadGroups plugloadGroups : plugloadGroupsList) {
	                	//if ((!plugloadGroups.getName().equalsIgnoreCase("default"))) {
		                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
		                	 profileNode.setNodeId(plugloadGroups.getId().toString());
		                	 //if(plugloadGroups.isDefaultProfile()==true)
		                	// {
		                	//	 profileNode.setName(plugloadGroups.getName()+"_Default");
		                	// }else
		                	// {
		                		 profileNode.setName(plugloadGroups.getName());
		                	// }
		                	 plugloadList = plugloadDao.loadPlugloadByPlugloadGroupId(plugloadGroups.getId());
		                	 if(plugloadList != null)
		                	 profileNode.setCount(plugloadList.size());
		                	 profileNode.setNodeType(FacilityType.PLUGLOADGROUP);
		                	 profileNode.setSelected(true);
		                	 templateNode.setSelected(true);
		                	 if(plugloadGroups.isDisplayProfile())
		                    	 profileNode.setSelected(true);
			                 else
			                	profileNode.setSelected(false);
		                     templateNode.addTreeNode(profileNode);
	                	//}
	                }
	                //if (!templateNode.getName().equalsIgnoreCase("default"))
	                rootNode.addTreeNode(templateNode);
	            }
	        }
	        return rootNode;
	    }
	    
	    public PlugloadGroups getGroupByProfileAndTenantDetails(byte profileNo, Long tenantId) {
	    	
	    	String hsql = "from PlugloadGroups pg WHERE pg.tenant ";	    	
	    	if(tenantId == null) {
	    		hsql += "IS NULL";
	    	} else {
	    		hsql += "= " + tenantId;
	    	}
	    	Query q = getSession().createSQLQuery(hsql);
	    	return (PlugloadGroups)q.uniqueResult();
	    	
	    } //end of method getGroupByProfileAndTenantDetails

}
