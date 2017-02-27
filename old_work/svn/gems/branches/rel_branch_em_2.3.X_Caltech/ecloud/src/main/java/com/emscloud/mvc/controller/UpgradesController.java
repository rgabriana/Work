package com.emscloud.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.communication.types.DebianType;
import com.emscloud.model.UpgradeFile;
import com.emscloud.model.Upgrades;
import com.emscloud.service.UpgradesManager;




@Controller
@RequestMapping("/upgrades")
public class UpgradesController extends WebApplicationObjectSupport {

	private static String TOMCAT_INSTALLATION_PATH = null;
	private static String UPGRADE_PATH = null;
	
	@Resource
	UpgradesManager upgradesManager;
	
	@RequestMapping(value = "/list.ems", method = { RequestMethod.GET, RequestMethod.POST })
	public String listUpgrades(Model model)
	{			
		ServletContext context = this.getWebApplicationContext()
				.getServletContext();
		
		/*int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
		String contextPath = context.getContextPath();
		String path= context.getRealPath("/");
		TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
		
		UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";
		String myPath = "C:\\Users\\admin\\workspace2\\trunk\\ecloud\\src\\main\\webapp\\";
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
		
		model.addAttribute("fileList", upgradeFileList);*/
		model.addAttribute("upgradeFile", new UpgradeFile());
		
		return "upgrades/list";
	}
	
	@RequestMapping(value = "/uploadImageFile.ems", method = { RequestMethod.POST })
	public String uploadImageFile(UpgradeFile uploadFile, HttpServletRequest req) {	
		
		ServletContext context = this.getWebApplicationContext()
		.getServletContext();
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
		TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
		String UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";

			if (uploadFile.getFileData() != null) {			
	
				File upgradeDirFilePath = new File(UPGRADE_PATH + "/"
						+ uploadFile.getFileData().getOriginalFilename());
				File upgradeDir                 = new File(UPGRADE_PATH);
				try {
					if(!upgradeDir.exists())
					{
	                    upgradeDir.mkdir();	
					}
					uploadFile.getFileData().transferTo(upgradeDirFilePath);
					Upgrades mUpgrade = new Upgrades();
					mUpgrade.setLocation(upgradeDirFilePath.toString());
					mUpgrade.setType(DebianType.EM.getName());
					mUpgrade.setName(uploadFile.getFileData().getOriginalFilename());
					upgradesManager.saveOrUpdate(mUpgrade);
				} catch (IOException e) {
					e.printStackTrace();
					return "redirect:/upgrades/list.ems";
				} catch (IllegalStateException e) {
					e.printStackTrace();
					return "redirect:/upgrades/list.ems";
				}
	
			} else {
				return "redirect:/upgrades/list.ems";
			}
	
			return "redirect:/upgrades/list.ems";
					
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

}
