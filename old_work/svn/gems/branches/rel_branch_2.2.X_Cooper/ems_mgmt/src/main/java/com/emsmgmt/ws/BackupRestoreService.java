package com.emsmgmt.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
	public String startBackup(@PathParam("filename") String inputFileName, @Context HttpServletRequest req) {
		
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
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
    	return "FAILURE";
	}
	
	
	@Path("/backup/restore/{filename}/{filepath}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String restoreBackUpFile(@PathParam("filename") String filename, @PathParam("filepath") String filepath, @Context HttpServletRequest req) {
		
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
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
    	return "FAILURE";
	}
	

	@Path("/backup/delete/{filename}/{filepath}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String deleteBackUpFile(@PathParam("filename") String filename, @PathParam("filepath") String filepath, @Context HttpServletRequest req) {

		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
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
    	return "I";
	}
	
	@Path("/upgrade/delete/{filename}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String deleteUpgradeFile(@PathParam("filename") String filename, @Context HttpServletRequest req) {
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
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
    	return "I";
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
	public String getBackupFileSize(@Context HttpServletRequest req) {
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {
			ServletContext context = this.getWebApplicationContext().getServletContext();
			String SCRIPT_LOCATION = context.getRealPath("/")+"adminscripts/checkfilesize.sh";
			int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());  
			String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0,cp);
			
			BufferedReader br = null;
			try {
	
				String scriptExecCommand ="/bin/bash " + SCRIPT_LOCATION + " " + TOMCAT_INSTALLATION_PATH + "/../work/Catalina/localhost/ems_mgmt/";
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(scriptExecCommand);
				proc.waitFor();	
				String line = "";
				br = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						return "-1";
					}
					return line;
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
			return "-1";
	}
	
	
	@Path("/upgrade/{filename}")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String upgradeGems(@PathParam("filename") String filename, @Context HttpServletRequest req) {
		
		HttpSession session = req.getSession(false);
    	if(session != null && session.getAttribute("isAuthenticated") != null && session.getAttribute("isAuthenticated").toString().equals("Y")) {

			boolean alreadyRunningError = false;
			
			ServletContext context = this.getWebApplicationContext().getServletContext();
			
			String SCRIPT_LOCATION = context.getRealPath("/")+"adminscripts/debian_upgrade.sh";
			int cp = context.getRealPath("/").lastIndexOf(context.getContextPath());  
			String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0,cp);
			
			try {
				if(!isAdminProcessRunning()) {
					String scriptExecCommand = "/bin/bash "+ SCRIPT_LOCATION + " " + URLDecoder.decode(filename, "UTF-8").replaceAll(" ", "#")
							+ " " +  TOMCAT_INSTALLATION_PATH + "/../Enlighted/UpgradeImages/";
					//System.out.println(scriptExecCommand);
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
    	return "FAILURE";
	}
	
	
	@Path("/upgrade/logs")
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	public String getUpgradeLogs() {
		BufferedReader br = null;
		StringBuffer errBuffer = new StringBuffer();
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("cat /var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log /var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log");
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
				if(isStatus || "EMS_UPGRADE_STARTED".equals(line)) {
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
	

}
