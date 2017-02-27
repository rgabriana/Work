package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emscloud.model.Customer;
import com.emscloud.model.EmProfileMapping;
import com.emscloud.model.Facility;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.ProfileHandler;
import com.emscloud.model.ProfileTemplate;
import com.emscloud.model.WeekDay;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmProfileMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.MetaDataManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.service.ProfileManager;
import com.emscloud.service.ProfileSyncManager;
import com.emscloud.service.ProfileTemplateManager;
import com.emscloud.service.UserManager;
import com.emscloud.types.FacilityType;
import com.emscloud.types.ProfileOverrideType;
import com.emscloud.types.RoleType;
import com.emscloud.util.FacilityCookieHandler;
import com.emscloud.util.JsTreeOptions;
import com.emscloud.util.tree.TreeNode;
@Controller
@RequestMapping("/profile")
public class ProfileController {

	@Resource(name = "profileGroupManager")
	private ProfileGroupManager groupManager;
	@Resource(name = "profileManager")
	private ProfileManager profileManager;
	/*
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	*/
	@Resource(name="customerManager")
    private CustomerManager customerManager;
	@Resource(name="metaDataManager")
	private MetaDataManager metaDataManager;
	@Resource(name="profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;
	@Resource
	private UserManager userManager;
	@Resource(name="emProfileMappingManager")
	EmProfileMappingManager emProfileMappingManager;
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext enlightedAuthContext;
	@Resource(name="profileSyncManager")
	ProfileSyncManager profileSyncManager;
	@Autowired
	private MessageSource messageSource;
	@Resource
	FacilityManager facilityManager;
	@RequestMapping(value = "/setting.ems", method = { RequestMethod.POST,
			RequestMethod.GET })
	// This is the cookie, we have put selected profile-node in.
	public String profileSetting(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedProfileCookie) String cookie) {

		// Note : Add in the order of enums defined
		Long min = (long) 0 , max = (long) 0;		
		ArrayList<String> list = new ArrayList<String>();
		list.add(ProfileOverrideType.No_Override.getName());
		list.add(ProfileOverrideType.Override1.getName());
		list.add(ProfileOverrideType.Override2.getName());
		list.add(ProfileOverrideType.Override3.getName());
		list.add(ProfileOverrideType.Override4.getName());
		model.addAttribute("overridelist", list);
		
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(ProfileOverrideType.No_Override.getName());
		list2.add(ProfileOverrideType.Override5.getName());
		list2.add(ProfileOverrideType.Override6.getName());
		list2.add(ProfileOverrideType.Override7.getName());
		list2.add(ProfileOverrideType.Override8.getName());
		model.addAttribute("override2list", list2);
		
		if (cookie == null || "".equals(cookie)) {
			return null; // TODO show internal error page.
		}

		int seperator = cookie
				.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
		if (seperator < 0) {
			return null; // TODO show internal error page.
		}
		String[] cookieArr = cookie.split(FacilityCookieHandler.facilityCookieNameSeparator);
		String type = cookie.substring(1, seperator); // group or fixture

		Long id = new Long(cookieArr[1]);
		Long parentId = new Long(cookieArr[2]);
		
		if ("profilegroup".equals(type)) {
			ProfileGroups group = groupManager.getGroupByName(groupManager
					.getGroupById(id).getName());
			ProfileHandler ph = profileManager.getProfileHandlerById(group
					.getProfileHandler().getId());
			model.addAttribute("profilehandler", ph);
			model.addAttribute("type", "group");
			model.addAttribute("typeid", group.getId());
			model.addAttribute("group", group);
			if(group.isDefaultProfile()==true)
			{
				model.addAttribute("management","readonly");
			}
			else
			{
				model.addAttribute("management","edit");
			}
			model.addAttribute("templateId", group.getProfileTemplate().getId());
			//model.addAttribute("groups", groupManager.loadAllProfileTemplateById(group.getProfileTemplate().getId(),0L));
			ProfileGroups derivedGrp = group.getDerivedFromGroup();
			// group is derived from other group then return derived group other wise  return default group
			if(derivedGrp!=null && derivedGrp.getId()!=null)
			{
				model.addAttribute("groups",derivedGrp);	
			}else if(group.isDefaultProfile()==true && derivedGrp==null)
			{
				//Load Default profile 
				derivedGrp = groupManager.getGroupById((long) 1);
				model.addAttribute("groups",derivedGrp);
			}else
			{
			    ProfileGroups dummyGrp = new ProfileGroups();
				dummyGrp.setName("None");
				model.addAttribute("groups",dummyGrp);
			}
			if(ph.getMorningProfile().getMinLevel() > min)
			{
				min = ph.getMorningProfile().getMinLevel();
			}
			else if(ph.getDayProfile().getMinLevel() > min)
			{
				min = ph.getDayProfile().getMinLevel();
			}
			else if(ph.getEveningProfile().getMinLevel() > min)
			{
				min = ph.getEveningProfile().getMinLevel();
			}
			else if(ph.getNightProfile().getMinLevel() > min)
			{
				min = ph.getNightProfile().getMinLevel();
			}		
			
			//Calculate the max 
			if(ph.getMorningProfile().getOnLevel() > max)
			{
				max = ph.getMorningProfile().getOnLevel();
			}
			else if(ph.getDayProfile().getOnLevel() > max)
			{
				max = ph.getDayProfile().getOnLevel();
			}
			else if(ph.getEveningProfile().getOnLevel() > max)
			{
				max = ph.getEveningProfile().getOnLevel();
			}
			else if(ph.getNightProfile().getOnLevel() > max)
			{
				max = ph.getNightProfile().getOnLevel();
			}
			model.addAttribute("minDR", min);
			model.addAttribute("maxDR", max);
		}else {
			return null; // TODO show internal error page.
		}
		return "profile/setting";
	}

