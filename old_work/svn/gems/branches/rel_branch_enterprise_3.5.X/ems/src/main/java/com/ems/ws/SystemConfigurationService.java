package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.SystemConfiguration;
import com.ems.security.exception.EmsValidationException;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.TemperatureType;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/systemconfig")
public class SystemConfigurationService {
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Autowired
	private MessageSource messageSource;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editSystemConfigValue(SystemConfiguration systemConfiguration) throws EmsValidationException {
		Response resp = new Response();
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("systemConfigName", systemConfiguration.getName());
		nameValMap.put("systemConfigValue", systemConfiguration.getValue());
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(resp!= null && resp.getStatus()!=200){		
			m_Logger.error("Validation error "+resp.getMsg());
			return resp;
		}
		
		SystemConfiguration sc = systemConfigurationManager.loadConfigByName(systemConfiguration.getName());
		if(sc != null && sc.getId() != null) {
			sc.setValue(systemConfiguration.getValue());
			systemConfigurationManager.save(sc);
		}
		else {
			systemConfigurationManager.save(systemConfiguration);
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("details/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SystemConfiguration loadConfigByName(@PathParam("name") String name) {
		
		return systemConfigurationManager.loadConfigByName(name);
		
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("gettemperaturetype")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureCount() {
		Response oResponse = new Response();
		SystemConfiguration temperatureConfig = systemConfigurationManager.loadConfigByName("temperature_unit");
		String dbTemp;
		if(temperatureConfig!=null)
		{
		dbTemp = temperatureConfig.getValue();
		if(dbTemp.equalsIgnoreCase(TemperatureType.F.getName()))
		{
			oResponse.setMsg(TemperatureType.F.getName());
		}
		else if(dbTemp.equalsIgnoreCase(TemperatureType.C.getName()))
		{
			oResponse.setMsg(TemperatureType.C.getName());
		}
		}
		return oResponse;		
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updatefloorplansize/{imagesize}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateFloorPlanImageSize(@PathParam("imagesize") String imagesize) {
		Response response = new Response();
		response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "systemConfigImageSizeValue",imagesize);
		if(response!= null && response.getStatus()!=200){		
			m_Logger.error("Validation error "+response.getMsg());
			return response;
		}
		
		SystemConfiguration sysConfig  = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		sysConfig.setValue(imagesize);
		systemConfigurationManager.update(sysConfig);		
		return response;
	}
	
}