package com.emsmgmt.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.emsmgmt.model.BackUpFile;
import com.emsmgmt.model.UpgradeFile;
import com.emsmgmt.utils.AdminPageUtils;

@Controller
public class ApplicationEntryPointController extends
		WebApplicationObjectSupport {

	@RequestMapping(value = "/home.emsmgmt")
	public String entryPoint() {
		return "home";
	}

	private static String DEFAULT_USB_PATH = "USB1";
	private static String TOMCAT_INSTALLATION_PATH = null;
	private static String UPGRADE_PATH = null;

	private static final String BACKUP_SYSTEM_PATH = "/opt/enLighted/DB/DBBK";

	private int UPGRADE_DB_STARTED = 9;
	private int DEPLOY_EMS_STARTED = 11;

	@RequestMapping(value = "/backup.emsmgmt")
	public String backup(@RequestParam(value = "usbMountPoint", required = false) String usbMountPoint,	Model model) {

		ServletContext context = this.getWebApplicationContext().getServletContext();
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
		TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);

		ArrayList<String> usbListing = new ArrayList<String>();
		if (usbMountPoint != null && !usbMountPoint.isEmpty() && !usbMountPoint.equalsIgnoreCase(""))
			usbListing.add(usbMountPoint);
		else
			usbListing = AdminPageUtils.getMountedUsbSticks();

		if (usbListing.size() == 0)
			usbListing.add(DEFAULT_USB_PATH);

		List<BackUpFile> filelist = new ArrayList<BackUpFile>();
		ArrayList<String> usbFilesString = getUsbFilesString(usbListing);
		if (usbFilesString != null && usbFilesString.size() > 0) {
			BackUpFile bkupFileObj = null;
			for (int i = 0; i < usbFilesString.size(); i++) {
				String fileString = usbFilesString.get(i);
				String[] result = fileString.split("#");
				bkupFileObj = new BackUpFile();

				bkupFileObj.setBackupfileSize(result[0]); // Size
				bkupFileObj.setCreationDate(result[1]); // Timestamp
				bkupFileObj.setBackupfileName(result[2]); // Name
				bkupFileObj.setFilepath(result[3]);

				filelist.add(bkupFileObj);
			}
		}

		File systemHomeDirectory = new File(BACKUP_SYSTEM_PATH);
		BackUpFile singleFileObj = null;
		// Traverse the home directory
		if (systemHomeDirectory.isDirectory()) {
			for (File child : systemHomeDirectory.listFiles()) {
				if (".".equals(child.getName()) || "..".equals(child.getName())
						|| child.isHidden()
						|| child.getName().indexOf(".tar.gz") == -1) {
					continue;
				} else {
					singleFileObj = getFileObject(child);
					filelist.add(singleFileObj);
				}
			}
		}
		model.addAttribute("backups", filelist);
		model.addAttribute("backUpFile", new BackUpFile());
		return "backup";
	}

	private BackUpFile getFileObject(File singleFile) {

		BackUpFile bfl = new BackUpFile();

		Date d = new Date(singleFile.lastModified());
		String formattedDate = DateUtil.formatDate(d, "yyyy-MM-dd HH:mm");

		bfl.setCreationDate(formattedDate);
		bfl.setFilepath(singleFile.getParentFile().getAbsolutePath());
		bfl.setBackupfileName(singleFile.getName().trim());
		bfl.setBackupfileSize((singleFile.length() / 1024) + "");

		return bfl;
	}

	private ArrayList<String> getUsbFilesString(ArrayList<String> usbListing) {

		ArrayList<String> fileList = new ArrayList<String>();
		Runtime rt = Runtime.getRuntime();
		Process proc;
		String fullUsbPath = "";
		for (int i = 0; i < usbListing.size(); i++) {
			fullUsbPath = "/media/" + usbListing.get(i) + "/dbbackup";
			String scriptExecCommand = "/bin/bash " + TOMCAT_INSTALLATION_PATH
					+ "/ems_mgmt/adminscripts/checkandgetbackupfiles.sh" + " "
					+ fullUsbPath;

			try {
				proc = rt.exec(scriptExecCommand);
				BufferedReader outputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String output = null;

				while ((output = outputStream.readLine()) != null) {
					output += "#" + fullUsbPath;
					fileList.add(output);
				}
				if (fileList.size() == 0) {
					return null;
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return null;
			}
		}
		return fileList;
	}

	@RequestMapping(value = "/uploadBackupFile.emsmgmt", method = { RequestMethod.POST })
	public String uploadBackupFile(BackUpFile uploadFile) {
		
		//ServletContext context = this.getWebApplicationContext().getServletContext();
		
		if (uploadFile.getFileData() != null) {
			
			//request.getSession().setAttribute("uploadFileSize", uploadFile.getFileData().getSize());
			//request.getSession().setAttribute("uploadFilePath", ((DiskFileItem)uploadFile.getFileData().getFileItem()).getStoreLocation().getAbsolutePath());
			//context.setAttribute("uploadFileSize", uploadFile.getFileData().getSize());
			//context.setAttribute("uploadFilePath", ((DiskFileItem)uploadFile.getFileData().getFileItem()).getStoreLocation().getAbsolutePath());
			//System.out.println(context.getAttribute("uploadFileSize").toString() + "  " + context.getAttribute("uploadFilePath").toString());
			//System.out.println(request.getSession().getAttribute("uploadFileSize").toString() + "  " + request.getSession().getAttribute("uploadFilePath").toString());
			File backupDir = new File(BACKUP_SYSTEM_PATH + "/"
					+ uploadFile.getFileData().getOriginalFilename());
			try {
				uploadFile.getFileData().transferTo(backupDir);
			} catch (IOException e) {
				e.printStackTrace();
				return "redirect:backup.emsmgmt?uploadStatus='F'";
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return "redirect:backup.emsmgmt?uploadStatus='F'";
			}

		} else {
			return "redirect:backup.emsmgmt?uploadStatus='F'";
		}

		return "redirect:backup.emsmgmt?uploadStatus='S'&filename='"
				+ uploadFile.getFileData().getOriginalFilename() + "'";
	}

	@RequestMapping(value = "/gemsupgrade.emsmgmt")
	public String gemsupgrade(Model model) {
		ServletContext context = this.getWebApplicationContext()
				.getServletContext();
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
		TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
		UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";
		String script_location = TOMCAT_INSTALLATION_PATH
				+ "/ems_mgmt/adminscripts/debian_upgrade.sh";

		int versionNumber = 0;
		File upgradeSystemFolder = new File(UPGRADE_PATH);
		List<UpgradeFile> upgradeFileList = new ArrayList<UpgradeFile>();
		// Traverse the home directory
		if (upgradeSystemFolder.isDirectory()) {
			for (File child : upgradeSystemFolder.listFiles()) {
				if (child.getName().indexOf(".deb") != -1) {
					// versionNumber=Integer.parseInt(AdminPageUtils.compatibleVersion(child,UPGRADE_PATH,this.context,"upgrade"));
					logger.info("listUpgradeFiles(),Version number: "
							+ versionNumber);
					if (".".equals(child.getName())
							|| "..".equals(child.getName()) || child.isHidden()) {
						continue;
					} else {
						UpgradeFile upgradeObj = getFileObject(child,
								versionNumber);
						upgradeFileList.add(upgradeObj);
					}
				}
			}
		}
		int previousUpgradeState = checkPreviousUpgradeSuccess();
		logger.info("Previous state..." + previousUpgradeState);

		AdminThread at = new AdminThread(TOMCAT_INSTALLATION_PATH,
				script_location);
		Thread th = new Thread(at);

		logger.info("Starting new thread " + th.getId());
		if (previousUpgradeState == UPGRADE_DB_STARTED) {
			// addActionError("Database was not upgraded properly..Reverting back");
			th.start();
		} else if (previousUpgradeState == DEPLOY_EMS_STARTED) {
			// addActionError("Application was not upgraded properly..Reverting back");
			th.start();
		}
		model.addAttribute("fileList", upgradeFileList);
		model.addAttribute("upgradeFile", new UpgradeFile());
		return "gemsupgrade";
	}

	private UpgradeFile getFileObject(File singleFile, int versionNumber) {
		UpgradeFile ufl = new UpgradeFile();

		Date d = new Date(singleFile.lastModified());
		String formattedDate = DateUtil.formatDate(d, "yyyy-MM-dd HH:mm:ss");

		ufl.setCreationDate(formattedDate);
		ufl.setUpgradeFileName(singleFile.getName().trim());
		ufl.setVersion(versionNumber);
		ufl.setUpgradeFileSize((singleFile.length() / 1024) + "");

		return ufl;
	}

	private int checkPreviousUpgradeSuccess() {

		File restorelogfile = new File(UPGRADE_PATH + "/admin_process_state");
		String outputLine = null;
		int return_state = 0;
		try {
			logger.info("Fetching state from "
					+ restorelogfile.getAbsolutePath());
			if (restorelogfile.exists()) {
				FileInputStream fis = new FileInputStream(restorelogfile);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));

				while ((outputLine = br.readLine()) != null) {
					logger.info("checkPreviousUpgradeSuccess():" + outputLine);
					return_state = Integer.parseInt(outputLine.trim());
				}
				br.close();
				fis.close();
			}
		} catch (IOException ioe) {
			logger.error("checkPreviousUpgradeSuccess():IOException", ioe);
		}

		return return_state;
	}
	
	
	@RequestMapping(value = "/uploadImageFile.emsmgmt", method = { RequestMethod.POST })
	public String uploadImageFile(UpgradeFile uploadFile) {

		if (uploadFile.getFileData() != null) {
			ServletContext context = this.getWebApplicationContext()
					.getServletContext();
			int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
			TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
			UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";

			File upgradeDirFilePath = new File(UPGRADE_PATH + "/"
					+ uploadFile.getFileData().getOriginalFilename());
			 File upgradeDir                 = new File(UPGRADE_PATH);
			try {
				if(!upgradeDir.exists())
                    upgradeDir.mkdir();
				uploadFile.getFileData().transferTo(upgradeDirFilePath);
			} catch (IOException e) {
				e.printStackTrace();
				return "redirect:gemsupgrade.emsmgmt?uploadStatus='F'";
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return "redirect:gemsupgrade.emsmgmt?uploadStatus='F'";
			}

		} else {
			return "redirect:gemsupgrade.emsmgmt?uploadStatus='F'";
		}

		return "redirect:gemsupgrade.emsmgmt?uploadStatus='S'&filename='"
				+ uploadFile.getFileData().getOriginalFilename() + "'";
	}
}

class AdminThread implements Runnable {

	private static String TOMCAT_INSTALLATION_PATH = "";
	private static String SCRIPT_LOCATION = "";
	static final Logger logger = Logger.getLogger("GemsUpgrade");

	public AdminThread(String tomcat_path, String script_path) {
		TOMCAT_INSTALLATION_PATH = tomcat_path;
		SCRIPT_LOCATION = script_path;
	}

	public void run() {
		revertBack();
	}

	private void revertBack() {

		String scriptExecCommand = "/bin/bash " + SCRIPT_LOCATION
				+ " dummy revert " + TOMCAT_INSTALLATION_PATH;
		logger.info("revertBack(): Script being called now : "
				+ scriptExecCommand);
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(scriptExecCommand);
			proc.waitFor();
		} catch (IOException ioe) {
			logger.error("revertBack(): Exception:" + ioe);
		} catch (Throwable th) {
			logger.error("revertBack(): Exception:" + th);
		}
	}
}
