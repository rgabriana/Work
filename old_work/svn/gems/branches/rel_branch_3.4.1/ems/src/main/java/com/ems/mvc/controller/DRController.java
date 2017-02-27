package com.ems.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.DRTarget;
import com.ems.model.DRUsers;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.DRTargetManager;
import com.ems.service.DRUserManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.DRType;
import com.ems.types.DrLevel;

@Controller
@RequestMapping("/dr")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class DRController {
	
	static final long keystoreCertificateFileSizeLimit  = 1048576;
	
	static final long truststoreCertificateFileSizeLimit  = 1048576;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "drTargetManager")
    private DRTargetManager drTargetManager;
    @Resource(name = "drUserManager")
    private DRUserManager drUserManager;
    
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	
	@RequestMapping("/listDR.ems")
    public String listDR(Model model) {
        return "dr/listDR";
    }
	
	@RequestMapping("/addUser.ems")
    public String addDRUser(Model model) {
		List<String> versionList = new ArrayList<String>();
		versionList.add("1.0");
		versionList.add("2.0");
		DRUsers drUser = drUserManager.getDRUser();
		
		if("".equals(drUser.getServicepath()) || drUser.getServicepath() == null){
			drUser.setServicepath("/OpenADR2/Simple/EiEvent");
   		}
		
		model.addAttribute("druser", drUser);
		if(drUser.getKeystoreFileName() != null && !"".equals(drUser.getKeystoreFileName())){
			model.addAttribute("druserKeystoreAvailable", true);
		}else{
			model.addAttribute("druserKeystoreAvailable", false);
		}
		if(drUser.getTruststoreFileName() != null && !"".equals(drUser.getTruststoreFileName())){
			model.addAttribute("druserTruststoreAvailable", true);
		}else{
			model.addAttribute("druserTruststoreAvailable", false);
		}
		model.addAttribute("druserKeystoreFileName", drUser.getKeystoreFileName());
		model.addAttribute("druserTruststoreFileName", drUser.getTruststoreFileName());
		model.addAttribute("versionList", versionList);
		model.addAttribute("drPollTimeInterval", systemConfigurationManager
				.loadConfigByName("dr.minimum.polltimeinterval").getValue());
        return "dr/addDRUser";
    }
	
	@RequestMapping(value = "/registerUserWithoutCertificate.ems",  method = RequestMethod.POST)
	public String registerUserWithoutCertificate(@ModelAttribute("druser") DRUsers druser) {
		
		boolean isDRCredentailValid = true;
		//VALIDATE DR USERNAME/PASSWORD
		//isDRCredentailValid = isValidDRUser(druser);
		if(isDRCredentailValid)
		{
			if(drUserManager.save(druser) != false)
			{
				return "redirect:/dr/addUser.ems?status=S";
			}
		}
		else
		{
			return "redirect:/dr/addUser.ems?status=E";
		}
		return null;
	}
	
	@RequestMapping(value = "/registerUserWithCertificate.ems",  method = RequestMethod.POST)
	public String registerUserWithCertificate(@RequestParam("keystoreCertificate") MultipartFile keystoreCertificateFile,
			@RequestParam("truststoreCertificate") MultipartFile truststoreCertificateFile,
			@ModelAttribute("druser") DRUsers druser) {
		
		boolean isDRCredentailValid = true;
		//VALIDATE DR USERNAME/PASSWORD
		//isDRCredentailValid = isValidDRUser(druser);
		if(isDRCredentailValid)
		{
			String enLightedADRcertsFolder = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/adr/certs/";
	        File oENLADRFolder = new File(enLightedADRcertsFolder);
	        if (!oENLADRFolder.exists()) {
	        	oENLADRFolder.mkdirs();
	        }
	   		
	   		if (!keystoreCertificateFile.isEmpty()) {
	   			
	   			if(keystoreCertificateFile.getSize() > keystoreCertificateFileSizeLimit){
	   				return "redirect:/dr/addUser.ems?status=keystoreFail";
	   			}
	   			try {
					drUserManager.uploadCertificateFile(enLightedADRcertsFolder,druser.getKeystoreFileName(),keystoreCertificateFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
	   		}
	   		
	   		if (!truststoreCertificateFile.isEmpty()) {
	   			
	   			if(truststoreCertificateFile.getSize() > truststoreCertificateFileSizeLimit){
	   				return "redirect:/dr/addUser.ems?status=truststoreFail";
	   			}
	   			
	   			try {
					drUserManager.uploadCertificateFile(enLightedADRcertsFolder,druser.getTruststoreFileName(),truststoreCertificateFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
	   		}
	   		
	   		if("".equals(druser.getServicepath()) || druser.getServicepath() == null){
	   			druser.setServicepath("/OpenADR2/Simple/EiEvent");
	   		}
			
			if(drUserManager.save(druser) != false)
			{
				return "redirect:/dr/addUser.ems?status=S";
			}
		}
		else
		{
			return "redirect:/dr/addUser.ems?status=E";
		}
		return null;
	}
	
	@RequestMapping("/prompt.ems")
    String showManualDrPrompt(Model model, @RequestParam("drId") Long drId) {
		String mode="";
		if(drId!=null && drId>0)
    	{
			mode="edit";			
			DRTarget drTarget = drTargetManager.getDRTargetById(drId);			
			//duration value in db is in seconds. prompt display is in minutes. hence the conversion.
			drTarget.setDuration(drTarget.getDuration()/60);
											
			model.addAttribute("drtarget", drTarget);
    	}
		else
		{
			mode="new";			
			DRTarget drTarget = new DRTarget();			
		    model.addAttribute("drtarget", drTarget);		    		    
		}		
	    	    	     
	    model.addAttribute("mode", mode);
	    model.addAttribute("eventType", DRType.values());	    
	    model.addAttribute("drLevel", DrLevel.values());
	    
        return "dr/prompt";
    }	

    @RequestMapping("/save.ems")
    String saveOverride(DRTarget drTarget) {
    	//user input in minutes. db value in seconds. hence the conversion
    	drTarget.setDuration(drTarget.getDuration()*60);
    	
    	if (drTarget.getId() == null) {   
    		drTarget.setOptIn(true);
    		drTarget.setJitter(0L);
    		if(drTarget.getDrType().equals(DRType.MANUAL.getName())) {
    			drTarget.setTargetReduction(drTargetManager.getPercentRedByDrLevel(drTarget.getPriceLevel()));
    		}
    		drTargetManager.saveOrUpdateDRTarget(drTarget);    	            
        } else {
        	DRTarget drTargetToSave = drTargetManager.getDRTargetById(drTarget.getId());
        	drTargetToSave.setPriceLevel(drTarget.getPriceLevel());
        	drTargetToSave.setPricing(drTarget.getPricing());
        	drTargetToSave.setDuration(drTarget.getDuration());        	
        	if(drTarget.getStartTime() != null)
        		drTargetToSave.setStartTime(drTarget.getStartTime());        	
        	drTargetToSave.setDescription(drTarget.getDescription());
        	if(drTargetToSave.getDrType().equals(DRType.MANUAL.getName())) {
        		drTargetToSave.setTargetReduction(drTargetManager.getPercentRedByDrLevel(drTarget.getPriceLevel()));
    		}
        	drTarget = drTargetManager.saveOrUpdateDRTarget(drTargetToSave);   
        }
    	
    	String startJobName = "DRStartJob" + drTarget.getId();
   		String startTriggerName = "DRStartTr" + drTarget.getId();
   		String endJobName = "DREndJob" + drTarget.getId();
   		String endTriggerName = "DREndTr" + drTarget.getId();
   		
   		Date endDate = new Date();
		endDate.setTime(drTarget.getStartTime().getTime() + drTarget.getDuration() * 1000 + 2000);
        try {
        	// Delete the older Quartz job and create a new one
    		drTargetManager.deleteScheduledJob(startJobName, drTarget.getDrType());
    		drTargetManager.scheduleOverrideJobs(startJobName, drTarget.getDrType(), startTriggerName, new Date(drTarget.getStartTime().getTime()));
            DeviceServiceImpl.getInstance().sendUTCTimeOnAllGateways();
			drTargetManager.deleteScheduledJob(endJobName, drTarget.getDrType());
			drTargetManager.scheduleOverrideJobs(endJobName, drTarget.getDrType(), endTriggerName, endDate);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
    	 return "redirect:/dr/listDR.ems";
   }

}
