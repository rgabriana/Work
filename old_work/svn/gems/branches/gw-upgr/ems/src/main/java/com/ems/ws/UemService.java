package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.processor.SUHeartBeatService;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.GatewayManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.util.tree.TreeNode;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/uem")
public class UemService {

	@Resource
	GatewayManager gatewayManager;
	
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	
	@Resource
	FixtureManager fixtureManager;
	
	@Autowired
	private MessageSource messageSource;
	
	@Resource
	FloorManager floorManager;
	
	@Resource
	FacilityTreeManager facilityTreeManager;
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getEmFacilityTree")
	@GET
	@Produces({ MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON })
	public TreeNode<String> getEmFacilityTree() {
		TreeNode<String> tree= facilityTreeManager.loadCompanyHierarchyForUem();
		return tree;
		
	}

	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("add/{host}/{port}/{key}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addUem(@PathParam("host") String host,
			@PathParam("port") Short port, @PathParam("key") String key) {

		Response resp = new Response();
		/*Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("ip", host);
    	nameValMap.put("port", port);    	
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
    	if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
		if (gatewayManager.getUEMGateway() == null) {
			List<Gateway> gws = gatewayManager.loadAllCommissionedGateways();
			if (gws.size() > 0) {
				Gateway gw = new Gateway();
				gw.setType("Gateway");
				gw.setName("UEM");

				try {
					Process pr = null;
					String[] cmdArr = { "/bin/bash",
							"/var/lib/tomcat6/webapps/ems/adminscripts/getMac.sh" };
					BufferedReader br = null;
					pr = Runtime.getRuntime().exec(cmdArr);
					pr.waitFor();
					br = new BufferedReader(new InputStreamReader(
							pr.getInputStream()));

					while (true) {
						String macId = br.readLine().trim();
						if (macId != null && !"".equals(macId)) {
							gw.setMacAddress(macId.toUpperCase());
							break;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				gw.setFloor(gws.get(0).getFloor());
				gw.setCampusId(gws.get(0).getCampusId());
				gw.setBuildingId(gws.get(0).getBuildingId());
				gw.setStatus(false);
				gw.setCommissioned(true);
				gw.setIpAddress(host);
				gw.setPort(port);
				gw.setVersion("2.0");
				short gwtype = 5;
				gw.setGatewayType(gwtype);
				gw.setWirelessEncryptKey(key);
				gw.setNoOfSensors(0);
				gw.setLastConnectivityAt(new Date());
				gatewayManager.save(gw);
				GatewayImpl.getInstance().addGatewayInfo(gw.getIpAddress());
				resp.setMsg("S");
			} else {
				resp.setMsg("FAIL: no device available.");
			}
		} else {
			Gateway gw = gatewayManager.getUEMGateway();
			gw.setWirelessEncryptKey(key);
			gw.setIpAddress(host);
			gw.setPort(port);
			gatewayManager.save(gw);
			resp.setMsg("S");
		}
		SUHeartBeatService.getInstance().setGw(gatewayManager.getUEMGateway());
		return resp;

	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getUemInfo")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUemInfo() {
		Response resp = new Response();
		String out = "0";
		SystemConfiguration sc = systemConfigurationManager.loadConfigByName("uem.enable");
		if(sc != null && sc.getValue() != null && !"".equals(sc.getValue())) {
			out = sc.getValue();
			if("1".equals(out)) {
				
				sc = systemConfigurationManager.loadConfigByName("uem.secretkey");
				if(sc != null && sc.getValue() != null && !"".equals(sc.getValue())) {
					out += "::::" + sc.getValue();
				}
				
				try {
					Process pr = null;
					String[] cmdArr = { "/bin/bash",
							"/var/lib/tomcat6/webapps/ems/adminscripts/getMac.sh" };
					BufferedReader br = null;
					pr = Runtime.getRuntime().exec(cmdArr);
					pr.waitFor();
					br = new BufferedReader(new InputStreamReader(
							pr.getInputStream()));

					while (true) {
						String macId = br.readLine().trim();
						if (macId != null && !"".equals(macId)) {
							out += "::::" + macId.toUpperCase();
							break;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				sc = systemConfigurationManager.loadConfigByName("uem.ip");
				if(sc != null && sc.getValue() != null && !"".equals(sc.getValue())) {
					out += "::::" + sc.getValue();
				}
			}
		}
		resp.setMsg(out);
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getSensorInfo/{mac}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSensorInfo(@PathParam("mac") String mac) {
		Response resp = new Response();
		/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "mac", mac);
    	if(resp!= null && resp.getStatus()!=200){
    	
    		return resp;
    	}*/
		String out = "";
		Fixture f = fixtureManager.getFixtureByMacAddr(mac);
		Set<String> outageMacs = fixtureManager.getOutageFixtures();
		if(f != null && f.getId() != null) {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			//{"id":"0","name":"1", "macAddress":"", "x": "", "y": "", "facilityId":"", "outageFlag":""}
			out += "{\"" +
					//"id\":\"" + f.getId() + "\",\"" +
					"name\":\"" + f.getSensorId() + 
					"\",\"macAddress\":\"" + f.getMacAddress() +
					"\",\"x\":\"" + f.getXaxis() 
					+ "\",\"y\":\"" + f.getYaxis() 
					+ "\",\"facilityId\":\"" + f.getFloorId() +
					"\",\"state\":\"" + f.getState() +
					"\",\"dimLevel\":\"" + f.getDimmerControl() + 
					"\",\"lastConnectivityAt\":\"" + dt.format(f.getLastConnectivityAt()) + 
					"\",\"outageFlag\":\"" + outageMacs.contains(f.getMacAddress()) + 
					"\"}";  
		}
		resp.setMsg(out);
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getAllSensors")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllSensors() {
		Response resp = new Response();
		List<Fixture> fixtures = fixtureManager.getAllCommissionedFixtureList();
		Set<String> outageMacs = fixtureManager.getOutageFixtures();
		StringBuilder out = new StringBuilder("");
		if(fixtures != null && fixtures.size() > 0) {
			out.append("{ \"sensors\": [");
			boolean isFirst = true;
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			for(Fixture f: fixtures ) {
				
				if(f != null && f.getId() != null) {
					if(!isFirst) {
						out.append(",");
					}
					else {
						isFirst = false;
					}

					out.append("{\"" +
						//"id\":\"" + f.getId() + "\",\"" +
						"name\":\"" + f.getSensorId() + 
						"\",\"macAddress\":\"" + f.getMacAddress() + 
						"\",\"x\":\"" + f.getXaxis() + 
						"\",\"y\":\"" + f.getYaxis() + 
						"\",\"facilityId\":\"" + f.getFloorId() +
						"\",\"state\":\"" + f.getState() +
						"\",\"dimLevel\":\"" + f.getDimmerControl() + 
						"\",\"lastConnectivityAt\":\"" + dt.format(f.getLastConnectivityAt()) + 
						"\",\"outageFlag\":\"" + outageMacs.contains(f.getMacAddress()) + 
						"\"}");  
				}
			}
			out.append("]}");
		}
		resp.setMsg(out.toString());
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getEmFacilityTreeForUem")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getEmFacilityTreeForUem() {
		Response resp = new Response();
		StringBuilder out = new StringBuilder();
		TreeNode<String> tree= facilityTreeManager.loadCompanyHierarchyForUem();
		out = createTreeJson(out, tree);
		resp.setMsg(out.toString());
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getFloorPlan/{fid}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public String getFloorPlan(@PathParam("fid") String fid) {
		
        try {
            Floor oFloor = floorManager.getFloorById(Long.parseLong(fid));
            if(oFloor != null && oFloor.getId() != null) {
            	return Base64.encodeBase64String(oFloor.getByteImage());
            }
        } catch (IndexOutOfBoundsException e1) {
           e1.printStackTrace();
        } catch (NumberFormatException e1) {
        	e1.printStackTrace();
        } catch (SQLException e1) {
        	e1.printStackTrace();
        } catch (IOException e1) {
        	e1.printStackTrace();
        }
        return null;
    }
	
	private StringBuilder createTreeJson(StringBuilder sb, TreeNode<String> tree) {
		sb.append("{\"id\":\"" + tree.getNodeId() + "\",\"" + "name\":\"" + tree.getName() + "\"");
		if(tree.getTreeNodeList() != null && tree.getTreeNodeList().size() > 0) {
			sb.append(",\"nodes\": [");
			boolean isFirst = true;
			for(TreeNode<String> node: tree.getTreeNodeList()) {
				if(!isFirst) {
					sb.append(",");
				}
				else {
					isFirst = false;
				}
				createTreeJson(sb, node);
			}
			sb.append("]");
		}
		
		sb.append("}");
		return sb;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("setOccChangeTrigger/{enable}/{triggerDelayTime}/{ack}")
	@POST
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response setPeriodicHB(List<Fixture> fixtures, @PathParam("enable") Short enable, @PathParam("triggerDelayTime") Integer triggerDelayTime, @PathParam("ack") Short ack) {
		Response resp = new Response();
		/*Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("uemEnable", enable);
    	nameValMap.put("triggerDelayTime", triggerDelayTime);
    	nameValMap.put("ack", ack);
    	resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
    	if(resp!= null && resp.getStatus()!=200){
    		return resp;
    	}*/
		if(fixtures != null && fixtures.size() > 0) {
			for(Fixture f: fixtures) {
				/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "mac", f.getMacAddress());
		    	if(resp!= null && resp.getStatus()!=200){
		    		
		    		return resp;
		    	}*/
				Fixture fix =  fixtureManager.getFixtureByMacAddr(f.getMacAddress());
				fix.setOccLevelTriggerTime(triggerDelayTime);
				fix.changeTriggerType(enable == 1, null);
				fixtureManager.save(fix);
				DeviceServiceImpl.getInstance().setTriggerType(fix, ServerConstants.SU_CMD_HB_CONFIG_MSG_TYPE, ack);
			}
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("getDimLevelsAndLastConnectivity")
	@POST
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDimLevelsAndLastConnectivity(List<Fixture> fixtures) {
		Response resp = new Response();
		StringBuilder out = new StringBuilder("");
		Set<String> outageMacs = fixtureManager.getOutageFixtures();
		if(fixtures != null && fixtures.size() > 0) {
			out.append("{ \"sensors\": [");
			boolean isFirst = true;
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			for (Fixture f: fixtures) {
				/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "mac", f.getMacAddress());
		    	if(resp!= null && resp.getStatus()!=200){
		    		return resp;
		    	}*/
				Fixture fix =  fixtureManager.getFixtureByMacAddr(f.getMacAddress());
				if(fix != null && fix.getId() != null) {
					if(!isFirst) {
						out.append(",");
					}
					else {
						isFirst = false;
					}
					

					// {"name":"1", "macAddress":"", "dimLevel": "", "lastConnectivityAt": ""}
					out.append("{\"" +
							"name\":\"" + fix.getSensorId() + 
							"\",\"macAddress\":\"" + fix.getMacAddress() + 
							"\",\"dimLevel\":\"" + fix.getDimmerControl() + 
							"\",\"lastConnectivityAt\":\"" + dt.format(fix.getLastConnectivityAt()) + 
							"\",\"x\":\"" + fix.getXaxis() + 
							"\",\"y\":\"" + fix.getYaxis() + 
							"\",\"facilityId\":\"" + fix.getFloorId() +
							"\",\"state\":\"" + fix.getState() +
							"\",\"outageFlag\":\"" + outageMacs.contains(fix.getMacAddress()) + 
							"\"}");  
				}
			}
			out.append("]}");
		}
		resp.setMsg(out.toString());
		return resp;
	}
	
	

}
