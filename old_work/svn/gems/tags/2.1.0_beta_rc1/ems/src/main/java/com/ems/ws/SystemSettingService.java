package com.ems.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.types.UserAuditActionType;

@Controller
@Path("/system")
public class SystemSettingService {
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	public SystemSettingService() {
	}

	/**
	 * @param static or dynamic ip type
	 * @param ip address
	 * @param mask
	 * @param gateway
	 * @return status of the service request
	 */
	@Path("updatenetworkdetails")
	@POST
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String editNetworkDetails(@FormParam("type") String type,
			@FormParam("ip") String ip, @FormParam("mask") String mask,
			@FormParam("gateway") String gateway) {

		boolean bStatus = true;
		StringBuffer oBuff = new StringBuffer();
		oBuff.append("auto lo").append("\r\n");
		oBuff.append("\t").append("iface lo inet loopback").append("\r\n");
		oBuff.append("auto eth0").append("\r\n");
		if (type.equals("static")) {
			oBuff.append("\t").append("iface eth0 inet static").append("\r\n");
			oBuff.append("\t").append("address ").append(ip).append("\r\n");
			oBuff.append("\t").append("netmask ").append(mask).append("\r\n");
			if (!gateway.equals(""))
				oBuff.append("\t").append("gateway ").append(gateway)
						.append("\r\n");
		} else {
			oBuff.append("\t").append("iface eth0 inet dhcp").append("\r\n");
		}
		oBuff.append("auto eth1").append("\r\n");
		oBuff.append("\t").append("iface eth1 inet static").append("\r\n");
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
							+ ServerMain.getInstance().getIpAddress("eth0"));
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

}
