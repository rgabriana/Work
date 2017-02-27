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

import com.ems.model.Ballast;
import com.ems.model.BallastList;
import com.ems.service.BallastManager;
import com.ems.service.FixtureManager;
import com.ems.ws.util.Response;





@Controller
@Path("/org/ballastservice")
public class BallastService {
	
	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	
	@Path("add")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addBallast(Ballast ballast) {
		
		Response resp = new Response();	
		ballastManager.addBallast(ballast);
		return resp;
	}
	
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editBallast(Ballast ballast) {
		
		Response resp = new Response();	
		ballastManager.editBallast(ballast);
		return resp;
	}
		

	@Path("loadBallastList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public BallastList loadBallastList (@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
		
		BallastList oBallastList =  ballastManager.loadBallastList(orderway, (page - 1) * BallastList.DEFAULT_ROWS, BallastList.DEFAULT_ROWS);
		oBallastList.setPage(page);
		
		if(oBallastList.getBallasts() == null || oBallastList.getBallasts().isEmpty()){
			oBallastList.setBallasts(new ArrayList<Ballast>());
		}
		
		return oBallastList;
	}
	
	@Path("deleteballast/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteBallastById (@PathParam("id") Long id) {
		
		Response response = new Response();
		
		Integer fixtureCount = 1;
		
		fixtureCount = fixtureManager.getFixtureCountByBallastId(id);
		
		if(fixtureCount > 0){
			response.setStatus(1);
			return response;
		}else{
			ballastManager.deleteBallastById(id);
			response.setStatus(0);
			return response;
		}
		
	}
	
}

	