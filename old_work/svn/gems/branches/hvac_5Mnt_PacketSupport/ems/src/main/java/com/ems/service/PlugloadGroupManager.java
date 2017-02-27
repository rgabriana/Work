package com.ems.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.PlugloadGroupDao;
import com.ems.dao.PlugloadProfileDao;
import com.ems.dao.PlugloadProfileTemplateDao;

import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;

import com.ems.model.WeekdayPlugload;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.types.FacilityType;
import com.ems.types.UserAuditActionType;
import com.ems.util.tree.TreeNode;
import com.ems.vo.AssignPlugloadList;


@Service("plugloadGroupManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadGroupManager {
	
	@Resource
	PlugloadGroupDao plugloadGroupDao;
	
	@Resource
	PlugloadProfileDao plugloadProfileDao;
	
	@Resource
	PlugloadProfileTemplateDao plugloadProfileTemplateDao;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	Map<Long, TreeNode<FacilityType>> plugloadProfileTreeMap = new HashMap<Long, TreeNode<FacilityType>>();
	
	public List<PlugloadGroups> loadAllProfileTemplateById(long templateId, Long tenantId) {
		return plugloadGroupDao.loadAllProfileTemplateById(templateId,tenantId);
	}

	public PlugloadGroups getGroupById(Long id) {
		return plugloadGroupDao.getGroupById(id);
		
	}
	
	public List<PlugloadGroups> loadAllPlugloadGroups() {
        return plugloadGroupDao.loadAllPlugloadGroups();
    }
	
	public List<PlugloadGroups> loadAllPlugloadGroupsExceptDeafult() {
		return plugloadGroupDao.loadAllPlugloadGroupsExceptDeafult();
	}
	
	public int deleteProfile(long profileId) {
		int status=1;
		try{
			PlugloadGroups group = (PlugloadGroups) plugloadGroupDao.loadObject(PlugloadGroups.class, profileId);
			
			List<PlugloadGroups> derivedProfileList = plugloadGroupDao.loadAllDerivedProfile(group.getId());
			if (derivedProfileList != null && !derivedProfileList.isEmpty())
		     {
	              Iterator<PlugloadGroups> it = derivedProfileList.iterator();
	              while (it.hasNext()) {
	            	  PlugloadGroups rowResult = it.next();
	            	  rowResult.setDerivedFromGroup(null);
	            	  plugloadGroupDao.saveObject(rowResult);
	              }
		     }
			
			PlugloadProfileHandler profileHandler =  (PlugloadProfileHandler) plugloadGroupDao.loadObject(PlugloadProfileHandler.class, group.getPlugloadProfileHandler().getId());		
			PlugloadProfile morningProfile =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getMorningProfile().getId());
			PlugloadProfile dayProfile =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getDayProfile().getId());
			PlugloadProfile eveningProfile =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getEveningProfile().getId());
			PlugloadProfile nightProfile =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getNightProfile().getId());
			
			PlugloadProfile morningProfileWeekend =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getMorningProfileWeekEnd().getId());
			PlugloadProfile dayProfileWeekend =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getDayProfileWeekEnd().getId());
			PlugloadProfile eveningProfileWeekend =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getEveningProfileWeekEnd().getId());
			PlugloadProfile nightProfileWeekend =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getNightProfileWeekEnd().getId());
			
			PlugloadProfile morningProfileHoliday =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getMorningProfileHoliday().getId());
			PlugloadProfile dayProfileHoliday =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getDayProfileHoliday().getId());
			PlugloadProfile eveningProfileHoliday =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getEveningProfileHoliday().getId());
			PlugloadProfile nightProfileHoliday =  (PlugloadProfile) plugloadGroupDao.loadObject(PlugloadProfile.class, profileHandler.getNightProfileHoliday().getId());
			
			PlugloadProfileConfiguration profileConfiguration =  (PlugloadProfileConfiguration) plugloadGroupDao.loadObject(PlugloadProfileConfiguration.class, profileHandler.getPlugloadProfileConfiguration().getId());
			
			List<WeekdayPlugload> weekDayList = plugloadGroupDao.loadAllWeekByProfileConfigurationId(profileConfiguration.getId());
			

			//Remove groups
			plugloadGroupDao.removeObject(PlugloadGroups.class, profileId);
			
			//Remove ProfileHandler
			plugloadGroupDao.removeObject(PlugloadProfileHandler.class, group.getPlugloadProfileHandler().getId());
			
			//Remove weekday profile
			plugloadGroupDao.removeObject(PlugloadProfile.class, morningProfile.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, dayProfile.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, eveningProfile.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, nightProfile.getId());
			
			//Remove weekend profile
			plugloadGroupDao.removeObject(PlugloadProfile.class, morningProfileWeekend.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, dayProfileWeekend.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, eveningProfileWeekend.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, nightProfileWeekend.getId());
			
			//Remove Holiday profile
			plugloadGroupDao.removeObject(PlugloadProfile.class, morningProfileHoliday.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, dayProfileHoliday.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, eveningProfileHoliday.getId());
			plugloadGroupDao.removeObject(PlugloadProfile.class, nightProfileHoliday.getId());
			
			if (weekDayList != null && !weekDayList.isEmpty())
		     {
	             Iterator<WeekdayPlugload> it = weekDayList.iterator();
	             while (it.hasNext()) {
	            	 WeekdayPlugload rowResult = it.next();
	            	 plugloadGroupDao.removeObject(WeekdayPlugload.class, rowResult.getId());
	             }
		     }
			
			//Remove ProfileConfiguration
			plugloadGroupDao.removeObject(PlugloadProfileConfiguration.class, profileHandler.getPlugloadProfileConfiguration().getId());
			
		}catch(Exception e){
			e.printStackTrace();
			status = 0;
		}
		
		
		return status;
	}
	
	public PlugloadGroups getPlugloadGroupByName(String sGroupname) {
        return plugloadGroupDao.getPlugloadGroupByName(sGroupname);
    }
	
	public PlugloadGroups getPlugloadGroupById(Long id) {
        return plugloadGroupDao.getPlugloadGroupById(id);
    }

	public PlugloadProfileHandler fetchPlugloadProfileHandlerById(Long id) {
		return plugloadProfileDao.fetchPlugloadProfileHandlerById(id);
	}

	public Short getMaxPlugloadProfileNo(Long tenantId) {
		
		return plugloadGroupDao.getMaxPlugloadProfileNo(tenantId);
	}
	
	public PlugloadGroups editName(PlugloadGroups groups) {
        return plugloadGroupDao.editName(groups);
    }
	
	public TreeNode<FacilityType> loadPlugloadProfileHierarchy(boolean visibilityCheck) {

  	TreeNode<FacilityType> plugloadProfileHierachy =null;
  	if(visibilityCheck)
  	{
  		plugloadProfileHierachy = plugloadGroupDao.loadPlugloadProfileHierarchy(visibilityCheck);
  	}
  	else
  	{
  		plugloadProfileHierachy = plugloadGroupDao.loadFilterPlugloadProfileHierarchy();
  	}
  	plugloadProfileTreeMap.put(0L, plugloadProfileHierachy);
    return plugloadProfileHierachy;
	}
	
	public List<Object> getPlugloadCountForPlugloadProfile(Long pid){
		return plugloadGroupDao.getPlugloadCountForPlugloadProfile(pid);
	}
	
	public List loadPlugloadList(Long pid, String selectedplugloads ,int offset, int limit,AssignPlugloadList returnList) {
		List result = null;
		result = plugloadGroupDao.loadPlugloadList(pid,selectedplugloads, offset, limit,returnList);
		return result;
	}
	
	
	public String updatePlugloadGroupVisibility(String[] selectedFacility){
		String  plugloadAssociatedProfiles ="";
		StringBuilder plugloadGroupVisibilityLog = new StringBuilder("::Plugload Profile Group Visibility::");
		StringBuilder plugloadTemplateVisibilityLog = new StringBuilder("::Plugload Profile Template Visibility::");
		for (String facility : selectedFacility) {
			String[] facilityDetail = facility.split("_");
			String facilityType = facilityDetail[0];
			Long facilityId = Long.parseLong(facilityDetail[1]);
			String visibilityStr = facilityDetail[2];
			boolean visibilityFlag = true;
			if("t".equalsIgnoreCase(visibilityStr))
			{
				visibilityFlag =true;
			}else if("f".equalsIgnoreCase(visibilityStr))
			{
				visibilityFlag =false;
			}
			if ("plugloadtemplate".equalsIgnoreCase(facilityType)) {			
				List<Object> list = plugloadProfileTemplateDao.getPlugloadCountForPlugloadProfileTemplate(facilityId);				
				if(!(visibilityFlag==false && list.size()>=1))	
				{
					plugloadProfileTemplateDao.updatePlugloadTemplateVisibility(facilityId, visibilityFlag);
					plugloadTemplateVisibilityLog.append("Plugload Template , name : " + plugloadProfileTemplateDao.getPlugloadProfileTemplateById(facilityId).getName() + ", id : " + facilityId + " Flag :"+visibilityFlag+"::");					
				}
			} else if ("plugloadgroup".equalsIgnoreCase(facilityType)) {
				List<Object> list = plugloadGroupDao.getPlugloadCountForPlugloadProfile(facilityId);
				PlugloadGroups plugloadGroups = plugloadGroupDao.getGroupById(facilityId);
				if(!(visibilityFlag==false && list.size()>=1))
				{
					plugloadGroupDao.updatePlugloadGroupVisibility(facilityId,visibilityFlag);
					plugloadGroupVisibilityLog.append(" Plugload Group , name : " + plugloadGroupDao.getGroupById(facilityId).getName() + " id : " + facilityId + " Flag :"+visibilityFlag+"::");					
				}
				else
				{
					//if(plugloadGroups.isDefaultProfile()==true)
					//{
						//plugloadAssociatedProfiles+=plugloadGroups.getName()+"_Default"+",";
					//}
					//else
					//{
						plugloadAssociatedProfiles+=plugloadGroups.getName()+",";
					//}
					//Reset all plugload profile template where plugload  is associated with plugload profile.
					plugloadProfileTemplateDao.updatePlugloadTemplateVisibility(plugloadGroups.getPlugloadProfileTemplate().getId(), true);
				}
			}
		}
		userAuditLoggerUtil.log("" + plugloadGroupVisibilityLog, UserAuditActionType.Plugload_Profile_Update.getName());
		userAuditLoggerUtil.log("" + plugloadTemplateVisibilityLog, UserAuditActionType.Plugload_Profile_Update.getName());
		plugloadAssociatedProfiles = plugloadAssociatedProfiles.replaceAll(",$", "");
		return plugloadAssociatedProfiles;
	}
	
}
