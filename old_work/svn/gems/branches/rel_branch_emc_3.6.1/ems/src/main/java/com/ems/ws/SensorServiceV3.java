package com.ems.ws;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.FixtureManager;
import com.ems.service.LicenseSupportManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.utils.DateUtil;
import com.ems.vo.model.Sensor;
import com.ems.vo.model.SensorEnergyStats;
import com.ems.ws.util.Response;

/**
 * @author sreedhar.kamishetti
 * 
 */
@Controller
@Path("/org/sensor/v3")
public class SensorServiceV3 extends SensorServiceV1 {

	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;
	@Autowired
	private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	@Resource(name = "licenseSupportManager")
	private LicenseSupportManager licenseSupportManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	/**
	 * 
	 */
	public SensorServiceV3() {
	}

	/**
	 * Returns the energy consumption data for a specified time for the fixture
	 * 
	 * @param fromDate
	 *            in the format yyyyMMddHHmmss
	 * @param toDate
	 *            in the format yyyyMMddHHmmss
	 * @param floorId
	 * @return sensorEnergyStats
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee','Bacnet')")
	@Path("sensorEnergyStats/15min/floor/{floorId}/{fromDate}/{toDate}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<SensorEnergyStats> getECDataByFixtureId(
			@PathParam("floorId") Long floorId,
			@PathParam("fromDate") String fromDate,
			@PathParam("toDate") String toDate) {
		List<SensorEnergyStats> list = null;
		Response resp = new Response();
		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("fromDate", fromDate);
		nameValMap.put("toDate", toDate);
		resp = CommonUtils.isParamValueAllowed(messageSource,
				systemConfigurationManager, nameValMap);
		if (resp != null && resp.getStatus() != 200) {
			m_Logger.error("Validation error " + resp.getMsg());
			return null;
		}
		list = energyConsumptionManager.getECDataByFixture(
				DateUtil.parseString(fromDate, "yyyyMMddHHmmss"),
				DateUtil.parseString(toDate, "yyyyMMddHHmmss"), floorId);
		
		if (list != null && !list.isEmpty()) {
			for (final SensorEnergyStats ses : list){
				if (ses != null){
					final List<com.ems.model.Sensor> sl = ses.getList();
					if(sl != null && !sl.isEmpty()){
						for(final com.ems.model.Sensor s : sl){
							Fixture fixture = FixtureCache.getInstance().getCachedFixture(s.getFixtureId().longValue());
							if(licenseSupportManager.isFixtureOccupancySensorEnabled()){
								if(fixture == null || fixture.getLightLevel()==null)
								{
									s.setAmbientLight(-1);
								}else
								{
									s.setAmbientLight(fixture.getLightLevel());	
								}
							}else{
								s.setOccCount(new BigInteger("-2"));
								s.setAmbientLight(-2);
							}
						}
					}
				}
			}
		}
		return list;
	}
	
	
	 /**
     * Returns Fixture Details
     * 
     * @param fid
     *            fixture unique identifier
     * @return fixture details
     */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Employee','Auditor','Bacnet')")
    @Path("details/{fid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Sensor getSensorDetails(@PathParam("fid") Long fid) {
        long captureTime = System.currentTimeMillis();
        long min5 = 5 * 60 * 1000;
        captureTime = captureTime - captureTime % min5 - min5;
        List results = fixtureManager.getRecentSensorData(fid, captureTime);
        Sensor sensor = null;
        if (results != null && !results.isEmpty()) {
            Iterator sensorIt = results.iterator();
            if (sensorIt.hasNext()) {
                Object[] dataArr = (Object[]) sensorIt.next();
                sensor = new Sensor((Long) dataArr[0], dataArr[1].toString(), (BigDecimal) dataArr[2],
                        (Long) dataArr[3], (Double) dataArr[4]);
                
                sensor.setLightLevel((Short) dataArr[5]);
                sensor.setOccupancy(null);
            }
        }
        return sensor;

    } // end of method getSensorDetails
} // end of class SensorServiceV3