package com.ems.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

import com.ems.model.Device;
import com.ems.model.Groups;
import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerConstants;
import com.ems.server.upgrade.FixtureImageUpgradeWorker;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.service.FirmwareUpgradeManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.SwitchManager;
import com.ems.types.DeviceType;
import com.ems.vo.DeviceFixture;
import com.ems.vo.DeviceGateway;
import com.ems.vo.DeviceStatusList;
import com.ems.vo.DeviceWds;
import com.ems.ws.util.Response;


@Controller
@Path("/org/imageupgrade")
public class ImageUpgradeService {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "gatewayManager")
	private GatewayManager gatewayManager;
	
	@Resource(name = "firmwareUpgradeManager")
	private FirmwareUpgradeManager firmwareUpgradeManager;
	

	@Resource
	SwitchManager switchManager;
	@Resource(name = "gemsGroupManager")
	private GemsGroupManager gemsGroupManager;
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	/**
	 * Return status if image upgrade is running
	 * @return boolean (true/false)
	 */
	@Path("jobstatus")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getImageUpgradeJobCurrentStatus() {
		Response oStatus = new Response();
		if(ImageUpgradeSO.isInProgress())
		{
			oStatus.setStatus(0);
			oStatus.setMsg("Image Running");
		}else
		{
			oStatus.setStatus(-1);
		}
		return oStatus;
	}
	
	@Path("status/{date}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public DeviceStatusList getAllDeviceStatus(@PathParam("date") String date) {
		
		DeviceStatusList mDeviceStatusList = new DeviceStatusList();

		List<DeviceFixture> mDeviceFixtureList = new ArrayList<DeviceFixture>();
		List<DeviceGateway> mDeviceGatewayList = new ArrayList<DeviceGateway>();
		List<DeviceWds> mDeviceWdsList = new ArrayList<DeviceWds>();

		Date convertedFromDate = null;
		Date convertedToDate = null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	    try {
			convertedToDate = dateFormat.parse(date);
			convertedFromDate = new Date(convertedToDate.getTime() - 24 * 3600 * 1000 );	// Get date 24 hours before current date/time
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	   
	    
		// Status of the fixtures
		List<ImageUpgradeDeviceStatus> oList = firmwareUpgradeManager
				.loadDeviceStatus(convertedFromDate,convertedToDate);		
		Iterator<ImageUpgradeDeviceStatus> oItr = oList.iterator();
		while (oItr.hasNext()) {
			ImageUpgradeDeviceStatus oDeviceStatus = oItr.next();
			// System.out.println(oDeviceStatus.getDeviceId() + ": " +
			// oDeviceStatus.getDevice_type());			
				if (DeviceType.Fixture.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceFixture mDeviceFixture = new DeviceFixture();
					mDeviceFixture.setFixtureId(oDeviceStatus.getDeviceId());
					mDeviceFixture.setFixtureName("");
					mDeviceFixture.setStatus(oDeviceStatus.getStatus());
					mDeviceFixture.setVersion(oDeviceStatus.getNew_version());
					mDeviceFixtureList.add(mDeviceFixture);		
				}
				
				if (DeviceType.Gateway.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceGateway mDeviceGateway = new DeviceGateway();					
					mDeviceGateway.setGatewayId(oDeviceStatus.getDeviceId());					
					mDeviceGateway.setGatewayName("");
					mDeviceGateway.setStatus(oDeviceStatus.getStatus());
					mDeviceGateway.setVersion(oDeviceStatus.getNew_version());
					mDeviceGatewayList.add(mDeviceGateway);			
				}
				if (DeviceType.WDS.getName().equalsIgnoreCase(
						oDeviceStatus.getDevice_type())) {
					DeviceWds mDeviceWds = new DeviceWds();									
					mDeviceWds.setWdsId(oDeviceStatus.getDeviceId());					
					mDeviceWds.setWdsName("");
					mDeviceWds.setStatus(oDeviceStatus.getStatus());					
					mDeviceWds.setVersion(oDeviceStatus.getNew_version());
					mDeviceWdsList.add(mDeviceWds);			
				}
			 
			 //Take the scheduled list from the thread map
			 //Take the in progress states from the fixture table
			 //Take the success/fails from the database 
			 
		}
		mDeviceStatusList.setDeviceFixtureList(mDeviceFixtureList);
		mDeviceStatusList.setDeviceGatewayList(mDeviceGatewayList);
		mDeviceStatusList.setDeviceWdsList(mDeviceWdsList);
		
		
		return mDeviceStatusList;

	}
	
}
