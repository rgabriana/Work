package com.ems.mvc.controller;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.model.Ballast;
import com.ems.model.Building;
import com.ems.model.Bulb;
import com.ems.model.Campus;
import com.ems.model.FixtureClass;
import com.ems.model.Floor;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.CompanyManager;
import com.ems.service.FacilitiesManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.Ballasts;
import com.ems.vo.model.Bulbs;
import com.ems.vo.model.FixtureClasses;
import com.ems.vo.model.PlacementInfoVO;

@Controller
@RequestMapping("/floor/")
public class FloorController {
	
	
	@Autowired
	UserAuditLoggerUtil userAuditLoggerUtil;
		
	@Resource
	private BulbManager bulbManager;
	
	@Resource
	private BallastManager ballastManager;
	
	@Resource
	private FixtureClassManager fixtureClassManager;
	
	@Resource
	private FixtureManager fixtureManager;
	
	@Resource
	private FacilitiesManager facilitiesManager;
	
	@Resource
	private FloorManager floorManager;
	
	@Resource
	private CompanyManager companyManager;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	/**
	 * @param model
	 *       used in communicating back
	 **/
	/*@RequestMapping(value = "/manage.ems", method = RequestMethod.GET)
	public String manageFloorUpload(HttpServletRequest request,Model model,@RequestParam(value = "uploadStatus", required = false) String uploadStatus,
			@RequestParam(value = "message", required = false) String message) {
		model.addAttribute("uploadStatus", uploadStatus);
		model.addAttribute("message", message);
		return "floor/upload";
	}*/
	
	
	@RequestMapping("/saveFloorUploadFile.ems")
	public String uploadFloorZipFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			@RequestParam("fileName") String fileName, Model model,@CookieValue(FacilityCookieHandler.selectedFacilityCookie) String cookie,
			Locale locale) throws EmsValidationException {
		
		String uploadStatus = "false" ;
		
		String message = "";
		
		/*Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("emConfig.uploadZipFile", file);
		nameValMap.put("emConfig.uploadZipFileName", fileName);
		CommonUtils.isParamValueAllowedAndThrowException(messageSource,
				systemConfigurationManager, nameValMap);*/
		
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(cookie);
		Long floorId = cookieHandler.getFacilityId();
		
		String tempDirName = "tmp";
		
		try {
			
			if (!file.isEmpty()) {
				uploadFloorZipFile(tempDirName, fileName, file);
				List<String> fileNames = unzip(tempDirName,tempDirName,fileName);
				
				if(ArgumentUtils.isNullOrEmpty(fileNames)){
					uploadStatus = "false";
					message = "Cannot upload the zip file as devices.xml file is not found.";
					return "redirect:/admin/organization/setting.ems?uploadStatus="+uploadStatus+"&message="+message;
				}else{
					if(!fileNames.contains("devices.xml")){
						uploadStatus = "false";
						message = "Cannot upload the zip file as devices.xml file is not found.";
						return "redirect:/admin/organization/setting.ems?uploadStatus="+uploadStatus+"&message="+message;
					}
				}
							
				
				for(String fn : fileNames){
					if("floorplan".equalsIgnoreCase(fn.split("\\.")[0])){
						UploadFloorPlan(tempDirName,fileName,fn,floorId);
					}
				}
								
				if(fileNames.contains("bulb_types.xml")){
					UploadBulbData(tempDirName, fileName,fileNames,"bulb_types.xml");
				}
				
				if(fileNames.contains("ballast_types.xml")){
					UploadBallastData(tempDirName, fileName,fileNames,"ballast_types.xml");
				}
				
				if(fileNames.contains("fixture_types.xml")){
					UploadFixtureClassData(tempDirName, fileName,fileNames,"fixture_types.xml");
				}
				
				if(fileNames.contains("devices.xml")){
					UploadSensorData(tempDirName, fileName,fileNames,"devices.xml");
				}
								
				uploadStatus = "true";
			} else {
				uploadStatus = "false";
			}
		} catch (IOException ioe) {
			uploadStatus = "false";
			//ioe.printStackTrace();
		}
		
		return "redirect:/admin/organization/setting.ems?uploadStatus="+uploadStatus;
	}
	
	public void uploadFloorZipFile(String path, String fileName, MultipartFile file) throws IOException{
		File imageStore = new File(path);
		if(!imageStore.exists()){
			imageStore.mkdirs();
		}
		
		File image=new File(File.separator + path + File.separator, fileName);
		
		byte[] bytes = file.getBytes();
		FileOutputStream fos = new FileOutputStream(image);
		fos.write(bytes);
		fos.flush();
		fos.close();
	}
	
	
	
	public List<String> unzip(String zipFilePath, String destDir ,String zipfileName) throws IOException {
		List<String> fileNames = new ArrayList<String>();
        //File dir = new File(destDir + File.separator + zipfileName.substring(0, zipfileName.length() - 4));
        File dir = new File(File.separator + destDir + File.separator);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        //try {
            fis = new FileInputStream(File.separator + zipFilePath + File.separator + zipfileName);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                fileNames.add(fileName);
                //File newFile = new File(destDir + File.separator + zipfileName.substring(0, zipfileName.length() - 4) + File.separator + fileName);
                File newFile = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);
                //System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                	fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
       // } catch (IOException e) {
            //e.printStackTrace();
       // }
		return fileNames;
    }
	
	
	
	public void UploadBulbData(String destDir, String zipfileName,List<String> fileNames,String fileName){
		
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);  
        JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Bulbs.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
	        Bulbs bulbs = (Bulbs) jaxbUnmarshaller.unmarshal(file); 
	       
	        bulbManager.uploadBulbList(bulbs.getBulbs());
	        
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_Logger.error("Invalid bulb_types.xml file") ;
		}  
	}
	
	public void UploadBallastData(String destDir, String zipfileName,List<String> fileNames,String fileName){
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);  
        JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Ballasts.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			Ballasts ballasts = (Ballasts) jaxbUnmarshaller.unmarshal(file); 
	        ballastManager.uploadBallastList(ballasts.getBallasts());
	        
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_Logger.error("Invalid ballast_types.xml file") ;
		}
	}
	
	public void UploadFixtureClassData(String destDir, String zipfileName,List<String> fileNames,String fileName){
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);  
        JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(FixtureClasses.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			FixtureClasses fixtureClasses = (FixtureClasses) jaxbUnmarshaller.unmarshal(file); 
	        
	        fixtureClassManager.uploadFixtureClass(fixtureClasses.getFixtureClasses());
	        
	        
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_Logger.error("Invalid fixture_types.xml file") ;
		}
	}
	
	public void UploadSensorData(String destDir, String zipfileName,List<String> fileNames,String fileName){
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);  
        JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(PlacementInfoVO.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			PlacementInfoVO placementInfoVO = (PlacementInfoVO) jaxbUnmarshaller.unmarshal(file); 
	        
	        fixtureManager.setPlacementInfo(placementInfoVO);
	        
	        
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_Logger.error("Invalid devices.xml file") ;
		}
	}
	
	public void UploadFloorPlan(String destDir, String zipfileName,String fileName,Long floorId){
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + File.separator + fileName);
		
		Floor floor;
		String floorName= "";
		String bldgName ="";
		String campusName = "";
		String companyName = "";
		try {
			floor = floorManager.getFloorById(floorId);
			
			floorName = floor.getName();
			
			Building building = floor.getBuilding();
			
			bldgName = building.getName();
			
			Campus campus = building.getCampus();
			
			campusName = campus.getName();
			
			companyName = companyManager.getCompany().getName();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
								
		int index = fileName.lastIndexOf('.');
    	String contentType = "image/jpeg";

    	if(index != -1)
    	{
    		String extension = fileName.substring(index + 1);

    		if(extension.equalsIgnoreCase("PNG"))
    			contentType = "image/png";
    	}
		
		byte[] imageData = null;
		
		BufferedImage image;
		try {
			image = ImageIO.read(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if(contentType.contains("jpeg"))
                ImageIO.write(image, "jpeg", baos);
            else
            	ImageIO.write(image, "png", baos);
            baos.flush();
            imageData = baos.toByteArray();
            baos.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			m_Logger.error("Invalid floorplan file") ;
		}
       
		
		try {
			facilitiesManager.setFloorPlan(companyName, campusName, bldgName, floorName, fileName, imageData);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_Logger.error("Invalid floorplan file") ;
		}
	}

}
