/**
 * 
 */
package com.ems.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Resource;

import com.ems.server.ServerMain;
import com.ems.vo.MasterGemsSetting;

/**
 * @author Sameer Surjikar
 *
 */
@Resource 


public class CommunicatorServices {

	private static CommunicatorServices instance ;
	
	private CommunicatorServices()
	{
		
		
	}
	public static CommunicatorServices getInstance()
	{
		if (instance == null) {
	            synchronized (CommunicatorServices.class) {
	                if (instance == null) {
	                    instance = new CommunicatorServices();
	                }
	            }
	        }
	        return instance;
	}
	public void setConfiguration(MasterGemsSetting masterGemsSetting)
	{
		
		try {
			String propFilePath = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/urls.properties";
			File f = new File(propFilePath);
			Properties pro = new Properties();
			pro.put("MasterGemsIp", masterGemsSetting.getMasterGemsIp());
			pro.put("MasterGemsPort", masterGemsSetting.getMasterGemsPort());
			pro.put("GemsIp", masterGemsSetting.getEmIp());
			pro.put("GemsPort", masterGemsSetting.getEmPort());
			FileOutputStream outStream = new FileOutputStream(propFilePath);
            pro.store(outStream, "Communicator configuration properties");
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	public MasterGemsSetting loadConfiguration()
	{
		MasterGemsSetting masterGem = null ;
		try {
			String propFilePath = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/urls.properties";
			File f = new File(propFilePath);
			InputStream is = new FileInputStream(propFilePath);
			Properties pro = new Properties();
			pro.load(is);
			 masterGem = new MasterGemsSetting() ;
			masterGem.setEmIp(pro.getProperty("GemsIp"));
			masterGem.setEmPort(pro.getProperty("GemsPort"));
			masterGem.setMasterGemsIp(pro.getProperty("MasterGemsIp"));
			masterGem.setMasterGemsPort(pro.getProperty("MasterGemsPort"));
			return masterGem ;
		} catch (FileNotFoundException e) {		
			return null ;
		} catch (IOException e) {	
			return null ;
		}
	}
}
