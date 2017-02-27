package com.ems.ws;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.FirmwareUpgradeScheduleList;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/firmwareupgradeschedule")
public class FirmwareUpgradeScheduleService {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "firmwareUpgradeScheduleManager")
    private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleManager;
    
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FirmwareUpgradeSchedule> loadAllFirmwareUpgradeSchedules() {
       return firmwareUpgradeScheduleManager.loadAllFirmwareUpgradeSchedules();
    }
    
    @Path("loadFirmwareUpgradeScheduleList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FirmwareUpgradeScheduleList loadFirmwareUpgradeScheduleList (@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
		
    	FirmwareUpgradeScheduleList firmwareUpgradeScheduleList =  firmwareUpgradeScheduleManager.loadFirmwareUpgradeScheduleList(orderby, orderway, (page - 1) * FirmwareUpgradeScheduleList.DEFAULT_ROWS, FirmwareUpgradeScheduleList.DEFAULT_ROWS);
    	firmwareUpgradeScheduleList.setPage(page);
		
		if(firmwareUpgradeScheduleList.getFirmwareUpgradeSchedules() == null || firmwareUpgradeScheduleList.getFirmwareUpgradeSchedules().isEmpty()){
			firmwareUpgradeScheduleList.setFirmwareUpgradeSchedules(new ArrayList<FirmwareUpgradeSchedule>());
		}
		
		return firmwareUpgradeScheduleList;
	}
    
  @Path("parseTarAndAddImagesToDB/{fileName}")
  @POST
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response parseTarAndAddImagesToDB (@PathParam("fileName") String fileName) {
    
  	Response resp = new Response();  	
  	firmwareUpgradeScheduleManager.parseAndAddImagesToDB(fileName);
  	resp.setStatus(0);
  	return resp;
  	
	}

}
