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
import com.emscloud.model.AppInstance;
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
import com.emscloud.service.AppInstanceManager;
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
@RequestMapping("/appinstance")
public class AppInstanceController {
	
	Logger logger = Logger.getLogger(AppInstanceController.class.getName());

	@Resource
	AppInstanceManager		appInstanceManager;
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	SystemConfigurationManager  systemConfigurationManager ;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	private GlemManager glemManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	
	@RequestMapping(value = "/listCustomerApps.ems")
	public String loadRegEms(Model model,  @RequestParam("customerId") long customerId) {
		
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customer", customer);
		model.addAttribute("customerId",customer.getId());
		model.addAttribute("mode",systemConfigurationManager.loadConfigByName("cloud.mode").getValue());
		return "appinstance/listCustomerApps";

	}
	
	@RequestMapping(value = "/list.ems", method = { RequestMethod.GET, RequestMethod.POST })
	public String listAppInstance(Model model,  @RequestParam("customerId") long customerId)
	{
		model.addAttribute("customerId", customerId);
		
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		
		return "appinstance/list";
	}
	
	@RequestMapping(value = "/listUnregApps.ems")
	public String loadUnregApps(Model model) {
		
		return "listUnregApps";

	}

	@RequestMapping(value = "/loadAppBrowseSetting.ems")
	public String loadBrowseSettingByAppInstanceId(Model model, @RequestParam("appInstanceId") Long appInstanceId) {
		model.addAttribute("appInstanceDetailsView", appInstanceManager.loadAppInstanceById(appInstanceId));
		model.addAttribute("systemConfiguration", systemConfigurationManager.loadConfigByName("browsing.supported.version"));
		return "appinstance/browseView";

	}
	
	
	@RequestMapping("/activate.ems")
    String activateAppInstance(Model model, @RequestParam("appInstanceId") Long appInstanceId) {

		AppInstance appInstance ;
		appInstance = appInstanceManager.loadAppInstanceById(appInstanceId);
		model.addAttribute("appInstance", appInstance);
		
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
        return "appActivate/details";
    }
	
	@RequestMapping("/activateApp.ems")
    String activateAppInstance(AppInstance appInstance, @RequestParam("customerId") Long customerId) throws Exception  {
        Customer customer = customerManager.loadCustomerById(customerId);
        
        AppInstance appInstanceObject;
		appInstanceObject = appInstanceManager.loadAppInstanceById(appInstance.getId());
        
		appInstanceObject.setCustomer(customer);
		appInstanceObject.setActive(true);
		appInstanceObject.setAppCommissionedDate(appInstance.getAppCommissionedDate());
		appInstanceObject.setName(appInstance.getName());
		appInstanceObject.setSshTunnelPort((long) CommonUtils.getRandomPort_App());
		
		String strKey = new Date().toString().replaceAll(" ", "");
		
		appInstanceObject = appInstanceManager.saveOrUpdate(appInstanceObject);
		cloudAuditLoggerUtil.log("App Instance Activated ( Mac Id: " +appInstanceObject.getMacId()+" ) of Customer "+customer.getName(), CloudAuditActionType.Em_Instance_Activation.getName());

		if(!RequestsBlockingPriorityQueue.getMap().containsKey(appInstanceObject.getMacId().toUpperCase().replaceAll(":", ""))) {
			logger.info("Create priority blocking queue for app " + appInstanceObject.getMacId());
			RequestsBlockingPriorityQueue.getMap().put(appInstanceObject.getMacId().toUpperCase().replaceAll(":", ""), new RequestsBlockingPriorityQueue(appInstanceObject.getMacId().toUpperCase().replaceAll(":", "")));
		}
		
		return "listUnregApps";
		/*if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			return "listUnregEms";
		}else{
			return "redirect:/appinstance/listUnregGlemApps.ems?customerId=" + customerId;
		}*/
    }
	
