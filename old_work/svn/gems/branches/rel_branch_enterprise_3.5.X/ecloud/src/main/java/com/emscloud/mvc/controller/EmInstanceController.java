package com.emscloud.mvc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.types.PriorityType;
import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.longpollutil.NewRequest;
import com.emscloud.communication.longpollutil.RequestsBlockingPriorityQueue;
import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmTasks;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.Upgrades;
import com.emscloud.model.UserCustomers;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.FacilityTreeManager;
import com.emscloud.service.GlemManager;
import com.emscloud.service.ReplicaServerManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.service.UpgradesManager;
import com.emscloud.service.UserCustomersManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.types.GlemModeType;
import com.emscloud.types.RoleType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.Constants;
import com.emscloud.util.LogEnums;
import com.emscloud.util.UTCConverter;
import com.emscloud.util.tree.TreeNode;
import com.emscloud.vo.LogEmTaskVO;

@Controller
@RequestMapping("/eminstance")
public class EmInstanceController {
	
	Logger logger = Logger.getLogger(EmInstanceController.class.getName());

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
	
	@Resource
	SystemConfigurationManager  systemConfigurationManager ;
	
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	private GlemManager glemManager;
	
	@Resource
	FacilityManager facilityManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	
	@RequestMapping(value = "/listCustomerEms.ems")
	public String loadRegEms(Model model,  @RequestParam("customerId") long customerId) {
		
		Customer customer = customerManager.loadCustomerById(customerId);
		Long orgId = facilityManager.getOrganizationIdOfCustomer(customerId);
		Facility facility = facilityManager.getFacility(orgId); 
		model.addAttribute("organizationname", facility.getName());
		model.addAttribute("customer", customer);
		model.addAttribute("customerId",customer.getId());
		model.addAttribute("mode",systemConfigurationManager.loadConfigByName("cloud.mode").getValue());
		return "eminstance/listCustomerEms";

	}
	
	@RequestMapping("/mapeminstance.ems")
	String mapEmInstance(Model model,
			@RequestParam("emInstanceId") Long emInstanceId,@RequestParam("customerId") Long customerId) {
		
		model.addAttribute("emInstanceId", emInstanceId);
		
		model.addAttribute("customerId", customerId);
		
		TreeNode<FacilityType> facilityTreeHierarchy = null;

		facilityTreeHierarchy = facilityTreeManager.loadCompanyHierarchyByCustomerId(customerId);
		
		model.addAttribute("facilityTreeHierarchy", facilityTreeHierarchy);
		
		List<FacilityEmMapping> facilityEmMappingList = new ArrayList<FacilityEmMapping>();
		
		facilityEmMappingList = facilityEmMappingManager.getAllMappedFaciltyList();
		
		model.addAttribute("facilityEmMappingList", facilityEmMappingList);
		
		return "eminstance/mapDetails";

	}

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
	@RequestMapping(value = "/listUnregGlemEms.ems")
	public String loadUnregGlemEms(Model model,@RequestParam("customerId") long customerId) {
		model.addAttribute("customerId", customerId);
		return "listUnregGlemEms";

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
	
	@RequestMapping(value = "/loadEmBrowseSetting.ems")
	public String loadBrowseSettingByEmInstanceId(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		model.addAttribute("emInstanceDetailsView", emInstanceManager.loadEmInstanceById(emInstanceId));
		model.addAttribute("systemConfiguration", systemConfigurationManager.loadConfigByName("browsing.supported.version"));
		return "eminstance/browseView";

	}
	
	@RequestMapping(value = "/save_mapping.ems", method = RequestMethod.POST)
	public String saveFacilityMapping(Model model,
			@RequestParam("selectedFacilities") String selectedFacilities) {
		return "redirect:/eminstance/list.ems";
	}

	@RequestMapping("/delete.ems")
	@PreAuthorize("hasAnyRole('Admin')")
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
	@PreAuthorize("hasAnyRole('Admin')")
    String createEmInstance(Model model, @RequestParam("customerId") Long customerId) {
		EmInstance emInstance = new EmInstance();
		model.addAttribute("emInstance", emInstance);
		model.addAttribute("customerId", customerId);
		model.addAttribute("mode", "create");
		model.addAttribute("replicaServerCollection", emInstanceManager.getAllReplicaServerWithEMCount());
        return "emInstance/details";
    }
	
	@RequestMapping("/activate.ems")
    String activateEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		model.addAttribute("emInstance", emInstance);
		
		ArrayList<Customer> customers = new ArrayList<Customer>();
		//Check for super user
		if(emsAuthContext.getCurrentUserRoleType().equals(RoleType.Admin))
		customers = (ArrayList<Customer>) customerManager.loadallCustomer();
		else
		{	//Load customers by assignment
			List<UserCustomers> uCustomers = userCustomersManager.loadUserCustomersByUserId(emsAuthContext.getUserId());
			for (Iterator<UserCustomers> iterator = uCustomers.iterator(); iterator.hasNext();) {
				UserCustomers userCustomers = (UserCustomers) iterator.next();				
				customers.add(customerManager.loadCustomerById(userCustomers.getCustomer().getId()));
			}		
		}	
		//customers = (ArrayList<Customer>) customerManager.loadallCustomer();
		model.addAttribute("customerList", customers);
		model.addAttribute("mode",systemConfigurationManager.loadConfigByName("cloud.mode").getValue());
		model.addAttribute("replicaServerCollection", replicaServerManager.getAllReplicaServers());
        return "emActivate/details";
    }
	
