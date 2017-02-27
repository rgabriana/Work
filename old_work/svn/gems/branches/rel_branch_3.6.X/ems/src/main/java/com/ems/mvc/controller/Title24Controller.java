package com.ems.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.action.SpringContext;
import com.ems.model.Company;
import com.ems.model.DRTarget;
import com.ems.model.Gateway;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.SystemConfiguration;
import com.ems.model.Title24;
import com.ems.security.exception.EmsValidationException;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.DRTargetManager;
import com.ems.service.DRUserManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.LicenseSupportManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.PlanMapManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.Title24Manager;
import com.ems.service.UserLocationsManager;
import com.ems.service.UserManager;
import com.ems.types.DRStatusType;
import com.ems.types.DRType;
import com.ems.types.DrLevel;
import com.ems.types.NetworkType;

@Controller
@RequestMapping("/title24")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class Title24Controller {

	private static Logger syslog = Logger.getLogger("SysLog");
	
	@Resource(name = "companyManager")
	private CompanyManager companyManager;
	@Resource(name = "buildingManager")
	private BuildingManager buildingManager;
	@Resource(name = "campusManager")
	private CampusManager campusManager;
	@Resource(name = "floorManager")
	private FloorManager floorManager;
	
	@Resource(name = "drTargetManager")
    private DRTargetManager drTargetManager;
    @Resource(name = "drUserManager")
    private DRUserManager drUserManager;
    
	@Resource(name = "areaManager")
	private AreaManager areaManager;
	@Resource(name = "planMapManager")
	private PlanMapManager planMapManager;
    
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    
    @Resource(name = "facilityTreeManager")
    private FacilityTreeManager facilityTreeManager;
    
    @Autowired
	private MessageSource messageSource;
    
    @Resource(name = "userManager")
    private UserManager userManager;
    
    @Resource(name = "userLocationsManager")
    private UserLocationsManager userLocationsManager;
    
    @Resource(name = "licenseSupportManager")
    private LicenseSupportManager licenseSupportManager;
    
    @Resource
    private NetworkSettingsManager networkSettingsManager;
    
    @Resource 
    private Title24Manager title24Manager;
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@RequestMapping("/compliance.ems")
	public String tocomplaince(Model model) {

		syslog.info("COmplaince settings entered...");
		Integer pricingType = null;
        SystemConfigurationManager sysConfigManager = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    	SystemConfiguration pricingTypeConfig = sysConfigManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			pricingType = 1;
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{
    			pricingType = 2;
    		}
    	}
        //ENL - 4179 End
    	Company company = companyManager.getAllCompanies().get(0);
    	if(pricingType!=null) company.setPricingType(pricingType);
		model.addAttribute("company", company);
		model.addAttribute("mode", "admin");
		ArrayList<String> list = new ArrayList<String>();
		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if(nimBuilding != null && nimBuilding.getNetworkSettings()!= null && nimBuilding.getNetworkSettings().getName()!= null){
			buildingMapping = nimBuilding.getNetworkSettings().getName();			
		}else{
			buildingMapping = corporateMapping;
		}
		list.add(ServerMain.getInstance().getIpAddress(corporateMapping));
		list.add(ServerMain.getInstance().getIpAddress(buildingMapping));
		list.add(ServerMain.getInstance().getSubnetMask(corporateMapping));
		list.add(ServerMain.getInstance().getDefaultGateway());
		
		list.add(ServerMain.getInstance().getSubnetMask(buildingMapping));
		model.addAttribute("system", list);
		model.addAttribute("dhcpPresent", ServerMain.getInstance().determineDHCP(corporateMapping));
        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.addAttribute("dhcpEnable", dhcpConfig.getValue());
        }
        List<Gateway> gwList = gatewayManager.loadAllGateways();
        if(gwList.isEmpty())
    		model.addAttribute("gatewaysPresent", "false");
        else
    		model.addAttribute("gatewaysPresent", "true");
        
       	final Title24 title24  = title24Manager.loadTitle24Details();
       	//TODO For 3.6 first drop as per discussion in the call no data from db is shown to UI.. REMOVE FOLLOWING CODE IF BACKEND TO ENABLE
       	final ObjectMapper mapper = new ObjectMapper();
       	try {
//			Title24 t24tmp = mapper.readValue("{\"compliance\":{\"flag\":\"true\"}}", Title24.class);
//			t24tmp.getCompliance().setFlag(title24.getCompliance().getFlag());
//			model.addAttribute("title24", t24tmp);
       		model.addAttribute("title24", title24);
		} catch (Exception e) {
			syslog.info("Test json not able to retrieve...",e);
		} 
       	
		//model.addAttribute("title24", title24);