	@RequestMapping("/edit.ems")
	String editAppInstance(Model model, @RequestParam("appInstanceId") Long appInstanceId) {

		AppInstance appInstance;
		appInstance = appInstanceManager.loadAppInstanceById(appInstanceId);
		if(appInstance != null){
			model.addAttribute("appInstance", appInstance);
			model.addAttribute("customerId", appInstance.getCustomer().getId());
			model.addAttribute("mode", "edit");
			model.addAttribute("supportedVersionString",systemConfigurationManager.loadConfigByName("browsing.supported.version").getValue());
			
//			String filePath = "/etc/enlighted/CA/ssl/pfx/";
//			try{
//				File fileToDownload = new File(filePath + appInstance.getDatabaseName() + ".pfx");
//				if(fileToDownload.exists() && emInstance.getSppaEnabled()){
//					model.addAttribute("downloadCertEnable", true);
//					
//				}else{
//					model.addAttribute("downloadCertEnable", false);
//				}
//				
//			}catch( Exception e){
//				e.printStackTrace();
//			}
		}
		
		
        return "appInstance/details";
    }
	
//	@RequestMapping("/downloadcert.ems")
//	public void downloadClientCert(@RequestParam("emInstanceId") Long emInstanceId,HttpServletResponse resp)
//	{
//		EmInstance emInstance;
//		emInstance = emInstanceManager.loadEmInstanceById(emInstanceId);
//		if(emInstance != null){
//			String filePath = "/etc/enlighted/CA/ssl/pfx/";
//			try{
//				File fileToDownload = new File(filePath + emInstance.getDatabaseName() + ".pfx");
//				if(fileToDownload.exists()){
//					//System.out.println("File Exists");
//					ServletOutputStream servletOutputStream = null;
//					try{
//						servletOutputStream = resp.getOutputStream();
//						resp.setContentType("application/octet-stream");
//					    resp.setHeader("Content-Disposition", "attachment; filename="+ emInstance.getDatabaseName() + ".pfx");
//					    byte[] bytes = getBytesFromFile(fileToDownload);
//					    resp.setContentLength((int) bytes.length);
//						servletOutputStream.write(bytes);
//					}catch (Exception e) {
//						e.printStackTrace();
//					}finally{
//						try {
//							servletOutputStream.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}else{
//					//System.out.println("File Does not Exists");
//				}
//				
//			}catch( Exception e){
//				e.printStackTrace();
//			}
//		}
//		
//	}
//	
//	public byte[] getBytesFromFile(File file) throws IOException { 
//		InputStream is = null;
//		byte[] bytes = null;
//		try {
//			is = new FileInputStream(file); 
//			long length = file.length(); 
//			bytes = new byte[(int)length]; 
//			int offset = 0; 
//			int numRead = 0; 
//			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) { 
//				offset += numRead; 
//			} 
//			if (offset < bytes.length) { 
//				throw new IOException("Could not completely read file "+file.getName()); 
//			} 
//		}
//		catch (IOException e) {
//			throw e;
//		}
//		finally {
//			IOUtils.closeQuietly(is);
//		}
//		return bytes; 
//	}
	
	@RequestMapping("/save.ems")
    String saveAppInstance(AppInstance appInstance, @RequestParam("customerId") Long customerId) {
        
        
    	if(appInstance.getId() != 0 )
        {
    		AppInstance appInstanceObject = appInstanceManager.loadAppInstanceById(appInstance.getId());
    		if(appInstanceObject != null){
        		appInstanceObject.setName(appInstance.getName());
        		appInstanceObject.setVersion(appInstance.getVersion());
        		appInstanceObject.setMacId(appInstance.getMacId());
        		appInstanceObject.setAppCommissionedDate(appInstance.getAppCommissionedDate());
        		appInstanceObject.setIpAddress(appInstance.getIpAddress());
        		appInstanceObject.setOpenSshTunnelToCloud(appInstance.getOpenSshTunnelToCloud());
        		AppInstance savedAppInstanceObject = appInstanceManager.saveOrUpdate(appInstanceObject);
        		
        		cloudAuditLoggerUtil.log("App Instance Updated ( Mac Id: " +savedAppInstanceObject.getMacId()+" ) of Customer "+customerManager.loadCustomerById(customerId).getName(), CloudAuditActionType.Em_Instance_Updated.getName());
    		}
    	} else{
    		Customer customer = customerManager.loadCustomerById(customerId);
            appInstance.setCustomer(customer);
            appInstance.setActive(true);
            AppInstance savedAppInstanceObject = appInstanceManager.saveOrUpdate(appInstance);
            if(!RequestsBlockingPriorityQueue.getMap().containsKey(savedAppInstanceObject.getMacId().toUpperCase().replaceAll(":", ""))) {
    			logger.info("Create priority blocking queue for app " + savedAppInstanceObject.getMacId());
    			RequestsBlockingPriorityQueue.getMap().put(savedAppInstanceObject.getMacId().toUpperCase().replaceAll(":", ""), new RequestsBlockingPriorityQueue(savedAppInstanceObject.getMacId().toUpperCase().replaceAll(":", "")));
    		}
    	}
    	Thread taskThread = new Thread(new NewRequest.AddEmTaskToQueue(appInstance.getMacId().toUpperCase(), (short) 0, 30000L));
		taskThread.start();
	    return "redirect:/appinstance/list.ems?customerId=" + customerId ;
    }
	
	
	@RequestMapping(value = "/viewappinstancedetails.ems", method = RequestMethod.POST)
    String appInstanceDetails(Model model, @RequestParam("appInstanceId") Long appInstanceId) {
		AppInstance appInstance = appInstanceManager.loadAppInstanceById(appInstanceId);
		if(appInstance != null){
			model.addAttribute("cpuThresholdValue", Constants.cpuThresholdValue);
			model.addAttribute("appInstanceId", appInstanceId);
			model.addAttribute("appInstanceName", appInstance.getName());
			model.addAttribute("customerId", appInstance.getCustomer().getId());
			model.addAttribute("showBrowse",systemConfigurationManager.loadConfigByName("browsing.show").getValue());
		}
		
		return "appinstance/viewdetails";
        
    }
	
	@RequestMapping(value = "/listDetails.ems")
	public String appInstanceListDetails(Model model, @RequestParam("appInstanceId") Long appInstanceId)
	{
		AppInstance appInstance;
		appInstance = appInstanceManager.loadAppInstanceById(appInstanceId);
		if(appInstance != null){
			try {
				appInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(appInstance.getLastConnectivityAt(), appInstance.getTimeZone()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			model.addAttribute("appInstanceDetailsView", appInstance);
			model.addAttribute("customerId", appInstance.getCustomer().getId());
		}
		
        return "appinstance/detailsView";
	}
}
