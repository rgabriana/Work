package com.ems.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.WeekDay;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.AssignFixtureList;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("groupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GroupDao extends BaseDaoHibernate {

	 @Resource
	 private ProfileTemplateDao profileTemplateDao;
	 
	 private int  minProfileNo = 17;
	 
	 private int MaxProfileNo = 255;

    /**
     * Load company's group
     * 
     * @param id
     *            company id
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadGroupByCompanyId(Long id) {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name) from Groups g where g.company.id = ?";
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
     * Load All groups
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadAllGroups() {
//        try {
//            List<Groups> results = null;
//            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileNo,g.profileTemplate.id,g.defaultProfile) from Groups g where g.profileTemplate.id!=null order by g.id";
//            Query q = getSession().createQuery(hsql.toString());
//            results = q.list();
//            if (results != null && !results.isEmpty()) {
//                return results;
//            }
//        } catch (HibernateException hbe) {
//            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
//        }
//        return null;
    	  try {
    		  	List<Groups> groupsList = new ArrayList<Groups>();
		    	List<Groups> resultList =null;
		    	
		        String hsql = "from Groups g where g.displayProfile='true' and g.profileNo>0 order by g.name";
		        Query q = getSession().createQuery(hsql.toString());
		        resultList = q.list();
		        
		        if (resultList != null && !resultList.isEmpty())
			    {
		            Iterator<Groups> it = resultList.iterator();
		            while (it.hasNext()) {
			            Groups rowResult = it.next();
				        Groups group = new Groups();
			            group.setCompany(rowResult.getCompany());
			            group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
			            group.setDefaultProfile(rowResult.isDefaultProfile());
			            group.setDisplayProfile(rowResult.isDisplayProfile());
			            group.setId(rowResult.getId());
			            group.setProfileHandler(rowResult.getProfileHandler());
			            group.setProfileNo(rowResult.getProfileNo());
			            group.setProfileTemplate(rowResult.getProfileTemplate());
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
     * Load All groups Except Default Profile
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<Groups> loadAllGroupsExceptDeafult() {
        try {
        	List<Groups> groupsList = new ArrayList<Groups>();
            List<Groups> results = null;
            String hsql = "Select g from Groups g where g.name!='Default' and g.displayProfile='true' and g.profileNo>0 order by g.name";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty())
		    {
	            Iterator<Groups> it = results.iterator();
	            while (it.hasNext()) {
		            Groups rowResult = it.next();
			        Groups group = new Groups();
		            group.setCompany(rowResult.getCompany());
		            group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
		            group.setDefaultProfile(rowResult.isDefaultProfile());
		            group.setDisplayProfile(rowResult.isDisplayProfile());
		            group.setId(rowResult.getId());
		            group.setProfileHandler(rowResult.getProfileHandler());
		            group.setProfileNo(rowResult.getProfileNo());
		            group.setProfileTemplate(rowResult.getProfileTemplate());
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
            if (groupsList != null && !groupsList.isEmpty()) {
                return groupsList;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Groups getGroupById(Long id) {
//        List<Groups> groupsList = getSession().createQuery("Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileNo,g.profileTemplate.id,g.defaultProfile) from Groups g where g.profileTemplate.id!=null and id = :id")
//                .setLong("id", id).list();
//        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
//            return groupsList.get(0);
//        }
//        return null;
    	
    	List<Groups> groupsList = null;
        String hsql = "from Groups g where g.id = ? order by name";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, id);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public Groups getGroupByName(String sGroupname) {
//        List<Groups> groupsList = getSession()
//                .createQuery(
//                        "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileNo, g.profileTemplate.id,g.defaultProfile) from Groups g where g.profileTemplate.id!=null and g.name = :name")
//                .setString("name", sGroupname).list();
//        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
//            return groupsList.get(0);
//        }
//        return null;
        
        List<Groups> groupsList = null;
        sGroupname = sGroupname.toUpperCase();
        String hsql = "from Groups g where upper(g.name) = ?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, sGroupname);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }

    public Groups editName(Groups groups) {
        Session session = getSession();
        session.createQuery("Update Groups set name = :name where id = :id").setString("name", groups.getName())
                .setLong("id", groups.getId()).executeUpdate();
        return getGroupById(groups.getId());
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

    // Added by Nitin to get the list of profiles based on company

    public TreeNode<FacilityType> loadProfileHierarchy(boolean visibilityCheck) {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId("0");
        rootNode.setName("Template");
        rootNode.setNodeType(FacilityType.TEMPLATE);

      //Get Profile Templates
        List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
        Collections.sort(profileTemplateList, new TemplateComparator());
        //int displayCount = 0;
        if (profileTemplateList != null) {
            for (ProfileTemplate profileTemplate : profileTemplateList) {
            	if (profileTemplate.isDisplayTemplate()==visibilityCheck)
            	{
	            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
	                templateNode.setNodeId(profileTemplate.getId().toString());
	                templateNode.setName(profileTemplate.getName());
	                templateNode.setNodeType(FacilityType.TEMPLATE);
	               // displayCount = 0;
	                
	                //Get Profiles
	                List<Groups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
	                Collections.sort(profilesList, new ProfileComparator());
	                for (Groups group : profilesList) {
	                	if ((group.isDisplayProfile()==visibilityCheck) && (!group.getName().equalsIgnoreCase("default"))) {
		                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
		                	 profileNode.setNodeId(group.getId().toString());
		                	 if(group.isDefaultProfile()==true)
		                	 {
		                		 profileNode.setName(group.getName()+"_Default");
		                	 }else
		                	 {
		                		 profileNode.setName(group.getName());
		                	 }
		                	 profileNode.setNodeType(FacilityType.GROUP);
		                	 profileNode.setSelected(true);
		                	 templateNode.setSelected(true);
		                     templateNode.addTreeNode(profileNode);
	                	}
	                }
	                if (!templateNode.getName().equalsIgnoreCase("default"))
	                rootNode.addTreeNode(templateNode);
            	}
            }
        }
        return rootNode;
    }
    
    // This tree will return all profiles node with out any visibility check
    public TreeNode<FacilityType> loadFilterProfileHierarchy() {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId("0");
        rootNode.setName("Template");
        rootNode.setNodeType(FacilityType.TEMPLATE);

        //Get Profile Templates
        List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
        Collections.sort(profileTemplateList, new TemplateComparator());
        //int displayCount = 0;
        if (profileTemplateList != null) {
            for (ProfileTemplate profileTemplate : profileTemplateList) {
            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
                templateNode.setNodeId(profileTemplate.getId().toString());
                templateNode.setName(profileTemplate.getName());
                templateNode.setNodeType(FacilityType.TEMPLATE);
                if(profileTemplate.isDisplayTemplate())
              	  templateNode.setSelected(true);
                else
              	  templateNode.setSelected(false);
               // displayCount = 0;
                
                //Get Profiles
                List<Groups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
                Collections.sort(profilesList, new ProfileComparator());
                for (Groups group : profilesList) {
                	if ((!group.getName().equalsIgnoreCase("default"))) {
	                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
	                	 profileNode.setNodeId(group.getId().toString());
	                	 if(group.isDefaultProfile()==true)
	                	 {
	                		 profileNode.setName(group.getName()+"_Default");
	                	 }else
	                	 {
	                		 profileNode.setName(group.getName());
	                	 }
	                	 profileNode.setNodeType(FacilityType.GROUP);
	                	 profileNode.setSelected(true);
	                	 templateNode.setSelected(true);
	                	 if(group.isDisplayProfile())
	                    	 profileNode.setSelected(true);
		                 else
		                	profileNode.setSelected(false);
	                     templateNode.addTreeNode(profileNode);
                	}
                }
                if (!templateNode.getName().equalsIgnoreCase("default"))
                rootNode.addTreeNode(templateNode);
            }
        }
        return rootNode;
    }

    @SuppressWarnings("unchecked")
	public List<Groups> getDRSensitivity() {
        try {
            List<Groups> results = null;
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileHandler.drReactivity, g.profileNo,g.profileTemplate.id) from Groups g where g.profileTemplate.id!=null";
            Query q = getSession().createQuery(hsql.toString());
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
     * Returns the dr sensitivity formatted for the webservice layer to access it.
     * @return Group EC records with dr sensitivity field updated.
     */
    @SuppressWarnings("unchecked")
	public List<GroupECRecord> getDRSensitivityRecords() {
        try {
            List<GroupECRecord> drSensitivityRecords = new ArrayList<GroupECRecord>();
            String hsql = "Select new Groups(g.id,g.name,g.profileHandler.id, g.profileHandler.profileChecksum, g.profileHandler.globalProfileChecksum, g.profileHandler.profileGroupId, g.profileHandler.drReactivity, g.profileNo,g.profileTemplate.id) from Groups g where g.profileTemplate.id!=null";
            Query q = getSession().createQuery(hsql.toString());
            List<Groups> results = q.list();
            Groups oGroup = null;
            if (results != null && !results.isEmpty()) {
                Iterator<Groups> oRecords = results.iterator();
                while (oRecords.hasNext()) {
                    oGroup = oRecords.next();
                    GroupECRecord groupRecord = new GroupECRecord();
                    groupRecord.setI(oGroup.getId().intValue());
                    groupRecord.setName(oGroup.getName());
                    groupRecord.setDrSensitivity((int) oGroup.getProfileHandler().getDrReactivity());
                    drSensitivityRecords.add(groupRecord);
                }
                return drSensitivityRecords;
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    public ProfileHandler getProfileHandlerByGroupId(Long id) {
    	
    	Session session = getSession();
        ProfileHandler profileHandler = (ProfileHandler) session.get(ProfileHandler.class, id);
        if(profileHandler!=null)
        	return profileHandler;
        return null;
    }

    public Long getGroupByProfileAndTenantDetails(byte profileNo, Long tenantId) {
    	String hsql = "SELECT  getGroupIdFromProfileNoAndTenantId(" + Byte.valueOf(profileNo) + ", " + tenantId + ")";    
        Query q = getSession().createSQLQuery(hsql.toString());
        long groupNo = (Integer)q.uniqueResult();
        return groupNo;
    }
    
    public class ProfileComparator implements Comparator<Groups>{
        @Override
        public int compare(Groups g1, Groups g2) {
            return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
        }
    }
    public class TemplateComparator implements Comparator<ProfileTemplate>{
	    @Override
	    public int compare(ProfileTemplate pt1, ProfileTemplate pt2) {
	        return pt1.getName().toLowerCase().compareTo(pt2.getName().toLowerCase());
	    }
	}
	@SuppressWarnings("unchecked")
	public List<Groups> loadAllProfileTemplateById(long templateId, Long tenantId) {
        List<Groups> resultList = null;
        List<Groups> groupsList = new ArrayList<Groups>();
        String hsql=null;
        try {
        	if(tenantId==0)
        		hsql = "from Groups g where g.displayProfile='true' and g.profileTemplate.id = ? order by name";
        	else
        		hsql = "from Groups g where g.displayProfile='true' and g.profileTemplate.id = ?  and g.tenant.id=? order by name";
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
	        
	        //Fill the fixture Count for the profiles.
        	List<Object> fixtureCountList =  getAllFixtureCountForProfile();
		    if (resultList != null && !resultList.isEmpty())
		    {
	              Iterator<Groups> it = resultList.iterator();
	              while (it.hasNext()) {
	            	  Groups rowResult = it.next();
	            	  Groups group = new Groups();
	                  group.setCompany(rowResult.getCompany());
	                  group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
	                  group.setDefaultProfile(rowResult.isDefaultProfile());
	                  group.setDisplayProfile(rowResult.isDisplayProfile());
	                  group.setId(rowResult.getId());
	                  group.setProfileHandler(rowResult.getProfileHandler());
	                  group.setProfileNo(rowResult.getProfileNo());
	                  group.setProfileTemplate(rowResult.getProfileTemplate());
	                  group.setTenant(rowResult.getTenant());
	                  if(rowResult.isDefaultProfile()==true)
	                  {
	                	  group.setName(rowResult.getName()+"_Default");
	                  }else
	                  {
	                	  group.setName(rowResult.getName());
	                  }
	                  if(fixtureCountList!=null && !fixtureCountList.isEmpty())
	                  {
		                  for (Iterator<Object> fixInterator = fixtureCountList.iterator(); fixInterator.hasNext();)
							{
		                	  	Object[] fixobject = (Object[]) fixInterator.next();
		                	  	Long profileId = (Long) fixobject[0];
				    			if(rowResult.getId().compareTo(profileId) == 0) {
				    				group.setFixtureCount((Long) fixobject[1]);
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
            return groupsList;
        }
        return null;
	}
	
	public void updateGroupVisibility(Long groupId, boolean visibility) {
		Session session = getSession();
		session.createQuery("Update Groups set displayProfile = :displayProfile where id = :id")
				.setBoolean("displayProfile", visibility)
				.setLong("id", groupId).executeUpdate();
	}
	
	public Short getMaxProfileNo(Long tenantId) {
		Integer profileNo = 0;
		  try {
		    Session s = getSession();
		    String hsql=null;
		    Query query =null;
		   if(tenantId==null)  //Regular USER
		    {
		    	hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join groups g on g.profile_no = pr_no where g.profile_no is NULL limit 1";
		    	query  = s.createSQLQuery(hsql);
		    }else   //Tenant USER
		    {
		    	hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join groups g on g.profile_no = pr_no and g.tenant_id ="+ tenantId +" where g.profile_no is NULL limit 1";
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

	@SuppressWarnings("unchecked")
	public List<Groups> loadAllProfileForTenantByTemplateId(Long templateId,Long tenantId) {
		List<Groups> resultList = null;
		List<Groups> groupsList = new ArrayList<Groups>();
        try {
        	 String hsql = "from Groups g where (g.displayProfile='true' and g.defaultProfile='true' or g.displayProfile='true' and g.tenant.id=?) and  g.profileTemplate.id = ? order by name";
             Query q = getSession().createQuery(hsql.toString());
             q.setParameter(0, tenantId);
             q.setParameter(1, templateId);
             resultList = q.list();
        
	        //Fill the fixture Count for the profiles.
        	List<Object> fixtureCountList =  getAllFixtureCountForTenantProfile(tenantId);
		    if (resultList != null && !resultList.isEmpty())
		    {
	              Iterator<Groups> it = resultList.iterator();
	              while (it.hasNext()) {
	                  Groups rowResult = it.next();
	                  Groups group = new Groups();
	                  group.setCompany(rowResult.getCompany());
	                  group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
	                  group.setDefaultProfile(rowResult.isDefaultProfile());
	                  group.setDisplayProfile(rowResult.isDisplayProfile());
	                  group.setId(rowResult.getId());
	                  group.setProfileHandler(rowResult.getProfileHandler());
	                  group.setProfileNo(rowResult.getProfileNo());
	                  group.setProfileTemplate(rowResult.getProfileTemplate());
	                  group.setTenant(rowResult.getTenant());
	                  if(rowResult.isDefaultProfile()==true)
	                  {
	                	  group.setName(rowResult.getName()+"_Default");
	                  }else
	                  {
	                	  group.setName(rowResult.getName());
	                  }
	                  if(fixtureCountList!=null && !fixtureCountList.isEmpty())
	                  {
		                  for (Iterator<Object> fixInterator = fixtureCountList.iterator(); fixInterator.hasNext();)
							{
		                	  	Object[] fixobject = (Object[]) fixInterator.next();
		                	  	Long profileId = (Long) fixobject[0];
				    			if(rowResult.getId().compareTo(profileId) == 0) {
				    				group.setFixtureCount((Long) fixobject[1]);
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
            return groupsList;
        }
        
        return null;
	}

	@SuppressWarnings("unchecked")
	public List<Groups> loadAllGroupsForTenantUser(Long tenantId) {
		List<Groups> groupsList = null;
        String hsql = "from Groups g where (g.displayProfile='true' and g.defaultProfile='true' or g.displayProfile='true' and g.tenant.id=?) order by name";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, tenantId);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList;
        }
        return null;
	}
	@SuppressWarnings("unchecked")
	public List<Object> getAllFixtureCountForProfile() {
		try {
			Session s = getSession();
			String hsql = "SELECT g.id,count(f.id) from Fixture f, Groups g where f.state!='DELETED' and f.groupId=g.id group by g.id";
			Query query = s.createQuery(hsql);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getFixtureCountForProfile(Long profileId) {
		try {
			Session s = getSession();
			String hsql = "SELECT g.id,count(f.id) from Fixture f, Groups g where f.state!='DELETED' and f.groupId=:profileId group by g.id";
			Query query = s.createQuery(hsql);
			query.setParameter("profileId",profileId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getAllFixtureCountForTenantProfile(Long tenantId) {
		try {
			Session s = getSession();
			String hsql = "SELECT g.id,count(f.id) from Fixture f, Groups g where f.state!='DELETED' and f.groupId=g.id and g.tenant.id=:tenantId group by g.id";
			Query query = s.createQuery(hsql);
			query.setParameter("tenantId",tenantId);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public List loadFixtureList(Long pid,String selectedfixtures, int offset, int limit,AssignFixtureList returnList) {
		List assignFixtureList = new ArrayList();
		List<Long> tmp = new ArrayList<Long>();
		StringTokenizer st = new StringTokenizer(selectedfixtures, ",");
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			tmp.add(Long.parseLong(token));
		}
		try {
            String queryStr = "select a.id as fixtureId,a.sensorId as fixtureName,a.groupId as currentGroupId,c.name as templateName , b.profileTemplate.id as template_id from Fixture a,Groups b,ProfileTemplate c where a.groupId=b.id AND b.profileTemplate.id=c.id AND a.id in (:ids)";
            Query query = getSession().createQuery(queryStr);				
			query.setParameterList("ids", tmp);
			if (limit >= 0) {
				//Set total number of records before offset and limit
				returnList.setRecords(query.list().size());
				int totalpages = (int) (Math.ceil(query.list().size() / new Double(AssignFixtureList.DEFAULT_ROWS)));
				returnList.setTotal(totalpages);
				query.setMaxResults(limit).setFirstResult(offset);
			}
			assignFixtureList = query.list();
        } catch (Exception e) {
            //logger.debug("Error in getting the list of selected fixtures information : " + e.getMessage());
        }
		return assignFixtureList;
	}
	
	@SuppressWarnings("unchecked")
	public List<WeekDay> loadAllWeekByProfileConfigurationId(Long profileConfigurationId) {
		List<WeekDay> weekList = null;
        String hsql = "from WeekDay w where w.profileConfiguration.id=? order by w.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, profileConfigurationId);
        weekList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(weekList)) {
            return weekList;
        }
        return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Groups> loadAllDerivedProfile(Long groupId) {
		List<Groups> derivedProfileList = null;
        String hsql = "from Groups g where g.derivedFromGroup.id=? order by g.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, groupId);
        derivedProfileList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(derivedProfileList)) {
            return derivedProfileList;
        }
        return null;
	}
	
	@SuppressWarnings("unchecked")
	public Groups getGroupByProfileNumber(short number) {        
        List<Groups> groupsList = null;
        String hsql = "from Groups g where g.profileNo = ?";
        Query q = getSession().createQuery(hsql.toString());        
        //q.setParameter(0, Byte.valueOf(number));
        q.setParameter(0, number);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }
}
