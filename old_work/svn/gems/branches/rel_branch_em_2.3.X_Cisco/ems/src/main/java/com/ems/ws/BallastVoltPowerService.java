package com.ems.ws;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Controller;

import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BallastVoltPowerManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.types.UserAuditActionType;

@Controller
@Path("/org/ballastvoltpowerservice")
public class BallastVoltPowerService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource(name = "ballastVoltPowerManager")
	private BallastVoltPowerManager ballastVoltPowerManager;
	@Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
	@Resource(name = "ballastManager")
    private BallastManager ballastManager;
	@Resource
	private EventsAndFaultManager eventsAndFaultManager;
	@SuppressWarnings("unchecked")
	@Path("list/getexportdata")
	@POST
	@Produces("application/csv")
	public Response getBallastCSVListByVoltageAndId(
			@FormParam("voltagelevelballastcsv") Double voltage,
			@FormParam("ballastidballastcsv") Long ballastId)
			throws UnsupportedEncodingException, ParseException {
		
		StringBuffer output = new StringBuffer("");		
		List<BallastVoltPower> mBallastVoltPower = ballastVoltPowerManager.getBallastVoltPowerByBallastIdInputVoltage(ballastId, voltage);
		NumberFormat nf = NumberFormat.getInstance();
       	nf.setMaximumFractionDigits(2);
		Ballast ballast = ballastManager.getBallastById(ballastId);
		output.append("#ballast type,"+ ballast.getBallastName() +"\r\n");
		output.append("#no. lamps,"+ ballast.getLampNum() +"\r\n");
		output.append("#lamp manufacturer,"+ ballast.getBallastManufacturer()+"\r\n");
		output.append("#lamp type,"+ ballast.getLampType() +"\r\n");
		output.append("#lamp wattage,"+ ballast.getWattage() +"\r\n");
		output.append("#line voltage,"+ voltage +"\r\n");
		output.append("light level"+","+"Power"+","+"include\r\n");
		for (Iterator iterator = mBallastVoltPower.iterator(); iterator
				.hasNext();) {
			BallastVoltPower ballastVoltPower = (BallastVoltPower) iterator
					.next();
			int enabled = 0;
			if(ballastVoltPower.getEnabled().equals(true))
			{
				enabled =1;
			}
			output.append(ballastVoltPower.getVolt()*10+","+ nf.format(ballastVoltPower.getPower())+","+enabled);
			
			output.append("\r\n");			
		}		
		
		return Response
		.ok(output.toString(), "text/csv")
		.header("Content-Disposition",
				"attachment;filename=Ballast_Volt_Power_Map_"+"BallastId_"+ballastId+"_Voltage_"+voltage+".csv")
		.build();
	}
	
	@Path("updateEnabledFlag/{ballastId}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public com.ems.ws.util.Response updateBallastVoltPowerMap(List<BallastVoltPower> ballastVoltPowerMap,@PathParam("ballastId") Long ballastId) {
		com.ems.ws.util.Response resp = new com.ems.ws.util.Response();
		if(ballastVoltPowerMap != null && ballastVoltPowerMap.size() > 0) {
			ballastVoltPowerManager.updateBallastVoltPowerMap(ballastVoltPowerMap);
			// Clean up ballast cache
			fixtureManager.invalidateBallastVPCurve(ballastId);
			
			userAuditLoggerUtil.log("Enable/Disable Volt Participation in Lamp Outage for Ballast " +ballastId, UserAuditActionType.Enable__Disable_Volt_Participation_In_LORP.getName());
		}
		resp.setMsg("S");
		return resp;
		
	}
	
	@Path("forgetBallastCurve/{ballastId}/inputVoltage/{inputVoltage}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public com.ems.ws.util.Response forgetBallastCurve(@PathParam("ballastId") Long ballastId,@PathParam("inputVoltage") Double inputVoltage) {
		com.ems.ws.util.Response resp = new com.ems.ws.util.Response();
		Ballast ballast = ballastManager.getBallastById(ballastId);
		// Check fixture(s) associated with this ballast and not "using" fixture curve
		// Clear outages for these fixture(s) if any.
		int[] fXListWithBallastCurve = fixtureManager.getFixtureIdsListUsingBallastCurve(ballastId,inputVoltage);
		int cnt = 0;
		if(fXListWithBallastCurve!=null && fXListWithBallastCurve.length >0)
		{
			for(int fixtId: fXListWithBallastCurve) {
				Fixture fixture = fixtureManager.getFixtureById(fixtId);
				eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR);
				eventsAndFaultManager.clearAlarm(fixture, EventsAndFault.FIXTURE_OUTAGE_EVENT_STR);
			}
		}
		ballastVoltPowerManager.deleteBallastCurve(ballastId,inputVoltage);
		// Clean up ballast cache
		fixtureManager.invalidateBallastVPCurve(ballastId);
		userAuditLoggerUtil.log("Deleted Ballast Curve for ballast " + ballast.getBallastName(), UserAuditActionType.Forget_Ballast.getName());
		resp.setMsg("S");
		return resp;
	}
}
