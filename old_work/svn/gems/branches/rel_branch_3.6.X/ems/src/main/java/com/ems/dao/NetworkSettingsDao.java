package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.NetworkSettings;
import com.ems.model.NetworkTypes;
import com.ems.model.SystemConfiguration;
import com.ems.types.NetworkType;

@Repository("networkSettingsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class NetworkSettingsDao extends BaseDaoHibernate {

	static final Logger logger = Logger.getLogger(NetworkSettingsDao.class
			.getName());

	public void saveNetworkSettings(NetworkSettings networkSettings) {
			Session session = getSession();
			final NetworkInterfaceMapping netbuilding = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
			NetworkSettings dbObject = loadNetworkSettingsByInterfaceName(networkSettings.getName());
			if(dbObject != null){
				dbObject.setConfigureIPV4(networkSettings.getConfigureIPV4());
				dbObject.setIpaddress(networkSettings.getIpaddress());
				dbObject.setDefault_gateway(networkSettings.getDefault_gateway());
				dbObject.setDns(networkSettings.getDns());
				dbObject.setSearch_domain_fields(networkSettings.getSearch_domain_fields());
				dbObject.setSubnet_mask(networkSettings.getSubnet_mask());		
				dbObject.setEnablePort(networkSettings.isEnablePort());
				session.saveOrUpdate(dbObject);				
					if(netbuilding != null){
						if(dbObject.getName().equalsIgnoreCase(netbuilding.getNetworkSettings().getName())){
							enableDisableDHCPServer(new Boolean(networkSettings.isIs_dhcp_server()).toString());
						}
					}else{
						enableDisableDHCPServer("false");
					}				
			}else{
				session.saveOrUpdate(networkSettings);				
					if(netbuilding != null){
						if(networkSettings.getName().equalsIgnoreCase(netbuilding.getNetworkSettings().getName())){
							enableDisableDHCPServer(new Boolean(networkSettings.isIs_dhcp_server()).toString());
						}
					}else{
						enableDisableDHCPServer("false");
					}			
			}			
	}
	
	public void enableDisableDHCPServer(String enabledDisableDHCPServer){
		
		Session session = getSession();		
		Query q = session.createQuery("from SystemConfiguration sc where sc.name=?");
		q.setParameter(0, "dhcp.enable");
		List list = q.list();
		if(list!= null && list.size()>0){
			SystemConfiguration sc = (SystemConfiguration) list.get(0);
			sc.setValue(enabledDisableDHCPServer);			
			session.saveOrUpdate(sc);
		}
	}

	public void saveInitialNetworkSettings(
			List<NetworkSettings> networkSettingsList) {
		Session session = getSession();
		for (NetworkSettings ns : networkSettingsList) {	
			//ns.setEnablePort(true);
			boolean isDHCP = ns.isIs_dhcp_server();
			logger.info("physical status is for "+ns.getName()+" is "+ns.getPhysical_state());
			if(!"0".equalsIgnoreCase(ns.getPhysical_state())){
				ns.setConnected_status("up");
			}else{
				ns.setConnected_status("down");
			}
			if(isDHCP){
				ns.setConfigureIPV4("DHCP");
			}else{
				ns.setConfigureIPV4("Static");
			}
			session.saveOrUpdate(ns);
		}
	}

	public List<NetworkSettings> loadAllNetworkInterfacesSettings() {				
		return getSession().createCriteria(NetworkSettings.class)
				.addOrder(Order.asc("id")).list();
	}

	public List<NetworkTypes> loadAllNetworkTypes() {
		List<NetworkTypes> networkTypes = new ArrayList<NetworkTypes>();
		Session session = getSession();
		networkTypes = session.createCriteria(NetworkTypes.class)
				.addOrder(Order.asc("id")).list();
		return networkTypes;
	}

	public List loadCustomFieldsFromNetworkType(String networkType) {
		String sql = "select interface_name, port_enabled from network_settings ns,network_interface_mapping nim, network_types nt where ns.id=nim.network_settings_id and nt.id=nim.network_type_id and nt.name='"
				+ networkType + "'";
		Session session = getSession();
		Query q = session.createSQLQuery(sql);
		List l = q.list();
		if (l != null && l.size() > 0) {
			return l;
		}
		return null;
	}
	public List<NetworkInterfaceMapping> loadCurrentNetworkInterfaceMapping() {

		List<NetworkInterfaceMapping> nsList = null;
		Session session = getSession();
		nsList = session.createCriteria(NetworkInterfaceMapping.class)
				.addOrder(Order.asc("networkTypes")).list();
		return nsList;

	}

	public NetworkInterfaceMapping loadCurrentNetworkInterfaceMappingByNetworkTypeId(Long networkTypeId) {

		List<NetworkInterfaceMapping> nsList = null;
		Session session = getSession();
		Query q = session.createQuery("from NetworkInterfaceMapping nim where nim.networkTypes.id=?");
		q.setParameter(0, networkTypeId);
		nsList = q.list();
		if(nsList!= null && nsList.size()>0){
			return nsList.get(0);
		}
		return null;

	}
	
	public NetworkTypes loadNetworkTypeByNetworkTypeId(Long networkTypeId) {

		List<NetworkTypes> nsList = null;
		Session session = getSession();
		Query q = session.createQuery("from NetworkTypes nt where id=?");
		q.setParameter(0, networkTypeId);
		nsList = q.list();
		if(nsList!= null && nsList.size()>0){
			return nsList.get(0);
		}
		return null;

	}
	
	public NetworkInterfaceMapping loadCurrentNetworkInterfaceMappingByNetworkType(String networkType) {
		//logger.info("inside network dao");
		
		List<NetworkInterfaceMapping> nsList = null;
		Session session = getSession();
		Query q = session.createQuery("from NetworkInterfaceMapping nim where nim.networkTypes.name=?");
		q.setParameter(0, networkType);
		nsList = q.list();
		if(nsList!= null && nsList.size()>0){
			//logger.info("ns is "+nsList.get(0).getNetworkSettings().getName()+" for "+networkType);
			return nsList.get(0);
		}
		//logger.info("returning null");
		return null;

	}
	
	public String loadCurrentMappingByNetworkType(String networkType){
		String sql =  "select interface_name from network_settings ns,network_interface_mapping nim, network_types nt where ns.id=nim.network_settings_id and nt.id=nim.network_type_id and nt.name='"+networkType+"'";
		Session session = getSession();
		Query q = session.createSQLQuery(sql);
		List l = q.list();
		if(l!= null && l.size()>0){
			return l.get(0).toString();
		}
		return null;
	}
	
	public boolean isPortEnabledForDHCP(){
		Session session = getSession();
		Query q = session.createSQLQuery("select port_enabled from network_settings ns,network_interface_mapping nim where ns.id=nim.network_settings_id and"
				+ "nim.network_type_id =1");
		List list = q.list();
		if(list != null && list.size()>0){
			return (Boolean)list.get(0);
		}else return false;			
		
	}
	
	public NetworkSettings loadNetworkSettingsByInterfaceName(String interfaceName) {

		List<NetworkSettings> nsList = null;
		Session session = getSession();
		nsList = session.createCriteria(NetworkSettings.class).add(Restrictions.eq("name", interfaceName)).list();
		if(nsList!= null && nsList.size()>0){
			return nsList.get(0);
		}
		return null;

	}
	
	public void saveNetworkInterfaceMappings(NetworkInterfaceMapping nim){
		logger.info(" in dao nim.getId "+nim.getId()+" nim.getns "+nim.getNetworkSettings()+"  NS ID == "+nim.getNetworkSettingsId());
		Session session = getSession();		
		if( nim.getNetworkSettingsId() == 0 && nim.getId()!=0){	
			 Object savedNim = session.get(NetworkInterfaceMapping.class, nim.getId());
			 System.out.println("===== savedNim is "+savedNim);
			 if(savedNim != null) {
				 session.delete(savedNim);
			 }
		}else if((nim.getId()== null ||nim.getId()==0) && nim.getNetworkSettingsId()==0){
			logger.info("=== nothing to map");
		}else{			
			logger.info("in save nim else");			
			session.saveOrUpdate(nim);			
		}
	}
	

	public NetworkSettings loadNetworkSettingsById(Long networkSettingsId) {
		List<NetworkSettings> nsList = null;
		Session session = getSession();
		nsList = session.createCriteria(NetworkSettings.class).add(Restrictions.eq("id", networkSettingsId)).list();
		if(nsList!= null && nsList.size()>0){
			return nsList.get(0);
		}
		return null;
		
	}

	public NetworkTypes loadNetworkTypeById(Long networkTypeId) {
		List<NetworkTypes> ntList = null;
		Session session = getSession();
		ntList = session.createCriteria(NetworkTypes.class).add(Restrictions.eq("id", networkTypeId)).list();
		if(ntList!= null && ntList.size()>0){
			return ntList.get(0);
		}
		return null;		
	}
	
	public boolean isDHCPEnabled(){
		Query q = getSession().createQuery("from SystemConfiguration sc where sc.name=?");
		q.setParameter(0, "dhcp.enable");
		List list = q.list();
		if(list!= null && list.size()>0){
			SystemConfiguration sc = (SystemConfiguration) list.get(0);			
			return new Boolean(sc.getValue());
		}
		return false;
	}

	public void saveInitialNetworkSettingsMappings(
			List<NetworkSettings> networkSettingsList,Map<String,String> physicalStatusMap) {
		Session session = getSession();
		NetworkTypes nt = null;
		NetworkSettings ns = null;
		NetworkInterfaceMapping nim = null;
		List<NetworkSettings> nsList = loadAllNetworkInterfacesSettings();
		boolean flag = false;
		//map corporate with eth0 by default
		nt = loadNetworkTypeByName(NetworkType.Corporate.getName());
		ns = loadNetworkSettingsByInterfaceName("eth0");
		if(ns!= null){
			nim = new NetworkInterfaceMapping();
		//	ns.setConfigureIPV4("Static");
			ns.setEnablePort(true);
			session.saveOrUpdate(ns);
			nim.setNetworkSettings(ns);
			nim.setNetworkTypes(nt);			
			session.saveOrUpdate(nim);			
		}else if(nsList!= null && nsList.size()>0){
			nim = new NetworkInterfaceMapping();
			ns = nsList.get(0);
			ns.setEnablePort(true);
			session.saveOrUpdate(ns);
			nim.setNetworkSettings(ns);
			nim.setNetworkTypes(nt);
			session.saveOrUpdate(nim);		
			flag = true;
		}
		NetworkSettings ns1 = null;		
		if(networkSettingsList != null && networkSettingsList.size() > 1 ){
			ns1 = networkSettingsList.get(1);
		}
		boolean isDHCPEnabled = isDHCPEnabled();
		// map building	to eth1 by default
		nt = loadNetworkTypeByName(NetworkType.Building.getName());
		ns = loadNetworkSettingsByInterfaceName("eth1");
		if(ns!= null){
			if(isDHCPEnabled){
				nim = new NetworkInterfaceMapping();
				ns.setConfigureIPV4("Static");
				ns.setEnablePort(true);
				session.saveOrUpdate(ns);			
				nim.setNetworkSettings(ns);
				nim.setNetworkTypes(nt);
				session.saveOrUpdate(nim);								
			}						
		}else if(flag){
			if(nsList.size()>1){				
				ns = nsList.get(1);
				if(isDHCPEnabled){					
						nim = new NetworkInterfaceMapping();
						ns.setConfigureIPV4("Static");				
						ns.setEnablePort(true);				
						session.saveOrUpdate(ns);
						nim.setNetworkSettings(ns);
						nim.setNetworkTypes(nt);
						session.saveOrUpdate(nim);
				}
			}
		}					
	}
	
	
	public void remapNetworkSettings(
			List<NetworkSettings> networkSettingsList,Map<String,String> currentMappingMap,Map<String,String> physicalStatusMap) {
		Session session = getSession();
		NetworkInterfaceMapping nim = null;
		NetworkSettings ns = null;
		NetworkTypes nt = null;
		List<NetworkSettings> nsList = networkSettingsList;//loadAllNetworkInterfacesSettings();
		
		if(currentMappingMap.containsKey(NetworkType.Corporate.getName())){
			String nimCorporate = loadCurrentMappingByNetworkType(NetworkType.Corporate.getName());
			if(nimCorporate == null){
				nt = loadNetworkTypeByName(NetworkType.Corporate.getName());
				nim = new NetworkInterfaceMapping();
				if(nsList.size()>0){
					ns = nsList.get(0);
					ns = loadNetworkSettingsByInterfaceName(ns.getName());
					ns.setEnablePort(true);
					session.saveOrUpdate(ns);
					nim.setNetworkSettings(ns);
					nim.setNetworkTypes(nt);
					session.saveOrUpdate(nim);
				}				
			}
		}
		if(currentMappingMap.containsKey(NetworkType.Building.getName())){			
			boolean isDHCPEnabled = isDHCPEnabled();
			String nimBuilding = loadCurrentMappingByNetworkType(NetworkType.Building.getName());			
			if(nimBuilding == null){
				nt = loadNetworkTypeByName(NetworkType.Building.getName());				
				nim = new NetworkInterfaceMapping();
				if(nsList.size()>1){					
					ns = nsList.get(1);		
					ns = loadNetworkSettingsByInterfaceName(ns.getName());	
					if(!isDHCPEnabled){
						if(ns != null && !"0".equalsIgnoreCase(physicalStatusMap.get(ns.getName()))){
							nim = new NetworkInterfaceMapping();
							ns.setConfigureIPV4("Static");								
							ns.setEnablePort(true);				
							session.saveOrUpdate(ns);
							nim.setNetworkSettings(ns);
							nim.setNetworkTypes(nt);
							session.saveOrUpdate(nim);					
						}
					}else{					
							nim = new NetworkInterfaceMapping();
							ns.setConfigureIPV4("Static");	
							ns.setIpaddress("169.254.0.1");
							ns.setSubnet_mask("255.255.0.0");
							ns.setEnablePort(true);				
							session.saveOrUpdate(ns);
							nim.setNetworkSettings(ns);
							nim.setNetworkTypes(nt);
							session.saveOrUpdate(nim);	
					}			
				}
			}
		}
		
		if(currentMappingMap.containsKey(NetworkType.BACnet.getName())){
			String nimBacnet = loadCurrentMappingByNetworkType(NetworkType.BACnet.getName());			
			if(nimBacnet == null){
				nt = loadNetworkTypeByName(NetworkType.BACnet.getName());				
				nim = new NetworkInterfaceMapping();
				if(nsList.size()>0){
					ns = nsList.get(0);				
					ns = loadNetworkSettingsByInterfaceName(ns.getName());	
					nim.setNetworkSettings(ns);
					nim.setNetworkTypes(nt);
					session.saveOrUpdate(nim);					
				}				
			}
		}
	}

	public NetworkTypes loadNetworkTypeByName(String networkTypeName) {
		List<NetworkTypes> nsList = null;
		Session session = getSession();
		Query q = session.createQuery("from NetworkTypes nt where name=?");
		q.setParameter(0, networkTypeName);
		nsList = q.list();
		if(nsList!= null && nsList.size()>0){
			return nsList.get(0);
		}
		return null;
	}
	
	public boolean isIntefaceMappedToBuilding(String interfaceName){
		Session session = getSession();		
		Query q = session.createQuery("from NetworkInterfaceMapping nim where nim.networkSettings.name=? and nim.networkTypes.name=?");
		q.setParameter(0, interfaceName);
		q.setParameter(1, NetworkType.Building.getName());
		List list = q.list();
		if(list != null && list.size() > 0){
			logger.info("interface "+interfaceName+" is mapped to building");
			return true;			
		}	
		else return false;
	}

	public void disableUnmappedPorts() {
		Session session = getSession();		
		String sql = "select id from network_settings where id not in(select network_settings_id from network_interface_mapping)";
		Query q = session.createSQLQuery(sql);
		List list = q.list();		
		NetworkSettings ns;
		for(Object o : list){
			ns = loadNetworkSettingsById(((BigInteger)o).longValue());			
			ns.setEnablePort(false);
			session.saveOrUpdate(ns);
		}		
	}

	public void deleteNetworkSettingsAndMapping(NetworkSettings ns) {
		Session session = getSession();
		String sql = "DELETE FROM network_interface_mapping where network_settings_id="+ns.getId();
		Query q = session.createSQLQuery(sql);
		q.executeUpdate();
		sql = "DELETE FROM network_settings where id="+ns.getId();
		q = session.createSQLQuery(sql);
		q.executeUpdate();
	}

	public void mapGatewayNetworkToNA(NetworkSettings ns) {
		Session session = getSession();
		NetworkInterfaceMapping nim = loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if(nim != null){			
			session.delete(nim);
		}
		
	}

	

}
