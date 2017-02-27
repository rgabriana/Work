package com.motion.utils;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

import com.motion.dao.FixtureDao;

public class CommonUtils {

	
	public static final Logger logger = Logger.getLogger(CommonUtils.class
			.getName());
	
	  public static final int byteArrayToShort(byte [] b) {
		    
		    int l = 0;
		    l |= b[0] & 0xff;
		    l  = (l<< 8) | b[1] & 0xff;
		    return l;
		    
		  }
	  
	//it expects the length of array to be 4
	  public static final int byteArrayToInt(byte [] b) {
	    
	    int l = 0;
	    l |= b[0] & 0xff;
	    l  = (l<<8) | b[1] & 0xff;
	    l  = (l<<8) | b[2] & 0xff;
	    l  = (l<<8) | b[3] & 0xff;
	    return l;
	    
	  } //end of method byteArrayToInt
	  
	  public static String randomColorNameGenerator()
	  {
		  String[] arr = {"Red","Blue","Green" , "Yellow" , "Pink" ,"Voilet" , "Black" } ;
		  Random randomGenerator = new Random();
		  int index = randomGenerator.nextInt(7);
		  return arr[index] ;
		  
	  }

	 
	  public static String getPropertyWithName(String PropertyName,
				String filePath) {
		  String result = null ;
			Properties mainProperties = new Properties();
			FileInputStream file;
			String path = filePath;
			try {
				file = new FileInputStream(path);
				mainProperties.load(file);
				logger.debug("Property "+PropertyName+" values is :- " +mainProperties.getProperty(PropertyName)) ;
				result =mainProperties.getProperty(PropertyName);
				file.close();			
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			return result ;
		}
	  
}
