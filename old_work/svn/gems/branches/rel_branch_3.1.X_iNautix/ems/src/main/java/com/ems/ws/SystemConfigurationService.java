package com.ems.ws;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.SystemConfiguration;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.TemperatureType;
import com.ems.ws.util.Response;

@Controller
@Path("/systemconfig")
public class SystemConfigurationService {
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editSystemConfigValue(SystemConfiguration systemConfiguration) {
		
		Response resp = new Response();	
		systemConfigurationManager.save(systemConfiguration);
		return resp;
	}
	
	@Path("details/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SystemConfiguration loadConfigByName(@PathParam("name") String name) {
		
		return systemConfigurationManager.loadConfigByName(name);
		
	}
	
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
	
}