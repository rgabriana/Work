package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.action.SpringContext;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.SystemConfiguration;
import com.ems.model.Wds;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.device.wds.WDSImpl;
import com.ems.service.ErcBatteryReportSchedulerJob;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.GatewayManager;
import com.ems.service.SwitchManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.WdsManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.CommonUtils;
import com.ems.vo.model.WdsList;
import com.ems.ws.util.Response;
import com.ems.server.SchedulerManager;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ExtendedColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@Path("/org/wds")
public class WdsService {
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    private EventsAndFaultManager eventMgr = null;
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "gatewayManager")
    private GatewayManager gatewayManager;
    
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;
    
    @Resource(name = "switchManager")
    private SwitchManager switchManager;
    
    @Autowired
    private MessageSource messageSource;
    
    @Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
    
    JobDetail ercBatteryReportSchedulerJob;

    public WdsService() {

    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    /**
     * Return wds list
     * 
     * @param property
     *            (company|campus|building|floor|area|secondarygateway|switch)
     * @param pid
     *            property unique identifier
     * @return Wds list for the selected org level
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor')")
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getWdsList(@PathParam("property") String property, @PathParam("pid") Long pid) {
    	List<Wds> wdsList = null;
        Response resp = new Response();
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "fixProperty", property);
        if(resp!=null && resp.getStatus()!=200){
        	m_Logger.error("Validation error "+resp.getMsg());
        	return wdsList;
        }
        if (property.equalsIgnoreCase("company")) {
        	wdsList = wdsManager.loadAllWds();
        } else if (property.equalsIgnoreCase("campus")) {
        	wdsList = wdsManager.loadWdsByCampusId(pid);
        } else if (property.equalsIgnoreCase("building")) {
        	wdsList = wdsManager.loadWdsByBuildingId(pid);
        } else if (property.equalsIgnoreCase("floor")) {
        	wdsList = wdsManager.loadWdsByFloorId(pid);
        } else if (property.equalsIgnoreCase("area")) {
        	wdsList = wdsManager.loadWdsByAreaId(pid);
        } else if (property.equalsIgnoreCase("secondarygateway")) {
        	wdsList = wdsManager.loadAllCommissionedWdsByGatewayId(pid);
        } else if (property.equalsIgnoreCase("switch")) {
        	wdsList = wdsManager.loadCommissionedWdsBySwitchId(pid);
        }
        if(wdsList!=null&&!wdsList.isEmpty())
        {
        	wdsManager.setWDSBatteryLevel(wdsList);
        	for(Wds wds :wdsList){
        		if(wds.getSwitchId() != null){
        			if(switchManager.getSwitchById(wds.getSwitchId()) != null ){
        				wds.setSwitchName(switchManager.getSwitchById(wds.getSwitchId()).getName());
        			}
        		}
        		if(wds.getGatewayId() != null){
        			Gateway gw = gatewayManager.loadGateway(wds.getGatewayId());
    	           	if(gw!=null)
    	           	{
    	           		wds.setGatewayName(gw.getName());
    	           	}
        		}
        	}
        } 
        return wdsList;
    }


    /**
     * Only discovered WDS by gateway id support for displaying them in the commissioning window.
     * @param property
     * @param pid
     * @return of WDSs
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("list/discovered/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getDiscoveredWdsList(@PathParam("property") String property, @PathParam("pid") Long pid) {
    	Response resp = new Response();
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "fixProperty", property);
        if(resp!=null && resp.getStatus()!=200){
        	m_Logger.error("Validation error "+resp.getMsg());
        	return null;
        }
        if (property.equalsIgnoreCase("secondarygateway")) {
            return wdsManager.getUnCommissionedWDSList(pid);
        }
        return null;
    }

    /**
     * Updates the position of the selected wds on the floorplan
     * 
     * @param wds
     *            List of selected wds with their respective x & y co-ordinates
     *            "<wdses><wds><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></wds></wdses>"
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateWdsPosition(List<Wds> wdses) {
        m_Logger.debug("Wds: " + wdses.size());
        Iterator<Wds> itr = wdses.iterator();
        StringBuffer wdsNames = new StringBuffer("");
        boolean log = false;
        while (itr.hasNext()) {
            Wds oWds = (Wds) itr.next();
            if (oWds.getXaxis() != null && oWds.getYaxis() != null) {
                wdsManager.updatePositionById(oWds);
                wdsNames.append(wdsManager.getWdsSwitchById(oWds.getId()).getName() + "(X:" + oWds.getXaxis() + " Y:" + oWds.getYaxis() + ") ");
                log = true;
            }
        }
        if(log) {
            userAuditLoggerUtil.log("Update ERC positions for " + wdsNames, UserAuditActionType.ERC_Update.getName());
        }
        return new Response();
    }

    /**
     * return wds discovery status
     * 
     * @return Discovery status as part of response object
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getdiscoverystatus")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getDiscoveryStatus() {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.getDiscoveryStatus());
        return oStatus;
    }

    /**
     * Returns WDS Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return fixture details
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("details/{wdsId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Wds getFixtureDetails(@PathParam("wdsId") Long wdsId) {
        return wdsManager.getWdsSwitchById(wdsId);
    }
    
    /**
     * Initiates the commission process
     * @param floorId
     * @param gatewayId
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("startcommission/floor/{floorId}/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response commission(@PathParam("floorId") Long floorId,
            @PathParam("gatewayId") Long gatewayId) {
        Response response = new Response();
        int status = wdsManager.commissionWds(floorId, gatewayId);
        response.setStatus(status);
        return response;
    }
    
    /**
     * updates Wds during Wds commissioning process updates the fields
     * of Wds which are editable in the Wds commissioning form
     * 
     * @param Wds
     *          <switches>
                <Wds>
                <id>1227</id>
                <name>WDS002</name>
                <floorid>10</floorid>
                <campusid>4</campusid>
                <xaxis>0</xaxis>
                <yaxis>0</yaxis>
                <dimmercontrol>0</dimmercontrol>
                <activecontrol>1</activecontrol>
                <switchtype>Real</switchtype>
                <state>DISCOVERED</state>
                <gatewayid>308</gatewayid>
                </Wds>
                </switches>
     * 
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("updateduringcommission")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateDuringCommission(Wds wdsSwitch) {
        Response response = new Response();
        // Get the fixture from the database
        Wds savedWds = wdsManager.getWdsSwitchById(wdsSwitch.getId());
        // Let's populate the values on the fixture
        savedWds.setName(wdsSwitch.getName());
        savedWds.setXaxis(wdsSwitch.getXaxis());
        savedWds.setYaxis(wdsSwitch.getYaxis());
        wdsManager.update(savedWds);
        //TODO: change audit component
        return response;
    }
    
    /**
     * 
     * @param wdsSwitch
     * @return
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("startcommission")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response startCommission(Wds wdsSwitch) {
        Response response = new Response();
        // Get the fixture from the database
        Wds savedWds = wdsManager.getWdsSwitchById(wdsSwitch.getId());
        int status = WDSImpl.getInstance().setWdsWirelessParams(savedWds.getId(), savedWds.getGatewayId());
        response.setStatus(status);
        if (status == 0) {
            userAuditLoggerUtil.log("Start commission of ERC " + wdsSwitch.getName(),
                    UserAuditActionType.ERC_Commission.getName());
        }

        return response;
    }
    
    /**
     * return fixture commission status
     * 
     * @return Commission status as part of response object
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getcommissionstatus")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCommissionStatus() {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.getCommissioningStatus());
        return oStatus;
    }
    
    /**
     * starts wds discovery process for selected floor and gateway
     * 
     * @param floorId
     *            Floor unique identifier
     * @param gatewayId
     *            Gateway unique identifier
     * @return
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("startWdsnetworkdiscovery/floor/{floorId}/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response startWDSNetworkDiscovery(@PathParam("floorId") Long floorId,
            @PathParam("gatewayId") Long gatewayId) {
        Response oStatus = new Response();
        oStatus.setStatus(wdsManager.startNetworkDiscovery(floorId,
                gatewayId));
        userAuditLoggerUtil.log("Start ERC network discovery for gateway "
                + gatewayManager.loadGateway(gatewayId).getGatewayName(),
                UserAuditActionType.ERC_Discovery.getName());
        return oStatus;
    }

    /**
     * cancels wds discovery process
     * 
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("cancelwdsnetworkdiscovery")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response cancelWdsNetworkDiscovery() {
        wdsManager.cancelNetworkDiscovery();
        userAuditLoggerUtil.log("Cancel ERC network discovery",
                UserAuditActionType.ERC_Discovery.getName());
        return new Response();
    }
    
    /**
     * used to exit WDS commissioning process
     * 
     * @param gatewayId
     *            Gateway unique identifier
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("exitcommission/gateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response exitCommission(@PathParam("gatewayId") Long gatewayId) {
        Response response = new Response();
        int status = wdsManager.exitCommissioning(gatewayId);
        response.setStatus(status);
        userAuditLoggerUtil.log("Exit ERC comission for gateway "
                + gatewayManager.loadGateway(gatewayId).getGatewayName(),
                UserAuditActionType.ERC_Commission.getName());
        return response;
    }
    
    /**
     * returns commission status of selected fixtures
     * 
     * @param wdsId
     *            Fixture unique identifier
     * @return Commission status as part of response object
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getcommissionstatus/wds/{wdsId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCommissionStatusString(
            @PathParam("wdsId") long wdsId) {
        Response oStatus = new Response();
        oStatus.setMsg(wdsManager.getCommissioningStatus(wdsId));
        return oStatus;
    }

    /**
     * return count of wds associated with the gateway
     * @param gatewayId
     * @return Response status
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("getwdscountbygateway/{gatewayId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getWdsCountByGateway(@PathParam("gatewayId") Long gatewayId) {
        Response oStatus = new Response();
        List<Wds> oList = wdsManager.getUnCommissionedWDSList(gatewayId);
        oStatus.setStatus((oList != null ? oList.size() : 0));
        return oStatus;
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("updateSwitchWds/{switchId}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateSwitchWds(@PathParam("switchId") Long switchId, List<Wds> wdsList) {
    	Switch sw = switchManager.getSwitchById(switchId);
    	Long gemsGroupId = sw.getGemsGroup().getId();
    	SwitchGroup swGrp = switchManager.getSwitchGroupByGemsGroupId(gemsGroupId);
    	List<Wds> existingWds = wdsManager.loadCommissionedWdsBySwitchId(switchId);
    	Set<Long> existingWdsIds = new HashSet<Long>();
    	if(existingWds != null &&  existingWds.size() > 0) {
    		for (Wds each: existingWds) {
    			existingWdsIds.add(each.getId());
    		}
    	}
    	
    	if(wdsList != null && wdsList.size() > 0) {
    		for(Wds each: wdsList) {
    			if(existingWdsIds.contains(each.getId())) {
    				existingWdsIds.remove(each.getId());
    			}
    			else {
    				Wds updateWds = wdsManager.loadWdsById(each.getId());
    				updateWds.setWdsSwitch(sw);
    				updateWds.setSwitchGroup(swGrp);
    				updateWds.setAssociationState(ServerConstants.WDS_STATE_ASSOCIATED);
    				wdsManager.update(updateWds);
    			}
    		}
    	}
    	
    	for(Long wdsId: existingWdsIds) {
    		Wds updateWds = wdsManager.loadWdsById(wdsId);
			updateWds.setAssociationState(ServerConstants.WDS_STATE_NOT_ASSOCIATED);
			wdsManager.update(updateWds);
    	}
    	
        Response response = new Response();
        response.setMsg("S");
        return response;
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("deleteWds/{wdsId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteWds(@PathParam("wdsId") Long wdsId) {
        Response response = new Response();
        Wds oWds = wdsManager.getWdsSwitchById(wdsId);
        if (oWds != null) {
            if (oWds.getWdsSwitch() != null) {
                response.setStatus(1);
            } else {
                wdsManager.deleteWds(wdsId);
            }
        }
        return response;
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee')")
    @Path("duplicatecheck/{wdsname}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response checkDuplicate(@PathParam("wdsname") String wdsname) {
		Response oStatus = new Response();
		oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "groupName", wdsname);
		if(oStatus!=null && oStatus.getStatus()!=200){
			m_Logger.error("Validation error "+oStatus.getMsg());
        	return oStatus;
        }
		Wds oWds = wdsManager.getWdsSwitchByName(wdsname);		
		if (oWds!=null && oWds.getId() > 0) {
			oStatus.setMsg(oWds.getName());
			oStatus.setStatus(0);
			return oStatus;
		}
		oStatus.setMsg("0");
		oStatus.setStatus(0);
		return oStatus;
	}
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("save/{ewsid}/{ewsname}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteWds(@PathParam("ewsid") Long ewsid, @PathParam("ewsname") String ewsname) {
        Response response = new Response();
        Wds oWds = wdsManager.getWdsSwitchById(ewsid);       
        oWds.setName(ewsname);
        wdsManager.update(oWds);
        return response;
    }
    
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("updatelog/{id}/{status}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response logWds(@PathParam("id") Long id , @PathParam("status") String status) {
    	eventMgr = (EventsAndFaultManager)SpringContext.getBean("eventsAndFaultManager");
        Response response = new Response();        
        Gateway gw = gatewayManager.loadGateway(id);
        if(status.equalsIgnoreCase("Success"))
        {
        	eventMgr.addEvent(gw,"ERC Commissioning Successful", EventsAndFault.EWS_COMISSION, EventsAndFault.MAJOR_SEV_STR);        	
        }
        else
        {
        	eventMgr.addEvent(gw,"ERC Commissioning Failed", EventsAndFault.EWS_COMISSION, EventsAndFault.MAJOR_SEV_STR);
        }
        return response;
    }
    
   
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("getWdsReportData")
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public WdsList getWdsReportData(@RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {

		Response response = new Response();
		/*response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "userdata", userdata);
		if(response!=null && response.getStatus()!=200){
			m_Logger.error("Validation error "+response.getMsg());
        	return new WdsList();
        }*/
		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;
				
		String[] params = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					Double pageNum = Double.parseDouble(keyval[1]);
					pageNum = Math.floor(pageNum);
					page= pageNum.intValue();
					//page = Integer.parseInt(keyval[1]);
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
		
		WdsList oWdsList = wdsManager.loadWdsList(orderBy,orderWay,(page - 1) * WdsList.DEFAULT_ROWS, WdsList.DEFAULT_ROWS);
		oWdsList.setPage(page);
		
		List<Wds> wdsList = oWdsList.getWds();
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
			int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        for (Wds wds : wdsList) {
	        	 //Gateway gw = gatewayManager.loadGateway(wds.getGatewayId());
	        	 //if(gw!=null)
	        	 //{
	        		 //wds.setGatewayName(gw.getName());
	        		 wdsManager.setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
	        	// }
	        }
	        oWdsList.setWds(wdsList);
		}
		return oWdsList;
	}
	
	
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in csv format
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("exportwdsdata")
	@POST
	@Produces("application/csv")
	public javax.ws.rs.core.Response exportWdsData() {

		StringBuffer output = new StringBuffer("");

		List<Wds> wdsList = wdsManager.loadAllWds();
        
	    output.append("ERC Battery Report\r\n\n");
	    
	    
        output.append("ERC Name"
                + ","
                + "Location"
                + ","
                + "Battery Level"
                + ","
                + "Last Reported Time");
        
        if(!ArgumentUtils.isNullOrEmpty(wdsList)){
        	int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        
        	for (int i = 0; i < wdsList.size(); i++) {
                Wds wds = wdsList.get(i);
                output.append("\r\n");
                wdsManager.setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
                String name = wds.getName();
                String location = wds.getLocation();
                String batteryLevel = wds.getBatteryLevel();
                String captureAtStr = wds.getCaptureAtStr();
                output.append(name
                        + ","
                        + location
                        + ","
                        + batteryLevel
                        + ","
                        + captureAtStr
                        );
            }
        }
        
		return javax.ws.rs.core.Response
				.ok(output.toString(), "text/csv")
				.header("Content-Disposition",
						"attachment;filename=ERC_Battery_Report.csv")
				.build();
	}
	
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in pdf format
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("exportwdspdfdata")
	@POST
	@Produces("application/pdf")
	public javax.ws.rs.core.Response exportWdsPdfData() {
		
		int extraLargeTextZize = 16;
		int MediumTextZize = 8;
		int smallTextZize = 7;
		Color customGreenColor = new Color (0, 51, 0);
		Font headerFont = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,extraLargeTextZize, Font.BOLD,customGreenColor);
		Font tahomaSmallBold = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,MediumTextZize,Font.BOLD,customGreenColor);
		Font tahomaSmall = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,smallTextZize,Font.NORMAL,customGreenColor);
		
		
		List<Wds> wdsList = wdsManager.loadAllWds();
		
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
        	int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        
        	for (int i = 0; i < wdsList.size(); i++) {
                Wds wds = wdsList.get(i);
                wdsManager.setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
            }
       }
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, baos);
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		document.open();
		// TITLE LABEL
		Chunk chunk = new Chunk("ERC Battery Report",headerFont); 
		Paragraph headerPara = new Paragraph(chunk);
		headerPara.setFont(headerFont);
		headerPara.setIndentationLeft((PageSize.A4.getWidth()/2)-70);
		headerPara.setSpacingBefore(38);
		try {
			document.add(headerPara);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		int[] widths = { 5, 15, 9, 9};
		PdfPTable table = new PdfPTable(4);
	    table.getDefaultCell().setBackgroundColor(ExtendedColor.LIGHT_GRAY);
	    table.setWidthPercentage(108);
		try {
			table.setWidths(widths);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		table.addCell(new Phrase("ERC Name", tahomaSmallBold));
		table.addCell(new Phrase("Location", tahomaSmallBold));
		table.addCell(new Phrase("Battery Level", tahomaSmallBold));
		table.addCell(new Phrase("Last Reported Time", tahomaSmallBold));
		
		table.getDefaultCell().setBackgroundColor(null);
		
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
		for (Wds wds : wdsList) {
			PdfPCell name= new PdfPCell();
			name.addElement(new Phrase(wds.getName(), tahomaSmall));
			table.addCell(name);
			
			PdfPCell loc= new PdfPCell();
			loc.addElement(new Phrase(wds.getLocation(),tahomaSmall));
			table.addCell(loc);
			
			PdfPCell batteryLvl = new PdfPCell();
			batteryLvl.addElement(new Phrase(wds.getBatteryLevel(), tahomaSmall));
			table.addCell(batteryLvl);
			
			PdfPCell capAt = new PdfPCell();
			capAt.addElement(new Phrase(wds.getCaptureAtStr(), tahomaSmall));
			table.addCell(capAt);
			
			}
		}
		table.setSpacingBefore(20);
		try {
			document.add(table);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		document.close();
        
		return javax.ws.rs.core.Response
				.ok((Object) baos.toByteArray(), "application/pdf")
				.header("Content-Disposition",
						"attachment;filename=ERC_Battery_Report.pdf")
				.build();
	}
	
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("saveErcBatteryReportScheduler/{enable}/{email}/{time}/{recurrence}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveErcBatteryReportScheduler(@PathParam("enable") String enable,@PathParam("email") String email,
			@PathParam("time") String time,@PathParam("recurrence") String recurrence){
		Response response = new Response();
		String cronstatement = "";
		String emailString = "";
		Response resp = new Response();
        /*Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("ercBatteryEnabled", enable);
    	nameValMap.put("email", email);
    	nameValMap.put("ercBatterytime", time);
    	nameValMap.put("recurrence", recurrence);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
    		return null;
    	}*/
		if("default".equals(email)){
			emailString = "";
		}else{
			emailString = email;
		}
		
		
		if("default".equals(recurrence)){
			cronstatement = "";
		}else{
			
			if("default".equals(time)){
				cronstatement = "";
			}else{
				
				SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
			    SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
			    Date date = null;
				try {
					date = parseFormat.parse(time);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    //System.out.println(parseFormat.format(date) + " = " + displayFormat.format(date));
			    
			    String[] timeStringArray = displayFormat.format(date).split(":");
				
				String timeHH = timeStringArray[0];
				
				String timeMM = timeStringArray[1];
				
				cronstatement = "0" + " " + timeMM + " " + timeHH + " ? * " + recurrence;
			}
				
		}
		
		
		
		SystemConfiguration ercBatteryReportSchedulerEnable = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.enable");
		if(ercBatteryReportSchedulerEnable!=null)
		{
			ercBatteryReportSchedulerEnable.setValue(enable);
			systemConfigurationManager.save(ercBatteryReportSchedulerEnable);				
		}
		
		SystemConfiguration ercBatteryReportSchedulerEmail = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.email");
		if(ercBatteryReportSchedulerEmail!=null)
		{
			ercBatteryReportSchedulerEmail.setValue(emailString);
			systemConfigurationManager.save(ercBatteryReportSchedulerEmail);			
		}
		
		SystemConfiguration ercBatteryReportSchedulerCronexpression = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.cronexpression");
		if(ercBatteryReportSchedulerCronexpression!=null)
		{
			ercBatteryReportSchedulerCronexpression.setValue(cronstatement);
			systemConfigurationManager.save(ercBatteryReportSchedulerCronexpression);			
		}
		
		
		String ercBatteryReportSchedulerJobName = "ercBatteryReportSchedulerJob";
		String ercBatteryReportSchedulerTriggerName = "ercBatteryReportSchedulerJobTrigger";
		//String cronErcBatteryReportSchedulerDefault = "0 30 16 ? * MON,TUE,WED,THU,FRI,SAT,SUN";
		
		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(ercBatteryReportSchedulerJobName, SchedulerManager
									.getInstance().getScheduler()
									.getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(ercBatteryReportSchedulerJobName, SchedulerManager
										.getInstance().getScheduler()
										.getSchedulerName())) == false)
					m_Logger.debug("Failed to delete Quartz job" + ercBatteryReportSchedulerJobName);
			}
		}catch (Exception e) {
			m_Logger.error(e.getMessage(), e);
		}
		
		if(!"".equals(cronstatement)){
			try {
					
				// create job
				ercBatteryReportSchedulerJob = newJob(ErcBatteryReportSchedulerJob.class)
						.withIdentity(
								ercBatteryReportSchedulerJobName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName()).build();
				// create trigger
				CronTrigger ercBatteryReportSchedulerJobTrigger = (CronTrigger) newTrigger()
						.withIdentity(
								ercBatteryReportSchedulerTriggerName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName())
						.withSchedule(
								CronScheduleBuilder.cronSchedule(cronstatement))
						.startNow().build();

				// schedule job
				SchedulerManager.getInstance().getScheduler()
						.scheduleJob(ercBatteryReportSchedulerJob, ercBatteryReportSchedulerJobTrigger);
				
			} catch (Exception e) {
				m_Logger.error(e.getMessage(), e);
			}
			
		}
		
		return response;
		
	}
	
	
	/**
	 * Service to do a rma replacement
	 * @param fromWdsId - Wds which needs to be replaced
	 * @param toWdsId - New Wds
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("rma/{from}/{to}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rmaWds(@PathParam("from")Long fromWdsId, @PathParam("to")Long toWdsId) {
	    String oldWdsName = wdsManager.loadWdsById(fromWdsId).getName();
		String newWdsName = wdsManager.loadWdsById(toWdsId).getName();
        boolean status = wdsManager.rmaWds(fromWdsId, toWdsId);
        
        Response oResponse = new Response();
        
        if(status){
            oResponse.setStatus(1);
            oResponse.setMsg("RMA successful");
        }else{
            oResponse.setStatus(0);
            oResponse.setMsg("RMA unsuccessful");
        }
        
      userAuditLoggerUtil.log("RMA ERC " + oldWdsName
                + " to " + newWdsName,
                UserAuditActionType.ERC_RMA.getName());
        
        return oResponse;
        
    }
	
	/**
	 * Service to do a wdsno replacement.
	 * @param fromWdsId - Wds which needs to be replaced
	 * @param toWdsId - New Wds
	 * @return
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("rmastart/{from}/{to}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response rmaWdsStart(@PathParam("from")Long fromWdsId, @PathParam("to")Long toWdsId) {
	    boolean status = wdsManager.rmaWdsStart(fromWdsId, toWdsId);
        
        Response oResponse = new Response();
        
        if(status){
            oResponse.setStatus(1);
            oResponse.setMsg("RMA WDS Started");
        }else{
            oResponse.setStatus(0);
            oResponse.setMsg("RMA WDS Start Failed");
        }
        
       return oResponse;
        
    }
	
}
