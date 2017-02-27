package com.ems.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ProfileDao;
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
    
    @Resource(name = "companyManager")
	private CompanyManager companyManager;
    
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
            if (sPrefix.contains("weekend"))
                sValue = scMap.get("default.weekend." + key);
            else if (sPrefix.contains("holiday"))
                sValue = scMap.get("default.holiday." + key);
            else
                sValue = scMap.get("default." + key);
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

}