package com.ems.ws;

import java.text.ParseException;
import java.util.ArrayList;
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

import com.ems.model.Ballast;
import com.ems.model.Bulb;
import com.ems.model.BulbList;
import com.ems.model.EventsAndFault;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BulbManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

@Controller
@Path("/org/bulbservice")
public class BulbService {

	@Resource
	private UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "fixtureClassManager")
	private FixtureClassManager fixtureClassManager;
	
	@Resource(name = "bulbManager")
	private BulbManager bulbManager;
	
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	
	@Resource
	private EventsAndFaultManager eventManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	@Path("details")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Bulb getBulbByDisplayLabel(Bulb bulb) {		
		//String uiBulbString = bulb.getManufacturer().trim() + bulb.getBulbName().trim() + bulb.getType().trim() + bulb.getInitialLumens() + bulb.getDesignLumens() + bulb.getEnergy() + bulb.getLifeInsStart() + bulb.getLifeProgStart() + bulb.getDiameter() + bulb.getLength() + bulb.getCri() + bulb.getColorTemp();
		//uiBulbString = uiBulbString.toUpperCase();
		Bulb mBulb = bulbManager.getBulbByName(bulb.getBulbName());
		if(mBulb != null)
		{
			return mBulb;
		}		
		return null; // return when does not exist
	}

	@Path("add")
	@POST
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response addBulb(Bulb bulb) {
		Response resp = new Response();
		bulb = bulbManager.addBulb(bulb);
		if(bulb!= null && bulb.getId() != null && bulb.getId() > 0)
		{	
			resp.setMsg(String.valueOf(bulb.getId()));	
			userAuditLoggerUtil.log(""+bulb.toString(), UserAuditActionType.Bulb_Add.getName());
		}
		else
		{
			resp.setMsg(String.valueOf(-1));
			userAuditLoggerUtil.log("Bulb Add was not successful", UserAuditActionType.Bulb_Add.getName());
		}
		return resp;
	}
	
	@Path("deletebulb/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteBulbById (@PathParam("id") Long id) {
		
		Response response = new Response();
		
		Integer fixtureCount = 1;
		Integer fixtureClassCount = 1;
		
		fixtureCount = fixtureManager.getFixtureCountByBulbId(id);
		fixtureClassCount = fixtureClassManager.getFixtureClassCountByBulbId(id);
		
		if(fixtureCount > 0){
			response.setStatus(1);
			return response;
		}
		if(fixtureClassCount > 0)
		{
			response.setStatus(2);
			return response;
		}		
		else{
			userAuditLoggerUtil.log(""+bulbManager.getBulbById(id).toString(), UserAuditActionType.Bulb_Edit.getName());
			bulbManager.deleteBulbById(id);
			response.setStatus(0);
			return response;
		}
		
	}
	
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editBulb(Bulb bulb) {		
		Response resp = new Response();
		Bulb dbBulb = bulbManager.getBulbById(bulb.getId());
		String changeLog = dbBulb.compare(bulb);
		dbBulb.setBulbName(bulb.getBulbName());
		dbBulb.setColorTemp(bulb.getColorTemp());
		dbBulb.setCri(bulb.getCri());
		dbBulb.setDesignLumens(bulb.getDesignLumens());
		dbBulb.setDiameter(bulb.getDiameter());
		dbBulb.setEnergy(bulb.getEnergy());
		dbBulb.setInitialLumens(bulb.getInitialLumens());
		dbBulb.setLength(bulb.getLength());
		dbBulb.setLifeInsStart(bulb.getLifeInsStart());
		dbBulb.setLifeProgStart(bulb.getLifeProgStart());
		dbBulb.setManufacturer(bulb.getManufacturer());
		dbBulb.setType(bulb.getType());
		bulbManager.editBulb(dbBulb);
		userAuditLoggerUtil.log("" +changeLog,UserAuditActionType.Bulb_Edit.getName());
		return resp;
	}

	@Path("loadBulbList")
	@POST
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BulbList loadBulbList(@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {

		BulbList oBulbList = bulbManager.loadBulbList(orderway, (page - 1)
				* BulbList.DEFAULT_ROWS, BulbList.DEFAULT_ROWS);
		oBulbList.setPage(page);

		if (oBulbList.getBulbs() == null || oBulbList.getBulbs().isEmpty()) {
			oBulbList.setBulbs(new ArrayList<Bulb>());
		}

		return oBulbList;
	}
	
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Bulb> getAllBulbList() {
		return bulbManager.getAllBulbs();
	}

	@Path("upload")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadFixtureClass(List<Bulb> bulbList)
	{
		Response resp = new Response();
		Integer status = 200;
		try
		{
			for(Bulb bulb : bulbList)
			{
				Bulb bulbExisting = bulbManager.getBulbByName(bulb.getBulbName());
				
				if(bulbExisting != null)
				{
					m_Logger.error("Bulb upload failed: Bulb with the same name already exists: " + bulb.getBulbName());
					eventManager.addEvent("Bulb upload failed: Bulb with the same name already exists: " + bulb.getBulbName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}

				bulbManager.addBulb(bulb);
			}
			resp.setStatus(status);		//Successful uploading of bulbs from EmConfig, keep this statement at last 
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}
		
		return resp;
	}
}
