/**
 * 
 */
package com.ems.cache;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.ems.ws.util.DeviceResponse;

/**
 * @author yogesh
 * 
 */
public class HvacDeviceStatusCache {
	private ConcurrentHashMap<String, DeviceResponse> ohvacDeviceStatus = new ConcurrentHashMap<String, DeviceResponse>();
	public static final HvacDeviceStatusCache m_instance = new HvacDeviceStatusCache();

	private HvacDeviceStatusCache() {

	}

	public static HvacDeviceStatusCache getInstance() {
		return m_instance;
	}

	public DeviceResponse getFunctionResponseForDevice(String sDeviceId,
			String sFunctionId, String args) {
		String strDeviceAndFunctionId = sDeviceId + "_" + sFunctionId + "_"
				+ args;
		DeviceResponse oResponse = ohvacDeviceStatus
				.get(strDeviceAndFunctionId);
		if (oResponse == null) {
			oResponse = new DeviceResponse();
			oResponse.setStatus(-1);
			oResponse.setMessage("Loading...");
		}
		return oResponse;
	}

	public void setFunctionResponseForDevice(String sDeviceId,
			String sFunctionId, DeviceResponse oResponse) {
		String args = "-1";
		if(oResponse != null) {
			args = oResponse.getArgs();
		}
		String strDeviceAndFunctionId = sDeviceId + "_" + sFunctionId + "_"
				+ args;
		oResponse.setoUpdatedTS(new Date());
		DeviceResponse oDeviceResponse = ohvacDeviceStatus
				.get(strDeviceAndFunctionId);
		if (oDeviceResponse == null) {
			ohvacDeviceStatus.put(strDeviceAndFunctionId, oResponse);
		} else {
			oDeviceResponse.copy(oResponse);
			oResponse = null;
		}
	}
}
