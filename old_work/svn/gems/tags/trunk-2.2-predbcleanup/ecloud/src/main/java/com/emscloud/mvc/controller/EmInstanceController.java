package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.CloudBuilding;
import com.emscloud.model.CloudCampus;
import com.emscloud.model.CloudFloor;
import com.emscloud.model.EmInstance;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.tree.TreeNode;
import com.emscloud.types.FacilityType;
import com.emscloud.dao.CloudBuildingDao;
import com.emscloud.dao.CloudCampusDao;
import com.emscloud.dao.CloudFloorDao;

@Controller
@RequestMapping("/eminstance")
public class EmInstanceController {

	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	CloudCampusDao			cloudCampusDao;
	
	@Resource
	CloudBuildingDao		cloudBuildingDao;
	
	@Resource
	CloudFloorDao			cloudFloorDao;

	@RequestMapping(value = "/list.ems")
	public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		
		return "eminstance/list";
	}

	@RequestMapping(value = "/map.ems")
	public String mapEmInstance(Model model,  @RequestParam("id") long id)
	{
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
		model.addAttribute("eminstance", emInstance);
		
		TreeNode<FacilityType> emFacilityTree = emInstanceManager.loadEmFacilityHierarchy(id);
		model.addAttribute("emfacilitytree", emFacilityTree);
		
		List<CloudCampus> cloudCampusList = cloudCampusDao.loadCloudCampusesByCustomerId(emInstance.getCustomer().getId());
		model.addAttribute("cloudcampuslist", cloudCampusList);

		List<CloudBuilding> cloudBuildingList = new ArrayList<CloudBuilding>();
		List<CloudFloor> cloudFloorList = new ArrayList<CloudFloor>();;
		
		for(CloudCampus campus : cloudCampusList)
		{
			List<CloudBuilding> bldgList = cloudBuildingDao.loadCloudBuildingsByCampusId(campus.getId());
			cloudBuildingList.addAll(bldgList);
			
			for(CloudBuilding bldg : bldgList)
			{
				List<CloudFloor> floorList = cloudFloorDao.loadCloudFloorsByBldgId(bldg.getId());
				cloudFloorList.addAll(floorList);
			}
		}
		
		model.addAttribute("cloudbuildinglist", cloudBuildingList);
		model.addAttribute("cloudfloorlist", cloudFloorList);
		
		
		return "eminstance/map";
	}

	@RequestMapping(value = "/save_mapping.ems", method = RequestMethod.POST)
	public String saveFacilityMapping(Model model,
			@RequestParam("selectedFacilities") String selectedFacilities) {

		String[] assignedFacilities = selectedFacilities.split(",");
		return "redirect:/eminstance/list.ems";
	}

	@RequestMapping("/delete.ems")
	public String deleteEmInstance(Model model, @RequestParam("id") long id) {
		Integer status =0;
		try
		{
			emInstanceManager.delete(id);
			status = 0;
		}catch(DataIntegrityViolationException de)
		{
			status = 1;
		}
		return "redirect:/eminstance/list.ems?deleteStatus="+status;
	}
}
