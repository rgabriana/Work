/**
 * 
 */
package com.ems.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ButtonManipulationDao;
import com.ems.dao.ButtonMapDao;
import com.ems.dao.EventsAndFaultDao;
import com.ems.dao.SystemConfigurationDao;
import com.ems.dao.WdsDao;
import com.ems.dao.WdsModelTypeDao;
import com.ems.model.ButtonManipulation;
import com.ems.model.ButtonMap;
import com.ems.model.EventsAndFault;
import com.ems.model.Switch;
import com.ems.model.SystemConfiguration;
import com.ems.model.Wds;
import com.ems.model.WdsModelType;
import com.ems.server.ServerConstants;
import com.ems.server.discovery.DiscoverySO;
import com.ems.types.ERCBatteryLevel;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;
import com.ems.vo.model.WdsList;

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
    
    @Resource
    private SystemConfigurationDao systemConfigurationDao;
    
    @Resource
    private EventsAndFaultDao eventsAndFaultDao;
    
    @Resource
    SwitchManager switchManager;
    
    @Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
    
    @Resource(name = "emailManager")
   	EmailManager emailManager ;
    
    private boolean isRunning = false;
    
    /**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * @param isRunning the isRunning to set
	 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	public void sendErcBatteryReportEmail() {
		try {
			setRunning(true);
			//System.out.println("In sendErcBatteryReportEmail");
			String receipientEmail = "";
			String emailSubject = "ERC Battery Report";
			String emailMessage = "Please find the ERC battery report as an attachment.";
			
			SystemConfiguration ercBatteryReportSchedulerEnable = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.enable");
			if(ercBatteryReportSchedulerEnable!=null)
			{
				if("true".equalsIgnoreCase(ercBatteryReportSchedulerEnable.getValue())){
					SystemConfiguration ercBatteryReportSchedulerEmail = systemConfigurationManager.loadConfigByName("erc.batteryreportscheduler.email");
					if(ercBatteryReportSchedulerEmail!=null)
					{
						receipientEmail = ercBatteryReportSchedulerEmail.getValue();
						
						String criticalWdsDataCsv = getCriticalWdsDataCSV();
						
						if(!"".equalsIgnoreCase(criticalWdsDataCsv)){
							InputStream criticalWdsDataIS = new ByteArrayInputStream(criticalWdsDataCsv.getBytes());
							EmailManager.EmailDTO dto = new EmailManager.EmailDTO();
							dto.setCommaSeperatedRecipientList(receipientEmail);
							dto.setHtmlMessage(emailMessage);
							dto.setHtmlSubject(emailSubject);
							final EmailManager.EmailDTO.AttachmentInfo attachment = new EmailManager.EmailDTO.AttachmentInfo();
							attachment.setFileName("ErcBatteryReport.csv");
							attachment.setMimeType("text/csv");
							attachment.setFileInputStream(criticalWdsDataIS);
							dto.getAttachments().add(attachment);
							
							emailManager.doSendEmail(dto);
						}
						
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally {
			setRunning(false);
		}
	}
    
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
    
    private EventsAndFault createNewEventsAndFault(Wds oWds)
    {
        EventsAndFault eventAndFault = new EventsAndFault();
        eventAndFault.setDevice(oWds);
        eventAndFault.setEventType(EventsAndFault.ERC_BATTERY_LEVEL_EVENT);
        eventAndFault.setActive(true);
        return eventAndFault;
    }
    
    public void updateBatteryLevel(Wds oWds,int batteryVolt)
    {
        Date now = new Date();
        oWds.setVoltageCaptureAt(now);
        oWds.setBatteryVoltage(batteryVolt);
        update(oWds);
        EventsAndFault eventAndFault =  eventsAndFaultDao.getUnresolvedEventsAndFaultByERCId(oWds.getId());
        List<Wds> wdsList = new ArrayList<Wds>();
        wdsList.add(oWds);
        setWDSBatteryLevel(wdsList);
        
        if(ERCBatteryLevel.Low.getName().equals(oWds.getBatteryLevel()))
        {
            if(eventAndFault==null)
            {
                eventAndFault = createNewEventsAndFault(oWds);
            }
            eventAndFault.setEventTime(now);
            eventAndFault.setSeverity(EventsAndFault.MINOR_SEV_STR);
            eventAndFault.setDescription(EventsAndFault.ERC_LOW_BATTERY_LEVEL_DESC);
            eventsAndFaultDao.saveOrUpdateEvent(eventAndFault);
        }
        else if(ERCBatteryLevel.Critical.getName().equals(oWds.getBatteryLevel()))
        {
            if(eventAndFault==null)
            {
                eventAndFault =  createNewEventsAndFault(oWds);
            }
            eventAndFault.setEventTime(now);
            eventAndFault.setSeverity(EventsAndFault.MAJOR_SEV_STR);
            eventAndFault.setDescription(EventsAndFault.ERC_CRITICAL_BATTERY_LEVEL_DESC);
            eventsAndFaultDao.saveOrUpdateEvent(eventAndFault);
        }
        else if(ERCBatteryLevel.Normal.getName().equals(oWds.getBatteryLevel()))
        {
            if(eventAndFault!=null)
            {
                eventsAndFaultDao.resolveAlarms(oWds.getId(), EventsAndFault.ERC_BATTERY_LEVEL_EVENT);
            }
        }
        
    }
    public void setWDSBatteryLevel(List<Wds> wdsList)
	{
    	int normalMin = 0,lowMin = 0;
	    String val = systemConfigurationDao.loadConfigByName("wds.normal.level.min").getValue();
	    if(val!=null)
	    {
	    	normalMin = Integer.parseInt(val);
	    }
	    val = systemConfigurationDao.loadConfigByName("wds.low.level.min").getValue();
	    if(val!=null)
	    {
	    	lowMin = Integer.parseInt(val);
	    }
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date now = new Date();
		for(Wds wds: wdsList)
		{
			setWDSBatteryLevel(wds,normalMin, lowMin,now,sdf);
		}
			
	}
    
    public void setWDSBatteryLevel(Wds wds,int normalMin, int lowMin,Date now,SimpleDateFormat sdf)
    {
    	Date date = wds.getVoltageCaptureAt();
		if(date!=null)
		{
			Long dateDiffInMinutes = DateUtil.getDateDiffInMinutes(date,now);
			//String captureAt = sdf.format(date) + " ("+dateDiffInMinutes.longValue() + " mins ago)";
			String captureAt = getIntervalString(dateDiffInMinutes*60*1000);
			wds.setCaptureAtStr(captureAt);
		}
		if(wds.getBatteryVoltage()==null)
		{
			wds.setBatteryLevel("Unknown");
			wds.setCaptureAtStr("NA");
		}
		else
		{
			if(wds.getBatteryVoltage() >= normalMin)
			{
				wds.setBatteryLevel(ERCBatteryLevel.Normal.getName());
			}
			else if(wds.getBatteryVoltage() >= lowMin)
			{
				wds.setBatteryLevel(ERCBatteryLevel.Low.getName());
			}
			else if(wds.getBatteryVoltage() < lowMin)
			{
				wds.setBatteryLevel(ERCBatteryLevel.Critical.getName());
			}
			
			if(ERCBatteryLevel.Critical.getName().equals(wds.getBatteryLevel())){
				//Sest the switch group also to AutoOn/AutoOff mode if it is earlier set to ManualOn/AutoOff mode..
				//First find switch group associated with the ERC if any.
				final Long swId = wds.getSwitchId();
				if(swId != null){
					final Switch sw = switchManager.getSwitchById(swId);
					if(sw.getModeType() == 1){
						sw.setModeType((short)0);
						sw.setForceAutoMode((short)1);
					}
					switchManager.saveSwitch(sw);
				}
			}else{
				//check if the switch mode has been set from ManualOn/AutoOff to AutoOn/AutoOff. Some how needs to set a flag in above code.
				final Long swId = wds.getSwitchId();
				if(swId != null){
					final Switch sw = switchManager.getSwitchById(swId);
					if(sw.getModeType() == 0 && sw.getForceAutoMode() == 1){
						sw.setModeType((short)1);
						sw.setForceAutoMode((short)0);
					}
					switchManager.saveSwitch(sw);
				}
			}
			
		}
    }
    
    private String getIntervalString(long millis)
    {
    	if (millis <= 0) {
			return "0 sec ago";
		}

		long x = millis / 1000;
    	long seconds = x % 60;
		x /= 60;
    	long minutes = x % 60;
		x /= 60;
    	long hours = x % 24;
		x /= 24;
    	long days = x;
    	
		if (days > 0) {
			if (hours > 0) {
				return days + " days " + hours + " hrs ago";
			} else {
				return days + " days ago";
			}
		} else if (hours > 0) {
			if (minutes > 0) {
				return hours + " hrs " + minutes + " min ago";
			} else {
				return hours + " hrs ago";
			}
		} else if (minutes > 0) {
			if (seconds > 0) {
				return minutes + " min " + seconds + " sec ago";
			} else {
				return minutes + " sec ago";
			}
		} else {
			return seconds + " sec ago";
		}
    }
    
    public WdsList loadWdsList(String orderBy,String orderWay,int offset, int limit){
    	return wdsDao.loadWdsList(orderBy,orderWay,offset, limit);
    }
    
    public String getWdsDataCSV(){
    	StringBuffer output = new StringBuffer("");

		List<Wds> wdsList = loadAllWds();
        
	    output.append("ERC Report\r\n\n");
	    
	    
        output.append("ERC Name"
                + ","
                + "Location"
                + ","
                + "Battery Level"
                + ","
                + "Last Reported Time");
        
        if(!ArgumentUtils.isNullOrEmpty(wdsList)){
        	int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        
        	for (int i = 0; i < wdsList.size(); i++) {
                Wds wds = wdsList.get(i);
                output.append("\r\n");
                setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
                String name = wds.getName();
                String location = wds.getLocation();
                String batteryLevel = wds.getBatteryLevel();
                String captureAtStr = wds.getCaptureAtStr();
                output.append(name
                        + ","
                        + location
                        + ","
                        + batteryLevel
                        + ","
                        + captureAtStr
                        );
            }
        }
		return output.toString();
    }
    
    public String getCriticalWdsDataCSV(){
    	StringBuffer output = new StringBuffer("");

		List<Wds> wdsList = loadAllWds();
		
        if(!ArgumentUtils.isNullOrEmpty(wdsList)){
        	int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        int criticalErcBatteryLevelCount = 0;
	        
        	for (int i = 0; i < wdsList.size(); i++) {
                Wds wds = wdsList.get(i);
                setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
                String name = wds.getName();
                String location = wds.getLocation();
                String batteryLevel = wds.getBatteryLevel();
                String captureAtStr = wds.getCaptureAtStr();
                if(ERCBatteryLevel.Critical.getName().equals(batteryLevel)){
                	
                	if(criticalErcBatteryLevelCount == 0){
                		output.append("ERC Report\r\n\n");
                	    
                	    
                        output.append("ERC Name"
                                + ","
                                + "Location"
                                + ","
                                + "Battery Level"
                                + ","
                                + "Last Reported Time");
                        
                   }
                	
                	output.append("\r\n");
                	
                	output.append(name
                            + ","
                            + location
                            + ","
                            + batteryLevel
                            + ","
                            + captureAtStr
                            );
                	criticalErcBatteryLevelCount++;
                }
            }
        }
		return output.toString();
    }

}
