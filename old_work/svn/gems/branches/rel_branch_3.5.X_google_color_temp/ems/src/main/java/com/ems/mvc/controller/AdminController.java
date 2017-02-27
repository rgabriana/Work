package com.ems.mvc.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.action.SpringContext;
import com.ems.annotaion.InvalidateFacilityTreeCache;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.PlanMap;
import com.ems.model.SystemConfiguration;
import com.ems.model.Timezone;
import com.ems.model.User;
import com.ems.model.UserLocations;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.server.ServerMain;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.LicenseSupportManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.PlanMapManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserLocationsManager;
import com.ems.service.UserManager;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.CommonUtils;
import com.enlightedinc.licenseutil.LicenseUtil;
import com.enlightedinc.vo.Licenses;

/**
 * If you want to trigger cleaning of facility tree cache mark the method with
 * InvalidateFacilityTreeCache
 * 
 * @author lalit
 * 
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "companyManager")
	private CompanyManager companyManager;
	@Resource(name = "buildingManager")
	private BuildingManager buildingManager;
	@Resource(name = "campusManager")
	private CampusManager campusManager;
	@Resource(name = "floorManager")
	private FloorManager floorManager;
	@Resource(name = "areaManager")
	private AreaManager areaManager;
	@Resource(name = "planMapManager")
	private PlanMapManager planMapManager;
    @Resource
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
    
    //static final long floorPlanSizeLimit = 1048576;
    
	@RequestMapping("/organization/setup.ems")
	public String orgSetup(Model model, @RequestParam(value = "dhcpSettingStatus", required = false) String dhcpSettingStatus) throws EmsValidationException {
			
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "dhcpSettingStatus", dhcpSettingStatus);
		
        //ENL - 4179 Start
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
		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType("Corporate");
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType("Building");
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
        
        if(dhcpSettingStatus != null)
        {
        	model.addAttribute("dhcpSettingStatus", dhcpSettingStatus);
        }
        return "admin/organization/setup";
	}
	
	@RequestMapping("/organization/serverdatetimesetup.ems")
	public String serverdatetimesetup(Model model) {
		
		String timezone = null;
		model.addAttribute("timezone", companyManager.getTimezoneList());
		final List<Company> companies = companyManager.getAllCompanies();
		if(companies.size() < 1) {
			timezone = System.getProperty("user.timezone");
			List<Timezone> tzlist = companyManager.getTimezoneList();
			for(Timezone tz: tzlist) {
				if(tz.getName().equals(timezone)) {
					timezone = tz.getName();
				}
			}
		}
		else {
			timezone = companies.get(0).getTimeZone();
		}
		model.addAttribute("currenttimezone", timezone);
		if(companies != null && companies.size() > 0){
			final Company company = companies.get(0); 
			model.addAttribute("ntpFlag", company.getNtpEnable());
			model.addAttribute("ntpServers", company.getNtpServers());
		}
        return "admin/organization/changeserverdatetimedialog";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/companyUpdate.ems", method = RequestMethod.POST)
	public String updateCompany(@ModelAttribute("company") Company company) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("orgName", company.getName());
        nameValMap.put("orgAddr", company.getAddress());
        nameValMap.put("conEmail", company.getEmail());
        nameValMap.put("conPhone", company.getContact());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
        
		StringTokenizer st1 = new StringTokenizer(company.getValidDomain(), ",");
		while(st1.hasMoreTokens())
		{
			String vOrgDomain = st1.nextToken();
			CommonUtils.isParamValueAllowedAndThrowException(messageSource,systemConfigurationManager, "vOrgDomain", vOrgDomain);
		}
		
		
		
		company.setId(companyManager.getCompany().getId());
		companyManager.updateCompany(company);
		
		//ENL-4179 read the pricing type details
		/*Integer pricingType = company.getPricingType();
		if(pricingType != null)
		{
			SystemConfiguration pricingTypeConfig = systemConfigurationManager.loadConfigByName("enable.pricing");
			if(pricingTypeConfig!=null)
			{
				pricingTypeConfig.setValue(pricingType.toString());
				systemConfigurationManager.save(pricingTypeConfig);				
			}
		}*/
		
		//ENL-4179 end 
		
		userAuditLoggerUtil.log("Organization is updated.", UserAuditActionType.Organization_Update.getName());
		
		return "redirect:/admin/organization/setup.ems";
	}

	@RequestMapping("/organization/setting.ems")
	public String orgSetting(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Company comp = companyManager.loadCompany();
		model.addAttribute("mode", "admin");
		model.addAttribute("campuses",
				companyManager.laodCompanyWithCampus(comp.getId())
						.getCampuses());
		model.addAttribute("companyName", comp.getName());
		
		SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");

		switch (cookieHandler.getFaciltiyType()) {
		case COMPANY: {
			Campus campus = new Campus();
			model.addAttribute("page", "campus");
			model.addAttribute("campus", campus);
			model.addAttribute("org", comp);
			return "createSite";
		}
		case CAMPUS: {
			Building building = new Building();
			model.addAttribute("page", "building");
			model.addAttribute("building", building);
			model.addAttribute("org",
					campusManager.loadCampusById(cookieHandler.getFacilityId()));
			model.addAttribute("default_selected_campus",
					cookieHandler.getFacilityId());
			return "createSite";
		}
		case BUILDING: {
			Floor floor = new Floor();
			model.addAttribute("page", "floor");
			model.addAttribute("floor", floor);
			model.addAttribute("org", buildingManager
					.getBuildingById(cookieHandler.getFacilityId()));
			model.addAttribute("default_selected_campus", buildingManager
					.getBuildingById(cookieHandler.getFacilityId()).getCampus()
					.getId());
			model.addAttribute(
					"buildings",
					buildingManager.getAllBuildingsByCampusId(buildingManager
							.getBuildingById(cookieHandler.getFacilityId())
							.getCampus().getId()));
			model.addAttribute("default_selected_building",
					cookieHandler.getFacilityId());
			if(floorPlanImageSizelimitConfig != null){
				model.addAttribute("floorplan_imagesize_limit", floorPlanImageSizelimitConfig.getValue());
			}
			return "createSite";
		}
		case FLOOR: {
			try {
				model.addAttribute("page", "area");
				Area area = new Area();
				model.addAttribute("area", area);
				Floor floor = floorManager.getFloorById(cookieHandler
						.getFacilityId());
				model.addAttribute("fp_size", (long)floor.getByteImage().length);
				InputStream in = new ByteArrayInputStream(floor.getByteImage());
				ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
				try {
				    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
				    if (readers.hasNext()) {
				        ImageReader reader = readers.next();
				        try {
				            reader.setInput(imageInputStream);
				            model.addAttribute("fp_height",reader.getHeight(0));
				            model.addAttribute("fp_width",reader.getWidth(0));
				        } finally {
				            reader.dispose();
				        }
				    }
				} finally {
				    if (imageInputStream != null) imageInputStream.close();
				}
				
				model.addAttribute("zoneSensorLicenseEnabled", licenseSupportManager.isZoneSensorsEnabled());
								
				model.addAttribute("org", floor);
				model.addAttribute("floors", floorManager
						.getAllFloorsByBuildingId(floor.getBuilding().getId()));
				model.addAttribute("default_selected_campus", floor
						.getBuilding().getCampus().getId());
				model.addAttribute("default_selected_building", floor
						.getBuilding().getId());
				model.addAttribute("default_selected_floor",
						cookieHandler.getFacilityId());
				if(floorPlanImageSizelimitConfig != null){
					model.addAttribute("floorplan_imagesize_limit", floorPlanImageSizelimitConfig.getValue());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "createSite";
		}
		case AREA: {
			try {
				model.addAttribute("page", "just_area");
				Area area = areaManager.getAreaById(cookieHandler
						.getFacilityId());
				model.addAttribute("org", area);
				model.addAttribute("areas", areaManager
						.getAllAreasByFloorId(area.getFloor().getId()));
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			model.addAttribute("zoneSensorLicenseEnabled", licenseSupportManager.isZoneSensorsEnabled());
				
			return "createSite";

		}
		default: {
			return "createSite";
		}
		}

	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addCampus.ems", method = RequestMethod.POST)
	public String addCampus(@ModelAttribute("campus") Campus campus) throws EmsValidationException {
		
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("campusName", campus.getName());
        nameValMap.put("campusLocation", campus.getLocation());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		campusManager.addCampus(campus);
		userAuditLoggerUtil.log("Create campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		
		
		List<User> users = userManager.loadCompanyUsers(); // to load all users except admin user
		
		if(!ArgumentUtils.isNullOrEmpty(users)){
		for(User user : users){
			try {
				if(userLocationsManager.loadUserLocation(user.getId(),FacilityType.COMPANY,companyManager.loadCompany().getId()) != null){
					if(user.getRole().getRoleType() == RoleType.FacilitiesAdmin){
						UserLocations userLocations = new UserLocations();
						userLocations.setApprovedLocationType(FacilityType.CAMPUS);
						userLocations.setLocationId(campus.getId());
						userLocations.setUser(user);
						userManager.saveUserLocation(userLocations);
					}else{
						facilityTreeManager.deleteParents(FacilityType.CAMPUS,user.getId(),campus.getId());
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateCampus.ems", method = RequestMethod.POST)
	public String updateCampus(@ModelAttribute("campus") Campus campus) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("campusName", campus.getName());
        nameValMap.put("campusLocation", campus.getLocation());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		campusManager.updateCampus(campus);
		userAuditLoggerUtil.log("Update campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addBuilding.ems", method = RequestMethod.POST)
	public String addBuilding(@ModelAttribute("building") Building building) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "buildingName", building.getName());
		Building savedBuilding = buildingManager.createBuilding(building);
		userAuditLoggerUtil.log("Create building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());
		
		List<User> users = userManager.loadCompanyUsers(); // to load all users except admin user
		
		if(!ArgumentUtils.isNullOrEmpty(users)){
		for(User user : users){
			try {
				if(userLocationsManager.loadUserLocation(user.getId(),FacilityType.CAMPUS,savedBuilding.getCampus().getId()) != null){
					if(user.getRole().getRoleType() == RoleType.FacilitiesAdmin){
						UserLocations userLocations = new UserLocations();
						userLocations.setApprovedLocationType(FacilityType.BUILDING);
						userLocations.setLocationId(savedBuilding.getId());
						userLocations.setUser(user);
						userManager.saveUserLocation(userLocations);
					}else{
						facilityTreeManager.deleteParents(FacilityType.BUILDING,user.getId(),savedBuilding.getId());
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateBuilding.ems", method = RequestMethod.POST)
	public String updateBuilding(@ModelAttribute("building") Building building) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "buildingName", building.getName());
		buildingManager.editName(building);
		userAuditLoggerUtil.log("Update building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addFloor.ems", method = RequestMethod.POST)
	public String addFloor(@ModelAttribute("floor") Floor floor) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("floorName", floor.getName());
        nameValMap.put("floorDesc", floor.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		if (!floor.getPlanMap().getFileData().isEmpty()) {
			SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
			if ((double)floor.getPlanMap().getFileData().getSize() > Double.parseDouble(floorPlanImageSizelimitConfig.getValue())*1024*1024){
				return "redirect:/admin/organization/setting.ems?upload=false&refreshTree=true";
			}
		}

		Floor savedFloor = floorManager.createFloor(floor);
		userAuditLoggerUtil.log("Create floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());
		
		
		List<User> users = userManager.loadCompanyUsers(); // to load all users except admin user
		
		if(!ArgumentUtils.isNullOrEmpty(users)){
		for(User user : users){
			try {
				if(userLocationsManager.loadUserLocation(user.getId(),FacilityType.BUILDING,savedFloor.getBuilding().getId()) != null){
					if(user.getRole().getRoleType() == RoleType.FacilitiesAdmin){
						UserLocations userLocations = new UserLocations();
						userLocations.setApprovedLocationType(FacilityType.FLOOR);
						userLocations.setLocationId(savedFloor.getId());
						userLocations.setUser(user);
						userManager.saveUserLocation(userLocations);
					}else{
						facilityTreeManager.deleteParents(FacilityType.FLOOR,user.getId(),savedFloor.getId());
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateFloor.ems", method = RequestMethod.POST)
	public String updateFloor(@ModelAttribute("floor") Floor floor) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("floorName", floor.getName());
        nameValMap.put("floorDesc", floor.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		
		if (!floor.getPlanMap().getFileData().isEmpty()) {
			if ((double)floor.getPlanMap().getFileData().getSize() > Double.parseDouble(floorPlanImageSizelimitConfig.getValue())*1024*1024){
				return "redirect:/admin/organization/setting.ems?upload=false&refreshTree=true";
			}
		}
		
		floorManager.updateFloor(floor);
		userAuditLoggerUtil.log("Update floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addArea.ems", method = RequestMethod.POST)
	public String addArea(@ModelAttribute("area") Area area) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("areaName", area.getName());
        nameValMap.put("areaDesc", area.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		Area savedArea = null;
		try {
			area.setProfileHandler(floorManager.getFloorById(
					area.getFloor().getId()).getProfileHandler());
			savedArea = areaManager.save(area);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		userAuditLoggerUtil.log("Create area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());
		
		
		
		List<User> users = userManager.loadCompanyUsers(); // to load all users except admin user
		
		if(!ArgumentUtils.isNullOrEmpty(users)){
		for(User user : users){
			try {
				if(userLocationsManager.loadUserLocation(user.getId(),FacilityType.FLOOR,savedArea.getFloor().getId()) != null){
					if(user.getRole().getRoleType() == RoleType.FacilitiesAdmin){
						UserLocations userLocations = new UserLocations();
						userLocations.setApprovedLocationType(FacilityType.AREA);
						userLocations.setLocationId(savedArea.getId());
						userLocations.setUser(user);
						userManager.saveUserLocation(userLocations);
					}else{
						facilityTreeManager.deleteParents(FacilityType.AREA,user.getId(),savedArea.getId());
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateArea.ems", method = RequestMethod.POST)
	public String updateArea(@ModelAttribute("area") Area area) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("areaName", area.getName());
        nameValMap.put("areaDesc", area.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		areaManager.updateArea(area);
		userAuditLoggerUtil.log("Update area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editCompanyName.ems", method = RequestMethod.POST)
	public String editCompanyName(@ModelAttribute("org") Company company) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "orgName", company.getName());
		Company savedCompany = companyManager.loadCompanyById(company.getId());
		String oldName = savedCompany.getName();
		savedCompany.setName(company.getName());
		companyManager.update(savedCompany);
		userAuditLoggerUtil.log("Edit organization name from  " + oldName + " to " + company.getName() +"(" + company.getId() +")", UserAuditActionType.Organization_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editCampusName.ems", method = RequestMethod.POST)
	public String editCampusName(@ModelAttribute("org") Campus campus) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "campusName", campus.getName());
		Campus savedCampus = campusManager.loadCampusById(campus.getId());
		String oldName = savedCampus.getName();
		savedCampus.setName(campus.getName());
		campusManager.update(savedCampus);
		userAuditLoggerUtil.log("Edit campus name from " + oldName + " to "  + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editBuildingName.ems", method = RequestMethod.POST)
	public String editBuildingName(@ModelAttribute("org") Building building) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "buildingName", building.getName());
		Building savedBuilding = buildingManager.getBuildingById(building
				.getId());
		String oldName = savedBuilding.getName();
		savedBuilding.setName(building.getName());
		buildingManager.update(savedBuilding);
		userAuditLoggerUtil.log("Edit building name from " + oldName + " to " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editFloorName.ems", method = RequestMethod.POST)
	public String editFloorName(@ModelAttribute("org") Floor floor) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "floorName", floor.getName());
		Floor savedFloor = floorManager.getFloorusingId(floor.getId());
		String oldName = savedFloor.getName();
		savedFloor.setName(floor.getName());
		
		try {
			if (!floor.getPlanMap().getFileData().isEmpty()) {
				
				SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
				
				if ((double)floor.getPlanMap().getFileData().getSize() > Double.parseDouble(floorPlanImageSizelimitConfig.getValue())*1024*1024){
					return "redirect:/admin/organization/setting.ems?upload=false&refreshTree=true";
				}
				
				PlanMap planMap = null;
				if (savedFloor.getPlanMap() != null) {
					savedFloor.setFloorPlanUrl(floor.getPlanMap().getFileData()
							.getOriginalFilename());
					planMap = planMapManager.loadPlanMapById(savedFloor
							.getPlanMap().getId());
					planMap.setPlan(floor.getPlanMap().getFileData()
							.getBytes());
					planMapManager.update(planMap);
				} else {
					planMap = new PlanMap();
					planMap.setPlan(floor.getPlanMap().getFileData()
							.getBytes());
					planMapManager.save(planMap);
					savedFloor.setPlanMap(planMap);
					savedFloor.setFloorPlanUrl(floor.getPlanMap().getFileData()
							.getOriginalFilename());
				}
				savedFloor.setUploadedOn(new Date());
			}

			floorManager.update(savedFloor);
			if (!floor.getPlanMap().getFileData().isEmpty()) {
				userAuditLoggerUtil.log("Edit floor plan and floor name from " + oldName + " to " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());
			}
			else {
				userAuditLoggerUtil.log("Edit floor name from " + oldName + " to " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());
			}
			
		} catch (Exception e) {

		}
		if (!floor.getPlanMap().getFileData().isEmpty()) {
			return "redirect:/admin/organization/setting.ems?upload=true&refreshTree=true";
		} else {
			return "redirect:/admin/organization/setting.ems?refreshTree=true";
		}

	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editAreaName.ems", method = RequestMethod.POST)
	public String editAreaName(@ModelAttribute("org") Area area) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "areaName", area.getName());
		Area savedArea = areaManager.getAreaUsingId(area.getId());
		String oldName = savedArea.getName();
		try {
			areaManager.updateArea(area);
		} catch (Exception e) {

		}
		userAuditLoggerUtil.log("Edit area name from " + oldName + " to " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	/**
	 * Called up from facilities Install tab
	 * 
	 * @return titles template definition to display installDevice page
	 */
	@RequestMapping(value = "/organization/installdevices.ems", method = RequestMethod.GET)
	public String installDevices(Model model) {
		SystemConfiguration enablePlugloadProfileFeature = systemConfigurationManager.loadConfigByName("enable.plugloadprofilefeature");
        if (enablePlugloadProfileFeature != null && "true".equalsIgnoreCase(enablePlugloadProfileFeature.getValue())) {
            model.addAttribute("enablePlugloadFeature", enablePlugloadProfileFeature.getValue());
        }else{
         	 model.addAttribute("enablePlugloadFeature", false);
       }
		return "devices/install";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/delete/refresh.ems")
	public String refreshTree() {
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}
    @RequestMapping(value = "/dhcp_setup/save.ems")
    String saveDhcpSetting(String dhcp) {	

    	boolean retVal = false;
    		
		if(dhcp.contentEquals("true"))
			retVal = ServerMain.getInstance().startDHCP();
    	else
			retVal = ServerMain.getInstance().stopDHCP();

		if(retVal) {
            SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
            if (dhcpConfig != null) {
            	dhcpConfig.setValue(dhcp);
                systemConfigurationManager.update(dhcpConfig);
        		if(dhcp.contentEquals("true"))
        			userAuditLoggerUtil.log("DHCP server turned on", UserAuditActionType.DHCP_Update.getName());
        		else
        			userAuditLoggerUtil.log("DHCP server turned off", UserAuditActionType.DHCP_Update.getName());
            }
            
            SSLSessionManager.getInstance().initNwInterface();

            return "redirect:/admin/organization/setup.ems?dhcpSettingStatus=success";
		}
		else
		    return "redirect:/admin/organization/setup.ems?dhcpSettingStatus=failure";

    }    
}
