package com.ems.ws;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ems.model.BallastVoltPower;
import com.ems.service.BallastManager;
import com.ems.service.BallastVoltPowerManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.vo.model.BallastVoltPowerList;
import com.ems.ws.util.Response;





@Controller
@Path("/org/ballastservice")
public class BallastService {
	
	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	
	@Resource(name = "fixtureClassManager")
	private FixtureClassManager fixtureClassManager;
	
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	
	@Resource(name = "ballastVoltPowerManager")
	private BallastVoltPowerManager ballastVoltPowerManager;
	
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
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) throws ParseException {
		
		if (bSearch == null) {
			bSearch = false;
		}

		BallastList oBallastList = ballastManager
				.loadBallastList(orderby, orderway, bSearch, searchField,
						searchString, searchOper, (page - 1)
								* BallastList.DEFAULT_ROWS,
						BallastList.DEFAULT_ROWS);
		oBallastList.setPage(page);

		List<Long> mBallastVoltList = ballastVoltPowerManager
				.getAllDistinctBallastVoltPowers();
		List<Ballast> mAllBallast = oBallastList.getBallasts();

		Map<Long, Long> mBallastVoltMap = new HashMap<Long, Long>();

		for (Long ballastVoltObj : mBallastVoltList) {
			mBallastVoltMap.put(ballastVoltObj, ballastVoltObj);
		}

		List<Ballast> modifiedBallastList = new ArrayList<Ballast>();
		if (mAllBallast != null && !mAllBallast.isEmpty()) {
			for (Ballast mBallast : mAllBallast) {
				if (mBallastVoltMap.containsKey(mBallast.getId())) {
					mBallast.setIsPowerMap(true);
				} else {
					mBallast.setIsPowerMap(false);
				}
				modifiedBallastList.add(mBallast);
			}
			oBallastList.setBallasts(modifiedBallastList);
		}
		
		if (oBallastList.getBallasts() == null
				|| oBallastList.getBallasts().isEmpty()) {
			oBallastList.setBallasts(new ArrayList<Ballast>());
		}

		return oBallastList;
	}
	
	@Path("loadBallastListByUsage")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public BallastList loadBallastListByUsage (@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,
			@FormParam("_search") Boolean bSearch,
			@FormParam("searchField") String searchField,
			@FormParam("searchString") String searchString,
			@FormParam("searchOper") String searchOper) throws ParseException {
		
		if (bSearch == null) {
			bSearch = false;
		}
		
		BallastList oBallastList = ballastManager.loadBallastListByUsage(
				orderby, orderway, bSearch, searchField, searchString,
				searchOper, (page - 1) * BallastList.DEFAULT_ROWS,
				BallastList.DEFAULT_ROWS);

		List<Long> mBallastVoltList = ballastVoltPowerManager
				.getAllDistinctBallastVoltPowers();
		if (mBallastVoltList != null && !mBallastVoltList.isEmpty() && oBallastList.getBallasts() != null && !oBallastList.getBallasts().isEmpty()) {
			Map<Long, Long> mBallastVoltMap = new HashMap<Long, Long>();

			for (Long ballastVoltObj : mBallastVoltList) {
				mBallastVoltMap.put(ballastVoltObj, ballastVoltObj);
			}

			List<Ballast> mAllBallast = oBallastList.getBallasts();

			List<Ballast> modifiedBallastList = new ArrayList<Ballast>();
			if (mAllBallast != null && !mAllBallast.isEmpty()) {
			for (Ballast mBallast : mAllBallast) {
				if (mBallastVoltMap.containsKey(mBallast.getId())) {
					mBallast.setIsPowerMap(true);
				} else {
					mBallast.setIsPowerMap(false);
				}
				modifiedBallastList.add(mBallast);
			}
			oBallastList.setBallasts(modifiedBallastList);
			}
		}
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
		Integer fixtureClassCount = 1;
		
		fixtureCount = fixtureManager.getFixtureCountByBallastId(id);
		fixtureClassCount = fixtureClassManager.getFixtureClassCountByBallastId(id);
		Integer isDefaultBallast = ballastManager.getBallastById(id).getIsDefault();
		if (isDefaultBallast.equals(new Integer(1))) {
			response.setStatus(3);
			return response;
		}
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
			ballastManager.deleteBallastById(id);
			response.setStatus(0);
			return response;
		}
		
	}
	
	/**
     * Return Ballast
     * 
     * @return Ballast for the selected Display Label
     */
	@Path("details")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Ballast getBallastByDisplayLabel(Ballast ballast) {
		
		String bulbStr = "bulbs";
		if(ballast.getLampNum() == 1) {
			bulbStr = "bulb";
		}
		
		String displayLabel = ballast.getDisplayLabel().trim();
		
		if(displayLabel == null || "".equals(displayLabel)){
			displayLabel = ballast.getBallastName() + "(" + ballast.getBallastManufacturer() + "," + ballast.getLampType() + "," + ballast.getWattage() + "W," + ballast.getLampNum() + " " + bulbStr + ")";
		}
		
		return ballastManager.getBallastByDisplayLabel(displayLabel);
	}
	
	@Path("list/voltpowermap/{ballastid}/{voltage}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BallastVoltPowerList loadFixtureListWithSpecificAttrs(@PathParam("ballastid") Long ballastid,
			@PathParam("voltage") Double voltage) {		
		BallastVoltPowerList oBallastVoltPowerList= new BallastVoltPowerList() ;
		List<BallastVoltPower> mBallastVoltPower = ballastVoltPowerManager.getBallastVoltPowerByBallastIdInputVoltage(ballastid, voltage);
		oBallastVoltPowerList.setBallastVoltPower(mBallastVoltPower);
		oBallastVoltPowerList.setRecords(mBallastVoltPower.size());	
		oBallastVoltPowerList.setTotal(1L);
		oBallastVoltPowerList.setPage(1);		
		return oBallastVoltPowerList;
	}
	
}

	