package com.emsdashboard.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.Globals;
import com.emsdashboard.service.DashboardDataManager;
import com.emsdashboard.ws.util.Communication;
import com.emsdashboard.ws.util.Response;
import com.emsdashboard.ws.util.WebServiceUtils;

@Controller
@Path("org/ec")
public class EnergyDataDetails {
	static final Logger logger = Logger.getLogger("EMS_DASHBOARD");
	@Resource(name = "dashboardDataManager")
	private DashboardDataManager dashboardDataManager;
	@Context
	ServletContext context;

	/**
	 * Return Total no of fixtures
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fixedPeriod
	 *            (CURRENT|DAY|WEEK|MONTH|YEAR)
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param ip
	 *            IP of gem server
	 * @return Count of total no of fixtures
	 */

	@Path("count/fcp/{property}/{pid}/{fixedperiod}/{fdate}/{ip}")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	public String getNumberOfFixtures(@PathParam("property") String property,
			@PathParam("pid") String pid,
			@PathParam("fixedperiod") String fixedperiod,
			@PathParam("fdate") String fdate, @PathParam("ip") String ip) {
		String url = null;
		url = "https://" + ip + "/ems/services/org/ec/count/fcp/" + property
				+ "/" + pid + "/" + fixedperiod + "/" + fdate;
		String out = "";
		Globals oCommStatus = new Globals();
		Communication cm = new Communication(url, oCommStatus);
		if (oCommStatus.state == 100) {
			cm.recvData();
			logger.debug("FCP Commucation Successful => URL: " + url + "\n"
					+ oCommStatus.buffer);
			out = oCommStatus.buffer;
		} else {
			logger.debug("***Commucation Over HTTP is unsuccessful***");
			return "<status>-1</status>";
		}
		return out;
	}

	/**
	 * Return Meter data with range value in KWH
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param tdate
	 *            ending date in specified period.
	 * @param ip
	 *            IP of gem server
	 */

	@Path("em/{property}/{pid}/{fdate}/{tdate}/{ip}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public String getMasterEnergyMeter(@PathParam("property") String property,
			@PathParam("pid") String pid, @PathParam("fdate") String fdate,
			@PathParam("tdate") String tdate, @PathParam("ip") String ip) {

		String out = "getMasterEnergyMeter";

		return out;
	}

	/**
	 * Return Meter data with range in percentage
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param tdate
	 *            ending date in specified period.
	 * @param ip
	 *            IP of gem server
	 */
	@Path("md/{property}/{pid}/{fdate}/{tdate}/{ip}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public String loadMeterDataWithDateRange(
			@PathParam("property") String property,
			@PathParam("pid") String pid, @PathParam("fdate") String fdate,
			@PathParam("tdate") String tdate, @PathParam("ip") String ip) {
		String url = null;
		url = "https://" + ip + "/ems/services/org/ec/md/" + property + "/"
				+ pid + "/" + fdate + "/" + tdate;
		String out = "";
		Globals oCommStatus = new Globals();
		Communication cm = new Communication(url, oCommStatus);
		if (oCommStatus.state == 100) {
			cm.recvData();
			logger.debug("MD Commucation Successful => URL: " + url + "\n"
					+ oCommStatus.buffer);
			out = oCommStatus.buffer;
		} else {
			logger.debug("***Commucation Over HTTP is unsuccessful***");
			return "<status>1</status>";
		}
		return out;
	}

	/**
	 * Return energy consumption
	 * 
	 * @param durationType
	 * @param ip
	 *            IP of gem server
	 */
	@Path("load/areareport/piechart/{duration}/{gemIP}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public String loadAreaReportPieChartData(
			@PathParam("duration") String durationType,
			@PathParam("gemIP") String gemIPAddress) {

		String out = "loadAreaReportPieChartData";

		return out;
	}

	/**
	 * Return energy consumption for mini gems
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fixedperiod
	 *            (CURRENT|DAY|WEEK|MONTH|YEAR)
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param ip
	 *            IP of gem server
	 */
	@Path("cp/{property}/{pid}/{fixedperiod}/{fdate:.*}/{ip}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public String loadEnergyConsumption(@PathParam("property") String property,
			@PathParam("pid") String pid,
			@PathParam("fixedperiod") String fixedperiod,
			@PathParam("fdate") String fdate, @PathParam("ip") String ip) {

		String url = null;
		url = "https://" + ip + "/ems/services/org/ec/cp/" + property + "/"
				+ pid + "/" + fixedperiod + "/" + fdate;
		String out = "";
		logger.debug("url " + url);
		Globals oCommStatus = new Globals();
		Communication cm = new Communication(url, oCommStatus);
		if (oCommStatus.state == 100) {
			cm.recvData();
			logger.debug("CP Commucation Successful => URL: " + url + "\n"
					+ oCommStatus.buffer);
			out = oCommStatus.buffer;
		} else {
			logger.debug("***Commucation Over HTTP is unsuccessful***");
			return "<status>1</status>";
		}
		return out;
	}

