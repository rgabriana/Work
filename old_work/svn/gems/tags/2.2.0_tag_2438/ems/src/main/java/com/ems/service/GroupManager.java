package com.ems.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.annotaion.InvalidateProfileTreeCache;
import com.ems.dao.GroupDao;
import com.ems.dao.ProfileDao;
import com.ems.dao.ProfileTemplateDao;
import com.ems.model.Fixture;
import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.User;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.util.tree.TreeNode;
import com.ems.vo.AssignFixtureList;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("groupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GroupManager {

    // Added by Nitin
    Map<Long, TreeNode<FacilityType>> profileTreeMap = new HashMap<Long, TreeNode<FacilityType>>();

    @Resource
    private GroupDao groupDao;

    @Resource
    private ProfileDao profileDao;
    
    @Resource
	private ProfileTemplateDao profileTemplateDao;
    
    @Resource
	private UserManager userManager;
    
   
    /**
     * save group details.
     * 
     * @param group
     *            com.ems.model.Group
     */
    public Groups save(Groups group) {
        return (Groups) groupDao.saveObject(group);
    }

    /**
     * update group details.
     * 
     * @param group
     *            com.ems.model.Group
     */
    public Groups update(Groups group) {
        return (Groups) groupDao.saveObject(group);
    }

    /**
     * Load company's group
     * 
     * @param id
     *            company id
     * @return com.ems.model.Group collection
     */
    public List<Groups> loadGroupByCompanyId(Long id) {
        return groupDao.loadGroupByCompanyId(id);
    }

    /**
     * Load all group
     * 
     * @return com.ems.model.Group collection
     */
    public List<Groups> loadAllGroups() {
        return groupDao.loadAllGroups();
    }
    
    /**
     * Load all group except Deafult profile
     * 
     * @return com.ems.model.Group collection
     */
    public List<Groups> loadAllGroupsExceptDeafult() {
        return groupDao.loadAllGroupsExceptDeafult();
    }

    /**
     * Delete Groups details
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
        groupDao.removeObject(Groups.class, id);
    }

    public Groups getGroupById(Long id) {
        return groupDao.getGroupById(id);
    }

    public Long getGroupByProfileAndTenantDetails(byte profileno, long tenantid) {
    	return groupDao.getGroupByProfileAndTenantDetails(profileno, tenantid);
    }
    
    public Groups getGroupByName(String sGroupname) {
        return groupDao.getGroupByName(sGroupname);
    }

    public Groups editName(Groups groups) {
        return groupDao.editName(groups);
    }

    public void updateGroupProfile(ProfileHandler profileHandler, Long groupId) {
        ProfileHandler copyProfileHandler = null;
        copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        /*
    	Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        ProfileHandler copyProfileHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            groupDao.updateGroupProfile(copyProfileHandler, groupId);
        } else {
            copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        }
        */
        DeviceServiceImpl.getInstance().updateGroupProfile(copyProfileHandler, groupId);
    }

    public void updateAdvanceGroupProfile(ProfileHandler profileHandler, Long groupId) {
        ProfileHandler copyProfileHandler = null;
        copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        /*
        Long globalProfileHandlerId = profileDao.getGlobalProfileHandlerId();
        ProfileHandler copyProfileHandler = null;
        if (profileHandler.getId().equals(globalProfileHandlerId)) {
            copyProfileHandler = profileHandler.copy();
            profileDao.saveProfileHandler(copyProfileHandler);
            groupDao.updateGroupProfile(copyProfileHandler, groupId);
        } else {
            copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        }
        */
        DeviceServiceImpl.getInstance().updateAdvanceGroupProfile(copyProfileHandler, groupId);
    }

    // Added by Nitin
    public TreeNode<FacilityType> loadProfileHierarchy(boolean visibilityCheck) {

//        if (profileTreeMap.containsKey(0L)) {
//            return profileTreeMap.get(0L);
//        }
    	TreeNode<FacilityType> profileHierachy =null;
    	if(visibilityCheck)
    	{
    		profileHierachy = groupDao.loadProfileHierarchy(visibilityCheck);
    	}
    	else
    	{
    		profileHierachy = groupDao.loadFilterProfileHierarchy();
    	}
        profileTreeMap.put(0L, profileHierachy);
        return profileHierachy;
    }
    
    public void inValidateProfilesTreeCache() {
    	//System.out.println("CLEARING PROFILE CACHE");
    	profileTreeMap.clear();
	}


    public List<Groups> getDRSensitivity() {
        return groupDao.getDRSensitivity();
    }

    public List<GroupECRecord> getDRSensitivityRecords() {
        return groupDao.getDRSensitivityRecords();
    }

    public void dimFixtures(int groupId, int percentage, int time) {
        DeviceServiceImpl.getInstance().dimFixturesByGroup(groupId, percentage, time);
    }

	public List<Object> getProfilesCount(Long pid) {
		return groupDao.getFixtureCountForProfile(pid);
	}

	public List<Groups> loadAllProfileTemplateById(long templateId) {
		return groupDao.loadAllProfileTemplateById(templateId);
	}
	
	@InvalidateProfileTreeCache	
	public void updateGroupVisibility(String[] selectedFacility){
		List<Object> fixtureCountList =  profileTemplateDao.getFixtureCountForTemplate();
		 
		for (String facility : selectedFacility) {
			String[] facilityDetail = facility.split("_");
			String facilityType = facilityDetail[0];
			Long facilityId = Long.parseLong(facilityDetail[1]);
			String visibilityStr = facilityDetail[2];
			boolean visibilityFlag = true;
			if("t".equalsIgnoreCase(visibilityStr))
			{
				visibilityFlag =true;
			}else if("f".equalsIgnoreCase(visibilityStr))
			{
				visibilityFlag =false;
			}
			if ("template".equalsIgnoreCase(facilityType)) {			
				List<Object> list = profileTemplateDao.getFixtureCountForProfileTemplate(facilityId);
				if(!(visibilityFlag==false && list.size()>=1))					
				profileTemplateDao.updateTemplateVisibility(facilityId, visibilityFlag);
			} else if ("group".equalsIgnoreCase(facilityType)) {
				List<Object> list = groupDao.getFixtureCountForProfile(facilityId);
				if(!(visibilityFlag==false && list.size()>=1))
				groupDao.updateGroupVisibility(facilityId,visibilityFlag);
			}
		}
	}

	public Byte getMaxProfileNo() {
		return groupDao.getMaxProfileNo();
	}

	public TreeNode<FacilityType> loadProfileHierarchyForUser(long currentUserId,boolean visibility) {
		// Let's get the current user
		User user = userManager.loadUserById(currentUserId);

		// If user role is admin, return the company hierarchy
		if (user.getRole().getRoleType() == RoleType.Admin) {
			return loadProfileHierarchy(visibility);
		}

		// Find the effective top hierarchy
		TreeNode<FacilityType> topHierarchy = null;

		// If tenant admin, return the tenant hierarchy
		if (user.getRole().getRoleType() == RoleType.TenantAdmin) {
			topHierarchy = loadTenantHierarchy(currentUserId, user.getTenant().getId(),visibility);
		}

		if (user.getRole().getRoleType() == RoleType.FacilitiesAdmin || user.getRole().getRoleType() == RoleType.Auditor) {
			topHierarchy = loadProfileHierarchy(visibility);
		} else if (user.getRole().getRoleType() == RoleType.Employee) {
			if (user.getTenant() == null) {
				topHierarchy = loadProfileHierarchy(visibility);
			} else {
				topHierarchy = loadTenantHierarchy(currentUserId,user.getTenant().getId(),visibility);
			}
		}
		return topHierarchy;
	}
	public TreeNode<FacilityType> loadTenantHierarchy(long currentUserId,long tenantId,boolean visibility) {
		// Let's get the current user
		User user = userManager.loadUserById(currentUserId);
		TreeNode<FacilityType> topHierarchy = null;
		if(visibility==true)
			topHierarchy = loadTenantProfilesHierarchy(user.getTenant().getId(),visibility);
		else
			topHierarchy = loadTenantFilterProfilesHierarchy(user.getTenant().getId(),visibility);
		return topHierarchy;
	}
	public TreeNode<FacilityType> loadTenantProfilesHierarchy(long tenantId,boolean visibility) {
		  TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	      rootNode.setNodeId("0");
	      rootNode.setName("Template");
	      rootNode.setNodeType(FacilityType.TEMPLATE);

	      //Get Profile Templates
	      List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
	        int displayCount = 0;
	        if (profileTemplateList != null) {
	            for (ProfileTemplate profileTemplate : profileTemplateList) {
	            	if (profileTemplate.isDisplayTemplate()==visibility)
	            	{
		            	TreeNode<FacilityType> templateNode = new TreeNode<FacilityType>();
		                templateNode.setNodeId(profileTemplate.getId().toString());
		                templateNode.setName(profileTemplate.getName());
		                templateNode.setNodeType(FacilityType.TEMPLATE);
		                displayCount = 0;
		                //Get Profiles
		                List<Groups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
		                Collections.sort(profilesList, new ProfileComparator());
		                for (Groups group : profilesList) {
		                	if((group.isDisplayProfile()==visibility) && group.getTenant()!=null && group.getTenant().getId()==tenantId)
		                	{
		                		 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
			                	 profileNode.setNodeId(group.getId().toString());
			                	 profileNode.setName(group.getName());
			                	 profileNode.setNodeType(FacilityType.GROUP);
			                     templateNode.addTreeNode(profileNode);
			                     displayCount++;
		                	}else
		                	{
		                		if ((group.isDisplayProfile()==visibility) && (!group.getName().equalsIgnoreCase("default"))) {
				                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
				                	 profileNode.setNodeId(group.getId().toString());
				                	 profileNode.setName(group.getName());
				                	 profileNode.setNodeType(FacilityType.GROUP);
				                     templateNode.addTreeNode(profileNode);
				                     displayCount++;
			                	}
		                	}
		                	
		                	
		                }
		                if (displayCount>0 && !templateNode.getName().equalsIgnoreCase("default"))
		                rootNode.addTreeNode(templateNode);
	            	}
	            }
	        }
	        profileTreeMap.put(0L, rootNode);
	        return rootNode;
	}
	// This tree list will return profile Tree hierarchy with out any visibility check of template/profiles - Used in FILTER Profile Tree
	public TreeNode<FacilityType> loadTenantFilterProfilesHierarchy(long tenantId,boolean visibility) {
		  TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	      rootNode.setNodeId("0");
	      rootNode.setName("Template");
	      rootNode.setNodeType(FacilityType.TEMPLATE);

	      //Get Profile Templates
	      List<ProfileTemplate> profileTemplateList = profileTemplateDao.loadAllProfileTemplate();
	        int displayCount = 0;
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
		                displayCount = 0;
		                //Get Profiles
		                List<Groups> profilesList = profileTemplate.getProfilesList(profileTemplate.getProfiles());
		                Collections.sort(profilesList, new ProfileComparator());
		                for (Groups group : profilesList) {
		                	if(group.getTenant()!=null && group.getTenant().getId()==tenantId)
		                	{
		                		 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
			                	 profileNode.setNodeId(group.getId().toString());
			                	 profileNode.setName(group.getName());
			                	 profileNode.setNodeType(FacilityType.GROUP);
			                     templateNode.addTreeNode(profileNode);
			                     if(group.isDisplayProfile())
			                    	 profileNode.setSelected(true);
				                 else
				                	profileNode.setSelected(false);
			                     displayCount++;
		                	}else
		                	{
		                		if ((!group.getName().equalsIgnoreCase("default"))) {
				                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
				                	 profileNode.setNodeId(group.getId().toString());
				                	 profileNode.setName(group.getName());
				                	 profileNode.setNodeType(FacilityType.GROUP);
				                     templateNode.addTreeNode(profileNode);
				                     if(group.isDisplayProfile())
				                    	 profileNode.setSelected(true);
					                 else
					                	profileNode.setSelected(false);
				                     displayCount++;
			                	}
		                	}
		                }
		                if (displayCount>0 && !templateNode.getName().equalsIgnoreCase("default"))
			                rootNode.addTreeNode(templateNode);
	            }
	        }
	        profileTreeMap.put(0L, rootNode);
	        return rootNode;
	}
	
	public class ProfileComparator implements Comparator<Groups>{
	        @Override
	        public int compare(Groups g1, Groups g2) {
	            return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
	        }
	}
	public List<Groups> loadAllProfileForTenantByTemplateId(Long templateId,Long tenantId) {
		return groupDao.loadAllProfileForTenantByTemplateId(templateId,tenantId);
	}

	public List<Groups> loadAllGroupsForTenantUser(Long tenantId) {
		  return groupDao.loadAllGroupsForTenantUser(tenantId);
	}
	public List loadFixtureList(Long pid, String selectedfixtures ,int offset, int limit,AssignFixtureList returnList) {
		List result = null;
		result = groupDao.loadFixtureList(pid,selectedfixtures, offset, limit,returnList);
		return result;
	}

	public int deleteProfile(long profileId) {
		int status=1;
		try
		{
			groupDao.removeObject(Groups.class, profileId);
		} catch (Exception e) {
			status=0;
			e.printStackTrace();
		}
		return status;
	}
}
