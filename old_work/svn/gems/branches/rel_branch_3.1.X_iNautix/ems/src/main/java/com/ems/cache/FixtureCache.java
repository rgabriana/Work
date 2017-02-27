/**
 * 
 */
package com.ems.cache;

import java.util.HashMap;
import java.util.List;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.service.FixtureManager;


/**
 * @author Sreedhar
 *
 */
public class FixtureCache {
  
  private static FixtureCache instance = null;
  
  //hash map to hold the devices in memory by snap address
  private HashMap<String, Long> deviceSnapMap = new HashMap<String, Long>();
  
  // hash map to hold the devices in memory
  private HashMap<Long, DeviceInfo> deviceMap = new HashMap<Long, DeviceInfo>();
  
  private FixtureManager fixtureMgr = null;  
  
  //private long lastCurrentDataId = 1;
 
  /**
   * 
   */
  public FixtureCache() {
  
    // TODO Auto-generated constructor stub
  }
  
  public static FixtureCache getInstance() {
    
    if(instance == null) {
      synchronized(FixtureCache.class) {
	if(instance == null) {
	  instance = new FixtureCache();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  public HashMap<Long, DeviceInfo> getDeviceMap() {

    return deviceMap;

  } // end of method getDeviceMap
  
  private synchronized void addDevice(Long id, DeviceInfo device) {

    deviceMap.put(id, device);

  } // end of method addDevice

  public synchronized void invalidateDeviceCache(Long id) {
  
    DeviceInfo device = deviceMap.get(id);
    if(device != null) {
      device.setValid(false);
      deviceSnapMap.remove(device.getFixture().getSnapAddress());
    }
      
  } //end of method invalidateDeviceCache
  
  public synchronized void invalidateDeviceCache(String snap) {
    
    if(deviceSnapMap.containsKey(snap)) {
      long id = deviceSnapMap.get(snap);
      invalidateDeviceCache(id);
    }    
    
  } //end of method invalidateDeviceCache
  
  public void initializeDeviceMap() {

    if (fixtureMgr == null) {
      fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
    }
    List<Fixture> fixtureList = fixtureMgr.loadAllFixtures();
    if (fixtureList == null) {
      return;
    }
    DeviceInfo device = null;
    int noOfFixtures = fixtureList.size();
    Fixture fixture = null;
    for (int i = 0; i < noOfFixtures; i++) {
      fixture = fixtureList.get(i);
      device = new DeviceInfo(fixture);
      addDevice(fixture.getId(), device);
    }

  } // end of method initializeDeviceMap
  
  public boolean isPFPktMarked(Long fixId) {

    DeviceInfo device = deviceMap.get(fixId);
    if (device != null) {
        if (device.isPFPktMarked(fixId)) {
            return true;
        }
    }
    return false;
    
  } // end of method isPFPktPending

  public DeviceInfo getDevice(Long id) {

    if (deviceMap.containsKey(id)) {
        return deviceMap.get(id);
    }
    return null;

  } // end of method getDevice
  
  public Fixture getCachedFixture(Long id) {
  	
  	if (deviceMap.containsKey(id)) {
  		DeviceInfo device = deviceMap.get(id);
  		return device.getFixture();
  	}  	
  	return null;
  	
  } //end of method getCachedFixture

  public Fixture getDeviceFixture(String snap) {
  
    try {
      if (deviceSnapMap.containsKey(snap)) {
        Long id = deviceSnapMap.get(snap); 
        if(deviceMap.containsKey(id)) {
          DeviceInfo device = deviceMap.get(id);
          if(device.isValid()) {
            return device.getFixture();        	
          }              
        }            
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Fixture fixture = fixtureMgr.getFixtureBySnapAddr(snap);
    if(fixture != null) {
      FixtureCache.getInstance().updateFixture(fixture);
    }
    return fixture;

  } // end of method getDeviceFixture

  public DeviceInfo updateFixture(Fixture fixture) {
  
    DeviceInfo device = null;
    if(deviceMap.containsKey(fixture.getId())) {
      device = deviceMap.get(fixture.getId());
      device.setFixture(fixture);
      device.setValid(true);
    } else {
      device = new DeviceInfo(fixture);
      device.setUptime(1);
      addDevice(fixture.getId(), device);	
    }
    deviceSnapMap.put(fixture.getSnapAddress(), fixture.getId());
    return device;
  
  } //end of method updateFixture

  public DeviceInfo getDevice(Fixture fixture) {

    DeviceInfo device = null;
    try {
        if (!deviceMap.containsKey(fixture.getId())) {
          device = updateFixture(fixture);
        } else {
          device = deviceMap.get(fixture.getId());
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
  public void getProfileFromSU(long fixtureId, byte[] packet) {

      if (fixtureMgr == null) {
          fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
      }
      Fixture fixture = fixtureMgr.getFixtureById(fixtureId);
      if (fixture == null) {
          //logger.error(fixtureId + ": Fixture is unknown or it is not commissioned");
          return;
      }
      // String fixtureName = fixture.getFixtureName();
      // ServerUtil.printPacket(fixtureName + "- CurrentState ", packet);
      DeviceInfo dInfo = getDevice(fixture);
      if (dInfo != null) {
          dInfo.ackPfH(fixtureId, packet);
      }
  } // end of method getProfileFromSU

  
} //end of class FixtureCache
