package com.ems.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
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

	public boolean saveInitialNetworkSettings() {
		List<NetworkSettings> networkSettingsList = new ArrayList<NetworkSettings>();	
		try {
			String interfacesJson = AdminUtil.callNetworkInterfacesScript(); 
			//"{\"interfaces\":[{\"name\":\"p0\",\"macaddress\":\"d4:3d:7e:6d:63:62\",\"ipaddress\":\"192.168.4.133\",\"subnet_mask\":\"255.255.255.0\",\"default_gateway\":\"192.168.4.1\",\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"up\",\"is_dhcp_server\":false},{\"name\":\"p1\",\"macaddress\":\"d4:3d:7e:6d:63:63\",\"ipaddress\":\"169.254.0.1\",\"subnet_mask\":\"255.255.0.0\",\"default_gateway\":null,\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"down\",\"is_dhcp_server\":false},{\"name\":\"wlan0\",\"macaddress\":\"6c:71:d9:4c:d5:2a\",\"ipaddress\":null,\"subnet_mask\":null,\"default_gateway\":null,\"dns\":\"\",\"search_domain_fields\":\"\",\"connected_status\":\"down\",\"is_dhcp_server\":false}]}";			
			logger.info("=========PHP Ouput is:" + interfacesJson + "");	
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
	
	public void applyNetworkSettings(){
		
		final SystemConfiguration config = systemConfigurationManager.loadConfigByName(Constants.IPTABLES_STATIC_PORTS_KEY);
		//Check the ports enabled for email(587) and backnet and ldap (389)
		
		final StringBuffer allPorts = new StringBuffer(config.getValue());
		//get backnet port from backNetManger.getConfig
		String port = bacnetManager.getBacnetConfiguration().getProperty("ListenPort");
		if (!StringUtils.isEmpty(port)){
			allPorts.append(","+port.trim());
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
		logger.info("All ports to be configured for corporate network are:"+ (allPorts.toString()) +":");
		//Check whether corporate network is enabled or not
		final NetworkInterfaceMapping netcorp = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		final boolean isCorpEnabled = netcorp != null && netcorp.getNetworkSettings() != null && netcorp.getNetworkSettings().isEnablePort();
		final NetworkInterfaceMapping netbuilding = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		final boolean isBuildingEnabled = netbuilding != null && netbuilding.getNetworkSettings() != null && netbuilding.getNetworkSettings().isEnablePort();
		final NetworkInterfaceMapping netbacnet = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.BACnet.getName());
		final boolean isBacnetEnabled = netbacnet != null && netbacnet.getNetworkSettings() != null && netbacnet.getNetworkSettings().isEnablePort();
		try {
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
				if(interfacesList.contains(s.getName())){
					logger.error("***ERROR OCCURED*** There is duplicate entry of interface "+s.getName()+" in the network_settings table. Please contact admin, *********************");
					continue;
				}
				interfacesList.add(s.getName());
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
							ifstr.append("\\n	gateway "+s.getDefault_gateway()+"");
						}
						if(!StringUtils.isEmpty(s.getDns())){
							ifstr.append("\\n\\ndns-nameservers "+s.getDns()+"");
						}
					}
				}
			}
			ifstr.append("");
			
			String[] allInOneCmd = {"/bin/bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/applyNetworkSettings.sh", 
					allPorts.toString(),netcorp.getNetworkSettings().getName().trim(),  acceptDropPortStr,
					isDHCPServer?netbuilding.getNetworkSettings().getName().trim():"null",
					ifstr.toString()
					};
			logger.info("allInOneCmd command is "+ Arrays.deepToString(allInOneCmd) );
			Process prAllInOne = Runtime.getRuntime().exec(allInOneCmd);
			
			final String inputStream = AdminUtil.readStreamOfProcess(prAllInOne);
			final int status = prAllInOne.waitFor();
			if(status != 0){
				logger.error("********************************ERROR:: NEtwork settings are not applied. Please contact admin.********************************************");
				logger.error("ERROR IS: "+inputStream);
			}
			SSLSessionManager.getInstance().initNwInterface();
		} catch (Exception e) {
			logger.error("ERROR: Not able to apply network settings to EM. Please contact Administrator ", e);
		}
		
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
                		logger.info("dhcpd3 is running");
                		return true;
                	}
                	if(dhcpProcessString.contains("dhcpd3 is not running")){
                		logger.info("dhcpd3 is not running");
                		return false;
                	}
                }
                //AdminUtil.readStreamOfProcess(dhcpProcess);
           }
        } catch (Exception e) {
            logger.error("dhcpd3 process could not be found " + e.getMessage());
        }
        return false;
	}

	

}
