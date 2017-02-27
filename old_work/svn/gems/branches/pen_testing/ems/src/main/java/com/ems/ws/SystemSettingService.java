package com.ems.ws;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.action.SpringContext;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.utils.MD5;
import com.ems.ws.util.Response;

@Controller
@Path("/system")
public class SystemSettingService {
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	@Autowired
	private MessageSource messageSource;
	
	@Resource
	NetworkSettingsManager networkSettingsManager;

	public SystemSettingService() {
	}

	/**
	 * @param static or dynamic ip type
	 * @param ip address
	 * @param mask
	 * @param gateway
	 * @return status of the service request
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updatenetworkdetails")
	@POST
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String editNetworkDetails(@FormParam("type") String type,
			@FormParam("ip") String ip, @FormParam("mask") String mask,
			@FormParam("gateway") String gateway) {
		Response resp = new Response();
		Map<String,Object> nameValMap = new HashMap<String,Object>();
		nameValMap.put("type", type);
		nameValMap.put("ip", ip);
		nameValMap.put("mask", mask);
		nameValMap.put("gateway", gateway);
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error "+resp.getMsg());
			return resp.toString();
		}		

		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType("Corporate");
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType("Building");
		if(nimBuilding != null && nimBuilding.getNetworkSettings()!= null && nimBuilding.getNetworkSettings().getName()!= null){
			buildingMapping = nimBuilding.getNetworkSettings().getName();
		}else{
			buildingMapping = corporateMapping;
		}
		boolean bStatus = true;
		StringBuffer oBuff = new StringBuffer();
		oBuff.append("auto lo").append("\r\n");
		oBuff.append("\t").append("iface lo inet loopback").append("\r\n");
		oBuff.append("auto "+corporateMapping).append("\r\n");
		if (type.equals("static")) {
			oBuff.append("\t").append("iface "+corporateMapping+" inet static").append("\r\n");
			oBuff.append("\t").append("address ").append(ip).append("\r\n");
			oBuff.append("\t").append("netmask ").append(mask).append("\r\n");
			if (!gateway.equals(""))
				oBuff.append("\t").append("gateway ").append(gateway)
						.append("\r\n");
		} else {
			oBuff.append("\t").append("iface "+corporateMapping+" inet dhcp").append("\r\n");
		}
		oBuff.append("auto "+buildingMapping).append("\r\n");
		oBuff.append("\t").append("iface "+buildingMapping+" inet static").append("\r\n");
		oBuff.append("\t").append("address ").append("169.254.0.1")
				.append("\r\n");
		oBuff.append("\t").append("netmask ").append("255.255.0.0")
				.append("\r\n");

		String tomcatLocation = ServerMain.getInstance().getTomcatLocation();

		File oNetworkInterfaceFile = new File(tomcatLocation
				+ "../../Enlighted/interface.1");

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(oNetworkInterfaceFile));
			bw.write(oBuff.toString());
			bw.flush();
			bw.close();
			bw = null;

			Process pr = null;
			try {
				String[] cmdArr = { "/bin/bash",
						tomcatLocation + "/adminscripts/setupnetworking.sh",
						oNetworkInterfaceFile.getPath() };
				pr = Runtime.getRuntime().exec(cmdArr);
				readErrorStream(pr);
				int result = pr.waitFor();
				if (result == 0) {
					// TODO Put successful logger
					// logger.info("Changed Successfully to: " +
					// ServerMain.getInstance().getIpAddress("eth0"));
					m_Logger.debug("Changed Successfully to: "
							+ ServerMain.getInstance().getIpAddress(corporateMapping));
					userAuditLoggerUtil.log("Network connection details updated", UserAuditActionType.Network_Update.getName());
				}

			} catch (Exception e) {
				bStatus = false;
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bStatus = false;
		} catch (IOException e) {
			e.printStackTrace();
			bStatus = false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (bStatus) {
			// TODO Put logger.
			return "S";
		} else {
			// TODO Put logger.
			return "F";
		}

	}

	private void readErrorStream(final Process process) {

		new Thread() {
			public void run() {
				BufferedReader br = null;
				StringBuffer errBuffer = new StringBuffer();
				try {
					ServerUtil.sleep(1);
					br = new BufferedReader(new InputStreamReader(
							process.getErrorStream()));
					String line = "";
					while (true) {
						line = br.readLine();
						if (line == null) {
							break;
						}
						errBuffer.append(line).append("\r\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (Exception e) {
						}
					}
					// TODO Log errBuffer
					// logger.debug(errBuffer.toString());
					m_Logger.debug(errBuffer.toString());
				}
			}
		}.start();
	} // end of method readErrorStream
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
    @Path("emsmgmt/validate/{key}")
    @POST
    @Produces("text/plain")
    public String validateEmsMgmtSession(@PathParam("key") String key, @Context HttpServletRequest req) {
    	HttpSession session = req.getSession(false);
		if(session != null && session.getAttribute("securityKey") != null && key.equals(session.getAttribute("securityKey").toString().substring(0, 15))) {
			try {
				String retKey = MD5.hash(session.getAttribute("securityKey").toString().substring(0, 15));
				userAuditLoggerUtil.log("EM management session validated successfully.", UserAuditActionType.EM_Management_Validation.getName());
				return retKey;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		userAuditLoggerUtil.log("EM management session validation failed.", UserAuditActionType.EM_Management_Validation.getName());
		return "F";
    }
    /**
	 * Send enable or disable Timer to Fetch FX Curve for SU 2 and CU 2 if it's already available.
	 * @param enable
	 * @return response status of this service call.
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("op/togglefetchbaselinetimer/{enabled}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response enabledisableFetchSUBaselineTask(@PathParam("enabled") Boolean enable) {
		ServerMain.getInstance().enabledisableFetchSUBaselineTask(enable);
		return new Response();
	}

}