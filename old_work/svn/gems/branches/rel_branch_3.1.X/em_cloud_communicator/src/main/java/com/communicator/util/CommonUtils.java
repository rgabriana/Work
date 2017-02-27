package com.communicator.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class CommonUtils {
	static final Logger logger = Logger.getLogger(CommonUtils.class.getName());
	
	
	public static void updatePropertyWithValue(String propertyName , String propertyValue , String filePath)
	{
		  FileInputStream in=null;
		  FileOutputStream out =null;
		  if(new File(filePath).exists()){
		try {
			in = new FileInputStream(filePath);
			Properties props = new Properties();
	        props.load(in);
	        out = new FileOutputStream(filePath);
	        props.setProperty(propertyName, propertyValue);
	        props.store(out, null);
	       
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} finally
		{
			 try {
				 in.close();
				 out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		  }else
		  {
			  logger.error("File "+filePath +" does not exist. Failed to update");
		  }
	        
	}
	

	  public static String getPropertyWithName(String PropertyName,
				String filePath) {
		  String result = null ;
		  if(new File(filePath).exists()){
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
			return result.trim() ;
		  }else
		  {
			  logger.error("File "+filePath +" does not exist");
			  return null;
		  }
			
		}
	  
	  
	  public static Properties getPropertiesMap(String filePath) {
		  if(new File(filePath).exists()){
			Properties mainProperties = new Properties();
			FileInputStream file;
			String path = filePath;
			try {
				file = new FileInputStream(path);
				mainProperties.load(file);
				
				file.close();			
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			return mainProperties;
		  }else
		  {
			  logger.error("File "+filePath +" does not exist");
			  return null;
		  }
			
		}
	  
	  static public void zipFolder(String srcFolder, String destZipFile) throws Exception {
		    ZipOutputStream zip = null;
		    FileOutputStream fileWriter = null;

		    fileWriter = new FileOutputStream(destZipFile);
		    zip = new ZipOutputStream(fileWriter);

		    addFolderToZip("", srcFolder, zip);
		    zip.flush();
		    zip.close();
		  }

		  static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
		      throws Exception {

		    File folder = new File(srcFile);
		    if (folder.isDirectory()) {
		      addFolderToZip(path, srcFile, zip);
		    } else {
		      byte[] buf = new byte[1024];
		      int len;
		      FileInputStream in = new FileInputStream(srcFile);
		      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
		      while ((len = in.read(buf)) > 0) {
		        zip.write(buf, 0, len);
		      }
		    }
		  }

		  static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
		      throws Exception {
		    File folder = new File(srcFolder);

		    for (String fileName : folder.list()) {
		      if (path.equals("")) {
		        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
		      } else {
		        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
		      }
		    }
		  }
		  
	 public static void executeShellCmd(String cmd)
	{
			  Runtime run = Runtime.getRuntime();
				try {
					Process pr = run.exec(cmd);
					BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					String line = "";
					while ((line=buf.readLine())!=null) {
						System.out.println(line);
						logger.info(line);
					}
				    pr.waitFor();
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}catch (Exception e)
				{
					logger.error(e.getMessage());
				}
	}
	
	public static void executeListOfShellCmd(ArrayList<String> cmdList)
	{
		try{
		File wd = new File("/bin");
		Runtime rt = Runtime.getRuntime();
		Process proc;
		PrintWriter out  =null ;
		proc = rt.exec("/bin/bash", null , wd);
		if (proc != null) {
			   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
			   for(String cmd : cmdList)
			   {
				   out.println(cmd);
			   }
			   out.println("exit");
			   // Do not try to read inputstream or errorstream as they keep the process blocked
				 // for cmd that are fired as background process.
			      proc.waitFor();
			      out.close();
			      proc.destroy();
			   }
		}catch(Exception e)
		{
			logger.info(e.getMessage());
		}
		
	}
	 
	 
}


