package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

import com.ems.cache.SweepTimerCache;
import com.ems.model.SweepTimer;
import com.ems.model.SweepTimerDetails;
import com.ems.service.SweepTimerManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/org/sweeptimer")
public class SweepTimerService {
	 private static final Logger m_Logger = Logger.getLogger("WSLogger");
	 public SweepTimerService() {

	 }
	 
	@Resource
	private SweepTimerManager sweepTimerManager;
	@Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;

	/**
	 * Returns Sweep Timer details
	 * 
	 * @param sweep timer name
	 * @return sweep timer details
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("list/{sweeptimername}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SweepTimer getUserList(@PathParam("sweeptimername") String sweeptimername) {
		Response resp = new Response();
	    resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "templateName", sweeptimername);
	    if(resp!= null && resp.getStatus()!=200){
	    	m_Logger.error("Validation error "+resp.getMsg());
			return null;
		}
		return sweepTimerManager.loadSweepTimerByName(sweeptimername);
	}
	
	 /**
     * Return Sweep Timer list
     * 
     * @return Sweep Timer list for the selected Facility level
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<SweepTimer> getSweepTimerList() {
		return sweepTimerManager.loadAllSweepTimer();
    }
    
    /**
     * Save the Sweep TImer
     * 
     * @return Returns the Response Object
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("save")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveSweepTimer(SweepTimer sweepTimer) {
    	Response reponse = new Response();
    	Iterator<SweepTimerDetails> oItr = sweepTimer.getSweepTimerDetails().iterator();
    	while (oItr.hasNext()) {
    		SweepTimerDetails oSTD = oItr.next();
    		oSTD.setSweepTimer(sweepTimer);
    	}
    	SweepTimer savedObj = sweepTimerManager.saveSweepTimer(sweepTimer);
    	SweepTimerCache.getInstance().updateSweepTimer(savedObj);
		return reponse;
    }
    
  
}
