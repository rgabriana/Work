package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
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

import com.ems.model.Company;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.ProfileTemplate;
import com.ems.model.Tenant;
import com.ems.model.WeekDay;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.server.ServerConstants;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.ProfileManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/profile")
public class ProfileController {

	@Resource
	private UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "profileManager")
	private ProfileManager profileManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name="companyManager")
	private CompanyManager companyManager;
	@Resource(name="metaDataManager")
	private MetaDataManager metaDataManager;
	@Resource(name="profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;
	@Resource
	private UserManager userManager;

	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/setting.ems", method = { RequestMethod.POST,
			RequestMethod.GET })
	// This is the cookie, we have put selected profile-node in.
	public String profileSetting(
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
		String type = cookie.substring(1, seperator); // group or fixture
		Long id = new Long(cookie.substring(seperator + 1));

		if ("group".equals(type)) {
			Groups group = groupManager.getGroupByName(groupManager
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
			Groups derivedGrp = group.getDerivedFromGroup();
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
				Groups dummyGrp = new Groups();
				dummyGrp.setName("None");
				model.addAttribute("groups",dummyGrp);
			}
		}else {
			return null; // TODO show internal error page.
		}
		return "profile/setting";
	}

	@RequestMapping(value = "/fixturesetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profileFixtureSetting(Model model,@RequestParam(value="fixtureId",required=false) Long id,@RequestParam(value = "groupId", required = false) Long groupId) {
		Groups oGroup =null;
		Fixture fixture = null;
		if(groupId!=null)
		{
			//Default Profile - Read Only Mode
			oGroup = groupManager.getGroupById(groupId);
			model.addAttribute("type", "group");
			model.addAttribute("typeid", oGroup.getId());
		}else
		{
			// Fixture Details - Read Only Mode
			fixture = fixtureManager.getFixtureById(id);
			oGroup = groupManager.getGroupById(fixture.getGroupId());
			model.addAttribute("typeid", fixture.getId());
			model.addAttribute("type", "fixture");
		}
		ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
		model.addAttribute("profilehandler", ph);
		model.addAttribute("group", oGroup);
		model.addAttribute("management","readonly");
		Groups derivedGrp = oGroup.getDerivedFromGroup();
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
			Groups dummyGrp = new Groups();
			dummyGrp.setName("None");
			model.addAttribute("groups",dummyGrp);
		}
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
		Groups groupObj = groupManager.getGroupById(typeid);
		groupObj.setName(profilename);
		groupManager.editName(groupObj);

        ProfileHandler ph1 = profileManager.getProfileHandlerById(ph.getId());

        StringBuilder profileLog = new StringBuilder(":::Weekday::") ;
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


        if ("group".equals(type)) {
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
            Fixture oFixture = fixtureManager.getFixtureById(typeid);
            fixtureManager.updateProfileHandler(ph1, typeid, 0L, ServerConstants.CUSTOM_PROFILE, oFixture.getCurrentProfile());
        } else {
    		if (type == null || "".equals(type)) {
            	return "{\"success\":-2, \"message\" : \""
        				+ "Incorrect Group type" + "\"}";
    		}
        }

