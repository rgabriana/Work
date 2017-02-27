package com.ems.ws;

import java.text.ParseException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.Bulb;
import com.ems.model.BulbList;
import com.ems.service.BulbManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.ws.util.Response;

@Controller
@Path("/org/bulbservice")
public class BulbService {

	@Resource(name = "fixtureClassManager")
	private FixtureClassManager fixtureClassManager;
	
	@Resource(name = "bulbManager")
	private BulbManager bulbManager;
	
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	
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
		bulbManager.addBulb(bulb);
		resp.setStatus(1);
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
		bulbManager.editBulb(bulb);
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

}
