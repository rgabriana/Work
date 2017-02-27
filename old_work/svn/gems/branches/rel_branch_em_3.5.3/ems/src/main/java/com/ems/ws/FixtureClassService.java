package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
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
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Autowired
	private MessageSource messageSource;

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("add/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("fixtureTypeNoOfBallast",noOfBallasts);
		nameVal.put("fixtureTypeVoltage",voltage);
		nameVal.put("fixtureTypeBallastId",ballastId);
		nameVal.put("fixtureTypeBulbId",bulbId);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!=null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return resp;
		}
		FixtureClass fixtureClass = fixtureClassManager.addFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
		userAuditLoggerUtil.log(""+fixtureClass.toString(), UserAuditActionType.FixtureType_Add.getName());
		resp.setMsg(fixtureClass.getId().toString());
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("edit/id/{id}/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editFixtureClass(@PathParam("id") String id,@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("fixtureTypeNoOfBallast",noOfBallasts);
		nameVal.put("fixtureTypeVoltage",voltage);
		nameVal.put("fixtureTypeBallastId",ballastId);
		nameVal.put("fixtureTypeBulbId",bulbId);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!=null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return resp;
		}
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
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
	
	

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("loadAllFixtureClasses")
	@GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FixtureClass> loadAllFixtureClasses () throws ParseException {
		
		return fixtureClassManager.loadAllFixtureClasses();
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("details/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureClass getFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();		
		nameVal.put("fixtureTypeNoOfBallast",noOfBallasts);
		nameVal.put("fixtureTypeVoltage",voltage);
		nameVal.put("fixtureTypeBallastId",ballastId);
		nameVal.put("fixtureTypeBulbId",bulbId);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!=null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return new FixtureClass();
		}
		return fixtureClassManager.getFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	
	@Path("v1/upload")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadFixtureClassV1(List<FixtureClass> fixtureClassList)
	{
		Integer status = 200;
		Response resp = new Response();
		
		try
		{
			for(FixtureClass fxClass : fixtureClassList)
			{
				FixtureClass fxClassExisting = fixtureClassManager.getFixtureClassByName(fxClass.getName());
				
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
				
				if(fxClassExisting != null)
				{
					int count = fixtureManager.loadCommissionedFixturesCountByFixtureClassId(fxClassExisting.getId());
					if(count > 0){
						m_Logger.debug("Ignoring this record as there are commissioned fixtures associated to same fixture class " + fxClass.getName());			
						resp.setMsg("Few fixture types were not updated as there are some commissioned fixtures associated to it.");
					}else{
						fixtureClassManager.editFixtureClass(fxClassExisting.getId().toString(), fxClass.getName(), fxClass.getNoOfBallasts().toString(), 
								fxClass.getVoltage().toString(), ballast.getId().toString(), bulb.getId().toString());			
						resp.setMsg("Updated fixture type records");
					}
				}else{
					fixtureClassManager.addFixtureClass(fxClass.getName(), fxClass.getNoOfBallasts().toString(), fxClass.getVoltage().toString(),
							ballast.getId().toString(), bulb.getId().toString());
					resp.setMsg("Added fixture type records");
				}
				
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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

	