	/**
	 * Returns meter data savings for a fixed period for dashboard data.
	 * 
	 * @param pid
	 *            property unique identifier
	 * @param property
	 *            (Gems)
	 * @param fixedperiod
	 *            (CURRENT|DAY|WEEK|MONTH|YEAR)
	 * @param fdate
	 *            from date back to the fixedperiod
	 * @return Dashboard records
	 */
	@Path("mds/{property}/{pid}/{fixedperiod}/{fdate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<DashboardRecord> getDashboardMeterDataInFixedPeriod(
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("fixedperiod") String fixedperiod,
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
		if (property.equalsIgnoreCase("gems")) {
			property = "gems_id";
		}
		
		//converting dates in GMT because Master EM has data captured in GMT which send in this form from EM. 
		oFDate = WebServiceUtils.convertDateToGMT(oFDate) ;
		
		if (fixedperiod.equalsIgnoreCase("CURRENT")) {
			return dashboardDataManager
					.loadRecentSummary(pid, property, oFDate);
		} else if (fixedperiod.equalsIgnoreCase("DAY")) {
			return dashboardDataManager.loadDaySummary(pid, property, oFDate);
		} else if (fixedperiod.equalsIgnoreCase("WEEK")) {
			return dashboardDataManager.loadWeekSummary(pid, property, oFDate);
		} else if (fixedperiod.equalsIgnoreCase("MONTH")) {
			return dashboardDataManager.loadMonthSummary(pid, property, oFDate);
		} else if (fixedperiod.equalsIgnoreCase("YEAR")) {
			return dashboardDataManager.loadYearSummary(pid, property, oFDate);
		}
		return null;
	}

	/**
	 * Returns the meter data savings dashboard record for flexible period
	 * 
	 * @param pid
	 *            property unique identifier
	 * @param property
	 *            (Gems)
	 * @param fdate
	 *            latest date
	 * @param tdate
	 *            oldest date
	 * @return Dashboard records
	 */
	@Path("fp/mds/{property}/{pid}/{fdate}/{tdate}")
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

		if (property.equalsIgnoreCase("gems")) {
			property = "gems_id";
		}
		
		// Logic to decide whether to convert the date to gmt or not. We need GMT for days related queries only.
		Date from = oFDate ;
		Date to = oTDate ;
		// Dates are converted to gmt because Master table gets data in GMT from  EM. 
		oFDate = WebServiceUtils.convertDateToGMT(oFDate) ;
		oTDate = WebServiceUtils.convertDateToGMT(oTDate) ;
		 long diff = oFDate.getTime() - oTDate.getTime();
	        // Calculate difference in days
	        long diffDays = diff / (24 * 60 * 60 * 1000);
	        if(diffDays > 1)
	        {
	        	oFDate = from ;
	    		oTDate = to ;
	        }
	     
		return dashboardDataManager.loadSummaryInPeriod(pid, property, oFDate, oTDate);

	}

	/**
	 * Take in the data from other webservices and
	 * 
	 */
	@Path("load/{gemsip}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response collectGemsData(@PathParam("gemsip") String gemsIp,
			List<DashboardRecord> records) {
		Response oResponse = new Response();
		DashboardRecord oRecord = null;
		records =WebServiceUtils.convertDashboardRecordToGMT(records) ; 
		if (records.size() != 0) {
			Iterator it = records.iterator() ;
			while(it.hasNext())
			{
    			oRecord = (DashboardRecord) it.next();
    			try
    			{
        			if(dashboardDataManager.saveOneHourDashBoardDetails(oRecord, gemsIp))
        			{
        				oResponse.setStatus(0);
        			}
    			}catch(Exception e)
    			{
    			   //System.out.println(gemsIp + " " +oRecord.getCaptureOn() + " Record Duplicated " + e.getMessage());
    			   logger.debug(gemsIp + " " +oRecord.getCaptureOn() + " Record Duplicated " + e.getMessage() );
    			}
			}
		} else {
			oResponse.setStatus(1);
		}
		return oResponse;
	}

}