//		try {
//			final ObjectMapper mapper = new ObjectMapper();
//			final String jsonStr = mapper.writeValueAsString(title24);
//			syslog.debug("JSON::"+jsonStr);
//		} catch (Exception e) {
//			syslog.error("Exeption in logging the title24 json",e);
//		}
		return "title24/compliance";
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@RequestMapping("/compliancereport.ems")
	public String tocomplaincereport(Model model) {

		syslog.info("COmplaince settings entered...");
		Integer pricingType = null;
        SystemConfigurationManager sysConfigManager = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    	SystemConfiguration pricingTypeConfig = sysConfigManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			pricingType = 1;
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{
    			pricingType = 2;
    		}
    	}
        //ENL - 4179 End
    	Company company = companyManager.getAllCompanies().get(0);
    	if(pricingType!=null) company.setPricingType(pricingType);
		model.addAttribute("company", company);
		model.addAttribute("mode", "admin");
		ArrayList<String> list = new ArrayList<String>();
		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if(nimBuilding != null && nimBuilding.getNetworkSettings()!= null && nimBuilding.getNetworkSettings().getName()!= null){
			buildingMapping = nimBuilding.getNetworkSettings().getName();			
		}else{
			buildingMapping = corporateMapping;
		}
		list.add(ServerMain.getInstance().getIpAddress(corporateMapping));
		list.add(ServerMain.getInstance().getIpAddress(buildingMapping));
		list.add(ServerMain.getInstance().getSubnetMask(corporateMapping));
		list.add(ServerMain.getInstance().getDefaultGateway());
		
		list.add(ServerMain.getInstance().getSubnetMask(buildingMapping));
		model.addAttribute("system", list);
		model.addAttribute("dhcpPresent", ServerMain.getInstance().determineDHCP(corporateMapping));
        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.addAttribute("dhcpEnable", dhcpConfig.getValue());
        }
        List<Gateway> gwList = gatewayManager.loadAllGateways();
        if(gwList.isEmpty())
    		model.addAttribute("gatewaysPresent", "false");
        else
    		model.addAttribute("gatewaysPresent", "true");
        
       	final Title24 title24  = title24Manager.loadTitle24Details();
       	//TODO For 3.6 first drop as per discussion in the call no data from db is shown to UI.. REMOVE FOLLOWING CODE IF BACKEND TO ENABLE
       	final ObjectMapper mapper = new ObjectMapper();
       	try {
			//Title24 t24tmp = mapper.readValue("{\"compliance\":{\"flag\":\"true\"}}", Title24.class);
			//t24tmp.getCompliance().setFlag(title24.getCompliance().getFlag());
			model.addAttribute("title24", title24);
		} catch (Exception e) {
			syslog.info("Test json not able to retrieve...",e);
		} 
       	
		//model.addAttribute("title24", title24);
//		try {
//			final ObjectMapper mapper = new ObjectMapper();
//			final String jsonStr = mapper.writeValueAsString(title24);
//			syslog.debug("JSON::"+jsonStr);
//		} catch (Exception e) {
//			syslog.error("Exeption in logging the title24 json",e);
//		}
		return "title24/report1";
	}
	
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@RequestMapping("/complianceUpdate.ems")
	public String updateCompliance(@ModelAttribute("title24") Title24 title24) throws EmsValidationException{
		
		//Just print the title24 variable here..
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonStr = mapper.writeValueAsString(title24);
			syslog.debug(jsonStr);
		} catch (Exception e) {
			syslog.error("Exeption in logging the title24 json",e);
		}
		
		//Update the title24 string now
		try {
			title24Manager.saveTitle24Details(title24);
			if (!StringUtils.isEmpty(title24.getFormtype()) && "accordianDRControlSubmit".equals(title24.getFormtype())){
				if (title24.getDracceptance().getDrtest() != null && !title24.getDracceptance().getDrtest().isEmpty()){
					final String drtest = title24.getDracceptance().getDrtest().get(0);
					if (!StringUtils.isEmpty(drtest) && drtest.equalsIgnoreCase("1")){
						//Trigger DR Schedular
						//Check if the DR Identifier already exists in DB
						DRTarget drTarget = new DRTarget();
						drTarget.setPriceLevel(DrLevel.MODERATE.getName());
						drTarget.setDescription("Title24 Autotriggerd");
						drTarget.setPricing(1.0);
						drTarget.setDuration(30 * 60);
						drTarget.setStartTime(new Date());
						
						DRTarget drTargetinDB = drTargetManager.getDRTargetByDrIdentifier(drTarget.getDrIdentifier());
						if(drTargetinDB==null)
						{
							drTarget.setDrStatus(DRStatusType.Scheduled.getName());
							drTarget.setDrType(DRType.MANUAL.getName());
							if (drTarget.getId() == null) {   
								drTarget.setOptIn(true);
								drTarget.setJitter(0L);
								if(drTarget.getDrType().equals(DRType.MANUAL.getName())) {
									drTarget.setTargetReduction(drTargetManager.getPercentRedByDrLevel(drTarget.getPriceLevel()));
								}
								drTargetManager.saveOrUpdateDRTarget(drTarget);    	            
							}
							String startJobName = "DRStartJob" + drTarget.getId();
							String startTriggerName = "DRStartTr" + drTarget.getId();
							String endJobName = "DREndJob" + drTarget.getId();
							String endTriggerName = "DREndTr" + drTarget.getId();
							Date endDate = new Date();
							endDate.setTime(drTarget.getStartTime().getTime() + drTarget.getDuration() * 1000 + 2000);
							try {
								// Delete the older Quartz job and create a new one
								drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
								drTargetManager.scheduleOverrideJobs(startJobName, drTarget.getDrType(), startTriggerName, new Date(drTarget.getStartTime().getTime()));
							    DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
								drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
								drTargetManager.scheduleOverrideJobs(endJobName, drTarget.getDrType(), endTriggerName, endDate);
							} catch (SchedulerException e) {
								syslog.error("Exeption in triggering dr the title24 json",e);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			syslog.info("ERROR: Not able to update the title24 json objet", e);
		} 
		return "redirect:/title24/compliance.ems";
	}
}
