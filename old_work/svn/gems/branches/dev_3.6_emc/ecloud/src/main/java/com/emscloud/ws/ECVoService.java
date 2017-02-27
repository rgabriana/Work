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
import com.emscloud.vo.DashboardRecord;

/**
 * @author Shilpa
 * 
 */
@Controller
@Path("/org/ec")
public class ECVoService {

	@Resource
	ECManager ecManager;

	public ECVoService() {

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
	@Path("energydata/{property}/{pid}/{fdate}/{tdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DashboardRecord> getDashboardMeterDataInPeriod(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("fdate") String fdate, @PathParam("tdate") String tdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date oFDate = null;
		Date oTDate = null;
		try {
			oFDate = inputFormat.parse(fdate);
		} catch (ParseException pe) {
			oFDate = new Date(System.currentTimeMillis());
		}
		try {
			oTDate = inputFormat.parse(tdate);
		} catch (ParseException pe) {
			oFDate = new Date(System.currentTimeMillis());
		}

		return ecManager.loadEnergyDataForPeriod(pid, property, oFDate, oTDate);
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
	@Path("currentenergydata/{property}/{pid}/{fdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DashboardRecord> getDashboardMeterDataInFixedPeriod(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("fdate") String fdate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar oCalendar = Calendar.getInstance();
		Date oFDate = null;
		try {
			oFDate = inputFormat.parse(fdate);
		} catch (ParseException pe) {
			oFDate = new Date(System.currentTimeMillis());
		} catch (NullPointerException npe) {
			oFDate = new Date(System.currentTimeMillis());
		}
		oCalendar.setTime(oFDate);

		return ecManager.loadCurrentEnergyData(pid, property, oFDate);
	}
}