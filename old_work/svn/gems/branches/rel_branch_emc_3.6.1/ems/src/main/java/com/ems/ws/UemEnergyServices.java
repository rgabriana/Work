package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.service.EnergyConsumptionManager;
import com.ems.uem.model.EcSyncVo;

@Controller
@Path("/org/uem/ec")
public class UemEnergyServices {
	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;

	static final Logger logger = Logger.getLogger(UemEnergyServices.class
			.getName());

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("floor/15min/sync")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<EcSyncVo> get15MinFloorEC(String validLastDate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		ArrayList<EcSyncVo> ecList = new ArrayList<EcSyncVo>();
		Date validLastT1 = null;
		try {

			if (validLastDate != null) {

				if (!validLastDate.equalsIgnoreCase("NA")) {
					validLastT1 = inputFormat.parse(validLastDate);
					logger.debug("last date recieved at EM end is "+validLastT1);
				}// else If service is first time called.
				else {
					validLastT1 = energyConsumptionManager
							.loadFirstEnergyConsumption().getCaptureAt();
					logger.debug("first capture at is  "+validLastT1);
				}
				ecList = (ArrayList<EcSyncVo>) energyConsumptionManager
						.load15minFloorEnergyConsumptionForAllFloorWithZb(validLastT1);
			} else {
				logger.warn("Time stamp passed is null. Cannot aggregate for null object");
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
