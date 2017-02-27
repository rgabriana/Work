package com.ems.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.annotaion.InvalidateProfileTreeCache;
import com.ems.dao.GroupDao;
import com.ems.model.Company;
import com.ems.model.GroupECRecord;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.User;
import com.ems.model.WeekDay;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.types.FacilityType;
import com.ems.types.ProfileOverrideType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.vo.AssignFixtureList;
import com.ems.vo.EMProfile;
import com.ems.ws.util.Response;

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
	UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource
    private GroupDao groupDao;
    
    @Resource
	private UserManager userManager;
    
    @Resource
    private ProfileManager profileManager; 
    @Resource
    ProfileTemplateManager profileTemplateManager;
    @Resource
    CompanyManager companyManager;
    @Resource
    MetaDataManager metaDataManager;
    static final Logger logger = Logger.getLogger("ProfileLogger");
    
   
    /**
     * save group details.
     * 
     * @param group
     *            com.ems.model.Group
     */
    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", key="#group.id")	})
    public Groups save(Groups group) {
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

    @Cacheable(value="group_id", key="#id")
    public Groups getGroupById(Long id) {
        return groupDao.getGroupById(id);
    }

    public Long getGroupByProfileAndTenantDetails(byte profileno, Long tenantid) {
    	return groupDao.getGroupByProfileAndTenantDetails(profileno, tenantid);
    }
    
    public Groups getGroupByName(String sGroupname) {
        return groupDao.getGroupByName(sGroupname);
    }
    
    public Groups getGroupByProfileNumber(short number) {
        return groupDao.getGroupByProfileNumber(number);
    }

    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", allEntries= true)	})
    public Groups editName(Groups groups) {
        return groupDao.editName(groups);
    }

    // Added by Nitin
    public TreeNode<FacilityType> loadProfileHierarchy(boolean visibilityCheck) {
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
    	profileTreeMap.clear();
	}


    public List<Groups> getDRSensitivity() {
        return groupDao.getDRSensitivity();
    }

    public List<GroupECRecord> getDRSensitivityRecords() {
        return groupDao.getDRSensitivityRecords();
    }

    //this is relative dimming
    public void dimFixtures(int groupId, int percentage, int time) {
        DeviceServiceImpl.getInstance().dimFixturesByGroup(groupId, percentage, time, ServerConstants.DIM_TYPE_RELATIVE, false);
    }

	public List<Object> getProfilesCount(Long pid) {
		return groupDao.getFixtureCountForProfile(pid);
	}

	public List<Groups> loadAllProfileTemplateById(long templateId, Long tenantId) {
		return groupDao.loadAllProfileTemplateById(templateId,tenantId);
	}
	
	@InvalidateProfileTreeCache	
    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", allEntries=true)	})
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
			if ("template".equalsIgnoreCase(facilityType)) {			
				List<Object> list = profileTemplateManager.getFixtureCountForProfileTemplate(facilityId);				
				if(!(visibilityFlag==false && list.size()>=1))	
				{
					profileTemplateManager.updateTemplateVisibility(facilityId, visibilityFlag);
					templateVisibilityLog.append("Template , name : " + profileTemplateManager.getProfileTemplateById(facilityId).getName() + ", id : " + facilityId + " Flag :"+visibilityFlag+"::");					
				}
			} else if ("group".equalsIgnoreCase(facilityType)) {
				List<Object> list = groupDao.getFixtureCountForProfile(facilityId);
				Groups group = groupDao.getGroupById(facilityId);
				if(!(visibilityFlag==false && list.size()>=1))
				{
					groupDao.updateGroupVisibility(facilityId,visibilityFlag);
					groupVisibilityLog.append(" Group , name : " + groupDao.getGroupById(facilityId).getName() + " id : " + facilityId + " Flag :"+visibilityFlag+"::");					
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
					profileTemplateManager.updateTemplateVisibility(group.getProfileTemplate().getId(), true);
				}
			}
		}
		userAuditLoggerUtil.log("" + groupVisibilityLog, UserAuditActionType.Profile_Update.getName());
		userAuditLoggerUtil.log("" + templateVisibilityLog, UserAuditActionType.Profile_Update.getName());
		fitureAssociatedProfiles = fitureAssociatedProfiles.replaceAll(",$", "");
		return fitureAssociatedProfiles;
	}

	public Short getMaxProfileNo(Long tenantId) {
		return groupDao.getMaxProfileNo(tenantId);
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
			topHierarchy = loadTenantFilterProfilesHierarchy(user.getTenant().getId());
		return topHierarchy;
	}
	
	
	public TreeNode<FacilityType> loadTenantProfilesHierarchy(long tenantId,boolean visibility) {
		  TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	      rootNode.setNodeId("0");
	      rootNode.setName("Template");
	      rootNode.setNodeType(FacilityType.TEMPLATE);

	      //Get Profile Templates
	      List<ProfileTemplate> profileTemplateList = profileTemplateManager.loadAllProfileTemplate();
	      Collections.sort(profileTemplateList, new TemplateComparator());
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
		              //For tenant - display all profiles which is visible and enlighted default profiles and profiles created by tenant
		                for (Groups group : profilesList) {
		                	if((group.isDisplayProfile()==visibility) && group.getTenant()!=null && group.getTenant().getId()==tenantId)
		                	{
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
			                     templateNode.addTreeNode(profileNode);
			                     displayCount++;
		                	}else
		                	{
		                		//Loads all Enlighted Default profiles.
		                		if ((group.isDisplayProfile()==visibility) && group.isDefaultProfile()==true && (!group.getName().equalsIgnoreCase("default"))) {
				                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
				                	 profileNode.setNodeId(group.getId().toString());
				                	 profileNode.setName(group.getName()+"_Default");
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
	public TreeNode<FacilityType> loadTenantFilterProfilesHierarchy(long tenantId) {
		  TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	      rootNode.setNodeId("0");
	      rootNode.setName("Template");
	      rootNode.setNodeType(FacilityType.TEMPLATE);

	      //Get Profile Templates
	      List<ProfileTemplate> profileTemplateList = profileTemplateManager.loadAllProfileTemplate();
	      Collections.sort(profileTemplateList, new TemplateComparator());
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
			                	 if(group.isDefaultProfile()==true)
			                	 {
			                		 profileNode.setName(group.getName()+"_Default");
			                	 }else
			                	 {
			                		 profileNode.setName(group.getName());
			                	 }
			                	 profileNode.setNodeType(FacilityType.GROUP);
			                     templateNode.addTreeNode(profileNode);
			                     if(group.isDisplayProfile())
			                    	 profileNode.setSelected(true);
				                 else
				                	profileNode.setSelected(false);
			                     displayCount++;
		                	}else
		                	{
		                		//Loads all Enlighted Default profiles.
		                		if ((!group.getName().equalsIgnoreCase("default")) && group.isDefaultProfile()==true) {
				                	 TreeNode<FacilityType> profileNode = new TreeNode<FacilityType>();
				                	 profileNode.setNodeId(group.getId().toString());
				                	 profileNode.setName(group.getName()+"_Default");
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
	
	
	public List<Groups> loadAllProfileForTenantByTemplateId(Long templateId,Long tenantId) {
		return groupDao.loadAllProfileForTenantByTemplateId(templateId,tenantId);
	}

	public List<Groups> loadAllGroupsForTenantUser(Long tenantId) {
		  return groupDao.loadAllGroupsForTenantUser(tenantId);
	}
	
	
	@SuppressWarnings("rawtypes")
	public List loadFixtureList(Long pid, String selectedfixtures ,int offset, int limit,AssignFixtureList returnList) {
		List result = null;
		result = groupDao.loadFixtureList(pid,selectedfixtures, offset, limit,returnList);
		return result;
	}

	
    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", key="#profileId")	})
	public int deleteProfile(long profileId) {
		int status=1;
		try
		{
			Groups group = (Groups) groupDao.loadObject(Groups.class, profileId);
			
			//If this groups has children associated then make derived_from_group = null, so that child profile wont throw exception about the profile which is not present.
			List<Groups> derivedProfileList = groupDao.loadAllDerivedProfile(group.getId());
			if (derivedProfileList != null && !derivedProfileList.isEmpty())
		     {
	              Iterator<Groups> it = derivedProfileList.iterator();
	              while (it.hasNext()) {
	            	  Groups rowResult = it.next();
	            	  rowResult.setDerivedFromGroup(null);
	            	  groupDao.saveObject(rowResult);
	              }
		     }
			
			ProfileHandler profileHandler =  profileManager.getProfileHandlerById(group.getProfileHandler().getId());
			groupDao.removeObject(Groups.class, profileId);
			profileManager.deleteProfileHandler(profileHandler);
			
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
	public class ProfileComparator implements Comparator<Groups>{
        @Override
        public int compare(Groups g1, Groups g2) {
            return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
        }
	}

	public List<Groups> getAllNonDefaultDerivedProfile()
	{
		return groupDao.getAllNonDefaultDerivedProfile();
	}

    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", allEntries=true)	})
    public Response updateEMProfiles(List<EMProfile> emProfiles) {
        Response response = new Response();
        Iterator<EMProfile> itr = emProfiles.iterator();
        int savedCount=0;
        while (itr.hasNext()) {
            EMProfile emProfileObj = (EMProfile) itr.next();
            Groups groupObj = getGroupById(emProfileObj.getId());
            try
            {
                String oriName = emProfileObj.getName();
                if(oriName.contains("_EM"))
                {
                    String[] tempArr = oriName.split("_EM");
                    oriName = tempArr[0];
                }
                groupObj.setName(oriName);
                editName(groupObj);
    
                ProfileHandler ph = emProfileObj.getProfileHandler();
             
                ProfileHandler ph1 = profileManager.getProfileHandlerById(groupObj.getProfileHandler().getId());
                Map<Byte,String> mMap = new HashMap<Byte, String>();
                mMap.put((byte) 0, ProfileOverrideType.No_Override.getName());
                mMap.put((byte) 1, ProfileOverrideType.Override1.getName());
                mMap.put((byte) 2, ProfileOverrideType.Override2.getName());
                mMap.put((byte) 3, ProfileOverrideType.Override3.getName());
                mMap.put((byte) 4, ProfileOverrideType.Override4.getName());
                
                ph1.copyProfilesFrom(ph);
                Set<WeekDay> Oriweek = emProfileObj.getProfileHandler().getProfileConfiguration().getWeekDays();
                Set<WeekDay> week = ph1.getProfileConfiguration().getWeekDays();
                for (WeekDay Oriday : Oriweek) {
                    //System.out.println("Oriweek " +  Oriday.getDay() + " Type " + Oriday.getType() + " Order " + Oriday.getShortOrder());
                    for (WeekDay day : week) {
                        if (day.getShortOrder().intValue() == Oriday.getShortOrder().intValue()) {
                            //System.out.println("***************************************** "  + day.getShortOrder() + " --------------------------- "+ Oriday.getShortOrder());
                            if (Oriday.getType().equals("weekday")) {
                                day.setType("weekday");
                            } else {
                                day.setType("weekend");
                            }
                            break;
                        }
                    }
                }
                ph1.copyPCTimesFrom(ph);
                
                // Update Advance Profile
                ph1.setDropPercent(ph.getDropPercent());
                ph1.setRisePercent(ph.getRisePercent());
                ph1.setIntensityNormTime(ph.getIntensityNormTime());
                ph1.setDimBackoffTime(ph.getDimBackoffTime());
                ph1.setMinLevelBeforeOff(ph.getMinLevelBeforeOff());
                ph1.setToOffLinger(ph.getToOffLinger());
                ph1.setInitialOnLevel(ph.getInitialOnLevel());
                ph1.setInitialOnTime(ph.getInitialOnTime());
                ph1.setDrReactivity(ph.getDrReactivity());
                ph1.setDarkLux(ph.getDarkLux());
                ph1.setNeighborLux(ph.getNeighborLux());
                ph1.setEnvelopeOnLevel(ph.getEnvelopeOnLevel());
                ph1.setIsHighBay(ph.getIsHighBay());
                ph1.setMotionThresholdGain(ph.getMotionThresholdGain());
    
                ph1.copyOverrideProfilesFrom(ph);
                
                // 1. Copy the profile configuration of the current group on to each fixtures within the group with this
                // profile configuration
                // and update the group_id to the current one.
                // 2. Enable push flag for the list of fixture(s) within the group to set to true, The profile will be
                // sync'd in the next PM stat
                profileManager.updateGroupProfile(ph1, groupObj.getId());
                profileManager.updateAdvanceGroupProfile(ph1, groupObj.getId());
                logger.error("Update Profile : Profile "+ groupObj.getId() + " updated to EM and Pushed to SU successfuly");
                savedCount++;
            }catch(Exception e)
            {
                response.setStatus(-1);
                response.setMsg("Profile "+ groupObj.getId()  + " failed to Save");
                logger.error("Update Profile : Profile "+ groupObj.getId() + " could not be saved. Reason :" + e.getMessage());
            }
        }
        if(emProfiles.size() == savedCount)
        {
            response.setMsg("Profile Saved");
        }
        return response;
    }

    public EMProfile PushNewProfileToEM(EMProfile emProfile) {
        EMProfile eMProfileResObj = new EMProfile();
        String profileName = emProfile.getName();
        Short newProfileNo = emProfile.getProfileNo();
        Groups group = new Groups();
        group.setProfileNo(newProfileNo);
        
        List<Groups> dbMatchingGroups = groupDao.getMatchingGroupByName(profileName);
        if((dbMatchingGroups!=null) && (!dbMatchingGroups.isEmpty()))
        {
            int size = dbMatchingGroups.size();
            if(size>0)
            {
                Groups lastMatchingGrp = dbMatchingGroups.get(size-1);
                String oriName = lastMatchingGrp.getName();
                String[] tempArr = oriName.split("_");
                Long id =0l;
                try
                {
                    id = Long.parseLong(tempArr[tempArr.length-1]);
                    id+=1;
                }catch(NumberFormatException e)
                {
                    id++;
                }
                profileName = profileName.concat("_"+id);
            }
        }
        group.setName(profileName);
        group.setDefaultProfile(false);
        group.setDisplayProfile(true);
        ProfileTemplate pSavedProfileTemplate = null;
        if(emProfile.getProfileTemplate()>16 || emProfile.getProfileTemplate()==-1)
        {
            Long profileTemplateId = emProfile.getProfileTemplate();
            pSavedProfileTemplate = profileTemplateManager.getProfileTemplateById(profileTemplateId);
            if(pSavedProfileTemplate==null)
            {
                ProfileTemplate profileTemplate = new ProfileTemplate();
                profileTemplate.setName(profileName);
                profileTemplate.setDisplayTemplate(true);
                pSavedProfileTemplate = profileTemplateManager.save(profileTemplate);
            }
        }else
        {
            pSavedProfileTemplate = profileTemplateManager.getProfileTemplateById(emProfile.getProfileTemplate());
        }
        
        group.setProfileTemplate(pSavedProfileTemplate);
        
        Groups derivedFrmGroup = null;
        if(emProfile.getDerivedFromGroup()>-1)
        {
            derivedFrmGroup =  getGroupById(emProfile.getDerivedFromGroup());
        }
        group.setDerivedFromGroup(derivedFrmGroup);
        
        ProfileHandler newPfh = emProfile.getProfileHandler();
       
        
        ProfileHandler ph1 = profileManager.createProfile("default.", newProfileNo, true);
        
        ph1.copyProfilesFrom(newPfh);
        
        Set<WeekDay> Oriweek = newPfh.getProfileConfiguration().getWeekDays();
        Set<WeekDay> week = ph1.getProfileConfiguration().getWeekDays();
        for (WeekDay Oriday : Oriweek) { 
            for (WeekDay day : week) {
                if (day.getShortOrder().intValue() == Oriday.getShortOrder().intValue()) {
                    if (Oriday.getType().equals("weekday")) {
                        day.setType("weekday");
                    } else {
                        day.setType("weekend");
                    }
                    break;
                }
            }
        }
        
        ph1.copyPCTimesFrom(newPfh);
        ph1.copyAdvanceProfileFrom(newPfh);
        ph1.setProfileGroupId(newProfileNo);
        ph1.copyOverrideProfilesFrom(newPfh);
        
        profileManager.saveProfileHandler(ph1);

        group.setProfileHandler(ph1);
        
        Company company = companyManager.getCompany();
        group.setCompany(company);
        
        Groups savedGrp = saveOrUpdateGroup(group);
        
        eMProfileResObj.setName(savedGrp.getName());
        eMProfileResObj.setId(savedGrp.getId());
        eMProfileResObj.setProfileNo(savedGrp.getProfileNo());
        eMProfileResObj.setProfileTemplate(pSavedProfileTemplate.getId());
        
        logger.info("New UEM profile "+ profileName +" Pushed Successfuly to EM");
        
        return eMProfileResObj;
    }

	public List<Groups> getAllDerivedGroups(Long groupId) {
		
		return groupDao.loadAllDerivedProfile(groupId);
		
	} //end of method getAllDerivedGroups
	
    @InvalidateProfileTreeCache
    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", key="#group.id")	})
    public Groups saveOrUpdateGroup(Groups group) {
        return groupDao.saveOrUpdateGroup(group);
    }
    

    public WeekDay saveOrUpdateWeekDay(WeekDay weekDay) {
        return groupDao.saveOrUpdateWeekDay(weekDay);
    }

    @Caching(evict = {@CacheEvict (value = {"fixture_id", "fixture_snap"}, allEntries=true),
			@CacheEvict (value="group_id", key="#g.id")	})
    public void setProfileHandler(Groups g, ProfileHandler ph) {
    	g.setProfileHandler(ph);
    	groupDao.update(g);
    }
}

