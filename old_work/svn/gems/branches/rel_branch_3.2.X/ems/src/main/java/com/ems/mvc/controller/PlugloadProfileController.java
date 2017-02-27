package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.ems.model.Company;
import com.ems.model.Groups;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.PlugloadProfileTemplate;
import com.ems.model.Tenant;
import com.ems.model.WeekdayPlugload;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.JsTreeOptions;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.CompanyManager;
import com.ems.service.MetaDataManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.PlugloadProfileManager;
import com.ems.service.PlugloadProfileTemplateManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.ProfileOverrideType;
import com.ems.types.RoleType;
import com.ems.util.tree.TreeNode;

@Controller
@RequestMapping("/plugloadProfile")
public class PlugloadProfileController {
	
	@Resource(name = "plugloadManager")
	private PlugloadManager plugloadManager;
	
	@Resource
	PlugloadGroupManager plugloadGroupManager;
	@Resource
	PlugloadProfileManager plugloadProfileManager;
	@Resource
	MetaDataManager metaDataManager;
	@Resource
	PlugloadProfileTemplateManager plugloadProfileTemplateManager;
	
	@Resource(name="companyManager")
	private CompanyManager companyManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@Resource
	private UserManager userManager;
	
	@Autowired
	private MessageSource messageSource;
	
	@RequestMapping(value = "/plugloadsetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String plugloadProfileSetting(Model model,@RequestParam(value="plugloadId",required=false) Long plugloadId,@RequestParam(value = "groupId", required = false) Long groupId) {
		Plugload plugload = null;
		PlugloadGroups plugloadGroup = null;
		
		if(groupId!=null)
		{
			//Default Profile - Read Only Mode - Calls from 
			plugloadGroup = plugloadGroupManager.getGroupById(groupId);
			model.addAttribute("type", "group");
			model.addAttribute("typeid", plugloadGroup.getId());
		}else
		{
			// Plugload Details - Read Only Mode - Calls from Floorplan - Plugload double click and PlugloadList - Edit Plugload pop up
			plugload = plugloadManager.getPlugloadById(plugloadId);
			plugloadGroup = plugloadGroupManager.getGroupById(plugload.getGroupId());
			model.addAttribute("typeid", plugload.getId());
			model.addAttribute("type", "plugload");
			
			//If any one of the Push profile flag is true then it indicates that profile sync is not done. This will activate Push Profile button on UI active.
			Boolean pushPendingStatus = false; 
			if(plugload.isPushGlobalProfile() || plugload.isPushProfile())
			{
				pushPendingStatus = true;
			}else
			{
				pushPendingStatus =false;
			}
			model.addAttribute("pushPendingStatus", pushPendingStatus);
		}
		
		
		plugload = plugloadManager.getPlugloadById(plugloadId);
		PlugloadProfileHandler profileHandler = plugloadProfileManager.getProfileHandlerById(1L);
		model.addAttribute("profilehandler", profileHandler);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(ProfileOverrideType.No_Override.getName());
		list.add(ProfileOverrideType.Override1.getName());
		list.add(ProfileOverrideType.Override2.getName());
		list.add(ProfileOverrideType.Override3.getName());
		list.add(ProfileOverrideType.Override4.getName());
		model.addAttribute("overridelist", list);	
		model.addAttribute("management","readonly");
		model.addAttribute("plugloadgroup",plugloadGroup);
		
		PlugloadGroups derivedGrp = plugloadGroup.getDerivedFromGroup();
		// group is derived from other group then return derived group other wise  return default group
		if(derivedGrp!=null && derivedGrp.getId()!=null)
		{
			model.addAttribute("groups",derivedGrp);	
		}else if(plugloadGroup.isDefaultProfile()==true && derivedGrp==null)
		{
			//Load Default profile 
			derivedGrp = plugloadGroupManager.getGroupById((long) 1);
			model.addAttribute("groups",derivedGrp);
		}else
		{
			Groups dummyGrp = new Groups();
			dummyGrp.setName("None");
			model.addAttribute("groups",dummyGrp);
		}
		
		return "plugloadprofile/plugload/setting";
	}
	