	@RequestMapping(value = "/fixturesetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profileFixtureSetting(Model model,@RequestParam(value="fixtureId",required=false) Long id,@RequestParam(value = "groupId", required = false) Long groupId) {
	    ProfileGroups oGroup =null;
	    //TODO:
		//Fixture fixture = null;
		if(groupId!=null)
		{
			//Default Profile - Read Only Mode - Calls from 
			oGroup = groupManager.getGroupById(groupId);
			model.addAttribute("type", "group");
			model.addAttribute("typeid", oGroup.getId());
		}
		
		ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
		model.addAttribute("profilehandler", ph);
		model.addAttribute("group", oGroup);
		model.addAttribute("management","readonly");
		ProfileGroups derivedGrp = oGroup.getDerivedFromGroup();
		// group is derived from other group then return derived group other wise  return default group
		if(derivedGrp!=null && derivedGrp.getId()!=null)
		{
			model.addAttribute("groups",derivedGrp);	
		}else if(oGroup.isDefaultProfile()==true && derivedGrp==null)
		{
			//Load Default profile 
			derivedGrp = groupManager.getGroupById((long) 1);
			model.addAttribute("groups",derivedGrp);
		}
		else
		{
		    ProfileGroups dummyGrp = new ProfileGroups();
			dummyGrp.setName("None");
			model.addAttribute("groups",dummyGrp);
		}
		// Note : Add in the order of enums defined
		Long min = (long) 0 , max = (long) 0;
		if(ph.getMorningProfile().getMinLevel() > min)
		{
			min = ph.getMorningProfile().getMinLevel();
		}
		else if(ph.getDayProfile().getMinLevel() > min)
		{
			min = ph.getDayProfile().getMinLevel();
		}
		else if(ph.getEveningProfile().getMinLevel() > min)
		{
			min = ph.getEveningProfile().getMinLevel();
		}
		else if(ph.getNightProfile().getMinLevel() > min)
		{
			min = ph.getNightProfile().getMinLevel();
		}		
		
		//Calculate the max 
		if(ph.getMorningProfile().getOnLevel() > max)
		{
			max = ph.getMorningProfile().getOnLevel();
		}
		else if(ph.getDayProfile().getOnLevel() > max)
		{
			max = ph.getDayProfile().getOnLevel();
		}
		else if(ph.getEveningProfile().getOnLevel() > max)
		{
			max = ph.getEveningProfile().getOnLevel();
		}
		else if(ph.getNightProfile().getOnLevel() > max)
		{
			max = ph.getNightProfile().getOnLevel();
		}
		ArrayList<String> list = new ArrayList<String>();
		list.add(ProfileOverrideType.No_Override.getName());
		list.add(ProfileOverrideType.Override1.getName());
		list.add(ProfileOverrideType.Override2.getName());
		list.add(ProfileOverrideType.Override3.getName());
		list.add(ProfileOverrideType.Override4.getName());
		model.addAttribute("overridelist", list);
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(ProfileOverrideType.No_Override.getName());
		list2.add(ProfileOverrideType.Override5.getName());
		list2.add(ProfileOverrideType.Override6.getName());
		list2.add(ProfileOverrideType.Override7.getName());
		list2.add(ProfileOverrideType.Override8.getName());
		model.addAttribute("override2list", list2);
		model.addAttribute("minDR", min);
		model.addAttribute("maxDR", max);
		return "profile/fixture/setting";
	}
	
