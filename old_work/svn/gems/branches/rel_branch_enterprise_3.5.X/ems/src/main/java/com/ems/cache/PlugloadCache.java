/**
 * 
 */
package com.ems.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.model.Plugload;
import com.ems.service.FixtureManager;
import com.ems.service.PlugloadManager;


/**
 * @author Sreedhar
 *
 */
public class PlugloadCache {
  
  private static PlugloadCache instance = null;
  
  //hash map to hold the devices in memory by snap address
  private HashMap<String, Long> deviceSnapMap = new HashMap<String, Long>();
  
  // hash map to hold the devices in memory
  private HashMap<Long, PlugloadInfo> deviceMap = new HashMap<Long, PlugloadInfo>();
  
  private PlugloadManager plugloadMgr = null;  
  
  private ArrayList<Long> nodesRebootList = new ArrayList<Long>();
  
  /**
   * 
   */
  public PlugloadCache() {
  
    // TODO Auto-generated constructor stub
  }
  
  public static PlugloadCache getInstance() {
    
    if(instance == null) {
      synchronized(PlugloadCache.class) {
      	if(instance == null) {
      		instance = new PlugloadCache();
      	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public HashMap<Long, PlugloadInfo> getDeviceMap() {

    return deviceMap;

  } // end of method getDeviceMap
  
  private synchronized void addDevice(Long id, PlugloadInfo device) {

    deviceMap.put(id, device);

  } // end of method addDevice

  public synchronized void invalidateDeviceCache(Long id) {
  
    PlugloadInfo device = deviceMap.get(id);
    if(device != null) {
      device.setValid(false);
      deviceSnapMap.remove(device.getPlugload().getSnapAddress());
    }
      
  } //end of method invalidateDeviceCache
  
  public synchronized void invalidateDeviceCache(String snap) {
    
    if(deviceSnapMap.containsKey(snap)) {
      long id = deviceSnapMap.get(snap);
      invalidateDeviceCache(id);
    }    
    
  } //end of method invalidateDeviceCache
  
  public void initializeDeviceMap() {

    if (plugloadMgr == null) {
    	plugloadMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
    }
    List<Plugload> plugloadList = plugloadMgr.getAllPlugloads();
    if (plugloadList == null) {
      return;
    }
    PlugloadInfo device = null;
    int noOfPlugloads = plugloadList.size();
    Plugload plugload = null;
    for (int i = 0; i < noOfPlugloads; i++) {
      plugload = plugloadList.get(i);
      device = new PlugloadInfo(plugload);
      addDevice(plugload.getId(), device);
    }

  } // end of method initializeDeviceMap

  public PlugloadInfo getDevice(Long id) {

    if (deviceMap.containsKey(id)) {
        return deviceMap.get(id);
    }
    return null;

  } // end of method getDevice
  
  public Plugload getCachedPlugload(Long id) {
  	
  	if (deviceMap.containsKey(id)) {
  		PlugloadInfo device = deviceMap.get(id);
  		return device.getPlugload();
  	}  	
  	return null;
  	
  } //end of method getCachedPlugload

  public Plugload getDevicePlugload(String snap) {
  
    try {
      if (deviceSnapMap.containsKey(snap)) {
        Long id = deviceSnapMap.get(snap); 
        if(deviceMap.containsKey(id)) {
          PlugloadInfo device = deviceMap.get(id);
          if(device.isValid()) {
            return device.getPlugload();        	
          }              
        }            
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Plugload plugload = plugloadMgr.getPlugloadBySnapAddress(snap);
    if(plugload != null) {
      updatePlugload(plugload);
    }
    return plugload;

  } // end of method getDevicePlugload

  public PlugloadInfo updatePlugload(Plugload plugload) {
  
    PlugloadInfo device = null;
    if(deviceMap.containsKey(plugload.getId())) {
      device = deviceMap.get(plugload.getId());
      device.setPlugload(plugload);
      device.setValid(true);
    } else {
      device = new PlugloadInfo(plugload);
      device.setUptime(1);
      addDevice(plugload.getId(), device);	
    }
    deviceSnapMap.put(plugload.getSnapAddress(), plugload.getId());
    return device;
  
  } //end of method updateFixture

  public PlugloadInfo getDevice(Plugload plugload) {

    PlugloadInfo device = null;
    try {
    	if (!deviceMap.containsKey(plugload.getId())) {
    		device = updatePlugload(plugload);
    	} else {
    		device = deviceMap.get(plugload.getId());
    	}
    } catch (Exception e) {
        e.printStackTrace();
    }
    return device;

  } // end of method getDevice
  
  /**
   * Fetch profile from SU
   * 
   * @param fixtureId
   * @param packet
   */
  public void getProfileFromPlugload(long plugloadId, byte[] packet) {
  	
  	if (plugloadMgr == null) {
  		plugloadMgr = (PlugloadManager) SpringContext.getBean("plugloadManager");
  	}
  	Plugload plugload = plugloadMgr.getPlugloadById(plugloadId);
  	if (plugload == null) {
  		return;
  	}
  	PlugloadInfo dInfo = getDevice(plugload);
  	if (dInfo != null) {
  		dInfo.ackPfH(plugloadId, packet);
  	}
  	
  } // end of method getProfileFromPlugload

  //TODO need to see whether we need to have separate nodes reboot list for Sensors and plugloads or do we need
  //to have same list
  public boolean nodeRebooted(Long id) {
  	
  	synchronized(nodesRebootList) {
  		nodesRebootList.add(id);
  	}
  	if(nodesRebootList.size() == 1) {  
  		return true;
  	}
  	return false;
  	
  } //end of method nodeRebooted
  
  public ArrayList<Long> drainNodeRebootList() {
  	
  	ArrayList<Long> nodeList = null;
  	synchronized(nodesRebootList) {
  		nodeList = (ArrayList<Long>)nodesRebootList.clone();
			nodesRebootList.clear();
  	}
  	return nodeList;
  	
  } //end of method drainNodeRebootList
  
} //end of class PlugloadCache
