package com.emscloud.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
import org.springframework.stereotype.Controller;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.ArgumentUtils;
import com.emscloud.model.CustomerBills;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.SppaManager;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.UTCConverter;
import com.emscloud.vo.EmHealthDataVO;

@Controller
@Path("/org/eminstance")
public class EmInstanceService {
	private static final String PORT = "{port}";

	private static final String SERVER_NAME = "{serverName}";
	

	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	EmStatsManager		emStatsManager;
	
	@Resource
	EmStateManager emStateManager;
	
	@Resource
	SppaManager sppaManager;
	

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    private String apacheSampleConfigFile = "/home/enlighted/000-default-em" ;
    private String newApacheConfigFile = "/etc/apache2/sites-enabled/000-default-em" ;
    private String tempApacheConfigFile = "/tmp/000-default-em" ;
    
    public static Logger logger = Logger.getLogger(EmInstanceService.class.getName());
    
    EmInstanceService()
	{
		
	}

	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceList(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadallEmInstances();
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		return instances ;
    	}
    	return null ;
    }
	
	@Path("listEmInstancesByReplicaServerId/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByReplicaServerId(@PathParam("id") Long id) {
    	List<EmInstance> instances = emInstanceManager.loadEmInstanceByReplicaServerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		return instances ;
    	}
    	return null ;
    }

	@Path("listemstats/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmStats> getEmStatsByEmInstanceId(@PathParam("id") Long id) {
    	List<EmStats> emStatsList = emInstanceManager.loadEmStatsByEmInstanceId(id,0,10);
    	
    	if(emStatsList != null && !emStatsList.isEmpty())
    	{
    		return emStatsList ;
    	}
    	return null ;
    }
	
	@Path("listemstats/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmStats> getEmStatsByEmInstanceId(@PathParam("id") Long id,
    											  @FormParam("page") Integer page,
    											  @FormParam("sidx") String orderby,
    											  @FormParam("sord") String orderway) {
    	List<EmStats> emStatsList = emInstanceManager.loadEmStatsByEmInstanceId(id,page*10,10);
    	
    	if(emStatsList != null && !emStatsList.isEmpty())
    	{
    		return emStatsList ;
    	}
    	return null ;
    }
	
	@Path("loademstats/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmStatsList loadEmStatsListWithSpecificAttrs(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
    	String timeZone = emInstance.getTimeZone();
    	
    	EmStatsList oEmStatsList = emInstanceManager.loadEmStatsListByEmInstanceId(id,orderway, (page - 1) * EmStatsList.DEFAULT_ROWS,
				EmStatsList.DEFAULT_ROWS);
		oEmStatsList.setPage(page);
		List<EmStats> emstatList = oEmStatsList.getEmStats();
		if(emstatList !=null){
			for(EmStats emStats :emstatList){
				emStats.setUtcCaptureAt(UTCConverter.getUTCTimestamp(emStats.getCaptureAt(), timeZone));
			}
			
			
		}
		oEmStatsList.setEmStats(emstatList);
		return oEmStatsList;
    }
	
	
	@Path("loademinstbycustomerid/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceListByCustomerId(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	//EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
    	//String timeZone = emInstance.getTimeZone();
    	
    	EmInstanceList oEmInstList = emInstanceManager.loadEmInstancesListByCustomerId(id, orderway, (page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oEmInstList.setPage(page);
		List<EmInstance> emInstList = oEmInstList.getEmInsts();
		if(emInstList !=null && !emInstList.isEmpty()){
			for(EmInstance emInstance :emInstList){
				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));
				EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setLastConnectivityAt(latestEmStats.getCaptureAt());
    				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(latestEmStats.getCaptureAt(), emInstance.getTimeZone()));
        			emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
    			}else{
    				emInstance.setLastConnectivityAt(null);
        		}
    			EmState emState = emStateManager.loadLastEmStatsByEmInstanceId(emInstance.getId());
    			if (emState != null){
    				if (emState.getEmStatus() == EmStatus.CALL_HOME){
    					emInstance.setSyncConnectivity("NA");
    				}else{
	    					if (emState.getDatabaseState() != DatabaseState.SYNC_READY){
	    						emInstance.setSyncConnectivity(emState.getDatabaseState().getName());
	    					}
	    					else{
	    						if(emState.getLog() == null){
	    							emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName());
	    						}else{
	    							emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName()+" "+emState.getLog());
	    						}
	    					}
    				}
    				
        		}else{
        			emInstance.setSyncConnectivity("NA");
        		}
    			
			}
		}
		else {
			emInstList = new ArrayList<EmInstance>();
		}
		oEmInstList.setEmInsts(emInstList);
		return oEmInstList;
    }
	
	@Path("listbycutomerid/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmInstance> getEmInstanceListByCustomerId(@PathParam("id") Long id) throws ParseException {
    	List<EmInstance> instances = emInstanceManager.loadEmInstancesByCustomerId(id);
    	
    	if(instances != null && !instances.isEmpty())
    	{
    		for(EmInstance emInstance : instances){
    			EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setLastConnectivityAt(latestEmStats.getCaptureAt());
    				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(latestEmStats.getCaptureAt(), "UTC"));
        			emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
    			}else{
    				emInstance.setLastConnectivityAt(null);
        		}
    			
    		}
    		
    		return instances ;
    	}
    	return null ;
    }
	
	@Path("listUnRegEmInstance")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceUnRegList(@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	
    	EmInstanceList oEmInstList = emInstanceManager.loadUnRegEmInstances(orderway, (page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oEmInstList.setPage(page);
		List<EmInstance> emInstList = oEmInstList.getEmInsts();
		if(emInstList !=null && !emInstList.isEmpty()){
			for(EmInstance emInstance :emInstList){
				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTime(emInstance.getLastConnectivityAt()));
			}
		}
		else {
			emInstList = new ArrayList<EmInstance>();
		}
		oEmInstList.setEmInsts(emInstList);
		return oEmInstList;
    }

	@Path("delete/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
	
	
	@Path("deleteUnRegEmInstance/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteUnRegEmInstance(@PathParam("id") Long id) {
		
		emInstanceManager.delete(id);
		
		return;
    }
	
	@Path("billinglistbycutomerid/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CustomerBills getEmInstanceBillingListByCustomerId(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
	
		CustomerBills customerSppaBills = 	sppaManager.getAllBillsPerCustomer(id,orderway, (page - 1) * CustomerBills.DEFAULT_ROWS,CustomerBills.DEFAULT_ROWS);
		customerSppaBills.setPage(page);
    	if(customerSppaBills.getCustomerSppaBill() != null && !customerSppaBills.getCustomerSppaBill().isEmpty())
    	{
    		return customerSppaBills;
    	}
    	return null ;
    }
	
	@Path("toggle/em/tunnel/flag/{id}/{value}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String toggleEmopenTunnel(@PathParam("id") String id,@PathParam("value") String value) {
		try{
			EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(id));
			String sampleDn = "em-"+em.getCustomer().getId()+"-"+em.getId() ;
			// So no one can toggle button untill the process is complete
			if(!em.getBrowseEnabledFromCloud() && em.getOpenTunnelToCloud())
			{
				return "In Progress" ;
			} else if (em.getBrowseEnabledFromCloud() && !em.getOpenTunnelToCloud())
			{
				return "In Progress";
			}
			em.setOpenTunnelToCloud(!Boolean.parseBoolean(value));
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
				em.setTunnelPort(0l);
				em.setBrowsableLink(null);
				removeApacheDefaultFile(domainName);
			}else
			{
				int port = CommonUtils.getRandomPort();
				em.setTunnelPort((long) port);
				
				em.setBrowsableLink(createBrowsableLink(domainName, String.valueOf(port)));
				setApacheDefaultFile(domainName ,  String.valueOf(port));
				
			}
		
			EmInstance newem =emInstanceManager.saveOrUpdate(em);
			CommonUtils.reloadApache() ;
			return String.valueOf(newem.getOpenTunnelToCloud());
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
					    	line = line.replace( SERVER_NAME ,databaseName+".enlightedCloud.net");
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

	private String createBrowsableLink(String DbName , String port )
	{
		return "https://"+DbName+".enlightedcloud.net/ems" ;
	}
	
	@Path("get/em/tunnel/flag/value/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String toggleEmopenTunnel(@PathParam("id") String id) {
		try{
			EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(id));
			return String.valueOf(em.getOpenTunnelToCloud());
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
		
	}
	
	@Path("/get/em/browsable/link/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String getBrowsableLink(@PathParam("id") String id) {
		try{
			EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(id));
			return String.valueOf(em.getBrowsableLink());
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
		
	}
	
	@Path("/listEMInstanceWithHealthData")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmHealthDataVO> getListofEMWithHealthData() {
    	List<EmHealthDataVO> healthDataVOList = emInstanceManager.getEMHealthDataVOList();
    	
    	if(healthDataVOList != null && !healthDataVOList.isEmpty())
    	{
    		return healthDataVOList ;
    	}
    	return null ;
    }
	
	
	
}
