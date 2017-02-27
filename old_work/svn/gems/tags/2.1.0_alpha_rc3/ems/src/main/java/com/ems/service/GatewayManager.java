package com.ems.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import org.apache.log4j.Logger;

import org.hibernate.HibernateException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BuildingDao;
import com.ems.dao.FloorDao;
import com.ems.dao.GatewayDao;
import com.ems.dao.InventoryDeviceDao;
import com.ems.model.Building;
import com.ems.model.Floor;
import com.ems.model.Gateway;
import com.ems.model.InventoryDevice;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.GatewayImpl;
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
    private FloorDao floorDao;

    @Resource
    private BuildingDao buildingDao;

    @Resource
    private InventoryDeviceDao inventoryDeviceDao;

    /**
     * save gatway details in database
     * 
     * @param gateway
     */
    public void save(Gateway gateway) {
        gatewayDao.saveObject(gateway);
    }

    /**
     * update gatway details in database
     * 
     * @param gateway
     */
    public void update(Gateway gateway) {
        gatewayDao.saveObject(gateway);
        GatewayImpl.getInstance().changeWirelessParams(gateway);
    }

    /**
     * update gatway parameters details in database
     * 
     * @param gateway
     */
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
            gatewayDao.updateGatewayParameters(gateway);
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
    public int deleteGateway(Long id) {
        int iStatus = 0;
        Gateway gateway = null;
        try {
            gateway = (Gateway) gatewayDao.getObject(Gateway.class, id);
        } catch (ObjectRetrievalFailureException orfe) {
            orfe.printStackTrace();
        }
        if (gateway != null) {
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
          if(lock.gotAck) {
          	//ServerUtil.sleepMilli(75);
          	GatewayImpl.getInstance().rebootGateway(gateway);            	
          }
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
    public Gateway loadGateway(Long id) {
        Gateway gateway = null;
        try {
            gateway = (Gateway) gatewayDao.getObject(Gateway.class, id);
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
    public List<Gateway> loadBuildingGateways(Long buildingId) {
        return gatewayDao.loadBuildingGateways(buildingId);
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

    public List<Gateway> loadAllGateways() {
        return gatewayDao.loadAllGateways();
    }

    public List<Gateway> loadUnCommissionedGateways() {
        return gatewayDao.loadUnCommissionedGateways();
    }

    public Gateway getGatewayByIp(String ip) {
        return gatewayDao.getGatewayByIp(ip);
    }

    /**
     * Called internally, especially when the user hits commission button and would like to commission this
     * InventoryDevice (GW). Function will add the gateway to gateway table and will delete it from the inventory table.
     * 
     * @param gateway
     */
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
            inventoryDeviceDao.deleteInventoryDeviceByMacAddress(sMacAddrr);
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
            gatewayDao.commission(gateway.getId());
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
            if (gw.getFloor().getId() != gateway.getFloor().getId()) {
                // Commissioning gateway on to different floor...
                Floor floor = floorDao.getFloorById(gateway.getFloor().getId());
                gw.setFloor(floor);
                //gateway.setFloor(floor);
                long buildingId = floor.getBuilding().getId();
                gw.setBuildingId(buildingId);
                Building building = buildingDao.getBuildingById(buildingId);
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
                gatewayDao.updateGatewayParameters(gw);
            }            
            return true;
    }

    public void setCommissionStatus(long gatewayId) {
        gatewayDao.commission(gatewayId);
    }

    public boolean isCommissioned(long gatewayId) {
        return gatewayDao.isCommissioned(gatewayId);
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

    public void updateGatewayPosition(Gateway gateway) {
        gatewayDao.updateGatewayPosition(gateway);
    }

    public void getRealtimeStats(String gwIp) {
        Gateway oGW = getGatewayByIp(gwIp);
        if (oGW != null)
            GatewayImpl.getInstance().sendGatewayInfoReq(oGW);
    }

    public void getRealtimeStatsByGWId(Long id) {
        Gateway oGW = loadGateway(id);
        if (oGW != null)
            GatewayImpl.getInstance().sendGatewayInfoReq(oGW);
    }

    
    public void saveGatewayInfo(Gateway gw) {
        gatewayDao.saveGatewayInfo(gw);
    }

    /**
     * Fetches the list from the inventory table and adds these to the gateway table with the appropriate floor id.
     * 
     * @param floorID
     * @return com.ems.model.Gateway collection
     */
    public List<Gateway> discoverGateways(long floorID) {
        List<InventoryDevice> oGWInInventoryList = inventoryDeviceDao
                .loadAllInventoryDeviceByType(ServerConstants.DEVICE_GATEWAY);
        List<Gateway> oGatewayList = new ArrayList<Gateway>();
        if (oGWInInventoryList != null) {
            Iterator<InventoryDevice> oGWInventoryItr = oGWInInventoryList.iterator();
            InventoryDevice oGWIDevice = null;
            Gateway oGateway = null;
            while (oGWInventoryItr.hasNext()) {
                oGWIDevice = oGWInventoryItr.next();
                oGateway = new Gateway();
                Floor floor = floorDao.getFloorById(floorID);
                oGateway.setFloor(floor);
                long buildingId = floor.getBuilding().getId();
                oGateway.setBuildingId(buildingId);
                Building building = buildingDao.getBuildingById(buildingId);
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
                String sMacAddrr = oGWIDevice.getMacAddr();
                // sMacAddrr = sMacAddrr.replace(":", "");
                oGateway.setGatewayName("GW" + ServerUtil.generateName(oGWIDevice.getSnapAddr()));
                oGateway.setEthSecType(ServerConstants.ETH_SEC_TYPE);
                oGateway.setEthSecEncryptType(ServerConstants.ETH_SEC_ENCRYPT_TYPE);
                oGateway.setEthSecKey(ServerConstants.SECURITY_KEY);
                oGateway.setChannel(ServerConstants.WIRELESS_DEF_CHANNEL);
                oGateway.setWirelessEncryptType(ServerConstants.ETH_SEC_TYPE);
                oGateway.setWirelessEncryptKey(ServerConstants.SECURITY_KEY);
                oGateway.setWirelessRadiorate(ServerConstants.WIRELESS_DEF_RADIO_RATE);
                oGateway.setNoOfSensors(0);
                oGateway.setSnapAddress(oGWIDevice.getSnapAddr());
                oGateway.setMacAddress(oGWIDevice.getMacAddr());
                oGateway.setEthIpaddrType(0);
                oGateway.setIpAddress(oGWIDevice.getIpAddress());
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

                AddGateway(oGateway);
                oGatewayList.add(oGateway);
            }
        }
        return oGatewayList;
    }

    public Gateway updateFields(Gateway gw) {
        return gatewayDao.updateFields(gw);
    }

    public void exitCommissioning(List<Long> gatewayIdList) {
        System.out.println("Exiting gateway commissioning process...");
        return;
    }

    public void setImageUpgradeStatus(long gwId, String status) {
        gatewayDao.setImageUpgradeStatus(gwId, status);
    } // end of method setImageUpgradeState

    public void setVersions(Gateway gw) {

        gatewayDao.setVersions(gw);

    } // end of method setVersions

}