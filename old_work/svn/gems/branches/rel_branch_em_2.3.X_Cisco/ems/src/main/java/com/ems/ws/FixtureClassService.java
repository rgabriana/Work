package com.ems.ws;

import java.text.ParseException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.FixtureClass;
import com.ems.model.FixtureClassList;
import com.ems.service.FixtureClassManager;
import com.ems.ws.util.Response;





@Controller
@Path("/org/fixtureclassservice")
public class FixtureClassService {
	
	@Resource(name = "fixtureClassManager")
    private FixtureClassManager fixtureClassManager;
		
	@Path("add/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();	
		fixtureClassManager.addFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
		return resp;
	}
	
	@Path("edit/id/{id}/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editFixtureClass(@PathParam("id") String id,@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		Response resp = new Response();	
		fixtureClassManager.editFixtureClass(id,name, noOfBallasts, voltage,ballastId,bulbId);
		return resp;
	}
	
	@Path("deleteFixtureClass/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteFixtureClassById (@PathParam("id") Long id) {
		
		Response response = new Response();
		
		fixtureClassManager.deleteFixtureClassById(id);
		response.setStatus(0);
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
	
	@Path("details/name/{name}/noOfBallasts/{noOfBallasts}/voltage/{voltage}/ballastId/{ballastId}/bulbId/{bulbId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FixtureClass getFixtureClass(@PathParam("name") String name,
            @PathParam("noOfBallasts") String noOfBallasts, @PathParam("voltage") String voltage , @PathParam("ballastId") String ballastId,@PathParam("bulbId") String bulbId) {
		
		return fixtureClassManager.getFixtureClass(name, noOfBallasts, voltage,ballastId,bulbId);
	}
	
	
}

	