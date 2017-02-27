/**
 * 
 */
package com.emscloud.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.emscloud.service.ECManager;
import com.emscloud.util.DateUtil;
import com.emscloud.vo.DashboardRecord;
import com.emscloud.vo.DashboardRecordV2;

/**
 * @author Shilpa
 * 
 */
@Controller
@Path("/org/ec/v2")
public class ECVoServiceV2 {

	@Resource
	ECManager ecManager;

	public ECVoServiceV2() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Returns the energy data savings dashboard record for flexible period
	 * 
	 * @param pid
	 *            property unique identifier
	 * @param property
	 *            (company|campus|building|floor)
	 * @param fdate
	 *            from(oldest) date
	 * @param tdate
	 *            to(latest) date
	 * @return Dashboard records
	 */
	@Path("energydata/{timezone}/{property}/{pid}/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DashboardRecordV2> getDashboardMeterDataInPeriod(
			@PathParam ("timezone") String timezone,
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		String inputPattern = "yyyyMMddHHmmss";
		String outputPattern = "yyyy-MM-dd HH:mm:ss";
		Date oFDate = DateUtil.createDateWithTimeZone(inputPattern, fdate, timezone.replaceAll("__", "/"));
		Date oTDate = DateUtil.createDateWithTimeZone(inputPattern, tdate, timezone.replaceAll("__", "/"));
		List<DashboardRecordV2> output =  ecManager.loadEnergyDataForPeriod(pid, property, oFDate, oTDate, timezone.replaceAll("__", "/"), outputPattern);
		return output;
	}

	/**
	 * Returns latest energy data savings for a date.
	 * 
	 * @param pid
	 *            property unique identifier
	 * @param property
	 *            (company|campus|building|floor)
	 * @param fdate
	 *            latest data for the date
	 * @return Dashboard records
	 */
	@Path("currentenergydata/{timezone}/{property}/{pid}/{fdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DashboardRecordV2> getDashboardMeterDataInFixedPeriod(@PathParam("timezone") String timezone,
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("fdate") String fdate) {
		String inputPattern = "yyyyMMddHHmmss";
		String outputPattern = "yyyy-MM-dd HH:mm:ss";
		Date oFDate = DateUtil.createDateWithTimeZone(inputPattern, fdate, timezone.replaceAll("__", "/"));
		return ecManager.loadCurrentEnergyData(pid, property, oFDate, timezone.replaceAll("__", "/"), outputPattern);
	}
}