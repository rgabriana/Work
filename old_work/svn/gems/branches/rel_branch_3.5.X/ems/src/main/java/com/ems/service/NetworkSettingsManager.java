package com.ems.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.NetworkSettingsDao;
import com.ems.model.EmailConfiguration;
import com.ems.model.LdapSettings;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.NetworkSettings;
import com.ems.model.NetworkTypes;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerMain;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.types.NetworkType;
import com.ems.util.Constants;
import com.ems.utils.AdminUtil;
import com.ems.vo.InterfacesList;

import edu.emory.mathcs.backport.java.util.Arrays;


@Service("networkSettingsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class NetworkSettingsManager {
	
	static final Logger logger = Logger.getLogger(NetworkSettingsManager.class);
	private static Logger syslog = Logger.getLogger("SysLog");

	@Autowired
	private MessageSource messageSource;
	@Resource
	private SystemConfigurationManager systemConfigurationManager;

	@Resource
	private NetworkSettingsDao networkSettingsDao;
	
	@Resource
	private BacnetManager bacnetManager;

	@Resource
	private EmailManager emailManager;
	
	@Resource
	private LdapSettingsManager ldapSettingsManager; 
	
	public void saveNetworkSettings(NetworkSettings networkSettings) {
		networkSettingsDao.saveNetworkSettings(networkSettings);
	}

	public NetworkTypes loadNetworkTypeByName(String networkTypeName) {
		return networkSettingsDao.loadNetworkTypeByName(networkTypeName);
	}
	
	/**
	 * This will make sure in defaulting bacnet network to corp network if no mapping present against bacnet network in the system
	 * @return
	 */
	public boolean configureBacnetNetworkInterfaceToCorpIfNotExists(){
		//UPDATE THE NETWORK CONFIGURATION FOR BACNET TO CORPO NETWORK IF NOT PRESENT IN THE DB
    	final NetworkInterfaceMapping netbacnet = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.BACnet.getName());
		final boolean isBacnetConfigured = netbacnet != null && netbacnet.getNetworkSettings() != null && netbacnet.getNetworkSettings().isEnablePort();
		if(!isBacnetConfigured){
			//Get the Current Corp Network
			final NetworkInterfaceMapping netcorp = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
			if(netcorp == null){
				logger.error("**CORP Network is not configured yet********* Please contanct admin................");
			}else{
				logger.info("***Creating bacnet network interface on corporate**********");
				NetworkInterfaceMapping nim = new NetworkInterfaceMapping(); 
				if(netbacnet != null){
					nim = netbacnet;
				}
				nim.setNetworkSettings(netcorp.getNetworkSettings());
				if(nim.getNetworkSettingsId() == null){
					nim.setNetworkSettingsId(netcorp.getNetworkSettings().getId());
				}
				final NetworkTypes nt = loadNetworkTypeByName(NetworkType.BACnet.getName());
				nim.setNetworkTypes(nt);
				nim.setNetworkTypeId(nt.getId());
				saveNetworkInterfaceMappings(nim);
				logger.info("***Successfully created bacnet network interface on corporate**********");
			}
		}
		return true;
	}
	public boolean saveInitialNetworkSettings() {
		List<NetworkSettings> networkSettingsList = new ArrayList<NetworkSettings>();	
		try {
			String interfacesJson = AdminUtil.callNetworkInterfacesScript(); 
			//"{\"interfaces\":[{\"name\":\"p0\",\"macaddress\":\"d4:3d:7e:6d:63:62\",\"ipaddress\":\"192.168.4.133\",\"subnet_mask\":\"255.255.255.0\",\"default_gateway\":\"192.168.4.1\",\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"up\",\"is_dhcp_server\":false},{\"name\":\"p1\",\"macaddress\":\"d4:3d:7e:6d:63:63\",\"ipaddress\":\"169.254.0.1\",\"subnet_mask\":\"255.255.0.0\",\"default_gateway\":null,\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"down\",\"is_dhcp_server\":false},{\"name\":\"wlan0\",\"macaddress\":\"6c:71:d9:4c:d5:2a\",\"ipaddress\":null,\"subnet_mask\":null,\"default_gateway\":null,\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"down\",\"is_dhcp_server\":false}]}";			
			syslog.info("=========PHP Ouput is:" + interfacesJson + "");	
			//boolean isDHCPEnabled = networkSettingsDao.isDHCPEnabled();
			ObjectMapper mapper = new ObjectMapper();
			if(interfacesJson != null){
				InterfacesList networkInterfaces = null;			
				networkInterfaces = mapper.readValue(interfacesJson, InterfacesList.class);
				for(NetworkSettings ni : networkInterfaces.getInterfaces()){					
					networkSettingsList.add(ni);								
				}
				networkSettingsDao.saveInitialNetworkSettings(networkSettingsList);		
				networkSettingsDao.saveInitialNetworkSettingsMappings(networkSettingsList);
				
				//Fix -- Timing issue -- sometimes the network_settings_info.php does return the IP against building as null. SO we have to explicitely check and add it.
				// if building network exists and its configure_ipv4 is static and if the dhcp server is enabled then add default up and mask to it.
				final NetworkInterfaceMapping netbuilding = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
				final boolean isBuildingEnabled = netbuilding != null && netbuilding.getNetworkSettings() != null;
				if(isBuildingEnabled){
					NetworkSettings ns = netbuilding.getNetworkSettings();
					if (!ns.isIs_interface_dhcp() && isDHCPEnabled()){
						ns.setIpaddress("169.254.0.1");
						ns.setSubnet_mask("255.255.0.0");
//						networkSettingsDao.saveNetworkSettings(ns);
						syslog.info("********************Made sure to overrite the initial gw ip as 169.254.0.1 and subnet as 255.255.0.0");
					}
				}
				return true;
			}else{
				return false;				
			}			

		}catch (JsonParseException e) {			
			e.printStackTrace();
			return false;
		} catch (JsonMappingException e) {			
			e.printStackTrace();
			return false;
		} catch (IOException e) {			
			e.printStackTrace();
			return false;
		} 
		catch (Exception ioe) {
			ioe.printStackTrace();
			return false;
		}
	}

		

	public List<NetworkSettings> loadAllNetworkInterfacesSettings() {
		return networkSettingsDao.loadAllNetworkInterfacesSettings();
	}

	public List<NetworkInterfaceMapping> loadCurrentNetworkInterfaceMapping() {
		return networkSettingsDao.loadCurrentNetworkInterfaceMapping();
	}

	public List<NetworkTypes> loadAllNetworkTypes() {
		return networkSettingsDao.loadAllNetworkTypes();
	}

	public List loadCustomFieldsFromNetworkType(String networkType) {
		return networkSettingsDao.loadCustomFieldsFromNetworkType(networkType);
	}
			
	public NetworkInterfaceMapping loadCurrentNetworkInterfaceMappingByNetworkType(
			String networkType) {
		return networkSettingsDao
				.loadCurrentNetworkInterfaceMappingByNetworkType(networkType);
	}

	public NetworkInterfaceMapping loadCurrentNetworkInterfaceMappingByNetworkTypeId(
			Long networkType) {
		return networkSettingsDao
				.loadCurrentNetworkInterfaceMappingByNetworkTypeId(networkType);
	}

	public void saveNetworkInterfaceMappings(NetworkInterfaceMapping nim) {
		networkSettingsDao.saveNetworkInterfaceMappings(nim);
	}

	//

	public NetworkSettings loadNetworkSettingsById(Long networkSettingsId) {
		return networkSettingsDao.loadNetworkSettingsById(networkSettingsId);

	}

	public boolean isDHCPEnabled() {
		return networkSettingsDao.isDHCPEnabled();
	}

	public NetworkTypes loadNetworkTypeById(Long networkTypeId) {
		return networkSettingsDao.loadNetworkTypeById(networkTypeId);

	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
	public boolean applyNetworkSettings(
			List<NetworkInterfaceMapping> networkInterfaceMappings,
			List<NetworkSettings> nsList, final Map<String, String> mapHelper)
			throws Exception {

		try {
			int bacnetPortPrev = 0;
			String bacnetinterfacePrev = "";
			if (mapHelper != null
					&& mapHelper.containsKey("bacnetListenPrevPort")) {
				final String bacnetListenPrevPort = mapHelper
						.get("bacnetListenPrevPort");
				bacnetPortPrev = !StringUtils.isEmpty(bacnetListenPrevPort) ? Integer
						.parseInt(bacnetListenPrevPort) : 0;
			}

			if (mapHelper != null
					&& mapHelper.containsKey("bacnetListenPrevInterface")) {
				final String bacnetListenPrevInterface = mapHelper
						.get("bacnetListenPrevInterface");
				bacnetinterfacePrev = !StringUtils
						.isEmpty(bacnetListenPrevInterface) ? bacnetListenPrevInterface
						: "";
			}
			updateNetworkDetails(networkInterfaceMappings, nsList);
			final SystemConfiguration config = systemConfigurationManager.loadConfigByName(Constants.IPTABLES_STATIC_PORTS_KEY);
			//Check the ports enabled for email(587) and backnet and ldap (389)
			
			final StringBuffer allPorts = new StringBuffer(config.getValue());
			final NetworkInterfaceMapping netbacnet = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.BACnet.getName());
			final boolean isBacnetEnabledFromNS = netbacnet != null && netbacnet.getNetworkSettings() != null && netbacnet.getNetworkSettings().isEnablePort();
			//Check whether corporate network is enabled or not
			final NetworkInterfaceMapping netcorp = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
			final boolean isBacnetEnabled = bacnetManager.isBacnetEnabled();
			boolean isBacnet = true;
			/**
			 * Covering following use cases for bacnet related port opening
			 * 
bacnet 		corp 		isbacnet_enabled 	port to open on eth0	port to open of eth1
NA			eth0		YES					NO						NO
NA			eth0		No					NO						NO

eth0		eth0		YES					YES						NO
eth0		eth0		NO					NO						NO

eth1		eth0		YES					NO						YES
eth1		eth0		NO					NO						YES

			 */
			if(isBacnetEnabledFromNS == false ||  !bacnetManager.isBacnetServiceEnabled()) {
				isBacnet = false;
			}else{
				//IF bacnetis running and bacnet network is supplied and bacnet network is on corp network then include port on it
				if(bacnetManager.isBacnetServiceEnabled() && isBacnetEnabledFromNS && netbacnet.getNetworkSettings().getName().equals(netcorp.getNetworkSettings().getName())){
					isBacnet = true;
				}else{
					isBacnet = false;
				}
			}
			extractEMPorts(allPorts,isBacnet );
			syslog.info("All ports to be configured for corporate network are:"+ (allPorts.toString()) +":");
			final boolean isCorpEnabled = netcorp != null && netcorp.getNetworkSettings() != null && netcorp.getNetworkSettings().isEnablePort();
			final NetworkInterfaceMapping netbuilding = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
			final boolean isBuildingEnabled = netbuilding != null && netbuilding.getNetworkSettings() != null && netbuilding.getNetworkSettings().isEnablePort();
			
			//FOR CORP NETWORK PORTS CONFIG
			String acceptDropPortStr = "ACCEPT";
			if (!isCorpEnabled){
					acceptDropPortStr="DROP";
			}
			
			// get dhcp server whether enabled or disabled.
			final SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
			final boolean isDHCPServer = isBuildingEnabled && dhcpConfig !=null && !StringUtils.isEmpty(dhcpConfig.getValue()) && dhcpConfig.getValue().equalsIgnoreCase(Constants.TRUE);
			
			//Update Interfaces configuration and restart them. Also apply iptables rules
			final StringBuffer ifstr = new StringBuffer("auto lo\\niface lo inet loopback\\n");
			final List<String> interfacesList = new ArrayList<String>();
			for (final NetworkSettings s : loadAllNetworkInterfacesSettings()){
				generateaAllInterfacesString(ifstr, interfacesList, s, netcorp.getNetworkSettings().getName(), isBuildingEnabled?netbuilding.getNetworkSettings().getName():null );
			}
			ifstr.append("");
			
			String bacnetportcmd="null";
			if (isBacnetEnabled && isBacnetEnabledFromNS){
			    final NetworkSettings netBacnet = netbacnet.getNetworkSettings();
			    final String bacnetinterface = netBacnet.getName();
			    if(!StringUtils.isEmpty(bacnetinterface)){
			     final int bacnetPort = bacnetManager.getConfig().getServerPort();
			     if(bacnetPort >= 0){
			      //String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", String.valueOf(bacnetPort), "0", bacnetinterface.trim() };
			      bacnetportcmd = " "+ ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh "+String.valueOf(bacnetPort)+ " "+String.valueOf(bacnetPortPrev)+" " + bacnetinterface.trim()+" "+bacnetinterfacePrev;
			    }
			  }
			}else{
				bacnetManager.diableBacnetFromNetworkSettings();
				bacnetportcmd = " "+ ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh "+String.valueOf(0)+ " "+String.valueOf(bacnetPortPrev)+" " + "null"+" "+bacnetinterfacePrev;
			}
			/**
			 * Command like will be executed in the backend
			 * /bin/bash /var/lib/tomcat6/webapps/ems/adminscripts/applyNetworkSettings.sh "22,8085,7,443,8443,80,47808,591" eth0 ACCEPT eth1 "auto lo\niface lo inet loopback\n\nauto eth0 \niface eth0 inet static\n        address 192.168.137.222\n       netmask 255.255.255.0\n gateway 192.168.137.1\nauto eth1 \niface eth1 inet static\n     address 169.254.0.1\n   netmask 255.255.0.0" " /var/lib/tomcat6/webapps/ems/adminscripts/enablePort.sh 8087 0 eth1 "
			 */
			String[] allInOneCmd = {"/bin/bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/applyNetworkSettings.sh", 
					allPorts.toString(),netcorp.getNetworkSettings().getName().trim(),  acceptDropPortStr,
					isDHCPServer?netbuilding.getNetworkSettings().getName().trim():"null",
					ifstr.toString(),
					bacnetportcmd
					};
			syslog.info("allInOneCmd command is "+ Arrays.deepToString(allInOneCmd) );
			Process prAllInOne = Runtime.getRuntime().exec(allInOneCmd);
			AdminUtil.readStreamOfProcess(prAllInOne, true);
			final int status = prAllInOne.waitFor();
			//syslog.info("INPUT STREAM MESSAGES ARE: "+inputStream);
			final boolean success = status == 0;
			if(!success){
				syslog.error("********************************ERROR:: NEtwork settings are not applied. Please contact admin.********************************************");
			}
			syslog.info("***********Starting SSL Network Interfaces*****************");
			SSLSessionManager.getInstance().initNwInterface();
			syslog.info("**Flag Network sttings got applied:"+success);
			
			if(!success){
				throw new Exception("***Network Settings not applied correctly. Rollback the transaction****");
			}
			return success;
		} catch (Exception e) {
			syslog.error("ERROR: Not able to apply network settings to EM. Please contact Administrator ", e);
			throw e;
		}
	}

	private void extractEMPorts(final StringBuffer allPorts, final boolean isBacnetEnabled) {
		String port = "";
		//get backnet port from backNetManger.getConfig
		if(isBacnetEnabled){
			port = bacnetManager.getBacnetConfiguration().getProperty("ListenPort");
			if (!StringUtils.isEmpty(port)){
				allPorts.append(","+port.trim());
			}
		}
		//get email config port from emailconfig
		final EmailConfiguration ecfg = emailManager.loadEmailConfig();
		if(ecfg != null){
			port = ecfg.getPort();
			if (!StringUtils.isEmpty(port)){
				allPorts.append(","+port.trim());
			}
		}
		//get ldapport from ldapconfig LdapSettings ldapSettings= ldapSettingsManager.loadById(1l)
		final LdapSettings ldapcfg= ldapSettingsManager.loadById(1l);
		if (ldapcfg != null){
			port = String.valueOf(ldapcfg.getPort());
			if (!StringUtils.isEmpty(port)){
				allPorts.append(","+port.trim());
			}
		}
	}

	private void generateaAllInterfacesString(final StringBuffer ifstr,
			final List<String> interfacesList, final NetworkSettings s, final String corpInterfaceName, final String buildInterfaceName) {
		if(interfacesList.contains(s.getName())){
			syslog.error("***ERROR OCCURED*** There is duplicate entry of interface "+s.getName()+" in the network_settings table. Please contact admin, *********************");
			return;
		}
		interfacesList.add(s.getName());
		final boolean isCorporate = s != null && !StringUtils.isEmpty(corpInterfaceName) && s.getName().equals(corpInterfaceName);
		final boolean isBuilding = s != null && !StringUtils.isEmpty(buildInterfaceName) && s.getName().equals(buildInterfaceName);
		if (s.isEnablePort()){
			ifstr.append("\\nauto "+s.getName());
		
			if (s.isIs_interface_dhcp()){
				ifstr.append("\\niface "+s.getName()+" inet dhcp");
			}else{
				ifstr.append(" \\niface "+s.getName()+" inet static");
				ifstr.append("\\n	address "+s.getIpaddress()+"");
				if(!StringUtils.isEmpty(s.getSubnet_mask())){
					ifstr.append("\\n	netmask "+s.getSubnet_mask()+"");
				}
				if(!StringUtils.isEmpty(s.getDefault_gateway())){
					//ifstr.append("\\n	gateway "+s.getDefault_gateway()+"");
					if(isCorporate){
						ifstr.append("\\n	up route add default gw "+s.getDefault_gateway()+" metric 100");
					}else if(isBuilding){
						ifstr.append("\\n	up route add default gw "+s.getDefault_gateway()+" metric 200");
					}
				}
				if(!StringUtils.isEmpty(s.getDns())){
					ifstr.append("\\n\\ndns-nameservers "+s.getDns()+"");
				}
			}
		}
	}

	private void updateNetworkDetails(
			List<NetworkInterfaceMapping> networkInterfaceMappings,
			List<NetworkSettings> nsList) {
		NetworkSettings ns= null;
		NetworkTypes nt = null;
		for(NetworkInterfaceMapping nim : networkInterfaceMappings){			
			ns = loadNetworkSettingsById(nim.getNetworkSettingsId());
			nt = loadNetworkTypeById(nim.getNetworkTypeId());				
			if(nim.getNetworkSettingsId() != null && nim.getNetworkSettingsId()!=0){					
				if(nim.getId() == null || nim.getId()==0){					
					nim.setId(null);
					nim.setNetworkSettings(ns);
					nim.setNetworkTypes(nt);
					saveNetworkInterfaceMappings(nim);
				}else{
					nim.setNetworkSettings(ns);
					nim.setNetworkTypes(nt);
					saveNetworkInterfaceMappings(nim);
				}				
			}else{					
				nim.setNetworkSettings(ns);
				nim.setNetworkTypes(nt);
				saveNetworkInterfaceMappings(nim);
			}
		}
		
		for(NetworkSettings ns1 : nsList){
			syslog.info("===ns ip address is "+ns1.getIpaddress()+"  and type is "+ns1.getConfigureIPV4()+"  name is "+ns1.getName());			
			saveNetworkSettings(ns1);			
		}
		disableUnmappedPorts();
	}

	public void enableDisableDHCPServer() {
		
		//networkSettingsDao.enableDisableDHCPServer();
		
	}

	public void disableUnmappedPorts() {
		networkSettingsDao.disableUnmappedPorts();
		
	}

	public String loadCurrentMappingByNetworkType(String networkType) {
		return networkSettingsDao.loadCurrentMappingByNetworkType(networkType);
	}
	
	public boolean isDhcpServerRunning(){
		
		try {
            Process dhcpProcess = Runtime.getRuntime().exec("sudo /etc/init.d/dhcp3-server status");
            
            //AdminUtil.readStreamOfProcess(dhcpProcess);
            
            if(dhcpProcess.waitFor() == 0){
            	if (dhcpProcess.exitValue() != 0) return false;
            	BufferedReader outReader = new BufferedReader(new InputStreamReader(dhcpProcess.getInputStream()));
                String dhcpProcessString = null;
                while ((dhcpProcessString = outReader.readLine()) != null) {
                	if(dhcpProcessString.contains("dhcpd3 is running")){
                		syslog.info("dhcpd3 is running");
                		return true;
                	}
                	if(dhcpProcessString.contains("dhcpd3 is not running")){
                		syslog.info("dhcpd3 is not running");
                		return false;
                	}
                }
                //AdminUtil.readStreamOfProcess(dhcpProcess);
           }
        } catch (Exception e) {
        	syslog.error("dhcpd3 process could not be found " + e.getMessage());
        }
        return false;
	}
	
	public String getApplySettingsStatus(){
		SystemConfiguration sc = systemConfigurationManager.loadConfigByName("apply.network.status");
		String status = "";
		if(sc != null){
			status = sc.getValue();
		}
		return status;
	}

	

}
