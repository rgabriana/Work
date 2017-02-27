/**
 * 
 */
package com.ems.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ButtonManipulationDao;
import com.ems.dao.ButtonMapDao;
import com.ems.dao.WdsDao;
import com.ems.dao.WdsModelTypeDao;
import com.ems.model.ButtonManipulation;
import com.ems.model.ButtonMap;
import com.ems.model.GemsGroupFixture;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.Wds;
import com.ems.model.WdsModelType;
import com.ems.server.ServerConstants;
import com.ems.server.discovery.DiscoverySO;

/**
 * @author yogesh
 * 
 */
@Service("wdsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsManager {
    static final Logger logger = Logger.getLogger("SwitchLogger");

    @Resource
    WdsDao wdsDao;
    
    @Resource
    WdsModelTypeDao wdsModelTypeDao;
    
    @Resource
    ButtonMapDao buttonMapDao;
    
    @Resource
    ButtonManipulationDao buttonManipulationDao;

    public String getNextWdsNo() {
        return wdsDao.getNextWdsNo();
    }
    
    public List<Wds> loadAllCommissionedWdsByGatewayId(Long secGwId) {
        List<Wds> wdsList = wdsDao.loadAllCommissionedWdsByGatewayId(secGwId);
        if (wdsList != null && !wdsList.isEmpty())
            return wdsList;
        return new ArrayList<Wds>();
    }

    public Wds getWdsSwitchById(Long wdsId) {
        return wdsDao.getWdsSwitchById(wdsId);
    }
    
    public Wds getWdsSwitchByName(String wdsname)
    {
    	return wdsDao.getWdsSwitchByName(wdsname);
    }

    public int getDiscoveryStatus() {
        int dStatus = DiscoverySO.getInstance().getWDSDiscoveryStatus();
        logger.debug("Discovery Status: " + dStatus);
        return dStatus;
    } // end of method getDiscoveryStatus

    public void cancelNetworkDiscovery() {
        DiscoverySO.getInstance().cancelNetworkDiscovery();
    }

    public int startNetworkDiscovery(Long floorId, Long gatewayId) {
    	if(logger.isDebugEnabled()) {
        logger.debug("startNetworkDiscovery for - " + floorId + " via gateway: " + gatewayId);
    	}
    	// Every time we start EWS discovery delete already discovered but uncommissioned EWSs
        deleteDiscoverdWds();
        // int ret = DiscoverySO.getInstance().startNetworkDiscovery(floorId, gatewayId, ServerConstants.DEVICE_SWITCH);
        int ret = DiscoverySO.getInstance().startSendingWdsDiscoveryPkts(floorId, gatewayId);
        logger.debug("Start Network Discovery Status: " + ret);
        return ret;
    }

    public int exitCommissioning(Long gatewayId) {
        int iUnCommissionedFixtures = 0;
        List<Wds> wdsList = getUnCommissionedWDSList(gatewayId);
        if (wdsList != null)
            iUnCommissionedFixtures = wdsList.size();
        logger.info("Exiting ERC commissioning process... GW (" + gatewayId + "), UnCommissioned ERC ("
                + iUnCommissionedFixtures + ")");
        // When Protocol implementation is done, return appropriate status
        return DiscoverySO.getInstance().finishWDSCommissioning(gatewayId, wdsList);
    }

    public List<Wds> getUnCommissionedWDSList(long gatewayId) {
        return wdsDao.getUnCommissionedWDSList(gatewayId);
    }

    public String getCommissioningStatus(long wdsId) {
        return wdsDao.getCommissioningStatus(wdsId);
    }

    public int getCommissioningStatus() {
        return DiscoverySO.getInstance().getCommissioningStatus();
    }

    public Wds getWdsSwitchBySnapAddress(String snapAddress) {
        return wdsDao.getWdsSwitchBySnapAddress(snapAddress);
    }

    public Wds AddWdsSwitch(Wds oWds) {
        return wdsDao.AddWdsSwitch(oWds);
    }

    public int commissionWds(Long floorId, Long gatewayId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling commissionERC API -- " + gatewayId);
        }
        int ret = DiscoverySO.getInstance().startWdsCommission(floorId, gatewayId);
        if (logger.isDebugEnabled()) {
            logger.debug("Done with commissionERC API");
        }
        return ret;
    }

    public List<Wds> loadAllWds() {
        return wdsDao.loadAllWds();
    }

    public List<Wds> loadWdsByCampusId(Long id) {
        return wdsDao.loadWdsByCampusId(id);
    }

    public List<Wds> loadWdsByBuildingId(Long id) {
        return wdsDao.loadWdsByBuildingId(id);
    }

    public List<Wds> loadWdsByFloorId(Long id) {
        return wdsDao.loadWdsByFloorId(id);
    }

    public List<Wds> loadWdsByAreaId(Long id) {
        return wdsDao.loadWdsByAreaId(id);
    }
    
    public List<Wds> loadCommissionedWdsBySwitchId(Long id) {
        return wdsDao.loadCommissionedWdsBySwitchId(id);
    }

    public List<Wds> loadNotAssociatedWdsBySwitchId(Long id) {
        return wdsDao.loadNotAssociatedWdsBySwitchId(id);
    }

    public Wds loadWdsById(Long id) {
        return (Wds) wdsDao.getObject(Wds.class, id);
    }

    public void updateState(Wds oWds) {
        wdsDao.updateState(oWds);
    }

    public void update(Wds oWds) {
        wdsDao.update(oWds);
    }

    public Wds updatePositionById(Wds oWds) {
        return wdsDao.updatePosition(oWds.getId(), oWds.getXaxis(), oWds.getYaxis());
    }
    
    public WdsModelType getWdsModelTypeById(Long id) {
    	return (WdsModelType) wdsModelTypeDao.getObject(WdsModelType.class, id);
    }
    
    public ButtonMap saveOrUpdateButtonMap(ButtonMap buttonMap) {
    	return (ButtonMap) buttonMapDao.saveObject(buttonMap); 
    }
    
    public ButtonManipulation saveOrUpdateButtonManipulation(ButtonManipulation buttonManipulation) {
    	return (ButtonManipulation) buttonManipulationDao.saveObject(buttonManipulation); 
    }
    
    public String deleteWds(Long id) {
    	Wds wds = loadWdsById(id);
    	ButtonManipulation buttonManipulation = buttonManipulationDao.getButtonManipulationByButtonMapId(wds.getButtonMap().getId());
    	buttonManipulationDao.removeObject(ButtonManipulation.class, buttonManipulation.getId());
    	buttonMapDao.removeObject(ButtonMap.class, wds.getButtonMap().getId());
    	wdsDao.removeObject(Wds.class, id);
    	return "S";
    }

    public String markWdsFroDeletion(Long id) {
        Wds oWds = loadWdsById(id);
        oWds.setState(ServerConstants.WDS_STATE_DELETED_STR);
        oWds.setAssociationState(ServerConstants.WDS_STATE_NOT_ASSOCIATED);
        updateState(oWds);
        return "S";
    }
    
    public void setImageUpgradeStatus(long wdsId, String status) {
      wdsDao.setImageUpgradeStatus(wdsId, status);
    } // end of method setImageUpgradeState
    
    public void updateVersion(String version, long id, long gwId) {
    	wdsDao.updateVersion(version, id, gwId);
    }
    
    /**
     * Only Discovered WDS are not useful, since the commissioning is transaction incase of WDS, it has to be discovery followed by commissioning
     * So we need to ensure that when the user goes in for Adding new WDS, cleanup the only disocvered WDS so that they won't show up automatically
     * in the UI. The user in this case will have to press the discovery first 
     */
    public void deleteDiscoverdWds() {
        List<Wds> wdsList = wdsDao.loadAllNonCommissionedWds();
        Iterator<Wds> oWdsitr = wdsList.iterator();
        while(oWdsitr.hasNext()) {
            Wds wds = oWdsitr.next();
            if (wds != null) {
                ButtonManipulation buttonManipulation = buttonManipulationDao.getButtonManipulationByButtonMapId(wds.getButtonMap().getId());
                if (buttonManipulation != null) {
                    buttonManipulationDao.removeObject(ButtonManipulation.class, buttonManipulation.getId());
                    buttonMapDao.removeObject(ButtonMap.class, wds.getButtonMap().getId());
                }
                wdsDao.removeObject(Wds.class, wds.getId());
            }
        }
    }

}
