package com.ems.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.EventsAndFault;
import com.ems.model.NetworkDetails;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.NetworkSettings;
import com.ems.model.NetworkTypes;
import com.ems.model.SystemConfiguration;
import com.ems.service.BacnetManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.ApplyNetworkStatus;
import com.ems.types.NetworkType;
import com.ems.util.Constants;
import com.ems.ws.util.Response;

@Controller
@Path("/org/networksettings")
public class NetworkSettingsService {
	
	
	private static final Logger logger = Logger.getLogger("WSLogger");
	private static Logger syslog = Logger.getLogger("SysLog");
	
	@Autowired
    private MessageSource messageSource;
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private NetworkSettingsManager networkSettingsManager;
	
	@Resource
	private EventsAndFaultManager eventsAndFaultManager;
 
	@Resource
	private BacnetManager bacnetManager;
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("saveNetworkInterfaceMappings")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveNetworkInterfaceMappings(List<NetworkInterfaceMapping> networkInterfaceMappings){
		Response response = new Response();
		NetworkSettings ns= null;
		NetworkTypes nt = null;
		try {
			for(NetworkInterfaceMapping nim : networkInterfaceMappings){			
				
				ns = networkSettingsManager.loadNetworkSettingsById(nim.getNetworkSettingsId());
				nt = networkSettingsManager.loadNetworkTypeById(nim.getNetworkTypeId());				
				if(nim.getNetworkSettingsId() != null && nim.getNetworkSettingsId()!=0){					
					if(nim.getId() == null || nim.getId()==0){					
						nim.setId(null);
						nim.setNetworkSettings(ns);
						nim.setNetworkTypes(nt);
						networkSettingsManager.saveNetworkInterfaceMappings(nim);
					}else{
						nim.setNetworkSettings(ns);
						nim.setNetworkTypes(nt);
						networkSettingsManager.saveNetworkInterfaceMappings(nim);
					}				
				}else{					
					nim.setNetworkSettings(ns);
					nim.setNetworkTypes(nt);
					networkSettingsManager.saveNetworkInterfaceMappings(nim);
				}
			}
			response.setStatus(0);
		} catch (Exception e) {
			response.setStatus(1);
			response.setMsg("Some exception occurred "+e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("saveNetworkSettings")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveNetworkSettings(List<NetworkSettings> nsList){
		
		Response response = new Response();
		for(NetworkSettings ns : nsList){
			System.out.println("===ns ip address is "+ns.getIpaddress()+"  and type is "+ns.getConfigureIPV4()+"  name is "+ns.getName());			
			networkSettingsManager.saveNetworkSettings(ns);			
		}
		//networkSettingsManager.enableDisableDHCPServer();
		networkSettingsManager.disableUnmappedPorts();
		return response;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("apply/settings")
	public javax.ws.rs.core.Response applyNetworkSettings(NetworkDetails networkDetails) {
		
		final SystemConfiguration config = systemConfigurationManager.loadConfigByName(Constants.APPLY_NETWORK_STATUS_KEY);
		if(config != null && config.getValue().equalsIgnoreCase(ApplyNetworkStatus.INPROGRESS.getName())){
			final String msg = "Network settings request already in progress.";
			syslog.error(msg);
			return javax.ws.rs.core.Response.notModified(msg).build();
		}
		String status = ApplyNetworkStatus.INPROGRESS.getName();
		try{
			// Log the json object
			if (networkDetails != null) {
				ObjectMapper mapper = new ObjectMapper();
				syslog.info("*********APPLYGING NETWORK SETTINGS: JSON String passed is:"
						+ mapper.writeValueAsString(networkDetails));
			}
			config.setValue(status);
			systemConfigurationManager.save(config);

			final Map<String, String> mapHelper = new HashMap<String, String>();
			// Find the earlier configured bacnet port if any
			int bacnetPortPrev = 0;
			final List l = networkSettingsManager.loadCustomFieldsFromNetworkType(NetworkType.BACnet.getName());
			final boolean isBacnetEnabledPrev = l != null && ((Object[]) l.get(0)).length >= 2	&& ((Object[]) l.get(0))[1] != null	&& (Boolean) (((Object[]) l.get(0))[1]);
			String bacnetinterfacePrev = "";
			if (isBacnetEnabledPrev) {
				bacnetinterfacePrev = l != null	&& ((Object[]) l.get(0)).length >= 2 && ((Object[]) l.get(0))[0] != null ? (((Object[]) l.get(0))[0]).toString() : null;
				if (!StringUtils.isEmpty(bacnetinterfacePrev)) {
					try {
						bacnetPortPrev = bacnetManager.getConfig().getServerPort();
						mapHelper.put("bacnetListenPrevInterface", bacnetinterfacePrev);
						mapHelper.put("bacnetListenPrevPort", String.valueOf(bacnetPortPrev));
					} catch (Exception e) {
						syslog.error("*****************ERROR :::PROBLEM IN READING PREVIOUS PORT FROM BACNETMANAGER********");
					}
				}
			}
			syslog.info("bacnet previous info port:" + bacnetPortPrev+ " interface:" + bacnetinterfacePrev);

			if (networkSettingsManager.applyNetworkSettings(
					networkDetails.getNetworkInterfaceMapping(),
					networkDetails.getInterfaces(), mapHelper)) {
				status = ApplyNetworkStatus.SUCCESS.getName();
				syslog.info("****Network settings succeessfully appplied*******");
				eventsAndFaultManager.addUpdateSingleAlarm(null, EventsAndFault.NETWORK_NOTIFICATION_TYPE, false);
				return javax.ws.rs.core.Response.ok().build();
			}else{
				status = ApplyNetworkStatus.FAILURE.getName();
				final String msg = "Network Settings are not applied. Please contact admin.";
				syslog.error(msg);
				eventsAndFaultManager.addUpdateSingleAlarm(msg, EventsAndFault.NETWORK_NOTIFICATION_TYPE, true);
				return javax.ws.rs.core.Response.notModified(msg).build();
			}
		} catch (Exception e) {
			syslog.error("***EXCEPTION OCCURED****", e);
			eventsAndFaultManager.addUpdateSingleAlarm(e.getMessage(), EventsAndFault.NETWORK_NOTIFICATION_TYPE, true);
			status = ApplyNetworkStatus.FAILURE.getName();
			return javax.ws.rs.core.Response.serverError()
					.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
		}finally{
			config.setValue(status);
			systemConfigurationManager.save(config);
		}
		
	}
	
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("applySettingsStatus")
	public String getApplySettingsStatus() {
		String status = networkSettingsManager.getApplySettingsStatus();		
		return status;		
	}

}
