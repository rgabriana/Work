package com.ems.mvc.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.Timezone;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.FloorManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;

@Controller
public class ApplicationEntryPointController {
	
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
	
	@Resource
	UserManager userManager;
	@Resource(name = "emsAuthContext")
	    private EmsAuthenticationContext emsAuthContext;
	
	@RequestMapping(value = "/createCompany.ems",  method = RequestMethod.POST)
	public String createCompany(@RequestParam("newPassword") String password,@RequestParam("changeTime") String changeTime, @RequestParam("gemstime") String gemstime, @ModelAttribute("company") Company company) {
		if(!companyManager.getAllCompanies().isEmpty()) {
			company.setId(1L);
		}
		companyManager.registerCompany(company, password);
		if("true".equals(changeTime)) {
			companyManager.setServerTime(gemstime);
		}
		userAuditLoggerUtil.log("Update company: " + company.getName() +"(" + company.getId() +")", UserAuditActionType.Company_Update.getName());
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "createCampus.ems")
	public ModelAndView createCampus() {
		Company comp = companyManager.loadCompanyById(1L);
		Campus campus = new Campus();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "campus");
		mvc.addObject("campus", campus);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses",companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		return mvc;
	}

	@RequestMapping(value = "/home.ems")
	public String entryPoint() {
	   Long userId = emsAuthContext.getUserId();
	   User user = userManager.loadUserById(userId);
	   
	   if (emsAuthContext.getCurrentUserRoleType() == RoleType.Admin) {
		   ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		   HttpSession session = attr.getRequest().getSession(true);
			if(session.getAttribute("securityKey") == null) {
				Random random = new Random();
				session.setAttribute("securityKey", ((Long)random.nextLong()).toString().substring(0, 15));
			}
			//System.out.println(session.getAttribute("securityKey"));
	   }
		
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
			modelAndView.addObject("timezone", companyManager.getTimezoneList());
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
	public String addCampus(@ModelAttribute("campus") Campus campus) {
		campusManager.addCampus(campus);
		userAuditLoggerUtil.log("Create campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());		
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "createBuilding.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createBuilding() {
		Company comp = companyManager.loadCompanyById(1L);
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
	public String addBuilding(@ModelAttribute("building") Building building) {
		buildingManager.createBuilding(building);
		userAuditLoggerUtil.log("Create building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());		
		return "redirect:createBuilding.ems?default_selected_campus="+building.getCampus().getId();
	}
	
    public void setCompanyManager(CompanyManager companyManager) {
        this.companyManager = companyManager;
    }
    
	@RequestMapping(value = "createFloor.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createFloor() {
		Company comp = companyManager.loadCompanyById(1L);
		Floor floor= new Floor();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "floor");
		mvc.addObject("floor", floor);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses", companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		mvc.addObject("floorcount", floorManager.getFloorCount());
		return mvc;
	}
	
	@RequestMapping(value = "addFloor.ems", method = RequestMethod.POST)
	public String addFloor(@ModelAttribute("floor") Floor floor) {
		floorManager.createFloor(floor);
		userAuditLoggerUtil.log("Create floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());		
		return "redirect:createFloor.ems?default_selected_campus="
		+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId();
	}
	
	@RequestMapping(value = "finishSetup.ems", method = RequestMethod.POST)
	public String finishSetup() {
		companyManager.completeSetup();
		userAuditLoggerUtil.log("Finish company setup", UserAuditActionType.Company_Update.getName());		
		return "redirect:facilities/home.ems";
	}
	
	@RequestMapping(value = "updateCampus.ems", method = RequestMethod.POST)
	public String updateCampus(@ModelAttribute("campus") Campus campus) {
		campusManager.updateCampus(campus);
		userAuditLoggerUtil.log("Update campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());	
		return "redirect:createCampus.ems";
	}
	
	@RequestMapping(value = "updateBuilding.ems", method = RequestMethod.POST)
	public String updateBuilding(@ModelAttribute("building") Building building) {
		buildingManager.editName(building);
		userAuditLoggerUtil.log("Update building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());	
		return "redirect:createBuilding.ems?default_selected_campus="+building.getCampus().getId();
	}
	
	@RequestMapping(value = "updateFloor.ems", method = RequestMethod.POST)
	public String updateFloor(@ModelAttribute("floor") Floor floor) {
		floorManager.updateFloor(floor);
		userAuditLoggerUtil.log("Update floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());	
		return "redirect:createFloor.ems?default_selected_campus="
		+ floor.getBuilding().getCampus().getId() + "&default_selected_building=" + floor.getBuilding().getId();
	}
	
	@RequestMapping(value = "createArea.ems", method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView createArea() {
		Company comp = companyManager.loadCompanyById(1L);
		Area area = new Area();
		ModelAndView mvc = new ModelAndView("createSite");
		mvc.addObject("page", "area");
		mvc.addObject("area", area);
		mvc.addObject("companySetup", "true");
		mvc.addObject("campuses", companyManager.laodCompanyWithCampus(comp.getId()).getCampuses());
		return mvc;
	}
	
	@RequestMapping(value = "addArea.ems", method = RequestMethod.POST)
	public String addArea(@ModelAttribute("area") Area area) {
		try {
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
	public String updateArea(@ModelAttribute("area") Area area) {
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
	

}
