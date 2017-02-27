package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.ems.model.BallastList;
import com.ems.model.BallastVoltPower;
import com.ems.model.EventsAndFault;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BallastVoltPowerManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.vo.model.BallastVoltPowerList;
import com.ems.ws.util.Response;





@Controller
@Path("/org/ballastservice")
public class BallastService {

	@Resource
	private UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "ballastManager")
    private BallastManager ballastManager;

	@Resource(name = "fixtureClassManager")
	private FixtureClassManager fixtureClassManager;

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;

	@Resource(name = "ballastVoltPowerManager")
	private BallastVoltPowerManager ballastVoltPowerManager;

	@Resource
	private EventsAndFaultManager eventManager;

	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;

	@Autowired
	private MessageSource messageSource;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("add")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addBallast(Ballast ballast) {

		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("ballastInputVoltage", ballast.getInputVoltage());
		nameVal.put("ballastFactor", ballast.getBallastFactor());
		nameVal.put("ballastWattage", ballast.getWattage());
		nameVal.put("ballastBaselineLoad", ballast.getBaselineLoad());
		nameVal.put("ballastItemNumber",ballast.getItemNum());
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return resp;
		}

		ballast = ballastManager.addBallast(ballast);

		if(ballast != null && ballast.getId() != null && ballast.getId() > 0)
		{
			resp.setMsg(String.valueOf(ballast.getId()));
			userAuditLoggerUtil.log(""+ballast.toString(), UserAuditActionType.Ballast_Add.getName());
		}
		else
		{
			resp.setMsg(String.valueOf(-1));
			userAuditLoggerUtil.log("::Ballast Add Failed::", UserAuditActionType.Ballast_Add.getName());
		}
		return resp;
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editBallast(Ballast ballast) {

		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("ballastInputVoltage", ballast.getInputVoltage());
		nameVal.put("ballastFactor", ballast.getBallastFactor());
		nameVal.put("ballastWattage", ballast.getWattage());
		nameVal.put("ballastBaselineLoad", ballast.getBaselineLoad());
		nameVal.put("ballastItemNumber",ballast.getItemNum());
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return resp;
		}

	    Ballast dbBallast = ballastManager.getBallastById(ballast.getId());
	    String changeLog = dbBallast.compare(ballast);
	    dbBallast.setItemNum(ballast.getItemNum());
	    dbBallast.setBallastName(ballast.getBallastName());
	    dbBallast.setInputVoltage(ballast.getInputVoltage());
	    dbBallast.setLampType(ballast.getLampType());
	    dbBallast.setLampNum(ballast.getLampNum());
	    dbBallast.setBallastFactor(ballast.getBallastFactor());
	    dbBallast.setWattage(ballast.getWattage());
	    dbBallast.setBallastManufacturer(ballast.getBallastManufacturer());
	    dbBallast.setBaselineLoad(ballast.getBaselineLoad());
	    dbBallast.setDisplayLabel(ballast.getDisplayLabel());

	    List<BigInteger> fixturesIdList = fixtureManager.loadFixturesIdListByBallastId(ballast.getId());
	    synchronized (this) {
            ballastManager.editBallast(dbBallast,fixturesIdList);
            //Invalidate the Fixture Cache for the list of fixtures
            if (fixturesIdList != null) {
            	for (BigInteger fixtureId: fixturesIdList)
                {
            		fixtureManager.resetFixtureBaseline(fixtureId.longValue());
            		FixtureCache.getInstance().invalidateDeviceCache(fixtureId.longValue());
                }
            }
        }
	    userAuditLoggerUtil.log(""+changeLog, UserAuditActionType.Ballast_Edit.getName());
	    return resp;
	}


	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
		BallastList oBallastList = new BallastList();
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("typeNumber", page);
		nameVal.put("typeBoolean", bSearch);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return oBallastList;
		}

		oBallastList	=	ballastManager
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

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
		BallastList oBallastList = null;
		Response resp = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("typeNumber", page);
		nameVal.put("typeBoolean", bSearch);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return oBallastList;
		}

		oBallastList = ballastManager.loadBallastListByUsage(
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

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
			userAuditLoggerUtil.log(""+ballastManager.getBallastById(id).toString(),UserAuditActionType.Ballast_Delete.getName());
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("getfixtureCount/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getFixtureCountByBallastId (@PathParam("id") Long id) {

		Response response = new Response();


		Integer fixtureCount = fixtureManager.getFixtureCountByBallastId(id);

		if(fixtureCount.intValue() > 0){
			response.setStatus(1);
		}else{
			response.setStatus(0);
		}

		return response;
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Ballast> getAllBallastList() {
		return ballastManager.getAllBallasts();
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("list/voltpowermap/{ballastid}/{voltage}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BallastVoltPowerList loadFixtureListWithSpecificAttrs(@PathParam("ballastid") Long ballastid,
			@PathParam("voltage") Double voltage) {

		Response response = new Response();
		Map<String,Object> nameVal = new HashMap<String,Object>();
		nameVal.put("id", ballastid);
		nameVal.put("ballastInputVoltage",voltage);
		response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
		if(response!= null && response.getStatus()!=200){
			m_Logger.error("Validation error"+response.getMsg());
			return new BallastVoltPowerList();
		}
		BallastVoltPowerList oBallastVoltPowerList= new BallastVoltPowerList() ;
		List<BallastVoltPower> mBallastVoltPower = ballastVoltPowerManager.getBallastVoltPowerByBallastIdInputVoltage(ballastid, voltage);
		oBallastVoltPowerList.setBallastVoltPower(mBallastVoltPower);
		oBallastVoltPowerList.setRecords(mBallastVoltPower.size());
		oBallastVoltPowerList.setTotal(1L);
		oBallastVoltPowerList.setPage(1);
		oBallastVoltPowerList.setRows(21L);
		return oBallastVoltPowerList;
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("upload")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadFixtureClass(List<Ballast> ballastList)
	{
		Response resp = new Response();
		Integer status = 200;
		try
		{
			for(Ballast ballast : ballastList)
			{
				Ballast ballastExisting = ballastManager.getBallastByDisplayLabel(ballast.getDisplayLabel());

				if(ballastExisting != null)
				{
					m_Logger.error("Ballast upload failed: Ballast with the same display lable already exists: " + ballast.getDisplayLabel());
					// Override default severity for Fixture Configuration Upload failure.
					eventManager.addAlarm("Ballast upload failed: Ballast with the same display lable already exists: " + ballast.getDisplayLabel(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}

				ballastManager.addBallast(ballast);
			}
			resp.setStatus(status);		//Successful uploading of ballasts from EmConfig ,  keep this statement at last
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}

		return resp;
	}

	@Path("/v1/upload")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadFixtureClassV1(List<Ballast> ballastList)
	{
		Response resp = new Response();
		Integer status = 200;
		try
		{
			for(Ballast ballast : ballastList)
			{
				Ballast ballastExisting = ballastManager.getBallastByDisplayLabel(ballast.getDisplayLabel());
				if(ballastExisting != null)
				{
					List<BigInteger> fixturesList = fixtureManager.loadFixturesIdListByBallastIdIfNoneCommissioned(ballastExisting.getId());
					if(fixturesList == null){
						m_Logger.debug("Ignoring this record as there are commissioned fixtures associated to same ballast.: " +ballast.getDisplayLabel());
						resp.setMsg("Updated ballast records. Few records were not updated as there are some commissioned fixtured associated to it.");
					}else{
						ballast.setId(ballastExisting.getId());
						ballastManager.mergeBallast(ballast,fixturesList);
						resp.setMsg("Updated ballast records");
					}
				}else{
					ballastManager.addBallast(ballast);
					resp.setMsg("Added ballast records");
				}
			}
			resp.setStatus(status);		//Successful uploading of ballasts from EmConfig ,  keep this statement at last
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}

		return resp;
	}
}


