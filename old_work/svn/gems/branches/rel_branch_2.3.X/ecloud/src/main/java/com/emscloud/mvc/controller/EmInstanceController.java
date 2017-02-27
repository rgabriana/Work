package com.emscloud.mvc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;

import com.communication.types.PriorityType;
import com.communication.types.TaskCode;
import com.communication.types.TaskStatus;

import com.emscloud.model.Customer;

import com.emscloud.model.EmInstance;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.Upgrades;

import com.emscloud.model.EmInstance;
import com.emscloud.model.EmTasks;
import com.emscloud.model.Upgrades;

import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.ReplicaServerManager;
import com.emscloud.service.UpgradesManager;
import com.emscloud.util.Constants;
import com.emscloud.util.UTCConverter;

@Controller
@RequestMapping("/eminstance")
public class EmInstanceController {

	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	UpgradesManager upgradesManager;
	
	@Resource
	ReplicaServerManager replicaServerManager;
	@Resource
	EmStateManager emStateManager;
	
	@Resource
	EmStatsManager		emStatsManager;

	@RequestMapping(value = "/list.ems", method = { RequestMethod.GET, RequestMethod.POST })
	public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		
		return "eminstance/list";
	}
	
	@RequestMapping(value = "/listUnregEms.ems")
	public String loadUnregEms(Model model) {
		
		return "listUnregEms";

	}

	@RequestMapping(value = "/statusList.ems")
	public String loadEmStatus(Model model) {
		
		return "listEmStatus";

	}
	
	@RequestMapping(value = "/loadEmTaskListByEmInstanceId.ems")
	public String loadEmTaskListByEmInstanceId(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		model.addAttribute("emInstanceId", emInstanceId);
		model.addAttribute("emInstanceName", emInstanceManager.loadEmInstanceById(emInstanceId).getName());
		return "loadEmTaskListByEmInstanceId";

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
	
	@RequestMapping("/create.ems")
    String createEmInstance(Model model, @RequestParam("customerId") Long customerId) {
		
		Customer customer = customerManager.loadCustomerById(customerId);
		EmInstance emInstance = new EmInstance();
		emInstance.setSppaPrice(customer.getSppaPrice());
		model.addAttribute("emInstance", emInstance);
		model.addAttribute("customerId", customerId);
		model.addAttribute("mode", "create");
		model.addAttribute("replicaServerCollection", replicaServerManager.getAllReplicaServers());
        return "emInstance/details";
    }
	
	@RequestMapping("/activate.ems")
    String activateEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstance", emInstance);
		
		ArrayList<Customer> customers = new ArrayList<Customer>();
		customers = (ArrayList<Customer>) customerManager.loadallCustomer();
		model.addAttribute("customerList", customers);
		
		model.addAttribute("replicaServerCollection", replicaServerManager.getAllReplicaServers());
        return "emActivate/details";
    }
	
	@RequestMapping("/activateEm.ems")
    String activateEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        Customer customer = customerManager.loadCustomerById(customerId);
        
        EmInstance emInstanceObject;
		emInstanceObject = emInstanceManager.loadEmInstanceById(emInstance.getId());
        
		emInstanceObject.setCustomer(customer);
		emInstanceObject.setActive(true);
		emInstanceObject.setName(emInstance.getName());
		emInstanceObject.setContactName(emInstance.getContactName());
		emInstanceObject.setContactEmail(emInstance.getContactEmail());
		emInstanceObject.setAddress(emInstance.getAddress());
		emInstanceObject.setContactPhone(emInstance.getContactPhone());
		emInstanceObject.setSppaEnabled(false);
		emInstanceObject.setSppaPrice(customer.getSppaPrice());
		emInstanceManager.saveOrUpdate(emInstanceObject);
		//update the state
		EmState emState = new EmState() ;
		emState.setEmInstanceId(emInstance.getId());
		emState.setDatabaseState(DatabaseState.NOT_MIGRATED);
		emState.setEmStatus(EmStatus.CALL_HOME);
		emState.setSetTime(Calendar.getInstance().getTime());
		emState.setFailedAttempts(0);
		emStateManager.saveOrUpdate(emState);
		return "listUnregEms";
    }
	
	@RequestMapping("/scheduleUpgrade.ems")
    String scheduleUpgradeEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		
		EmTasks task = new EmTasks();
		task.setEmInstanceId(emInstanceId);
		
		model.addAttribute("emtask", task);
		model.addAttribute("emInstanceName", emInstance.getName());
		model.addAttribute("emInstanceVersion", emInstance.getVersion());
		
		ArrayList<Upgrades> upgrades = new ArrayList<Upgrades>();
		upgrades = (ArrayList<Upgrades>) upgradesManager.loadallUpgrades();
		model.addAttribute("upgradesList", upgrades);
		model.addAttribute("priorityTypeList", PriorityType.values());
		
		return "emScheduleUpgrade/details";
    }
	
	@RequestMapping("/scheduleEmUpgrade.ems")
    String scheduleEmUpgrade(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		
		EmTasks task = new EmTasks();
		task.setEmInstanceId(emInstanceId);
		
		model.addAttribute("emtask", task);
		model.addAttribute("emInstanceName", emInstance.getName());
		model.addAttribute("emInstanceVersion", emInstance.getVersion());
		
		ArrayList<Upgrades> upgrades = new ArrayList<Upgrades>();
		upgrades = (ArrayList<Upgrades>) upgradesManager.loadallUpgrades();
		model.addAttribute("upgradesList", upgrades);
		model.addAttribute("priorityTypeList", PriorityType.values());
		
		return "scheduleEmUpgrade/details";
    }
	
	@RequestMapping("/loadEmEvents.ems")
    String loadEmEvents(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		
		model.addAttribute("emInstanceName", emInstance.getName());
		
		return "loadEmEvents";
    }
	
	@RequestMapping("/edit.ems")
    String editEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstance", emInstance);
		model.addAttribute("customerId", emInstance.getCustomer().getId());
		model.addAttribute("mode", "edit");
		model.addAttribute("replicaServerCollection", replicaServerManager.getAllReplicaServers());
		
		String filePath = "/etc/enlighted/CA/ssl/pfx/";
		try{
			File fileToDownload = new File(filePath + emInstance.getDatabaseName() + ".pfx");
			if(fileToDownload.exists() && emInstance.getSppaEnabled()){
				model.addAttribute("downloadCertEnable", true);
				
			}else{
				model.addAttribute("downloadCertEnable", false);
			}
			
		}catch( Exception e){
			e.printStackTrace();
		}
		
        return "emInstance/details";
    }
	
	@RequestMapping("/downloadcert.ems")
	public void downloadClientCert(@RequestParam("emInstanceId") Long emInstanceId,HttpServletResponse resp)
	{
		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		String filePath = "/etc/enlighted/CA/ssl/pfx/";
		try{
			File fileToDownload = new File(filePath + emInstance.getDatabaseName() + ".pfx");
			if(fileToDownload.exists()){
				//System.out.println("File Exists");
				ServletOutputStream servletOutputStream = null;
				try{
					servletOutputStream = resp.getOutputStream();
					resp.setContentType("application/octet-stream");
				    resp.setHeader("Content-Disposition", "attachment; filename="+ emInstance.getDatabaseName() + ".pfx");
				    byte[] bytes = getBytesFromFile(fileToDownload);
				    resp.setContentLength((int) bytes.length);
					servletOutputStream.write(bytes);
				}catch (Exception e) {
					e.printStackTrace();
				}finally{
					try {
						servletOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				//System.out.println("File Does not Exists");
			}
			
		}catch( Exception e){
			e.printStackTrace();
		}
	}
	
	public byte[] getBytesFromFile(File file) throws IOException { 
		InputStream is = new FileInputStream(file); 
		long length = file.length(); 
		byte[] bytes = new byte[(int)length]; 
		int offset = 0; 
		int numRead = 0; 
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) { 
			offset += numRead; 
		} 
		if (offset < bytes.length) { 
			throw new IOException("Could not completely read file "+file.getName()); 
		} 
		is.close(); 
		return bytes; 
	}
	
	@RequestMapping("/save.ems")
    String saveEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        
        
    	if(emInstance.getId() != 0 )
        {
    		EmInstance emInstanceObject = emInstanceManager.loadEmInstanceById(emInstance.getId());
    		if(emInstance.getSppaEnabled()){
    			String dbname = "em_" + customerId + "_" + emInstance.getId();
    			emInstanceObject.setDatabaseName(dbname);
    			emInstanceObject.setSppaEnabled(true);
    			emInstanceObject.setReplicaServer(emInstance.getReplicaServer());
    			emInstanceObject.setSppaBillEnabled(emInstance.getSppaBillEnabled());
    			emInstanceObject.setTaxable(emInstance.getTaxable());
    			emInstanceObject.setGeoLocation(emInstance.getGeoLocation());
    			if(emInstance.getSppaBillEnabled()) {
    				if(emInstance.getTaxable()) {
    					emInstanceObject.setTaxRate(emInstance.getTaxRate());
    				}else
    				{
    					emInstanceObject.setTaxRate((double) 0);
    				}
    				emInstanceObject.setBlockPurchaseEnergy(emInstance.getBlockPurchaseEnergy());
    			}
    			if(emStateManager.loadLastEmStatsByEmInstanceId(emInstance.getId()).getDatabaseState()==DatabaseState.NOT_MIGRATED){
            		EmState emState = new EmState() ;
    	    		emState.setEmInstanceId(emInstance.getId());
    	    		emState.setDatabaseState(DatabaseState.MIGRATION_READY);
    	    		emState.setEmStatus(EmStatus.SPPA);
    	    		emState.setSetTime(Calendar.getInstance().getTime());
    	    		emState.setFailedAttempts(0);
    	    		emStateManager.saveOrUpdate(emState);
            	}
    		}else{ 
    			emInstanceObject.setDatabaseName(null);
    			emInstanceObject.setSppaEnabled(false);
    			emInstanceObject.setReplicaServer(null);
    			EmState emState = new EmState() ;
    			emState.setEmInstanceId(emInstanceObject.getId());
    			emState.setDatabaseState(DatabaseState.NOT_MIGRATED);
    			emState.setEmStatus(EmStatus.CALL_HOME);
    			emState.setSetTime(Calendar.getInstance().getTime());
    			emState.setFailedAttempts(0);
    			emStateManager.saveOrUpdate(emState);
    		}
    		emInstanceObject.setName(emInstance.getName());
    		emInstanceObject.setVersion(emInstance.getVersion());
    		emInstanceObject.setMacId(emInstance.getMacId());
    		emInstanceObject.setContactName(emInstance.getContactName());
    		emInstanceObject.setContactEmail(emInstance.getContactEmail());
    		emInstanceObject.setAddress(emInstance.getAddress());
    		emInstanceObject.setContactPhone(emInstance.getContactPhone());
    		emInstanceObject.setSppaPrice(emInstance.getSppaPrice());
    		
    		EmInstance savedEmInstanceObject = emInstanceManager.saveOrUpdate(emInstanceObject);
    		
    		/*if(savedEmInstanceObject.getSppaEnabled()){
    			Runtime rt = Runtime.getRuntime();
        		Process proc = null;
        		String dataBaseName = savedEmInstanceObject.getDatabaseName();
        		//ArrayList<String> outputList = new ArrayList<String>();
        		try {
    				proc = rt.exec(new String[]{"/bin/bash", "/etc/enlighted/CA/scripts/generateClientcert.sh",dataBaseName});
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}*/
    		
    		
    	} else{
    		Customer customer = customerManager.loadCustomerById(customerId);
            emInstance.setCustomer(customer);
            emInstance.setActive(true);
            emInstance.setSppaEnabled(false);
            EmInstance savedEmInstanceObject = emInstanceManager.saveOrUpdate(emInstance);
    		Long emInstanceId = savedEmInstanceObject.getId();
    		EmState emState = new EmState() ;
			emState.setEmInstanceId(emInstanceId);
			emState.setDatabaseState(DatabaseState.NOT_MIGRATED);
			emState.setEmStatus(EmStatus.CALL_HOME);
			emState.setSetTime(Calendar.getInstance().getTime());
			emState.setFailedAttempts(0);
			emStateManager.saveOrUpdate(emState);
    	}
	    return "redirect:/eminstance/list.ems?customerId=" + customerId ;
    }
	
	@RequestMapping(value = "/viewemstats.ems", method = RequestMethod.POST)
    String viewEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
		model.addAttribute("emInstanceId", emInstanceId);
		model.addAttribute("emInstanceName", emInstance.getName());
		model.addAttribute("customerId", emInstance.getCustomer().getId());
		return "eminstance/viewemstats";
        
    }
	
	@RequestMapping(value = "/loademstats.ems", method = RequestMethod.GET)
    String loadEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
		model.addAttribute("emInstanceId", emInstanceId);
		model.addAttribute("emInstanceName", emInstance.getName());
		model.addAttribute("customerId", emInstance.getCustomer().getId());
		return "eminstance/loademstats";
        
    }
	
	@RequestMapping(value = "/vieweminstancedetails.ems", method = RequestMethod.POST)
    String emInstanceDetails(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
		model.addAttribute("emInstanceId", emInstanceId);
		model.addAttribute("emInstanceName", emInstance.getName());
		model.addAttribute("customerId", emInstance.getCustomer().getId());
		return "eminstance/viewdetails";
        
    }
	
	@RequestMapping(value = "/listDetails.ems")
	public String emInstanceListDetails(Model model, @RequestParam("emInstanceId") Long emInstanceId)
	{
		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		try {
			emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
		if(latestEmStats != null){
			emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
		}
		
		model.addAttribute("emInstanceDetailsView", emInstance);
		model.addAttribute("customerId", emInstance.getCustomer().getId());
		model.addAttribute("replicaServerCollection", replicaServerManager.getAllReplicaServers());
        return "eminstance/detailsView";
	}
		
}
