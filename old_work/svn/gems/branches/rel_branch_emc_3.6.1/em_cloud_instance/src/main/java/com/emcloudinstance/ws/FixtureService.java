/**
 * 
 */
package com.emcloudinstance.ws;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.utils.ArgumentUtils;
import com.emcloudinstance.service.DeviceManager;
import com.emcloudinstance.vo.Fixture;

/**
 * @author yogesh
 * 
 */
@Component
@Path("/org/fixture")
public class FixtureService {
	static final Logger logger = Logger.getLogger(FixtureService.class
			.getName());

	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;

	@Path("list/{property}/{pid}/{limit:.*}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Fixture> getFixtureList(@Context HttpHeaders headers,
			@PathParam("property") String property, @PathParam("pid") Long pid,
			@PathParam("limit") String limit) {
		String mac = null;
		String emTimeZone = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		// Let everything to be in UTC we will convert date to respective Em
		// time zone while querying.
		inputFormat.setTimeZone(TimeZone.getDefault());

		try {
			mac = headers.getRequestHeader("em_mac").get(0);
			emTimeZone = headers.getRequestHeader("em_time_zone").get(0);
			if (!ArgumentUtils.isNullOrEmpty(mac)
					&& !ArgumentUtils.isNullOrEmpty(emTimeZone)) {
				return deviceManager.loadFixtures(mac, property, pid, limit);

			}
		} catch (Exception e) {
			logger.error(" " + e.getMessage(), e);
		}
		return null;

	}

}
