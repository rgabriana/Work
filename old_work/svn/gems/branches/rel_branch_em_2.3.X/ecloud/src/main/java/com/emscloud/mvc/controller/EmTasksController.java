package com.emscloud.mvc.controller;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.communication.types.TaskCode;
import com.communication.types.TaskStatus;
import com.communication.utils.JsonUtil;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmTasks;
import com.emscloud.model.Upgrades;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.UpgradesManager;


@Controller
@RequestMapping("/emtasks")
public class EmTasksController {
	
	@Resource
	EmTasksManager emTasksManager;
	@Resource
	EmInstanceManager emInstanceManager;
	
	@Resource
	UpgradesManager upgradesManager;
	
	@RequestMapping("/saveScheduleUpgrade.ems")
    String saveScheduleUpgradeEmInstance(Model model, EmTasks emtask) {
		
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emtask.getEmInstanceId());
		emtask.setNumberOfAttempts(0);
		emtask.setStartTime(new Date());
		emtask.setTaskCode(TaskCode.UPGRADE);
		emtask.setTaskStatus(TaskStatus.SCHEDULED);
		HashMap<String, String> map = new HashMap<String, String>();
		Upgrades deb = upgradesManager.loadDebianById(Long.parseLong(emtask.getParameters()));
		map.put("imageId", new Long(deb.getId()).toString());
		map.put("imageName", deb.getName());
		map.put("imageType", deb.getType());
		emtask.setParameters(JsonUtil.getJSONString(map));
		emTasksManager.saveObject(emtask);
		return "redirect:/eminstance/list.ems?customerId=" + emInstance.getCustomer().getId() ;
		
    }
	
	@RequestMapping("/saveEmTask.ems")
    String saveEmTask(Model model, EmTasks emtask) {
		
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emtask.getEmInstanceId());
		emtask.setNumberOfAttempts(0);
		emtask.setStartTime(new Date());
		emtask.setTaskCode(TaskCode.UPGRADE);
		emtask.setTaskStatus(TaskStatus.SCHEDULED);
		HashMap<String, String> map = new HashMap<String, String>();
		Upgrades deb = upgradesManager.loadDebianById(Long.parseLong(emtask.getParameters()));
		map.put("imageId", new Long(deb.getId()).toString());
		map.put("imageName", deb.getName());
		map.put("imageType", deb.getType());
		emtask.setParameters(JsonUtil.getJSONString(map));
		emTasksManager.saveObject(emtask);
		model.addAttribute("emInstanceId", emtask.getEmInstanceId());
		model.addAttribute("emInstanceName", emInstance.getName());
		return "loadEmTaskListByEmInstanceId";
	}

}
