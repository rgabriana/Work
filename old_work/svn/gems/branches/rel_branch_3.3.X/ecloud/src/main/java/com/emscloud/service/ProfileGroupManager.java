package com.emscloud.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.UemAdapter;
import com.emscloud.communication.enlightedUrls.EmFixtureUrls;
import com.emscloud.communication.vos.EMProfile;
import com.emscloud.communication.vos.Fixture;
import com.emscloud.dao.FacilityDao;
import com.emscloud.dao.ProfileDao;
import com.emscloud.dao.ProfileGroupDao;
import com.emscloud.dao.ProfileSyncStatusDao;
import com.emscloud.dao.ProfileTemplateDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.Profile;
import com.emscloud.model.ProfileConfiguration;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileHandler;
import com.emscloud.model.ProfileSyncStatus;
import com.emscloud.model.ProfileTemplate;
import com.emscloud.model.Users;
import com.emscloud.model.WeekDay;
import com.emscloud.server.ServerConstants;
import com.emscloud.types.FacilityType;
import com.emscloud.types.RoleType;
import com.emscloud.util.tree.TreeNode;
import com.emscloud.vo.EmProfileList;
import com.sun.jersey.api.client.GenericType;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Service("profileGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileGroupManager {

	Map<Long, TreeNode<FacilityType>> profileTreeMap = new HashMap<Long, TreeNode<FacilityType>>();
	@Resource
	private ProfileGroupDao profileGroupDao;
	@Resource
	private UserManager userManager;
	@Resource
	private ProfileDao profileDao;
	@Resource
	private ProfileTemplateDao profileTemplateDao;
	@Resource
    UemAdapter uemAdapter;
    @Resource
    FacilityEmMappingManager facilityEmMappingManager;
    @Resource
    CommunicationUtils communicationUtils;
    @Resource
    FacilityManager facilityManager;
    @Resource
    ProfileSyncStatusDao profileSyncStatusDao;
    @Resource(name="emProfileMappingManager")
    EmProfileMappingManager emProfileMappingManager;
    @Resource
	FacilityDao facilityDao;
    @Resource(name="profileSyncManager")
    ProfileSyncManager profileSyncManager;
    @Resource(name="emInstanceManager")
    EmInstanceManager emInstanceManager;
    @Resource
	FacilityTreeManager facilityTreeManager;
    @Resource
    ProfileTemplateManager profileTemplateManager;
	@Resource
	private GlemManager glemManager;
    static final Logger logger = Logger.getLogger("CloudProfile");
	/**
     * Load all group
     * 
     * @return com.ems.model.Group collection
     */
    public List<ProfileGroups> loadAllGroups() {
        return profileGroupDao.loadAllGroups();
    }
    
    
    /**
     * Load all groups  With Default Groups attached '_Default' at end. 
     * 
     * @return com.ems.model.Group collection
     */
    public List<ProfileGroups> loadAllGroupsIncludingDefault() {
        return profileGroupDao.loadAllGroupsIncludingDefault();
    }
    
    
    /**
     * Load all group except Deafult profile
     * 
     * @return com.ems.model.Group collection
     */
    public List<ProfileGroups> loadAllGroupsExceptDeafult() {
        return profileGroupDao.loadAllGroupsExceptDeafult();
    }
    public ProfileGroups getGroupById(Long id) {
        return profileGroupDao.getGroupById(id);
    }
    public ProfileGroups getGroupByName(String sGroupname) {
        return profileGroupDao.getGroupByName(sGroupname);
    }
    public ProfileGroups editName(ProfileGroups groups) {
        return profileGroupDao.editName(groups);
    }
    public void updateGroupProfile(ProfileHandler profileHandler, Long groupId) {
        ProfileHandler copyProfileHandler = null;
        copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        //TODO: NEED TO UPDATE profile on the EM manager
        //DeviceServiceImpl.getInstance().updateGroupProfile(copyProfileHandler, groupId);
    }

    public void updateAdvanceGroupProfile(ProfileHandler profileHandler, Long groupId) {
        ProfileHandler copyProfileHandler = null;
        copyProfileHandler = profileDao.saveProfileHandler(profileHandler);
        //TODO: NEED TO UPDATE profile on the EM manager
        //DeviceServiceImpl.getInstance().updateAdvanceGroupProfile(copyProfileHandler, groupId);
    }
    public List<ProfileGroups> loadAllProfileTemplateById(long templateId, Long tenantId,boolean globalFlag) {
        return profileGroupDao.loadAllProfileTemplateById(templateId,tenantId,globalFlag);
    }
    public TreeNode<FacilityType> loadProfileHierarchy(boolean visibilityCheck) {

      	TreeNode<FacilityType> profileHierachy =null;
      	if(visibilityCheck)
      	{
      		profileHierachy = profileGroupDao.loadProfileHierarchy(visibilityCheck);
      	}
      	else
      	{
      		profileHierachy = profileGroupDao.loadFilterProfileHierarchy();
      	}
        profileTreeMap.put(0L, profileHierachy);
        return profileHierachy;
    }

	public TreeNode<FacilityType> loadGlemProfileHierarchy(boolean visibilityCheck, long customerId) {
		TreeNode<FacilityType> facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchyByCustomerId(customerId);
		Facility facility = facilityDao.loadFacilityTreeByCustomer(customerId);
		TreeNode<FacilityType> rootNode = createNode(facility.getId(), facility.getName(), FacilityType.getFacilityType(facility.getType()),0l);
		TreeNode<FacilityType> globalDefaultProfileHierarchy = profileGroupDao.loadGlemDefaultProfileHierarchy(visibilityCheck);
		rootNode.addTreeNode(globalDefaultProfileHierarchy);
		// Load All Campuses
		List<TreeNode<FacilityType>> campusHirarchyList = facilityTreeHierarchy.getTreeNodeList();
		if(campusHirarchyList!=null)
		{
			Iterator<TreeNode<FacilityType>> campusListItr = campusHirarchyList.iterator();
			while (campusListItr.hasNext()) {
				TreeNode<FacilityType> campusObj = campusListItr.next();
				TreeNode<FacilityType> campusNode = createNode(campusObj.getNodeId(), campusObj.getName(), campusObj.getNodeType(),rootNode.getNodeId());
				rootNode.addTreeNode(campusNode);
				// Load All Building
				List<TreeNode<FacilityType>> buildingHieraychyList = campusObj.getTreeNodeList();
				if(buildingHieraychyList!=null)
				{
					Iterator<TreeNode<FacilityType>> buildingListItr = buildingHieraychyList.iterator();
					while (buildingListItr.hasNext()) {
						TreeNode<FacilityType> buildingObj = buildingListItr.next();
						TreeNode<FacilityType> buildingNode = createNode(buildingObj.getNodeId(), buildingObj.getName(), buildingObj.getNodeType(),campusNode.getNodeId());
						campusNode.addTreeNode(buildingNode);
						// Load all FLoor mapped to facility by building Id
						List<TreeNode<FacilityType>> floorHierarchyList = buildingObj
								.getTreeNodeList();
						if(floorHierarchyList!=null)
						{
							Iterator<TreeNode<FacilityType>> floorListItr = floorHierarchyList
									.iterator();
							ProfileTemplate profileTemplate = null;
							ProfileTemplate OldTemplateName = null;
							TreeNode<FacilityType> templateNode = null;
							TreeNode<FacilityType> profileNode =null;
							ProfileGroups profileGroups = null;
							ProfileGroups oldProfileGroups = null;
							HashMap<String, TreeNode<FacilityType>> profileTemplateMap = new HashMap<String, TreeNode<FacilityType>>();
							HashMap<String, TreeNode<FacilityType>> profileMap = new HashMap<String, TreeNode<FacilityType>>();
							boolean existingNode = false;
							boolean existingProfile = false;
							
							while (floorListItr.hasNext()) {
								
								TreeNode<FacilityType> floorObj = floorListItr.next();
								if (!floorObj.isMapped()) {
									continue;
								}
								// Load All profiles by Building Id
								FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(floorObj.getNodeId());
								List<EmProfileMapping> emProfileMapping = emProfileMappingManager.getEmProfileMappingByEmId(facilityEmMapping.getEmId());
								if(emProfileMapping!=null)
								{
									Iterator<EmProfileMapping> profileItr = emProfileMapping.iterator();
									while (profileItr.hasNext()) {
										existingNode = false;
										existingProfile=false;
										EmProfileMapping emProfileMappingObj = profileItr.next();
										ProfileGroups profileObj = profileGroupDao.getGroupById(emProfileMappingObj.getUemProfileId());
										profileTemplate = profileObj.getProfileTemplate();
										if(profileTemplate.isDisplayTemplate()==visibilityCheck)
										{
											TreeNode<FacilityType> value = profileTemplateMap.get(profileTemplate.getName());
											if (value == null) {
												templateNode = createNode(profileTemplate.getId(), profileTemplate.getName(), FacilityType.PROFILETEMPLATE,buildingNode.getNodeId());
												profileTemplateMap.put(profileTemplate.getName(),templateNode);
											}else
											{
												templateNode = value;
												existingNode = true;
											}
											profileGroups = profileObj;
											if(profileObj.isDisplayProfile() == visibilityCheck)
											{
												TreeNode<FacilityType> pvalue = profileMap.get(profileGroups.getName());
												if(pvalue==null)
												{
													profileNode = createNode(profileObj.getId(), profileObj.getName(), FacilityType.PROFILEGROUP,templateNode.getNodeId());
													profileMap.put(profileGroups.getName(),profileNode);
												}else
												{
													profileNode = pvalue;
													existingProfile= true;
												}
												if(templateNode!=null && (oldProfileGroups==null || !(oldProfileGroups.getName().equalsIgnoreCase(profileObj.getName())))  && existingProfile==false)
												templateNode.addTreeNode(profileNode);
												oldProfileGroups = profileGroups;
											}
											if ((OldTemplateName == null	|| !(OldTemplateName.getName().equalsIgnoreCase(profileObj.getProfileTemplate().getName()))) && existingNode==false)
												buildingNode.addTreeNode(templateNode);
											OldTemplateName = profileTemplate;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		profileTreeMap.put(0L, rootNode);
		return rootNode;
    }
  
    private TreeNode<FacilityType> createNode(Long id, String name, FacilityType type,Long parentNodeId) {
		  TreeNode<FacilityType> node = new TreeNode<FacilityType>();
		  node.setName(name);
		  node.setNodeId(id);
		  node.setParentNodeId(parentNodeId);
		  node.setNodeType(type);
		  return node;
	}

    public TreeNode<FacilityType> loadProfileHierarchyForUser(long currentUserId,boolean visibility) {
        // Let's get the current user
        Users user = userManager.loadUserByUserId(currentUserId);

        // If user role is admin, return the company hierarchy
        if (user.getRoleType().equalsIgnoreCase(RoleType.Admin.getName())) {
            return loadProfileHierarchy(visibility);
        }

        //TODO : SUPPORT FOR TENANT PENDING
        
        // Find the effective top hierarchy
        TreeNode<FacilityType> topHierarchy = null;

        // If tenant admin, return the tenant hierarchy
        
        /*
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
        */
        return topHierarchy;
    }
    
    //@InvalidateProfileTreeCache 
    public String updateGroupVisibility(String[] selectedFacility){
        //List<Object> fixtureCountList =  profileTemplateDao.getFixtureCountForTemplate();
        String  fitureAssociatedProfiles ="";
        StringBuilder groupVisibilityLog = new StringBuilder("::Profile Group Visibility::");
        StringBuilder templateVisibilityLog = new StringBuilder("::Profile Template Visibility::");
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
            if ("profiletemplate".equalsIgnoreCase(facilityType)) {            
               // List<Object> list = profileTemplateDao.getFixtureCountForProfileTemplate(facilityId);
               //TODO: NEED TO SUPPORT FIXTUTE COUNT FOR TEMPLATE
                List<Object> list = new ArrayList<Object>();
                if(!(visibilityFlag==false && list.size()>=1))  
                {
                    profileTemplateDao.updateTemplateVisibility(facilityId, visibilityFlag);
                    templateVisibilityLog.append("Template , name : " + profileTemplateDao.getProfileTemplateById(facilityId).getName() + ", id : " + facilityId + " Flag :"+visibilityFlag+"::");                  
                }
            } else if ("profilegroup".equalsIgnoreCase(facilityType)) {
               //TODO: NEED TO SUPPORT FIXTUTE COUNT FOR PROFILE
               // List<Object> list = profileGroupDao.getFixtureCountForProfile(facilityId);
                ProfileGroups group = profileGroupDao.getGroupById(facilityId);
                List<Object> list = new ArrayList<Object>();
                if(!(visibilityFlag==false && list.size()>=1)) //
                {
                    profileGroupDao.updateGroupVisibility(facilityId,visibilityFlag);
                    groupVisibilityLog.append(" Group , name : " + profileGroupDao.getGroupById(facilityId).getName() + " id : " + facilityId + " Flag :"+visibilityFlag+"::");                    
                }
                else
                {
                    if(group.isDefaultProfile()==true)
                    {
                        fitureAssociatedProfiles+=group.getName()+"_Default"+",";
                    }
                    else
                    {
                        fitureAssociatedProfiles+=group.getName()+",";
                    }
                    //Reset all profile template where fixture is associated with profile.
                    profileTemplateDao.updateTemplateVisibility(group.getProfileTemplate().getId(), true);
                }
            }
        }
        //userAuditLoggerUtil.log("" + groupVisibilityLog, UserAuditActionType.Profile_Update.getName());
        //userAuditLoggerUtil.log("" + templateVisibilityLog, UserAuditActionType.Profile_Update.getName());
        fitureAssociatedProfiles = fitureAssociatedProfiles.replaceAll(",$", "");
        return fitureAssociatedProfiles;
    }
    public Short getMaxProfileNo(Long tenantId,Long emInstanceId) {
     	Short profileNo = null;
		if (emInstanceId == null) {
			profileNo = profileGroupDao.getMaxProfileNo(tenantId, emInstanceId);
		} else {
			// NOTE : Instead of fetching profile_no from locally through
			// em_profile_mapping table, fetch the exact profile_no available
			// from EM through exposed web service
			try
			{
			EmInstance emInstance = emInstanceManager
					.getEmInstance(emInstanceId);
			ResponseWrapper<com.emscloud.communication.vos.Response> response = uemAdapter
					.executeGet(emInstance, glemManager.getAdapter()
							.getContextUrl() + EmFixtureUrls.getEMMaxProfileNo,
							MediaType.APPLICATION_XML,
							com.emscloud.communication.vos.Response.class);
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				profileNo = Short.parseShort(response.getItems().getMsg());
			} else {
				if(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
				{
					logger.error("It seems that getmaxprofileno() service is not present on EM. Please check EM is upgraded with 3.1.10 and above."
							+ response.getEm().getMacId() + " reason :- "
							+ response.getStatus());
				}
				logger.error("Not able to get next available Profile_no from EM:- "
						+ response.getEm().getMacId() + " reason :- "
						+ response.getStatus());
			}
			}catch(Exception e)
			{
				logger.error("Not able to get next available Profile_no from EM");
			}
		}
		return profileNo;
    }
    public List<Object> getProfilesCount(Long pid) {
        //TODO: NEED TO SUPPORT FIXTURE ASSOCIATED WITH PROFILE
        List<Object> object = new ArrayList<Object>();
        return object;//profileGroupDao.getFixtureCountForProfile(pid);
    }
    
    public String getFixtureCountByProfileGroupId(Long pgId){
    	
		long result = 0L;
		ProfileGroups profileGroup = getGroupById(pgId);
		HashMap<EmInstance, Long> emProfileGroupMap = communicationUtils.getEmProfileGroupMap(profileGroup);
		
		for(Entry<EmInstance, Long> e : emProfileGroupMap.entrySet()) {
	        EmInstance emInstance = e.getKey();
	        Long emGroupId = e.getValue();
	        
	        ResponseWrapper<com.emscloud.communication.vos.Response> response = uemAdapter
			.executeGet(emInstance, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getFixtureCountByProfileGroupId+emGroupId, MediaType.APPLICATION_XML,com.emscloud.communication.vos.Response.class);
	        
	        if (response.getStatus()== Response.Status.OK
					.getStatusCode()) {
				result += Long.parseLong(response.getItems().getMsg());
			} else {
				logger.error("Not able to get Fixture Count Details from EM:- "
						+ response.getEm().getMacId()
						+ " reason :- " + response.getStatus());
			}
	                    
	    }
		return String.valueOf(result);
    }
    
    public com.emscloud.communication.vos.Response bulkProfileAssign(Long profilegroupid, Long pid, List<Fixture> fixtureList) {
    	com.emscloud.communication.vos.Response result = new com.emscloud.communication.vos.Response();
		try {
			Facility floor = getFacility(pid);
			if (FacilityType.FLOOR.ordinal() == floor.getType()) {
				ProfileGroups profileGroups =  profileGroupDao.getGroupById(profilegroupid);
				String profileGroupName="";
				Long emGroupId;
				if(profileGroups.isDefaultProfile())
				{
				    profileGroupName = profileGroups.getName()+"_Default";
				    emGroupId = profilegroupid;
				    result = assignProfileToFixtures(emGroupId, profileGroupName,floor,fixtureList);
				    if(result.getStatus()>0)
                    {
                        logger.info("Default PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " assigned to Fixtures successfully");
                    }
				}else
				{
					logger.info("Profile Group Id passed is not a default profile. Given Profile seems to be custom profile.");
					
					// Now CHeck whether the Given Profile ID present in em_profile_mapping table
		            // If Present in em_profile_mapping table then call sync profile otherwise call push new profile profile
		            // work flow.
					FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(floor.getId());
		            EmProfileMapping emProfileMapping = emProfileMappingManager.getEmProfileMappingByUemProfileIdAndEMId(facilityEmMapping.getEmId(), profilegroupid);
		            // PROFILE ALREADY PRESENT ON EM AND ASSIGN PROFILE TO FIXTURE
		            if (emProfileMapping != null)
		            {
		                emProfileMapping.setSyncStatus(1);
		                emProfileMappingManager.saveOrUpdate(emProfileMapping);
		                com.emscloud.communication.vos.Response response = profileSyncManager.syncProfileGroupsToEM();
		                if (response.getStatus() == 0)
		                {
		                    logger.info("Existing PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " marked for push to Fixtures");
		                    profilegroupid = emProfileMapping.getEmGroupId();
		                    
		                    String oriName = profileGroups.getName();
		                    //String[] tempArr = oriName.split("_EM");
		                    //oriName = tempArr[0];
		                    profileGroupName = oriName;
		                    
		                    result = assignProfileToFixtures(profilegroupid, profileGroupName,floor,fixtureList);
		                    
		                    if(result.getStatus()>0)
		                    {
		                        logger.info("PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " assigned to Fixtures successfully");
		                    }
		                }
		                else
		                {
		                	logger.info("Existing PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " assigned to Fixtures Failed. Please try after some time.");
		                }
		            } else
		            {
		               // PROFILE NEED TO BE CREATED FIRST ON EM AND THEN ASSIGN PROFILE
		                EMProfile eMProfile = profileSyncManager.PushNewProfileToEM(profilegroupid, pid);
		                if (eMProfile!=null)
		                {
		                    logger.info("New PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId() + " marked for push to Fixtures");
		                    profileGroupName = profileGroups.getName();
		                    profilegroupid = eMProfile.getId();
		                    result = assignProfileToFixtures(profilegroupid, profileGroupName, floor, fixtureList);
		                    if(result.getStatus()>0)
		                    {
		                        logger.info("New PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " assigned to Fixtures successfully");
		                    }
		                }else
		                {
		                	logger.info("New PROFILE ASSIGNMENT : Profile Group Id : " + profileGroups.getId()+ " assigned to Fixtures Failed. Please try after some time.");
		                }
		            }
				}
			} else 
			{
				logger.error("Floor Id passed is not corespond to type "
						+ FacilityType.FLOOR.getName() + " but to "
						+ floor.getType().toString());
			}
		} catch (Exception e) {
			logger.error("Bulk Profile assignment failed " + e.getMessage());
		}
		return result;
    }
    
    private com.emscloud.communication.vos.Response assignProfileToFixtures(Long emGroupId, String profileGroupName, Facility floor, List<Fixture> fixtureList)
    {
    	com.emscloud.communication.vos.Response result = null;
        // Now Push the Profile to Fixture
       
    	try
    	{
        List<EmInstance> emList = communicationUtils.getEmMap(floor);
      
        List<ResponseWrapper<com.emscloud.communication.vos.Response>> response = uemAdapter
                .executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.bulkassignProfileToFixtures
                        + profileGroupName + "/" + emGroupId, MediaType.APPLICATION_XML,
                        MediaType.APPLICATION_XML,
                        com.emscloud.communication.vos.Response.class,
                        communicationUtils.convertModelListToString(
                                fixtureList, Fixture.class));
        if (response.get(0).getStatus() == Response.Status.OK.getStatusCode()) 
        {
            result = response.get(0).getItems();
        } else 
        {
            logger.error("Assigning Profile to Fixtures"
                    + " command failed from EM:- "
                    + response.get(0).getEm().getMacId()
                    + " reason :- " + response.get(0).getStatus());
        }
    	}catch(Exception e)
    	{
    		logger.error("Assigning Profile to Fixtures command failed from EM");
    	}
        return result;
    }
    public int deleteProfile(long profileId) {
        int status=1;
        try
        {
            ProfileGroups group = (ProfileGroups) profileGroupDao.loadObject(ProfileGroups.class, profileId);
            
            //If this groups has children associated then make derived_from_group = null, so that child profile wont throw exception about the profile which is not present.
            List<ProfileGroups> derivedProfileList = profileGroupDao.loadAllDerivedProfile(group.getId());
            if (derivedProfileList != null && !derivedProfileList.isEmpty())
             {
                  Iterator<ProfileGroups> it = derivedProfileList.iterator();
                  while (it.hasNext()) {
                      ProfileGroups rowResult = it.next();
                      rowResult.setDerivedFromGroup(null);
                      profileGroupDao.saveObject(rowResult);
                  }
             }
            
            ProfileHandler profileHandler =  (ProfileHandler) profileGroupDao.loadObject(ProfileHandler.class, group.getProfileHandler().getId());
            
            Profile morningProfile =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getMorningProfile().getId());
            Profile dayProfile =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getDayProfile().getId());
            Profile eveningProfile =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getEveningProfile().getId());
            Profile nightProfile =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getNightProfile().getId());
            
            Profile morningProfileWeekend =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getMorningProfileWeekEnd().getId());
            Profile dayProfileWeekend =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getDayProfileWeekEnd().getId());
            Profile eveningProfileWeekend =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getEveningProfileWeekEnd().getId());
            Profile nightProfileWeekend =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getNightProfileWeekEnd().getId());
            
            Profile morningProfileHoliday =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getMorningProfileHoliday().getId());
            Profile dayProfileHoliday =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getDayProfileHoliday().getId());
            Profile eveningProfileHoliday =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getEveningProfileHoliday().getId());
            Profile nightProfileHoliday =  (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getNightProfileHoliday().getId());
            
            Profile override5 = (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getOverride5().getId());
            Profile override6 = (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getOverride6().getId());
            Profile override7 = (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getOverride7().getId());
            Profile override8 = (Profile) profileGroupDao.loadObject(Profile.class, profileHandler.getOverride8().getId());
            
            ProfileConfiguration profileConfiguration =  (ProfileConfiguration) profileGroupDao.loadObject(ProfileConfiguration.class, profileHandler.getProfileConfiguration().getId());
            
            List<WeekDay> weekDayList = profileGroupDao.loadAllWeekByProfileConfigurationId(profileConfiguration.getId());
            
            //Remove groups
            profileGroupDao.removeObject(ProfileGroups.class, profileId);
            
            //Remove ProfileHandler
            profileGroupDao.removeObject(ProfileHandler.class, group.getProfileHandler().getId());
            
            //Remove weekday profile
            profileGroupDao.removeObject(Profile.class, morningProfile.getId());
            profileGroupDao.removeObject(Profile.class, dayProfile.getId());
            profileGroupDao.removeObject(Profile.class, eveningProfile.getId());
            profileGroupDao.removeObject(Profile.class, nightProfile.getId());
            
            //Remove weekend profile
            profileGroupDao.removeObject(Profile.class, morningProfileWeekend.getId());
            profileGroupDao.removeObject(Profile.class, dayProfileWeekend.getId());
            profileGroupDao.removeObject(Profile.class, eveningProfileWeekend.getId());
            profileGroupDao.removeObject(Profile.class, nightProfileWeekend.getId());
            
            //Remove Holiday profile
            profileGroupDao.removeObject(Profile.class, morningProfileHoliday.getId());
            profileGroupDao.removeObject(Profile.class, dayProfileHoliday.getId());
            profileGroupDao.removeObject(Profile.class, eveningProfileHoliday.getId());
            profileGroupDao.removeObject(Profile.class, nightProfileHoliday.getId());
            
            profileGroupDao.removeObject(Profile.class, override5.getId());
            profileGroupDao.removeObject(Profile.class, override6.getId());
            profileGroupDao.removeObject(Profile.class, override7.getId());
            profileGroupDao.removeObject(Profile.class, override8.getId());
            
            //Remove Weekday
            if (weekDayList != null && !weekDayList.isEmpty())
             {
                  Iterator<WeekDay> it = weekDayList.iterator();
                  while (it.hasNext()) {
                      WeekDay rowResult = it.next();
                      profileGroupDao.removeObject(WeekDay.class, rowResult.getId());
                  }
             }
            
            //Remove ProfileConfiguration
            profileGroupDao.removeObject(ProfileConfiguration.class, profileHandler.getProfileConfiguration().getId());
            
        } catch (Exception e) {
            status=0;
            e.printStackTrace();
        }
        return status;
    }
    public class TemplateComparator implements Comparator<ProfileTemplate>{
        @Override
        public int compare(ProfileTemplate pt1, ProfileTemplate pt2) {
            return pt1.getName().toLowerCase().compareTo(pt2.getName().toLowerCase());
        }
    }
    public class ProfileComparator implements Comparator<ProfileGroups>{
        @Override
        public int compare(ProfileGroups g1, ProfileGroups g2) {
            return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
        }
    }
    
    public ProfileHandler fetchProfileHandlerById(Long id) {
        return profileDao.fetchProfileHandlerById(id);
     }
    
    public List<EmProfileList> getAllDerivedEMProfiles(Long emId) {
        List<EmProfileList> emDerivedProfilesList = new ArrayList<EmProfileList>();
        // Get List of all Registered Energy Manager
        List<EmInstance> emList = null;
        if(emId.longValue() > 0)
        {
            emList = new ArrayList<EmInstance>();
            EmInstance emInstanceObj = emInstanceManager.getEmInstance(emId.longValue());
            emList.add(emInstanceObj);
        }else
        {
            List<Long> emInstanceIds= facilityEmMappingManager.getDistictMappedEMIdList();
            if(emInstanceIds!=null && emInstanceIds.size()>0)
            {
                emList = new ArrayList<EmInstance>();
                Iterator<Long> rwitr = emInstanceIds.iterator();
                while (rwitr.hasNext()) {
                    Long nextRw = rwitr.next();
                    EmInstance emInstanceObj = emInstanceManager.getEmInstance(nextRw);
                    emList.add(emInstanceObj);
                }
            }else
            {
                Long companyId = (long) 1;
                Facility company = facilityManager.getFacility(companyId);
                emList = communicationUtils.getEmMap(company);
            }
        }
       
        try {
            
            if (emList!=null && emList.size()>0) {
             
                List<ResponseWrapper<List<EMProfile>>> response = uemAdapter
                            .executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getEMProfiles,
                                    "application/xml",new GenericType<List<EMProfile>>(){});
                    // Iterate Over the all the Response Wrapper result
                    Iterator<ResponseWrapper<List<EMProfile>>> rwitr = response.iterator();
                    while (rwitr.hasNext()) {
                        ResponseWrapper<List<EMProfile>> nextRw = rwitr.next();
                        if (nextRw.getStatus() == Response.Status.OK.getStatusCode()) {
                            ArrayList<EMProfile> emProfiles = (ArrayList<EMProfile>) nextRw.getItems();
                            EmProfileList emProfilesList = new EmProfileList();
                            EmInstance em = nextRw.getEm();
                            emProfilesList.setEm(em);
                            emProfilesList.setProfileList(emProfiles);
                            emDerivedProfilesList.add(emProfilesList);
                            
                            ProfileSyncStatus profileSyncStatus = profileSyncStatusDao.getProfileSyncStatusByEMId(em.getId());
                            if(profileSyncStatus==null)
                            {
                                profileSyncStatus= new ProfileSyncStatus();
                                profileSyncStatus.setEmId(em.getId());
                                profileSyncStatusDao.saveOrUpdate(profileSyncStatus);
                            }
                        } else {
                            logger.error("Not able to get Profiles from EM:- "
                                    + nextRw.getEm().getMacId()
                                    + " reason :- " + nextRw.getStatus());
                        }
                    }
            } else {
                logger.error("None of the EM is mapped to UEM Floor. Please Map EM Instance to UEM Floors" );
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return emDerivedProfilesList;
    }
    
    public com.emscloud.communication.vos.Response updateEMProfiles(List<EMProfile> emprofiles,List<EmInstance> emList) {
    	com.emscloud.communication.vos.Response result = null;
        try
        {
	        List<ResponseWrapper<com.emscloud.communication.vos.Response>> response = uemAdapter
	                .executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.updateEMProfiles,MediaType.APPLICATION_XML,MediaType.APPLICATION_XML,com.emscloud.communication.vos.Response.class,communicationUtils.convertModelListToString(emprofiles,EMProfile.class));
	        if (response.get(0).getStatus() == Response.Status.OK
	                .getStatusCode()) {
	            result = response.get(0).getItems();
	        } else {
	            logger.error("Update EM Profiles command failed from UEM:- "
	                    + response.get(0).getEm().getMacId()
	                    + " reason :- " + response.get(0).getStatus());
	        }
        }catch(Exception e)
        {
        	  logger.error("Update EM Profiles command failed from UEM");
        }
        return result;
    }
    
    public Facility getFacility(long id) {
		return facilityDao.getFacility(id);
	}
    public EMProfile pushNewProfileToEM(EMProfile emProfile, List<EmInstance> emList) {
        EMProfile result = null;
        try
        {
	        List<ResponseWrapper<EMProfile>> response = uemAdapter
	                .executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.pushNewEMProfile,MediaType.APPLICATION_XML,MediaType.APPLICATION_XML,EMProfile.class,communicationUtils.convertModelToString(emProfile));
	        if (response.get(0).getStatus() == Response.Status.OK
	                .getStatusCode()) {
	            result = response.get(0).getItems();
	        } else {
	            logger.error("Profile Pushed command failed from UEM:- "
	                    + response.get(0).getEm().getIpAddress()
	                    + " reason :- " + response.get(0).getStatus());
	        }
        }catch(Exception e)
        {
        	  logger.error("Profile Pushed command failed from UEM");
        }
        return result;
        
    }
    
    public String getDisplayProfileName(Long profileId)
    {
    	 ProfileGroups oriProfileGrpObj = getGroupById(profileId);
    	 String profileName = oriProfileGrpObj.getName();
    	 if(profileName.indexOf("_"+ServerConstants.DEFAULT_PROFILE)==-1 && !profileName.equals(ServerConstants.DEFAULT_PROFILE))
         {
         	if(oriProfileGrpObj!=null && oriProfileGrpObj.isDefaultProfile()==true && oriProfileGrpObj.getProfileNo()>0)
         	{
         		profileName=profileName+"_"+ServerConstants.DEFAULT_PROFILE;
         	}
         }
    	return profileName;
    }


	public List<ProfileGroups> loadAllDownloadedProfilesByTemplateId(long templateId) {
		 return profileGroupDao.loadAllDownloadedProfilesByTemplateId(templateId);
	}


	public List<ProfileGroups> loadAllDownloadedProfilesByCampusId(
			long campusId) {
		List<ProfileGroups> profileList = new ArrayList<ProfileGroups>();
		// Load All profiles by Campus Id
		HashMap<Long, ProfileGroups> profileMap = new HashMap<Long, ProfileGroups>();
		List<Facility> buildingList= facilityManager.getChildFacilitiesByFacilityId(campusId);
		if(buildingList!=null && buildingList.size()>0)
		{
			Iterator<Facility> buildings = buildingList.iterator();
			while(buildings.hasNext())
			{
				Facility  buildingObj = buildings.next();
				List<Facility> emLists= facilityManager.getChildFacilitiesByFacilityId(buildingObj.getId());
				if(emLists!=null && emLists.size()>0)
				{
					Iterator<Facility> floors = emLists.iterator();
					while(floors.hasNext())
					{
						Facility floorObj = floors.next();
						FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(floorObj.getId());
						if(facilityEmMapping!=null)
						{
							List<EmProfileMapping> emProfileMapping = emProfileMappingManager.getEmProfileMappingByEmId(facilityEmMapping.getEmId());
							if(emProfileMapping!=null)
							{
								Iterator<EmProfileMapping> profileItr = emProfileMapping.iterator();
								while (profileItr.hasNext()) {
									EmProfileMapping emProfileMappingObj = profileItr.next();
									ProfileGroups profileObj = profileGroupDao.getGroupById(emProfileMappingObj.getUemProfileId());
									ProfileGroups pvalue = profileMap.get(profileObj.getId());
									if(pvalue==null)
									{
										profileList.add(profileObj);
										profileMap.put(profileObj.getId(), profileObj);
									}
								}
							}
						}
					}
				}
			}
		}
		return profileList;
	}


	public List<ProfileGroups> loadAllDownloadedProfilesByBuildingId(
			long buildingId) {
		List<Facility> emLists= facilityManager.getChildFacilitiesByFacilityId(buildingId);
		List<ProfileGroups> profileList = new ArrayList<ProfileGroups>();
		HashMap<Long, ProfileGroups> profileMap = new HashMap<Long, ProfileGroups>();
		// Load All profiles by Building Id
		if(emLists!=null && emLists.size()>0)
		{
			Iterator<Facility> floors = emLists.iterator();
			while(floors.hasNext())
			{
				Facility floorObj = floors.next();
				FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(floorObj.getId());
				if(facilityEmMapping!=null)
				{
					List<EmProfileMapping> emProfileMapping = emProfileMappingManager.getEmProfileMappingByEmId(facilityEmMapping.getEmId());
					
					if(emProfileMapping!=null)
					{
						Iterator<EmProfileMapping> profileItr = emProfileMapping.iterator();
						while (profileItr.hasNext()) {
							EmProfileMapping emProfileMappingObj = profileItr.next();
							ProfileGroups profileObj = profileGroupDao.getGroupById(emProfileMappingObj.getUemProfileId());
							ProfileGroups pvalue = profileMap.get(profileObj.getId());
							if(pvalue==null)
							{
								profileList.add(profileObj);
								profileMap.put(profileObj.getId(), profileObj);
							}
						}
					}
				}
			}
			
		}
		return profileList;
	}

	public List<ProfileGroups> loadAllGlobalDefaultProfiles() {
		return profileGroupDao.loadAllGlobalDefaultProfiles();
	}
}