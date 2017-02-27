package com.ems.service;

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
import com.ems.model.WeekDay;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Service("profileManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileManager {

    static final Logger logger = Logger.getLogger("ProfileLogger");

    @Resource
    ProfileDao profileDao;

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

    public ProfileHandler createProfile(String strParamPrefix, int profileGroupId) {
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
        profileHandler.setProfileGroupId((byte) profileGroupId);
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
            saveProfile(profile);
        }
        ProfileConfiguration profileConfiguration = new ProfileConfiguration();
        profileConfiguration.setMorningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.morning_time"));
        profileConfiguration.setDayTime(getConfigurationValue(scMap, sParamPrefix, "pfc.day_time"));
        profileConfiguration.setEveningTime(getConfigurationValue(scMap, sParamPrefix, "pfc.evening_time"));
        profileConfiguration.setNightTime(getConfigurationValue(scMap, sParamPrefix, "pfc.night_time"));
        // Save profile Configuration
        saveProfileConfiguration(profileConfiguration);
        saveDefaultWeekDays(scMap, profileConfiguration);
        // Set profile configuration to handler.
        profileHandler.setProfileConfiguration(profileConfiguration);
        saveProfileHandler(profileHandler);
        return profileHandler;
    }

    public void saveDefaultWeekDays(HashMap<String, String> scMap, ProfileConfiguration profileConfiguration) {
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
                metaDataManager.saveOrUpdateWeekDay(weekDay);
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
                metaDataManager.saveOrUpdateWeekDay(weekDay);
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
        // new Thread() {
        // public void run() {
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
                    group.setProfileHandler(profileHandler);
                } else {
                    groupkey = "default." + groupkey + ".";
                    groupProfileHandler = createProfile(groupkey, groupId);
                    group.setProfileHandler(groupProfileHandler);
                }
                group.setCompany(company);
                metaDataManager.saveOrUpdateGroup(group);
            }
        }
        // }
        // }.start();
    }

	public int getOnLevel(Long fixtureId, int dayOfWeek, int minOfDay) {
		return profileDao.getOnLevel(fixtureId, dayOfWeek, minOfDay);
	}

}
