package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.GatewayDao;
import com.ems.model.Building;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.GwStats;
import com.ems.model.InventoryDevice;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.GatewayImpl;
import com.ems.server.ssl.SSLSessionManager;
import com.ems.server.util.ServerUtil;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("gatewayManager")
@Transactional(propagation = Propagation.REQUIRED)
public class GatewayManager {

  private static final Logger logger = Logger.getLogger(GatewayManager.class.getName());
  
    @Resource
    private GatewayDao gatewayDao;

    @Resource
    private FloorManager floorManager;

    @Resource
    private BuildingManager buildingManager;

    @Resource
    private InventoryDeviceManager inventoryDeviceManager;
    
    @Resource(name = "cacheManager")
	EhCacheCacheManager ehCacheCacheManager;
    
    
    public void setLastConnectivity(Long id) {
    	Ehcache cache = (Ehcache)ehCacheCacheManager.getCache("gateway_id").getNativeCache();
    	if(cache != null && cache.getKeys().size() > 0) {
    		Element element = cache.get(id);
    		if(element != null && element.getObjectValue() != null) {
    			((Gateway) element.getObjectValue()).setLastConnectivityAt(new Date());
    			((Gateway) element.getObjectValue()).setLastStatsRcvdTime(new Date());
    		}
    	}
    }

    /**
     * save gatway details in database
     * 
     * @param gateway
     */
    @CacheEvict(value="gateway_id", key="#gateway.id")
    public void save(Gateway gateway) {
        gatewayDao.saveObject(gateway);
    }

    /**
     * update gatway details in database
     * 
     * @param gateway
     */
    @CacheEvict(value="gateway_id", key="#gateway.id")
    public void update(Gateway gateway) {
        gatewayDao.saveObject(gateway);
        GatewayImpl.getInstance().changeWirelessParams(gateway);
    }

