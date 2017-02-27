package com.emsmgmt.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

	private static String DEFAULT_USB_PATH = "USB1";
	private static String TOMCAT_INSTALLATION_PATH = null;
	private static String UPGRADE_PATH = null;

	private static final String BACKUP_SYSTEM_PATH = "/opt/enLighted/DB/DBBK";
	
    @RequestMapping(value = "/navigatePage.emsmgmt", method = {RequestMethod.POST})
    public String navigateToPage(@RequestParam("page") String page, @RequestParam("code1") String code1, @RequestParam("code2") String code2, HttpServletRequest req) {
    	//System.out.println(page + "  " + code1 + "  " + code2);
    	HttpSession session = req.getSession(true);
    	if(session != null && code1 != null && code2 != null) {
    		try {
				code1 = hash(code1);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		//System.out.println(code1);
    		if(code1.equals(code2)) {
    			session.setAttribute("isAuthenticated", "Y");
    			if("backup".equals(page)) {
    				return "redirect:/backup.emsmgmt";
    			}
    			if("gemsupgrade".equals(page)) {
    				return "redirect:/gemsupgrade.emsmgmt";
    			}
    		}
    	}
        return "redirect:/error.emsmgmt";
    }
    
    
	private String hash(String clear) throws Exception {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(clear.getBytes());

		int size = b.length;
		StringBuffer h = new StringBuffer(size);
		for (int i = 0; i < size; i++) {
			int u = b[i]&255; // unsigned conversion
			if (u<16) {
				h.append("0"+Integer.toHexString(u));
			} else {
				h.append(Integer.toHexString(u));
			}
		}
		return h.toString();
		} catch (Exception e) {
			throw new Exception(e);	
		}
	}
    
    @RequestMapping(value = "/error.emsmgmt")
    public String errorPage() {
    	return "error";
    }

	@RequestMapping(value = "/backup.emsmgmt")
	public String backup(@RequestParam(value = "usbMountPoint", required = false) String usbMountPoint,	Model model, HttpServletRequest req) {
		
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
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
    	return "redirect:error.emsmgmt";
	}

	private BackUpFile getFileObject(File singleFile) {

		BackUpFile bfl = new BackUpFile();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date d = new Date(singleFile.lastModified());
		bfl.setCreationDate(sdf.format(d));
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
		for (String usbname: usbListing) {
			fullUsbPath = "/media/" + usbname;
			try {
				proc = rt.exec(new String[]{"/bin/bash", TOMCAT_INSTALLATION_PATH + "/ems_mgmt/adminscripts/checkandgetbackupfiles.sh",fullUsbPath});
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
				proc = rt.exec(new String[]{"/bin/bash", TOMCAT_INSTALLATION_PATH + "/ems_mgmt/adminscripts/checkandgetbackupfiles.sh",fullUsbPath});
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

	@RequestMapping(value = "/uploadBackupFile.emsmgmt", method = { RequestMethod.POST })
	public String uploadBackupFile(BackUpFile uploadFile, HttpServletRequest req) {
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
			if (uploadFile.getFileData() != null) {
				
				//System.out.println(((DiskFileItem)uploadFile.getFileData().getFileItem()).getStoreLocation().getAbsolutePath() + " " + uploadFile.getFileData().getSize());
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
    	return "redirect:error.emsmgmt";
	}

	@RequestMapping(value = "/gemsupgrade.emsmgmt")
	public String gemsupgrade(Model model, HttpServletRequest req) {
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
			ServletContext context = this.getWebApplicationContext()
					.getServletContext();
			int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
			TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
			
			UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";
	
			int versionNumber = 0;
			File upgradeSystemFolder = new File(UPGRADE_PATH);
			List<UpgradeFile> upgradeFileList = new ArrayList<UpgradeFile>();
			// Traverse the home directory
			if (upgradeSystemFolder.isDirectory()) {
				for (File child : upgradeSystemFolder.listFiles()) {
					if (child.getName().indexOf(".deb") != -1) {
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
			
			
			model.addAttribute("fileList", upgradeFileList);
			model.addAttribute("upgradeFile", new UpgradeFile());
			return "gemsupgrade";
    	}
    	return "redirect:error.emsmgmt";
	}

	private UpgradeFile getFileObject(File singleFile, int versionNumber) {
		UpgradeFile ufl = new UpgradeFile();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date(singleFile.lastModified());
		ufl.setCreationDate(sdf.format(d));
		ufl.setUpgradeFileName(singleFile.getName().trim());
		ufl.setVersion(versionNumber);
		ufl.setUpgradeFileSize((singleFile.length() / 1024) + "");

		return ufl;
	}
	
	
	@RequestMapping(value = "/uploadImageFile.emsmgmt", method = { RequestMethod.POST })
	public String uploadImageFile(UpgradeFile uploadFile, HttpServletRequest req) {
		
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {

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
    	return "redirect:error.emsmgmt";
	}
}

