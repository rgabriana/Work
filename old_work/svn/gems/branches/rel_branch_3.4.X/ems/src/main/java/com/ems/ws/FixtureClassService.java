package com.ems.ws;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Ballast;
import com.ems.model.Bulb;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureClass;
import com.ems.model.FixtureClassList;
import com.ems.model.LocatorDevice;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;





@Controller
@Path("/org/fixtureclassservice")
public class FixtureClassService {
	
	@Resource(name = "fixtureClassManager")
    private FixtureClassManager fixtureClassManager;

	@Resource(name = "bulbManager")
    private BulbManager bulbManager;

	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	
	@Resource
	private EventsAndFaultManager eventManager;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	FixtureManager fixtureManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	@Path("add/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();	
		FixtureClass fixtureClass = fixtureClassManager.addFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
		userAuditLoggerUtil.log(""+fixtureClass.toString(), UserAuditActionType.FixtureType_Add.getName());
		resp.setMsg(fixtureClass.getId().toString());
		return resp;
	}
	
	@Path("edit/id/{id}/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editFixtureClass(@PathParam("id") String id,@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();	
		FixtureClass uiFixtureClass = new FixtureClass();
		uiFixtureClass.setBallast(ballastManager.getBallastById(Long.parseLong(ballastId)));
		uiFixtureClass.setBulb(bulbManager.getBulbById(Long.parseLong(bulbId)));
		uiFixtureClass.setName(name);
		uiFixtureClass.setNoOfBallasts(Integer.parseInt(noOfBallasts));
		uiFixtureClass.setVoltage(Integer.parseInt(voltage));
		FixtureClass dbFixtureClass = fixtureClassManager.getFixtureClassById(Long.parseLong(id));		
		String changeLog = dbFixtureClass.compare(uiFixtureClass);
		fixtureClassManager.editFixtureClass(id,name, noOfBallasts, voltage,ballastId,bulbId);
		
		List<BigInteger> fixturesIdList = fixtureManager.loadFixturesIdListByBallastId(Long.parseLong(ballastId));
	    synchronized (this) {
            //Invalidate the Fixture Cache for the list of fixtures
            if (fixturesIdList != null) {
            	for (BigInteger fixtureId: fixturesIdList)
                {
            		FixtureCache.getInstance().invalidateDeviceCache(fixtureId.longValue());
                }
            }
        }
		userAuditLoggerUtil.log(""+changeLog, UserAuditActionType.FixtureType_Edit.getName());
		return resp;
	}
	
	@Path("deleteFixtureClass/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteFixtureClassById (@PathParam("id") Long id) {
		
		Response response = new Response();
		
		Integer fixtureCount = fixtureClassManager.getFixtureCountByFixtureClassId(id);
		
		if(fixtureCount.intValue() > 0){
			response.setStatus(1);
		}
		
		if (response.getStatus() == 0) {
			userAuditLoggerUtil.log(""+fixtureClassManager.getFixtureClassById(id).toString(), UserAuditActionType.FixtureType_Delete.getName());
			fixtureClassManager.deleteFixtureClassById(id);
		}
		return response;
	}
	
	
	@Path("getfixtureCount/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getFixtureCountByFixtureClassId (@PathParam("id") Long id) {
		
		Response response = new Response();
		
		Integer fixtureCount = fixtureClassManager.getFixtureCountByFixtureClassId(id);
		
		if(fixtureCount.intValue() > 0){
			response.setStatus(1);
		}else{
			response.setStatus(0);
		}
		
		return response;
	}
	
	

	@Path("loadFixtureClassList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureClassList loadFixtureClassList (@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
		
		FixtureClassList fixtureClassList =  fixtureClassManager.loadFixtureClassList(orderway, (page - 1) * FixtureClassList.DEFAULT_ROWS, FixtureClassList.DEFAULT_ROWS);
		fixtureClassList.setPage(page);
		
		if(fixtureClassList.getFixtureclasses() == null || fixtureClassList.getFixtureclasses().isEmpty()){
			fixtureClassList.setFixtureclasses(new ArrayList<FixtureClass>());
		}
		
		return fixtureClassList;
	}

	@Path("loadAllFixtureClasses")
	@GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FixtureClass> loadAllFixtureClasses () throws ParseException {
		
		return fixtureClassManager.loadAllFixtureClasses();
	}
	
	@Path("details/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureClass getFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		return fixtureClassManager.getFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
	}
	
