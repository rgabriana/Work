package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ems.model.BackUpFile;
import com.ems.server.ServerMain;
import com.ems.utils.AdminUtil;

/**
 * Controller for populating the list of backup files.
 * @author Sharad M
 *
 */

@Controller
@RequestMapping("/restorebackup")
public class RestoreBackupController {
	
	private static String DEFAULT_USB_PATH = "USB1";
	private static String TOMCAT_INSTALLATION_PATH = null;
	
	 @RequestMapping(value = "promptRestoreBackupPage.ems")
		public String backup(Model model) {
			
			TOMCAT_INSTALLATION_PATH = ServerMain.getInstance().getTomcatLocation()+ ".."+File.separator;
			ArrayList<String> usbListing = new ArrayList<String>();
			usbListing = AdminUtil.getMountedUsbSticks();
	
			if (usbListing.size() == 0)
				usbListing.add(DEFAULT_USB_PATH);
	
			List<BackUpFile> filelist = new ArrayList<BackUpFile>();
			
			ArrayList<String> usbFilesString = getUsbFilesString(usbListing);
			if (usbFilesString != null && usbFilesString.size() > 0) {
				BackUpFile bkupFileObj = null;
				for (String fileString : usbFilesString) {
					String[] result = fileString.split("#");
					bkupFileObj = new BackUpFile();
	
					bkupFileObj.setBackupfileSize(((Long)((Long.parseLong(result[0]))/1024)).toString()); // Size
					bkupFileObj.setCreationDate(result[1]); // Timestamp
					bkupFileObj.setBackupfileName(result[2]); // Name
					bkupFileObj.setFilepath(result[3]);
					filelist.add(bkupFileObj);
				}
			}
			if(filelist != null && filelist.size() > 0)
			{
				ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
				HttpSession session = attr.getRequest().getSession(true);
				model.addAttribute("backups", filelist);
				model.addAttribute("securityKey", session.getAttribute("securityKey"));
				model.addAttribute("apacheInstalled", session.getAttribute("apacheInstalled"));
				return "promptRestoreBackupPage";
			}
			else
			{
				return "redirect:/companySetup.ems";
			}
	    	
		}
		public static ArrayList<String> getUsbFilesString(ArrayList<String> usbListing) {
		
			ArrayList<String> fileList = new ArrayList<String>();
			Runtime rt = Runtime.getRuntime();
			Process proc;
			String fullUsbPath = "";
			for (String usbname: usbListing) {
				fullUsbPath = "/media/" + usbname;
				try {
					proc = rt.exec(new String[]{"/bin/bash", "/var/www/em_mgmt/adminscripts/checkandgetbackupfiles.sh",fullUsbPath});
					BufferedReader outputStream = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String output = null;
		
					while ((output = outputStream.readLine()) != null) {
						output += "#" + fullUsbPath;
						fileList.add(output);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			for (String usbname: usbListing) {
				fullUsbPath = "/media/" + usbname + "/dbbackup";
				try {
					proc = rt.exec(new String[]{"/bin/bash", "/var/www/em_mgmt/adminscripts/checkandgetbackupfiles.sh",fullUsbPath});
					BufferedReader outputStream = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String output = null;
		
					while ((output = outputStream.readLine()) != null) {
						output += "#" + fullUsbPath;
						fileList.add(output);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			return fileList;
		}
}
