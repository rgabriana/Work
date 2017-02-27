package com.emcloudinstance.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.utils.ArgumentUtils;
import com.emcloudinstance.service.EnergyConsumptionManager;
import com.emcloudinstance.vo.EcSyncVo;

@Component
@Path("/org/ec")
public class ECServices {

	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;

	static final Logger logger = Logger.getLogger(ECServices.class.getName());

	@Path("floor/15min/sync")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<EcSyncVo> get15MinFloorEC(String validLastDate,
			@Context HttpHeaders headers) {
		String mac = null;
		String emTimeZone = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		// Let everything to be in UTC we will convert date to respective Em time zone while querying.
		inputFormat.setTimeZone(TimeZone.getDefault());
		ArrayList<EcSyncVo> ecList = new ArrayList<EcSyncVo>();
		Date validLastT1 = null;
		try {
			mac = headers.getRequestHeader("em_mac").get(0);
			emTimeZone = headers.getRequestHeader("em_time_zone").get(0);
			if (validLastDate != null && !ArgumentUtils.isNullOrEmpty(mac)
					&& !ArgumentUtils.isNullOrEmpty(emTimeZone)) {
				if (!validLastDate.equalsIgnoreCase("NA")) {
					validLastT1 = inputFormat.parse(validLastDate);
				}// else If service is first time called.
				else {
					validLastT1 = energyConsumptionManager
							.loadFirstEnergyConsumptionDate(mac,emTimeZone);
				}
				ecList = (ArrayList<EcSyncVo>) energyConsumptionManager
						.load15minFloorEnergyConsumptionForAllFloorWithZb(
								validLastT1, mac,emTimeZone);
			} else {
				logger.warn("Time stamp passed is null or Mac is null or Em time Zone is null. Cannot aggregate for null object");
			}
		} catch (ParseException pe) {
			logger.error("correct time format not passed" + pe.getMessage(), pe);
		} catch (Exception e) {
			logger.error(
					"Error while getting 15 sync energy aggregation for uem "
							+ e.getMessage(), e);
		}
		return ecList;
	}

}