	/**
	 * All in one profile update
	 * @param type
	 * @param typeid
	 * @param weekdays
	 * @param ph
	 * @param local
	 * @return json message to determine success or failure
	 */
	@RequestMapping(value = "/updateProfileData.ems", method = RequestMethod.POST)
    @ResponseBody
    public String updateProfileData(@RequestParam("type") String type, @RequestParam("typeid") Long typeid, @RequestParam("profilename") String profilename,
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") ProfileHandler ph, Locale local) {
        if (weekdays == null || "".equals(weekdays)) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Week information not provided" + "\"}";
        }

        String[] days = weekdays.split(",");
		if (type == null || "".equals(type)) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Group type not provided" + "\"}";
		}
		ProfileGroups groupObj = groupManager.getGroupById(typeid);
		groupObj.setName(profilename);
		groupManager.editName(groupObj);

		StringBuilder profileLog = new StringBuilder(":::Override Profiles::") ;
        ProfileHandler ph1 = profileManager.getProfileHandlerById(groupObj.getProfileHandler().getId());
        Map<Byte,String> mMap = new HashMap<Byte, String>();
        mMap.put((byte) 0, ProfileOverrideType.No_Override.getName());
        mMap.put((byte) 1, ProfileOverrideType.Override1.getName());
        mMap.put((byte) 2, ProfileOverrideType.Override2.getName());
        mMap.put((byte) 3, ProfileOverrideType.Override3.getName());
        mMap.put((byte) 4, ProfileOverrideType.Override4.getName());
        mMap.put((byte) 5, ProfileOverrideType.Override5.getName());
        mMap.put((byte) 6, ProfileOverrideType.Override6.getName());
        mMap.put((byte) 7, ProfileOverrideType.Override7.getName());
        mMap.put((byte) 8, ProfileOverrideType.Override8.getName());

        if(!ph.getDrLowLevel().equals(ph1.getDrLowLevel()))
        {
        	profileLog.append("Mapping Values for Low Changed for profile : "
    				+ profilename + ",from : "+mMap.get(ph1.getDrLowLevel()).toString() + " to : "+mMap.get(ph.getDrLowLevel()).toString());        
        }
        if(!ph.getDrHighLevel().equals(ph1.getDrHighLevel()))
        {
        	profileLog.append("Mapping Values for High Changed for profile : "
    				+ profilename + ",from : "+mMap.get(ph1.getDrHighLevel()).toString() + " to : "+mMap.get(ph.getDrHighLevel()).toString());        	
        }
        if(!ph.getDrModerateLevel().equals(ph1.getDrModerateLevel()))
        {
        	profileLog.append("Mapping Values for Moderate Changed for profile : "
    				+ profilename + ",from : "+mMap.get(ph1.getDrModerateLevel()).toString() + " to : "+mMap.get(ph.getDrModerateLevel()).toString());        	
        }
        if(!ph.getDrSpecialLevel().equals(ph1.getDrSpecialLevel()))
        {
        	profileLog.append("Mapping Values for Special Changed for profile : "
    				+ profilename + ",from : "+mMap.get(ph1.getDrSpecialLevel()).toString() + " to : "+mMap.get(ph.getDrSpecialLevel()).toString());        	
        }
        if(!ph.getHolidayLevel().equals(ph1.getHolidayLevel()))
        {
        	profileLog.append("Mapping Values for Holiday Changed for profile : "
    				+ profilename + ",from : "+mMap.get(ph1.getHolidayLevel()).toString() + " to : "+mMap.get(ph.getHolidayLevel()).toString());        	
        }

        profileLog.append(":::Weekday::") ;
           if(ph1.getMorningProfile().getMinLevel().longValue() != ph.getMorningProfile().getMinLevel().longValue() || ph1.getMorningProfile().getOnLevel().longValue() != ph.getMorningProfile().getOnLevel().longValue()){
        	   profileLog.append("Morning:Min " + ph1.getMorningProfile().getMinLevel()+ "->" + ph.getMorningProfile().getMinLevel()
        							+ ",Max " +  ph1.getMorningProfile().getOnLevel()+ "->" + ph.getMorningProfile().getOnLevel() + ", ");
           }
           
           if(ph1.getDayProfile().getMinLevel().longValue() != ph.getDayProfile().getMinLevel().longValue() || ph1.getDayProfile().getOnLevel().longValue() != ph.getDayProfile().getOnLevel().longValue()){
        	   profileLog.append("Day:Min " + ph1.getDayProfile().getMinLevel()+ "->" + ph.getDayProfile().getMinLevel()
        							+ ",Max " +  ph1.getDayProfile().getOnLevel()+ "->" + ph.getDayProfile().getOnLevel() + ", ");
           }
        			
           if(ph1.getEveningProfile().getMinLevel().longValue() != ph1.getEveningProfile().getMinLevel().longValue() || ph1.getEveningProfile().getOnLevel().longValue() !=ph.getEveningProfile().getOnLevel().longValue() ){
        	   profileLog.append( "Evening:Min " + ph1.getEveningProfile().getMinLevel()+ "->" + ph.getEveningProfile().getMinLevel()
        							+ ",Max " +  ph1.getEveningProfile().getOnLevel()+ "->" + ph.getEveningProfile().getOnLevel() + ", ");
           }
           
           if(ph1.getNightProfile().getMinLevel().longValue() != ph.getNightProfile().getMinLevel().longValue() || ph1.getNightProfile().getOnLevel().longValue() != ph.getNightProfile().getOnLevel().longValue()){
        	   profileLog.append( "Night:Min " + ph1.getNightProfile().getMinLevel()+ "->" + ph.getNightProfile().getMinLevel()
        							+ ",Max " +  ph1.getNightProfile().getOnLevel()+ "->" + ph.getNightProfile().getOnLevel() + ", ");
           }
           
           profileLog.append("Weekend::");

           if(ph1.getMorningProfileWeekEnd().getMinLevel().longValue() != ph.getMorningProfileWeekEnd().getMinLevel().longValue() || ph1.getMorningProfileWeekEnd().getOnLevel().longValue() != ph.getMorningProfileWeekEnd().getOnLevel().longValue()){
        	   profileLog.append( "Morning:Min " + ph1.getMorningProfileWeekEnd().getMinLevel()+ "->" + ph.getMorningProfileWeekEnd().getMinLevel()
        							+ ",Max " +  ph1.getMorningProfileWeekEnd().getOnLevel()+ "->" + ph.getMorningProfileWeekEnd().getOnLevel() + ", ");
           }
           
           if(ph1.getDayProfileWeekEnd().getMinLevel().longValue() != ph.getDayProfileWeekEnd().getMinLevel().longValue() || ph1.getDayProfileWeekEnd().getOnLevel().longValue() != ph.getDayProfileWeekEnd().getOnLevel().longValue()){
        	   profileLog.append("Day:Min " + ph1.getDayProfileWeekEnd().getMinLevel()+ "->" + ph.getDayProfileWeekEnd().getMinLevel()
        							+ ",Max " +  ph1.getDayProfileWeekEnd().getOnLevel()+ "->" + ph.getDayProfileWeekEnd().getOnLevel() + ", ");
           }
           
           if(ph1.getEveningProfileWeekEnd().getMinLevel().longValue() != ph.getEveningProfileWeekEnd().getMinLevel().longValue() || ph1.getNightProfileWeekEnd().getOnLevel().longValue() != ph.getNightProfileWeekEnd().getOnLevel().longValue()){
        	   profileLog.append("Evening:Min " + ph1.getEveningProfileWeekEnd().getMinLevel()+ "->" + ph.getEveningProfileWeekEnd().getMinLevel()
        							+ ",Max " +  ph1.getNightProfileWeekEnd().getOnLevel()+ "->" + ph.getNightProfileWeekEnd().getOnLevel() + ", ");
           }
           
           if(ph1.getNightProfileWeekEnd().getMinLevel().longValue() != ph.getNightProfileWeekEnd().getMinLevel().longValue() || ph1.getNightProfileWeekEnd().getOnLevel().longValue() != ph.getNightProfileWeekEnd().getOnLevel().longValue()){
        	   profileLog.append("Night:Min " + ph1.getNightProfileWeekEnd().getMinLevel()+ "->" + ph.getNightProfileWeekEnd().getMinLevel()
        							+ ",Max " +  ph1.getNightProfileWeekEnd().getOnLevel()+ "->" + ph.getNightProfileWeekEnd().getOnLevel() + ". ");
           }
    				
        							
        ph1.copyProfilesFrom(ph);
        // short circuited the saving of weekday, Need ordered lists instead of sets
        Set<WeekDay> week = ph1.getProfileConfiguration().getWeekDays();
        int count = 0;
        for (WeekDay day : week) {
            if ("true".equals(days[count])) {
                day.setType("weekday");
            } else {
                day.setType("weekend");
            }
            count++;
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
		ph1.setDaylightHarvesting(ph.getDaylightHarvesting());
		ph1.setDaylightProfileBelowMin(ph.getDaylightProfileBelowMin());
		ph1.setDaylightForceProfileMinValue(ph.getDaylightForceProfileMinValue());
		ph1.setHolidayLevel(ph.getHolidayLevel());
		
		ph1.copyOverrideProfilesFrom(ph);
        if ("profilegroup".equals(type)) {
            // 1. Copy the profile configuration of the current group on to each fixtures within the group with this
            // profile configuration
            // and update the group_id to the current one.
            // 2. Enable push flag for the list of fixture(s) within the group to set to true, The profile will be
            // sync'd in the next PM stat
            groupManager.updateGroupProfile(ph1, typeid);
			groupManager.updateAdvanceGroupProfile(ph1, typeid);
        } else if ("fixture".equals(type)) {
            // 1. Set the group_id to 0 (in fixture and fixture's profile handler as well);
            // 2. enable push flag for the fixture to set to true
            //TODO:
           // Fixture oFixture = fixtureManager.getFixtureById(typeid);
            //fixtureManager.updateProfileHandler(ph1, typeid, 0L, ServerConstants.CUSTOM_PROFILE, oFixture.getCurrentProfile());
        } else {
    		if (type == null || "".equals(type)) {
            	return "{\"success\":-2, \"message\" : \""
        				+ "Incorrect Group type" + "\"}";
    		}
        }
        
        // Update the Profiles sync status flag if current profile is EM only profile. i.e. present in em_profile_mapping table
        List<EmProfileMapping>  emProfileMappingList= emProfileMappingManager.getEMProfileMappingByUEMProfileId(groupObj.getId());
        if(emProfileMappingList!=null && emProfileMappingList.size()>0)
        {
        	Iterator<EmProfileMapping> itr= emProfileMappingList.iterator();
        	while(itr.hasNext())
        	{
        		EmProfileMapping emProfileMapping = itr.next();
        		emProfileMapping.setSyncStatus(1);
                emProfileMappingManager.saveOrUpdate(emProfileMapping);
                profileSyncManager.syncProfileGroupsToEM();
        	}
        }
        
		return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("profile.message.success", null,
				local) + "\", \"groupId\" : \""+groupObj.getId() +"\"}";
	}	

	/**
	 * Real Time pushing profile to Fixture
	 */
	@RequestMapping(value = "/realTimeProfilePush.ems", method = RequestMethod.POST)
    @ResponseBody
    public String realTimeProfilePush(@RequestParam("type") String type, @RequestParam("typeid") Long fixtureId, Locale local) {
		if ("fixture".equals(type)) {
		        //TODO:
		        //fixtureManager.pushProfileToFixtureNow(fixtureId);
		}
		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("profile.pushProfilemessage.success", null,
						local) + "\", \"typeid\" : \""+fixtureId +"\"}";
	}
	
	/**
	 * Manages the list of fixtures and discover more for a selected profile
	 * 
	 * @param model
	 *            used in communicating back
	 * @param cookie
	 *            distinguishes the appropriate profile
	 * @return titles template definition to display profiles-fixtures-settings
	 *         page
	 */

	@RequestMapping(value = "/profilesfixturessettings.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profilesFixturesSettings(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedProfileCookie) String cookie) {

		if (cookie == null || "".equals(cookie)) {
			return null; // TODO show internal error page.
		}

		int seperator = cookie
				.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
		if (seperator < 0) {
			return null; // TODO show internal error page.
		}
		String type = cookie.substring(1, seperator); // group or fixture or template
		Long id = new Long(cookie.substring(seperator + 1));

		//TODO:
		if ("profilegroup".equals(type)) {
			//model.addAttribute("fixtures",fixtureManager.loadFixtureByGroupId(id));
		}
		else if ("profiletemplate".equals(type)) {
				//model.addAttribute("fixtures",fixtureManager.loadFixtureByTemplateId(id));
		} else {
			return null; // TODO show internal error page.
		}
		return "profile/profilesfixturessettings";
	}
	@RequestMapping(value = "/list.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadAllProfiles(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,@RequestParam(value = "templateId", required = false) long templateId,@RequestParam(value = "parentNodeId", required = false) Long parentNodeId)
	{
		List<ProfileGroups> profileList=null;
		Facility facility=null;
		if(parentNodeId!=null && parentNodeId>0)
		{
			facility = facilityManager.getFacility(parentNodeId);
		}
		if (enlightedAuthContext.getCurrentUserRoleType() == RoleType.Admin)
		{
			if(facility!=null && facility.getType()==FacilityType.ORGANIZATION.ordinal())
			{
				//load all profile for entire campus
				model.addAttribute("mode", FacilityType.ORGANIZATION.getName());
				profileList = groupManager.loadAllDownloadedProfilesByCampusId(templateId);
			}else if(facility!=null && facility.getType()==FacilityType.CAMPUS.ordinal())
			{
				//load all profile for entire building
				model.addAttribute("mode", FacilityType.CAMPUS.getName());
				profileList = groupManager.loadAllDownloadedProfilesByBuildingId(templateId);
			}else if(facility!=null && facility.getType()==FacilityType.BUILDING.ordinal())
			{
				//load all profile for template w.r.t campus/building level.
				profileList = groupManager.loadAllDownloadedProfilesByTemplateId(templateId);
				model.addAttribute("mode", FacilityType.BUILDING.getName());
			}else if(facility==null && parentNodeId.longValue()==0 && templateId==0)
			{
				//load all profiles for all global default hierachy
				profileList = groupManager.loadAllGlobalDefaultProfiles();
				model.addAttribute("mode", FacilityType.GLOBALDEFAULT.getName());
			}else
			{
				//load all profile for default templates
				model.addAttribute("mode", FacilityType.PROFILETEMPLATE.getName());
				profileList = groupManager.loadAllProfileTemplateById(templateId,0L,true);
			}
		}
		model.addAttribute("profileList", profileList);
		model.addAttribute("templateId", templateId);
		return "profileManagement/list";
	}
    
	@RequestMapping(value = "/addeditsetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profileAddEditSetting(Model model,@CookieValue(FacilityCookieHandler.selectedProfileCookie) String cookie,@RequestParam("groupId") Long groupId,@RequestParam("templateId") long templateId,@RequestParam("type") String type,
			@RequestParam("customerId") Long customerId,@RequestParam(value="oldProfileName",required=false) String oldProfileName) {
	    ProfileGroups oGroup=null;
	    String[] cookieArr = cookie.split(FacilityCookieHandler.facilityCookieNameSeparator);
		Long parentId = new Long(cookieArr[2]);
		Facility facility=null;
		if(parentId!=null && parentId>0)
		{
			facility = facilityManager.getFacility(parentId);
		}
		model.addAttribute("type", "group");
		model.addAttribute("customerId", customerId);
		if(oldProfileName!=null)                                                                                                                                                                                               
		{
			model.addAttribute("oldProfileName",oldProfileName);
		}
		List<ProfileGroups> profileList=null;
		ProfileGroups derivedGrp=null;
		String returnURL="";
		
		//In Case of New, load list of all Derived profile available as per the role applied
		if(type.equalsIgnoreCase("new"))
		{
		    //enlightedAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin
			if (enlightedAuthContext.getCurrentUserRoleType() == RoleType.Admin)
			{
				//Template ID is <=17, it means it is enlighted provided default template. ID above than 16 will be user defined template.
				if(templateId<=17)
				{
					if(facility!=null && facility.getType()==FacilityType.BUILDING.ordinal())
					{
						profileList = groupManager.loadAllProfileTemplateById(templateId,(long) 0,false);
					}else
					{
						profileList = groupManager.loadAllProfileTemplateById(templateId,(long) 0,true);
					}
				}else
				{
					profileList = groupManager.loadAllGroupsExceptDeafult();
				}
			}
			model.addAttribute("groups", profileList);
			if(groupId==-1)
			{
				oGroup = groupManager.getGroupById(profileList.get(0).getId());
			}else
			{
				oGroup = groupManager.getGroupById(groupId);
			}
			ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
			model.addAttribute("profilehandler", ph);
			model.addAttribute("management","new");
			returnURL = "profile/addeditsetting";
		}else
		{
			//In Case of Edit profile, load the existing derived profile
			oGroup = groupManager.getGroupById(groupId);
			derivedGrp = oGroup.getDerivedFromGroup();
			// group is derived from other group then return derived group other wise return default group
			if(derivedGrp==null && oGroup.isDefaultProfile()==true)
			{				//Load Default profile 
				derivedGrp = groupManager.getGroupById((long) 1);
			}else if(derivedGrp==null)
			{
				derivedGrp = new ProfileGroups();
				derivedGrp.setName("None");
			}
			
			ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
			model.addAttribute("profilehandler", ph);
			model.addAttribute("groups", derivedGrp);
			model.addAttribute("management","edit");			
			returnURL = "profile/addeditsetting";
		}
		
		//Template ID is >17, it means we are creating new profile first time for user defined template. Therefore retaining same template ID.
		if(groupId==-1 || templateId>17)
		{
			model.addAttribute("templateId", templateId);
		}
		else
		{
			model.addAttribute("templateId", oGroup.getProfileTemplate().getId());
		}
		model.addAttribute("typeid", oGroup.getId());
		model.addAttribute("group", oGroup);
		// Note : Add in the order of enums defined
		Long min = (long) 0 , max = (long) 0;
		ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
		if(ph.getMorningProfile().getMinLevel() > min)
		{
			min = ph.getMorningProfile().getMinLevel();
		}
		else if(ph.getDayProfile().getMinLevel() > min)
		{
			min = ph.getDayProfile().getMinLevel();
		}
		else if(ph.getEveningProfile().getMinLevel() > min)
		{
			min = ph.getEveningProfile().getMinLevel();
		}
		else if(ph.getNightProfile().getMinLevel() > min)
		{
			min = ph.getNightProfile().getMinLevel();
		}		
		
		//Calculate the max 
		if(ph.getMorningProfile().getOnLevel() > max)
		{
			max = ph.getMorningProfile().getOnLevel();
		}
		else if(ph.getDayProfile().getOnLevel() > max)
		{
			max = ph.getDayProfile().getOnLevel();
		}
		else if(ph.getEveningProfile().getOnLevel() > max)
		{
			max = ph.getEveningProfile().getOnLevel();
		}
		else if(ph.getNightProfile().getOnLevel() > max)
		{
			max = ph.getNightProfile().getOnLevel();
		}
		ArrayList<String> list = new ArrayList<String>();
		list.add(ProfileOverrideType.No_Override.getName());
		list.add(ProfileOverrideType.Override1.getName());
		list.add(ProfileOverrideType.Override2.getName());
		list.add(ProfileOverrideType.Override3.getName());
		list.add(ProfileOverrideType.Override4.getName());
		model.addAttribute("overridelist", list);
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(ProfileOverrideType.No_Override.getName());
		list2.add(ProfileOverrideType.Override5.getName());
		list2.add(ProfileOverrideType.Override6.getName());
		list2.add(ProfileOverrideType.Override7.getName());
		list2.add(ProfileOverrideType.Override8.getName());
		model.addAttribute("override2list", list2);
		model.addAttribute("minDR", min);
		model.addAttribute("maxDR", max);
		return returnURL;
	}
	
	@RequestMapping(value = "/updateProfileName.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public String renameProfile(@ModelAttribute("group") ProfileGroups group,@RequestParam("templateId") long templateId,Locale local) {
    	//Need to Update existing profile name
	    ProfileGroups groupObj = groupManager.getGroupById(group.getId());
		groupObj.setName(group.getName());
		groupManager.editName(groupObj);
    	return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("profile.renamemessage.success", null,
				local) + "\"}";
	}
	
	@RequestMapping("/template_profile_visibility.ems")
    public String setVisibilityOfGroups(@RequestParam("selectedTemplateProfiles") String selectedTemplateProfiles){
		String visibility[] = selectedTemplateProfiles.split(",");
		String  fixtureAssociatedProfile;
		fixtureAssociatedProfile = groupManager.updateGroupVisibility(visibility);
        return "redirect:/profile/filterProfile.ems?fixtureAssociatedProfile="+fixtureAssociatedProfile;
    }
	
	@RequestMapping(value = "/createNewProfileData.ems", method = RequestMethod.POST)
    @ResponseBody
    public String createNewProfileData(@RequestParam("type") String type, @RequestParam("typeid") Long typeid, @RequestParam("profilename") String profilename,
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") ProfileHandler ph, @RequestParam("templateId") long templateId, @RequestParam("customerId") Long customerId,
            Locale local) {
		
		if (weekdays == null || "".equals(weekdays)) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Week information not provided" + "\"}";
        }
		
		String[] days = weekdays.split(",");
		 if (type == null || "".equals(type)) {
	        	return "{\"success\":-1, \"message\" : \""
	    				+ "Group type not provided" + "\"}";
		}
		
		String groupkey = "";
		groupkey = profilename.replaceAll(" ", "").toLowerCase();
		groupkey = "default." + groupkey + ".";
		ProfileGroups derivedGrp = groupManager.getGroupById(typeid);
		ProfileHandler ph1 = profileManager.createProfile(groupkey, derivedGrp.getId().intValue(),true);
		ProfileHandler mDefaultDr = profileManager.getProfileHandlerById(derivedGrp.getProfileHandler().getId());
         //Now modify
        ph1.copyProfilesFrom(ph);
        							
        // short circuited the saving of weekday, Need ordered lists instead of sets
        Set<WeekDay> week = ph1.getProfileConfiguration().getWeekDays();
        for (int i = 1; i <= days.length; i++) {
	        for (WeekDay day : week) {
	        	if (day.getShortOrder() == i) {
		        	if ("true".equals(days[i-1])) {
		                day.setType("weekday");
		            } else {
		                day.setType("weekend");
		            }
		        	break;
	        	}
	        }
        }
        ph1.copyPCTimesFrom(ph);
        
        Long tenantID=null;
        // Update Advance Profile
        Long emId=null;
        Short profileNo = (groupManager.getMaxProfileNo(tenantID,emId));//tenantID
        
        if (profileNo==0) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Maximum 255 Profile can be created per user" + "\"}";
        }
        
        if (profileNo==null) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Profile can not be saved due to some internal server problem" + "\"}";
        }
        
        ph1.copyAdvanceProfileFrom(ph);
        ph1.setProfileGroupId(profileNo);
		ph1.copyOverrideProfilesFrom(ph);
		profileManager.saveProfileHandler(ph1);
		Customer company = customerManager.loadCustomerById(customerId);
		ProfileGroups group = new ProfileGroups();
        group.setProfileNo(profileNo);
		group.setName(profilename);
		group.setDefaultProfile(false);
		group.setDisplayProfile(true);
		ProfileTemplate profileTemplate = profileTemplateManager.getProfileTemplateById(templateId);
		group.setProfileTemplate(profileTemplate);
		group.setDerivedFromGroup(derivedGrp);
        group.setProfileHandler(ph1);
        group.setCompany(company);
        group.setGlobalCreatedProfile(true);
        
        metaDataManager.saveOrUpdateGroup(group);
        
        //mDefaultDr default profile handler , ph is with changed values.
        Map<Byte,String> mMap = new HashMap<Byte, String>();
        mMap.put((byte) 0, ProfileOverrideType.No_Override.getName());
        mMap.put((byte) 1, ProfileOverrideType.Override1.getName());
        mMap.put((byte) 2, ProfileOverrideType.Override2.getName());
        mMap.put((byte) 3, ProfileOverrideType.Override3.getName());
        mMap.put((byte) 4, ProfileOverrideType.Override4.getName());
        mMap.put((byte) 5, ProfileOverrideType.Override5.getName());
        mMap.put((byte) 6, ProfileOverrideType.Override6.getName());
        mMap.put((byte) 7, ProfileOverrideType.Override7.getName());
        mMap.put((byte) 8, ProfileOverrideType.Override8.getName());
        StringBuilder profileLog = new StringBuilder("::Override Profiles::");
        if(!ph.getDrLowLevel().equals(mDefaultDr.getDrLowLevel()))
        {
        	profileLog.append("Mapping Values for Low Changed for profile : "
    				+ profilename + ",from : "+mMap.get(mDefaultDr.getDrLowLevel()).toString() + " to : "+mMap.get(ph.getDrLowLevel()).toString());        	
        }
        if(!ph.getDrHighLevel().equals(mDefaultDr.getDrHighLevel()))
        {
        	profileLog.append("Mapping Values for High Changed for profile : "
    				+ profilename + ",from : "+mMap.get(mDefaultDr.getDrHighLevel()).toString() + " to : "+mMap.get(ph.getDrHighLevel()).toString());        	
        }
        if(!ph.getDrModerateLevel().equals(mDefaultDr.getDrModerateLevel()))
        {
        	profileLog.append("Mapping Values for Moderate Changed for profile : "
    				+ profilename + ",from : "+mMap.get(mDefaultDr.getDrModerateLevel()).toString() + " to : "+mMap.get(ph.getDrModerateLevel()).toString());        	
        }
        if(!ph.getDrSpecialLevel().equals(mDefaultDr.getDrSpecialLevel()))
        {
        	profileLog.append("Mapping Values for Special Changed for profile : "
    				+ profilename + ",from : "+mMap.get(mDefaultDr.getDrSpecialLevel()).toString() + " to : "+mMap.get(ph.getDrSpecialLevel()).toString());        	
        }
        if(!ph.getHolidayLevel().equals(mDefaultDr.getHolidayLevel()))
        {
        	profileLog.append("Mapping Values for Holiday Changed for profile : "
    				+ profilename + ",from : "+mMap.get(mDefaultDr.getHolidayLevel()).toString() + " to : "+mMap.get(ph.getHolidayLevel()).toString());        	
        }
        
		return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("profile.newProfilemessage.success", null,
				local) + "\", \"groupId\" : \""+group.getId() +"\"}";
	}
	@RequestMapping(value = "/filterProfile.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String filterProfile(Model model, @RequestParam(value = "fixtureAssociatedProfile", required = false) String fixtureAssociatedProfile) {
		TreeNode<FacilityType> facilityTreeHierarchy = null;
		boolean visibilityCheck =false;
		facilityTreeHierarchy = groupManager.loadProfileHierarchyForUser(enlightedAuthContext.getUserId(),visibilityCheck);
		model.addAttribute("profileTreeHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		if(fixtureAssociatedProfile!=null)
			model.addAttribute("fixtureAssociatedProfile", fixtureAssociatedProfile);
		return "profile/filterProfile";
	}
	
}
