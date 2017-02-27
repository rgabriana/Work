package com.ems.service;

import java.util.ArrayList;
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
import com.ems.model.Gateway;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfileHandler;
import com.ems.server.ServerConstants;
import com.ems.server.device.plugload.PlugloadImpl;
import com.ems.server.discovery.DiscoverySO;
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

	public void test() {
		plugloadDao.loadAllPlugloads();
	}

	public List<Plugload> loadPlugloadByFloorId(Long pid) {
		return plugloadDao.loadPlugloadByFloorId(pid);
	}

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

	/**
	 * save Plugload details.
	 * 
	 * @param plugload
	 *            com.ems.model.Plugload
	 */
	public Plugload save(Plugload plugload) {
		return (Plugload) plugloadDao.saveObject(plugload);
	}

	public void discoverPlugloads(long floorId, long gwId) {

		PlugloadImpl.getInstance().discoverPlugloads(floorId, gwId);

	} // end of method discoverPlugloads

	public List<Plugload> getUnCommissionedPlugloadList(long gatewayId) {

		return plugloadDao.getUnCommissionedPlugloadList(gatewayId);

	}

	public void updateCommissionStatus(int[] fixtureIds, int status) {

		plugloadDao.updateCommissionStatus(fixtureIds, status);

	}

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

	public void enablePushGlobalProfileForPlugload(Plugload plugload) {

		if (plugload != null) {
			plugloadDao.enablePushGlobalProfileForPlugload(plugload.getId());
			plugload.setPushGlobalProfile(true);
		}

	} // end of method enablePushGlobalProfileForPlugload

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
				// TODO:
				/*
				 * Thread oSWFDThread = new Thread(plugload.getName() + "SWFD")
				 * { public void run() {
				 * DeviceServiceImpl.getInstance().setWirelessFactoryDefaults
				 * (plugload, true); } }; oSWFDThread.start(); try {
				 * oSWFDThread.join(); } catch (InterruptedException ie) {
				 * logger.warn(id + ": interrupted!"); } if
				 * (DeviceServiceImpl.getInstance
				 * ().getSuWirelessChangeAckStatus(plugload) == false) { iStatus
				 * = 2; }
				 */
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
	
	public void enablePushProfileAndGlobalPushProfile(Long plugloadId, boolean pushProfileStatus, boolean globalPushProfileStatus) {
		
		plugloadDao.enablePushProfileAndGlobalPushProfile(plugloadId, pushProfileStatus, globalPushProfileStatus);
		Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
		if(plugload != null) {
			plugload.setPushProfile(pushProfileStatus);
			plugload.setPushGlobalProfile(globalPushProfileStatus);
		}	
	
	} //end of method enablePushProfileAndGlobalPushProfile
	
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
    
    public int commissionPlugload(Long plugloadId, Long gwId) {
    	if (DiscoverySO.getInstance().commissionPlugload(plugloadId, gwId) == true) {
    		return 0;
    	}
    	return 1;
    }
    
    public void pushPlugloadProfileToPlugloadNow(Long plugloadId) {
        // PUSHING REAL TIME Plugload PROFILE TO Plugload
        Plugload oPlugload = getPlugloadById(plugloadId);
        PlugloadGroups plugloadGroups = plugloadGroupDao.getGroupById(oPlugload.getGroupId());
        //DeviceServiceImpl.getInstance().sendCurrentPlugloadProfile(plugloadGroups.getPlugloadProfileHandler(), plugloadId.intValue()); //todo
        //DeviceServiceImpl.getInstance().setGlobalPlugloadProfile(plugloadId); //todo
    }
    

}