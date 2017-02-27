package com.ems.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.NetworkSettings;
import com.ems.model.NetworkTypes;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;

import com.ems.ws.util.Response;

@Controller
@Path("/org/networksettings")
public class NetworkSettingsService {
	
	
	private static final Logger logger = Logger.getLogger("WSLogger");
	
	@Autowired
    private MessageSource messageSource;
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource
    private NetworkSettingsManager networkSettingsManager;
	
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
	
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("apply/settings")
	public javax.ws.rs.core.Response applyNetworkSettings() {
		
		try{
			networkSettingsManager.applyNetworkSettings();
			return javax.ws.rs.core.Response.ok().build();
		} catch (Exception e) {
			logger.error("***EXCEPTION OCCURED****", e);
			e.printStackTrace();
			return javax.ws.rs.core.Response.serverError()
					.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
	}

}
