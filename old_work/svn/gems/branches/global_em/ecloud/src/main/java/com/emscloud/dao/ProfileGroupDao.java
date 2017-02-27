package com.emscloud.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileTemplate;
import com.emscloud.model.WeekDay;
import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Repository("profileGroupDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileGroupDao extends BaseDaoHibernate {

	 @Resource
	 ProfileTemplateDao profileTemplateDao;
	 private int  minProfileNo = 17;
	 
	 private int MaxProfileNo = 255;
	 
	 private int UEMMaxProfileNo = 65536;

    /**
     * Load All groups
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<ProfileGroups> loadAllGroups() {
    	  try {
    		  	List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
		        String hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileNo>0 order by g.name";
		        Query q = getSession().createQuery(hsql.toString());
		        List<ProfileGroups> resultList = q.list();
		        if (resultList != null && !resultList.isEmpty())
			    {
		        	groupsList = resultList;
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
     * Load All groups With Default Groups attached '_Default' at end.
     * 
     * @return com.ems.model.Group collection
     */
    @SuppressWarnings("unchecked")
    public List<ProfileGroups> loadAllGroupsIncludingDefault() {
    	  try {
    		  	List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
		        String hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileNo>0 order by g.name";
		        Query q = getSession().createQuery(hsql.toString());
		        List<ProfileGroups> resultList = q.list();
		        if (resultList != null && !resultList.isEmpty())
			    {
		        	Iterator<ProfileGroups> it = resultList.iterator();
	                while (it.hasNext()) {
	                    ProfileGroups rowResult = it.next();
	                    ProfileGroups group = new ProfileGroups();
	                    group.setCompany(rowResult.getCompany());
	                    group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
	                    group.setDefaultProfile(rowResult.isDefaultProfile());
	                    group.setDisplayProfile(rowResult.isDisplayProfile());
	                    group.setId(rowResult.getId());
	                    group.setProfileHandler(rowResult.getProfileHandler());
	                    group.setProfileNo(rowResult.getProfileNo());
	                    group.setProfileTemplate(rowResult.getProfileTemplate());
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
    public List<ProfileGroups> loadAllGroupsExceptDeafult() {
        try {
            List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
            List<ProfileGroups> results = null;
            String hsql = "Select g from ProfileGroups g where g.name!='Default' and g.displayProfile='true' and g.profileNo>0 order by g.name";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty())
            {
                Iterator<ProfileGroups> it = results.iterator();
                while (it.hasNext()) {
                    ProfileGroups rowResult = it.next();
                    ProfileGroups group = new ProfileGroups();
                    group.setCompany(rowResult.getCompany());
                    group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
                    group.setDefaultProfile(rowResult.isDefaultProfile());
                    group.setDisplayProfile(rowResult.isDisplayProfile());
                    group.setId(rowResult.getId());
                    group.setProfileHandler(rowResult.getProfileHandler());
                    group.setProfileNo(rowResult.getProfileNo());
                    group.setProfileTemplate(rowResult.getProfileTemplate());
                    //TODO: NEED TO SUPPORT TENANT
                    //group.setTenant(rowResult.getTenant());
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
    public ProfileGroups getGroupById(Long id) {
        List<ProfileGroups> groupsList = null;
        String hsql = "from ProfileGroups g where g.id = ? order by name";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, id);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public ProfileGroups getGroupByName(String sGroupname) {
        List<ProfileGroups> groupsList = null;
        sGroupname = sGroupname.toUpperCase();
        String hsql = "from ProfileGroups g where upper(g.name) = ?";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, sGroupname);
        groupsList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
            return groupsList.get(0);
        }
        return null;
    }
    public ProfileGroups editName(ProfileGroups groups) {
        Session session = getSession();
        session.createQuery("Update ProfileGroups set name = :name where id = :id").setString("name", groups.getName())
                .setLong("id", groups.getId()).executeUpdate();
        return getGroupById(groups.getId());
    }
    public Long getGroupByProfileAndTenantDetails(byte profileNo, Long tenantId) {
    	String hsql = "SELECT  getGroupIdFromProfileNoAndTenantId(" + Byte.valueOf(profileNo) + ", " + tenantId + ")";    
        Query q = getSession().createSQLQuery(hsql.toString());
        long groupNo = (Integer)q.uniqueResult();
        return groupNo;
    }
    public TreeNode<FacilityType> loadProfileHierarchy(boolean visibilityCheck) {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId((long) 0);
        rootNode.setName("Template");
        rootNode.setNodeType(FacilityType.PROFILETEMPLATE);

        //Get Profile Templates
        List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
        Collections.sort(profileTemplateList, new TemplateComparator());
        //int displayCount = 0;
        if (profileTemplateList != null) {
            for (ProfileTemplate profileTemplate : profileTemplateList) {
            	if (profileTemplate.isDisplayTemplate()==visibilityCheck)
            	{
	            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
	                templateNode.setNodeId(profileTemplate.getId());
	                templateNode.setName(profileTemplate.getName());
	                templateNode.setNodeType(FacilityType.PROFILETEMPLATE);
	                //TODO: NEED TO SUPPORT FIXTURE ASSOCIATED WITH TEMPLATE
	                //List<Fixture> fxList = fixtureDao.loadFixtureByTemplateId(profileTemplate.getId());
	                //if(fxList != null)
	                templateNode.setCount(0);  
	             
	               // displayCount = 0;
	                
	                //Get Profiles
	                List<ProfileGroups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
	                Collections.sort(profilesList, new ProfileComparator());
	                for (ProfileGroups group : profilesList) {
	                	if ((group.isDisplayProfile()==visibilityCheck) && (!group.getName().equalsIgnoreCase("default"))) {
		                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
		                	 profileNode.setNodeId(group.getId());
		                	 if(group.isDefaultProfile()==true)
		                	 {
		                		 profileNode.setName(group.getName()+"_Default");
		                	 }else
		                	 {
		                		 profileNode.setName(group.getName());
		                	 }
		                	 profileNode.setNodeType(FacilityType.PROFILEGROUP);
		                	 profileNode.setSelected(true);
		                	 //TODO: NEED TO SUPPORT FIXTURE ASSOCIATED WITH PROFILE
		                	 // fxList = fixtureDao.loadFixtureByGroupId(group.getId());
		                	 //if(fxList != null)
		                	 profileNode.setCount(0);
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
    
    public TreeNode<FacilityType> loadGlemDefaultProfileHierarchy(boolean visibilityCheck) {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId((long) 0);
        rootNode.setName("Global Default");
        rootNode.setNodeType(FacilityType.GLOBALDEFAULT);
        rootNode.setParentNodeId(0l);
        //Get Profile Templates
        List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
        Collections.sort(profileTemplateList, new TemplateComparator());
        if (profileTemplateList != null) {
            for (ProfileTemplate profileTemplate : profileTemplateList) {
            	if (profileTemplate.isDisplayTemplate()==visibilityCheck)
            	{
	            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
	            	templateNode.setParentNodeId(rootNode.getNodeId());
	                templateNode.setNodeId(profileTemplate.getId());
	                templateNode.setName(profileTemplate.getName());
	                templateNode.setNodeType(FacilityType.PROFILETEMPLATE);
	                templateNode.setCount(0);  
	             
	                //Get Profiles
	                List<ProfileGroups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
	                Collections.sort(profilesList, new ProfileComparator());
	                for (ProfileGroups group : profilesList) {
	                	if ((group.isDisplayProfile()==visibilityCheck) && (!group.getName().equalsIgnoreCase("default")) && group.isGlobalCreatedProfile()==true) {
		                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
		                	 profileNode.setNodeId(group.getId());
		                	 if(group.isDefaultProfile()==true)
		                	 {
		                		 profileNode.setName(group.getName()+"_Default");
		                	 }else
		                	 {
		                		 profileNode.setName(group.getName());
		                	 }
		                	 profileNode.setNodeType(FacilityType.PROFILEGROUP);
		                	 profileNode.setSelected(true);
		                	 profileNode.setCount(0);
		                	 profileNode.setParentNodeId(templateNode.getNodeId());
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
        rootNode.setNodeId((long)0);
        rootNode.setName("Template");
        rootNode.setNodeType(FacilityType.PROFILETEMPLATE);

        //Get Profile Templates
        List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
        Collections.sort(profileTemplateList, new TemplateComparator());
        //int displayCount = 0;
        if (profileTemplateList != null) {
            for (ProfileTemplate profileTemplate : profileTemplateList) {
            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
                templateNode.setNodeId(profileTemplate.getId());
                templateNode.setName(profileTemplate.getName());
                templateNode.setNodeType(FacilityType.PROFILETEMPLATE);
                if(profileTemplate.isDisplayTemplate())
              	  templateNode.setSelected(true);
                else
              	  templateNode.setSelected(false);
                
                //TODO: NEED TO SUPPORT FIXTURE ASSOCIATED WITH TEMPLATE
                // List<Fixture> fxList = fixtureDao.loadFixtureByTemplateId(profileTemplate.getId());
                //if(fxList != null)
                templateNode.setCount(0);//fxList.size()
                
               // displayCount = 0;
                
                //Get Profiles
                List<ProfileGroups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
                Collections.sort(profilesList, new ProfileComparator());
                for (ProfileGroups group : profilesList) {
                	if ((!group.getName().equalsIgnoreCase("default"))) {
	                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
	                	 profileNode.setNodeId(group.getId());
	                	 if(group.isDefaultProfile()==true)
	                	 {
	                		 profileNode.setName(group.getName()+"_Default");
	                	 }else
	                	 {
	                		 profileNode.setName(group.getName());
	                	 }
	                	 //TODO: NEED TO SUPPORT FIXTURE ASSOCIATED WITH PROFILE
	                	 //fxList = fixtureDao.loadFixtureByGroupId(group.getId());
	                	 //if(fxList != null)
	                	 profileNode.setCount(0);
	                	 profileNode.setNodeType(FacilityType.PROFILEGROUP);
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
    public List<ProfileGroups> loadAllProfileTemplateById(long templateId, Long tenantId,boolean globalFlag) {
        List<ProfileGroups> resultList = null;
        List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
        String hsql=null;
        try {
        	if(globalFlag==true)
        	{
        		  if(tenantId==0)
                      hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileTemplate.id = ? and g.globalCreatedProfile= 'true' order by name";
                  else
                      hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileTemplate.id = ?  and g.tenant.id=? and g.globalCreatedProfile= 'true' order by name";
        	}else
        	{
        		  if(tenantId==0)
        			  hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileTemplate.id = ? order by name";
        		  else
                      hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileTemplate.id = ?  and g.tenant.id=? order by name";
        	}
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
            
            //TODO:FILL THE FIXTURE COUNT FOR PROFILE
            //List<Object> fixtureCountList =  getAllFixtureCountForProfile();
            if (resultList != null && !resultList.isEmpty())
            {
                  Iterator<ProfileGroups> it = resultList.iterator();
                  while (it.hasNext()) {
                      ProfileGroups rowResult = it.next();
                      ProfileGroups group = new ProfileGroups();
                      group.setCompany(rowResult.getCompany());
                      group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
                      group.setDefaultProfile(rowResult.isDefaultProfile());
                      group.setDisplayProfile(rowResult.isDisplayProfile());
                      group.setId(rowResult.getId());
                      group.setProfileHandler(rowResult.getProfileHandler());
                      group.setProfileNo(rowResult.getProfileNo());
                      group.setProfileTemplate(rowResult.getProfileTemplate());
                      //TODO: NEED TO SUPPORT TENANT
                      //group.setTenant(rowResult.getTenant());
                      if(rowResult.isDefaultProfile()==true)
                      {
                          group.setName(rowResult.getName()+"_Default");
                      }else
                      {
                          group.setName(rowResult.getName());
                      }
                      //TODO : NEED TO FILL FIXTURE COUNT for PROFILE
                      /*
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
                      */
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
        session.createQuery("Update ProfileGroups set displayProfile = :displayProfile where id = :id")
                .setBoolean("displayProfile", visibility)
                .setLong("id", groupId).executeUpdate();
    }
    public Short getMaxProfileNo(Long tenantId,Long emInstanceId) {
        Integer profileNo = 0;
          try {
            Session s = getSession();
            String hsql=null;
            Query query =null;
            if(emInstanceId==null && tenantId==null)  //Regular USER
            {
               hsql ="select pr_no from generate_series("+minProfileNo+","+UEMMaxProfileNo+") as pr_no left join (select * from profile_groups where id not in (select uem_profile_id from em_profile_mapping)) pg on"+ 
               " pr_no=pg.profile_no where pg.profile_no is NULL limit 1";
            }else if(emInstanceId!=null) // Profiles to be Pushed to EM from  UEM - In this case profiles will be created with upper limit of 255 profiles per EM instance basis
            {
               hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join em_profile_mapping g on g.em_profile_no = pr_no and g.em_id="+ emInstanceId +" where g.em_profile_no is NULL limit 1";
            }else  if(tenantId!=null)   //Tenant USER
            {
                hsql ="select pr_no from generate_series("+minProfileNo+","+MaxProfileNo+") as pr_no left join profile_groups g on g.profile_no = pr_no and g.tenant_id ="+ tenantId +" where g.profile_no is NULL limit 1";
            }
            query  = s.createSQLQuery(hsql);
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
    public List<ProfileGroups> loadAllDerivedProfile(Long groupId) {
        List<ProfileGroups> derivedProfileList = null;
        String hsql = "from ProfileGroups g where g.derivedFromGroup.id=? order by g.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, groupId);
        derivedProfileList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(derivedProfileList)) {
            return derivedProfileList;
        }
        return null;
    }
    public class ProfileComparator implements Comparator<ProfileGroups>{
        @Override
        public int compare(ProfileGroups g1, ProfileGroups g2) {
            return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
        }
    }
    public class TemplateComparator implements Comparator<ProfileTemplate>{
	    @Override
	    public int compare(ProfileTemplate pt1, ProfileTemplate pt2) {
	        return pt1.getName().toLowerCase().compareTo(pt2.getName().toLowerCase());
	    }
	}

	public List<ProfileGroups> loadAllDownloadedProfilesByTemplateId(
			long templateId) {
		List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
		String hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileNo>0 and g.globalCreatedProfile='false' and g.profileTemplate.id = ? order by g.name";
		Query q = getSession().createQuery(hsql.toString());
		q.setParameter(0, templateId);
		List<ProfileGroups> resultList = q.list();
		if (resultList != null && !resultList.isEmpty()) {
			groupsList = resultList;
		}
		if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
			return groupsList;
		}
		return null;
	}
	public List<ProfileGroups> loadAllGlobalDefaultProfiles() {
		List<ProfileGroups> groupsList = new ArrayList<ProfileGroups>();
		String hsql = "from ProfileGroups g where g.displayProfile='true' and g.profileNo>0 and g.globalCreatedProfile='true' order by g.name";
		Query q = getSession().createQuery(hsql.toString());
		List<ProfileGroups> resultList = q.list();
		if (resultList != null && !resultList.isEmpty()) {
			Iterator<ProfileGroups> it = resultList.iterator();
			while (it.hasNext()) {
				ProfileGroups rowResult = it.next();
				ProfileGroups group = new ProfileGroups();
				group.setCompany(rowResult.getCompany());
				group.setDerivedFromGroup(rowResult.getDerivedFromGroup());
				group.setDefaultProfile(rowResult.isDefaultProfile());
				group.setDisplayProfile(rowResult.isDisplayProfile());
				group.setId(rowResult.getId());
				group.setProfileHandler(rowResult.getProfileHandler());
				group.setProfileNo(rowResult.getProfileNo());
				group.setProfileTemplate(rowResult.getProfileTemplate());
				if (rowResult.isDefaultProfile() == true) {
					group.setName(rowResult.getName() + "_Default");
				} else {
					group.setName(rowResult.getName());
				}
				groupsList.add(group);
			}
		}
		if (!ArgumentUtils.isNullOrEmpty(groupsList)) {
			return groupsList;
		}
		return null;
	}
}