	@RequestMapping(value = "/plugloadprofilesetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String plugloadProfileSetting(Model model,@CookieValue(FacilityCookieHandler.selectedPlugloadProfileCookie) String cookie) {
		
		if (cookie == null || "".equals(cookie)) {
			return null; // TODO show internal error page.
		}

		int seperator = cookie
				.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
		if (seperator < 0) {
			return null; // TODO show internal error page.
		}
		String type = cookie.substring(1, seperator);
		Long id = new Long(cookie.substring(seperator + 1));
		
		if("plugloadgroup".equals(type)){
			
			PlugloadGroups plugloadGroup = plugloadGroupManager.getPlugloadGroupByName(plugloadGroupManager
					.getGroupById(id).getName());
			
			PlugloadProfileHandler profileHandler = plugloadProfileManager.getProfileHandlerById(plugloadGroup.getPlugloadProfileHandler().getId());
			
			model.addAttribute("profilehandler", profileHandler);
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(ProfileOverrideType.No_Override.getName());
			list.add(ProfileOverrideType.Override1.getName());
			list.add(ProfileOverrideType.Override2.getName());
			list.add(ProfileOverrideType.Override3.getName());
			list.add(ProfileOverrideType.Override4.getName());
			
			model.addAttribute("overridelist", list);	
			model.addAttribute("typeid",plugloadGroup.getId());
			
			if(plugloadGroup.isDefaultProfile()==true)
			{
				model.addAttribute("management","readonly");
			}
			else
			{
				model.addAttribute("management","edit");
			}
			model.addAttribute("plugloadgroup", plugloadGroup);
			model.addAttribute("type", "plugloadgroup");
			model.addAttribute("templateId", plugloadGroup.getPlugloadProfileTemplate().getId());
			
			PlugloadGroups derivedGrp = plugloadGroup.getDerivedFromGroup();
			// group is derived from other group then return derived group other wise  return default group
			if(derivedGrp!=null && derivedGrp.getId()!=null)
			{
				model.addAttribute("groups",derivedGrp);	
			}else if(plugloadGroup.isDefaultProfile()==true && derivedGrp==null)
			{
				//Load Default profile 
				derivedGrp = plugloadGroupManager.getGroupById((long) 1);
				model.addAttribute("groups",derivedGrp);
			}else
			{
				Groups dummyGrp = new Groups();
				dummyGrp.setName("None");
				model.addAttribute("groups",dummyGrp);
			}
			
			
		}else{
			return null;
		}
		
		return "plugloadprofile/setting";
		
	}
	
	/**
	 * Manages the list of fixtures  for a selected plugload profile
	 * 
	 * @param model
	 *            used in communicating back
	 * @param cookie
	 *            distinguishes the appropriate plugload profile
	 * @return titles template definition to display plugload-profiles-fixtures-settings
	 *         page
	 */

	@RequestMapping(value = "/plugloadprofilesdevicessettings.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String plugloadProfilesDevicesSettings(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedPlugloadProfileCookie) String cookie) {

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

		if ("plugloadgroup".equals(type)) {
			model.addAttribute("plugloads",plugloadManager.loadPlugloadByPlugloadGroupId(id));
		}
		else if ("plugloadtemplate".equals(type)) {
				model.addAttribute("plugloads",plugloadManager.loadPlugloadByProfileTemplateId(id));
		} else {
			return null; // TODO show internal error page.
		}
		return "plugloadprofile/plugloadprofilesdevicessettings";
	}
	
	@RequestMapping(value = "/list.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String loadAllPlugloadProfiles(Model model,@RequestParam(value = "plugloadTemplateId", required = false) long plugloadTemplateId)
	{
		List<PlugloadGroups> plugloadProfileList=null;
		if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin || emsAuthContext.getCurrentUserRoleType() == RoleType.FacilitiesAdmin)
		{
			plugloadProfileList = plugloadGroupManager.loadAllProfileTemplateById(plugloadTemplateId,0L);
		}
		model.addAttribute("plugloadProfileList", plugloadProfileList);
		model.addAttribute("plugloadTemplateId", plugloadTemplateId);
		return "plugloadProfileManagement/list";
	}
	