	@Path("upload")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadFixtureClass(List<FixtureClass> fixtureClassList)
	{
		Integer status = 200;
		Response resp = new Response();
		
		try
		{
			for(FixtureClass fxClass : fixtureClassList)
			{
				FixtureClass fxClassExisting = fixtureClassManager.getFixtureClassByName(fxClass.getName());
				
				if(fxClassExisting != null)
				{
					m_Logger.error("Upload fixture type error: Fixture class with the same name already exists: " + fxClass.getName());
					eventManager.addEvent("Upload fixture type error: Fixture class with the same name already exists: " + fxClass.getName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}
				
				Bulb bulb = bulbManager.getBulbByName(fxClass.getBulb().getBulbName());
				
				if(bulb == null)
				{
					m_Logger.error("Upload fixture type error: Bulb associated with the fixture type does not exist, name = " + fxClass.getBulb().getBulbName());
					eventManager.addEvent("Upload fixture type error: Bulb associated with the fixture type does not exist, name = " + fxClass.getBulb().getBulbName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}
	
				Ballast ballast = ballastManager.getBallastByDisplayLabel(fxClass.getBallast().getDisplayLabel());
				
				if(ballast == null)
				{
					m_Logger.error("Upload fixture type error: Ballast associated with the fixture type does not exist, name = " + fxClass.getBallast().getBallastName());
					eventManager.addEvent("Upload fixture type error: Ballast associated with the fixture type does not exist, name = " + fxClass.getBallast().getBallastName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}
				
				fixtureClassManager.addFixtureClass(fxClass.getName(), fxClass.getNoOfBallasts().toString(), fxClass.getVoltage().toString(),
						ballast.getId().toString(), bulb.getId().toString());
			}
		resp.setStatus(status);		//Successful uploading of fixture types from EmConfig ,  keep this statement at last
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}
		return resp;
	}
	
	/**
     * Bulk Assign Fixture Type to the list of selected fixtures
     * 
     * @param fixtureList
     *            fixture "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
     * @param currentfixturetypeid
     *         	  id of the current selected fixture type
     * @return Response totalRecordUpdated
     */
    @Path("assignfixturetype/{currentfixturetypeid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response bulkFixtureTypeAssignToFixture(@PathParam("currentfixturetypeid") Long currentFixturetypeId,List<Fixture> fixtureList) {
        Response oStatus = new Response();
        Long totalRecordUpdated = null;
        String fixtureIdsList="";
        List<Long> fixtureIds  = new ArrayList<Long>();
		for (Iterator<Fixture> iterator = fixtureList.iterator(); iterator.hasNext();) {
			Long id = new Long(iterator.next().getId() + "");
			fixtureIdsList+=id;
			 if (iterator.hasNext()) {   
				  fixtureIdsList += ",";    
			  } 
			fixtureIds.add(id);
		}
        if (fixtureIds != null && !fixtureIds.isEmpty()) {
            m_Logger.debug("Assinging fixture type id: '" + currentFixturetypeId + "' to (" + fixtureIdsList + ") fixtures");
            totalRecordUpdated = fixtureClassManager.bulkFixtureTypessignToFixture(fixtureIdsList, currentFixturetypeId);
        }
        userAuditLoggerUtil.log("Assign fixture type id " + currentFixturetypeId + " to fixtures " + fixtureIdsList, UserAuditActionType.Assign_Fixture_Type.getName());
        oStatus.setStatus(totalRecordUpdated.intValue());
        return oStatus;
    }
    
    
    /**
     * Bulk Assign Fixture Type to the list of selected Locator Devices
     * 
     * @param locatorDeviceList
     *            fixture "<locatorDevices><locatorDevice><id>1</id></locatorDevice><locatorDevice><id>2</id></locatorDevice></locatorDevices>"
     * @param currentfixturetypeid
     *         	  id of the current selected fixture type
     * @return Response totalRecordUpdated
     */
    @Path("assignfixturetypetolocatordevice/{currentfixturetypeid}")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response bulkFixtureTypeAssignToLocatorDevices(@PathParam("currentfixturetypeid") Long currentFixturetypeId,List<LocatorDevice> locatorDeviceList) {
        Response oStatus = new Response();
        Long totalRecordUpdated = null;
        String locatorDeviceIdsList="";
        List<Long> locatorDeviceIds  = new ArrayList<Long>();
		for (Iterator<LocatorDevice> iterator = locatorDeviceList.iterator(); iterator.hasNext();) {
			Long id = new Long(iterator.next().getId() + "");
			locatorDeviceIdsList+=id;
			 if (iterator.hasNext()) {   
				 locatorDeviceIdsList += ",";    
			  } 
			 locatorDeviceIds.add(id);
		}
        if (locatorDeviceIds != null && !locatorDeviceIds.isEmpty()) {
            m_Logger.debug("Assinging fixture type id: '" + currentFixturetypeId + "' to (" + locatorDeviceIdsList + ") Other Devices");
            totalRecordUpdated = fixtureClassManager.bulkFixtureTypeAssignToLocatorDevices(locatorDeviceIdsList, currentFixturetypeId);
        }
        userAuditLoggerUtil.log("Assign fixture type id " + currentFixturetypeId + " to Other Devices " + locatorDeviceIdsList, UserAuditActionType.Assign_Fixture_Type.getName());
        oStatus.setStatus(totalRecordUpdated.intValue());
        return oStatus;
    }
    
    
}

	