	@RequestMapping("/activateEm.ems")
    String activateEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) throws Exception  {
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
		emInstanceObject.setSshTunnelPort((long) CommonUtils.getRandomPort());
		emInstanceObject.setEmCommissionedDate(emInstance.getEmCommissionedDate());
		
		String strKey = new Date().toString().replaceAll(" ", "");
		
		if(emInstanceObject.getApiKey() == null || "".equals(emInstanceObject.getApiKey())) {
			emInstanceObject.setApiKey(ArgumentUtils.getDigest(strKey));
		}
		if(emInstanceObject.getSecretKey() == null || "".equals(emInstanceObject.getSecretKey())) {
			emInstanceObject.setSecretKey(ArgumentUtils.getDigest(new StringBuffer(strKey.toString())
			.reverse().toString()));
		}
		EmInstance savedEmInstance = emInstanceManager.saveOrUpdate(emInstanceObject);
		cloudAuditLoggerUtil.log("Em Instance Activated ( Mac Id: " +savedEmInstance.getMacId()+" ) of Customer "+customer.getName(), CloudAuditActionType.Em_Instance_Activation.getName());

		if(!RequestsBlockingPriorityQueue.getMap().containsKey(emInstanceObject.getMacId().toUpperCase().replaceAll(":", ""))) {
			logger.info("Create priority blocking queue for em " + emInstanceObject.getMacId());
			RequestsBlockingPriorityQueue.getMap().put(emInstanceObject.getMacId().toUpperCase().replaceAll(":", ""), new RequestsBlockingPriorityQueue(emInstanceObject.getMacId().toUpperCase().replaceAll(":", "")));
		}
		
		//update the state
		EmState emState = new EmState() ;
		emState.setEmInstanceId(emInstance.getId());
		emState.setDatabaseState(DatabaseState.NOT_MIGRATED);
		emState.setEmStatus(EmStatus.CALL_HOME);
		emState.setSetTime(Calendar.getInstance().getTime());
		emState.setFailedAttempts(0);
		emStateManager.saveOrUpdate(emState);
		
		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			return "listUnregEms";
		}else{
			return "redirect:/eminstance/listUnregGlemEms.ems?customerId=" + customerId;
		}
    }
	
	@RequestMapping("/scheduleUpgrade.ems")
    String scheduleUpgradeEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			EmTasks task = new EmTasks();
			task.setEmInstanceId(emInstanceId);
			
			model.addAttribute("emtask", task);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("emInstanceVersion", emInstance.getVersion());
		}
		
		
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
		if(emInstance != null){
			EmTasks task = new EmTasks();
			task.setEmInstanceId(emInstanceId);
			
			model.addAttribute("emtask", task);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("emInstanceVersion", emInstance.getVersion());
		}
		
		
		ArrayList<Upgrades> upgrades = new ArrayList<Upgrades>();
		upgrades = (ArrayList<Upgrades>) upgradesManager.loadallUpgrades();
		
		ArrayList<Upgrades> upgrades1 = new ArrayList<Upgrades>();	
		
		if("10.04".equals(emInstance.getOsVersion())){
			for(Upgrades upgrade : upgrades){
				if(upgrade.getName().contains("_em_all.deb") && !upgrade.getName().contains("_a32_em_all.deb") && !upgrade.getName().contains("_a64_em_all.deb")){
					upgrades1.add(upgrade);
				}
			}
		}else if("14.04".equals(emInstance.getOsVersion())){
			if("32".equals(emInstance.getOsArch())){
				for(Upgrades upgrade : upgrades){
					if(upgrade.getName().contains("_a32_em_all.deb")){
						upgrades1.add(upgrade);
					}
				}
			}else if("64".equals(emInstance.getOsArch())){
				for(Upgrades upgrade : upgrades){
					if(upgrade.getName().contains("_a64_em_all.deb")){
						upgrades1.add(upgrade);
					}
				}
			}
		}
		
		model.addAttribute("upgradesList", upgrades1);
		
		model.addAttribute("priorityTypeList", PriorityType.values());
		
		return "scheduleEmUpgrade/details";
    }
	@RequestMapping("/scheduleLogTask.ems")
    String scheduleLogTaskEmInstance(Model model, @RequestParam("emInstanceId") Long emInstanceId) {

		EmInstance emInstance ;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			LogEmTaskVO task = new LogEmTaskVO();
			task.setEmInstanceId(emInstanceId);
			
			model.addAttribute("emtask", task);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("emInstanceVersion", emInstance.getVersion());
		}
		
	
		model.addAttribute("logEnumsList", LogEnums.values());
		ArrayList<String> typeOfUpload = new ArrayList<String>() ;
		typeOfUpload.add("CURRENT");
		typeOfUpload.add("ALL") ;
		model.addAttribute("typeOfUploadList", typeOfUpload.toArray());
		model.addAttribute("priorityTypeList", PriorityType.values());
		
		return "emScheduleLogTask/details";
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
		if(emInstance != null){
			model.addAttribute("emInstance", emInstance);
			model.addAttribute("customerId", emInstance.getCustomer().getId());
			model.addAttribute("mode", "edit");
			model.addAttribute("replicaServerCollection", emInstanceManager.getAllReplicaServerWithEMCount());
			model.addAttribute("supportedVersionString",systemConfigurationManager.loadConfigByName("browsing.supported.version").getValue());
			
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
		}
		
		
        return "emInstance/details";
    }
	
	@RequestMapping("/downloadcert.ems")
	public void downloadClientCert(@RequestParam("emInstanceId") Long emInstanceId,HttpServletResponse resp)
	{
		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
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
		
	}
	
	public byte[] getBytesFromFile(File file) throws IOException { 
		InputStream is = null;
		byte[] bytes = null;
		try {
			is = new FileInputStream(file); 
			long length = file.length(); 
			bytes = new byte[(int)length]; 
			int offset = 0; 
			int numRead = 0; 
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) { 
				offset += numRead; 
			} 
			if (offset < bytes.length) { 
				throw new IOException("Could not completely read file "+file.getName()); 
			} 
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			IOUtils.closeQuietly(is);
		}
		return bytes; 
	}
	
	@RequestMapping("/save.ems")
    String saveEmInstance(EmInstance emInstance, @RequestParam("customerId") Long customerId) {
        
        
    	if(emInstance.getId() != 0 )
        {
    		EmInstance emInstanceObject = emInstanceManager.loadEmInstanceById(emInstance.getId());
    		if(emInstanceObject != null){
    			if(emInstance.getSppaEnabled()){
        			String dbname = "em_" + customerId + "_" + emInstance.getId();
        			emInstanceObject.setDatabaseName(dbname);
        			emInstanceObject.setSppaEnabled(true);
        			emInstanceObject.setReplicaServer(emInstance.getReplicaServer());
        			emInstanceObject.setSppaBillEnabled(emInstance.getSppaBillEnabled());
        			emInstanceObject.setTaxable(emInstance.getTaxable());
        			emInstanceObject.setNoOfEmergencyFixtures(emInstance.getNoOfEmergencyFixtures());
        			emInstanceObject.setEmergencyFixturesGuidelineLoad(emInstance.getEmergencyFixturesGuidelineLoad());
        			emInstanceObject.setEmergencyFixturesLoad(emInstance.getEmergencyFixturesLoad());
        			if(emInstance.getSppaBillEnabled()) {
        				if(emInstance.getTaxable()) {
        					emInstanceObject.setTaxRate(emInstance.getTaxRate());
        				}else
        				{
        					emInstanceObject.setTaxRate((double) 0);
        				}
        			}
        			if(emStateManager.loadLastEmStatsByEmInstanceId(emInstance.getId()).getDatabaseState()==DatabaseState.NOT_MIGRATED){
                		EmState emState = new EmState() ;
        	    		emState.setEmInstanceId(emInstance.getId());
        	    		emState.setDatabaseState(DatabaseState.MIGRATION_READY);
        	    		emState.setEmStatus(EmStatus.SPPA);
        	    		emState.setSetTime(Calendar.getInstance().getTime());
        	    		emState.setFailedAttempts(0);
        	    		emStateManager.saveOrUpdate(emState);
        	    		emInstanceObject.setLastSuccessfulSyncTime(new Date());
                	}
        			// Call the Generate Certificate Script
        			try {
        	            emInstanceManager.generateSppaCert(emInstance.getId());
        	        } catch (Exception e) {
        	           logger.error("ERROR: Issue with generating SPPACert for the EM:"+emInstance.getId(), e);
        	        }
        		}else{ 
        			emInstanceObject.setDatabaseName(null);
        			emInstanceObject.setSppaEnabled(false);
        			emInstanceObject.setReplicaServer(null);
        			emInstanceObject.setPauseSyncStatus(false);
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
        		emInstanceObject.setOpenSshTunnelToCloud(emInstance.getOpenSshTunnelToCloud());
        		emInstanceObject.setEmCommissionedDate(emInstance.getEmCommissionedDate());
        		EmInstance savedEmInstanceObject = emInstanceManager.saveOrUpdate(emInstanceObject);
        		
        		cloudAuditLoggerUtil.log("Em Instance Updated ( Mac Id: " +savedEmInstanceObject.getMacId()+" ) of Customer "+customerManager.loadCustomerById(customerId).getName(), CloudAuditActionType.Em_Instance_Updated.getName());
        		
    		}
    		
    	} else{
    		Customer customer = customerManager.loadCustomerById(customerId);
            emInstance.setCustomer(customer);
            emInstance.setActive(true);
            emInstance.setSppaEnabled(false);
            EmInstance savedEmInstanceObject = emInstanceManager.saveOrUpdate(emInstance);
            if(!RequestsBlockingPriorityQueue.getMap().containsKey(savedEmInstanceObject.getMacId().toUpperCase().replaceAll(":", ""))) {
    			logger.info("Create priority blocking queue for em " + savedEmInstanceObject.getMacId());
    			RequestsBlockingPriorityQueue.getMap().put(savedEmInstanceObject.getMacId().toUpperCase().replaceAll(":", ""), new RequestsBlockingPriorityQueue(savedEmInstanceObject.getMacId().toUpperCase().replaceAll(":", "")));
    		}
    		Long emInstanceId = savedEmInstanceObject.getId();
    		EmState emState = new EmState() ;
			emState.setEmInstanceId(emInstanceId);
			emState.setDatabaseState(DatabaseState.NOT_MIGRATED);
			emState.setEmStatus(EmStatus.CALL_HOME);
			emState.setSetTime(Calendar.getInstance().getTime());
			emState.setFailedAttempts(0);
			emStateManager.saveOrUpdate(emState);
    	}
    	Thread taskThread = new Thread(new NewRequest.AddEmTaskToQueue(emInstance.getMacId().toUpperCase(), (short) 0, 30000L));
		taskThread.start();
	    return "redirect:/eminstance/list.ems?customerId=" + customerId ;
    }
	
	@RequestMapping(value = "/viewemstats.ems", method = RequestMethod.POST)
    String viewEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
			model.addAttribute("emInstanceId", emInstanceId);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("customerId", emInstance.getCustomer().getId());
		}
		
		return "eminstance/viewemstats";
        
    }
	
	@RequestMapping(value = "/loademstats.ems", method = RequestMethod.GET)
    String loadEmStats(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
			model.addAttribute("emInstanceId", emInstanceId);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("customerId", emInstance.getCustomer().getId());
		}
		
		return "eminstance/loademstats";
        
    }
	
	@RequestMapping(value = "/vieweminstancedetails.ems", method = RequestMethod.POST)
    String emInstanceDetails(Model model, @RequestParam("emInstanceId") Long emInstanceId) {
		EmInstance emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
			model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
			model.addAttribute("emInstanceId", emInstanceId);
			model.addAttribute("emInstanceName", emInstance.getName());
			model.addAttribute("customerId", emInstance.getCustomer().getId());
			model.addAttribute("showBrowse",systemConfigurationManager.loadConfigByName("browsing.show").getValue());
		}
		
		return "eminstance/viewdetails";
        
    }
	
	@RequestMapping(value = "/listDetails.ems")
	public String emInstanceListDetails(Model model, @RequestParam("emInstanceId") Long emInstanceId)
	{
		EmInstance emInstance;
		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
		if(emInstance != null){
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
		}
		
        return "eminstance/detailsView";
	}
}