	@RequestMapping(value = "/addEditPlugloadProfileSetting.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String profileAddEditSetting(Model model,@RequestParam("groupId") Long groupId,@RequestParam("templateId") long templateId,@RequestParam("type") String type,
			@RequestParam(value="oldProfileName",required=false) String oldProfileName) {
		System.out.println("______________create plugload");
		PlugloadGroups plugloadGroup = null;
		model.addAttribute("type", "new");		
		
		if(oldProfileName!=null)
		{
			model.addAttribute("oldProfileName",oldProfileName);
		}
		List<PlugloadGroups> profileList = null;
		PlugloadGroups derivedGrp = null;
		String returnURL = "plugloadProfile/addeditsetting";
		if("new".equalsIgnoreCase(type)){
			
			//Template ID is <=1, it means it is enlighted provided default plugload template. ID above than 1 will be user defined plugload template.
			if(templateId<=1)
			{
				profileList = plugloadGroupManager.loadAllProfileTemplateById(templateId,0L);
			}else
			{
				profileList = plugloadGroupManager.loadAllPlugloadGroupsExceptDeafult();
			}
			System.out.println("__________________ profile list"+profileList);
			if(profileList != null){
				model.addAttribute("groups", profileList);
				if(groupId==-1)
				{
					plugloadGroup = plugloadGroupManager.getGroupById(profileList.get(0).getId());
				}else
				{
					plugloadGroup = plugloadGroupManager.getGroupById(groupId);
				}
				PlugloadProfileHandler ph = plugloadProfileManager.getProfileHandlerById(plugloadGroup.getPlugloadProfileHandler().getId());
				System.out.println("++++++++++++"+ph.getPlugloadProfileConfiguration().getWeekDays());
				model.addAttribute("profilehandler", ph);
				model.addAttribute("management","new");
				returnURL = "plugloadProfile/addeditsetting";
			}
		}else
		{
			//In Case of Edit plugload profile, load the existing derived profile
			plugloadGroup = plugloadGroupManager.getGroupById(groupId);
			derivedGrp = plugloadGroup.getDerivedFromGroup();
			// group is derived from other group then return derived group other wise return default group
			if(derivedGrp==null && plugloadGroup.isDefaultProfile()==true)
			{				//Load Default profile 
				derivedGrp = plugloadGroupManager.getGroupById((long) 1);
			}else if(derivedGrp==null)
			{
				derivedGrp = new PlugloadGroups();
				derivedGrp.setName("None");
			}
			
			PlugloadProfileHandler ph = plugloadProfileManager.getProfileHandlerById(plugloadGroup.getPlugloadProfileHandler().getId());
			model.addAttribute("profilehandler", ph);
			model.addAttribute("groups", derivedGrp);
			model.addAttribute("management","edit");			
			returnURL = "plugloadProfile/addeditsetting";
		}
		
		//Template ID is >1, it means we are creating new plugload profile first time for user defined plugload template. Therefore retaining same template ID.
		if(groupId==-1 || templateId>1)
		{
			model.addAttribute("templateId", templateId);
		}
		else
		{
			model.addAttribute("templateId", plugloadGroup.getPlugloadProfileTemplate().getId());
		}
		model.addAttribute("typeid", plugloadGroup.getId());
		model.addAttribute("plugloadgroup",plugloadGroup);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(ProfileOverrideType.No_Override.getName());
		list.add(ProfileOverrideType.Override1.getName());
		list.add(ProfileOverrideType.Override2.getName());
		list.add(ProfileOverrideType.Override3.getName());
		list.add(ProfileOverrideType.Override4.getName());
		model.addAttribute("overridelist", list);
		
		return returnURL ;
	}
	
	@RequestMapping(value = "/createNewPlugloadProfileData.ems", method = RequestMethod.POST)
    @ResponseBody
    public String createNewProfileData(@RequestParam("type") String type, @RequestParam("typeid") Long typeid, @RequestParam("profilename") String profilename,
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") PlugloadProfileHandler ph, @RequestParam("templateId") long templateId, Locale local) {
	
		
		 if (type == null || "".equals(type)) {
	        	return "{\"success\":-1, \"message\" : \""
	    				+ "Group type not provided" + "\"}";
		}
		
		String groupkey = "";
		groupkey = profilename.replaceAll(" ", "").toLowerCase();
		groupkey = "default." + groupkey + ".";
		
		PlugloadGroups derivedPlugloadGrp = plugloadGroupManager.getGroupById(typeid);
		
		PlugloadProfileHandler ph1 = null;
		ph1 = plugloadProfileManager.createPlugloadProfiles(ph,weekdays);
    
        //ph1.copyPCTimesFrom(ph);
		
		//Tenant tenant = userManager.loadUserById(emsAuthContext.getUserId()).getTenant();
        Long tenantID=null;
        //if(tenant!=null)
       // {
       // 	tenantID = tenant.getId();
       // }
     
		Short profileNo = (plugloadGroupManager.getMaxPlugloadProfileNo(tenantID));        
        if (profileNo==0) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Maximum 255 Profile can be created per user" + "\"}";
        }
        