    /**
     * update gatway parameters details in database
     * 
     * @param gateway
     */
    @CacheEvict(value="gateway_id", key="#gateway.id")
    public void updateGatewayParameters(Gateway gateway) {
      LockObj lock = new LockObj();
      lockHashMap.put(gateway.getId(), lock);
      GatewayImpl.getInstance().changeWirelessParams(gateway);        
      synchronized (lock) {
          try {
              lock.wait(3000); // wait for 3 sec
          } catch (Exception e) {
              logger.debug(e.getMessage());
          }
      }
      // commit the changes in the database only if get the ack
      if (lock.gotAck) {
          logger.debug("received the wireless change ack");
          // got the ack for change wireless parameters message
          try {
        	  GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
              Gateway gw = gwMgr.loadGateway(gateway.getId());
              gw.setGatewayName(gateway.getGatewayName());
              gw.setWirelessNetworkId(gateway.getWirelessNetworkId());
              gw.setWirelessEncryptType(gateway.getWirelessEncryptType());
              gw.setWirelessEncryptKey(gateway.getWirelessEncryptKey());
              gw.setEthSecEncryptType(gateway.getEthSecEncryptType());
              gw.setEthSecKey(gateway.getEthSecKey());
              gw.setChannel(gateway.getChannel());
            gatewayDao.updateGatewayParameters(gw);
              // return "success";
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
    }

    /**
     * delete gatway details in database only if no of sensors associated with it are 0
     * 
     * @param gateway
     */
    @CacheEvict(value="gateway_id", key="#id")
    public int deleteGateway(Long id) {
        int iStatus = 0;
        Gateway gateway = null;
        try {
        	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
            gateway = gwMgr.loadGateway(id);
        } catch (ObjectRetrievalFailureException orfe) {
            orfe.printStackTrace();
        }
        if (gateway != null) {
            // Remove prior instance of this device from inventory device if any
        	inventoryDeviceManager.deleteInventoryDeviceByMacAddress(gateway.getMacAddress());

            if(gateway.isCommissioned()) {
            LockObj lock = new LockObj();
            lockHashMap.put(gateway.getId(), lock);
            GatewayImpl.getInstance().setWirelessFactoryDefaults(gateway);         
            synchronized (lock) {
              try {
        	lock.wait(3000); // wait for 3 sec
              } catch (Exception e) {
        	logger.debug(e.getMessage());
              }
            }         
          }
          GatewayImpl.getInstance().rebootGateway(gateway);         
          gatewayDao.removeObject(Gateway.class, id);
          //remove the gateway from the cache
          ServerMain.getInstance().deleteGateway(id);
          iStatus = 1;
        }
        return iStatus;
    }

    /**
     * Return gatways details if id given
     * 
     * @param id
     * @return com.ems.model.Gateway
     */
    @Cacheable(value="gateway_id", key="#id")
    public Gateway loadGateway(Long id) {
        Gateway gateway = null;
        try {
            gateway = (Gateway) gatewayDao.loadGateway(id);
        } catch (Exception e) {
            logger.error(id + ": gateway does not exists!");
        }        
        return gateway;
    }
    
    @CacheEvict(value="gateway_id", key="#id")
    public void cacheEvict(Long id) {
    }
    
    
    /**
     * Return gatways details if id given
     * 
     * @param id
     * @return com.ems.model.Gateway
     */
    public Gateway loadGatewayWithActiveSensors(Long id) {
        Gateway gateway = null;
        try {
        	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
            gateway = gwMgr.loadGateway(id);
            if(gateway != null && gateway.getId() != null) {
            	List<Gateway> list = new ArrayList<Gateway>();
            	list.add(gateway);
            	getActiveGatewaySensors(list);
            }
        } catch (ObjectRetrievalFailureException orfe) {          
            orfe.printStackTrace();
        }       
        return gateway;
    }

    /**
     * Return gatways details if sid given
     * 
     * @param sid
     * @return com.ems.model.Gateway
     */
    public Gateway loadGatewayByUid(String uid) {
        return gatewayDao.loadGatewayByUid(uid);
    }

    /**
     * Return gatways details if Mac address given
     * 
     * @param macAddress
     * @return com.ems.model.Gateway
     */
    public Gateway loadGatewayByMacAddress(String macAddress) {
        return gatewayDao.loadGatewayByMacAddress(macAddress);
    }

    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadCampusGateways(Long campusId) {
        return gatewayDao.loadCampusGateways(campusId);
    }
    
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadCampusGatewaysWithActiveSensors(Long campusId) {
        return gatewayDao.loadCampusGatewaysWithActiveSensors(campusId);
    }

    /**
     * Return commissioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadCommissionedCampusGateways(Long campusId) {
        return gatewayDao.loadCommisionedCampusGateways(campusId) ;
    }
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadBuildingGateways(Long buildingId) {
        return gatewayDao.loadBuildingGateways(buildingId);
    }
    
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadBuildingGatewaysWithActiveSensors(Long buildingId) {
        return gatewayDao.loadBuildingGatewaysWithActiveSensors(buildingId);
    }
    /**
     * Return  commissioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadCommissionedBuildingGateways(Long buildingId) {
        return gatewayDao.loadCommissionedBuildingGateways(buildingId);
    }
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadFloorGateways(Long floorId) {
        return gatewayDao.loadFloorGateways(floorId);
    }
    
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadFloorGatewaysWithActiveSensors(Long floorId) {
        return gatewayDao.loadFloorGatewaysWithActiveSensors(floorId);
    }
    /**
     * Return  commissioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    public List<Gateway> loadCommissionedFloorGateways(Long floorId) {
        return gatewayDao.loadCommissionedFloorGateways(floorId);
    }

    public List<Gateway> loadAllGateways() {
        return gatewayDao.loadAllGateways();
    }
    
    public List<Gateway> loadAllGatewaysWithActiveSensors() {
        return gatewayDao.loadAllGatewaysWithActiveSensors();
    }
    public List<Gateway> loadAllCommissionedGateways() {
        return gatewayDao.loadAllCommissionedGateways();
    }
    public List<Gateway> loadUnCommissionedGateways() {
        return gatewayDao.loadUnCommissionedGateways();
    }
    
    public List<Gateway> loadCommissionedGateways() {
      return gatewayDao.loadCommissionedGateways();
    }
    
   
    public Gateway getGatewayByIp(String ip) {
        return gatewayDao.getGatewayByIp(ip);
    }
    
    public Gateway getUEMGateway() {
        return gatewayDao.getUEMGateway();
    }


    /**
     * Called internally, especially when the user hits commission button and would like to commission this
     * InventoryDevice (GW). Function will add the gateway to gateway table and will delete it from the inventory table.
     * 
     * @param gateway
     */
    @CacheEvict(value="gateway_id", allEntries = true)
    private Gateway AddGateway(Gateway gateway) {

        Gateway gw = null;
        try {
            gw = loadGatewayByMacAddress(gateway.getMacAddress());
            String sMacAddrr = gateway.getMacAddress();
            if (gw == null) {
                // gateway.setMacAddress(sMacAddrr.replace(":", ""));
                // Assumption is that the IP address of the gateway never changes...
                save(gateway);
            } else {
                // Its rare but this could happen. Gateway's ip address could be changed
                gw.setIpAddress(gateway.getIpAddress());
                save(gw);
            }
            // Added this gateway to the GatewayInfo map
            GatewayImpl.getInstance().addGatewayInfo(gateway.getIpAddress());
            // Delete this from the inventory list...
            inventoryDeviceManager.deleteInventoryDeviceByMacAddress(sMacAddrr);
        } catch (HibernateException he) {
            he.printStackTrace();
        }
        return gw;
    }

    class LockObj {

      boolean gotAck = false;

    } // end of class LockObj

    static HashMap<Long, LockObj> lockHashMap = new HashMap<Long, LockObj>();

    public static void receivedGwWirelessChangeAck(long gwId) {

      LockObj lock = lockHashMap.get(gwId);
      if(lock == null) {
	return;
      }
      lock.gotAck = true;
      try {
    	synchronized (lock) {
    	  lock.notify();
    	}
      } catch (Exception e) {
    	logger.debug(e.getMessage());
      }

    } // end of method receivedGwWirelessChangeAck

    /**
     * The Gateway should already be present in the gateway table.
     * Gateway object passed is half baked, we need to fetch the actual gateway object from the database
     * and set the incoming parameters to it.
     */
    @CacheEvict(value="gateway_id", key="#gateway.id")
    public boolean commission(Gateway gateway) {
        
        Gateway gw = loadGatewayByMacAddress(gateway.getMacAddress());
        if (gw == null) {
          // gateway could not be found in the database
          return false;
        }
        
        LockObj lock = new LockObj();
        lockHashMap.put(gateway.getId(), lock);
        GatewayImpl.getInstance().changeWirelessParams(gateway);        
        synchronized (lock) {
            try {
                lock.wait(3000); // wait for 3 sec
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        if (lock.gotAck) {
            // got the ack for commissioning message
        	gw.setCommissioned(true);
            gatewayDao.updateGw(gw);
            //start the timers for this gateway
            ServerMain.getInstance().getGatewayInfo(gateway.getId()).startTimers();
        } else {
          // we didn't get the ack so don't commit the changes in the database
          return false;
        }
        ServerMain.getInstance().updateGatewayInfo(gateway.getId());
      
            gw.setGatewayName(gateway.getGatewayName());
            gw.setMacAddress(gateway.getMacAddress());
            gw.setIpAddress(gateway.getIpAddress());
            gw.setWirelessRadiorate(gateway.getWirelessRadiorate());
            gw.setChannel(gateway.getChannel());
            gw.setWirelessNetworkId(gateway.getWirelessNetworkId());
            gw.setWirelessEncryptType(gateway.getWirelessEncryptType());
            gw.setWirelessEncryptKey(gateway.getWirelessEncryptKey());
            if (gw.getFloor().getId().compareTo(gateway.getFloor().getId()) != 0) {
                // Commissioning gateway on to different floor...
                Floor floor = null;
				try {
					floor = floorManager.getFloorById(gateway.getFloor().getId());
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
                gw.setFloor(floor);
                //gateway.setFloor(floor);
                long buildingId = floor.getBuilding().getId();
                gw.setBuildingId(buildingId);
                Building building = buildingManager.getBuildingById(buildingId);
                gw.setCampusId(building.getCampus().getId());
                // Update Gateway location.
                String location = "";
                try {
                    location = floor.getName();
                    location = building.getName() + " -> " + location;
                    location = building.getCampus().getName() + " -> " + location;
                    gw.setLocation(location);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                gatewayDao.updateGateway(gw);
            } else {
                // Commissioning gateway on to same floor...
              gatewayDao.updateGw(gw);
            }            
            return true;
    }

    public boolean isCommissioned(long gatewayId) {
    	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        Gateway gw = gwMgr.loadGateway(gatewayId);
        return gw.isCommissioned();
    }
    
    public boolean performAfterCommissionSteps(Gateway gateway) {
        Gateway gw = loadGatewayByMacAddress(gateway.getMacAddress());
        if (gw == null) {
          // gateway could not be found in the database
          return false;
        }
        //send the security command
        GatewayImpl.getInstance().sendGatewaySecurityCommand(gw);
        //send the gateway info packet so that version is retrieved
        GatewayImpl.getInstance().sendGatewayInfoReq(gw);
        return true;
    }
    
    public List<Gateway> getActiveGatewaySensors(List<Gateway> list) {
    	return gatewayDao.getActiveGatewaySensors(list);
    }

    @CacheEvict(value="gateway_id", key="#gateway.id")
    public void updateGatewayPosition(Gateway gateway) {
    	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        Gateway gw = gwMgr.loadGateway(gateway.getId());
        gw.setXaxis(gateway.getXaxis());
        gw.setYaxis(gateway.getYaxis());
        gatewayDao.updateGw(gw);
    }

    public void getRealtimeStats(String gwIp) {
        Gateway oGW = getGatewayByIp(gwIp);
        if (oGW != null)
            GatewayImpl.getInstance().sendGatewayInfoReq(oGW);
    }

    public void getRealtimeStatsByGWId(Long id) {
    	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        Gateway oGW = gwMgr.loadGateway(id);
        if (oGW != null)
            GatewayImpl.getInstance().sendGatewayInfoReq(oGW);
    }

    
    @CacheEvict(value="gateway_id", key="#gw.id")
    public void saveGatewayInfo(Gateway gw) {
        gatewayDao.saveGatewayInfo(gw);
    }

    private Gateway getNewGateway(long floorID)
    {
        Gateway oGateway = new Gateway();
        Floor floor = null;
		try {
			floor = floorManager.getFloorById(floorID);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        oGateway.setFloor(floor);
        long buildingId = floor.getBuilding().getId();
        oGateway.setBuildingId(buildingId);
        Building building = buildingManager.getBuildingById(buildingId);
        oGateway.setCampusId(building.getCampus().getId());
        oGateway.setFloorName(floor.getName());
        // Set Gateway location.
        String location = "";
        try {
            location = floor.getName();
            location = building.getName() + " -> " + location;
            location = building.getCampus().getName() + " -> " + location;
            oGateway.setLocation(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // sMacAddrr = sMacAddrr.replace(":", "");
        oGateway.setEthSecType(ServerConstants.ETH_SEC_TYPE);
        oGateway.setEthSecEncryptType(ServerConstants.ETH_SEC_ENCRYPT_TYPE);
        oGateway.setEthSecKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
        oGateway.setChannel(ServerConstants.WIRELESS_DEF_CHANNEL);
        oGateway.setWirelessEncryptType(ServerConstants.ETH_SEC_TYPE);
        oGateway.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
        oGateway.setWirelessRadiorate(ServerConstants.WIRELESS_DEF_RADIO_RATE);
        oGateway.setNoOfSensors(0);
        oGateway.setEthIpaddrType(0);
        oGateway.setGatewayType((short) ServerConstants.ZIGBEE_GW_SU);
        oGateway.setPort((short) ServerConstants.GW_UDP_PORT);
        oGateway.setXaxis(0);
        oGateway.setYaxis(0);
        oGateway.setLastConnectivityAt(new Date());
        oGateway.setLastStatsRcvdTime(new Date());
        oGateway.setCurrUptime(0l);
        oGateway.setWirelessNetworkId(ServerConstants.DEFAUL_NETWORK_ID);
        oGateway.setCurrNoPktsFromGems(0l);
        oGateway.setCurrNoPktsFromNodes(0l);
        oGateway.setCurrNoPktsToGems(0l);
        oGateway.setCurrNoPktsToNodes(0l);
        
        return oGateway;
    }
    
    /**
     * Fetches the list from the inventory table and adds these to the gateway table with the appropriate floor id.
     * 
     * @param floorID
     * @return com.ems.model.Gateway collection
     */
    public List<Gateway> discoverGateways(long floorID) {
        List<InventoryDevice> oGWInInventoryList = inventoryDeviceManager
                .loadAllInventoryDeviceByType(ServerConstants.DEVICE_GATEWAY);
        List<Gateway> oGatewayList = new ArrayList<Gateway>();
        if (oGWInInventoryList != null) {
            Iterator<InventoryDevice> oGWInventoryItr = oGWInInventoryList.iterator();
            InventoryDevice oGWIDevice = null;
            Gateway oGateway = null;
            while (oGWInventoryItr.hasNext()) {
                oGWIDevice = oGWInventoryItr.next();
                oGateway = getNewGateway(floorID);
                oGateway.setGatewayName("GW" + ServerUtil.generateName(oGWIDevice.getSnapAddr()));
                oGateway.setSnapAddress(oGWIDevice.getSnapAddr());
                oGateway.setMacAddress(oGWIDevice.getMacAddr());
                oGateway.setIpAddress(oGWIDevice.getIpAddress());

                AddGateway(oGateway);
                oGatewayList.add(oGateway);
            }
        }
        return oGatewayList;
    }
    
    public int getGatewayCountByName(String name)
    {
    	int count= gatewayDao.loadCommisionedGateways(name);
    	return count;
    }

    @CacheEvict(value="gateway_id", key="#gw.id")
    public Gateway updateFields(Gateway gw) {
        return gatewayDao.updateFields(gw);
    }

    public void exitCommissioning(List<Long> gatewayIdList) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("Exiting gateway commissioning process...");
    	}
        return;
    }

    @CacheEvict(value="gateway_id", key="#gwId")
    public void setImageUpgradeStatus(long gwId, String status) {
    	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        Gateway gateway = gwMgr.loadGateway(gwId);
        gateway.setUpgradeStatus(status);
        gatewayDao.updateGw(gateway);
    } // end of method setImageUpgradeState

    @CacheEvict(value="gateway_id", key="#gw.id")
    public void setVersions(Gateway gw) {
        gatewayDao.setVersions(gw);
    } // end of method setVersions

	// Convert the MAC address into lower case and no padding format
    private String convertMAC(String strMACAddr)
    {
    	strMACAddr = strMACAddr.toLowerCase();
    	
        StringTokenizer st = new StringTokenizer(strMACAddr, ":");
        String strNewAddr = new String();
        String token = null;
        while(st.hasMoreTokens()) {
          token = st.nextToken();
          
          if(token.charAt(0) == '0')
        	  strNewAddr += token.charAt(1);
          else
        	  strNewAddr += token;

          if(st.hasMoreTokens())
        	  strNewAddr += ":";
        }
        
        return strNewAddr;
     }
    
    @CacheEvict(value="gateway_id", allEntries = true)
    public int addGateway(String strMACAddr, String strIPAddr, String strFloorId)
    {
    	Long floorId = Long.parseLong(strFloorId);
    	try {
    	
    		// Convert the MAC address into lower case and no padding format
    		String strNewMACAddr = convertMAC(strMACAddr);
    		
	    	// Check if the gateway with the same mac address already exists
            Gateway tmpGw = loadGatewayByMacAddress(strNewMACAddr);

            if(tmpGw != null)
            	return 2;
            
            tmpGw = getGatewayByIp(strIPAddr);
            
            if(tmpGw != null)
            	return 3;
	    	
	    	Gateway gateway = getNewGateway(floorId);
	    	
	    	String strSnapAddr = ServerUtil.getSNAPFromMac(strMACAddr);
	        gateway.setGatewayName("GW" + ServerUtil.generateName(strSnapAddr));
	        gateway.setSnapAddress(strSnapAddr);
	        gateway.setMacAddress(strNewMACAddr);
	        gateway.setIpAddress(strIPAddr);
	        gateway.setVersion("2.0");
	        
	        if(SSLSessionManager.getInstance().authEnlightedGateway(gateway) == false)
	        	return 1;
	        
	        // Save the gateway to the database so that it gets the id to send the gateway info command
	        save(gateway);
	        
	        // Added this gateway to the GatewayInfo map
	        GatewayImpl.getInstance().addGatewayInfo(gateway.getIpAddress());
	        
	        return 0;
    	}
    	catch(SSLException sslE)
    	{
    		sslE.printStackTrace();
    		return 1;
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		
    		return 1;
    	}
    }
    
    @CacheEvict(value="gateway_id", key="#currGwStats.gwId")
    public void updateCurrentGwStats(GwStats currGwStats, Gateway gwVer) {
    	GatewayManager gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        Gateway gw = gwMgr.loadGateway(currGwStats.getGwId());
    	gw.setCurrUptime(currGwStats.getUptime());
        gw.setCurrNoPktsFromGems(currGwStats.getNoPktsFromGems());
        gw.setCurrNoPktsFromNodes(currGwStats.getNoPktsFromNodes());
        gw.setCurrNoPktsToGems(currGwStats.getNoPktsToGems());
        gw.setCurrNoPktsToNodes(currGwStats.getNoPktsToNodes());
        gw.setLastConnectivityAt(new Date());
        gw.setLastStatsRcvdTime(new Date());
        gw.setVersion(gwVer.getVersion());
        gw.setApp1Version(gwVer.getApp1Version());
        gw.setBootLoaderVersion(gwVer.getBootLoaderVersion());
    	gatewayDao.updateGw(gw);
    }
}
