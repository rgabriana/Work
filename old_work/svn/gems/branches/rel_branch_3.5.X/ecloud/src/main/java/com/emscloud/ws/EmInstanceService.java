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
import java.util.Calendar;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.communication.utils.ArgumentUtils;
import com.emscloud.model.CustomerBills;
import com.emscloud.model.EmHealthList;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.EmStatsList;
import com.emscloud.service.EmFacilityManager;
import com.emscloud.service.EmFacilityTreeManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmLastEcSynctimeManager;
import com.emscloud.service.EmLastGenericSynctimeManager;
import com.emscloud.service.EmStateManager;
import com.emscloud.service.EmStatsManager;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.MonitoringManager;
import com.emscloud.service.SppaManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.UTCConverter;
import com.emscloud.util.tree.TreeNode;
import com.emscloud.vo.EmHealthDataVO;
import com.emscloud.vo.FixtureHealthDataVO;
import com.emscloud.vo.GatewayHealthDataVO;

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
	
	//@Resource
	//CloudConnectionTemplate cloudConnectionTemplate ;
	
	@Resource
	MonitoringManager monitoringManager;
	
	@Resource
	EmFacilityTreeManager emFacilityTreeManager;
	
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	EmLastGenericSynctimeManager emLastGenericSynctimeManager;
	
	@Resource
	EmLastEcSynctimeManager emLastEcSynctimeManager;
	
	@Resource
	EmFacilityManager emFacilityManager;
	
	@Resource
	EmTasksManager emTasksManager;
	 
	@Resource(name = "messageSource")
    private MessageSource messageSource;
	
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
    	List<EmInstance> instances = emInstanceManager.loadAllEmInstances();
    	
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
    	EmStatsList oEmStatsList = null;
    	String timeZone = null;
    	if(emInstance != null){
    		timeZone = emInstance.getTimeZone();
    		oEmStatsList = emInstanceManager.loadEmStatsListByEmInstanceId(id,orderway, (page - 1) * EmStatsList.DEFAULT_ROWS,
    				EmStatsList.DEFAULT_ROWS);
    		oEmStatsList.setPage(page);
    		List<EmStats> emstatList = oEmStatsList.getEmStats();
    		if(emstatList !=null){
    			for(EmStats emStats :emstatList){
    				emStats.setUtcCaptureAt(UTCConverter.getUTCTimestamp(emStats.getCaptureAt(), timeZone));
    			}
    			
    			
    		}
    		oEmStatsList.setEmStats(emstatList);
    	}
    	
    	
		return oEmStatsList;
    }
	
	
	@Path("loademinstbycustomerid/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceListByCustomerId(@PathParam("id") Long id,
    		@RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {
    	//EmInstance emInstance = emInstanceManager.loadEmInstanceById(id);
    	//String timeZone = emInstance.getTimeZone();
		
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
		
    	EmInstanceList oEmInstList = emInstanceManager.loadEmInstancesListByCustomerId(id, orderBy,orderWay, bSearch, searchField, searchString,
				searchOper,(page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		oEmInstList.setPage(page);
		List<EmInstance> emInstList = oEmInstList.getEmInsts();
		if(emInstList !=null && !emInstList.isEmpty()){
			for(EmInstance emInstance :emInstList){
				emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));
				EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
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
    			emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));    			
    			EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
    			if(latestEmStats != null){
    				emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
    			}
    			
    		}
    		
    		return instances ;
    	}
    	return null ;
    }
	
	@Path("listUnRegEmInstance")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstanceUnRegList(@RequestParam("data") String userdata) throws ParseException, UnsupportedEncodingException {
    	
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
		EmInstanceList oEmInstList = emInstanceManager.loadUnRegEmInstances(orderBy,orderWay, bSearch, searchField, searchString,
				searchOper,(page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		//EmInstanceList oEmInstList = emInstanceManager.loadUnRegEmInstances(orderWay, (page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
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
		
		EmInstance regEmInstance = emInstanceManager.getEmInstance(id);
		regEmInstance.setEmState(1L); // mark as deleted
		emInstanceManager.saveOrUpdate(regEmInstance);
		logger.info("marking em with id "+id +" as deleted");
		/*emLastGenericSynctimeManager.deleteEmLastGenericSynctimeByEmId(id);		
		emLastEcSynctimeManager.deleteEmLastEcSynctimeByEmId(id);
		emStateManager.deleteEmStateByEmId(id);
		emFacilityManager.deleteEmFacilityByEmId(id);
		
		if(regEmInstance.getLatestEmsHealthMonitor() != null)
		{
			regEmInstance.setLatestEmsHealthMonitor(null);
			emInstanceManager.saveOrUpdate(regEmInstance);			
		}		
		monitoringManager.deleteEmHealthMonitorByEmId(id);
		
		emStatsManager.deleteEmStatsByEmId(id);		
		emTasksManager.deleteEmTasksByEmId(id);
		facilityEmMappingManager.deleteFacilityEmMappingByEmId(id);
		
		emInstanceManager.delete(id);
		
		cloudAuditLoggerUtil.log("Deleted Registered EM Instance Server: "+regEmInstance.getName()+" ( Mac Id :"+regEmInstance.getMacId()+" )", CloudAuditActionType.Em_Instance_Reg_Deleted.getName());
		
		return;*/
    }
	
	
	@Path("deleteUnRegEmInstance/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteUnRegEmInstance(@PathParam("id") Long id) {
		
		EmInstance unregEmInstance = emInstanceManager.getEmInstance(id);
		
		emLastGenericSynctimeManager.deleteEmLastGenericSynctimeByEmId(id);
		
		emLastEcSynctimeManager.deleteEmLastEcSynctimeByEmId(id);
		
		emInstanceManager.delete(id);
		
		cloudAuditLoggerUtil.log("Deleted UnRegistered EM Instance Server: "+unregEmInstance.getName()+" ( Mac Id :"+unregEmInstance.getMacId()+" )", CloudAuditActionType.Em_Instance_Unreg_Deleted.getName());
		
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
			if(em != null){
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
			}
			
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
		return "";
	}
	
	@Path("reset/browsabilityflag/{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String resetBrowsabilityFlag(@PathParam("id") String id) {
		try{
			EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(id));
			if(em != null){
				em.setBrowseEnabledFromCloud(false);
				em.setOpenTunnelToCloud(false);
				emInstanceManager.saveOrUpdate(em);
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
					    	line = line.replace( SERVER_NAME ,databaseName+".enlightedcloud.net");
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
			if(em != null){
				return String.valueOf(em.getBrowsableLink());				
			}
			return "";
		}catch (Exception e){
			e.printStackTrace();
			return "error" ;
		}
		
	}
	
	@Path("listEMInstanceWithHealthData")
    @POST
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmHealthList getListofEMWithHealthData(@FormParam("page") int page,
    		@FormParam("sidx") String orderBy,@FormParam("sord") String orderWay,@FormParam("searchCol") String searchCol,@FormParam("searchStr") String searchStr) {
		EmHealthList emHealthList = emInstanceManager.getEMHealthDataVOList(page,orderBy,orderWay, searchCol, searchStr,
				"",(page - 1) * EmInstanceList.DEFAULT_ROWS, EmInstanceList.DEFAULT_ROWS);
		
    	return emHealthList ;
    }
	
	
	@Path("/gatewayListForHealthData/{emInstanceId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<GatewayHealthDataVO> getGatewayListForHealthData(@PathParam("emInstanceId") String emInstanceId) {
		
		return monitoringManager.getGatewayListForHealthData(emInstanceId);
    }
	@Path("/fixtureListForHealthData/{emInstanceId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FixtureHealthDataVO> getFixtureListForHealthData(@PathParam("emInstanceId") String emInstanceId) {
		
		return monitoringManager.getFixtureListForHealthData(emInstanceId);
    }
	
	/**
     * Get EmInstance Facility Tree
     * 
     * @param id
     *            EmInstance unique identifier
     * @return EM Instance Facility Tree Hierarchy
     */
    @Path("emInstfacilityTree/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public TreeNode<FacilityType> getEmInstfacilityTree(@PathParam("id") long id) {
	    TreeNode<FacilityType> emInstFacilityTreeHierarchy = new TreeNode<FacilityType>();
	    emInstFacilityTreeHierarchy = emFacilityTreeManager.loadEmInstanceCompanyHierarchy(id);
	    
	    if(emInstFacilityTreeHierarchy != null){
	    	for (TreeNode<FacilityType> campus : emInstFacilityTreeHierarchy.getTreeNodeList()) {
				for (TreeNode<FacilityType> building : campus.getTreeNodeList()){
					for (TreeNode<FacilityType> floor : building.getTreeNodeList()){
						if(facilityEmMappingManager.getFacilityEmMappingOnEmFloorId(id, floor.getNodeId()) == null){
							floor.setMapped(false);
						}else{
							floor.setMapped(true);
						}
					}
				}
			}
	    }
	    return emInstFacilityTreeHierarchy;
    }
  
    /**
     * update pause_sync status of EmInstance
     * 
     * @param id
     *            EmInstance unique identifier
     * @param status
     *            boolean status
     * @return update status
     */
    @Path("updatepauseresume/{id}/{status}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String updatePauseResumeStatus(@PathParam("id") long id,
    		                        @PathParam("status") Boolean status) {
    	return emInstanceManager.updatePauseResumeStatus(id, status);
    }

    /**
     * Checks whether Restricted Migration is already initiated or not
     * @param id
     * @return
     */
    @Path("checkrestrictedmigration/{id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkIfRestrictedMigrationAlreadyInitiated(@PathParam("id") long id){
    	EmInstance emInstance = emInstanceManager.getEmInstance(id);
    	if(emInstance == null){
    		return Response.status(Response.Status.NOT_ACCEPTABLE).entity("EM not exists").build();
    	}
    	EmState currState = emStateManager.loadEmStateById(emInstance.getLatestEmStateId());
    	if(currState != null && ( currState.getDatabaseState() != DatabaseState.SYNC_READY && currState.getDatabaseState() != DatabaseState.SYNC_FAILED ) ){
    		final String msg = messageSource.getMessage("restrictedmigration.cannotstart",
					null, LocaleContextHolder.getLocale());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(msg).build();
    	}
    	return Response.status(Response.Status.OK).build();
    }
    /**
     * trigger the restricted migration by setting the EM_STATE to RESTRICTED_REMIGRATION_READY
     * @param id
     * @param status
     * @return
     */
    @Path("startrestrictedmigration/{id}/{status}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response startRestrictedMigration(@PathParam("id") long id,
    		                        @PathParam("status") Boolean status) {
    	EmInstance emInstance = emInstanceManager.getEmInstance(id);
    	if(emInstance == null){
    		return Response.status(Response.Status.NOT_ACCEPTABLE).entity("EM not exists").build();
    	}
    	DatabaseState state = DatabaseState.RESTRICTED_REMIGRATION_READY;
    	final EmState emState = new EmState() ;
		emState.setFailedAttempts(0);
		emState.setEmInstanceId(id);
		emState.setDatabaseState(state);
		if(state.getName().contains("_IN_"))
		{
			emState.setLog("{lastMinId/Exception}{,} :- ");
		}
		if (emInstance.getSppaEnabled()) {
			emState.setEmStatus(EmStatus.SPPA);
		} else {
			emState.setEmStatus(EmStatus.CALL_HOME);
		}
		emState.setSetTime(Calendar.getInstance().getTime()) ;
		EmState newstat = emStateManager.saveOrUpdate(emState);
    	emInstanceManager.updatePauseResumeStatus(id, status);
    	return Response.status(Response.Status.OK).build();
    }
}
