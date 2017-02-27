package com.ems.ws;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.cache.SweepTimerCache;
import com.ems.model.SweepTimer;
import com.ems.model.SweepTimerDetails;
import com.ems.model.User;
import com.ems.service.SweepTimerManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/sweeptimer")
public class SweepTimerService {
	
	 public SweepTimerService() {

	 }
	 
	@Resource
	private SweepTimerManager sweepTimerManager;

	/**
	 * Returns Sweep Timer details
	 * 
	 * @param sweep timer name
	 * @return sweep timer details
	 */
	@Path("list/{sweeptimername}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SweepTimer getUserList(@PathParam("sweeptimername") String sweeptimername) {
		return sweepTimerManager.loadSweepTimerByName(sweeptimername);
	}
	
	 /**
     * Return Sweep Timer list
     * 
     * @return Sweep Timer list for the selected Facility level
     */
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
