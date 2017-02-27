package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
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
import org.springframework.stereotype.Controller;

import com.ems.model.FirmwareUpgradeSchedule;
import com.ems.model.FirmwareUpgradeScheduleList;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FirmwareUpgradeScheduleManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/org/firmwareupgradeschedule")
public class FirmwareUpgradeScheduleService {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;
    
    @Autowired
    private MessageSource messageSource;
    @Resource
    private SystemConfigurationManager systemConfigurationManager;
    private static final Logger logger = Logger.getLogger("WSLogger");

    @Resource(name = "firmwareUpgradeScheduleManager")
    private FirmwareUpgradeScheduleManager firmwareUpgradeScheduleManager;
    
	@PreAuthorize("hasAnyRole('Admin')")
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FirmwareUpgradeSchedule> loadAllFirmwareUpgradeSchedules() {
       return firmwareUpgradeScheduleManager.loadAllFirmwareUpgradeSchedules();
    }
    
	@PreAuthorize("hasAnyRole('Admin')")
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
    
	@PreAuthorize("hasAnyRole('Admin')")
  @Path("parseTarAndAddImagesToDB/{fileName}")
  @POST
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response parseTarAndAddImagesToDB (@PathParam("fileName") String fileName) {
    
  	Response resp = new Response();
  	/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "userdata", fileName);
  	if(resp!= null && resp.getStatus()!=200){
		return resp;
	}*/
  	firmwareUpgradeScheduleManager.parseAndAddImagesToDB(fileName);
  	resp.setStatus(0);
  	return resp;
  	
	}

}
