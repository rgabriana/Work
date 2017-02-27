package com.ems.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.PlugloadCache;
import com.ems.dao.GatewayDao;
import com.ems.dao.PlugloadDao;
import com.ems.dao.PlugloadGroupDao;
import com.ems.dao.PlugloadProfileDao;
import com.ems.model.Gateway;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfileHandler;
import com.ems.server.ServerConstants;
import com.ems.server.device.DeviceService;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.plugload.PlugloadImpl;

import com.ems.server.discovery.DiscoverySO;
import com.ems.service.FixtureManager.FixtureSorter;
import com.ems.vo.model.PlugloadList;

@Service("plugloadManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadManager {

	static final Logger logger = Logger.getLogger("plugloadLogger");
	@Resource
	private PlugloadDao plugloadDao;

	@Resource
	private GatewayDao gatewayDao;
	@Resource
	private PlugloadGroupDao plugloadGroupDao;
	
	@Resource
	private PlugloadProfileDao plugloadProfileDao;

	public void test() {
		plugloadDao.loadAllPlugloads();
	}

	public List<Plugload> loadPlugloadByFloorId(Long pid) {
		return plugloadDao.loadPlugloadByFloorId(pid);
	}
	
	/**
   * Load plugload details.load all placed plugloads of given floor
   * 
   * @param id
   *            floor id
   * @return com.ems.model.Plugload collection load only id,plugloadId,floor id,area id,subArea id, x axis,y axis details
   *         of plugload other details loads as null.
   */
  public List<Plugload> loadPlacedPlugloadsByFloorId(Long id) {
  	
  	logger.info("Entering loadPlacedPlugloadsByFloorId");
  	List<Plugload> plugloads = plugloadDao.loadPlacedPlugloadsByFloorId(id);
  	if (plugloads != null) {
  		logger.info("Exiting loadPlacedPlugloadsByFloorId -- " + plugloads.size());
  	}	else {
  		logger.info("Exiting loadPlacedPlugloadsByFloorId -- ");
  	}
  	return plugloads;
  	
  } //end of method loadPlacedPlugloadsByFloorId
  
  public void updatePlugloadSecGw(List<Plugload> plugloads, Gateway gw) {
  	
		Iterator<Plugload> itr = plugloads.iterator();
		while (itr.hasNext()) {
			Plugload plugload = (Plugload) itr.next();
			Plugload plObj = getPlugloadById(plugload.getId());
			plObj.setGateway(gw);			
			save(plObj);
			PlugloadCache.getInstance().invalidateDeviceCache(plObj.getSnapAddress());			
		} 

	} //end of method updatePlugloadSecGw

	public List<Plugload> loadPlugloadByAreaId(Long pid) {
		return plugloadDao.loadPlugloadByAreaId(pid);
	}

	public PlugloadList loadAllPlugloads() {
		return plugloadDao.loadPlugloadList();
	}

	public Plugload getPlugloadById(long plugloadId) {
		return plugloadDao.getPlugloadById(plugloadId);

	}

	public Plugload updatePlugload(Plugload plugload) {
		return plugloadDao.saveOrUpdatePlugload(plugload);

	}

	public List<Plugload> loadPlugloadByPlugloadGroupId(Long id) {
		return plugloadDao.loadPlugloadByPlugloadGroupId(id);
	}

	public List<Plugload> loadPlugloadByProfileTemplateId(Long id) {
		return plugloadDao.loadPlugloadByProfileTemplateId(id);
	}

	public void updatePlugloadProfileHandler(
			PlugloadProfileHandler profileHandler) {
		plugloadDao.updateProfileHandler(profileHandler);
	}

	public Plugload getPlugloadBySnapAddress(String snapAddress) {

		return plugloadDao.getPlugloadBySnapAddress(snapAddress);

	} // end of method getPlugloadBySnapAddress

	/**
	 * get the deleted plugload by snap address
	 * 
	 * @param snapAddr
	 * @return the deleted plugload by snap address
	 */
	public Plugload getDeletedPlugloadBySnapAddr(String snapAddr) {

		return (Plugload) plugloadDao.getDeletedPlugloadBySnapAddr(snapAddr);

	} // end of method getDeletedPlugloadBySnapAddr
	
	public void resetPushProfileForPlugload(Long plugloadId) {
		
    plugloadDao.resetPushProfileForPlugload(plugloadId);
    Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
    if(plugload != null) {
    	plugload.setPushProfile(false);
    }        
    
	} //end of method resetPushProfileForPlugload

	public void resetPushGlobalProfileForPlugload(Long plugloadId) {
		
    plugloadDao.resetPushGlobalProfileForPlugload(plugloadId);
    Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
    if(plugload != null) {
    	plugload.setPushGlobalProfile(false);
    }

	} //end of method resetPushGlobalProfileForPlugload

	/**
	 * save Plugload details.
	 * 
	 * @param plugload
	 *            com.ems.model.Plugload
	 */
	public Plugload save(Plugload plugload) {
		return (Plugload) plugloadDao.saveObject(plugload);
	}

	public int discoverPlugloads(long floorId, long gwId) {

		return DiscoverySO.getInstance().startNetworkDiscovery(floorId, gwId, ServerConstants.DEVICE_PLUGLOAD);

	} // end of method discoverPlugloads

	public List<Plugload> getUnCommissionedPlugloadList(long gatewayId) {

		return plugloadDao.getUnCommissionedPlugloadList(gatewayId);

	}

	public void updateCommissionStatus(int[] fixtureIds, int status) {

		plugloadDao.updateCommissionStatus(fixtureIds, status);

	}
	
	/**
   * Sort Plugloads based on X & Y co-ordinates
   * 
   * @param plugloadArr
   *            input plugloadid array
   * @return sorted plugloadid array
   */
  public int[] sortPlugloads(int[] plugloadArr) {
  	
  	if (logger.isDebugEnabled()) {
  		logger.debug("Begin sorting...");
  	}
  	StringBuffer oBuffer = new StringBuffer();
  	int noOfPlugloads = plugloadArr.length;
  	List<Plugload> plugloadList = new ArrayList<Plugload>();
  	for (int i = 0; i < noOfPlugloads; i++) {
  		Plugload plugload = getPlugloadById(plugloadArr[i]);
  		if (plugload == null) {
  			logger.error(plugloadArr[i] + ": There is no Plugload");
  			continue;
  		}
  		plugloadList.add(plugload);
  	}
  	Collections.sort(plugloadList, new FixtureManager(). new FixtureSorter());
  	if (plugloadList.size() > 0) {
  		plugloadArr = null;
  		plugloadArr = new int[plugloadList.size()];
  		Plugload oPlugload = null;
  		for (int count = 0; count < plugloadList.size(); count++) {
  			oPlugload = (Plugload) plugloadList.get(count);
  			plugloadArr[count] = oPlugload.getId().intValue();              
  		}
  		plugloadList.clear();
  		plugloadList = null;
  	}
  	if (logger.isDebugEnabled()) {
  		logger.debug(oBuffer.toString());
  		logger.debug("End sorting...");
  	}  	
  	return plugloadArr;
  	
  } //end of method sortPlugloads
  
  /**
   * Updated on the return Ack for the hopper command from the SU.
   * @param lugloadId
   * @param isHopper
   */
  public void updateHopperState(Long plugloadId, Integer isHopper) {
  	
  	if(logger.isDebugEnabled()) {
  		logger.debug(plugloadId + ": Hooper (" + isHopper + ")");
  	}
  	Plugload dbPl = getPlugloadById(plugloadId);
  	dbPl.setIsHopper(isHopper);
  	plugloadDao.update(dbPl);
  	// update cache
  	Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
  	if(plugload != null) {
  		plugload.setIsHopper(isHopper);
  	}
  	
  } //end of method updateHopperState
  
  public void enableHopper(long plugloadId, boolean enable) {

    PlugloadImpl.getInstance().enableHopper(plugloadId, enable);

  } //end of method enableHopper

	public void turnOnOffPlugloads(int[] plugloadArr, int percentage, int time) {

    if (time == 1777) {
    	// enable hopper
    	enableHopper(plugloadArr[0], true);
    	return;
    }
    if (time == 1778) {
    	enableHopper(plugloadArr[0], false);
    	return;
    }
    plugloadArr = sortPlugloads(plugloadArr);    
    if (logger.isDebugEnabled()) {
    	logger.debug("called absolute dimplugloads with percentage -- " + percentage);
    }
    PlugloadImpl.getInstance().turnOnOffPlugloads(plugloadArr, percentage, time);

	} // end of method turnOnOffPlugloads

	/**
	 * return list of Plugload object associated with gateway
	 * 
	 * @param gatewayId
	 * @return list of Plugload object associated with gateway
	 */
	public List<Plugload> loadAllPlugloadsByGatewayId(Long gatewayId) {

		List<Plugload> plugloadList = plugloadDao
				.loadAllPlugloadsByGatewayId(gatewayId);
		if (plugloadList != null && !plugloadList.isEmpty()) {
			return plugloadList;
		}
		return new ArrayList<Plugload>();

	} // end of method loadAllPlugloadsByGatewayId

	public void setImageUpgradeStatus(long plugloadId, String status) {

		plugloadDao.setImageUpgradeStatus(plugloadId, status);

	} // end of method setImageUpgradeState

	public void updateFirmwareVersion(String version, long id, long gwId) {

		plugloadDao.updateFirmwareVersion(version, id, gwId);

	} // end of method updateFirmwareVersion

	public void updateVersion(String version, long id, long gwId) {

		plugloadDao.updateVersion(version, id, gwId);

	} // end of method updateVersion

	/**
	 * Load plugloads available in plugload table
	 * 
	 * @return
	 */
	public List<Plugload> getAllPlugloads() {

		return plugloadDao.loadAllPlugloads();

	} // end of method getAllPlugloads

	public void updatePlugloadVersionSyncedState(Plugload plugload) {

		plugloadDao.updatePlugloadVersionSyncedState(plugload);

	} // end of method updatePlugloadVersionSyncedState

	public void updateBootInfo(Plugload plugload, String upgrStatus) {

		plugloadDao.updateBootInfo(plugload, upgrStatus);

	} // end of method updateBootInfo

	public void enablePushProfileForPlugload(Plugload plugload) {

		if (plugload != null) {
			plugloadDao.enablePushProfileForPlugload(plugload.getId());
			plugload.setPushProfile(true);
		}

	} // end of method enablePushProfileForPlugload
	
	public void enablePushProfileForPlugload(Long plugloadId) {
		plugloadDao.enablePushProfileForPlugload(plugloadId);
		Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
		if (plugload != null) {
			plugload.setPushProfile(true);
		}
	}

	public void enablePushGlobalProfileForPlugload(Plugload plugload) {

		if (plugload != null) {
			plugloadDao.enablePushGlobalProfileForPlugload(plugload.getId());
			plugload.setPushGlobalProfile(true);
		}

	} // end of method enablePushGlobalProfileForPlugload
	
	public void enablePushGlobalProfileForPlugload(Long plugloadId) {
		plugloadDao.enablePushGlobalProfileForPlugload(plugloadId);
		Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
		if (plugload != null) {
			plugload.setPushGlobalProfile(true);
		}
	}

	public PlugloadProfileHandler getProfileHandlerByPlugloadId(Long plugloadId) {

		return plugloadDao.getProfileHandlerByPlugloadId(plugloadId);

	} // end of method getProfileHandlerByPlugloadId

	public int getProfileNoForPlugload(long plugloadId) {

		return plugloadDao.getProfileNoForPlugload(plugloadId);

	} // end of method getProfileNoForPlugload

	public Long assignGroupProfileToPlugload(long plugloadId, byte profileno) {

		// Fetch the tenant id (currently set to null)
		PlugloadGroups plugGroup = plugloadGroupDao
				.getGroupByProfileAndTenantDetails(profileno, null);
		if (plugGroup != null) {
			PlugloadCache.getInstance().invalidateDeviceCache(plugloadId);
			return plugloadDao.assignGroupProfileToPlugload(plugloadId,
					plugGroup);
		}
		return 0L; // profileno should match

	} // end of method assignGroupProfileToPlugload
	
	public void getCurrentDetails(Long fixtureId) {
		
    PlugloadImpl.getInstance().getPlugloadCurrentStatus(fixtureId);
    
	} //end of method getCurrentDetails

	public Plugload updatePosition(Long id, Integer xaxis, Integer yaxis,
			String state) {
		return plugloadDao.updatePosition(id, xaxis, yaxis, state);
	}

	public PlugloadList loadPlugloadListWithSpecificAttrs(String property,
			Long pid, String orderby, String orderway, Boolean bSearch,
			String searchField, String searchString, String searchOper,
			int offset, int limit) {
		return plugloadDao.loadPlugloadListWithSpecificAttrs(property, pid,
				orderby, orderway, bSearch, searchField, searchString,
				searchOper, offset, limit);
	}

	public void cancelNetworkDiscovery() {
		DiscoverySO.getInstance().cancelNetworkDiscovery();
	}

	public int getDiscoveryStatus() {
		int dStatus = DiscoverySO.getInstance().getDiscoveryStatus();
		if (logger.isDebugEnabled()) {
			logger.debug("Discovery Status: " + dStatus);
		}
		return dStatus;
	} // end of method getDiscoveryStatus

	public int getCommissioningStatus() {
		return DiscoverySO.getInstance().getCommissioningStatus();
	} // end of method getCommissioningStatus

	 public String getCommissionStatus(long plugloadId) {
	        return plugloadDao.getCommissionStatus(plugloadId);
	    }
	/**
	 * Delete Plugload details
	 * 
	 * @param id
	 *            database id(primary key)
	 */
	public int deletePlugload(Long id) {
		int iStatus = 1;
		try {
			final Plugload plugload = (Plugload) plugloadDao.getObject(
					Plugload.class, id);
			if (plugload == null) {
				return 3;
			}
			if (plugload.getState().equals(
					ServerConstants.FIXTURE_STATE_COMMISSIONED_STR)) {
				// plugload is already commissioned so first change it to
				// factory default
				// before deleting it
				if (logger.isDebugEnabled()) {
					logger.debug(id
							+ ": sending wireless factory because of delete");
				}
				
				Thread oSWFDThread = new Thread(plugload.getName() + "SWFD") { 
					public void run() {
						PlugloadImpl.getInstance().setApplyWirelessDefaultParams(plugload, true); 
					} 
				}; 
				oSWFDThread.start(); 
				try {
					oSWFDThread.join(); 
				} catch (InterruptedException ie) {
					logger.warn(id + ": interrupted!"); 
				} 
				if (DeviceServiceImpl.getInstance().getSuWirelessChangeAckStatus(plugload) == false) { 
					iStatus = 2; 
				}				 
			}
			if (iStatus == 1) {
				invalidatePlugloadCacheAndUpdateState(plugload);
			}
		} catch (ObjectRetrievalFailureException orfe) {
			iStatus = 3;
			orfe.printStackTrace();
		}
		return iStatus;
	}
	
	public void rebootPlugload(long plugloadId) {

    PlugloadImpl.getInstance().rebootPlugload(plugloadId);

  } // end of method rebootPlugload
	
	public Integer getPlugloadHopperStatus(long plugloadId) {
		
  	return plugloadDao.getPlugloadHopperStatus(plugloadId);
  	
  } //end of method getPlugloadHopperStatus
	
	/**
   * get the plugloads by state
   * 
   * @param state
   *            plugload state
   */
  public List<Plugload> loadPlugloadsByState(String state) {
  	
  	String plugloadState = null;
  	if (state.equalsIgnoreCase(ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR)) {
  		plugloadState = ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR;
  	} else if (state.equalsIgnoreCase(ServerConstants.PLUGLOAD_STATE_DELETED_STR)) {
  		plugloadState = ServerConstants.PLUGLOAD_STATE_DELETED_STR;
  	} else if (state.equalsIgnoreCase(ServerConstants.PLUGLOAD_STATE_DISCOVER_STR)) {
  		plugloadState = ServerConstants.PLUGLOAD_STATE_DISCOVER_STR;
  	} else if (state.equalsIgnoreCase(ServerConstants.PLUGLOAD_STATE_PLACED_STR)) {
  		plugloadState = ServerConstants.PLUGLOAD_STATE_PLACED_STR;
  	} else {
  		return null;
  	}

  	logger.info("Entering loadPlugloadsBystate");
  	List<Plugload> plugloads = plugloadDao.loadPlugloadsByState(plugloadState);
  	if (plugloads != null) {
  		logger.info("Exiting loadPlugloadsBystate -- " + plugloads.size());
  	} else {
  		logger.info("Exiting loadPlugloadsBystate -- ");
  	}
  	return plugloads;

  } //end of method loadPlugloadsByState
  
  public List<Plugload> loadPlacedAndCommissionedPlugloadsByFloorId(Long id) {
  	
    logger.info("Entering loadPlacedAndCommissionedPlugloadsByFloorId");
    List<Plugload> plugloads = plugloadDao.loadPlacedAndCommissionedPlugloadsByFloorId(id);
    if (plugloads != null) {
    	logger.info("Exiting loadPlacedAndCommissionedPlugloadsByFloorId -- " + plugloads.size());
    }
    else {
    	logger.info("Exiting loadPlacedAndCommissionedPlugloadsByFloorId -- ");
    }
    return plugloads;
    
  } //end of method loadPlacedAndCommissionedPlugloadsByFloorId

	@SuppressWarnings("unused")
	private void invalidatePlugloadCacheAndUpdateState(Plugload plugload) {
		Long id = plugload.getId();
		// deleteGroupAndLightLevelsForFixture(id);
		PlugloadCache.getInstance().invalidateDeviceCache(id);
		plugload.setState(ServerConstants.FIXTURE_STATE_DELETED_STR);
		plugloadDao.updateState(plugload);
		Gateway gateway = plugload.getGateway();
		if (gateway != null) {
			if (gateway.getNoOfSensors() > 0)
				gateway.setNoOfSensors(gateway.getNoOfSensors() - 1);
			gatewayDao.updateFields(gateway);
		}
	}
	
	public void updateState(Plugload plugload) {
		
		plugloadDao.updateState(plugload);
    
	} // end of method updateState
	
	public void enablePushProfileAndGlobalPushProfile(Long plugloadId, boolean pushProfileStatus, boolean globalPushProfileStatus) {
		
		plugloadDao.enablePushProfileAndGlobalPushProfile(plugloadId, pushProfileStatus, globalPushProfileStatus);
		Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
		if(plugload != null) {
			plugload.setPushProfile(pushProfileStatus);
			plugload.setPushGlobalProfile(globalPushProfileStatus);
		}	
	
	} //end of method enablePushProfileAndGlobalPushProfile
	
	public void updateRealtimeStats(Plugload plugload) {
  
		plugloadDao.updateRealtimeStats(plugload);
	
	} //end of method updateRealtimeStats
	
	public void changeGroupsSyncPending(Plugload plugload) {

    plugloadDao.changeGroupsSyncPending(plugload);

	} // end of method changeGroupsSyncPending
	
	public void updateStats(Plugload plugload) {

    try {
        plugloadDao.updateStats(plugload);
    } catch (Exception e) {
        e.printStackTrace();
    }

	} // end of method updateStats
	
    public void updateStateAndLastConnectivityTime(Plugload plugload) {

        try {
            plugloadDao.updateStateAndLastConnectivityTime(plugload);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method updateState

    public int startPlugloadCommissionProcess(long gatewayId, long floorId, int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling bulk commissionPlugload API -- " + gatewayId);
        }
        // TODO: Needs optimization...
        List<Plugload> oList = getUnCommissionedPlugloadList(gatewayId);
        int ret = DiscoverySO.getInstance().startPlugloadCommission(floorId, gatewayId, oList, type);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with bulk commissionPlugload API");
        }
        return ret;
    }
    public int startPlugloadCommissionProcess(long gatewayId, long floorId, int type,List<Plugload> oList) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling bulk commissionPlugload API -- " + gatewayId);
        }
        int ret = DiscoverySO.getInstance().startPlugloadCommission(floorId, gatewayId, oList, type);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with bulk commissionPlugload API");
        }
        return ret;
    }
    public int commissionPlugload(Long plugloadId, Long gwId) {
    	if (DiscoverySO.getInstance().commissionPlugload(plugloadId, gwId) == true) {
    		return 0;
    	}
    	return 1;
    }
    
    public void pushPlugloadProfileToPlugloadNow(Long plugloadId) {
        // PUSHING REAL TIME Plugload PROFILE TO Plugload
        Plugload oPlugload = getPlugloadById(plugloadId);
        //PlugloadGroups plugloadGroups = plugloadGroupDao.getGroupById(oPlugload.getGroupId());
        PlugloadImpl.getInstance().sendProfileToPlugload(plugloadId);
        PlugloadImpl.getInstance().setGlobalProfile(plugloadId); 
    }
    
    public Long bulkProfileAssignToPlugload(String plugloadIdsList, Long plugloadGroupid, String currentPlugloadProfile) {
    	Long totalRecordUpdated = null;
        synchronized (this) {
            totalRecordUpdated = plugloadDao.bulkProfileAssignToPlugload(plugloadIdsList, plugloadGroupid, currentPlugloadProfile);
            //Invalidate the Plugload Cache for all plugloads
            String plugloadArray[];
            plugloadArray = plugloadIdsList.split(",");
            if (plugloadArray != null) {
            	for (String s: plugloadArray)
                {
            		 Long plugloadId = Long.parseLong(s);
            		 PlugloadCache.getInstance().invalidateDeviceCache(plugloadId);
                }
            }
        }
		return totalRecordUpdated;
    }
    
    public void changePlugloadProfile(Long plugloadId, Long plugloadGroupId, String currentPlugloadProfile, String originalPlugloadProfileFrom) {
        synchronized (this) {
        	PlugloadCache.getInstance().invalidateDeviceCache(plugloadId);
            Long globalProfileHandlerId = plugloadProfileDao.getGlobalProfileHandlerId();
            if (plugloadGroupId.equals(0L)) {
                plugloadDao.updatePlugloadProfileHandlerIdForPlugload(plugloadId, globalProfileHandlerId, currentPlugloadProfile,
                		originalPlugloadProfileFrom);
            } else {
            	plugloadDao.changePlugloadProfile(plugloadId, plugloadGroupId, globalProfileHandlerId, currentPlugloadProfile,
                		originalPlugloadProfileFrom);
                DeviceServiceImpl.getInstance().updatePlugloadGroupProfile(plugloadGroupId, plugloadId.longValue());
            }
        }
    }

	/**
     * return list of Gateway object associated with secondary gateway
     * 
     * @param secGwId
     * @return list of Gateway object associated with secondary gateway
     */
    public List<Plugload> loadAllPlugloadBySecondaryGatewayId(Long secGwId) {
        List<Plugload> plugloadList = plugloadDao.loadAllPlugloadBySecondaryGatewayId(secGwId);
        if (plugloadList != null && !plugloadList.isEmpty())
            return plugloadList;
        return new ArrayList<Plugload>();
    }
    public int exitCommissioning(Long gatewayId) {
        int iUnCommissionedPlugloads = 0;
        List<Plugload> plugloadList = getUnCommissionedPlugloadList(gatewayId);
        if (plugloadList != null)
        	iUnCommissionedPlugloads = plugloadList.size();
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting plugload commissioning process... GW (" + gatewayId + "), UnCommissioned Plugload ("
                    + iUnCommissionedPlugloads + ")");
            logger.debug("gwid == " + gatewayId);
        }
        int commStatus = 8;   
        return DiscoverySO.getInstance().finishPlugloadCommissioning(gatewayId, plugloadList);
    }
    
    public Long loadAllPlugloadsList() {

		Long count = plugloadDao
				.loadAllPlugloadsCount();
		if (count==null) count=0l;
		return count;

	}

    public void auto(int[] plugloadArr) {
        if (logger.isDebugEnabled()) {
            logger.debug("inside auto...");
        }
        DeviceServiceImpl.getInstance().setAutoState(plugloadArr);

    } // end of method auto

}
