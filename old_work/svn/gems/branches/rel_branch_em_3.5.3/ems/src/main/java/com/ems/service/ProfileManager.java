package com.ems.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ProfileDao;
import com.ems.model.CannedProfileConfiguration;
import com.ems.model.Company;
import com.ems.model.Groups;
import com.ems.model.Holiday;
import com.ems.model.Profile;
import com.ems.model.ProfileConfiguration;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.SystemConfiguration;
import com.ems.model.WeekDay;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Service("profileManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileManager {

    static final Logger logger = Logger.getLogger("ProfileLogger");

    @Resource
    ProfileDao profileDao;
    
    @Resource(name = "profileManager")
	private ProfileManager profileManager;
    
    @Resource(name = "groupManager")
	private GroupManager groupManager;

    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    @Resource(name = "companyManager")
	private CompanyManager companyManager;
    
    @Resource(name = "cannedProfileConfigurationManager")
	private CannedProfileConfigurationManager cannedProfileConfigurationManager;
    
    
    public List<Profile> getAllProfiles() {
        return profileDao.getAllProfiles();
    }

    public void saveProfile(Profile profile) {
        profileDao.saveProfile(profile);
    }

    public Profile updateProfileDetails(String column, String value, String id) {
        if (validateProfileData(value)) {
            profileDao.updateProfileDetails(column, value, id);
        }
        return (Profile) profileDao.getObject(Profile.class, new Long(id));
    }

    private boolean validateProfileData(String value) {
        boolean valid = false;
        try {
            Integer integer = new Integer(value);
            if (integer.intValue() < 0 || integer.intValue() > 200) {
                valid = false;
            } else {
                valid = true;
            }
        } catch (Exception e) {
            valid = false;
        }
        return valid;
    }

    /**
     * Load ProfileHandler details
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.ProfileHandler
     */
    public ProfileHandler loadProfileHandler(Long id) {
        return (ProfileHandler) profileDao.getObject(ProfileHandler.class, id);
    }

    /**
     * update ProfileConfiguration details
     * 
     * @param column
     *            name of column need to update
     * @param value
     *            new value
     * @param id
     *            database id(primary key)
     */
    public void updateProfileConfigurationTime(String column, String value, String id) {
        profileDao.updateProfileConfigurationTime(column, value, id);
    }

    /**
     * update WeekDay details
     * 
     * @param column
     *            name of column need to update
     * @param value
     *            new value
     * @param id
     *            database id(primary key)
     */
    public void updateProfileConfigurationWeekDay(String column, String value, String id) {
        profileDao.updateProfileConfigurationWeekDay(column, value, id);
    }

    //TODO not required.
    /**
     * Save holiday details
     * 
     * @param holiday
     */
    public void saveHoliday(Holiday holiday) {
        profileDao.saveObject(holiday);
    }

    /**
     * load ProfileConfiguration details
     * 
     * @param id
     *            database id(primary key)
     * @return
     */
    public ProfileConfiguration loadProfileConfiguration(Long id) {
        return (ProfileConfiguration) profileDao.getObject(ProfileConfiguration.class, id);

    }

    //TODO not required.
    /**
     * Load all holidays for given config.
     * 
     * @param configId
     *            ProfileConfiguration id.
     * @return collection of com.ems.model.Holiday
     */
    public List<Holiday> loadHolidaysByConfigId(Long configId) {
        return profileDao.loadHolidaysByConfigId(configId);
    }
    
    
    //TODO not required.
    /**
     * Remove Holiday detail.
     * 
     * @param id
     *            database id(primary key)
     */
    public void deleteHoliday(Long id) {
        profileDao.removeObject(Holiday.class, id);
    }

    public ProfileConfiguration saveProfileConfiguration(ProfileConfiguration profileConfiguration) {
        return profileDao.saveProfileConfiguration(profileConfiguration);
    }

    public ProfileHandler saveProfileHandler(ProfileHandler profileHandler) {
        return profileDao.saveProfileHandler(profileHandler);
    }

    public Profile getProfileById(Long id) {
        return (Profile) profileDao.getObject(Profile.class, id);
    }
    
    public Profile getProfileByProfileNo(Long number)
    {
    	return (Profile) profileDao.getObject(Profile.class, number);
    }

    public ProfileHandler getProfileHandlerById(Long id) {
        return profileDao.getProfileHandlerById(id);
    }

    public Long getProfileHandlerIDByGroupName(String sGroupName) {
        return profileDao.getProfileHandlerIDByGroupName(sGroupName);
    }

    @Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
    @Resource(name = "metaDataManager")
    private MetaDataManager metaDataManager;
    
    @Resource(name = "profileTemplateManager")
    private ProfileTemplateManager profileTemplateManager;

    public void setupProfileOverrides() {
        // Fetchs ENL provided default profile for the ENL provide groups
        List<ProfileHandler> mDefaultProfilesList = profileManager
                .getDefaultProfilesFromSystemConfigurationForComparison();
        // Fetches the existing "default" groups that are in the database.
        List<Groups> mDBGroupsList = groupManager.loadAllGroups();

        // Now start comparison, iterate over the existing profiles in the database
        for (short i = 0; i < mDBGroupsList.size(); i++) {
            Groups mGroup = mDBGroupsList.get(i);
            if (mGroup != null) {
                // Add only enlighted Provided defaults
                if (mGroup.getDerivedFromGroup() == null) {
                    ProfileHandler mGroupProfileHandler = profileManager.getProfileHandlerById(mGroup
                            .getProfileHandler().getId());
                    int iGroupId = mGroup.getProfileNo() - 1;
                    if (iGroupId <= 16) {
                        ProfileHandler mDefaultProfileHandler = mDefaultProfilesList.get(iGroupId);
                        if (mGroupProfileHandler.copyProfileOverrides(mDefaultProfileHandler) > 0) {
                            logger.info("Copy profile: " + mDefaultProfileHandler.getProfileGroupId() + " to "
                                    + mGroupProfileHandler.getProfileGroupId());
                            saveProfileHandler(mGroupProfileHandler);
                            fixtureManager.enablePushProfileForGroup(mGroup);
                        } else {
                            logger.info("Profile: " + mGroupProfileHandler.getProfileGroupId()
                                    + " profile overide already sync'd");
                        }
                    }
                } else {
                    int parentGroupId = mGroup.getDerivedFromGroup().getProfileNo();
                    if (parentGroupId <= 16) {
                        logger.info("Dervied profile: " + mGroup.getProfileNo() + ", Parent profile: "
                                + parentGroupId);
                        ProfileHandler mGroupProfileHandler = profileManager.getProfileHandlerById(mGroup
                                .getProfileHandler().getId());
                        ProfileHandler mDefaultProfileHandler = mDefaultProfilesList.get(parentGroupId - 1);
                        if (mDefaultProfileHandler != null) {
                            if (mGroupProfileHandler.copyProfileOverrides(mDefaultProfileHandler) > 0) {
                                logger.info("Copy profile: " + mDefaultProfileHandler.getProfileGroupId() + " to "
                                        + mGroupProfileHandler.getProfileGroupId());
                                saveProfileHandler(mGroupProfileHandler);
                                fixtureManager.enablePushProfileForGroup(mGroup);
                            } else {
                                logger.info("Profile: " + mGroupProfileHandler.getProfileGroupId()
                                        + " profile overide already sync'd");
                            }
                        }
                    } else {
                        logger.error(parentGroupId
                                + " Profile not amongst the defaults, skipped profile override update: "
                                + mGroup.getName());
                    }
                }
            }
        }
        SystemConfiguration profileOverideConfig = systemConfigurationManager
                .loadConfigByName("profileoverride.init.enable");
        profileOverideConfig.setValue("false");
        systemConfigurationManager.save(profileOverideConfig);
        logger.error("Profile override init completed");
    }

    /**
     * 1. Compares ENL default profiles to detect any changes made by the customer 2. Ensures that the customer changed
     * "default" profile are created as new instances and put in the same template along with ensuring that the fixture
     * are updated for a one time profile push. 3. ENL default profile values are restored on the changed profile
     * handler.
     */
    public void compareProfiles() {
        // Fetchs ENL provided default profile for the ENL provide groups
        List<ProfileHandler> mDefaultProfilesList = profileManager.getDefaultProfilesFromSystemConfigurationForComparison();
        // Fetches the existing "default" groups that are in the database.
        List<Groups> mDBDefaultGroupChangedList = new ArrayList<Groups>();
        
        // Now start comparison
        //ProfileHandlerCompare mComparer = new ProfileHandlerCompare();
        for (short i = 1; i <= 16; i++) {
            Groups mGroup = groupManager.getGroupByProfileNumber(i);
            if (mGroup != null)
                // Add only enlighted Provided defaults
                if (mGroup.getProfileNo() != null && mGroup.getProfileNo() <= 16 && mGroup.getProfileNo() > 0
                        && mGroup.getDerivedFromGroup() == null) 
                {
                    logger.error("Adding the changed default profile number for comparison : " + mGroup.getProfileNo());
                    mDBDefaultGroupChangedList.add(mGroup);
                }
        }

        try {
            for (int i = 0; i < mDBDefaultGroupChangedList.size(); i++) {
                Groups mChangedGroup = mDBDefaultGroupChangedList.get(i);
                ProfileHandler mChangedProfile = mChangedGroup.getProfileHandler();
                ProfileHandler mDefaultProfile = mDefaultProfilesList.get(mChangedGroup.getProfileNo() - 1); // Major
                
                int result = mDefaultProfile.compare(mChangedProfile); //mComparer.compare(mDefaultProfile, mChangedProfile);
                logger.error(mChangedGroup.getProfileNo() + " Result (0:No change, -1: requires new instance) : "
                        + result);
                if (result == -1) {
                	logger.error("Creating new instance for changed group/profile for profile number : "
                            + mChangedGroup.getProfileNo());
                    // Major- set the old default profile number
                    Short newInstanceProfileNo = groupManager.getMaxProfileNo(null);
                    if (newInstanceProfileNo != 0) {
                        // Create and save to DB
                        ProfileHandler mprofileHandler = createProfile("default.", 1, true);
                        mprofileHandler.copyFrom(mChangedProfile);
                        // set the newly created profile handler's profile group no
                        mprofileHandler.setProfileGroupId(newInstanceProfileNo);
    
                        // Create new Group to point to this profile handler
                        Groups group = new Groups();
                        String groupName = mChangedGroup.getName() + "_1";
                        group.setName(groupName);
                        group.setProfileHandler(mprofileHandler);
                        group.setProfileNo(newInstanceProfileNo);
                        group.setCompany(companyManager.getAllCompanies().get(0));
                        group.setProfileTemplate(mChangedGroup.getProfileTemplate());
                        group.setDisplayProfile(true);
                        group.setDefaultProfile(false);
                        group.setDerivedFromGroup(mChangedGroup);
                        metaDataManager.saveOrUpdateGroup(group);
    
                        // Push profile to fixtures
                        Long mNewGroupNumber = (long) group.getId();
                        Long mOldGroupNumber = mChangedGroup.getId();
                        // End push profile to fixtures that need to be transitioned to the newly created instance
                        profileDao.profileUpgrade(mOldGroupNumber, mNewGroupNumber, groupName);
    
                        // Restore the default values for the changed profile handler
                        if (mChangedGroup.getProfileNo() != 1 && mChangedProfile.getProfileGroupId() == 1) {
                        	logger.error("Creating new instance for group that was non 'DEFAULT' but pointing to default: "
                                    + mChangedGroup.getProfileNo());
    
                            ProfileHandler oNewPFH = new ProfileHandler();
                            oNewPFH = createProfile("default.", 1, true);
                            oNewPFH.copyFrom(mDefaultProfile);
                            // saved profile...
                            mChangedGroup.setProfileHandler(oNewPFH);
                            // save group;
                            metaDataManager.saveOrUpdateGroup(mChangedGroup);
                        } else {
                        	logger.error("Restoring the changed profile to ENL default values: "
                                    + mChangedGroup.getProfileNo());
                            mChangedProfile.copyFrom(mDefaultProfile);
                            saveProfileHandler(mChangedProfile);
                        }
                     // Calling Flushing method while saving group, so that groupManager.getMaxProfileNo() will return next available profile no from the database memory
                     // Previously without using Flush, groupManager.getMaxProfileNo was returning the same number present in the database all the time because of transaction was
                     // not in the committed state.
                        metaDataManager.flush();
                    }else {
                    	logger.error("Can't get profile no for creating new instance "
                                + mChangedGroup.getProfileNo());
                    }
                }
            }
        } catch (Exception e) {
        	logger.error(e.getMessage());
        }
        SystemConfiguration profileUpgradeEnableConfig = systemConfigurationManager
                .loadConfigByName("profileupgrade.enable");
        profileUpgradeEnableConfig.setValue("false");
        systemConfigurationManager.save(profileUpgradeEnableConfig);
        logger.error("Profile comparison completed");
    }
    
    public List<ProfileHandler> getDefaultProfilesFromSystemConfigurationForComparison() {
        List<ProfileHandler> mDefaultProfiles = new ArrayList<ProfileHandler>();
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String groupList = getConfigurationValue(scMap, "default.", "metadata.areas");
        if (!ArgumentUtils.isNullOrEmpty(groupList)) {
            String[] groups = groupList.split(",");
            ProfileHandler groupProfileHandler = null;
            String groupName = "";
            String groupkey = "";
            // Group Id 1 is set to Default Profile.
            for (int i = 0, groupId = 1; i < groups.length; i++, groupId++) {
                groupName = groups[i].trim();
                Groups group = new Groups();
                group.setName(groupName);
                groupkey = groupName.replaceAll(" ", "").toLowerCase();
                if (groupName.equals(ServerConstants.DEFAULT_PROFILE)) {
                    ProfileHandler profileHandler = profileManager.createProfile("default.",
                            ServerConstants.DEFAULT_PROFILE_GID, false);
                    group.setProfileHandler(profileHandler);
                    mDefaultProfiles.add(profileHandler);
                } else {
                    groupkey = "default." + groupkey + ".";
                    groupProfileHandler = createProfile(groupkey, groupId, false);
                    group.setProfileHandler(groupProfileHandler);
                    mDefaultProfiles.add(groupProfileHandler);
                }
            }
        }
        return mDefaultProfiles;
    }    
        
    public ProfileHandler createProfile(String strParamPrefix, int profileGroupId, boolean bSave) {
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String sParamPrefix = strParamPrefix;
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.setDarkLux(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.dark_lux")));
        profileHandler.setNeighborLux(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.neighbor_lux")));
        profileHandler.setEnvelopeOnLevel(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.envelope_on_level")));
        profileHandler.setDropPercent(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.drop")));
        profileHandler.setRisePercent(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.rise")));
        profileHandler.setDimBackoffTime(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.dim_backoff_time")));
        profileHandler.setIntensityNormTime(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.intensity_norm_time")));
        profileHandler.setOnAmbLightLevel(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.on_amb_light_level")));
        profileHandler.setMinLevelBeforeOff(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.min_level_before_off")));
        profileHandler.setRelaysConnected(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.relays_connected")));
        profileHandler.setStandaloneMotionOverride(Short.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.standalone_motion_override")));
        profileHandler.setDrReactivity(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.dr_reactivity")));
        profileHandler.setToOffLinger(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.to_off_linger")));
        profileHandler.setInitialOnLevel(Byte
                .valueOf(getConfigurationValue(scMap, sParamPrefix, "pfh.initial_on_level")));
        profileHandler.setInitialOnTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.initial_on_time")));
        profileHandler.setProfileGroupId((short) profileGroupId);
        profileHandler.setProfileFlag((byte) 0);
        profileHandler.setDrLowLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.dr_low")));
        profileHandler.setDrModerateLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.dr_moderate")));
        profileHandler.setDrHighLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.dr_high")));
        profileHandler.setDrSpecialLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "pfh.dr_special")));

        for (int i = 0; i < 12; i++) {
            Profile profile = new Profile();
            switch (i) {
            case 0:
            case 4:
            case 8:
                sParamPrefix = strParamPrefix;
                if (i == 4)
                    sParamPrefix += "weekend.";
                else if (i == 8)
                    sParamPrefix += "holiday.";
                profile.setMinLevel(Long
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.morning.min_level")));
                profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.morning.on_level")));
                profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.morning.motion_detect_duration")));
                profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.morning.manual_override_duration")));
                profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.morning.motion_sensitivity")));
                profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.morning.ramp_up_time")));
                profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.morning.ambient_sensitivity")));
                if (i == 0)
                    profileHandler.setMorningProfile(profile);
                else if (i == 4)
                    profileHandler.setMorningProfileWeekEnd(profile);
                else if (i == 8)
                    profileHandler.setMorningProfileHoliday(profile);
                break;
            case 1:
            case 5:
            case 9:
                sParamPrefix = strParamPrefix;
                if (i == 5)
                    sParamPrefix += "weekend.";
                else if (i == 9)
                    sParamPrefix += "holiday.";
                profile.setMinLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.day.min_level")));
                profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.day.on_level")));
                profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.day.motion_detect_duration")));
                profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.day.manual_override_duration")));
                profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.day.motion_sensitivity")));
                profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.day.ramp_up_time")));
                profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.day.ambient_sensitivity")));
                if (i == 1)
                    profileHandler.setDayProfile(profile);
                else if (i == 5)
                    profileHandler.setDayProfileWeekEnd(profile);
                else if (i == 9)
                    profileHandler.setDayProfileHoliday(profile);
                break;
            case 2:
            case 6:
            case 10:
                sParamPrefix = strParamPrefix;
                if (i == 6)
                    sParamPrefix += "weekend.";
                else if (i == 10)
                    sParamPrefix += "holiday.";
                profile.setMinLevel(Long
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.evening.min_level")));
                profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.evening.on_level")));
                profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.evening.motion_detect_duration")));
                profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.evening.manual_override_duration")));
                profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.evening.motion_sensitivity")));
                profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.evening.ramp_up_time")));
                profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.evening.ambient_sensitivity")));
                if (i == 2)
                    profileHandler.setEveningProfile(profile);
                else if (i == 6)
                    profileHandler.setEveningProfileWeekEnd(profile);
                else if (i == 10)
                    profileHandler.setEveningProfileHoliday(profile);
                break;
            case 3:
            case 7:
            case 11:
                sParamPrefix = strParamPrefix;
                if (i == 7)
                    sParamPrefix += "weekend.";
                else if (i == 11)
                    sParamPrefix += "holiday.";
                profile.setMinLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.night.min_level")));
                profile.setOnLevel(Long.valueOf(getConfigurationValue(scMap, sParamPrefix, "profile.night.on_level")));
                profile.setMotionDetectDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.night.motion_detect_duration")));
                profile.setManualOverrideDuration(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.night.manual_override_duration")));
                profile.setMotionSensitivity(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.night.motion_sensitivity")));
                profile.setRampUpTime(Long.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.night.ramp_up_time")));
                profile.setAmbientSensitivity(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "profile.night.ambient_sensitivity")));
                if (i == 3)
                    profileHandler.setNightProfile(profile);
                else if (i == 7)
                    profileHandler.setNightProfileWeekEnd(profile);
                else if (i == 11)
                    profileHandler.setNightProfileHoliday(profile);
                break;
            }
            if (bSave) {
                saveProfile(profile);
            }
        }
        
        Profile override5 = new Profile();
        override5.setAmbientSensitivity(10);
        override5.setManualOverrideDuration(60L);
        override5.setMinLevel(0L);
        override5.setMotionDetectDuration(1L);
        override5.setMotionSensitivity(10L);
        override5.setOnLevel(0L);
        override5.setRampUpTime(0L);
        saveProfile(override5);
        profileHandler.setOverride5(override5);
        
        Profile override6 = new Profile();
        override6.setAmbientSensitivity(10);
        override6.setManualOverrideDuration(60L);
        override6.setMinLevel(0L);
        override6.setMotionDetectDuration(1L);
        override6.setMotionSensitivity(10L);
        override6.setOnLevel(50L);
        override6.setRampUpTime(0L);
        saveProfile(override6);
        profileHandler.setOverride6(override6);
        
        Profile override7 = new Profile();
        override7.setAmbientSensitivity(10);
        override7.setManualOverrideDuration(60L);
        override7.setMinLevel(0L);
        override7.setMotionDetectDuration(1L);
        override7.setMotionSensitivity(10L);
        override7.setOnLevel(50L);
        override7.setRampUpTime(0L);
        saveProfile(override7);
        profileHandler.setOverride7(override7);
        
        Profile override8 = new Profile();
        override8.setAmbientSensitivity(10);
        override8.setManualOverrideDuration(60L);
        override8.setMinLevel(0L);
        override8.setMotionDetectDuration(1L);
        override8.setMotionSensitivity(10L);
        override8.setOnLevel(50L);
        override8.setRampUpTime(0L);
        saveProfile(override8);
        profileHandler.setOverride8(override8);
        
        
        sParamPrefix = strParamPrefix;
        ProfileConfiguration profileConfiguration = new ProfileConfiguration();
        profileConfiguration.setMorningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.morning_time"));
        profileConfiguration.setDayTime(getConfigurationValue(scMap, sParamPrefix, "pfc.day_time"));
        profileConfiguration.setEveningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.evening_time"));
        profileConfiguration.setNightTime(getConfigurationValue(scMap, sParamPrefix, "pfc.night_time"));
        // Save profile Configuration
        if (bSave) {
            saveProfileConfiguration(profileConfiguration);
        }
        saveDefaultWeekDays(scMap, profileConfiguration, bSave);
        // Set profile configuration to handler.
        profileHandler.setProfileConfiguration(profileConfiguration);
        if(bSave) {
            saveProfileHandler(profileHandler);
        }
        return profileHandler;
    }

    public void saveDefaultWeekDays(HashMap<String, String> scMap, ProfileConfiguration profileConfiguration, boolean bSave) {
        String weekDayList = getConfigurationValue(scMap, "default.", "metadata.weekday");
        String weekEndList = getConfigurationValue(scMap, "default.", "metadata.weekend");
        int order = 0;
        Set<WeekDay> oWeekDays = new HashSet<WeekDay>();
        if (!ArgumentUtils.isNullOrEmpty(weekDayList)) {
            String[] weekdays = weekDayList.split(",");
            for (int i = 0; i < weekdays.length; i++) {
                WeekDay weekDay = new WeekDay();
                weekDay.setDay(weekdays[i].trim());
                weekDay.setProfileConfiguration(profileConfiguration);
                weekDay.setShortOrder(++order);
                weekDay.setType("weekday");
                oWeekDays.add(weekDay);
                if(bSave) {
                    metaDataManager.saveOrUpdateWeekDay(weekDay);
                }
            }
        }
        if (!ArgumentUtils.isNullOrEmpty(weekEndList)) {
            String[] weekends = weekEndList.split(",");
            for (int i = 0; i < weekends.length; i++) {
                WeekDay weekDay = new WeekDay();
                weekDay.setDay(weekends[i].trim());
                weekDay.setProfileConfiguration(profileConfiguration);
                weekDay.setShortOrder(++order);
                weekDay.setType("weekend");
                oWeekDays.add(weekDay);
                if(bSave) {
                    metaDataManager.saveOrUpdateWeekDay(weekDay);
                }
            }
        }
        profileConfiguration.setWeekDays(oWeekDays);
    }
  
    /**
     * Returns default values, in case the values are not pre-populated in the database then makes sure that the
     * defaults are still returned.
     * 
     * @param scMap
     *            Map containing key-value pairs.
     * @param key
     *            "name of the configuration parameter"
     * @return value "value of the configuration parameter"
     */
    public String getConfigurationValue(HashMap<String, String> scMap, String sPrefix, String key) {
        String sValue = scMap.get(sPrefix + key);
        if (sValue == null || sValue.equals("")) {        	
        	// You should make sure that it can be canned profile too , or else other kind of profile
        	String[] splitString;
			splitString = sPrefix.split("\\.");
			String cannedPrefix = splitString[1];
			String parentProfilePrefix = scMap.get(cannedPrefix);   // Now parent --should use only splitString[1] for name
																	// profile
																	// contains
																	// the
																	// actual
																	// values.
			// Canned Profile Start
			boolean cannedExist = false;
			// One more condition to check for the flag in
			// canned_profiles_configuration table.
			Boolean status = null;
			try {
				CannedProfileConfiguration cannedConfig = cannedProfileConfigurationManager.loadConfigByName(cannedPrefix);
				if(cannedConfig!=null) 
				status = cannedConfig.getStatus();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.info("Canned Profile , Exception for :"+cannedPrefix+" : "+e.getMessage());				
				e.printStackTrace();
			}
			if (parentProfilePrefix != null
					&& !parentProfilePrefix.equalsIgnoreCase("")
					&& status == false) {
				cannedExist = true;
			}
			if (cannedExist) {
						if (sPrefix.contains("weekend")) {
							sValue = scMap.get(parentProfilePrefix + "weekend." + key);
						} else if (sPrefix.contains("holiday")) {
							sValue = scMap.get(parentProfilePrefix + "holiday." + key);
						} else {
							// It is weekday or advanced values
							sValue = scMap.get(parentProfilePrefix + key);
						}
			} 
			else { 
			            if (sPrefix.contains("weekend"))
			                sValue = scMap.get("default.weekend." + key);
			            else if (sPrefix.contains("holiday"))
			                sValue = scMap.get("default.holiday." + key);
			            else
			                sValue = scMap.get("default." + key);
			}
            if (sValue == null || sValue.equals("")) {
                if (key.endsWith("profile.morning.min_level"))
                    sValue = "0";
                else if (key.endsWith("profile.morning.on_level"))
                    sValue = "75";
                else if (key.endsWith("profile.morning.motion_detect_duration"))
                    sValue = "5";
                else if (key.endsWith("profile.morning.manual_override_duration"))
                    sValue = "60";
                else if (key.endsWith("profile.morning.motion_sensitivity"))
                    sValue = "1";
                else if (key.endsWith("profile.morning.ramp_up_time"))
                    sValue = "0";
                else if (key.endsWith("profile.morning.ambient_sensitivity"))
                    sValue = "5";

                else if (key.endsWith("profile.day.min_level"))
                    sValue = "20";
                else if (key.endsWith("profile.day.on_level"))
                    sValue = "75";
                else if (key.endsWith("profile.day.motion_detect_duration"))
                    sValue = "15";
                else if (key.endsWith("profile.day.manual_override_duration"))
                    sValue = "60";
                else if (key.endsWith("profile.day.motion_sensitivity"))
                    sValue = "1";
                else if (key.endsWith("profile.day.ramp_up_time"))
                    sValue = "0";
                else if (key.endsWith("profile.day.ambient_sensitivity"))
                    sValue = "5";

                else if (key.endsWith("profile.evening.min_level"))
                    sValue = "0";
                else if (key.endsWith("profile.evening.on_level"))
                    sValue = "75";
                else if (key.endsWith("profile.evening.motion_detect_duration"))
                    sValue = "5";
                else if (key.endsWith("profile.evening.manual_override_duration"))
                    sValue = "60";
                else if (key.endsWith("profile.evening.motion_sensitivity"))
                    sValue = "1";
                else if (key.endsWith("profile.evening.ramp_up_time"))
                    sValue = "0";
                else if (key.endsWith("profile.evening.ambient_sensitivity"))
                    sValue = "5";

                else if (key.endsWith("profile.night.min_level"))
                    sValue = "0";
                else if (key.endsWith("profile.night.on_level"))
                    sValue = "75";
                else if (key.endsWith("profile.night.motion_detect_duration"))
                    sValue = "5";
                else if (key.endsWith("profile.night.manual_override_duration"))
                    sValue = "60";
                else if (key.endsWith("profile.night.motion_sensitivity"))
                    sValue = "1";
                else if (key.endsWith("profile.night.ramp_up_time"))
                    sValue = "0";
                else if (key.endsWith("profile.night.ambient_sensitivity"))
                    sValue = "5";

                // Advance Global variables...
                else if (key.equals("pfh.dark_lux"))
                    sValue = "20";
                else if (key.equals("pfh.neighbor_lux"))
                    sValue = "200";
                else if (key.equals("pfh.envelope_on_level"))
                    sValue = "50";
                else if (key.equals("pfh.drop"))
                    sValue = "10";
                else if (key.equals("pfh.rise"))
                    sValue = "20";
                else if (key.equals("pfh.dim_backoff_time"))
                    sValue = "10";
                else if (key.equals("pfh.intensity_norm_time"))
                    sValue = "10";
                else if (key.equals("pfh.on_amb_light_level"))
                    sValue = "0";
                else if (key.equals("pfh.min_level_before_off"))
                    sValue = "20";
                else if (key.equals("pfh.relays_connected"))
                    sValue = "1";
                else if (key.equals("pfh.standalone_motion_override"))
                    sValue = "0";
                else if (key.equals("pfh.dr_reactivity"))
                    sValue = "0";
                else if (key.equals("pfh.to_off_linger"))
                    sValue = "30";
                else if (key.equals("pfh.initial_on_level"))
                    sValue = "50";
                else if (key.equals("pfh.initial_on_time"))
                    sValue = "5";
                else if (key.equals("pfh.profile_group_id"))
                    sValue = "1";

                else if (key.equals("pfc.morning_time"))
                    sValue = "6:00 AM";
                else if (key.equals("pfc.day_time"))
                    sValue = "9:00 AM";
                else if (key.equals("pfc.evening_time"))
                    sValue = "6:00 PM";
                else if (key.equals("pfc.night_time"))
                    sValue = "9:00 PM";
                else
                    sValue = "0";
            }
        }
        return sValue;
    }   
        
    public void createCannedProfiles()
    {    	
		try {
			List<CannedProfileConfiguration> mConfigList = cannedProfileConfigurationManager
					.loadAllConfig();
			if (mConfigList != null && mConfigList.size() > 0)
				for (Iterator<CannedProfileConfiguration> iterator = mConfigList
						.iterator(); iterator.hasNext();) {
					CannedProfileConfiguration cannedProfileConfiguration = (CannedProfileConfiguration) iterator
							.next();
					if (cannedProfileConfiguration.getStatus() == false) { // Create
																			// only
																			// if
																			// the
																			// flag
																			// is
																			// set
																			// to
																			// false
						String groupKey = cannedProfileConfiguration.getName();						
						Integer parentId = cannedProfileConfiguration
								.getParentProfileid();
						Short newInstanceProfileNo = groupManager
								.getMaxProfileNo(null);
						/*String[] splitGroupName = cannedProfileConfiguration
								.getName().split("\\.");
*/
						String groupName = groupKey;//splitGroupName[1];
						Groups derivedGrp = groupManager.getGroupByProfileNumber(Short.valueOf(String.valueOf(parentId)));
						
						if (newInstanceProfileNo != 0
								&& groupManager.getGroupByName(groupName) == null
								&& derivedGrp != null) // Existing group should not
														// exist but derived groups should be present
						{
							groupKey = groupKey.replaceAll(" ",
							 "").toLowerCase();
							ProfileHandler profileHandler = profileManager
									.createProfile("default." + groupKey
											+ ".", parentId, true);
							profileHandler.setProfileGroupId(newInstanceProfileNo);

							// saveProfileHandler(profileHandler);
							Company company = companyManager.getCompany();
							Groups group = new Groups();
							group.setProfileNo(newInstanceProfileNo);
							group.setName(groupName);
							group.setDefaultProfile(false);
							group.setDisplayProfile(true);
							ProfileTemplate profileTemplate = profileTemplateManager
									.getProfileTemplateById(derivedGrp
											.getProfileTemplate().getId());
							group.setProfileTemplate(profileTemplate);
							group.setDerivedFromGroup(derivedGrp);
							group.setProfileHandler(profileHandler);
							group.setCompany(company);
							metaDataManager.saveOrUpdateGroup(group);
							cannedProfileConfiguration.setStatus(true); // Mark the
																		// canned
																		// profile
																		// as
																		// created
							cannedProfileConfigurationManager
									.save(cannedProfileConfiguration);
							metaDataManager.flush();
						}
					}
				}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			logger.info("Canned Profile : "+e.getStackTrace());
			e.printStackTrace();
		}catch(Exception e)	{
			logger.info("Canned Profile : "+e.getStackTrace());
			e.printStackTrace();
		}
		
    }

    public void saveDefaultGroups(final ProfileHandler profileHandler, final Company company) {
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String groupList = getConfigurationValue(scMap, "default.", "metadata.areas");
        if (!ArgumentUtils.isNullOrEmpty(groupList)) {
            String[] groups = groupList.split(",");
            ProfileHandler groupProfileHandler = null;
            String groupName = "";
            String groupkey = "";
            // Group Id 1 is set to Default Profile.
            for (int i = 0, groupId = 1; i < groups.length; i++, groupId++) {
                groupName = groups[i].trim();
                Groups group = new Groups();
                ProfileTemplate profileTemplate=null;
                group.setName(groupName);
                groupkey = groupName.replaceAll(" ", "").toLowerCase();
                if (groupName.equals(ServerConstants.DEFAULT_PROFILE)) {
                    group.setProfileHandler(profileHandler);
                } else {
                    groupkey = "default." + groupkey + ".";
                    groupProfileHandler = createProfile(groupkey, groupId, true);
                    group.setProfileHandler(groupProfileHandler);
                }
                if(groupId<=groups.length)
                {
	                group.setProfileNo((short) groupId);
	                group.setCompany(company);
	                profileTemplate = new ProfileTemplate();
	                profileTemplate.setName(groupName);
	                profileTemplate.setDisplayTemplate(true);
	                profileTemplate.setTemplateNo((long) groupId);
                }
                if (groupName.equals(ServerConstants.OUTDOOR_PROFILE)) {
                    ProfileConfiguration pfConfiguration =groupProfileHandler.getProfileConfiguration();
                    // make all day as weekday for the outdoor profile
                    Set<WeekDay> week = pfConfiguration.getWeekDays();
                        for (WeekDay day : week) {
                            if (day.getType().equalsIgnoreCase("weekend")) {
                                    day.setType("weekday");
                            }
                        }
                }
                profileTemplateManager.save(profileTemplate);                
                group.setProfileTemplate(profileTemplate);
                group.setDisplayProfile(true);
                group.setDefaultProfile(true);
                metaDataManager.saveOrUpdateGroup(group);
                
            }
        }
    }

	public int getOnLevel(Long fixtureId, int dayOfWeek, int minOfDay) {
		return profileDao.getOnLevel(fixtureId, dayOfWeek, minOfDay);
	}
	
	public int getMinLevel(Long fixtureId, int dayOfWeek, int minOfDay) {
		return profileDao.getMinLevel(fixtureId, dayOfWeek, minOfDay);
	}

    public void addMoreDefaultProfile() {
        //Add Outdoor profile as new default profiles
        Short newInstanceProfileNo = groupManager.getMaxProfileNo(null);
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String groupName = "";
        String groupkey = "";
        String groupList = getConfigurationValue(scMap, "default.", "metadata.areas");
        if (!ArgumentUtils.isNullOrEmpty(groupList)) {
            String[] groups = groupList.split(",");

            for (int i = 0; i < groups.length; i++) {
                String gName = groups[i].trim();
                if (gName != null && gName.equals(ServerConstants.OUTDOOR_PROFILE)) {
                    groupName = gName;
                    groupkey = groupName.replaceAll(" ", "").toLowerCase();
                    break;
                }
            }
            ProfileHandler pfHandler = null;

            ProfileTemplate profileTemplate = new ProfileTemplate();
            profileTemplate.setName(groupName);
            profileTemplate.setDisplayTemplate(true);
            profileTemplate.setTemplateNo((long) newInstanceProfileNo);
            profileTemplateManager.save(profileTemplate);

            Groups group = groupManager.getGroupByName(groupName);
            if(group==null)
            {
                group = new Groups();
                if (groupName.equals(ServerConstants.OUTDOOR_PROFILE)) {
                    groupkey = "default." + groupkey + ".";
                    pfHandler = createProfile(groupkey, newInstanceProfileNo, true);
                    ProfileConfiguration pfConfiguration = pfHandler.getProfileConfiguration();
                    // make all day as weekday for the outdoor profile
                    Set<WeekDay> week = pfConfiguration.getWeekDays();
                        for (WeekDay day : week) {
                            if (day.getType().equalsIgnoreCase("weekend")) {
                                    day.setType("weekday");
                            }
                        }
                        group.setProfileHandler(pfHandler);
                }
                Company company = companyManager.getCompany();
                group.setProfileNo(newInstanceProfileNo);
                group.setName(groupName);
                group.setDefaultProfile(true);
                group.setDisplayProfile(true);

                group.setProfileTemplate(profileTemplate);
                group.setCompany(company);
                metaDataManager.saveOrUpdateGroup(group);
            }else
            {
                logger.debug("Outdoor profile already exists");
            }
            SystemConfiguration addMoreProfileConfig = systemConfigurationManager
                    .loadConfigByName("add.more.defaultprofile");
            addMoreProfileConfig.setValue("false");
            systemConfigurationManager.save(addMoreProfileConfig);
        }
    }

}
