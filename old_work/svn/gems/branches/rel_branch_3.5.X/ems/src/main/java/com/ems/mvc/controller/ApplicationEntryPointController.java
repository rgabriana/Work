package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ems.action.SpringContext;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.Pricing;
import com.ems.model.SystemConfiguration;
import com.ems.model.Timezone;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.mvc.validator.ENLValidator;
import com.ems.security.EmsAuthenticationContext;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.PricingManager;
import com.ems.service.ProfileManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.Constants;
import com.ems.utils.AdminUtil;
import com.ems.utils.CommonUtils;

@Controller
public class ApplicationEntryPointController {
	
	  static final Logger logger = Logger.getLogger("ApplicationEntryPointController");
	  
	  //static final long floorPlanSizeLimit = 1048576;
	  
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "companyManager")
	private CompanyManager companyManager;
	@Resource(name = "campusManager")
	private CampusManager campusManager;
	@Resource (name = "buildingManager")
	private BuildingManager buildingManager;
	@Resource (name = "floorManager")
	private FloorManager floorManager;
	@Resource (name = "areaManager")
	private AreaManager areaManager;
	@Autowired
	private MessageSource messageSource;
	@Resource
	UserManager userManager;
	@Resource(name = "emsAuthContext")
	    private EmsAuthenticationContext emsAuthContext;
    @Resource
    private SystemConfigurationManager systemConfigurationManager;
    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
	
    @Resource (name="passwordValidator")
    private ENLValidator passwordValidator;
    
    @Resource (name= "pricingManager")
	private PricingManager pricingManager;
    
	@RequestMapping(value = "/createCompany.ems",  method = RequestMethod.POST)
	public String createCompany(@ModelAttribute("company") Company company) throws EmsValidationException {

		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("orgName", company.getName());
        nameValMap.put("orgAddr", company.getAddress());
        nameValMap.put("conEmail", company.getEmail());
        nameValMap.put("conPhone", company.getContact());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
        if(company.getValidDomain()!=null)
        {
        	StringTokenizer st1 = new StringTokenizer(company.getValidDomain(), ",");
			while(st1.hasMoreTokens())
			{
				String vOrgDomain = st1.nextToken();
				CommonUtils.isParamValueAllowedAndThrowException(messageSource,systemConfigurationManager, "vOrgDomain", vOrgDomain);
			}
        }
		company.setId(1L);
		companyManager.registerCompany(company);
		
		//create canned profile
		  try {
			  ProfileManager profileManager = (ProfileManager) SpringContext
	                    .getBean("profileManager");

				SystemConfiguration cannedProfileUpgradeEnableConfig = systemConfigurationManager
						.loadConfigByName("cannedprofile.enable");			
				if (cannedProfileUpgradeEnableConfig != null) {
					System.out.println(cannedProfileUpgradeEnableConfig
							.getValue());
					if ("0".equalsIgnoreCase(cannedProfileUpgradeEnableConfig
							.getValue())) {
						profileManager.createCannedProfiles();							//Create the canned profiles once creation of default profile groups is complete.
					}
					else if("1".equalsIgnoreCase(cannedProfileUpgradeEnableConfig.getValue()))
					{
						System.out.println("Canned profile creation has been completed in previous cycles.");
					}
				}else {
					System.out.println("cannedprofileupgrade column not found");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block4
				e.printStackTrace();
				System.out.println(e.getMessage());
			}        
		  
		userAuditLoggerUtil.log("Update organization: " + company.getName() +"(" + company.getId() +")", UserAuditActionType.Organization_Update.getName());
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "createCampus.ems")
	public ModelAndView createCampus() {
		Company comp = companyManager.loadCompany();
		Campus campus = new Campus();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "campus");
		mvc.addObject("campus", campus);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses",companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
        return mvc;
	}

	
	@RequestMapping(value = "/home.ems")
	public String entryPoint(HttpServletRequest request, HttpServletResponse response) {
	   Long userId = emsAuthContext.getUserId();
	   User user = userManager.loadUserById(userId);
	   boolean apacheInstalled = false;
	   boolean isForgotPasswordLoginRequest = false;
	   final boolean isExternalUser = systemConfigurationManager.isExternalUser(user);
	   isForgotPasswordLoginRequest = !StringUtils.isEmpty(user.getForgotPasswordIdentifier()) && (!isExternalUser); 
	   final Object forgotReqAttr = request.getSession().getAttribute(Constants.FORGOT_REQ_ATTRIBUTE);
	   request.getSession().removeAttribute(Constants.FORGOT_REQ_ATTRIBUTE);
	   //isForgotPasswordLoginRequest = isForgotPasswordLoginRequest && forgotReqAttr!= null && (Boolean)forgotReqAttr;
	   final String forgotPassUrl =  "redirect:/users/"+userId+"/forgotPassword.ems";
	   SystemConfiguration systemConfiguration =  systemConfigurationManager.loadConfigByName("em.forcepasswordexpiry");
	   if(!isExternalUser && systemConfiguration != null && "true".equalsIgnoreCase(systemConfiguration.getValue())){
		   Date d1 = user.getPasswordChangedAt();			
			if(d1!= null){
				Date d2 = Calendar.getInstance().getTime();
				if(((d2.getTime() - d1.getTime())/(24 * 60 * 60 * 1000)) > 180){
					return "users/changepasswordonexpiry";
				}
			}
	   }	   
	   if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin) {
		   if(isForgotPasswordLoginRequest){
			   return forgotPassUrl;//"redirect:/forgotpassword/resetPassword.ems";
		   }
		   ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		   HttpSession session = attr.getRequest().getSession(true);
		   if(session.getAttribute("apacheInstalled") == null) {
			   BufferedReader br = null;
			   StringBuffer buf = new StringBuffer();
				try {
					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec("apache2ctl -v");
					pr.waitFor();
					String line = "";

					br = new BufferedReader(new InputStreamReader(
							pr.getInputStream()));
					while (true) {
						line = br.readLine();
						if(line == null) {
							break;
						}
						buf.append(line);
					}
					if(buf.toString().indexOf("Apache/2") != -1) {
						apacheInstalled = true;
					}
					
				} catch (IOException e) {
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}


			   if(apacheInstalled) {
				   session.setAttribute("apacheInstalled", true);
			   }
			   else {
					if(session.getAttribute("securityKey") == null) {
						Random random = new Random();
						session.setAttribute("securityKey", ((Long)random.nextLong()).toString().substring(0, 15));
					}
			   }
		   }
	   }else{
		   if(isForgotPasswordLoginRequest){
			   //return "redirect:/forgotpassword/home.ems";
			   return forgotPassUrl;//"redirect:/users/"+userId+"/forgotPassword.ems";
		   }
	   }
		
		//Check for Restore backup if any backup file present in USB mounted
	   if(companyManager.getAllCompanies().size() < 1 || companyManager.getAllCompanies().get(0).getCompletionStatus() < 3)
	   {
		   ArrayList<String> usbListing = new ArrayList<String>();
		   usbListing = AdminUtil.getMountedUsbSticks();
		   if (usbListing != null && usbListing.size() > 0) 
		   {
				return "redirect:/restorebackup/promptRestoreBackupPage.ems";
		   }
	   }//END of RESTORE BACKUP
	   
	   if (!user.getTermConditionAccepted()) {
		 return "redirect:termAndConditions.ems";
        }
	   if(companyManager.getAllCompanies().size() < 1 || companyManager.getAllCompanies().get(0).getCompletionStatus() < 3) {
			return "redirect:companySetup.ems";
		} else {
			return "redirect:facilities/home.ems";
		}
	}
	
	@RequestMapping(value = "termAndConditions.ems")
	public String termAndConditions() {
		return "termAndConditions";
	}
	
	@RequestMapping(value = "acceptTermsAndConditions.ems")
	public String acceptTermAndConditions() {
		Long userId = emsAuthContext.getUserId();
		User user = userManager.loadUserById(userId);
		if(user!=null)
		{
			user.setTermConditionAccepted(true);
			userManager.update(user);
		}
		return "redirect:companySetup.ems";
	}
	
	@RequestMapping(value = "companySetup.ems")
	public ModelAndView companySetup() {
		ModelAndView modelAndView = null; new ModelAndView("companySetup");
		if(companyManager.getAllCompanies().size() < 1) {
			modelAndView = new ModelAndView("companySetup");
			Company company = new Company();
			String timezone = System.getProperty("user.timezone");
			List<Timezone> tzlist = companyManager.getTimezoneList();
			for(Timezone tz: tzlist) {
				if(tz.getName().equals(timezone)) {
					company.setTimeZone(tz.getName());
				}
			}
			modelAndView.addObject("company", company);
			modelAndView.addObject("companySetup", "true");
	        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
	        if (dhcpConfig != null) {
	        	modelAndView.addObject("dhcpEnable", dhcpConfig.getValue());
	        }
	        List<Gateway> gwList = gatewayManager.loadAllGateways();
	        if(gwList.isEmpty())
	        	modelAndView.addObject("gatewaysPresent", "false");
	        else
	        	modelAndView.addObject("gatewaysPresent", "true");
	        
		}else if(companyManager.getAllCompanies().get(0).getCompletionStatus()==1) {
			modelAndView = new ModelAndView("companySetup");
			modelAndView.addObject("company", companyManager.getAllCompanies().get(0));
			modelAndView.addObject("companySetup", "true");
			modelAndView.addObject("timezone", companyManager.getTimezoneList());
		}
		else  if(companyManager.getAllCompanies().get(0).getCompletionStatus() < 3){
			//TODO should be profiles page
			modelAndView = new ModelAndView(new RedirectView("facilities/home.ems"));
		}
		else {
			modelAndView = new ModelAndView(new RedirectView("facilities/home.ems"));
		}
        return modelAndView;
	}
	
	@RequestMapping(value = "addCampus.ems", method = RequestMethod.POST)
	public String addCampus(@ModelAttribute("campus") Campus campus) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("campusName", campus.getName());
        if(!StringUtils.isEmpty(campus.getLocation())){
        	nameValMap.put("campusLocation", campus.getLocation());
        }
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		campusManager.addCampus(campus);
		userAuditLoggerUtil.log("Create campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());		
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "createBuilding.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createBuilding() {
		Company comp = companyManager.loadCompany();
		Building building = new Building();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "building");
		mvc.addObject("building", building);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses", companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		mvc.addObject("buildingcount", buildingManager.getBuildingCount());
		return mvc;
	}
	
	@RequestMapping(value = "addBuilding.ems", method = RequestMethod.POST)
	public String addBuilding(@ModelAttribute("building") Building building) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "buildingName", building.getName());
		buildingManager.createBuilding(building);
		userAuditLoggerUtil.log("Create building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());		
		return "redirect:createBuilding.ems?default_selected_campus="+building.getCampus().getId();
	}
	
    public void setCompanyManager(CompanyManager companyManager) {
        this.companyManager = companyManager;
    }
    
	@RequestMapping(value = "createFloor.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createFloor() {
		Company comp = companyManager.loadCompany();
		Floor floor= new Floor();
		SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "floor");
		mvc.addObject("floor", floor);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses", companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		mvc.addObject("floorcount", floorManager.getFloorCount());
		mvc.addObject("floorplan_imagesize_limit", floorPlanImageSizelimitConfig.getValue());
		return mvc;
	}
	
	@RequestMapping(value = "addFloor.ems", method = RequestMethod.POST)
	public String addFloor(@ModelAttribute("floor") Floor floor) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("floorName", floor.getName());
        nameValMap.put("floorDesc", floor.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		if (!floor.getPlanMap().getFileData().isEmpty()) {
			if ((double)floor.getPlanMap().getFileData().getSize() > Double.parseDouble(floorPlanImageSizelimitConfig.getValue())*1024*1024){
				return "redirect:createFloor.ems?default_selected_campus="
				+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId()+"&upload=false";
			}
		}
		
		floorManager.createFloor(floor);
		userAuditLoggerUtil.log("Create floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());		
		return "redirect:createFloor.ems?default_selected_campus="
		+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId();
	}
	
	@RequestMapping(value = "finishSetup.ems", method = RequestMethod.POST)
	public String finishSetup() {
		companyManager.completeSetup();
		userAuditLoggerUtil.log("Finish organization setup", UserAuditActionType.Organization_Update.getName());		
		return "redirect:facilities/home.ems";
	}
	
	@RequestMapping(value = "updateCampus.ems", method = RequestMethod.POST)
	public String updateCampus(@ModelAttribute("campus") Campus campus) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("campusName", campus.getName());
        nameValMap.put("campusLocation", campus.getLocation());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		campusManager.updateCampus(campus);
		userAuditLoggerUtil.log("Update campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());	
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "updateBuilding.ems", method = RequestMethod.POST)
	public String updateBuilding(@ModelAttribute("building") Building building) throws EmsValidationException {
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, "buildingName", building.getName());
		buildingManager.editName(building);
		userAuditLoggerUtil.log("Update building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());	
		return "redirect:createBuilding.ems?default_selected_campus="+building.getCampus().getId();
	}
	
	@RequestMapping(value = "updateFloor.ems", method = RequestMethod.POST)
	public String updateFloor(@ModelAttribute("floor") Floor floor) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("floorName", floor.getName());
        nameValMap.put("floorDesc", floor.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		SystemConfiguration floorPlanImageSizelimitConfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		if (!floor.getPlanMap().getFileData().isEmpty()) {
			if ((double)floor.getPlanMap().getFileData().getSize() > Double.parseDouble(floorPlanImageSizelimitConfig.getValue())*1024*1024 ){
				return "redirect:createFloor.ems?default_selected_campus="
				+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId()+"&upload=false";
			}
		}
		
		floorManager.updateFloor(floor);
		userAuditLoggerUtil.log("Update floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());	
		return "redirect:createFloor.ems?default_selected_campus="
		+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId();
	}
	
	@RequestMapping(value = "createArea.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createArea() {
		Company comp = companyManager.loadCompany();
		Area area = new Area();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "area");
		mvc.addObject("area", area);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses", companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		return mvc;
	}
	
	@RequestMapping(value = "addArea.ems", method = RequestMethod.POST)
	public String addArea(@ModelAttribute("area") Area area) throws EmsValidationException {
		try {
			Map<String,Object> nameValMap = new HashMap<String, Object>();
	        nameValMap.put("areaName", area.getName());
	        nameValMap.put("areaDesc", area.getDescription());
	        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
			area.setProfileHandler(floorManager.getFloorById(area.getFloor().getId()).getProfileHandler());
			areaManager.save(area);
			userAuditLoggerUtil.log("Create area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());	
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:createArea.ems?default_selected_campus="
		+ area.getFloor().getBuilding().getCampus().getId() + 
		"&default_selected_building=" + area.getFloor().getBuilding().getId() +
		"&default_selected_floor=" + area.getFloor().getId();
	}
	
	@RequestMapping(value = "updateArea.ems", method = RequestMethod.POST)
	public String updateArea(@ModelAttribute("area") Area area) throws EmsValidationException {
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("areaName", area.getName());
        nameValMap.put("areaDesc", area.getDescription());
        CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		areaManager.updateArea(area);
		userAuditLoggerUtil.log("Update area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());	
		return "redirect:createArea.ems?default_selected_campus="
		+ area.getFloor().getBuilding().getCampus().getId() + 
		"&default_selected_building=" + area.getFloor().getBuilding().getId() +
		"&default_selected_floor=" + area.getFloor().getId();
	}
	 
    @RequestMapping(value = "deleteArea.ems")
    public String deleteAreaRefresh(@RequestParam("cid") Long cid, @RequestParam("bid") Long bid, @RequestParam("fid") Long fid) {
        return "redirect:createArea.ems?default_selected_campus="
		+ cid + "&default_selected_building=" + bid + "&default_selected_floor=" + fid;
    }
    
    @RequestMapping(value = "deleteFloor.ems")
    public String deleteFloorRefresh(@RequestParam("cid") Long cid, @RequestParam("bid") Long bid) {
        return "redirect:createFloor.ems?default_selected_campus="
		+ cid + "&default_selected_building=" + bid;
    }
    
    @RequestMapping(value = "deleteBuilding.ems")
    public String deleteBuildingRefresh(@RequestParam("cid") Long cid) {
        return "redirect:createBuilding.ems?default_selected_campus="+ cid;
    }
    
    @RequestMapping(value = "deleteCampus.ems")
    public String deleteCampusRefresh() {
        return "redirect:createCampus.ems";
    }
    
    @RequestMapping("/changepasswordsuccess.ems")
    public String changePasswordSuccess(Model model) {
        return "redirect:facilities/home.ems";
    }
    
    @RequestMapping(value = "createPricing.ems", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView listPricing(Model model) {
		List<Pricing> pricingList = pricingManager.getPricingList();
		List<Pricing> weekDayPricingList = new ArrayList<Pricing>();
		List<Pricing> weekEndPricingList = new ArrayList<Pricing>();
		for (Pricing pricing : pricingList) {
			if (pricing.getDayType().equals(Constants.WEEK_DAY)) {
				weekDayPricingList.add(pricing);
			}
			if (pricing.getDayType().equals(Constants.WEEK_END)) {
				weekEndPricingList.add(pricing);		
			}
		}
		ModelAndView mvc = new ModelAndView("createPricing");
		mvc.addObject("weekdays", weekDayPricingList);
		mvc.addObject("weekends", weekEndPricingList);
		mvc.addObject("companySetup", "true");
		
		Float defaultPrice = companyManager.getCompany().getPrice();
		
		model.addAttribute("defaultPrice", defaultPrice);
		
		String[] currencyArray = {"AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BOV","BRL","BSD","BTN","BWP","BYR","BZD","CAD","CDF","CHE","CHF","CHW","CLF","CLP","CNY","COP","COU","CRC","CUP","CVE","CYP","CZK","DJF","DKK","DOP","DZD","EEK","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL","HRK","HTG","HUF","IDR","ILS","INR","IQD","IRR","ISK","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT","LAK","LBP","LKR","LRD","LSL","LTL","LVL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRO","MTL","MUR","MVR","MWK","MXN","MXV","MYR","MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF","SAR","SBD","SCR","SDG","SEK","SGD","SHP","SKK","SLL","SOS","SRD","STD","SYP","SZL","THB","TJS","TMM","TND","TOP","TRY","TTD","TWD","TZS","UAH","UGX","USD","USN","USS","UYU","UZS","VEB","VND","VUV","WST","XAF","XAG","XAU","XBA","XBB","XBC","XBD","XCD","XDR","XFO","XFU","XOF","XPD","XPF","XPT","XTS","XXX","YER","ZAR","ZMK","ZWD"};
    	model.addAttribute("currencyArray", currencyArray);
		
		SystemConfiguration pricingTypeConfig = systemConfigurationManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			mvc.addObject("pricingType","1");
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{    			
    			mvc.addObject("pricingType","2");
    		}
    	}
    	
    	SystemConfiguration pricingCurrencyConfig = systemConfigurationManager.loadConfigByName("pricing.currency");
		if(pricingCurrencyConfig!=null)
		{
			mvc.addObject("currencyType",pricingCurrencyConfig.getValue());			
		}
    	
		return mvc;
    }
}
