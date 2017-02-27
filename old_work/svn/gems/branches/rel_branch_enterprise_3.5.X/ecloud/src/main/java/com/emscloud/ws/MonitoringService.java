package com.emscloud.ws;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;

@Controller
@Path("/org/monitoring")
public class MonitoringService {

	@Path("updateDeviceStatus")
	@POST
	@Consumes(MediaType.TEXT_PLAIN )
	@Produces({ MediaType.TEXT_PLAIN })
	public void updateDevice(String request) {
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
				
        
	}
}
