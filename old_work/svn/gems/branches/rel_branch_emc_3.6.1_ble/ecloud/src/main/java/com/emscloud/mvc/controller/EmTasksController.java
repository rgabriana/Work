package com.emscloud.mvc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.communication.types.TaskCode;
import com.communication.types.TaskStatus;
import com.communication.utils.JsonUtil;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksUUID;
import com.emscloud.model.Upgrades;
import com.emscloud.service.CertificateManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.EmTasksUUIDManager;
import com.emscloud.service.UpgradesManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.vo.LogEmTaskVO;
import com.communication.utils.EmTasksUUIDUtil;


@Controller
@RequestMapping("/emtasks")
public class EmTasksController {
	
	@Resource
	EmTasksManager emTasksManager;
	@Resource
	EmTasksUUIDManager emTasksUUIDManager;
	@Resource
	EmInstanceManager emInstanceManager;
	
	@Resource
	UpgradesManager upgradesManager;
	
	@Resource
	CertificateManager certificateManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@RequestMapping("/saveScheduleUpgrade.ems")
    String saveScheduleUpgradeEmInstance(Model model, EmTasks emtask) {
		
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emtask.getEmInstanceId());
		if(emInstance != null){
			emtask.setNumberOfAttempts(0);
			emtask.setStartTime(new Date());
			emtask.setTaskCode(TaskCode.UPGRADE);
			emtask.setTaskStatus(TaskStatus.SCHEDULED);
			HashMap<String, String> map = new HashMap<String, String>();
			Upgrades deb = upgradesManager.loadDebianById(Long.parseLong(emtask.getParameters()));
			map.put("imageId", new Long(deb.getId()).toString());
			map.put("imageName", deb.getName());
			map.put("imageType", deb.getType());
			try {
				map.put("md5", DigestUtils.md5Hex(new FileInputStream(deb.getLocation())));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			emtask.setParameters(JsonUtil.getJSONString(map));
			emTasksManager.saveObject(emtask);
		}
		
		return "redirect:/eminstance/list.ems?customerId=" + emInstance.getCustomer().getId() ;
		
    }
	
	@RequestMapping("/saveScheduleCertificateUpgrade.ems")
	String saveScheduleCertificateUpgradeEmInstance(Model model, EmTasks emtask) {

		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emtask
				.getEmInstanceId());

		emtask.setNumberOfAttempts(0);
		emtask.setStartTime(new Date());
		emtask.setTaskCode(TaskCode.CERTIFICATE_SYNC);
		emtask.setTaskStatus(TaskStatus.SCHEDULED);
		HashMap<String, String> map = new HashMap<String, String>();
				
		//add trust store file details
		map.put("tsCertFile", "enlighted.ts");
		
		try {			
			map.put("md5ts", DigestUtils.md5Hex(new FileInputStream(certificateManager.getTSLocation())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//add key store file details if the same exists
		boolean ifKsFileExists = new File(certificateManager.getKSLocation(emtask.getEmInstanceId())).exists();
		if(ifKsFileExists){
			map.put("ksCertFile", certificateManager.getKSFileName(emtask.getEmInstanceId()));			

			try {
				map.put("md5ks", DigestUtils.md5Hex(new FileInputStream(certificateManager.getKSLocation(emtask.getEmInstanceId()))));				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//create and save new uuid for em task
			EmTasksUUID taskUuid = new EmTasksUUID();
			taskUuid.setUuid(UUID.randomUUID().toString());
			taskUuid.setActive(true);
			taskUuid = emTasksUUIDManager.saveOrUpdate(taskUuid);
			//update uuid details in em_tasks table
			emtask.setEmTasksUuid(taskUuid.getId());
			//update ks otp/uuid in map
			try {
				map.put("ksUuid", taskUuid.getUuid());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		emtask.setParameters(JsonUtil.getJSONString(map));
		emTasksManager.saveObject(emtask);
		
		if (emInstance != null) {
			cloudAuditLoggerUtil.log(
					"Certificate Sync task added for EM : "
							+ emInstance.getName() + " ( Mac Id :"
							+ emInstance.getMacId() + " ) of Customer "
							+ emInstance.getCustomer().getName(),
					CloudAuditActionType.Em_Instance_Certificate_Sync_Task
							.getName());
			model.addAttribute("emInstanceId", emtask.getEmInstanceId());
			model.addAttribute("emInstanceName", emInstance.getName());
		}

		return "loadEmTaskListByEmInstanceId";

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
		try {
			map.put("md5", DigestUtils.md5Hex(new FileInputStream(deb.getLocation())));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		emtask.setParameters(JsonUtil.getJSONString(map));
		emTasksManager.saveObject(emtask);
		if(emInstance != null){
			cloudAuditLoggerUtil.log("Upgrade task added for EM : "+emInstance.getName()+" ( Mac Id :"+emInstance.getMacId()+" ) of Customer "+emInstance.getCustomer().getName(), CloudAuditActionType.Em_Instance_Upgrade_Task.getName());
			model.addAttribute("emInstanceId", emtask.getEmInstanceId());
			model.addAttribute("emInstanceName", emInstance.getName());
		}
		
		return "loadEmTaskListByEmInstanceId";
	}
	@RequestMapping("/saveEmLogTask.ems")
    String saveEmLogTask(Model model, LogEmTaskVO emtaskVo) {
		EmTasks emtask = new EmTasks() ;
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emtaskVo.getEmInstanceId());
		emtask.setEmInstanceId(emtaskVo.getEmInstanceId());
		emtask.setNumberOfAttempts(0);
		emtask.setStartTime(new Date());
		emtask.setTaskCode(TaskCode.LOG_UPLOAD);
		emtask.setTaskStatus(TaskStatus.SCHEDULED);
		emtask.setPriority(emtaskVo.getPriority());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("fileName",emtaskVo.getLogNameParameters() );
		map.put("typeOfUpload",  emtaskVo.getLogTypeParameters());
		emtask.setParameters(JsonUtil.getJSONString(map));
		emTasksManager.saveObject(emtask);
		if(emInstance != null){
			cloudAuditLoggerUtil.log("Log Upload task added for EM : "+emInstance.getName()+" ( Mac Id :"+emInstance.getMacId()+" ) of Customer "+emInstance.getCustomer().getName(), CloudAuditActionType.Em_Instance_Log_Upload_Task.getName());
			model.addAttribute("emInstanceId", emtaskVo.getEmInstanceId());
			model.addAttribute("emInstanceName", emInstance.getName());
		}
		
		return "loadEmTaskListByEmInstanceId";
	}
}
