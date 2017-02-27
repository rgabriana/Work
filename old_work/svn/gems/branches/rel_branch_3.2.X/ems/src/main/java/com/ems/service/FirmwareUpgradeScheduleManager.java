package com.ems.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.FirmwareUpgradeScheduleDao;
import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.FirmwareUpgradeScheduleList;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.vo.DeviceFirmware;
import com.ems.vo.FirmwareMeta;

@Service("firmwareUpgradeScheduleManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FirmwareUpgradeScheduleManager {
	
	@Resource
	FirmwareUpgradeScheduleDao firmwareUpgradeScheduleDao;

	public List<FirmwareUpgradeSchedule> loadAllFirmwareUpgradeSchedules() {
		return firmwareUpgradeScheduleDao.loadAllFirmwareUpgradeSchedules();
	}
	
	public FirmwareUpgradeScheduleList loadFirmwareUpgradeScheduleList(String orderby,String orderway, int offset, int limit) {
		return firmwareUpgradeScheduleDao.loadFirmwareUpgradeScheduleList(orderby, orderway, offset, limit);
	}
	
	private List<File> unTar(final File inputFile, final File outputDir) {

    System.out.println(String.format("Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

    final List<File> untaredFiles = new LinkedList<File>();
    try {    	
    	final InputStream is = new FileInputStream(inputFile); 
    	final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
    	TarArchiveEntry entry = null; 
    	while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
        final File outputFile = new File(outputDir, entry.getName());
        if (entry.isDirectory()) {
        	System.out.println(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
        	if (!outputFile.exists()) {
        		System.out.println(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
        		if (!outputFile.mkdirs()) {
        			throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
        		}
        	}
        } else {
        	System.out.println(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
        	final OutputStream outputFileStream = new FileOutputStream(outputFile);        	
        	IOUtils.copy(debInputStream, outputFileStream);
          outputFileStream.close();
        }
        untaredFiles.add(outputFile);
    }
    debInputStream.close(); 
    }
    catch(Exception e) {
    	e.printStackTrace();
    }
    return untaredFiles;
    
	} //end of method unTar
	
	public void parseAndAddImagesToDB(String tarFileName) {
				
		String tarFilePath = ImageUpgradeSO.getInstance().getImageLocation() + tarFileName;
		File tarFile = new File(tarFilePath);
		File extractDir = new File(ImageUpgradeSO.getInstance().getImageLocation());
		//extractDir.mkdir();
		List<File> untaredFiles = unTar(tarFile, extractDir);
		Iterator<File> fileIter = untaredFiles.iterator();
		while(fileIter.hasNext()) {
			File extFile = fileIter.next();
			if(extFile.getName().contains(".xml") || extFile.getName().contains(".XML")) {
				//parse the xml file to populate the firmware list in the database
				try { 
					JAXBContext jaxbContext = JAXBContext.newInstance(FirmwareMeta.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					FirmwareMeta meta = (FirmwareMeta) jaxbUnmarshaller.unmarshal(extFile);
					System.out.println("no of firmwares " + meta.getDevices().size());
					Iterator<DeviceFirmware> deviceFirmwares = meta.getDevices().iterator();
					while(deviceFirmwares.hasNext()) {
						DeviceFirmware firmware = deviceFirmwares.next();
						System.out.println("file name -- " + firmware.getImageFile());
						System.out.println("device type -- " + firmware.getType());
						System.out.println("model no " + firmware.getModels());
						System.out.println("version -- " + firmware.getVersion());
						
						StringTokenizer st = new StringTokenizer(firmware.getModels(), ",");
						while(st.hasMoreTokens()) {
							String model = st.nextToken().trim();
							FirmwareUpgradeSchedule schedule = getFirmwareUpgradeSchedule(firmware.getImageFile(), model);
							if(schedule == null) {
								addFirmwareImage(firmware.getImageFile(), firmware.getType(), model, firmware.getVersion(), "");
							} else {
								schedule.setActive(false);
								schedule.setAddedTime(new Date());
								schedule.setDescription("");
								schedule.setFileName(firmware.getImageFile());								
								modifyFirmwareUpgradeSchedule(schedule);
							}
						}						
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				break;
			} else {
				//these are image files copy them to UpgradeImages as well
				System.out.println("file -- " + extFile.getName());
				try {
				copyFileToDirectory(extFile, ImageUpgradeSO.getInstance().getImageLocation());
				}
				catch(IOException ioe) {
					System.out.println("copy file to upgrade images directory - " + ioe.getMessage());
				}
				
			}
		}
		
	} //end of method parseAndAddImagesToDB
	
	private void copyFileToDirectory(File sourceFile, String directoryPath) throws IOException {
	
		FileChannel source = null;
		FileChannel destination = null;
		File destFile = new File(directoryPath + sourceFile.getName());
		System.out.println("dest file -- " + destFile.getAbsolutePath());
		
		try {			
			if(!destFile.exists()) {
	      destFile.createNewFile();
			}		
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
		}		
		finally {
      if(source != null) {
          source.close();
      }
      if(destination != null) {
          destination.close();
      }
		}
		
	} //end of method copyFileToDirectory
	
	public void addFirmwareImage(String imageName, String deviceType, String modelNo, String version, 
			String desc) {
		
		FirmwareUpgradeSchedule schedule = new FirmwareUpgradeSchedule();
		schedule.setActive(false);
		schedule.setAddedTime(new Date());
		schedule.setDescription(desc);
		schedule.setFileName(imageName);
		schedule.setModelNo(modelNo);
		schedule.setDeviceType(deviceType);		
		schedule.setVersion(version);
		schedule.setOnReboot(false);
		schedule.setDuration(0);
		schedule.setScheduledTime(null);
		addFirmwareImage(schedule);
		
	} //end of method addFirmwareImage

	public void addFirmwareImage(FirmwareUpgradeSchedule schedule) {
		
		firmwareUpgradeScheduleDao.addFirmwareUpgradeSchedule(schedule);
    	
	} //end of method addFirmwareScheudle
	
	public FirmwareUpgradeSchedule getFirmwareUpgradeSchedule(String imgName, String model) {
		
		return firmwareUpgradeScheduleDao.getFirmwareUpgradeSchedule(imgName, model);
		
	} //end of method getFirmwareUpgradeSchedule
	
	public List<FirmwareUpgradeSchedule> getFirmwareUpgradeSchedule(String imgName) {
		
		return firmwareUpgradeScheduleDao.getFirmwareUpgradeSchedule(imgName);
		
	} //end of method getFirmwareUpgradeSchedule
    
	public List<FirmwareUpgradeSchedule> getAllActiveFirwareSchedules() {
    	
		return firmwareUpgradeScheduleDao.getAllActiveFirwareSchedules();
		
	} //end of method getAllActiveFirmwareSchedules
	
	public void modifyFirmwareUpgradeSchedule(FirmwareUpgradeSchedule schedule) {
		
		firmwareUpgradeScheduleDao.modifyFirmwareUpgradeSchedule(schedule);
		
	} //end of method modifyFirmwareUpgradeSchedule
    
	public void deleteFirmwareSchedule(Long id) {
    	
		firmwareUpgradeScheduleDao.deleteFirmwareSchedule(id);
		
	} //end of method deleteFirmwareSchedule
    
	public void deactivateFirmwareImage(String fileName, String deviceType, String modelNo) {
		
		firmwareUpgradeScheduleDao.deActivateFirmwareSchedule(fileName, deviceType, modelNo);
		
  } //end of method deActivateFirmwareSchedule
	
	public void activateFirmwareImage(String fileName, String deviceType, String modelNo) {
		
		firmwareUpgradeScheduleDao.activateFirmwareSchedule(fileName, deviceType, modelNo);
		
	} //end of method activateFirmwareImage
	
	public FirmwareUpgradeSchedule getActiveFirmwareScheduleByModelNo(String deviceType,String modelNo) {
		return firmwareUpgradeScheduleDao.getActiveFirmwareScheduleByModelNo(deviceType, modelNo);
		
	}
	
} //end of class FirmwareUpgradeScheduleManager