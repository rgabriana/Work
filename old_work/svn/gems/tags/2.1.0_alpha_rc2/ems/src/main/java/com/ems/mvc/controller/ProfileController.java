package com.ems.mvc.controller;

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

import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.model.WeekDay;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.ProfileManager;

@Controller
@RequestMapping("/profile")
public class ProfileController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "profileManager")
    private ProfileManager profileManager;
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/setting.ems", method = { RequestMethod.POST, RequestMethod.GET })
    // This is the cookie, we have put selected profile-node in.
    public String profileSetting(Model model, @CookieValue(FacilityCookieHandler.selectedProfileCookie) String cookie) {

        if (cookie == null || "".equals(cookie)) {
            return null; // TODO show internal error page.
        }

        int seperator = cookie.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
        if (seperator < 0) {
            return null; // TODO show internal error page.
        }
        String type = cookie.substring(1, seperator); // group or fixture
        Long id = new Long(cookie.substring(seperator + 1));

        if ("group".equals(type)) {
            Groups group = groupManager.getGroupByName(groupManager.getGroupById(id).getName());
            ProfileHandler ph = profileManager.getProfileHandlerById(group.getProfileHandler().getId());
            model.addAttribute("profilehandler", ph);
            model.addAttribute("type", "group");
            model.addAttribute("typeid", group.getId());
        } else {
            return null; // TODO show internal error page.
        }
        return "profile/setting";
    }

    @RequestMapping(value = "/fixturesetting.ems", method = { RequestMethod.POST, RequestMethod.GET })
    public String profileFixtureSetting(Model model, @RequestParam("fixtureId") Long id) {
        Fixture fixture = fixtureManager.getFixtureById(id);
        ProfileHandler ph = profileManager.getProfileHandlerById(fixture.getProfileHandler().getId());
        model.addAttribute("profilehandler", ph);
        model.addAttribute("type", "fixture");
        model.addAttribute("typeid", fixture.getId());
        return "profile/fixture/setting";
    }

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
        
        userAuditLoggerUtil.log("Update Profile Handler: " + ph1.getId());

        return "{\"success\":1, \"message\" : \"" + messageSource.getMessage("profile.message.success", null, local)
                + "\"}";
    }

    @RequestMapping(value = "/updateAdvancedConfiguration.ems", method = RequestMethod.POST)
    @ResponseBody
    public String updateAdvancedConfiguration(@RequestParam("type") String type, @RequestParam("typeid") Long typeid,
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

        if ("group".equals(type)) {
            // 1. Copy the profile configuration of the current group on to each fixtures within the group with this
            // profile configuration
            // and update the group_id to the current one.
            // 2. Enable push flag for the list of fixture(s) within the group to set to true, The profile will be
            // sync'd in the next PM stat
            groupManager.updateAdvanceGroupProfile(ph1, typeid);
        } else if ("fixture".equals(type)) {
            // 1. Set the group_id to 0 (in fixture and fixture's profile handler as well);
            // 2. enable push flag for the fixture to set to true
            Fixture oFixture = fixtureManager.getFixtureById(typeid);
            fixtureManager.updateProfileHandler(ph1, typeid, 0L, ServerConstants.CUSTOM_PROFILE, oFixture.getCurrentProfile());
        } else {
            return null; // TODO show internal error page.
        }
        
        userAuditLoggerUtil.log("Update Advance Profile Handler: " + ph1.getId());
        
        return "{\"success\":1, \"message\" : \"" + messageSource.getMessage("profile.message.success", null, local)
                + "\"}";
    }

    //Added by Nitin to get profile fixtures settings
    /**
     * Manages the list of fixtures and discover more for a selected profile
     * 
     * @param model
     *            used in communicating back
     * @param cookie
     *            distinguishes the appropriate profile
     * @return titles template definition to display profiles-fixtures-settings page
     */
    
    @RequestMapping(value = "/profilesfixturessettings.ems", method = { RequestMethod.POST, RequestMethod.GET })
    public String profilesFixturesSettings(Model model, @CookieValue(FacilityCookieHandler.selectedProfileCookie) String cookie) {

        if (cookie == null || "".equals(cookie)) {
            return null; // TODO show internal error page.
        }

        int seperator = cookie.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
        if (seperator < 0) {
            return null; // TODO show internal error page.
        }
        String type = cookie.substring(1, seperator); // group or fixture
        Long id = new Long(cookie.substring(seperator + 1));

        if ("group".equals(type)) {            
            model.addAttribute("fixtures", fixtureManager.loadFixtureByGroupId(id));
        } 
        else {
            return null; // TODO show internal error page.
        }
        return "profile/profilesfixturessettings";
    }   
}