//		String name = "";
//		if (ph1.getProfileGroupId().intValue() > 0) {
//			name = groupManager.getGroupById(
//					ph1.getProfileGroupId().longValue()).getName();
//		} else {
//			name = "profile handler " + ph.getId();
//		}
        
	//	userAuditLoggerUtil.log("Update profile configuration for "
		//		+ profilename, UserAuditActionType.Profile_Update.getName());
		
		userAuditLoggerUtil.log("Update profile configuration for " + profilename + profileLog + " There might be more profile changes.", UserAuditActionType.Profile_Update.getName());


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
		        fixtureManager.pushProfileToFixtureNow(fixtureId);
		}
		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("profile.pushProfilemessage.success", null,
						local) + "\", \"typeid\" : \""+fixtureId +"\"}";
	}
	
	@Deprecated
	@RequestMapping(value = "/updateBasicConfiguration.ems", method = RequestMethod.POST)
    @ResponseBody
    public String updateBasicConfiguration(@RequestParam("type") String type, @RequestParam("typeid") Long typeid,
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") ProfileHandler ph, Locale local) {
        if (weekdays == null || "".equals(weekdays)) {
            return null; // TODO show internal error page.
        }
        String[] days = weekdays.split(",");
        if (type == null || "".equals(type)) {
            return null; // TODO show internal error page.
        }
        ProfileHandler ph1 = profileManager.getProfileHandlerById(ph.getId());

        StringBuilder profileLog = new StringBuilder(":::Weekday::") ;
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

        if ("group".equals(type)) {
            // 1. Copy the profile configuration of the current group on to each fixtures within the group with this
            // profile configuration
            // and update the group_id to the current one.
            // 2. Enable push flag for the list of fixture(s) within the group to set to true, The profile will be
            // sync'd in the next PM stat
            groupManager.updateGroupProfile(ph1, typeid);
        } else if ("fixture".equals(type)) {
            // 1. Set the group_id to 0 (in fixture and fixture's profile handler as well);
            // 2. enable push flag for the fixture to set to true
            Fixture oFixture = fixtureManager.getFixtureById(typeid);
            fixtureManager.updateProfileHandler(ph1, typeid, 0L, ServerConstants.CUSTOM_PROFILE, oFixture.getCurrentProfile());
        } else {
            return null; // TODO show internal error page.
        }
        
        String name = "";
        if(ph1.getProfileGroupId().intValue() > 0) {
        	name = groupManager.getGroupById(ph1.getProfileGroupId().longValue()).getName();
        }
        else {
        	name = "profile handler " + ph.getId();
        }
        
        userAuditLoggerUtil.log("Update basic profile configuration for " + name + profileLog + " There might be more profile changes.", UserAuditActionType.Profile_Update.getName());

        return "{\"success\":1, \"message\" : \"" + messageSource.getMessage("profile.message.success", null, local)
                + "\"}";
    }

	@Deprecated
	@RequestMapping(value = "/updateAdvancedConfiguration.ems", method = RequestMethod.POST)
	@ResponseBody
	public String updateAdvancedConfiguration(
			@RequestParam("type") String type,
			@RequestParam("typeid") Long typeid,
			@ModelAttribute("profilehandler") ProfileHandler ph, Locale local) {
		if (type == null || "".equals(type)) {
			return null; // TODO show internal error page.
		}

		ProfileHandler ph1 = profileManager.getProfileHandlerById(ph.getId());
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


		if ("group".equals(type)) {
			// 1. Copy the profile configuration of the current group on to each
			// fixtures within the group with this
			// profile configuration
			// and update the group_id to the current one.
			// 2. Enable push flag for the list of fixture(s) within the group
			// to set to true, The profile will be
			// sync'd in the next PM stat
			groupManager.updateAdvanceGroupProfile(ph1, typeid);
		} else if ("fixture".equals(type)) {
			// 1. Set the group_id to 0 (in fixture and fixture's profile
			// handler as well);
			// 2. enable push flag for the fixture to set to true
			Fixture oFixture = fixtureManager.getFixtureById(typeid);
			fixtureManager.updateProfileHandler(ph1, typeid, 0L,
					ServerConstants.CUSTOM_PROFILE,
					oFixture.getCurrentProfile());
		} else {
			return null; // TODO show internal error page.
		}

		String name = "";
		if (ph1.getProfileGroupId().intValue() > 0) {
			name = groupManager.getGroupById(
					ph1.getProfileGroupId().longValue()).getName();
		} else {
			name = "profile handler " + ph.getId();
		}

		userAuditLoggerUtil.log("Update advanced profile configuration for "
				+ name, UserAuditActionType.Profile_Update.getName());

		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("profile.message.success", null,
						local) + "\"}";
	}

	// Added by Nitin to get profile fixtures settings
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

		if ("group".equals(type)) {
			model.addAttribute("fixtures",fixtureManager.loadFixtureByGroupId(id));
		}
		else if ("template".equals(type)) {
				model.addAttribute("fixtures",fixtureManager.loadFixtureByTemplateId(id));
		} else {
			return null; // TODO show internal error page.
		}
		return "profile/profilesfixturessettings";
	}
	@RequestMapping(value = "/list.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadAllProfiles(Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,@RequestParam(value = "templateId", required = false) long templateId)
	{
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long id = cookieHandler.getFacilityId();
		List<Groups> profileList=null;
		if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin || emsAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin)
		{
			profileList = groupManager.loadAllProfileTemplateById(templateId,0L);
		}else if(emsAuthContext.getCurrentUserRoleType()==RoleType.TenantAdmin)
		{
			 //Set the Tenant profile if present
	        Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
	        Long tenantId=0L;
			if(tenant != null)
			{
				tenantId = tenant.getId();
			}
			profileList = groupManager.loadAllProfileForTenantByTemplateId(templateId,tenantId);
		}
		
		model.addAttribute("profileList", profileList);
		model.addAttribute("templateId", templateId);
		model.addAttribute("floorId", id);
		return "profileManagement/list";
	}
    
	@RequestMapping(value = "/addeditsetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profileAddEditSetting(Model model,@RequestParam("groupId") Long groupId,@RequestParam("templateId") long templateId,@RequestParam("type") String type,
			@RequestParam(value="oldProfileName",required=false) String oldProfileName) {
		Groups oGroup=null;
		model.addAttribute("type", "group");
		if(oldProfileName!=null)
		{
			model.addAttribute("oldProfileName",oldProfileName);
		}
		List<Groups> profileList=null;
		Groups derivedGrp=null;
		String returnURL="";
		
		//In Case of New, load list of all Derived profile available as per the role applied
		if(type.equalsIgnoreCase("new"))
		{
			if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin || emsAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin)
			{
				//Template ID is <=16, it means it is enlighted provided default template. ID above than 16 will be user defined template.
				if(templateId<=16)
				{
					profileList = groupManager.loadAllProfileTemplateById(templateId,(long) 0);
				}else
				{
					profileList = groupManager.loadAllGroupsExceptDeafult();
				}
			}else if(emsAuthContext.getCurrentUserRoleType()==RoleType.TenantAdmin)
			{
				 //Set the Tenant profile if present
		        Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
		        Long tenantId=0L;
				if(tenant != null)
				{
					tenantId = tenant.getId();
					profileList = groupManager.loadAllProfileForTenantByTemplateId(templateId,tenantId);
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
				derivedGrp = new Groups();
				derivedGrp.setName("None");
			}
			
			ProfileHandler ph = profileManager.getProfileHandlerById(oGroup.getProfileHandler().getId());
			model.addAttribute("profilehandler", ph);
			model.addAttribute("groups", derivedGrp);
			model.addAttribute("management","edit");
			returnURL = "profile/addeditsetting";
		}
		
		//Template ID is >16, it means we are creating new profile first time for user defined template. Therefore retaining same template ID.
		if(groupId==-1 || templateId>16)
		{
			model.addAttribute("templateId", templateId);
		}
		else
		{
			model.addAttribute("templateId", oGroup.getProfileTemplate().getId());
		}
		model.addAttribute("typeid", oGroup.getId());
		model.addAttribute("group", oGroup);
		return returnURL;
	}
	
	@RequestMapping(value = "/updateProfileName.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public String renameProfile(@ModelAttribute("group") Groups group,@RequestParam("templateId") long templateId,Locale local) {
    	//Need to Update existing profile name
		Groups groupObj = groupManager.getGroupById(group.getId());
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
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") ProfileHandler ph, @RequestParam("templateId") long templateId, Locale local) {
		//System.out.println("profilename-----> " + profilename);
		
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
		Groups derivedGrp = groupManager.getGroupById(typeid);
		ProfileHandler ph1 = profileManager.createProfile(groupkey, derivedGrp.getId().intValue(),true);
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
        
        Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
        Long tenantID=null;
        if(tenant!=null)
        {
        	tenantID = tenant.getId();
        }
        // Update Advance Profile
        Short profileNo = (groupManager.getMaxProfileNo(tenantID));
        
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
		
		profileManager.saveProfileHandler(ph1);
		Company company = companyManager.getCompany();
		Groups group = new Groups();
        group.setProfileNo(profileNo);
		group.setName(profilename);
		group.setDefaultProfile(false);
		group.setDisplayProfile(true);
		ProfileTemplate profileTemplate = profileTemplateManager.getProfileTemplateById(templateId);
		group.setProfileTemplate(profileTemplate);
		group.setDerivedFromGroup(derivedGrp);
        group.setProfileHandler(ph1);
        group.setCompany(company);
        
        //Set the Tenant profile if present
		if(tenant != null)
		group.setTenant(tenant);
		 
        metaDataManager.saveOrUpdateGroup(group);
        
		userAuditLoggerUtil.log("Saved new profile"
				+ profilename, UserAuditActionType.Profile_Update.getName());

		//System.out.println("NEW PROFILE ID " + group.getId());
		return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("profile.newProfilemessage.success", null,
				local) + "\", \"groupId\" : \""+group.getId() +"\"}";
	}
	@RequestMapping(value = "/filterProfile.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String filterProfile(Model model, @RequestParam(value = "fixtureAssociatedProfile", required = false) String fixtureAssociatedProfile) {
		TreeNode<FacilityType> facilityTreeHierarchy = null;
		boolean visibilityCheck =false;
		facilityTreeHierarchy = groupManager.loadProfileHierarchyForUser(emsAuthContext.getUserId(),visibilityCheck);
		model.addAttribute("profileTreeHierarchy", facilityTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		if(fixtureAssociatedProfile!=null)
			model.addAttribute("fixtureAssociatedProfile", fixtureAssociatedProfile);
		return "profile/filterProfile";
	}
	
}
