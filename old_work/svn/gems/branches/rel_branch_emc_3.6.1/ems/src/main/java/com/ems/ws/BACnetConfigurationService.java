package com.ems.ws;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.BACnetConfig;
import com.ems.model.BACnetConfiguration;
import com.ems.model.BacnetObjectsCfg;
import com.ems.model.BacnetReportConfiguration;
import com.ems.model.BacnetReportConfigurationList;
import com.ems.model.SystemConfiguration;
import com.ems.security.exception.EmsValidationException;
import com.ems.server.ServerMain;
import com.ems.service.BACnetConfigurationManager;
import com.ems.service.BacnetManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/bacnetconfig")
public class BACnetConfigurationService {
	
	@Resource(name = "bacnetConfigurationManager")
    private BACnetConfigurationManager bacnetConfigurationManager;
	
	@Resource(name = "bacnetManager")
    private BacnetManager bacnetManager;
	
	@Autowired
	private MessageSource messageSource;
	
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	private static final Logger m_Logger = Logger.getLogger("BacnetLog");
	
	/**
     * @return BACnetConfiguration list
     */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
    @Path("alldetails")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BACnetConfiguration> loadAllBACnetConfig() {
    	List<BACnetConfiguration> list = bacnetConfigurationManager.loadAllBACnetConfig();
      return list;
    }
	
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editBACnetConfigValue(BACnetConfiguration bacnetConfiguration) throws EmsValidationException {
		Response resp = new Response();
		if(bacnetConfiguration.getName()!=null && bacnetConfiguration.getValue()!=null){
			BACnetConfiguration bc = bacnetConfigurationManager.loadBACnetConfigById(bacnetConfiguration.getId());
			if(bc != null && bc.getId() != null) {
				bc.setValue(bacnetConfiguration.getValue());
				bacnetConfigurationManager.save(bc);
			}
			else {
				bacnetConfigurationManager.save(bacnetConfiguration);
			}
		} else {
			m_Logger.error("Name/Value is null");
			throw new IllegalArgumentException("Name/Value is null");
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("save/{isEnableBacnetChecked}")
	//@Path("save")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveBACnetConfigurationValue(List<BACnetConfiguration> bACnetConfigurations,@PathParam("isEnableBacnetChecked") String isEnableBacnetChecked) {
		Response resp = new Response();
		
		try {
			if(bACnetConfigurations !=null && !bACnetConfigurations.isEmpty()){
				for(BACnetConfiguration bacnetConfiguration : bACnetConfigurations){
					if(bacnetConfiguration!=null){
						if(bacnetConfiguration.getName()==null || bacnetConfiguration.getValue()==null ){
							m_Logger.error("BACnetConfiguration should not have null");
							resp.setMsg("BACnetConfiguration Name/Value should not have null");
							resp.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						} else if(bacnetConfiguration.getName().equalsIgnoreCase("null") || bacnetConfiguration.getValue().equalsIgnoreCase("null") || bacnetConfiguration.getName().equalsIgnoreCase("") || bacnetConfiguration.getValue().equalsIgnoreCase("")){
							m_Logger.error("BACnetConfiguration should not have null or empty");
							resp.setMsg("BACnetConfiguration Name/Value should not have null or empty");
							resp.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						}
					}
				}
				if (resp.getStatus() == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode()){
					return resp;
				}
				resp = bacnetConfigurationManager.saveBACnetConfigurationDetails(bACnetConfigurations);
				if (resp.getStatus() == 0){
					resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "saveBACnetConfigurationValue.isEnableBacnetChecked", isEnableBacnetChecked);
					if (resp.getStatus() == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode()){
						return resp;
					} else {
						BACnetConfig config = bacnetManager.getConfig();
						config.setEnableBacnet(Boolean.valueOf(isEnableBacnetChecked));
						String result = bacnetManager.saveConfig(config);
						if(result.equals("SAVE_SUCCESS")){
							m_Logger.error("Able to save BacnetConfig");
							resp.setMsg("Able to save BacnetConfig");
							resp.setStatus(javax.ws.rs.core.Response.Status.ACCEPTED.getStatusCode());
						} else if (result.equals("SAVE_ERROR")){
							m_Logger.error("Unable to save BacnetConfig");
							resp.setMsg("Unable to save BacnetConfig");
							resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
						}
					}
				}
			} else {
				m_Logger.error("BACnetConfiguration is null");
				throw new IllegalArgumentException("BACnetConfiguration is null");
			}
		} catch (Exception e) {
			m_Logger.error("Error occured saving BacnetConfig", e);
			resp.setMsg("Not able to update bacnetConfig");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("details/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public BACnetConfiguration loadBACnetConfigByName(@PathParam("name") String name) {
		
		return bacnetConfigurationManager.loadBACnetConfigByName(name);
		
	}
	
	/**
	 * Export Bacnet Report Details
	 * @return : Bacnet Report Details in csv format
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("exportbacnetreportcsv")
	@POST
	@Produces("application/csv")
	public javax.ws.rs.core.Response exportDeviceDetailsCsv() {

		StringBuffer output = new StringBuffer("");
		
		m_Logger.debug("Fetching List of Bacnet Report Details");
		List<BacnetReportConfiguration> bacnetReportList = new ArrayList<BacnetReportConfiguration>();
		//bacnetReportList = bacnetConfigurationManager.getBacnetConfigReport();
		bacnetReportList = bacnetConfigurationManager.loadAllBacnetReportCfgs();
		output.append("BACnet Report Details\r\n\n");
	    
        output.append("Device Id"
                + ","
        		+ "Object Type"
                + ","
                + "Object instance"
                + ","
                + "Object name"
                );
        
        if(bacnetReportList!=null && !bacnetReportList.isEmpty()){
        	for(BacnetReportConfiguration bacnetReportConfiguration :bacnetReportList ){
        		output.append("\r\n");
                String deviceId = bacnetReportConfiguration.getDeviceid();
                String objectType = bacnetReportConfiguration.getObjecttype();
                String objectInstance = bacnetReportConfiguration.getObjectinstance();
                String objectName = bacnetReportConfiguration.getObjectname();
                output.append(
                		deviceId
                		+ "," 
                		+ objectType
                		+ ","
                		+ objectInstance
                        + ","
                        + objectName
                        );
        	}
        }
        
		return javax.ws.rs.core.Response
				.ok(output.toString(), "text/csv")
				.header("Content-Disposition",
						"attachment;filename=Bacnet_Devices_Details.csv")
				.build();
	}
	
	/**
     * @return BACnetConfiguration list
     */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
    @Path("getBacnetConfigReport")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BacnetReportConfiguration> getBacnetConfigReport() {
    	List<BacnetReportConfiguration> list = new ArrayList<BacnetReportConfiguration>();
      return list;
    }
	
	/**
     * @return BacnetObjectsCfg list
     */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
    @Path("getAllBacnetObjectsCfgs")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BacnetObjectsCfg> getAllBacnetObjectsCfgs() {
    	List<BacnetObjectsCfg> list = new ArrayList<BacnetObjectsCfg>();
		try {
			list = bacnetConfigurationManager.loadAllBacnetObjectCfgs();
		} catch (Exception e) {
			m_Logger.debug("Error While Fetching Bacnet_Objects_Cfg : BacnetObjectsCfg", e);
		}
      return list;
    }
	
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("saveBacnetObjectCfgs")
	//@Path("save")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveBacnetObjectCfgs(List<BacnetObjectsCfg> bacObjCfgs) {
		Response resp = new Response();
		
		try {
			if(bacObjCfgs !=null && !bacObjCfgs.isEmpty()){
				for(BacnetObjectsCfg bacnetObjectCfg : bacObjCfgs){
					if(bacnetObjectCfg!=null){
						if(bacnetObjectCfg.getId()==null || bacnetObjectCfg.getIsvalidobject() == null ){
							m_Logger.error("BacnetObjectsCfg should not have null");
							resp.setMsg("BacnetObjectsCfg Id/Isvalidobject should not have null");
							resp.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						} else if(bacnetObjectCfg.getIsvalidobject().equalsIgnoreCase("null") || bacnetObjectCfg.getIsvalidobject().equalsIgnoreCase("")){
							m_Logger.error("BacnetObjectsCfg should not have null");
							resp.setMsg("BacnetObjectsCfg Id/Isvalidobject should not have null");
							resp.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
						}
					}
				}
				if (resp.getStatus() == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode()){
					return resp;
				}
				resp = bacnetConfigurationManager.saveBacnetObjectCfgs(bacObjCfgs);
				
			} else {
				m_Logger.error("BACnetConfiguration is null");
				throw new IllegalArgumentException("BACnetConfiguration is null");
			}
		} catch (Exception e) {
			m_Logger.error("Error occured saving BacnetConfig", e);
			resp.setMsg("Not able to update bacnetConfig");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
	@Path("updateBacnetReportConfiguration")
	@POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateBacnetReportConfiguration() {
		Response resp = new Response();
		String bacnetReportFlag = "done";
		SystemConfiguration bacnetReportFlagSysConf = systemConfigurationManager.loadConfigByName("bacnet.report.flag");
		if (bacnetReportFlagSysConf!=null){
			bacnetReportFlag = bacnetReportFlagSysConf.getValue();
			if(bacnetReportFlag!=null && bacnetReportFlag.equals("done")){
				bacnetReportFlagSysConf.setValue("inprogress");
				systemConfigurationManager.save(bacnetReportFlagSysConf);
			}
		}
		final List<BacnetReportConfiguration> list = new ArrayList<BacnetReportConfiguration>();
    	FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			String bacnetReportFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet/config/bacnetReport.txt";
    		File file = new File(bacnetReportFile);
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if(line.startsWith("#") || line.trim().equals("")){
					continue;
				}
				if(line.contains("\t")){
					String[] arrlist = line.toString().split("\t");
					if(arrlist!=null && arrlist.length == 4){
						BacnetReportConfiguration bacnetReportObj = new BacnetReportConfiguration();
						bacnetReportObj.setDeviceid(arrlist[0].trim());
						bacnetReportObj.setObjecttype(arrlist[1].trim());
						bacnetReportObj.setObjectinstance(arrlist[2].trim());
						bacnetReportObj.setObjectname(arrlist[3].trim());
						
						list.add(bacnetReportObj);
					}
				}
			}
			if(list!=null && !list.isEmpty()){
				bacnetConfigurationManager.truncateBacnetReportCfgs();
				resp = bacnetConfigurationManager.updateBacnetReportConfiguration(list);
				if (bacnetReportFlagSysConf!=null){
					bacnetReportFlag = bacnetReportFlagSysConf.getValue();
					if(bacnetReportFlag!=null && bacnetReportFlag.equals("inprogress")){
						bacnetReportFlagSysConf.setValue("done");
						systemConfigurationManager.save(bacnetReportFlagSysConf);
					}
				}
				SystemConfiguration bacnetReportUpdatedAtSysConf = systemConfigurationManager.loadConfigByName("bacnet.report.updatedAt");
				if(bacnetReportUpdatedAtSysConf!=null){
					Calendar calendar = Calendar.getInstance();
					java.util.Date ourJavaTimestampObject = new java.util.Date(calendar.getTime().getTime());
					bacnetReportUpdatedAtSysConf.setValue(ourJavaTimestampObject.toString());
					systemConfigurationManager.save(bacnetReportUpdatedAtSysConf);
				}
			} else {
				m_Logger.error("BacnetReportConfiguration is null");
				resp.setMsg("BacnetReportConfiguration is null ");
				resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
				throw new IllegalArgumentException("BacnetReportConfiguration is null ");
			}
		} catch (Exception e) {
			m_Logger.error("Error occured update report records to DB", e);
			resp.setMsg("Not able to update report records to DB");
			resp.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return resp;
	}
	
	@Path("loadBacnetReportConfigurationList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BacnetReportConfigurationList loadBacnetReportConfigurationList (@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) throws ParseException {
		
		BacnetReportConfigurationList bacnetReportConfigurationList = new BacnetReportConfigurationList();
		if (bSearch == null) {
			bSearch = false;
		}
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();		
		nameVal.put("typeNumber", page);
		nameVal.put("typeBoolean", bSearch);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return bacnetReportConfigurationList;
		}		
		
		bacnetReportConfigurationList = bacnetConfigurationManager.loadAllBacnetReportCfgs(orderby, orderway, bSearch, 
				searchField, searchString, searchOper, 
				(page - 1) * BacnetReportConfigurationList.DEFAULT_ROWS, BacnetReportConfigurationList.DEFAULT_ROWS);
		bacnetReportConfigurationList.setPage(page);
		return bacnetReportConfigurationList;
	}
}
	
