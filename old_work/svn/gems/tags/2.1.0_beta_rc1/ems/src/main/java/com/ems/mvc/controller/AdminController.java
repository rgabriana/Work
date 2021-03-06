package com.ems.mvc.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.annotaion.InvalidateFacilityTreeCache;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.PlanMap;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.service.AreaManager;
import com.ems.service.BuildingManager;
import com.ems.service.CampusManager;
import com.ems.service.CompanyManager;
import com.ems.service.FloorManager;
import com.ems.service.PlanMapManager;
import com.ems.types.UserAuditActionType;

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

	@RequestMapping("/organization/setup.ems")
	public String orgSetup(Model model) {
		model.addAttribute("company", companyManager.getAllCompanies().get(0));
		model.addAttribute("timezone", companyManager.getTimezoneList());
		model.addAttribute("mode", "admin");
		ArrayList<String> list = new ArrayList<String>();
		list.add(ServerMain.getInstance().getIpAddress("eth0"));
		list.add(ServerMain.getInstance().getIpAddress("eth1"));
		model.addAttribute("system", list);
		return "admin/organization/setup";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/companyUpdate.ems", method = RequestMethod.POST)
	public String updateCompany(@RequestParam("gemstime") String gemstime,
			@RequestParam("changeTime") String changeTime,
			@ModelAttribute("company") Company company) {
		company.setId(1L);
		companyManager.updateCompany(company);
		if ("true".equals(changeTime)) {
			companyManager.setServerTime(gemstime);
		}
		
		userAuditLoggerUtil.log("Company is updated.", UserAuditActionType.Company_Update.getName());
		
		return "redirect:/admin/organization/setup.ems";
	}

	@RequestMapping("/organization/setting.ems")
	public String orgSetting(
			Model model,
			@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie) {
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Company comp = companyManager.loadCompanyById(1L);
		model.addAttribute("mode", "admin");
		model.addAttribute("campuses",
				companyManager.laodCompanyWithCampus(comp.getId())
						.getCampuses());
		model.addAttribute("companyName", comp.getName());

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
			return "createSite";
		}
		case FLOOR: {
			try {
				model.addAttribute("page", "area");
				Area area = new Area();
				model.addAttribute("area", area);
				Floor floor = floorManager.getFloorById(cookieHandler
						.getFacilityId());
				model.addAttribute("org", floor);
				model.addAttribute("floors", floorManager
						.getAllFloorsByBuildingId(floor.getBuilding().getId()));
				model.addAttribute("default_selected_campus", floor
						.getBuilding().getCampus().getId());
				model.addAttribute("default_selected_building", floor
						.getBuilding().getId());
				model.addAttribute("default_selected_floor",
						cookieHandler.getFacilityId());
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
			return "createSite";

		}
		default: {
			return "createSite";
		}
		}

	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addCampus.ems", method = RequestMethod.POST)
	public String addCampus(@ModelAttribute("campus") Campus campus) {
		campusManager.addCampus(campus);
		userAuditLoggerUtil.log("Create campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateCampus.ems", method = RequestMethod.POST)
	public String updateCampus(@ModelAttribute("campus") Campus campus) {
		campusManager.updateCampus(campus);
		userAuditLoggerUtil.log("Update campus: " + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addBuilding.ems", method = RequestMethod.POST)
	public String addBuilding(@ModelAttribute("building") Building building) {
		buildingManager.createBuilding(building);
		userAuditLoggerUtil.log("Create building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateBuilding.ems", method = RequestMethod.POST)
	public String updateBuilding(@ModelAttribute("building") Building building) {
		buildingManager.editName(building);
		userAuditLoggerUtil.log("Update building: " + building.getName() +"(" + building.getId() +")", UserAuditActionType.Building_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addFloor.ems", method = RequestMethod.POST)
	public String addFloor(@ModelAttribute("floor") Floor floor) {

		floorManager.createFloor(floor);
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateFloor.ems", method = RequestMethod.POST)
	public String updateFloor(@ModelAttribute("floor") Floor floor) {
		floorManager.updateFloor(floor);
		userAuditLoggerUtil.log("Update floor: " + floor.getName() +"(" + floor.getId() +")", UserAuditActionType.Floor_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/addArea.ems", method = RequestMethod.POST)
	public String addArea(@ModelAttribute("area") Area area) {
		try {
			area.setProfileHandler(floorManager.getFloorById(
					area.getFloor().getId()).getProfileHandler());
			areaManager.save(area);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		userAuditLoggerUtil.log("Create area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/updateArea.ems", method = RequestMethod.POST)
	public String updateArea(@ModelAttribute("area") Area area) {
		areaManager.updateArea(area);
		userAuditLoggerUtil.log("Update area: " + area.getName() +"(" + area.getId() +")", UserAuditActionType.Area_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editCompanyName.ems", method = RequestMethod.POST)
	public String editCompanyName(@ModelAttribute("org") Company company) {
		Company savedCompany = companyManager.loadCompanyById(company.getId());
		String oldName = savedCompany.getName();
		savedCompany.setName(company.getName());
		companyManager.update(savedCompany);
		userAuditLoggerUtil.log("Edit company name from  " + oldName + " to " + company.getName() +"(" + company.getId() +")", UserAuditActionType.Company_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editCampusName.ems", method = RequestMethod.POST)
	public String editCampusName(@ModelAttribute("org") Campus campus) {
		Campus savedCampus = campusManager.loadCampusById(campus.getId());
		String oldName = savedCampus.getName();
		savedCampus.setName(campus.getName());
		campusManager.update(savedCampus);
		userAuditLoggerUtil.log("Edit campus name from " + oldName + " to "  + campus.getName() +"(" + campus.getId() +")", UserAuditActionType.Campus_Update.getName());
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/organization/editBuildingName.ems", method = RequestMethod.POST)
	public String editBuildingName(@ModelAttribute("org") Building building) {
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
	public String editFloorName(@ModelAttribute("org") Floor floor) {
		Floor savedFloor = floorManager.getFloorusingId(floor.getId());
		String oldName = savedFloor.getName();
		savedFloor.setName(floor.getName());
		
		try {
			if (!floor.getPlanMap().getFileData().isEmpty()) {
				Blob blob = new SerialBlob(floor.getPlanMap().getFileData()
						.getBytes());
				PlanMap planMap = null;
				if (savedFloor.getPlanMap() != null) {
					savedFloor.setFloorPlanUrl(floor.getPlanMap().getFileData()
							.getOriginalFilename());
					planMap = planMapManager.loadPlanMapById(savedFloor
							.getPlanMap().getId());
					planMap.setPlan(blob);
					planMapManager.update(planMap);
				} else {
					planMap = new PlanMap();
					planMap.setPlan(blob);
					planMapManager.save(planMap);
					savedFloor.setPlanMap(planMap);
					savedFloor.setFloorPlanUrl(floor.getPlanMap().getFileData()
							.getOriginalFilename());
				}
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
	public String editAreaName(@ModelAttribute("org") Area area) {
		Area savedArea = areaManager.getAreaUsingId(area.getId());
		String oldName = savedArea.getName();
		savedArea.setName(area.getName());
		try {
			areaManager.update(savedArea);
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
	public String installDevices() {
		return "devices/install";
	}

	@InvalidateFacilityTreeCache
	@RequestMapping(value = "/delete/refresh.ems")
	public String refreshTree() {
		return "redirect:/admin/organization/setting.ems?refreshTree=true";
	}
}
