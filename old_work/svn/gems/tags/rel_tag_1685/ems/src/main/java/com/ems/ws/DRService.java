/**
 * 
 */
package com.ems.ws;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.model.DRRecord;
import com.ems.model.DRTarget;
import com.ems.model.GroupECRecord;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.device.DRThread;
import com.ems.service.DRTargetManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.PricingManager;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/dr")
public class DRService {
	private static Logger m_Logger = Logger.getLogger("DemandResponse");
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;
	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "pricingManager")
	private PricingManager pricingManager;
	@Resource(name = "drTargetManager")
	private DRTargetManager drTargetManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;

	public DRService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Fetches the power used per Group with the range of time
	 * 
	 * @param fdate
	 *            older date (format: yyyyMMddHHmmss)
	 * @param tdate
	 *            new date (format: yyyyMMddHHmmss)
	 * @return GroupECRecord list
	 */
	@Path("group/ec/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GroupECRecord> loadGroupEnergyConsumptionBetweenPeriods(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		try {
			oFDate = inputFormat.parse(fdate);
			oTDate = inputFormat.parse(tdate);
			// FIXME: based on respective period.
			return energyConsumptionManager.loadGroupEnergyConsumptionBetweenPeriods(oFDate, oTDate);
		} catch (ParseException pe) {
			m_Logger.warn(pe.getMessage());
		}
		return null;
	}

	/**
	 * Returns the DR reactivity value for each group in the system
	 * 
	 * @return GroupECRecord list
	 */
	@Path("group/sensitivity")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<GroupECRecord> getGroupDRSensitivity() {
		return groupManager.getDRSensitivityRecords();
	}

	/**
	 * Fetches the current pricing in the day
	 * 
	 * @return Response object with msg containing the pricing information
	 */
	@Path("pricing/current")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCurrentPricing() {
		Response response = new Response();
		// Msg used to send the pricing for the current period.
		response.setMsg(String.valueOf(pricingManager.getPrice(new Date())));
		return response;
	}

	/**
	 * Dims the fixtures for specified time in the respective group, with the
	 * computed percentage based on the weight (derived from formula based on dr
	 * sensitivity); TODO: Optimize the command send
	 * 
	 * @param groupId
	 *            Group id
	 * @param percentage
	 *            (-100 | 0 | 100)
	 * @param time
	 *            specified time in minutes
	 * @return response status
	 */
	@Path("op/dim/group/{groupid}/{percentage}/{time}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String dimFixture(String data, @PathParam("groupid") Integer groupId,
			@PathParam("percentage") String percentage,
			@PathParam("time") String time) {
		groupManager.dimFixtures(groupId, Integer.valueOf(percentage),
				Integer.valueOf(time));
		//TODO uncomment following line once audit logger code is fixed. 
		//userAuditLoggerUtil.log("Dimmed Fixture, Group Id : " + groupId + " %age: " + percentage);
		return "S";
	}

	/**
	 * Updates the drTarget
	 * 
	 * @param drTarget
	 * @return Response Status
	 */
	@Path("update")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateDR(DRTarget dr) {
		Response oStatus = new Response();
		DRTarget dr1 = drTargetManager.getDRTargetById(dr.getId());
		dr1.setDuration(dr.getDuration());
		dr1.setPriceLevel(dr.getPriceLevel());
		dr1.setTargetReduction(dr.getTargetReduction());
		dr1.setPricing(dr.getPricing());
		String currentEnabled = dr1.getEnabled();
		if ("Yes".equals(currentEnabled)) {
			oStatus.setMsg("R");
		} else if (dr.getEnabled().equals("No")) {
			drTargetManager.updateAttributes(dr1);
			oStatus.setMsg("S");
		} else if (dr.getEnabled().equals("Yes")) {
			drTargetManager.updateAttributes(dr1);
			try {
				DRThread drThread = DRThread.getInstance();
				if (drThread.getDrTarget() != null) {
					oStatus.setMsg("E");
				} else {
					drThread.setDrTarget(dr1);
					drThread.setDrTargetManager(drTargetManager);
					drThread.start();
					oStatus.setMsg("S");
				}
			} catch (Exception e) {
				e.printStackTrace();
				oStatus.setMsg("U");
			}
		}
		oStatus.setStatus(1);
		//userAuditLoggerUtil.log("Update DR: " + dr1.getId());
		return oStatus;
	}

	/**
	 * initiates dr event
	 * 
	 * @param drTarget
	 * @return response status
	 */
	@Path("initiate")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response initiateDR(DRTarget dr) {
		Response oStatus = new Response();
		try {
			DRThread drThread = DRThread.getInstance();
			if (drThread.getDrTarget() != null) {
				oStatus.setMsg("E");
			} else {
				drTargetManager.updateAttributes(dr);
				drThread.setDrTarget(dr);
				drThread.setDrTargetManager(drTargetManager);
				drThread.start();
				oStatus.setMsg("S");
			}
		} catch (Exception e) {
			e.printStackTrace();
			oStatus.setMsg("U");
		}
		oStatus.setStatus(1);
		return oStatus;
	}

	/**
	 * cancels dr event
	 * 
	 * @return response status
	 */
	@Path("cancel")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response cancelDR() {
		Response oStatus = new Response();
		try {
			DRThread drThread = DRThread.getInstance();
			DRTarget dr = drThread.getDrTarget();
			if (dr != null) {
				drThread.cancelDR();
				oStatus.setMsg("S");
			} else {
				oStatus.setMsg("S");
			}
		} catch (Exception e) {
			e.printStackTrace();
			oStatus.setMsg("U");
		}
		oStatus.setStatus(1);
		//userAuditLoggerUtil.log("Cancel DR");
		return oStatus;
	}
	
	/**
	 * Fetches the avg power and recent power used by fixture along with the fixture details.
	 * 
	 * @param fdate
	 *            older date (format: yyyyMMddHHmmss)
	 * @param tdate
	 *            new date (format: yyyyMMddHHmmss)
	 * @return DRRecord list
	 */
	@Path("fixture/record/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DRRecord> getFixtureECProfileRecords(
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		List<DRRecord> out = new ArrayList<DRRecord>();
		try {
			oFDate = inputFormat.parse(fdate);
			oTDate = inputFormat.parse(tdate);
			Map<Integer, Object[]> map1 = fixtureManager.getRecentFixtureDetails();
			Map<Integer, Double> map2 = energyConsumptionManager.getFixtureECOverPeriod(oFDate, oTDate);
			Map<Integer, Double> map3 = energyConsumptionManager.getRecentFixtureEC();
			if (map1 != null && !map1.isEmpty()) {
				for(Integer i: map1.keySet()) {
					Object[] object = map1.get(i);
					DRRecord drrecord = new DRRecord(((BigInteger)object[0]).intValue(),
							(Integer)object[1], 
							object[2].toString(), 
							((Short)object[3]).intValue(),
							((BigInteger)object[4]).intValue(),
							((BigInteger)object[5]).intValue(),
							(map2.get(i) == null ? 0D : map2.get(i)),
							(map3.get(i) == null ? 0D : map3.get(i)));
					out.add(drrecord);
				}
			}
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		return out;
	}

}
