/**
 * 
 */
package com.ems.ws;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

import com.ems.cache.HvacDeviceStatusCache;
import com.ems.model.Building;
import com.ems.model.Floor;
import com.ems.model.HVACDevice;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BuildingManager;
import com.ems.service.FloorManager;
import com.ems.service.HVACDevicesManager;
import com.ems.types.HvacDeviceFunctionType;
import com.ems.types.HvacDeviceFunctionType.FanSpeed;
import com.ems.types.HvacDeviceFunctionType.HvacErrorCodes;
import com.ems.types.HvacDeviceFunctionType.RunningMode;
import com.ems.types.UserAuditActionType;
import com.ems.utils.ExecuteExternalProgram;
import com.ems.ws.util.DeviceResponse;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/hvac")
public class HVACDevicesService {
	private static final Logger logger = Logger.getLogger("HVACLogger");

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "hvacDevicesManager")
	private HVACDevicesManager hvacDevicesManager;

	@Resource(name = "floorManager")
	private FloorManager floorManager;

	@Resource(name = "buildingManager")
	private BuildingManager buildingManager;

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	public HVACDevicesService() {

	}

	/**
	 * Returns list of hvac devices
	 * 
	 * @param property
	 * @param pid
	 * @return
	 */
	@Path("list/{property}/{pid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<HVACDevice> getHVACDeviceList(
			@PathParam("property") String property, @PathParam("pid") Long pid) {
		return hvacDevicesManager.loadHVACDevicesByFacilityId(property, pid);
	}

	/**
	 * Returns Hvac details
	 * 
	 * @param hvac
	 *            name
	 * @return hvac details
	 */
	@Path("list/{hvacname}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public HVACDevice getUserList(@PathParam("hvacname") String hvacname) {
		return hvacDevicesManager.loadHvacByUserName(hvacname);
	}

	/**
	 * commission hvac device, send hvac request as follows <hvacdevice>
	 * <name>AC1</name> <floorid>1</floorid> <buildingid>1</buildingid>
	 * <campusid>1</campusid> <xaxis>20</xaxis> <yaxis>200</yaxis>
	 * <devicetype>1</devicetype> </hvacdevice>
	 * 
	 * @param device
	 * @return response status
	 * @throws IOException
	 * @throws SQLException
	 */
	@Path("commission")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveDevice(HVACDevice device) {
		Response oResponse = new Response();
		try {
			Floor floor = floorManager.getFloorById(device.getFloorId());
			Building building = buildingManager.loadBuilding(floor
					.getBuilding().getId());
			device.setBuildingId(building.getId());
			device.setCampusId(building.getCampus().getId());
			hvacDevicesManager.save(device);
		} catch (SQLException e) {
			oResponse.setStatus(1);
			oResponse.setMsg(e.getMessage());
		} catch (IOException e) {
			oResponse.setStatus(2);
			oResponse.setMsg(e.getMessage());
		}
		return oResponse;
	}

	/**
	 * Decommission's HVAC from GEMS
	 * 
	 * @param hvac
	 *            id "<hvacdevice><id>1</id></hvacdevice>"
	 * @return Response status, 1 indicates success.
	 */
	@Path("decommission")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deCommissionHVACDevice(HVACDevice hvacDevice) {
		Response response = new Response();
		response.setStatus(hvacDevicesManager.deleteHvacDevice(hvacDevice
				.getId()));
		userAuditLoggerUtil.log("Delete HVAC Device " + hvacDevice.getName(),
				UserAuditActionType.HVAC_Device_Delete.getName());
		return response;
	}

	/**
	 * 3rd Party HVAC module uses this service to publish the status of the
	 * respective function.
	 * <deviceResponse><status>0</status><message/><result>40</result></
	 * deviceResponse>
	 * 
	 * @param deviceId
	 * @param functionId
	 * @param oDeviceResponse
	 * @return response
	 */
	@Path("status/device/{deviceid}/function/{functionid}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response postHVACDeviceResult(
			@PathParam("deviceid") String deviceId,
			@PathParam("functionid") String functionId,
			DeviceResponse oDeviceResponse) {
		Response oResponse = new Response();
		HvacDeviceStatusCache oDeviceStatus = HvacDeviceStatusCache
				.getInstance();
		oDeviceStatus.setFunctionResponseForDevice(deviceId, functionId,
				oDeviceResponse);
		logger.info("Device: " + deviceId + ", functionId: " + functionId
				+ ", data: " + oDeviceResponse.toString());
		return oResponse;
	}

	/**
	 * Updates the position of the selected HVAC on the floorplan
	 * 
	 * @param Hvac
	 *            List of selected gateway with their respective x & y
	 *            co-ordinates
	 *            "<hVACDevices><hvacdevice><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></hvacdevice></hVACDevices>"
	 * @return Response status
	 */
	@Path("du/updateposition")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateGatewayPosition(List<HVACDevice> hvacDevice) {
		Iterator<HVACDevice> itr = hvacDevice.iterator();
		while (itr.hasNext()) {
			HVACDevice hvDevice = (HVACDevice) itr.next();
			if (hvDevice.getXaxis() != null && hvDevice.getYaxis() != null) {
				hvacDevicesManager.updateGatewayPosition(hvDevice);
			}
		}
		return new Response();
	}

	/**
	 * Fires the Device application based on the function and the args supplied
	 * 
	 * @param deviceId
	 *            DeviceId
	 * @param functionId
	 *            HVACDeviceFunctionType
	 * @param args
	 *            {FanSpeed | RunningMode | Temp}
	 * @return Response of the service call
	 */
	@Path("op/device/{deviceid}/function/{functionid}/{args:.*}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deviceOperation(@PathParam("deviceid") Integer deviceId,
			@PathParam("functionid") Integer functionId,
			@PathParam("args") Integer args) {
		Response oResponse = new Response();
		HvacDeviceFunctionType oFunction = HvacDeviceFunctionType
				.valueOf(functionId);
		StringBuffer oConsole = new StringBuffer();
		oConsole.append("Device: ").append(deviceId);
		ExecuteExternalProgram eeProgram = null;
		String sSudo = "sudo";
		String sProgram = "/var/lib/tomcat6/Enlighted/modbus_app";
		String[] procArgs = null;
		if (oFunction != null) {
			oConsole.append(", Function: ").append(oFunction.getFunctionId());
			switch (oFunction) {
			case READ_ROOM_TEMP:
				procArgs = new String[] {sSudo, sProgram, "-o",
						String.valueOf(deviceId), String.valueOf(functionId) };
				break;
			case READ_AMB_TEMP:
				procArgs = new String[] {sSudo,  sProgram, "-o",
						String.valueOf(deviceId), String.valueOf(functionId) };
				break;
			case READ_COMPRESSOR_STATUS:
				procArgs = new String[] {sSudo, sProgram, "-o",
						String.valueOf(deviceId), String.valueOf(functionId) };
				break;
			case SET_ROOM_TEMP:
				if (args != null) {
					oConsole.append(" ").append("args: ").append(args);
					procArgs = new String[] {sSudo, sProgram, "-o",
							String.valueOf(deviceId), String.valueOf(functionId), String.valueOf(args) };
					System.out.println("HAVC DEBUGGING SET ROOM TEMP--- > " +  Arrays.toString(procArgs));
				} else {
					oResponse.setStatus(HvacErrorCodes.NO_ARGUMENT.getId());
				}
				break;
			case SET_FAN_SPEED:
				if (args != null) {
					FanSpeed oFanSpeed = HvacDeviceFunctionType
							.getFanSpeed(args);
					if (oFanSpeed != null) {
						oConsole.append(" ").append("args: ")
								.append(oFanSpeed.getId());
						procArgs = new String[] {sSudo, sProgram, "-o",
								String.valueOf(deviceId),
								String.valueOf(functionId),
								String.valueOf(args) };
					} else {
						oResponse.setStatus(HvacErrorCodes.INVALID_ARGUMENT
								.getId());
					}
				} else {
					oResponse.setStatus(HvacErrorCodes.NO_ARGUMENT.getId());
				}
				break;
			case SET_RUNNING_MODE:
				if (args != null) {
					RunningMode oMode = HvacDeviceFunctionType
							.getRunningMode(args);
					if (oMode != null) {
						oConsole.append(" ").append("args: ")
								.append(oMode.getId());
						procArgs = new String[] {sSudo, sProgram, "-o",
								String.valueOf(deviceId),
								String.valueOf(functionId),
								String.valueOf(args) };
						
						System.out.println("HAVC DEBUGGING --- > " +  Arrays.toString(procArgs));
					} else {
						oResponse.setStatus(HvacErrorCodes.INVALID_ARGUMENT
								.getId());
					}
				} else {
					oResponse.setStatus(HvacErrorCodes.NO_ARGUMENT.getId());
				}
				break;
			case READ_MODBUS_STATUS:
				procArgs = new String[] {sSudo, sProgram, "-o",
						String.valueOf(deviceId), String.valueOf(functionId) };
				break;
			default:
				oResponse.setStatus(HvacErrorCodes.INVALID_FUNCTION.getId());
				break;
			}
		} else {
			oResponse.setStatus(HvacErrorCodes.INVALID_FUNCTION.getId());
		}
		logger.info(oConsole.toString());
		if (procArgs != null) {
			logger.info("Firing Application "+ Arrays.toString(procArgs));
			eeProgram = new ExecuteExternalProgram(procArgs);
			eeProgram.start();
			try {
				eeProgram.join();
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
			}
			oResponse.setStatus(eeProgram.getiStatus());
			oResponse.setMsg(eeProgram.getProcError());
		}
		return oResponse;
	}

	/**
	 * Used by the UI layer to fetch the status of the function that was fired
	 * before.
	 * 
	 * @param deviceId
	 *            DeviceId
	 * @param functionId
	 *            HVACDeviceFunctionType
	 * @return Response with status
	 */
	@Path("op/status/device/{deviceid}/function/{functionid}/args/{args}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public DeviceResponse getDeviceFunctionStatus(
			@PathParam("deviceid") String deviceId,
			@PathParam("functionid") String functionId,
			@PathParam("args") String args) {
		return HvacDeviceStatusCache.getInstance()
				.getFunctionResponseForDevice(deviceId, functionId, args);
	}

}
