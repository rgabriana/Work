package com.emscloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.action.SpringContext;
import com.emscloud.communication.vos.EMProfile;
import com.emscloud.communication.vos.Response;
import com.emscloud.dao.ProfileSyncStatusDao;
import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.EmTemplateMapping;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileHandler;
import com.emscloud.model.ProfileSyncStatus;
import com.emscloud.model.ProfileTemplate;
import com.emscloud.model.WeekDay;
import com.emscloud.server.ServerConstants;
import com.emscloud.vo.EmProfileList;
import com.emscloud.vo.EmTemplateList;
/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Service("profileSyncManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileSyncManager {

    static final Logger logger = Logger.getLogger(ProfileSyncManager.class.getName());

    @Resource
    ProfileDefaultConfigurationManager profileDefaultConfigurationManager;
    @Resource(name = "profileGroupManager")
    private ProfileGroupManager profileGroupManager;
    @Resource(name= "profileManager")
    private ProfileManager profileManager;
    @Resource(name = "metaDataManager")
    private MetaDataManager metaDataManager;
    @Resource(name="profileTemplateManager")
    ProfileTemplateManager profileTemplateManager;
    @Resource(name="profileSyncStatusDao")
    ProfileSyncStatusDao profileSyncStatusDao;
    @Resource(name="emProfileMappingManager")
    EmProfileMappingManager emProfileMappingManager;
    @Resource(name="emProfileTemplateMappingManager")
    EmProfileTemplateMappingManager emProfileTemplateMappingManager;
    @Resource(name="customerManager")
    CustomerManager customerManager;
    @Resource(name="emInstanceManager")
    EmInstanceManager emInstanceManager;
    @Resource(name="facilityEmMappingManager")
    FacilityEmMappingManager facilityEmMappingManager;
    public boolean createDefaultProfilesInUEM()
    {
        boolean isAllDefaultProfileSaved=false;
        List<ProfileGroups> profileGroupList = profileGroupManager.loadAllGroups();
        //This should be called only once in the application lifetime
        if(profileGroupList==null){
            ProfileHandler profileHandler = profileManager.createProfile("default.",ServerConstants.DEFAULT_PROFILE_GID,true);
            CustomerManager customerManager = (CustomerManager) SpringContext.getBean("customerManager");
            List<Customer> customerlist = customerManager.loadallCustomer();
            if(customerlist!=null && customerlist.size()>0)
            {
                profileManager.saveDefaultGroups(profileHandler);
                isAllDefaultProfileSaved = true;
                 logger.info("Global Default profiles created");
            }else
            {
                logger.error("Default profiles can not be created as there is not any customer present");
            }
        }else
        {
            isAllDefaultProfileSaved = true;
            logger.info("Default profiles creation already done");
        }
        return isAllDefaultProfileSaved;
    }
    public void downloadDerivedEMTemplatesToUEM(Long emInstId)
    {
        try {
            List<EmTemplateList> allEmTemplatesList = profileTemplateManager.getAllDerivedProfileTemplate(emInstId);
            if (allEmTemplatesList != null && allEmTemplatesList.size() > 0)
            {
                for (Iterator<EmTemplateList> allEmTemplatesListiterator = allEmTemplatesList.iterator(); allEmTemplatesListiterator.hasNext();) {
                    EmTemplateList emTemplateItr = (EmTemplateList) allEmTemplatesListiterator.next();
                    List<ProfileTemplate> emTemplates= emTemplateItr.getEmTemplate();
                    EmInstance emInstance = emTemplateItr.getEm();
                    ProfileSyncStatus profileSyncStatus = profileSyncStatusDao.getProfileSyncStatusByEMId(emInstance.getId());
                    int totalProfileSavedCount = 0;
                    if(profileSyncStatus!=null && profileSyncStatus.getTemplateDownloadSync()==false)
                    {
                        if (emTemplates != null && emTemplates.size() > 0)
                        {
                            for (Iterator<ProfileTemplate> emTemplateiterator = emTemplates.iterator(); emTemplateiterator.hasNext();) {
                                ProfileTemplate emTemplate = (ProfileTemplate) emTemplateiterator.next();
                                
                                EmTemplateMapping emTemplateMapping = emProfileTemplateMappingManager.getEmTemplateMappingByEmTemplateIdAndEMId(emTemplate.getId(),emInstance.getId());
                                
                                if(emTemplateMapping==null)
                                {
                                    ProfileTemplate dbTemplate= profileTemplateManager.getProfileTemplateByName(emTemplate.getName());
                                    if(dbTemplate==null)
                                    {
                                        ProfileTemplate profileTemplate = new ProfileTemplate();
                                        Long templateNo = profileTemplateManager.getNextProfileTemplateNo();
                                        profileTemplate.setName(emTemplate.getName());
                                        profileTemplate.setTemplateNo(templateNo+1);
                                        profileTemplate.setDisplayTemplate(true);
                                        dbTemplate = profileTemplateManager.save(profileTemplate);
                                    }
                                    emTemplateMapping = new EmTemplateMapping();
                                    emTemplateMapping.setEmId(emInstance.getId());
                                    emTemplateMapping.setEmTemplateId(emTemplate.getId());
                                    emTemplateMapping.setEmTemplateName(emTemplate.getName());
                                    emTemplateMapping.setUemTemplateId(dbTemplate.getId());
                                    emProfileTemplateMappingManager.saveOrUpdate(emTemplateMapping);
                                    logger.info("DOWNLOAD TEMPLATE : Template "+emTemplate.getName() +" Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                                }
                               
                                //System.out.println("Template "+emTemplate.getName() +" Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                                totalProfileSavedCount++;
                            }
                            if(totalProfileSavedCount==emTemplates.size())
                            {
                                profileSyncStatus.setTemplateDownloadSync(true);
                            }
                        }else
                        {
                            //No Custom Templates Found
                            profileSyncStatus.setTemplateDownloadSync(true);
                        }
                        profileSyncStatusDao.saveOrUpdate(profileSyncStatus);
                    }else
                    {
                        logger.info("DOWNLOAD TEMPLATE : Template already Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                        //System.out.println("Template already Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                    }
                }
            }else
            {
                logger.error("DOWNLOAD TEMPLATE : Not able to get template list from EM. Please check EM connected to UEM.");
            }
        }catch (NumberFormatException e) {
            logger.error("DOWNLOAD TEMPLATE : Templates Failed from EM to UEM: "+e.getStackTrace());
            e.printStackTrace();
        }catch(Exception e) {
            logger.error("DOWNLOAD TEMPLATE : Download Templates Failed from EM to UEM: : "+e.getStackTrace());
            e.printStackTrace();
        }
    }
    public void downloadDerivedEMProfilesToUEM(Long emId) {
        try {
                List<EmProfileList> allEmProfilesList = profileGroupManager.getAllDerivedEMProfiles(emId);
                if (allEmProfilesList != null && allEmProfilesList.size() > 0)
                {
                    for (Iterator<EmProfileList> allEmProfilesListiterator = allEmProfilesList.iterator(); allEmProfilesListiterator.hasNext();) {
                        EmProfileList emprofileItr = (EmProfileList) allEmProfilesListiterator.next();
                        List<EMProfile> emProfiles = emprofileItr.getProfileList();
                        EmInstance emInstance = emprofileItr.getEm();
                        ProfileSyncStatus profileSyncStatus = profileSyncStatusDao.getProfileSyncStatusByEMId(emInstance.getId());
                        int totalProfileSavedCount = 0;
                        if(profileSyncStatus!=null && profileSyncStatus.getProfileDownloadSync()==false && profileSyncStatus.getTemplateDownloadSync()==true)
                        {
                            if (emProfiles != null && emProfiles.size() > 0)
                            {
                                for (Iterator<EMProfile> emProfileiterator = emProfiles.iterator(); emProfileiterator.hasNext();) {
                                    EMProfile emProfile = (EMProfile) emProfileiterator.next();
                                    
                                    EmProfileMapping emProfileMapping = emProfileMappingManager.getEmTemplateMappingByEmProfileNoAndEMId(emProfile.getProfileNo(),emInstance.getId());
                                    
                                    if(emProfileMapping==null)
                                    {
                                        String profilename = emProfile.getName();
                                        String groupkey = "";
                                        groupkey = profilename.replaceAll(" ", "").toLowerCase();
                                        groupkey = "default." + groupkey + ".";
                                        
                                        EmTemplateMapping emTemplateMapping= emProfileTemplateMappingManager.getEmTemplateMappingByEmTemplateIdAndEMId(emProfile.getProfileTemplate(), emInstance.getId());
                                        
                                        ProfileGroups derivedGrp = profileGroupManager.getGroupById(emProfile.getDerivedFromGroup());
                                        
                                        //If derivedGrp is null i.e check it's actual group id in em_profile_mapping table
                                        
                                        if(derivedGrp==null)
                                        {
                                            EmProfileMapping ProfileMapping = emProfileMappingManager.getEmTemplateMappingByEmGroupIdAndEMId(emProfile.getDerivedFromGroup(),emInstance.getId());
                                            derivedGrp = profileGroupManager.getGroupById(ProfileMapping.getUemProfileId());
                                        }
                                        
                                        ProfileHandler ph1 = profileManager.createProfile(groupkey, derivedGrp.getId().intValue(),true);
                                        
                                        //Now modify
                                        ph1.copyProfilesFrom(emProfile.getProfileHandler());
                                                                
                                        Set<WeekDay> Oriweek = emProfile.getProfileHandler().getProfileConfiguration().getWeekDays();
                                        
                                        // short circuited the saving of weekday, Need ordered lists instead of sets
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
                                        ph1.copyPCTimesFrom(emProfile.getProfileHandler());
                                        
                                        Short newInstanceProfileNo = emProfile.getProfileNo();
                                        ph1.copyAdvanceProfileFrom(emProfile.getProfileHandler());
                                        ph1.setProfileGroupId(newInstanceProfileNo);
                                        ph1.copyOverrideProfilesFrom(emProfile.getProfileHandler());
                                        profileManager.saveProfileHandler(ph1);
                                       
                                        ProfileGroups group = new ProfileGroups();
                                        group.setProfileNo(newInstanceProfileNo);
                                        if(emInstance.getName()!=null)
                                        {
                                            profilename += "_"+emInstance.getName();
                                        }else
                                        {
                                            profilename += "_"+emInstance.getId();
                                        }
                                        group.setName(profilename);
                                        group.setDefaultProfile(false);
                                        group.setDisplayProfile(true);
                                        
                                        ProfileTemplate profileTemplate =null;
                                        if(emTemplateMapping!=null)
                                         profileTemplate = profileTemplateManager.getProfileTemplateById(emTemplateMapping.getUemTemplateId());
                                        else
                                           profileTemplate = profileTemplateManager.getProfileTemplateById(emProfile.getProfileTemplate());
                                        group.setProfileTemplate(profileTemplate);
                                        group.setDerivedFromGroup(derivedGrp);
                                        group.setProfileHandler(ph1);
                                        group.setCompany(emInstance.getCustomer());
                                        ProfileGroups savedGroup = metaDataManager.saveOrUpdateGroup(group);
                                        metaDataManager.flush();
                                        
                                        emProfileMapping = new EmProfileMapping();
                                        emProfileMapping.setEmId(emInstance.getId());
                                        emProfileMapping.setEmGroupId(emProfile.getId());
                                        emProfileMapping.setEmProfileNo(newInstanceProfileNo);
                                        emProfileMapping.setUemProfileId(savedGroup.getId());
                                        emProfileMapping.setTemplateId(emProfile.getProfileTemplate());
                                        emProfileMapping.setSyncStatus(0);
                                        emProfileMappingManager.saveOrUpdate(emProfileMapping);
                                        
                                        logger.info("DOWNLOAD PROFILE : Profile "+ emProfile.getName() +" Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                                    }
                                 
                                    //System.out.println("Profile "+ emProfile.getName() +" Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                                    
                                    totalProfileSavedCount++;
                                }
                                
                                if(totalProfileSavedCount==emProfiles.size())
                                {
                                    profileSyncStatus.setProfileDownloadSync(true);
                                    profileSyncStatusDao.saveOrUpdate(profileSyncStatus);
                                }
                            }
                        }
                        else
                        {
                            logger.info("DOWNLOAD PROFILE: Profiles already Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                            //System.out.println("Profiles already Downloaded from EM "+ emInstance.getIpAddress() +" to UEM");
                        }
                    }
                 }else
                 {
                     logger.error("DOWNLOAD PROFILE : Not able to get derived profiles list from EM. Please check EM connected to UEM.");
                 }
                    
        } catch (NumberFormatException e) {
            logger.warn("DOWNLOAD PROFILE : Download Profile Failed from EM to UEM: "+e.getStackTrace());
            e.printStackTrace();
        }catch(Exception e) {
            logger.warn("DOWNLOAD PROFILE : Download Profile Failed from EM to UEM: : "+e.getStackTrace());
            e.printStackTrace();
        }
    }
    
    
    public Response syncProfileGroupsToEM()
    {
        Response response = new Response();
        //Get the List of all profiles to be pushed having sync_status flag as 1
        List<EmProfileMapping> emProfileMapping = emProfileMappingManager.getListOfDirtyProfilesInUEM();
        List<EMProfile> emProfilesList = new ArrayList<EMProfile>();
        
        if (emProfileMapping != null && emProfileMapping.size() > 0)
        {
            for (Iterator<EmProfileMapping> emProfileMappingiterator = emProfileMapping.iterator(); emProfileMappingiterator.hasNext();) {
                EmProfileMapping emProfileMappingObj = (EmProfileMapping) emProfileMappingiterator.next();
                EmInstance emInstance = emInstanceManager.getEmInstance(emProfileMappingObj.getEmId());
                
                List<FacilityEmMapping> facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnEmId(emInstance.getId());
                // Ensure that the Said EM Instance is present in the Facility_em_mapping table, otherwise bypass sync workflow
                if(facilityEmMapping!=null && facilityEmMapping.size()>0)
                {
                    try
                    {
                        ProfileGroups uemDBProfile = profileGroupManager.getGroupById(emProfileMappingObj.getUemProfileId());
                        EMProfile emProfile = new EMProfile();
                        emProfile.setId(emProfileMappingObj.getEmGroupId());
                        
                        ProfileHandler derivedProfileHandler = profileGroupManager.fetchProfileHandlerById(uemDBProfile.getProfileHandler().getId());
                        emProfile.setProfileHandler(derivedProfileHandler);
                       
                        emProfile.setDerivedFromGroup(uemDBProfile.getDerivedFromGroup().getId());
                        emProfile.setName(uemDBProfile.getName());
                        emProfile.setProfileTemplate(emProfileMappingObj.getTemplateId());
                        //ProfileTemplate pTemplate= profileTemplateManager.getProfileTemplateById(uemDBProfile.getProfileTemplate().getId());
                        //emProfile.setProfileTemplateName(pTemplate != null ? pTemplate.getName() : "");
                        emProfile.setProfileNo(emProfileMappingObj.getEmProfileNo());
                        emProfilesList.add(emProfile);
                        emProfileMappingObj.setSyncStartTime(new Date());
                        
                        List<EmInstance> emList = new ArrayList<EmInstance>();
                        emList.add(emInstance);
                        response = profileGroupManager.updateEMProfiles(emProfilesList,emList);
                        
                        if(response!=null && response.getStatus()==0)
                        {
                            emProfileMappingObj.setSyncStatus(0);
                            emProfileMappingManager.saveOrUpdate(emProfileMappingObj);
                            logger.info("SYNC :Profile No  "+ emProfileMappingObj.getEmProfileNo() +" of EM Pushed successfuly to EM "+ emInstance.getIpAddress() +" to UEM");
                        }else
                        {
                            response = new Response();
                            response.setMsg("Profile Pushed Failed");
                            response.setStatus(-1);
                            logger.error("SYNC :Profile No  "+ emProfileMappingObj.getEmProfileNo() + " Failed to push to EM "+ emInstance.getIpAddress());
                        }
                    }catch(Exception e)
                    {
                        logger.error("SYNC : Profiles Pushed Failed to EM "+ emInstance.getIpAddress() +" to UEM");
                    }
                }else
                {
                    logger.info("SYNC : "+ emInstance.getIpAddress() +" is not mapped to UEM. Therefore Profile Sync Mechanism wont be excecuted.");
                }
            }
        }
        return response;
    }
    
    public EMProfile PushNewProfileToEM(Long groupId, Long floorId)
    {
        EMProfile emProfile=null;
        EMProfile savedEMProfile=null;
        Response response = new Response();
        Long emId = null;
        EmInstance emInstance=null;
        
        if(floorId!=null && groupId!=null)
        {
            FacilityEmMapping facilityEmMapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(floorId);
            
            if(facilityEmMapping!=null)
                emId  = facilityEmMapping.getEmId();
            ProfileGroups grp = profileGroupManager.getGroupById(groupId);
            
            emInstance = emInstanceManager.getEmInstance(emId);
            List<EmInstance> emList = new ArrayList<EmInstance>();
            emList.add(emInstance);
            
            //Step 1 : First check whether the given Profile is present on the EM. If present then there would be mapping entry in DB
            EmProfileMapping emProfileMapping = emProfileMappingManager.getEmProfileMappingByUemProfileIdAndEMId(emId,groupId);
            
            if(emProfileMapping==null)
            {
                
                Short newProfileNoForEM=0;
                Long maxGroupId=(long) -1;
                //Need to Calculate next eligible profile number number with 255 as upper limit.
                Long tenantId=null;
                newProfileNoForEM = profileGroupManager.getMaxProfileNo(tenantId,emId);
                
                if(newProfileNoForEM>255)
                {
                    logger.error("Maximum 255 Profiles can be created per EM Instance basis");
                    return null;
                }
              
                emProfileMapping = new EmProfileMapping();
                emProfileMapping.setEmId(emId);
                emProfileMapping.setEmGroupId(maxGroupId);
                emProfileMapping.setEmProfileNo(newProfileNoForEM);
                emProfileMapping.setUemProfileId(groupId);
                emProfileMapping.setSyncStatus(0);
            
                Long pTId = grp.getProfileTemplate().getId();
                EmTemplateMapping emTemplateMapping= emProfileTemplateMappingManager.getEmTemplateMappingByUEMTemplateIdAndEMId(pTId, emId);
                Long profileTemplateId = pTId;
                if(emTemplateMapping!=null)
                {
                    profileTemplateId = emTemplateMapping.getEmTemplateId();
                }
                
                //Step 2: Create New EMProfile VO based on above group 
                emProfile = new EMProfile();
                emProfile.setId(maxGroupId);
                String oriName = grp.getName();
                if(oriName.contains("_EM"))
                {
                    String[] tempArr = oriName.split("_EM");
                    oriName = tempArr[0];
                }
                emProfile.setName(oriName);
                emProfile.setProfileNo(newProfileNoForEM);
                
                //Check for its Derived from Group - if derived from non default derived profile then set is as -1
                Long derivedProfileNo=(long) 1;
                ProfileGroups derivedGrp = profileGroupManager.getGroupById(grp.getDerivedFromGroup().getId());
                if(derivedGrp.isDefaultProfile()==true)
                {
                    derivedProfileNo = grp.getDerivedFromGroup().getId();
                }
                emProfile.setDerivedFromGroup(derivedProfileNo);
                emProfile.setProfileTemplate(grp.getProfileTemplate() != null ? profileTemplateId : -1);
                //ProfileTemplate pTemplate= profileTemplateManager.getProfileTemplateById(grp.getProfileTemplate().getId());
                //emProfile.setProfileTemplateName(pTemplate != null ? pTemplate.getName() : "");
                
                //Now update the ProfileHandler's profilegroupdId with the newProfileNoForEM
                ProfileHandler derivedProfileHandler = profileGroupManager.fetchProfileHandlerById(grp.getProfileHandler().getId());
                derivedProfileHandler.setProfileGroupId(newProfileNoForEM);
                
                emProfile.setProfileHandler(derivedProfileHandler);
                
               //Step 3: Send this new profile to EM
                savedEMProfile = profileGroupManager.pushNewProfileToEM(emProfile,emList);
                
                if(savedEMProfile!=null)
                {
                    emProfileMapping.setEmGroupId(savedEMProfile.getId());
                    emProfileMapping.setTemplateId(savedEMProfile.getProfileTemplate());
                    emProfileMappingManager.saveOrUpdate(emProfileMapping);
                    logger.info("PUSH :Profile "+ grp.getName() + " Pushed successfully to EM "+ emInstance.getIpAddress());
                }else
                {
                    response.setMsg("Profile Pushed Failed");
                    response.setStatus(-1);
                    logger.error("PUSH :Profile "+ grp.getName() + " Failed to push to EM "+ emInstance.getIpAddress());
                }
            }else
            {
                emProfileMapping.setSyncStatus(1);
                emProfileMappingManager.saveOrUpdate(emProfileMapping);
                syncProfileGroupsToEM();
                logger.info("PUSH : Profile "+ grp.getName() + " sent for Sync Process to EM "+ emInstance.getIpAddress());
            }
        }
        return savedEMProfile;
    }
    
    /*
     * Sample function to create given number for dummy profiles under the given template 
     */
    public int createCustomGroups(int from, int to, Long templateId)
    {
        String profilename ="UEM Profile";
        Long typeid =(long) 1;
        ProfileHandler ph = profileManager.getProfileHandlerById((long) 1);
        int updatedCount=0;
        for(int i=from; i<=to;i++)
        {
            String groupkey = "";
            groupkey = profilename.replaceAll(" ", "").toLowerCase();
            groupkey = "default." + groupkey + ".";
            ProfileGroups derivedGrp = profileGroupManager.getGroupById(typeid);
            ProfileHandler ph1 = profileManager.createProfile(groupkey, derivedGrp.getId().intValue(),true);
            
             //Now modify
            ph1.copyProfilesFrom(ph);
            ph1.copyPCTimesFrom(ph);
            
            Long tenantID=null;
            Long emInstanceId=null;
            // Update Advance Profile
            Short profileNo = (profileGroupManager.getMaxProfileNo(tenantID,emInstanceId));
            
            ph1.copyAdvanceProfileFrom(ph);
            ph1.setProfileGroupId(profileNo);
            ph1.copyOverrideProfilesFrom(ph);
            profileManager.saveProfileHandler(ph1);
            ProfileGroups group = new ProfileGroups();
            group.setProfileNo(profileNo);
            group.setName(profilename+"_"+i);
            group.setDefaultProfile(false);
            group.setDisplayProfile(true);
            ProfileTemplate profileTemplate = profileTemplateManager.getProfileTemplateById(templateId);
            group.setProfileTemplate(profileTemplate);
            group.setDerivedFromGroup(derivedGrp);
            group.setProfileHandler(ph1);
            List<Customer> cList = customerManager.loadallCustomer();
            Customer c = customerManager.loadCustomerById(cList.get(0).getId());
            group.setCompany(c);
            metaDataManager.saveOrUpdateGroup(group);
            updatedCount++;
            metaDataManager.flush();
        }
        return updatedCount;
    }
    
    public void resetDownloadProfileSyncFlag(Long emId)
    {
            List<FacilityEmMapping> facilityEmMappingList = facilityEmMappingManager.getFacilityEmMappingOnEmId(emId);
            if(facilityEmMappingList==null || facilityEmMappingList.size()==0)
            {
                ProfileSyncStatus profileSyncStatus = profileSyncStatusDao.getProfileSyncStatusByEMId(emId);
                profileSyncStatus.setProfileDownloadSync(false);
                profileSyncStatus.setTemplateDownloadSync(false);
            }
    }
}
