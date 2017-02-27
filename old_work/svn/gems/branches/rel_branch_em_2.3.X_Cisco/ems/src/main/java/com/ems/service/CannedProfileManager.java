package com.ems.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.model.Company;
import com.ems.model.CannedProfileConfiguration;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.SystemConfiguration;

@Service("cannedProfileManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CannedProfileManager {
	private static final Logger profileLogger = Logger
			.getLogger("ProfileLogger");

	/**
	 * Integer profile id , String variable , String value of the variable
	 */
	static Map<Integer, Map<String, Map<String, String>>> maps = new HashMap<Integer, Map<String, Map<String, String>>>(); // final
																															// map

	static Map<String, String> oValues = new HashMap<String, String>();
	static Map<String, Map<String, String>> profileNames = new HashMap<String, Map<String, String>>();

	private static CannedProfileConfigurationManager customProfileConfigurationManager;

	static {
		// Open Office Section Start
		/*
		 * oValues.put("default.openoffice.profile.morning.on_level", "70");
		 * oValues.put("default.openoffice.profile.day.on_level", "70");
		 * oValues.put("default.openoffice.profile.evening.on_level", "70");
		 * oValues.put("default.openoffice.profile.night.on_level", "70");
		 * oValues.put("default.openoffice.weekend.profile.morning.on_level",
		 * "70"); oValues.put("default.openoffice.weekend.profile.day.on_level",
		 * "70");
		 * oValues.put("default.openoffice.weekend.profile.evening.on_level",
		 * "70");
		 * oValues.put("default.openoffice.weekend.profile.night.on_level",
		 * "70");
		 * oValues.put("default.openoffice.holiday.profile.morning.on_level",
		 * "70"); oValues.put("default.openoffice.holiday.profile.day.on_level",
		 * "70");
		 * oValues.put("default.openoffice.holiday.profile.evening.on_level",
		 * "70");
		 * oValues.put("default.openoffice.holiday.profile.night.on_level",
		 * "70"); profileNames.put("Open Office Max 70", valueCopy(oValues));
		 * oValues.clear();
		 * 
		 * oValues.put("default.openoffice.profile.morning.on_level", "35");
		 * oValues.put("default.openoffice.profile.day.on_level", "35");
		 * oValues.put("default.openoffice.profile.evening.on_level", "35");
		 * oValues.put("default.openoffice.profile.night.on_level", "35");
		 * oValues.put("default.openoffice.weekend.profile.morning.on_level",
		 * "35"); oValues.put("default.openoffice.weekend.profile.day.on_level",
		 * "35");
		 * oValues.put("default.openoffice.weekend.profile.evening.on_level",
		 * "35");
		 * oValues.put("default.openoffice.weekend.profile.night.on_level",
		 * "35");
		 * oValues.put("default.openoffice.holiday.profile.morning.on_level",
		 * "35"); oValues.put("default.openoffice.holiday.profile.day.on_level",
		 * "35");
		 * oValues.put("default.openoffice.holiday.profile.evening.on_level",
		 * "35");
		 * oValues.put("default.openoffice.holiday.profile.night.on_level",
		 * "35"); profileNames.put("Open Office Max 35", valueCopy(oValues));
		 * oValues.clear();
		 * 
		 * oValues.put("default.openoffice.profile.morning.on_level", "50");
		 * oValues.put("default.openoffice.profile.day.on_level", "50");
		 * oValues.put("default.openoffice.profile.evening.on_level", "50");
		 * oValues.put("default.openoffice.profile.night.on_level", "50");
		 * oValues.put("default.openoffice.weekend.profile.morning.on_level",
		 * "50"); oValues.put("default.openoffice.weekend.profile.day.on_level",
		 * "50");
		 * oValues.put("default.openoffice.weekend.profile.evening.on_level",
		 * "50");
		 * oValues.put("default.openoffice.weekend.profile.night.on_level",
		 * "50");
		 * oValues.put("default.openoffice.holiday.profile.morning.on_level",
		 * "50"); oValues.put("default.openoffice.holiday.profile.day.on_level",
		 * "50");
		 * oValues.put("default.openoffice.holiday.profile.evening.on_level",
		 * "50");
		 * oValues.put("default.openoffice.holiday.profile.night.on_level",
		 * "50"); profileNames.put("Open Office Max 50", valueCopy(oValues));
		 * oValues.clear(); maps.put(9, copyProfileNames(profileNames));
		 * profileNames.clear();
		 */
		// Breakroom Section Start - Guidelines to add other profile changes as
		// well
		/*
		 * oValues.put("default.breakroom.profile.morning.on_level", "20");
		 * oValues.put("default.breakroom.profile.day.on_level", "20");
		 * oValues.put("default.breakroom.profile.evening.on_level", "20");
		 * oValues.put("default.breakroom.profile.night.on_level", "20");
		 * oValues.put("default.breakroom.weekend.profile.morning.on_level",
		 * "20"); oValues.put("default.breakroom.weekend.profile.day.on_level",
		 * "20");
		 * oValues.put("default.breakroom.weekend.profile.evening.on_level",
		 * "20");
		 * oValues.put("default.breakroom.weekend.profile.night.on_level",
		 * "20");
		 * oValues.put("default.breakroom.holiday.profile.morning.on_level",
		 * "20"); oValues.put("default.breakroom.holiday.profile.day.on_level",
		 * "20");
		 * oValues.put("default.breakroom.holiday.profile.evening.on_level",
		 * "20");
		 * oValues.put("default.breakroom.holiday.profile.night.on_level",
		 * "20"); profileNames.put("Breakroom Final Max 20",
		 * valueCopy(oValues)); oValues.clear(); maps.put(2,
		 * copyProfileNames(profileNames)); profileNames.clear();
		 * 
		 * // Now create the Conference room changed values //
		 * default.conferenceroom.pfh.dr_reactivity
		 * oValues.put("default.conferenceroom.pfh.dr_reactivity", "1");
		 * profileNames.put("Conference Room DR", valueCopy(oValues));
		 * oValues.clear(); maps.put(3, copyProfileNames(profileNames));
		 * profileNames.clear();
		 */}

	static Map<String, Map<String, String>> copyProfileNames(
			Map<String, Map<String, String>> toCopy) {
		Map<String, Map<String, String>> copiedProfileNames = new HashMap<String, Map<String, String>>();
		Iterator<String> oKeys = toCopy.keySet().iterator();
		while (oKeys.hasNext()) {
			String key = (String) oKeys.next();
			Iterator<String> innerKeys = toCopy.get(key).keySet().iterator();
			Map<String, String> innerMap = new HashMap<String, String>();
			while (innerKeys.hasNext()) {
				String innerKey = innerKeys.next();
				String innervalue = toCopy.get(key).get(innerKey);
				innerMap.put(innerKey, innervalue);
			}
			copiedProfileNames.put(key, innerMap);
		}
		return copiedProfileNames;
	}

	static Map<String, String> valueCopy(Map<String, String> mapToCopy) {
		Map<String, String> copiedMap = new HashMap<String, String>();
		Iterator<String> oKeys = mapToCopy.keySet().iterator();
		while (oKeys.hasNext()) {
			String key = (String) oKeys.next();
			String value = mapToCopy.get(key);
			copiedMap.put(key, value);
		}
		return copiedMap;
	}

	static void setProfileMaps() {
		try {
			customProfileConfigurationManager = (CannedProfileConfigurationManager) SpringContext
					.getBean("cannedProfileConfigurationManager");
			for (int i = 1; i <= 16; i++) {
				// Get the list of profiles where the flag is false for the
				// particular parent profile id
				List<CannedProfileConfiguration> mConfig = (List<CannedProfileConfiguration>) customProfileConfigurationManager
						.loadConfigByProfileId(i);
				if (mConfig != null)
					for (Iterator iterator = mConfig.iterator(); iterator
							.hasNext();) {
						CannedProfileConfiguration customProfileConfiguration = (CannedProfileConfiguration) iterator
								.next();
						if (!customProfileConfiguration.getStatus()
								&& customProfileConfiguration
										.getParentProfileid() != null) {
							// The profile has not been created
							if (customProfileConfiguration
									.getParentProfileid() == 2
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Breakroom_Normal")) {
								
								oValues.put(
										"default.breakroom.profile.day.min_level",
										"0");
								oValues.put(
										"default.breakroom.profile.morning.on_level",
										"60");
								oValues.put(
										"default.breakroom.profile.day.on_level",
										"60");
								oValues.put(
										"default.breakroom.profile.evening.on_level",
										"60");
								oValues.put(
										"default.breakroom.profile.night.on_level",
										"60");
								oValues.put(
										"default.breakroom.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.breakroom.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.breakroom.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.breakroom.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.breakroom.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.breakroom.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.breakroom.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.breakroom.holiday.profile.night.on_level",
										"40");
								oValues.put(
										"default.breakroom.pfh.to_off_linger",
										"900");			
								
								//default.breakroom.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.breakroom.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.profile.day.motion_detect_duration",
										"3");
								oValues.put(
										"default.breakroom.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.weekend.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.weekend.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.weekend.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.holiday.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.holiday.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.holiday.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.breakroom.holiday.profile.night.motion_detect_duration",
										"1");
								
								
								oValues.put(
										"default.breakroom.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.breakroom.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.breakroom.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.breakroom.holiday.profile.night.ambient_sensitivity",
										"10");
								
								//Custom values
								oValues.put("default.breakroom.pfh.envelope_on_level",
								"20");
								oValues.put("default.breakroom.pfh.dark_lux",
								"5");
								oValues.put("default.breakroom.pfh.neighbor_lux",
								"250");						
								profileNames.put("Breakroom_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 9
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Open Office_Dim")) {
								
								//ramp up time
								oValues.put(
										"default.openoffice.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.night.ramp_up_time",
										"2");
								//end ramp up time

								
								
								oValues.put(
										"default.openoffice.profile.morning.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.day.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.evening.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.night.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.morning.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.day.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.evening.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.night.min_level",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.morning.min_level",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.day.min_level",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.evening.min_level",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.night.min_level",
										"10");
								////
								oValues.put(
										"default.openoffice.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.openoffice.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.openoffice.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.openoffice.holiday.profile.night.on_level",
										"40");															
								//default.openoffice.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.openoffice.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.weekend.profile.morning.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.holiday.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.holiday.profile.day.motion_detect_duration",
										"5");
								oValues.put(
										"default.openoffice.holiday.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.holiday.profile.night.motion_detect_duration",
										"5");															
								oValues.put(
										"default.openoffice.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.openoffice.pfh.envelope_on_level",
												"20");
								oValues.put("default.openoffice.pfh.dark_lux",
								"5");
								oValues.put("default.openoffice.pfh.neighbor_lux",
								"250");
								profileNames.put("Open Office_Dim",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 5
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Closed Corridor_Normal")) {								
								oValues.put(
										"default.closedcorridor.profile.morning.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.day.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.evening.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.night.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.min_level",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.min_level",
										"0");
								////
								oValues.put(
										"default.closedcorridor.profile.morning.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.day.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.evening.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.night.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.on_level",
										"40");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.on_level",
										"30");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.on_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.on_level",
										"30");															
								//default.closedcorridor.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.closedcorridor.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.motion_detect_duration",
										"1");
								
								oValues.put(
										"default.closedcorridor.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.closedcorridor.pfh.envelope_on_level",
												"20");
								oValues.put("default.closedcorridor.pfh.dark_lux",
								"5");
								oValues.put("default.closedcorridor.pfh.neighbor_lux",
								"250");
								oValues.put(
										"default.closedcorridor.pfh.to_off_linger",
										"30");
								profileNames.put("Closed Corridor_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 5
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Closed Corridor_AlwaysOn")) {								
								oValues.put(
										"default.closedcorridor.profile.morning.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.day.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.evening.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.profile.night.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.min_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.min_level",
										"20");
								////
								oValues.put(
										"default.closedcorridor.profile.morning.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.day.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.evening.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.profile.night.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.on_level",
										"50");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.on_level",
										"40");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.on_level",
										"30");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.on_level",
										"20");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.on_level",
										"30");															
								//default.closedcorridor.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.closedcorridor.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.motion_detect_duration",
										"1");
								
								oValues.put(
										"default.closedcorridor.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.closedcorridor.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.closedcorridor.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.closedcorridor.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.closedcorridor.pfh.envelope_on_level",
												"20");
								oValues.put("default.closedcorridor.pfh.dark_lux",
								"5");
								oValues.put("default.closedcorridor.pfh.neighbor_lux",
								"250");								
								profileNames.put("Closed Corridor_AlwaysOn",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 3
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Conference Room_Normal")) {								
								oValues.put(
										"default.conferenceroom.profile.morning.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.profile.day.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.profile.evening.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.profile.night.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.weekend.profile.morning.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.weekend.profile.day.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.weekend.profile.evening.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.weekend.profile.night.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.holiday.profile.morning.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.holiday.profile.day.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.holiday.profile.evening.min_level",
										"0");
								oValues.put(
										"default.conferenceroom.holiday.profile.night.min_level",
										"0");
								////
								oValues.put(
										"default.conferenceroom.profile.morning.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.profile.day.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.profile.evening.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.profile.night.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.conferenceroom.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.conferenceroom.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.conferenceroom.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.conferenceroom.holiday.profile.night.on_level",
										"40");															
								//default.conferenceroom.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.conferenceroom.profile.morning.motion_detect_duration",
										"10");
								oValues.put(
										"default.conferenceroom.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.conferenceroom.profile.evening.motion_detect_duration",
										"10");
								oValues.put(
										"default.conferenceroom.profile.night.motion_detect_duration",
										"3");
								oValues.put(
										"default.conferenceroom.weekend.profile.morning.motion_detect_duration",
										"3");
								oValues.put(
										"default.conferenceroom.weekend.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.conferenceroom.weekend.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.conferenceroom.weekend.profile.night.motion_detect_duration",
										"3");
								oValues.put(
										"default.conferenceroom.holiday.profile.morning.motion_detect_duration",
										"5");
								oValues.put(
										"default.conferenceroom.holiday.profile.day.motion_detect_duration",
										"5");
								oValues.put(
										"default.conferenceroom.holiday.profile.evening.motion_detect_duration",
										"5");
								oValues.put(
										"default.conferenceroom.holiday.profile.night.motion_detect_duration",
										"3");							

								oValues.put(
										"default.conferenceroom.profile.morning.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.profile.day.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.profile.evening.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.conferenceroom.weekend.profile.morning.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.weekend.profile.day.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.weekend.profile.evening.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.conferenceroom.holiday.profile.morning.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.holiday.profile.day.ambient_sensitivity",
										"8");
								oValues.put(
										"default.conferenceroom.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.conferenceroom.holiday.profile.night.ambient_sensitivity",
										"8");
								//Custom values
								
								//oValues.put("default.conferenceroom.pfh.envelope_on_level",
								//				"20");
								oValues.put("default.conferenceroom.pfh.dark_lux",
								"5");
								oValues.put("default.conferenceroom.pfh.neighbor_lux",
								"250");
								/*oValues.put(
										"default.conferenceroom.pfh.to_off_linger",
										"30");*/
								profileNames.put("Conference Room_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 9
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Open Office_Normal")) {								
								oValues.put(
										"default.openoffice.profile.morning.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.day.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.evening.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.night.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.morning.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.day.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.evening.min_level",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.night.min_level",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.morning.min_level",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.day.min_level",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.evening.min_level",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.night.min_level",
										"10");
								////
								oValues.put(
										"default.openoffice.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.openoffice.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.openoffice.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.openoffice.holiday.profile.night.on_level",
										"40");															
								//default.openoffice.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.openoffice.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.weekend.profile.morning.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.holiday.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.holiday.profile.day.motion_detect_duration",
										"5");
								oValues.put(
										"default.openoffice.holiday.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.holiday.profile.night.motion_detect_duration",
										"5");
								
								//ramp up time
								oValues.put(
										"default.openoffice.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.night.ramp_up_time",
										"2");
								//end ramp up time

								oValues.put(
										"default.openoffice.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.openoffice.pfh.envelope_on_level",
												"20");
								oValues.put("default.openoffice.pfh.dark_lux",
								"5");
								oValues.put("default.openoffice.pfh.neighbor_lux",
								"250");
								/*oValues.put(
										"default.openoffice.pfh.to_off_linger",
										"30");*/
								profileNames.put("Open Office_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 9
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Open Office_AlwaysOn")) {								
								oValues.put(
										"default.openoffice.profile.morning.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.day.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.evening.min_level",
										"20");
								oValues.put(
										"default.openoffice.profile.night.min_level",
										"20");
								oValues.put(
										"default.openoffice.weekend.profile.morning.min_level",
										"20");
								oValues.put(
										"default.openoffice.weekend.profile.day.min_level",
										"20");
								oValues.put(
										"default.openoffice.weekend.profile.evening.min_level",
										"20");
								oValues.put(
										"default.openoffice.weekend.profile.night.min_level",
										"20");
								oValues.put(
										"default.openoffice.holiday.profile.morning.min_level",
										"20");
								oValues.put(
										"default.openoffice.holiday.profile.day.min_level",
										"20");
								oValues.put(
										"default.openoffice.holiday.profile.evening.min_level",
										"20");
								oValues.put(
										"default.openoffice.holiday.profile.night.min_level",
										"20");
								////
								oValues.put(
										"default.openoffice.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.openoffice.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.openoffice.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.openoffice.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.openoffice.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.openoffice.holiday.profile.night.on_level",
										"40");															
								//default.openoffice.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.openoffice.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.weekend.profile.morning.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.openoffice.holiday.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.openoffice.holiday.profile.day.motion_detect_duration",
										"5");
								oValues.put(
										"default.openoffice.holiday.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.openoffice.holiday.profile.night.motion_detect_duration",
										"5");
								
								//ramp up time
								oValues.put(
										"default.openoffice.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.weekend.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.openoffice.holiday.profile.night.ramp_up_time",
										"2");
								//end ramp up time

								oValues.put(
										"default.openoffice.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.openoffice.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.openoffice.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.openoffice.pfh.envelope_on_level",
												"20");
								oValues.put("default.openoffice.pfh.dark_lux",
								"5");
								oValues.put("default.openoffice.pfh.neighbor_lux",
								"250");
								/*oValues.put(
										"default.openoffice.pfh.to_off_linger",
										"30");*/
								profileNames.put("Open Office_AlwaysOn",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 10
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Private Office_Normal")) {								
								oValues.put(
										"default.privateoffice.profile.morning.min_level",
										"0");
								oValues.put(
										"default.privateoffice.profile.day.min_level",
										"0");
								oValues.put(
										"default.privateoffice.profile.evening.min_level",
										"0");
								oValues.put(
										"default.privateoffice.profile.night.min_level",
										"0");
								oValues.put(
										"default.privateoffice.weekend.profile.morning.min_level",
										"0");
								oValues.put(
										"default.privateoffice.weekend.profile.day.min_level",
										"0");
								oValues.put(
										"default.privateoffice.weekend.profile.evening.min_level",
										"0");
								oValues.put(
										"default.privateoffice.weekend.profile.night.min_level",
										"0");
								oValues.put(
										"default.privateoffice.holiday.profile.morning.min_level",
										"0");
								oValues.put(
										"default.privateoffice.holiday.profile.day.min_level",
										"0");
								oValues.put(
										"default.privateoffice.holiday.profile.evening.min_level",
										"0");
								oValues.put(
										"default.privateoffice.holiday.profile.night.min_level",
										"0");
								////
								oValues.put(
										"default.privateoffice.profile.morning.on_level",
										"60");
								oValues.put(
										"default.privateoffice.profile.day.on_level",
										"60");
								oValues.put(
										"default.privateoffice.profile.evening.on_level",
										"60");
								oValues.put(
										"default.privateoffice.profile.night.on_level",
										"60");
								oValues.put(
										"default.privateoffice.weekend.profile.morning.on_level",
										"60");
								oValues.put(
										"default.privateoffice.weekend.profile.day.on_level",
										"60");
								oValues.put(
										"default.privateoffice.weekend.profile.evening.on_level",
										"60");
								oValues.put(
										"default.privateoffice.weekend.profile.night.on_level",
										"60");
								oValues.put(
										"default.privateoffice.holiday.profile.morning.on_level",
										"50");
								oValues.put(
										"default.privateoffice.holiday.profile.day.on_level",
										"40");
								oValues.put(
										"default.privateoffice.holiday.profile.evening.on_level",
										"30");
								oValues.put(
										"default.privateoffice.holiday.profile.night.on_level",
										"40");															
								//default.privateoffice.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.privateoffice.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.privateoffice.profile.day.motion_detect_duration",
										"15");
								oValues.put(
										"default.privateoffice.profile.evening.motion_detect_duration",
										"7");
								oValues.put(
										"default.privateoffice.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.privateoffice.weekend.profile.morning.motion_detect_duration",
										"3");
								oValues.put(
										"default.privateoffice.weekend.profile.day.motion_detect_duration",
										"10");
								oValues.put(
										"default.privateoffice.weekend.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.privateoffice.weekend.profile.night.motion_detect_duration",
										"3");
								oValues.put(
										"default.privateoffice.holiday.profile.morning.motion_detect_duration",
										"7");
								oValues.put(
										"default.privateoffice.holiday.profile.day.motion_detect_duration",
										"5");
								oValues.put(
										"default.privateoffice.holiday.profile.evening.motion_detect_duration",
										"3");
								oValues.put(
										"default.privateoffice.holiday.profile.night.motion_detect_duration",
										"3");
								
								//ramp up time
								oValues.put(
										"default.privateoffice.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.weekend.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.weekend.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.weekend.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.weekend.profile.night.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.holiday.profile.morning.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.holiday.profile.day.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.holiday.profile.evening.ramp_up_time",
										"2");
								oValues.put(
										"default.privateoffice.holiday.profile.night.ramp_up_time",
										"2");
								//end ramp up time

								oValues.put(
										"default.privateoffice.profile.morning.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.profile.day.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.profile.evening.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.privateoffice.weekend.profile.morning.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.weekend.profile.day.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.weekend.profile.evening.ambient_sensitivity",
										"8");
								oValues.put(
										"default.privateoffice.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.privateoffice.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.privateoffice.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.privateoffice.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.privateoffice.holiday.profile.night.ambient_sensitivity",
										"0");
								//Custom values
								
								oValues.put("default.privateoffice.pfh.envelope_on_level",
												"20");
								oValues.put("default.privateoffice.pfh.dark_lux",
								"5");
								oValues.put("default.privateoffice.pfh.neighbor_lux",
								"250");
								/*oValues.put(
										"default.privateoffice.pfh.to_off_linger",
										"30");*/
								profileNames.put("Private Office_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 16
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Highbay_Normal")) {							
								////
								oValues.put(
										"default.highbay.profile.morning.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.day.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.evening.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.night.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.morning.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.day.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.evening.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.night.on_level",
										"70");
								oValues.put(
										"default.highbay.holiday.profile.morning.on_level",
										"60");
								oValues.put(
										"default.highbay.holiday.profile.day.on_level",
										"50");
								oValues.put(
										"default.highbay.holiday.profile.evening.on_level",
										"40");
								oValues.put(
										"default.highbay.holiday.profile.night.on_level",
										"50");															
								//default.highbay.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.highbay.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.profile.day.motion_detect_duration",
										"2");
								oValues.put(
										"default.highbay.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.night.motion_detect_duration",
										"1");
								
								//motion sensitivity

								oValues.put(
										"default.highbay.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.night.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.night.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.night.motion_sensitivity",
										"1");
								//end ms							

								oValues.put(
										"default.highbay.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.highbay.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.highbay.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.night.ambient_sensitivity",
										"10");
								//Custom values
								/*oValues.put(
										"default.highbay.pfh.dr_low",
										"1");
								oValues.put(
										"default.highbay.pfh.dr_moderate",
										"2");
								oValues.put(
										"default.highbay.pfh.dr_high",
										"3");
								oValues.put(
										"default.highbay.pfh.dr_special",
										"4");*/
								oValues.put("default.highbay.pfh.envelope_on_level",
												"20");
								oValues.put("default.highbay.pfh.dark_lux",
								"5");
								oValues.put("default.highbay.pfh.neighbor_lux",
								"250");
								oValues.put(
										"default.highbay.pfh.to_off_linger",
										"300");
								profileNames.put("Highbay_Normal",
										valueCopy(oValues));
								oValues.clear();
							}else if (customProfileConfiguration
									.getParentProfileid() == 16
									&& customProfileConfiguration.getName()
											.equalsIgnoreCase(
													"Highbay_AlwaysOn")) {
								
								oValues.put(
										"default.highbay.profile.morning.min_level",
										"20");
								oValues.put(
										"default.highbay.profile.day.min_level",
										"20");
								oValues.put(
										"default.highbay.profile.evening.min_level",
										"20");
								oValues.put(
										"default.highbay.profile.night.min_level",
										"20");
								oValues.put(
										"default.highbay.weekend.profile.morning.min_level",
										"20");
								oValues.put(
										"default.highbay.weekend.profile.day.min_level",
										"20");
								oValues.put(
										"default.highbay.weekend.profile.evening.min_level",
										"20");
								oValues.put(
										"default.highbay.weekend.profile.night.min_level",
										"20");
								oValues.put(
										"default.highbay.holiday.profile.morning.min_level",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.day.min_level",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.evening.min_level",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.night.min_level",
										"10");
								////
								oValues.put(
										"default.highbay.profile.morning.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.day.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.evening.on_level",
										"70");
								oValues.put(
										"default.highbay.profile.night.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.morning.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.day.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.evening.on_level",
										"70");
								oValues.put(
										"default.highbay.weekend.profile.night.on_level",
										"70");
								oValues.put(
										"default.highbay.holiday.profile.morning.on_level",
										"60");
								oValues.put(
										"default.highbay.holiday.profile.day.on_level",
										"50");
								oValues.put(
										"default.highbay.holiday.profile.evening.on_level",
										"40");
								oValues.put(
										"default.highbay.holiday.profile.night.on_level",
										"50");															
								//default.highbay.holiday.profile.night.motion_detect_duration
								oValues.put(
										"default.highbay.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.profile.day.motion_detect_duration",
										"2");
								oValues.put(
										"default.highbay.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.night.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.morning.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.day.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.evening.motion_detect_duration",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.night.motion_detect_duration",
										"1");
								
								//motion sensitivity

								oValues.put(
										"default.highbay.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.profile.night.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.weekend.profile.night.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.morning.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.day.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.evening.motion_sensitivity",
										"1");
								oValues.put(
										"default.highbay.holiday.profile.night.motion_sensitivity",
										"1");
								//end ms							

								oValues.put(
										"default.highbay.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.highbay.weekend.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.weekend.profile.night.ambient_sensitivity",
										"0");
								oValues.put(
										"default.highbay.holiday.profile.morning.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.day.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.evening.ambient_sensitivity",
										"10");
								oValues.put(
										"default.highbay.holiday.profile.night.ambient_sensitivity",
										"10");
								//Custom values
								/*oValues.put(
										"default.highbay.pfh.dr_low",
										"1");
								oValues.put(
										"default.highbay.pfh.dr_moderate",
										"2");
								oValues.put(
										"default.highbay.pfh.dr_high",
										"3");
								oValues.put(
										"default.highbay.pfh.dr_special",
										"4");*/
								oValues.put("default.highbay.pfh.envelope_on_level",
												"20");
								oValues.put("default.highbay.pfh.dark_lux",
								"5");
								oValues.put("default.highbay.pfh.neighbor_lux",
								"250");
								oValues.put(
										"default.highbay.pfh.to_off_linger",
										"300");
								profileNames.put("Highbay_AlwaysOn",
										valueCopy(oValues));
								oValues.clear();
							}				
						}
					}
				maps.put(i, copyProfileNames(profileNames));
				profileNames.clear();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			profileLogger.error(e.getMessage());
		}
	}

	/**
	 * Neet to call once to set the canned profiles
	 * 
	 * @param scMap
	 */
	public static void setCannedProfiles(HashMap<String, String> scMap) {
		// Define the managers
		System.out.println("In the method");
		setProfileMaps();
		try {
			ProfileManager profileManager = (ProfileManager) SpringContext
					.getBean("profileManager");
			CompanyManager companyManager = (CompanyManager) SpringContext
					.getBean("companyManager");
			ProfileTemplateManager profileTemplateManager = (ProfileTemplateManager) SpringContext
					.getBean("profileTemplateManager");
			GroupManager groupManager = (GroupManager) SpringContext
					.getBean("groupManager");
			MetaDataManager metaDataManager = (MetaDataManager) SpringContext
					.getBean("metaDataManager");
			SystemConfigurationManager systemConfigurationManager = (SystemConfigurationManager) SpringContext
					.getBean("systemConfigurationManager");
			// Keep the backup of the default map values
			HashMap<String, String> copiedMap = (HashMap<String, String>) valueCopy(scMap);
			String groupList = profileManager.getConfigurationValue(scMap,
					"default.", "metadata.areas");
			String[] groups = groupList.split(",");

			Iterator<Integer> oKeys = maps.keySet().iterator();
			while (oKeys.hasNext()) {
				Integer profileId = (Integer) oKeys.next(); // Profile Id is
															// nothing but the
															// group id
				Iterator<String> profileNamesKeys = maps.get(profileId)
						.keySet().iterator();
				while (profileNamesKeys.hasNext()) {
					String profileName = (String) profileNamesKeys.next();
					Iterator<String> profileNameValues = maps.get(profileId)
							.get(profileName).keySet().iterator();
					scMap = (HashMap<String, String>) valueCopy(copiedMap);
					while (profileNameValues.hasNext()) {
						String key = profileNameValues.next();
						String value = maps.get(profileId).get(profileName)
								.get(key);
						if (scMap.containsKey(key)) {
							scMap.put(key, value);
						}
					}
					// Create the profile with new map values now.
					// Derive the correct group name
					if (groupManager.getGroupByName(profileName) == null) {
						String groupKey = groups[profileId - 1].trim();
						groupKey = groupKey.replaceAll(" ", "").toLowerCase();
						// End derive name
						Short profileNumber = groupManager.getMaxProfileNo(null);
						ProfileHandler profileHandler = profileManager
								.createCannedProfile("default." + groupKey
										+ ".", profileNumber, scMap, true);
						Groups derivedGrp = groupManager.getGroupById(Long
								.valueOf(profileId.toString()));
						Company company = companyManager.getCompany();
						Groups group = new Groups();
						group.setProfileNo(profileNumber);
						group.setName(profileName);
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
						metaDataManager.flush(); // Write to db as soon
						CannedProfileConfiguration config = customProfileConfigurationManager
								.loadConfigByName(profileName);
						config.setStatus(new Boolean(true)); // Mark the profile
																// as created
						customProfileConfigurationManager.update(config);
					} else {
						profileLogger
								.error("Profile with the same name already exist : "
										+ profileName);
					}
				}
			}
			SystemConfiguration cannedProfileUpgradeEnableConfig = systemConfigurationManager
					.loadConfigByName("cannedprofile.enable");
			cannedProfileUpgradeEnableConfig.setValue("1");
			systemConfigurationManager.save(cannedProfileUpgradeEnableConfig);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			profileLogger.error(e.getStackTrace());
		} catch (Exception e) {
			// TODO: handle exception
			profileLogger.error(e.getStackTrace());
		}
	}
}
