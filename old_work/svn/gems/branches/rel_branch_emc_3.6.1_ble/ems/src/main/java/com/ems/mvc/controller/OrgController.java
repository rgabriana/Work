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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ems.annotaion.InvalidateFacilityTreeCache;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Floor;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.CompanyManager;
import com.ems.service.FacilitiesManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.Ballasts;
import com.ems.vo.model.Bulbs;
import com.ems.vo.model.FixtureClasses;
import com.ems.vo.model.OrganizationInfo;
import com.ems.vo.model.PlacementInfoVO;
import com.ems.ws.util.Response;

@Controller
@RequestMapping("/org/")
public class OrgController {
	
	
	@Autowired
	UserAuditLoggerUtil userAuditLoggerUtil;
		
	@Resource
	private BulbManager bulbManager;
	
	@Resource(name="facilityTreeManager")
  private FacilityTreeManager facilityTreeManager;
	
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
	
	@InvalidateFacilityTreeCache
	@RequestMapping("/saveFloorUploadFile.ems")
	public String uploadFloorZipFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,
			@RequestParam("fileName") String fileName,
			Locale locale) throws EmsValidationException {
		
		String uploadStatus = "false" ;
		String message = "";
		String tempDirName = "tmp";
		
		try {
			if (file.isEmpty()) {
				return "redirect:/admin/organization/setting.ems?uploadStatus=false";
			}
			uploadFloorZipFile(tempDirName, fileName, file);
			List<String> fileNames = unzip(tempDirName,tempDirName,fileName);
				
			PlacementInfoVO placementInfoVO = null;
			Floor floor = null;
			if(ArgumentUtils.isNullOrEmpty(fileNames)){
				message = "Cannot upload the zip file as devices.xml file is not found.";
				m_Logger.error(message);
				return "redirect:/admin/organization/setting.ems?uploadStatus=false"+"&message="+message;
			}
			if(!fileNames.contains("devices.xml")){
				message = "Cannot upload the zip file as devices.xml file is not found.";
				m_Logger.error(message);
				return "redirect:/admin/organization/setting.ems?uploadStatus=false"+"&message="+message;
			}
			//parse the org facility file
			if(fileNames.contains("orgFacility.xml")){
				Response resp = UploadOrgFacility(tempDirName, fileName, "orgFacility.xml");
				if(resp.getStatus() > 0) {
					message = resp.getMsg();
					m_Logger.error(message);
					return "redirect:/admin/organization/setting.ems?uploadStatus=false"+"&message="+message;
				}
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("Org facility is imported");
			}
			
			File deviceFile = new File(File.separator + tempDirName + File.separator + fileName.substring(0, fileName.length()-4) + File.separator + "devices.xml");  
			JAXBContext jaxbContext;
			try {
				jaxbContext = JAXBContext.newInstance(PlacementInfoVO.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
				placementInfoVO = (PlacementInfoVO) jaxbUnmarshaller.unmarshal(deviceFile); 
				floor = fixtureManager.getFloorOfPlacementInfo(placementInfoVO);
				if(floor == null) {
					//floor does not exists. So,  reject the em config file
					message = "Floor which EmConfig file is pointing to, does not exists.";
					m_Logger.error(message);
					return "redirect:/admin/organization/setting.ems?uploadStatus=false" + "&message="+message;
				}   
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				String msg = "Invalid devices.xml file";
				m_Logger.error(msg) ;
				return "redirect:/admin/organization/setting.ems?uploadStatus=false" + "&message="+message;
			}	
					
			for(String fn : fileNames){
				if("floorplan".equalsIgnoreCase(fn.split("\\.")[0])){
					UploadFloorPlan(tempDirName,fileName,fn,  floor.getId());
				}
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("floor plan is uploaded");
			}		
			
			if(fileNames.contains("bulb_types.xml")){
				UploadBulbData(tempDirName, fileName, "bulb_types.xml");
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("bulbs file is uploaded");
			}	
			
			if(fileNames.contains("ballast_types.xml")){
				UploadBallastData(tempDirName, fileName, "ballast_types.xml");
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("ballasts file is uploaded");
			}	
				
			if(fileNames.contains("fixture_types.xml")){
				UploadFixtureClassData(tempDirName, fileName, "fixture_types.xml");
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("fixture type file is uploaded");
			}	
				
			if(fileNames.contains("devices.xml")){
				fixtureManager.setPlacementInfo(placementInfoVO);
			}
			if(m_Logger.isDebugEnabled()) {
				m_Logger.debug("devices file is uploaded");
			}	
			uploadStatus = "true";
			//facilityTreeManager.inValidateFacilitiesTreeCache();
		} catch (IOException ioe) {
			uploadStatus = "false";
			//ioe.printStackTrace();
		}
		return "redirect:/admin/organization/setting.ems?uploadStatus="+uploadStatus+"&refreshTree=true";
		
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
	
	public Response UploadOrgFacility(String destDir, String zipfileName, String fileName){
		
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + 
				File.separator + fileName);  
		JAXBContext jaxbContext;
		Response resp = null;
		try {
			jaxbContext = JAXBContext.newInstance(OrganizationInfo.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			OrganizationInfo orgInfo = (OrganizationInfo) jaxbUnmarshaller.unmarshal(file); 
	    return facilitiesManager.updateOrgFacility(orgInfo);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			String msg = "Invalid orgFacility.xml file";
			m_Logger.error(msg) ;
			resp = new Response();
			resp.setMsg(msg);
			resp.setStatus(1);
		} catch(Exception ex) {
			String msg = "Problem with the org facilty";
			m_Logger.error(msg, ex);
			resp = new Response();
			resp.setMsg(msg);
			resp.setStatus(1);
		}
		return resp;
		
	}
	
	public void UploadBulbData(String destDir, String zipfileName, String fileName){
		
		File file = new File(File.separator + destDir + File.separator + zipfileName.substring(0, zipfileName.length()-4) + 
				File.separator + fileName);  
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
	
	public void UploadBallastData(String destDir, String zipfileName, String fileName){
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
	
	public void UploadFixtureClassData(String destDir, String zipfileName, String fileName){
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
	
	public void UploadSensorData(String destDir, String zipfileName, String fileName){
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
