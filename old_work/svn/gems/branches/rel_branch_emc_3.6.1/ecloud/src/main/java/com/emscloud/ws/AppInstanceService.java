package com.emscloud.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.model.AppInstance;
import com.emscloud.model.AppInstanceList;
import com.emscloud.model.EmInstanceList;
import com.emscloud.service.AppInstanceManager;
import com.emscloud.service.MonitoringManager;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.UTCConverter;

@Controller
@Path("/org/appinstance")
public class AppInstanceService {
	private static final String PORT = "{port}";

	private static final String SERVER_NAME = "{serverName}";
	

	@Resource
	AppInstanceManager		appInstanceManager;
	
	@Resource
	MonitoringManager monitoringManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource(name = "messageSource")
    private MessageSource messageSource;
	
	@Resource
	private CloudAdapter cloudAdapter;
	
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    private String apacheSampleConfigFile = "/home/enlighted/000-default-apps" ;
    private String newApacheConfigFile = "/etc/apache2/sites-enabled/000-default-apps" ;
    private String tempApacheConfigFile = "/tmp/000-default-apps" ;
    
    public static Logger logger = Logger.getLogger(AppInstanceService.class.getName());
    
    AppInstanceService()
	{
		
	}

	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<AppInstance> getAppInstanceList(@PathParam("id") Long id) {
    	List<AppInstance> instances = appInstanceManager.loadAllAppInstances();
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		return instances ;
    	}
    	return null ;
    }
	
	
	@Path("loadappinstbycustomerid/{id}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public AppInstanceList loadAppInstanceListByCustomerId(@PathParam("id") Long id,
			@RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {
		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		//long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;
		String searchField = null;
		String searchString = null;
		String searchOper = "cn";
		Boolean bSearch = false;
	
	
		String[] params = null;
		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
					params = query.split("#");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}
		
		
		if (params != null && params.length > 0) {
			if (params[1] != null && !"".equals(params[1])) {
				searchField = params[1];
			} else {
				searchField = null;
			}
			if (params[2] != null && !"".equals(params[2])) {
				searchString = URLDecoder.decode(params[2], "UTF-8");
			} else {
				searchString = null;
			}
			
			if (params[3] != null && !"".equals(params[3])) {
				bSearch = true;
			} else {
				bSearch = false;
			}
		}
	
		AppInstanceList oAppInstList = appInstanceManager.loadAppInstancesListByCustomerId(id, orderBy,orderWay, bSearch, searchField, searchString,
				searchOper,(page - 1) * AppInstanceList.DEFAULT_ROWS, AppInstanceList.DEFAULT_ROWS);
		oAppInstList.setPage(page);
		List<AppInstance> appInstList = oAppInstList.getAppInsts();
		if(appInstList !=null && !appInstList.isEmpty()){
			for(AppInstance appInstance :appInstList){				
				appInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(appInstance.getLastConnectivityAt(), appInstance.getTimeZone()));				
			}
		}
		else {
			appInstList = new ArrayList<AppInstance>();
		}
		oAppInstList.setAppInsts(appInstList);
		return oAppInstList;
	}
	
	@Path("listbycutomerid/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<AppInstance> getAppInstanceListByCustomerId(@PathParam("id") Long id) throws ParseException {
    	List<AppInstance> instances = appInstanceManager.loadAppInstancesByCustomerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		for(AppInstance appInstance : instances){
    			appInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(appInstance.getLastConnectivityAt(), appInstance.getTimeZone()));    			
    		}
    		
    		return instances ;
    	}
    	return null ;
    }
	
	@Path("listUnRegAppInstance")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public AppInstanceList loadAppInstanceUnRegList(@RequestParam("data") String userdata) throws ParseException, UnsupportedEncodingException {
    	
		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		//long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;
		String searchField = null;
		String searchString = null;
		String searchOper = "cn";
		Boolean bSearch = false;
		
		
		String[] params = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
					params = query.split("#");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}
		
		
		if (params != null && params.length > 0) {
			if (params[1] != null && !"".equals(params[1])) {
				searchField = params[1];
			} else {
				searchField = null;
			}

			if (params[2] != null && !"".equals(params[2])) {
				searchString = URLDecoder.decode(params[2], "UTF-8");
			} else {
				searchString = null;
			}
			
			if (params[3] != null && !"".equals(params[3])) {
				bSearch = true;
			} else {
				bSearch = false;
			}

		}
		AppInstanceList oAppInstList = appInstanceManager.loadUnRegAppInstances(orderBy,orderWay, bSearch, searchField, searchString,
				searchOper,(page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oAppInstList.setPage(page);
		List<AppInstance> appInstList = oAppInstList.getAppInsts();
		if(appInstList !=null && !appInstList.isEmpty()){
			for(AppInstance appInstance :appInstList){
				appInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTime(appInstance.getLastConnectivityAt()));
			}
		}
		else {
			appInstList = new ArrayList<AppInstance>();
		}
		oAppInstList.setAppInsts(appInstList);
		return oAppInstList;
    }

	
	@Path("toggle/app/tunnel/flag/{id}/{value}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String toggleAppopenTunnel(@PathParam("id") String id,@PathParam("value") String value) {
		String retVal = "";
		try{
			AppInstance app = appInstanceManager.loadAppInstanceById(Long.parseLong(id));
			if(app != null){
				String sampleDn = "app-"+app.getCustomer().getId()+"-"+app.getId() ;
				// So no one can toggle button untill the process is complete
				if(app.getOpenTunnelToCloud())
				{
					retVal = "In Progress" ;
				} 
				app.setOpenTunnelToCloud(!Boolean.parseBoolean(value));
				// assign port if earlier state if false and zero if earlier state is true.
				String domain =CommonUtils.getHostName() ;
				String domainName= sampleDn;
				try{
				if(ArgumentUtils.isNullOrEmpty(domain))
				{
					domainName = sampleDn;
				}else {
					if(ArgumentUtils.isNullOrEmpty(CommonUtils.getHostName().split("-")[3])){
						domainName = sampleDn ;
					}else {
						//domainName = CommonUtils.getHostName().split("-")[3]+ "-"+sampleDn ;
						domainName = sampleDn+"."+CommonUtils.getHostName().split("-")[3];
					}
				}}catch(Exception e)
				{
					logger.info(e.getMessage());
					logger.warn("setting link name to its dbName..");
					domainName = sampleDn ;
				}
				if(Boolean.parseBoolean(value))
				{
					app.setTunnelPort(0l);
					app.setBrowsableLink(null);
					removeApacheDefaultFile(domainName);
				}else
				{
					removeApacheDefaultFile(domainName);
					int port = CommonUtils.getRandomPort_App();
					app.setTunnelPort((long) port);
					
					app.setBrowsableLink(createBrowsableLink(domainName, String.valueOf(port)));
					setApacheDefaultFile(domainName ,  String.valueOf(port));
					
				}
			
				AppInstance newApp =appInstanceManager.saveOrUpdate(app);
				CommonUtils.reloadApache() ;
				retVal = String.valueOf(newApp.getOpenTunnelToCloud()); 
			}
			
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
		return retVal;
	}
	
	@Path("reset/browsabilityflag/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String resetBrowsabilityFlag(@PathParam("id") String id) {
		try{
			AppInstance app = appInstanceManager.loadAppInstanceById(Long.parseLong(id));
			if(app != null){
				String sampleDn = "app-"+app.getCustomer().getId()+"-"+app.getId() ;
				String domain =CommonUtils.getHostName() ;
				String domainName= sampleDn;
				try{
					if(ArgumentUtils.isNullOrEmpty(domain))
					{
						domainName = sampleDn;
					}else {
						if(ArgumentUtils.isNullOrEmpty(CommonUtils.getHostName().split("-")[3])){
							domainName = sampleDn ;
						}else {
							domainName = sampleDn+"."+CommonUtils.getHostName().split("-")[3];
						}
					}
				}catch(Exception e) {
					logger.info(e.getMessage());
					logger.warn("setting link name to its dbName..");
					domainName = sampleDn ;
				}
				removeApacheDefaultFile(domainName);
				CommonUtils.reloadApache() ;
				app.setOpenTunnelToCloud(false);
				appInstanceManager.saveOrUpdate(app);
			}			
			return "true";
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
	}
	
	private void removeApacheDefaultFile(String dbName) {
		File newFile= null ;
		File sampleFile = null ;
		String end = "#-----End of " + dbName ;
		String start = "#-----Start of " + dbName ;
		try{
			
			 newFile = new File(tempApacheConfigFile);
			 sampleFile = new File(newApacheConfigFile);
			 if(newFile.exists())
			 {
				 newFile.delete();
			 }
			 newFile.createNewFile() ;
			if(sampleFile.exists()){
					    BufferedReader reader = new BufferedReader(new FileReader(sampleFile));
					    PrintWriter writer = new PrintWriter(new FileWriter(newFile));
					    String line = null;
					    while ((line = reader.readLine()) != null)
					    {
					    	// remove text patch from start to end
					    	if(line.contains(start))
					    	{
					    		while(!line.contains(end))
					    		{
					    			line = reader.readLine() ;
					    		}
					    		line = reader.readLine() ;
					    	}else
					    	{
					    		writer.println(line);
					    	}
					        
					    }
					    reader.close();
					    writer.close();
					    //copy tmp file back
					    reader = new BufferedReader(new FileReader(newFile));
					    writer = new PrintWriter(new FileWriter(sampleFile));
					    line = null;
					    while ((line = reader.readLine()) != null)
					    {
					         writer.println(line);
					      
					    }
					    reader.close();
					    writer.close();
			}else
			{
				logger.error(apacheSampleConfigFile + "does not exists. Please Contact Administrator.");
			}
			
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally
		{
			newFile= null ;
			 sampleFile = null ;
		}
		
	}

	private void setApacheDefaultFile(String databaseName, String port) {
		File newFile= null ;
		File sampleFile = null ;
		try{
			
			 newFile = new File(newApacheConfigFile);
			 sampleFile = new File(apacheSampleConfigFile);
			if(newFile.exists()){
			if(sampleFile.exists()){
					    BufferedReader reader = new BufferedReader(new FileReader(sampleFile));
					    PrintWriter writer = new PrintWriter(new FileWriter(newFile , true));
					    String line = null;
					    writer.append("\n");
					    writer.append("#-----Start of " + databaseName) ;
					    writer.append("\n");
					    while ((line = reader.readLine()) != null)
					    {
					    	line = line.replace( SERVER_NAME ,databaseName + "." + EmInstanceService.domainName);
					    	line = line.replace(PORT,port);
					         writer.append(line);
					         writer.append("\n");
					    }
					    writer.append("#-----End of " + databaseName) ;
					    writer.append("\n");
					    reader.close();
					    writer.close();
			}else
			{
				logger.error(apacheSampleConfigFile + "does not exists. Please Contact Administrator.");
			}
			}else
			{
				logger.error(newApacheConfigFile + "does not exists. Please Contact Administrator.");
			}
			
			
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally
		{
			newFile= null ;
			 sampleFile = null ;
		}
		
	}

	private String createBrowsableLink(String DbName , String port) {
		return "https://"+DbName + "."  + EmInstanceService.domainName +"/" ;
	}
	
	@Path("get/app/tunnel/flag/value/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String getAppTunnelOpen(@PathParam("id") String id) {
		try{
			AppInstance app = appInstanceManager.loadAppInstanceById(Long.parseLong(id));
			return String.valueOf(app.getOpenTunnelToCloud());
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
	}
	
	@Path("/get/app/browsable/link/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String getBrowsableLink(@PathParam("id") String id) {
		try{
			AppInstance app = appInstanceManager.loadAppInstanceById(Long.parseLong(id));
			if(app != null){
				return String.valueOf(app.getBrowsableLink());				
			}
			return "";
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
	}
	
}
