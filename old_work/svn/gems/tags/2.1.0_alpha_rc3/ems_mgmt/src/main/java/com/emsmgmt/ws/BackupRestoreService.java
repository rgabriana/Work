package com.emsmgmt.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.context.support.WebApplicationObjectSupport;


@Controller
@Path("/org")
public class BackupRestoreService extends WebApplicationObjectSupport {
	
	private static boolean adminProcessRunning =  false;
	
	private static boolean isAdminProcessRunning() {
		return adminProcessRunning;
	}
	
	private synchronized static void setAdminProcessRunning(boolean adminProcessRunning) {
		BackupRestoreService.adminProcessRunning = adminProcessRunning;
	}
	
	@Path("/backup/create/{filename}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String startBackup(@PathParam("filename") String inputFileName) {
		String filename = inputFileName.trim();
		boolean alreadyRunningError = false;
		
		ServletContext context = this.getWebApplicationContext().getServletContext();
		
		String SCRIPT_LOCATION = context.getRealPath("/")+"adminscripts/backuprestoreguiaction.sh";
		String BACKUP_SYSTEM_PATH = "/opt/enLighted/DB/DBBK";
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());  
		String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0,cp);
		
		File systemHomeDirectory =new File(BACKUP_SYSTEM_PATH);
		
		try {
			if(!isAdminProcessRunning()) {
				//script <operation> <filename> <backupdirectoryonsystem> <tomcat install dir>
				String scriptExecCommand ="/bin/bash " + SCRIPT_LOCATION + " backup " + URLDecoder.decode(filename, "UTF-8") +" "+ 
						systemHomeDirectory +" "+TOMCAT_INSTALLATION_PATH;
				setAdminProcessRunning(true);
				Runtime rt = Runtime.getRuntime();
				//Start the process
				Process proc = rt.exec(scriptExecCommand);
				//Process proc = rt.exec("sleep 5");
				proc.waitFor();		
			}
			else {
				alreadyRunningError  = true;
				return "ALREADY_RUNNING";
			}
		} 
		catch(InterruptedException e) {
			e.printStackTrace();
			return "FAILURE";
		}
		catch(IOException e) {
			e.printStackTrace();
			return "FAILURE";
		}
		finally {
			//ADMIN_PROCESS = false;
			if(!alreadyRunningError) {
				setAdminProcessRunning(false);
			}
		}
		return "SUCCESS";
	}
	
	
	@Path("/backup/restore/{filename}/{filepath}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String restoreBackUpFile(@PathParam("filename") String filename, @PathParam("filepath") String filepath) {

		filepath = filepath.replaceAll("=", "/");
		boolean alreadyRunningError = false;
		
		ServletContext context = this.getWebApplicationContext().getServletContext();
		
		String SCRIPT_LOCATION = context.getRealPath("/")+"adminscripts/backuprestoreguiaction.sh";
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());  
		String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0,cp);
		


		
		try {
			if(!isAdminProcessRunning()) {
				String scriptExecCommand = "/bin/bash "+ SCRIPT_LOCATION + " " + "dorestore" +" " + URLDecoder.decode(filename, "UTF-8") +" "+filepath+" "+
		                TOMCAT_INSTALLATION_PATH;
				setAdminProcessRunning(true);
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(scriptExecCommand);
				proc.waitFor();
			}
			else {
				alreadyRunningError  = true;
				return "ALREADY_RUNNING";
			}
		} 
		catch(InterruptedException e) {
			e.printStackTrace();
			return "FAILURE";
		}
		catch(IOException e) {
			e.printStackTrace();
			return "FAILURE";
		}
		finally {
			//ADMIN_PROCESS = false;
			if(!alreadyRunningError) {
				setAdminProcessRunning(false);
			}
		}
		return "SUCCESS";
	}
	

	@Path("/backup/delete/{filename}/{filepath}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String deleteBackUpFile(@PathParam("filename") String filename, @PathParam("filepath") String filepath) {

		filepath = filepath.replaceAll("=", "/");
        try {
            File backupDir                  = new File(filepath+"/"+URLDecoder.decode(filename, "UTF-8"));
            boolean delSuccess = backupDir.delete();
            if(!delSuccess) {
            	return "F";
            }
        }
        catch(SecurityException secex) {
           secex.printStackTrace();
           return "I";
        }
        catch(Throwable th) {
            th.printStackTrace();
            return "I";
        }
        return "S";
	}
	
	@Path("/upgrade/delete/{filename}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String deleteUpgradeFile(@PathParam("filename") String filename) {

		ServletContext context = this.getWebApplicationContext()
				.getServletContext();
		int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());
		String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0, cp);
		String UPGRADE_PATH = TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages";
		
        try {
        	File backupDir                  = new File(UPGRADE_PATH+"/"+URLDecoder.decode(filename, "UTF-8"));
            boolean delSuccess = backupDir.delete();
            if(!delSuccess) {
            	return "F";
            }
        }
        catch(SecurityException secex) {
           secex.printStackTrace();
           return "I";
        }
        catch(Throwable th) {
            th.printStackTrace();
            return "I";
        }
        return "S";
	}
	
	
	
	
	
	@Path("/backup/logs")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String getBackupRestoreLogs() {
		BufferedReader br = null;
		StringBuffer errBuffer = new StringBuffer();
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("cat /var/lib/tomcat6/Enlighted/adminlogs/backuprestore_error.log /var/lib/tomcat6/Enlighted/adminlogs/backuprestore.log");
			pr.waitFor();
			String line = "";

			br = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			boolean isStatus = false;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				if(isStatus || "EMS_BACKUP_STARTED".equals(line) || "EMS_RESTORE_STARTED".equals(line)) {
					isStatus = true;
					errBuffer.append(line);
				}
				else {
					errBuffer.append(line).append("<br />");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return errBuffer.toString();
		
	}
	
	@Path("/upload/size")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String getBackupFileSize() {
		
		ServletContext context = this.getWebApplicationContext().getServletContext();
		BufferedReader br = null;
		if(context.getAttribute("uploadFilePath") != null && context.getAttribute("uploadFileSize") != null) {
			//System.out.println(context.getAttribute("uploadFilePath").toString() + "   "  + 
				//	context.getAttribute("uploadFileSize").toString());
			File uploadFile = new File(context.getAttribute("uploadFilePath").toString());
			if(uploadFile.exists()) {
				//System.out.println(uploadFile.getAbsolutePath() + "  " + uploadFile.getName());
				try {

					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec(new String[] {"ls", "-s", uploadFile.getAbsolutePath()});
					pr.waitFor();
					String line = "";
				
					br = new BufferedReader(new InputStreamReader(
							pr.getInputStream()));
					
					line = br.readLine();
					if(line.contains(uploadFile.getName())) {
						Double percent = (Long.parseLong((line.split(" "))[0]) * 1024 * 100)/(Double.parseDouble(context.getAttribute("uploadFileSize").toString()));
						return (new Long(percent.longValue())).toString();
					}
			
				} catch (Exception e) {
					e.printStackTrace();
				} 
				finally {
					if (br != null) {
						try {
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}	
			}
		}
		
		return "-1";
	}
	

}
