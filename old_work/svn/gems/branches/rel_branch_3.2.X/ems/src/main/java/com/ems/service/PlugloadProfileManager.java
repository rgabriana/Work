package com.ems.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.MetaDataDao;
import com.ems.dao.PlugloadProfileDao;
import com.ems.model.Company;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.PlugloadProfileTemplate;
import com.ems.model.WeekdayPlugload;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

@Service("plugloadProfileManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadProfileManager {

	@Resource
	PlugloadProfileDao plugloadProfileDao;

	@Resource
	MetaDataManager metaDataManager;
	@Resource
	private MetaDataDao metaDataDao;

	@Resource
	PlugloadManager plugloadManager;

	@Resource
	PlugloadGroupManager plugloadGroupManager;
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource(name = "plugloadProfileTemplateManager")
    private PlugloadProfileTemplateManager plugloadProfileTemplateManager;
	
	@Resource(name = "companyManager")
    private CompanyManager companyManager;

	public PlugloadProfileHandler getProfileHandlerById(Long id) {
		return plugloadProfileDao.getProfileHandlerById(id);
	}

	public PlugloadProfileHandler savePlugloadProfileHandler(
			PlugloadProfileHandler plugloadProfileHandler) {
		return plugloadProfileDao.savePlugloadProfileHandler(plugloadProfileHandler);
	}

	public PlugloadProfileHandler createPlugloadProfiles(
			PlugloadProfileHandler ph, String param) {
		System.out.println("inside create");
		PlugloadProfileHandler plugloadProfileHandler = new PlugloadProfileHandler();
		PlugloadProfile plugloadProfile, fromPlugloadProfile;

		for (int i = 0; i < 12; i++) {
			plugloadProfile = new PlugloadProfile();
			switch (i) {
			case 0:
				fromPlugloadProfile = ph.getMorningProfile();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setMorningProfile(plugloadProfile);
				break;
			case 4:
				fromPlugloadProfile = ph.getMorningProfileWeekEnd();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler
						.setMorningProfileWeekEnd(plugloadProfile);
				break;
			case 8:
				fromPlugloadProfile = ph.getMorningProfileHoliday();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler
						.setMorningProfileHoliday(plugloadProfile);
				break;
			case 1:
				fromPlugloadProfile = ph.getDayProfile();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setDayProfile(plugloadProfile);
				break;
			case 5:
				fromPlugloadProfile = ph.getDayProfileWeekEnd();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setDayProfileWeekEnd(plugloadProfile);
				break;
			case 9:
				fromPlugloadProfile = ph.getDayProfileHoliday();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setDayProfileHoliday(plugloadProfile);
				break;
			case 2:
				fromPlugloadProfile = ph.getEveningProfile();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setEveningProfile(plugloadProfile);
				break;
			case 6:
				fromPlugloadProfile = ph.getEveningProfileWeekEnd();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler
						.setEveningProfileWeekEnd(plugloadProfile);
				break;
			case 10:
				fromPlugloadProfile = ph.getEveningProfileHoliday();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler
						.setEveningProfileHoliday(plugloadProfile);
				break;
			case 3:
				fromPlugloadProfile = ph.getNightProfile();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setNightProfile(plugloadProfile);
				break;
			case 7:
				fromPlugloadProfile = ph.getNightProfileWeekEnd();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setNightProfileWeekEnd(plugloadProfile);
				break;
			case 11:
				fromPlugloadProfile = ph.getNightProfileHoliday();
				plugloadProfile.setActiveMotion(fromPlugloadProfile
						.getActiveMotion());
				plugloadProfile.setMode(fromPlugloadProfile.getMode());
				plugloadProfileHandler.setNightProfileHoliday(plugloadProfile);
				break;
			default:
				break;

			}

			saveProfile(plugloadProfile);

		}
		PlugloadProfileConfiguration fromPlugloadProfileConfiguration = ph
				.getPlugloadProfileConfiguration();
		PlugloadProfileConfiguration plugloadProfileConfiguration = new PlugloadProfileConfiguration();
		plugloadProfileConfiguration
				.setMorningTime(fromPlugloadProfileConfiguration
						.getMorningTime());
		plugloadProfileConfiguration
				.setDayTime(fromPlugloadProfileConfiguration.getDayTime());
		plugloadProfileConfiguration
				.setEveningTime(fromPlugloadProfileConfiguration
						.getEveningTime());
		plugloadProfileConfiguration
				.setNightTime(fromPlugloadProfileConfiguration.getNightTime());
		// System.out.println("weekday is "+fromPlugloadProfileConfiguration.getWeekDays());
		// saveWeekdayPlugload(fromPlugloadProfileConfiguration.getWeekDays());
		plugloadProfileConfiguration = saveProfileConfiguration(plugloadProfileConfiguration);
		saveWeekDays(param, plugloadProfileConfiguration, true);
		plugloadProfileHandler.setInitialOnLevel(ph.getInitialOnLevel());
		plugloadProfileHandler.setInitialOnTime(ph.getInitialOnTime());
		plugloadProfileHandler.setSafetyMode(ph.getSafetyMode());
		plugloadProfileHandler.setNoOfMissedHeartbeats(ph
				.getNoOfMissedHeartbeats());
		plugloadProfileHandler.setHeartbeatInterval(ph.getHeartbeatInterval());
		plugloadProfileHandler.copyOverrideProfilesFrom(ph);
		plugloadProfileHandler
				.setPlugloadProfileConfiguration(plugloadProfileConfiguration);
		System.out.println("values are " + ph.getSafetyMode() + " "
				+ ph.getNoOfMissedHeartbeats() + " "
				+ ph.getHeartbeatLingerPeriod());
		System.out.println("new values are "
				+ plugloadProfileHandler.getSafetyMode() + " "
				+ plugloadProfileHandler.getNoOfMissedHeartbeats() + " "
				+ plugloadProfileHandler.getHeartbeatLingerPeriod());
		savePlugloadProfileHandler(plugloadProfileHandler);

		//Long newInstanceProfileNo =	plugloadGroupManager.getMaxProfileNo(null);
		//PlugloadGroups plugloadGroups = new PlugloadGroups();
		//plugloadGroups.setName("Test"); // get the group name
		//plugloadGroups.setPlugloadProfileHandler(plugloadProfileHandler);
		//plugloadGroups.setProfileNo(newInstanceProfileNo);
		//plugloadGroups.setDisplayProfile(true);
		//plugloadGroups.setDefaultProfile(false);
		//metaDataManager.saveOrUpdatePlugloadGroup(plugloadGroups);

		return plugloadProfileHandler;

	}

	public PlugloadProfileConfiguration saveProfileConfiguration(
			PlugloadProfileConfiguration profileConfiguration) {
		return plugloadProfileDao
				.savePlugloadProfileConfiguration(profileConfiguration);
	}

	public void saveProfile(PlugloadProfile profile) {
		plugloadProfileDao.saveProfile(profile);
	}

	public void saveWeekDays(String weekdayString,
			PlugloadProfileConfiguration profileConfiguration, boolean bSave) {

		int order = 0;
		Set<WeekdayPlugload> oWeekDays = new HashSet<WeekdayPlugload>();

		String[] weekdays = weekdayString.split(",");
		for (int i = 0; i < weekdays.length; i++) {
			WeekdayPlugload weekDay = new WeekdayPlugload();
			weekDay.setDay(weekDay.days.get(i));
			weekDay.setPlugloadProfileConfiguration(profileConfiguration);
			weekDay.setShortOrder(++order);
			weekDay.setType(weekdays[i].equals("true") ? "weekday" : "weekend");
			oWeekDays.add(weekDay);
			if (bSave) {
				metaDataManager.saveOrUpdateWeekdayPlugload(weekDay);
			}
		}
		profileConfiguration.setWeekDays(oWeekDays);
	}

	public void saveWeekdayPlugload(Set<WeekdayPlugload> weekdays) {
		int i = 0;

		Iterator<WeekdayPlugload> iter = weekdays.iterator();
		while (iter.hasNext()) {
			System.out.println("weekdya is " + iter.next());
		}

		for (WeekdayPlugload w : weekdays) {
			System.out.println(++i + "weekday is " + w.getDay() + " "
					+ w.getType());
		}

		/*
		 * int order = 0; Set<WeekdayPlugload> oWeekDays = new
		 * HashSet<WeekdayPlugload>();
		 * 
		 * String[] weekdays = weekdayString.split(","); for (int i = 0; i <
		 * weekdays.length; i++) { WeekdayPlugload weekDay = new
		 * WeekdayPlugload(); weekDay.setDay(weekDay.days.get(i));
		 * weekDay.setPlugloadProfileConfiguration(profileConfiguration);
		 * weekDay.setShortOrder(++order);
		 * weekDay.setType(weekdays[i].equals("true") ? "weekday" : "weekend");
		 * oWeekDays.add(weekDay); if(bSave) {
		 * metaDataManager.saveOrUpdateWeekdayPlugload(weekDay); } }
		 * profileConfiguration.setWeekDays(oWeekDays);
		 */
	}

	public void changePlugloadProfile(Long plugloadId, Long groupId,
			String currentProfile, String originalProfileFrom) {
		
		plugloadProfileDao.changePlugloadProfile(plugloadId, groupId, currentProfile, originalProfileFrom);

	}
	
	public int getProfileModeForPlugload(Long plugloadId, int dayOfWeek, int minOfDay) {
		
		return plugloadProfileDao.getProfileModeForPlugload(plugloadId, dayOfWeek, minOfDay);
		
	} //end of method getProfileModeForPlugload
	
	public PlugloadProfileHandler createPlugloadProfile(String strParamPrefix, int plugloadProfileGroupId, boolean bSave) {
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String sParamPrefix = strParamPrefix;
        PlugloadProfileHandler plugloadProfileHandler = new PlugloadProfileHandler();
        plugloadProfileHandler.setInitialOnTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "plpfh.initial_on_time")));
        plugloadProfileHandler.setHeartbeatInterval(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "plpfh.heartbeat_interval")));
        plugloadProfileHandler.setHeartbeatLingerPeriod(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "plpfh.heartbeat_linger_period")));
        plugloadProfileHandler.setNoOfMissedHeartbeats(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "plpfh.no_of_missed_heartbeats")));
        plugloadProfileHandler.setSafetyMode(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix, "plpfh.fallback_mode")));
        plugloadProfileHandler.setProfileGroupId((short) plugloadProfileGroupId);
        plugloadProfileHandler.setProfileFlag((byte) 0);
        plugloadProfileHandler.setDrLowLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "plpfh.dr_low")));
        plugloadProfileHandler.setDrModerateLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "plpfh.dr_moderate")));
        plugloadProfileHandler.setDrHighLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "plpfh.dr_high")));
        plugloadProfileHandler.setDrSpecialLevel(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix,
                "plpfh.dr_special")));

        for (int i = 0; i < 12; i++) {
            PlugloadProfile plugloadProfile = new PlugloadProfile();
            switch (i) {
            case 0:
            case 4:
            case 8:
                sParamPrefix = strParamPrefix;
                if (i == 4)
                    sParamPrefix += "weekend.";
                else if (i == 8)
                    sParamPrefix += "holiday.";
                plugloadProfile.setActiveMotion(Integer
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.morning.active_motion_window")));
                plugloadProfile.setMode(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.morning.mode")));
                plugloadProfile.setManualOverrideTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "plugloadprofile.morning.manual_override_time")));
                if (i == 0)
                	plugloadProfileHandler.setMorningProfile(plugloadProfile);
                else if (i == 4)
                	plugloadProfileHandler.setMorningProfileWeekEnd(plugloadProfile);
                else if (i == 8)
                	plugloadProfileHandler.setMorningProfileHoliday(plugloadProfile);
                break;
            case 1:
            case 5:
            case 9:
                sParamPrefix = strParamPrefix;
                if (i == 5)
                    sParamPrefix += "weekend.";
                else if (i == 9)
                    sParamPrefix += "holiday.";
                plugloadProfile.setActiveMotion(Integer
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.day.active_motion_window")));
                plugloadProfile.setMode(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.day.mode")));
                plugloadProfile.setManualOverrideTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "plugloadprofile.day.manual_override_time")));
                if (i == 1)
                	plugloadProfileHandler.setDayProfile(plugloadProfile);
                else if (i == 5)
                	plugloadProfileHandler.setDayProfileWeekEnd(plugloadProfile);
                else if (i == 9)
                	plugloadProfileHandler.setDayProfileHoliday(plugloadProfile);
                break;
            case 2:
            case 6:
            case 10:
                sParamPrefix = strParamPrefix;
                if (i == 6)
                    sParamPrefix += "weekend.";
                else if (i == 10)
                    sParamPrefix += "holiday.";
                plugloadProfile.setActiveMotion(Integer
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.evening.active_motion_window")));
                plugloadProfile.setMode(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.evening.mode")));
                plugloadProfile.setManualOverrideTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "plugloadprofile.evening.manual_override_time")));
                if (i == 2)
                	plugloadProfileHandler.setEveningProfile(plugloadProfile);
                else if (i == 6)
                	plugloadProfileHandler.setEveningProfileWeekEnd(plugloadProfile);
                else if (i == 10)
                	plugloadProfileHandler.setEveningProfileHoliday(plugloadProfile);
                break;
            case 3:
            case 7:
            case 11:
                sParamPrefix = strParamPrefix;
                if (i == 7)
                    sParamPrefix += "weekend.";
                else if (i == 11)
                    sParamPrefix += "holiday.";
                plugloadProfile.setActiveMotion(Integer
                        .valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.night.active_motion_window")));
                plugloadProfile.setMode(Byte.valueOf(getConfigurationValue(scMap, sParamPrefix, "plugloadprofile.night.mode")));
                plugloadProfile.setManualOverrideTime(Integer.valueOf(getConfigurationValue(scMap, sParamPrefix,
                        "plugloadprofile.night.manual_override_time")));
                if (i == 3)
                	plugloadProfileHandler.setNightProfile(plugloadProfile);
                else if (i == 7)
                	plugloadProfileHandler.setNightProfileWeekEnd(plugloadProfile);
                else if (i == 11)
                	plugloadProfileHandler.setNightProfileHoliday(plugloadProfile);
                break;
            }
            if (bSave) {
                savePlugloadProfile(plugloadProfile);
            }
        }
        
        sParamPrefix = strParamPrefix;
        PlugloadProfileConfiguration plugloadProfileConfiguration = new PlugloadProfileConfiguration();
        plugloadProfileConfiguration.setMorningTime(getConfigurationValue(scMap, sParamPrefix, "plpfc.morning_time"));
        plugloadProfileConfiguration.setDayTime(getConfigurationValue(scMap, sParamPrefix, "plpfc.day_time"));
        plugloadProfileConfiguration.setEveningTime(getConfigurationValue(scMap, sParamPrefix, "plpfc.evening_time"));
        plugloadProfileConfiguration.setNightTime(getConfigurationValue(scMap, sParamPrefix, "plpfc.night_time"));
        // Save profile Configuration
        if (bSave) {
            saveProfileConfiguration(plugloadProfileConfiguration);
        }
        saveDefaultWeekdayPlugloads(scMap, plugloadProfileConfiguration, bSave);
        // Set profile configuration to handler.
        plugloadProfileHandler.setPlugloadProfileConfiguration(plugloadProfileConfiguration);
        if(bSave) {
            savePlugloadProfileHandler(plugloadProfileHandler);
        }
        return plugloadProfileHandler;
    }

    public void saveDefaultWeekdayPlugloads(HashMap<String, String> scMap, PlugloadProfileConfiguration plugloadProfileConfiguration, boolean bSave) {
        String weekDayList = getConfigurationValue(scMap, "default.", "plugloadprofile.metadata.weekday");
        String weekEndList = getConfigurationValue(scMap, "default.", "plugloadprofile.metadata.weekend");
        int order = 0;
        Set<WeekdayPlugload> oWeekDays = new HashSet<WeekdayPlugload>();
        if (!ArgumentUtils.isNullOrEmpty(weekDayList)) {
            String[] weekdays = weekDayList.split(",");
            for (int i = 0; i < weekdays.length; i++) {
            	WeekdayPlugload weekDay = new WeekdayPlugload();
                weekDay.setDay(weekdays[i].trim());
                weekDay.setPlugloadProfileConfiguration(plugloadProfileConfiguration);
                weekDay.setShortOrder(++order);
                weekDay.setType("weekday");
                oWeekDays.add(weekDay);
                if(bSave) {
                    metaDataManager.saveOrUpdateWeekdayPlugload(weekDay);
                }
            }
        }
        if (!ArgumentUtils.isNullOrEmpty(weekEndList)) {
            String[] weekends = weekEndList.split(",");
            for (int i = 0; i < weekends.length; i++) {
            	WeekdayPlugload weekDay = new WeekdayPlugload();
                weekDay.setDay(weekends[i].trim());
                weekDay.setPlugloadProfileConfiguration(plugloadProfileConfiguration);
                weekDay.setShortOrder(++order);
                weekDay.setType("weekend");
                oWeekDays.add(weekDay);
                if(bSave) {
                    metaDataManager.saveOrUpdateWeekdayPlugload(weekDay);
                }
            }
        }
        plugloadProfileConfiguration.setWeekDays(oWeekDays);
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
                if (key.endsWith("plugloadprofile.morning.active_motion_window"))
                    sValue = "30";
                else if (key.endsWith("plugloadprofile.morning.mode"))
                    sValue = "2";
                else if (key.endsWith("plugloadprofile.morning.manual_override_time"))
                    sValue = "60";
                
                else if (key.endsWith("plugloadprofile.day.active_motion_window"))
                    sValue = "30";
                else if (key.endsWith("plugloadprofile.day.mode"))
                    sValue = "2";
                else if (key.endsWith("plugloadprofile.day.manual_override_time"))
                    sValue = "60";

                else if (key.endsWith("plugloadprofile.evening.active_motion_window"))
                    sValue = "30";
                else if (key.endsWith("plugloadprofile.evening.mode"))
                    sValue = "2";
                else if (key.endsWith("plugloadprofile.evening.manual_override_time"))
                    sValue = "60";

                else if (key.endsWith("plugloadprofile.night.active_motion_window"))
                    sValue = "30";
                else if (key.endsWith("plugloadprofile.night.mode"))
                    sValue = "2";
                else if (key.endsWith("plugloadprofile.night.manual_override_time"))
                    sValue = "60";

                // Advance Global variables...
                else if (key.equals("plpfh.initial_on_time"))
                    sValue = "0";
                else if (key.equals("plpfh.heartbeat_interval"))
                    sValue = "30";
                else if (key.equals("plpfh.heartbeat_linger_period"))
                    sValue = "30";
                else if (key.equals("plpfh.no_of_missed_heartbeats"))
                    sValue = "3";
                else if (key.equals("plpfh.fallback_mode"))
                    sValue = "1";
                else if (key.equals("plpfh.profile_group_id"))
                    sValue = "1";

                else if (key.equals("plpfc.morning_time"))
                    sValue = "6:00 AM";
                else if (key.equals("plpfc.day_time"))
                    sValue = "9:00 AM";
                else if (key.equals("plpfc.evening_time"))
                    sValue = "6:00 PM";
                else if (key.equals("plpfc.night_time"))
                    sValue = "9:00 PM";
                else
                    sValue = "0";
            }
        }
        return sValue;
    }
    
    public void savePlugloadProfile(PlugloadProfile plugloadProfile) {
        plugloadProfileDao.saveProfile(plugloadProfile);
    }
    
    public void saveDefaultPlugloadGroups(final PlugloadProfileHandler plugloadProfileHandler) {
        HashMap<String, String> scMap = systemConfigurationManager.loadAllConfigMap();
        String plugloadGroupList = getConfigurationValue(scMap, "default.", "plugloadprofile.metadata.areas");
        if (!ArgumentUtils.isNullOrEmpty(plugloadGroupList)) {
            String[] plugloadGroupsList = plugloadGroupList.split(",");
            PlugloadProfileHandler groupPlugloadProfileHandler = null;
            String plugloadGroupName = "";
            String plugloadGroupkey = "";
            // Group Id 1 is set to Default Plugload Profile.
            for (int i = 0, groupId = 1; i < plugloadGroupsList.length; i++, groupId++) {
                plugloadGroupName = plugloadGroupsList[i].trim();
                PlugloadGroups plugloadGroup = new PlugloadGroups();
                PlugloadProfileTemplate plugloadProfileTemplate=null;
                plugloadGroup.setName(plugloadGroupName);
                plugloadGroupkey = plugloadGroupName.replaceAll(" ", "").toLowerCase();
                if (plugloadGroupName.equals(ServerConstants.DEFAULT_PLUGLOAD_PROFILE)) {
                	plugloadGroup.setPlugloadProfileHandler(plugloadProfileHandler);
                } else {
                	plugloadGroupkey = "default." + plugloadGroupkey + ".";
                	groupPlugloadProfileHandler = createPlugloadProfile(plugloadGroupkey, groupId, true);
                	plugloadGroup.setPlugloadProfileHandler(groupPlugloadProfileHandler);
                }
                if(groupId<=plugloadGroupsList.length)
                {
                	plugloadGroup.setProfileNo((short) groupId);
                	Company company = companyManager.loadCompanyById(1L);
                	plugloadGroup.setCompany(company);
	                plugloadProfileTemplate = new PlugloadProfileTemplate();
	                plugloadProfileTemplate.setName(plugloadGroupName);
	                plugloadProfileTemplate.setDisplayTemplate(true);
	                plugloadProfileTemplate.setTemplateNo((long) groupId);
                }
                plugloadProfileTemplateManager.save(plugloadProfileTemplate);                
                plugloadGroup.setPlugloadProfileTemplate(plugloadProfileTemplate);
                plugloadGroup.setDisplayProfile(true);
                plugloadGroup.setDefaultProfile(true);
                metaDataManager.saveOrUpdatePlugloadGroup(plugloadGroup);
                
            }
        }
    }

}