        if (profileNo==null) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Profile can not be saved due to some internal server problem" + "\"}";
        }
        
        ph1.copyAdvancedSettingsFrom(ph);
        ph1.setProfileGroupId(profileNo);
	//	ph1.copyOverrideProfilesFrom(ph);
		plugloadProfileManager.savePlugloadProfileHandler(ph1);
		
		Company company = companyManager.getCompany();
		
		PlugloadGroups group = new PlugloadGroups();
        group.setProfileNo(profileNo);
		group.setName(profilename);
		group.setDefaultProfile(false);
		group.setDisplayProfile(true);
		PlugloadProfileTemplate profileTemplate = plugloadProfileTemplateManager.getPlugloadProfileTemplateById(templateId);
		group.setPlugloadProfileTemplate(profileTemplate);		
		group.setDerivedFromGroup(derivedPlugloadGrp);
		group.setPlugloadProfileHandler(ph1);
        group.setCompany(company);
        metaDataManager.saveOrUpdatePlugloadGroup(group);
		
		return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("plugloadprofile.newProfilemessage.success", null,
				local) + "\", \"groupId\" : \""+group.getId() +"\"}";
	}
	
	@RequestMapping(value = "/updatePlugloadProfileData.ems", method = RequestMethod.POST)
    @ResponseBody
    public String updatePlugloadProfileData(@RequestParam("type") String type, @RequestParam("typeid") Long typeid, @RequestParam(value="profilename",required=false) String profileName,
            @RequestParam("weekdays") String weekdays, @ModelAttribute("profilehandler") PlugloadProfileHandler ph, Locale local) {
       System.out.println("inside update plugloadprofile"+profileName);
		if (weekdays == null || "".equals(weekdays)) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Week information not provided" + "\"}";
        }

        String[] days = weekdays.split(",");
		if (type == null || "".equals(type)) {
        	return "{\"success\":-1, \"message\" : \""
    				+ "Group type not provided" + "\"}";
		}
		
		PlugloadGroups plugloadGroupObj = plugloadGroupManager.getGroupById(typeid);
		plugloadGroupObj.setName(profileName);
		plugloadGroupManager.editName(plugloadGroupObj);
		
		StringBuilder profileLog = new StringBuilder(":::Override Profiles::") ;
        PlugloadProfileHandler ph1 = plugloadProfileManager.getProfileHandlerById(plugloadGroupObj.getPlugloadProfileHandler().getId());
        Map<Byte,String> mMap = new HashMap<Byte, String>();
        mMap.put((byte) 0, ProfileOverrideType.No_Override.getName());
        mMap.put((byte) 1, ProfileOverrideType.Override1.getName());
        mMap.put((byte) 2, ProfileOverrideType.Override2.getName());
        mMap.put((byte) 3, ProfileOverrideType.Override3.getName());
        mMap.put((byte) 4, ProfileOverrideType.Override4.getName());

        if(!ph.getDrLowLevel().equals(ph1.getDrLowLevel()))
        {
        	profileLog.append("Mapping Values for Low Changed for profile : "
    				+ "" + ",from : "+mMap.get(ph1.getDrLowLevel()).toString() + " to : "+mMap.get(ph.getDrLowLevel()).toString());        
        }
        if(!ph.getDrHighLevel().equals(ph1.getDrHighLevel()))
        {
        	profileLog.append("Mapping Values for High Changed for profile : "
    				+ "" + ",from : "+mMap.get(ph1.getDrHighLevel()).toString() + " to : "+mMap.get(ph.getDrHighLevel()).toString());        	
        }
        if(!ph.getDrModerateLevel().equals(ph1.getDrModerateLevel()))
        {
        	profileLog.append("Mapping Values for Moderate Changed for profile : "
    				+ "" + ",from : "+mMap.get(ph1.getDrModerateLevel()).toString() + " to : "+mMap.get(ph.getDrModerateLevel()).toString());        	
        }
        if(!ph.getDrSpecialLevel().equals(ph1.getDrSpecialLevel()))
        {
        	profileLog.append("Mapping Values for Special Changed for profile : "
    				+ "" + ",from : "+mMap.get(ph1.getDrSpecialLevel()).toString() + " to : "+mMap.get(ph.getDrSpecialLevel()).toString());        	
        }

        profileLog.append(":::Weekday::") ;
           if(ph1.getMorningProfile().getActiveMotion().longValue() != ph.getMorningProfile().getActiveMotion().longValue() || ph1.getMorningProfile().getMode().longValue() != ph.getMorningProfile().getMode().longValue()){
        	   profileLog.append("Morning: Active Motion " + ph1.getMorningProfile().getActiveMotion()+ "->" + ph.getMorningProfile().getActiveMotion()
        							+ ",Mode " +  ph1.getMorningProfile().getMode()+ "->" + ph.getMorningProfile().getMode()+ ", ");
           }
           
           if(ph1.getDayProfile().getActiveMotion().longValue() != ph.getDayProfile().getActiveMotion().longValue() || ph1.getDayProfile().getMode().longValue() != ph.getDayProfile().getMode().longValue()){
        	   profileLog.append("Day: Active Motion " + ph1.getDayProfile().getActiveMotion()+ "->" + ph.getDayProfile().getActiveMotion()
        							+ ",Mode " +  ph1.getDayProfile().getMode()+ "->" + ph.getDayProfile().getMode()+ ", ");
           }
           if(ph1.getEveningProfile().getActiveMotion().longValue() != ph.getEveningProfile().getActiveMotion().longValue() || ph1.getEveningProfile().getMode().longValue() != ph.getEveningProfile().getMode().longValue()){
        	   profileLog.append("Evening: Active Motion " + ph1.getEveningProfile().getActiveMotion()+ "->" + ph.getEveningProfile().getActiveMotion()
        							+ ",Mode " +  ph1.getEveningProfile().getMode()+ "->" + ph.getEveningProfile().getMode()+ ", ");
           }
           if(ph1.getNightProfile().getActiveMotion().longValue() != ph.getNightProfile().getActiveMotion().longValue() || ph1.getNightProfile().getMode().longValue() != ph.getNightProfile().getMode().longValue()){
        	   profileLog.append("Night: Active Motion " + ph1.getNightProfile().getActiveMotion()+ "->" + ph.getNightProfile().getActiveMotion()
        							+ ",Mode " +  ph1.getNightProfile().getMode()+ "->" + ph.getNightProfile().getMode()+ ", ");
           }
        			
          
           
           profileLog.append("Weekend::");
           System.out.println("weekend ph1"+ph1.getMorningProfileWeekEnd().getActiveMotion()+" "+ph1.getMorningProfileWeekEnd().getMode() );
           System.out.println("weekend ph"+ph.getMorningProfileWeekEnd().getActiveMotion()+" "+ph.getMorningProfileWeekEnd().getMode() );
           if(ph1.getMorningProfileWeekEnd().getActiveMotion().longValue() != ph.getMorningProfileWeekEnd().getActiveMotion().longValue() || ph1.getMorningProfileWeekEnd().getMode().longValue() != ph.getMorningProfileWeekEnd().getMode().longValue()){
        	   profileLog.append("Morning Weekend: Active Motion " + ph1.getMorningProfileWeekEnd().getActiveMotion()+ "->" + ph.getMorningProfileWeekEnd().getActiveMotion()
        							+ ",Mode " +  ph1.getMorningProfileWeekEnd().getMode()+ "->" + ph.getMorningProfileWeekEnd().getMode()+ ", ");
           }
           if(ph1.getDayProfileWeekEnd().getActiveMotion().longValue() != ph.getDayProfileWeekEnd().getActiveMotion().longValue() || ph1.getDayProfileWeekEnd().getMode().longValue() != ph.getDayProfileWeekEnd().getMode().longValue()){
        	   profileLog.append("Day Weekend: Active Motion " + ph1.getDayProfileWeekEnd().getActiveMotion()+ "->" + ph.getDayProfileWeekEnd().getActiveMotion()
        							+ ",Mode " +  ph1.getDayProfileWeekEnd().getMode()+ "->" + ph.getDayProfileWeekEnd().getMode()+ ", ");
           }
           
           if(ph1.getEveningProfileWeekEnd().getActiveMotion().longValue() != ph.getEveningProfileWeekEnd().getActiveMotion().longValue() || ph1.getEveningProfileWeekEnd().getMode().longValue() != ph.getEveningProfileWeekEnd().getMode().longValue()){
        	   profileLog.append("Evening Weekend: Active Motion " + ph1.getEveningProfileWeekEnd().getActiveMotion()+ "->" + ph.getEveningProfileWeekEnd().getActiveMotion()
        							+ ",Mode " +  ph1.getEveningProfileWeekEnd().getMode()+ "->" + ph.getEveningProfileWeekEnd().getMode()+ ", ");
           }
       
           if(ph1.getNightProfileWeekEnd().getActiveMotion().longValue() != ph.getNightProfileWeekEnd().getActiveMotion().longValue() || ph1.getNightProfileWeekEnd().getMode().longValue() != ph.getNightProfileWeekEnd().getMode().longValue()){
        	   profileLog.append("Night Weekend: Active Motion " + ph1.getNightProfileWeekEnd().getActiveMotion()+ "->" + ph.getNightProfileWeekEnd().getActiveMotion()
        							+ ",Mode " +  ph1.getNightProfileWeekEnd().getMode()+ "->" + ph.getNightProfileWeekEnd().getMode()+ ", ");
           }
       
    				
        							
        ph1.copyProfilesFrom(ph);
        // short circuited the saving of weekday, Need ordered lists instead of sets
        Set<WeekdayPlugload> week = ph1.getPlugloadProfileConfiguration().getWeekDays();
        int count = 0;
        for (WeekdayPlugload day : week) {
            if ("true".equals(days[count])) {
                day.setType("weekday");
            } else {
                day.setType("weekend");
            }
            count++;
        }
        ph1.copyPCTimesFrom(ph);
             
		ph1.copyOverrideProfilesFrom(ph);           
		ph1.copyAdvancedSettingsFrom(ph);
		plugloadManager.updatePlugloadProfileHandler(ph1);
		
        return "{\"success\":1, \"message\" : \""
		+ messageSource.getMessage("plugloadprofile.message.success", null,
				local) + "\", \"groupId\" : \""+plugloadGroupObj.getId() +"\"}";
	}
	
	@RequestMapping(value = "/filterPlugloadProfile.ems", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String filterPlugloadProfile(Model model, @RequestParam(value = "plugloadAssociatedProfile", required = false) String plugloadAssociatedProfile) {
		TreeNode<FacilityType> plugloadProfileTreeHierarchy = null;
		boolean visibilityCheck =false;
		plugloadProfileTreeHierarchy = plugloadGroupManager.loadPlugloadProfileHierarchy(visibilityCheck);
		model.addAttribute("plugloadProfileTreeHierarchy", plugloadProfileTreeHierarchy);
		JsTreeOptions jsTreeOptions = new JsTreeOptions();
		jsTreeOptions.setCheckBoxes(true);
		model.addAttribute("jsTreeOptions", jsTreeOptions);
		if(plugloadAssociatedProfile!=null)
			model.addAttribute("plugloadAssociatedProfile", plugloadAssociatedProfile);
		return "plugloadprofile/filterPlugloadProfile";
	}
	
	@RequestMapping("/plugload_template_profile_visibility.ems")
    public String setVisibilityOfPlugloadGroups(@RequestParam("selectedPlugloadTemplateProfiles") String selectedPlugloadTemplateProfiles){
		String visibility[] = selectedPlugloadTemplateProfiles.split(",");
		String  plugloadAssociatedProfile = "";
		//plugloadAssociatedProfile = plugloadGroupManager.updateGroupVisibility(visibility);
        return "redirect:/plugloadProfile/filterPlugloadProfile.ems?plugloadAssociatedProfile="+plugloadAssociatedProfile;
    }
	
	/**
	 * Real Time pushing Plugload profile to Plugload
	 */
	@RequestMapping(value = "/realTimePlugloadProfilePush.ems", method = RequestMethod.POST)
    @ResponseBody
    public String realTimePlugloadProfilePush(@RequestParam("type") String type, @RequestParam("typeid") Long plugloadId, Locale local) {
		if ("plugload".equals(type)) {
			plugloadManager.pushPlugloadProfileToPlugloadNow(plugloadId);
		}
		return "{\"success\":1, \"message\" : \""
				+ messageSource.getMessage("plugloadprofile.pushProfilemessage.success", null,
						local) + "\", \"typeid\" : \""+plugloadId +"\"}";
	}
	

}
