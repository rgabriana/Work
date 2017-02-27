/**
 * 
 */
package com.ems.server.device.plugload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.PlugloadCache;
import com.ems.cache.PlugloadInfo;
import com.ems.model.EventsAndFault;
import com.ems.model.Gateway;
import com.ems.model.GemsGroup;
import com.ems.model.GemsGroupFixture;
import com.ems.model.GemsGroupPlugload;
import com.ems.model.MotionGroup;
import com.ems.model.MotionGroupPlugloadDetails;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.Switch;
import com.ems.model.SwitchGroup;
import com.ems.model.WeekdayPlugload;
import com.ems.server.GatewayInfo;
import com.ems.server.PerfSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.device.CommandScheduler;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayComm;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;
import com.ems.service.GemsPlugloadGroupManager;
import com.ems.service.MotionGroupManager;
import com.ems.service.PlugloadGroupManager;
import com.ems.service.PlugloadManager;
import com.ems.service.SwitchManager;
import com.ems.types.GGroupType;
import com.ems.types.MotionGroupOverrideType;

/**
 * @author sreedhar.kamishetti
 *
 */
public class PlugloadImpl {

	public static byte TX_POWER = 0;
	
	private static PlugloadImpl instance = null;
	
	private static Logger logger = Logger.getLogger("PlugloadLogger");
	private static Logger discLogger = Logger.getLogger("Discovery");
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");
		
  private GatewayManager gwMgr = null;  
  private PlugloadManager plugloadMgr = null;
  private PlugloadGroupManager groupMgr = null;
  private GemsPlugloadGroupManager gemsGrpMgr = null;
  private SwitchManager switchMgr = null;
  private MotionGroupManager motionGrpMgr = null;
  
  //thread pool for groups sync packets
  private int noOfGroupsSyncProcessThreads = 1;
  private EmsThreadPool groupsSyncProcessThPool = null;
    	
	/**
	 * 
	 */
	private PlugloadImpl() {
		
		gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
		groupMgr = (PlugloadGroupManager) SpringContext.getBean("plugloadGroupManager");
		
		gemsGrpMgr = (GemsPlugloadGroupManager) SpringContext.getBean("gemsPlugloadGroupManager");
		switchMgr = (SwitchManager) SpringContext.getBean("switchManager");
		motionGrpMgr = (MotionGroupManager) SpringContext.getBean("motionGroupManager");
		
		groupsSyncProcessThPool = new EmsThreadPool(noOfGroupsSyncProcessThreads, "GroupsSyncThread");
		
	}
	
	public static PlugloadImpl getInstance() {
		
		if (instance == null) {
			synchronized (PlugloadImpl.class) {
				if (instance == null) {
					instance = new PlugloadImpl();
				}
      }
		}
		return instance;
  
	} //end of method getInstance
		
	public void discoverPlugloads(long floorId, long gatewayId) {
		
    try {
    	Gateway gw = gwMgr.loadGateway(gatewayId);
    	if (gw != null) {
    		// if the gateway is not commissioned, don't allow to discover the nodes
    		if (!gw.isCommissioned()) {
    			return;
    		}
    		
    		DiscoveryReq req = new DiscoveryReq(gw.getSnapAddress(), ServerConstants.DEVICE_PLUGLOAD);
    		byte[] dataPkt = req.getByteArray();
    		discLogger.debug("data length - " + dataPkt.length);    		
    		byte[] header = DeviceServiceImpl.getInstance().getHeader(dataPkt.length, 
    				ServerConstants.CMD_PLUGLOAD_DISCOVER, "3.2.1", null);
    		header[1] = 0x12; //change the header to have device type
    		discLogger.debug("header length -- " + header.length);
    		byte[] pkt = new byte[header.length + dataPkt.length + 1];
    		System.arraycopy(header, 0, pkt, 0, header.length);
    		System.arraycopy(dataPkt, 0, pkt, header.length, dataPkt.length);
    		pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;
    		   		
    		if (discLogger.isDebugEnabled()) {
    			discLogger.debug("plugload discover req: " + ServerUtil.getLogPacket(pkt));
    		}
        GatewayComm.getInstance().sendNodeDataToGateway(gatewayId, gw.getIpAddress(), pkt);
        if (discLogger.isDebugEnabled()) {
        	discLogger.debug("after mcast of plugload discovery");
        }
    	}
    } catch (Exception ex) {
    	ex.printStackTrace();
    	discLogger.debug(ex.getMessage());
    }

	} // end of method discoverPlugloads

	class DiscoveryReq implements Serializable {
		
		private static final long serialVersionUID = 1420672609912364060L;

		String gwSnapAddress;
		byte deviceType;
		byte vendorType = 0;
		byte mntType = 0;
		byte behaviortype = 0;
		byte flags = 0;
		byte interGapMsgs = 12;
		byte totalPop = -52;
		byte reqLQI = 0;
		byte wiringTestResult = 0; 
		
		public DiscoveryReq(String gwSnap, byte devType) {
						
			gwSnapAddress = gwSnap;
			deviceType = devType;
			
		} //end of constructor
		
		public byte[] getByteArray () throws IOException {
			
			/* commented out as it is throwing notserializable exception
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      ObjectOutputStream o = new ObjectOutputStream(b);      
      o.writeObject(this);
      byte[] bytes = b.toByteArray();      
      return bytes; */
			
			byte[] reqPkt = new byte[12];
			int i = 0;						
			byte[] snapAddr = ServerUtil.getSnapAddr(gwSnapAddress);
      System.arraycopy(snapAddr, 0, reqPkt, i, snapAddr.length);
			i += 3;			
			reqPkt[i++] = deviceType;
			reqPkt[i++] = vendorType;
			reqPkt[i++] = mntType;
			reqPkt[i++] = behaviortype;
			reqPkt[i++] = flags;
			reqPkt[i++] = interGapMsgs;
			reqPkt[i++] = totalPop;
			reqPkt[i++] = reqLQI;
			reqPkt[i++] = wiringTestResult;
			return reqPkt;
      
		} //end of method getByteArray
		
	} //end of class DiscoveryReq
	
	class WirelessParamsReq {
		
		byte subType = 3; // to make the wireless param settings permanent
		byte channel;
		byte radioRate;
		short networkId;
		String wirelessKey;
		byte encryptType;
		byte isHopper = 0;
		byte hopCount = DeviceServiceImpl.DEFAULT_SU_HOP_COUNT;
		short timeDelay;
		byte txPower = DeviceServiceImpl.HOPPER_TX_POWER;
		byte ccaMode = 1;
		
		public WirelessParamsReq (int subType, int channel, int radioRate, int networkId, String key,
				short timeDelay, int encryptType) {
			
			this.subType = (byte)subType;
			this.channel = (byte)channel;
			this.radioRate = (byte)radioRate;
			this.networkId = (short)networkId;
			if(key.equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
				key = ServerConstants.DEF_WLESS_SECURITY_KEY;
			}
			this.wirelessKey = key;
			this.encryptType = (byte)encryptType;			
			this.timeDelay = timeDelay;
			
		} //end of constructor
		
		public byte[] getByteArray() {
			
			byte[] reqPkt = new byte[29];
			int i = 0;						
			reqPkt[i++] = subType;
			reqPkt[i++] = channel;
			reqPkt[i++] = radioRate;
			byte[] tempArr = ServerUtil.shortToByteArray(networkId);
			System.arraycopy(tempArr, 0, reqPkt, i, tempArr.length);
			i += 2;
			System.arraycopy(wirelessKey.getBytes(), 0, reqPkt, i, wirelessKey.length());
			i += wirelessKey.length();
			reqPkt[i++] = 0;
			reqPkt[i++] = encryptType;
			reqPkt[i++] = isHopper;
			reqPkt[i++] = hopCount;
			tempArr = ServerUtil.shortToByteArray(timeDelay);
			System.arraycopy(tempArr, 0, reqPkt, i, tempArr.length);
			i += 2;
			reqPkt[i++] = txPower;
			reqPkt[i++] = ccaMode;			
			return reqPkt;
      
		} //end of method getByteArray
		
	} //end of class WirelessParamsReq
	
	class OverrideProfileReq {
		
		byte checksum;
		PlugloadProfile override5;
		PlugloadProfile override6;
		PlugloadProfile override7;
		PlugloadProfile override8;
		
		OverrideProfileReq(PlugloadProfile override5, PlugloadProfile override6, PlugloadProfile override7, PlugloadProfile override8) {
		
			this.override5 = override5;
			this.override6 = override6;
			this.override7 = override7;
			this.override8 = override8;
			
		} //end of constructor
		
		public byte[] getByteArray() {
			
			byte[] override5ByteArr = new ProfileData(override5).getByteArray();
			byte[] override6ByteArr = new ProfileData(override6).getByteArray();
			byte[] override7ByteArr = new ProfileData(override7).getByteArray();
			byte[] override8ByteArr = new ProfileData(override8).getByteArray();
			
			byte[] profileData = new byte[override5ByteArr.length + override6ByteArr.length + 
			                              override7ByteArr.length + override8ByteArr.length + 1];
			
			checksum = ServerUtil.computeChecksum(override5ByteArr);
			checksum = ServerUtil.computeChecksum(checksum, override6ByteArr);
			checksum = ServerUtil.computeChecksum(checksum, override7ByteArr);
			checksum = ServerUtil.computeChecksum(checksum, override8ByteArr);
			
			int i = 0;
			profileData[i++] = checksum;
			System.arraycopy(override5ByteArr, 0, profileData, i, override5ByteArr.length);
			i += override5ByteArr.length;
			System.arraycopy(override6ByteArr, 0, profileData, i, override6ByteArr.length);
			i += override6ByteArr.length;
			System.arraycopy(override7ByteArr, 0, profileData, i, override7ByteArr.length);
			i += override7ByteArr.length;
			System.arraycopy(override8ByteArr, 0, profileData, i, override8ByteArr.length);
			i += override8ByteArr.length;
			
			return profileData;
			
		} //end of method getByteArray
		
	} //end of class OverrideProfileReq
	
	class ScheduledProfileReq {
		
		byte checksum;
		
		PlugloadProfile weekDayMorningProfile;
		PlugloadProfile weekDayDayProfile;
		PlugloadProfile weekDayEveningProfile;
		PlugloadProfile weekDayNightProfile;
		
		PlugloadProfile weekEndMorningProfile;
		PlugloadProfile weekEndDayProfile;
		PlugloadProfile weekEndEveningProfile;
		PlugloadProfile weekEndNightProfile;
		
		PlugloadProfile overrideMorningProfile;
		PlugloadProfile overrideDayProfile;
		PlugloadProfile overrideEveningProfile;
		PlugloadProfile overrideNightProfile;
		
		public ScheduledProfileReq(PlugloadProfileHandler prHand) {
			
			PlugloadProfile mornProfile = prHand.getMorningProfile();
	    PlugloadProfile dayProfile = prHand.getDayProfile();
	    PlugloadProfile evenProfile = prHand.getEveningProfile();
	    PlugloadProfile nightProfile = prHand.getNightProfile();    
	    
	    PlugloadProfile mornWeekEndProfile = prHand.getMorningProfileWeekEnd();
	    PlugloadProfile dayWeekEndProfile = prHand.getDayProfileWeekEnd();
	    PlugloadProfile evenWeekEndProfile = prHand.getEveningProfileWeekEnd();
	    PlugloadProfile nightWeekEndProfile = prHand.getNightProfileWeekEnd();    

	    PlugloadProfile mornHolProfile = prHand.getMorningProfileHoliday();
	    PlugloadProfile dayHolProfile = prHand.getDayProfileHoliday();
	    PlugloadProfile evenHolProfile = prHand.getEveningProfileHoliday();
	    PlugloadProfile nightHolProfile = prHand.getNightProfileHoliday();
	    
	    this.weekDayMorningProfile = mornProfile;
			this.weekDayDayProfile = dayProfile;
			this.weekDayEveningProfile = evenProfile;
			this.weekDayNightProfile = nightProfile;
			
			this.weekEndMorningProfile = mornWeekEndProfile;
			this.weekEndDayProfile = dayWeekEndProfile;
			this.weekEndEveningProfile = evenWeekEndProfile;
			this.weekEndNightProfile = nightWeekEndProfile;
			
			this.overrideMorningProfile = mornHolProfile;
			this.overrideDayProfile = dayHolProfile;
			this.overrideEveningProfile = evenHolProfile;
			this.overrideNightProfile = nightHolProfile;
	    
		} //end of constructor
		
		public byte[] getProfileData() {
			
			byte[] weekDayMorningByteArr = new ProfileData(weekDayMorningProfile).getByteArray();
			byte[] weekDayDayByteArr = new ProfileData(weekDayDayProfile).getByteArray();
			byte[] weekDayEveningByteArr = new ProfileData(weekDayEveningProfile).getByteArray();
			byte[] weekDayNigthByteArr = new ProfileData(weekDayNightProfile).getByteArray();
			
			byte[] weekEndMorningByteArr = new ProfileData(weekEndMorningProfile).getByteArray();
			byte[] weekEndDayByteArr = new ProfileData(weekEndDayProfile).getByteArray();
			byte[] weekEndEveningByteArr = new ProfileData(weekEndEveningProfile).getByteArray();
			byte[] weekEndNightByteArr = new ProfileData(weekEndNightProfile).getByteArray();
			
			byte[] overrideMorningByteArr = new ProfileData(overrideMorningProfile).getByteArray();
			byte[] overrideDayByteArr = new ProfileData(overrideDayProfile).getByteArray();
			byte[] overrideEveningByteArr = new ProfileData(overrideEveningProfile).getByteArray();
			byte[] overrideNightByteArr = new ProfileData(overrideNightProfile).getByteArray();
			
			byte[] profileData = new byte[weekDayMorningByteArr.length + weekDayDayByteArr.length + 
			                              weekDayEveningByteArr.length + weekDayNigthByteArr.length + 
			                              weekEndMorningByteArr.length + weekEndDayByteArr.length +
			                              weekEndEveningByteArr.length + weekEndNightByteArr.length +
			                              overrideMorningByteArr.length + overrideDayByteArr.length + 
			                              overrideEveningByteArr.length + overrideNightByteArr.length];
			int i = 0;			
			System.arraycopy(weekDayMorningByteArr, 0, profileData, i, weekDayMorningByteArr.length);
			i += weekDayMorningByteArr.length;
			System.arraycopy(weekDayDayByteArr, 0, profileData, i, weekDayDayByteArr.length);
			i += weekDayDayByteArr.length;
			System.arraycopy(weekDayEveningByteArr, 0, profileData, i, weekDayEveningByteArr.length);
			i += weekDayEveningByteArr.length;
			System.arraycopy(weekDayNigthByteArr, 0, profileData, i, weekDayNigthByteArr.length);
			i += weekDayNigthByteArr.length;
			
			System.arraycopy(weekEndMorningByteArr, 0, profileData, i, weekEndMorningByteArr.length);
			i += weekEndMorningByteArr.length;
			System.arraycopy(weekEndDayByteArr, 0, profileData, i, weekEndDayByteArr.length);
			i += weekEndDayByteArr.length;
			System.arraycopy(weekEndEveningByteArr, 0, profileData, i, weekEndEveningByteArr.length);
			i += weekEndEveningByteArr.length;
			System.arraycopy(weekEndNightByteArr, 0, profileData, i, weekEndNightByteArr.length);
			i += weekEndNightByteArr.length;
			
			System.arraycopy(overrideMorningByteArr, 0, profileData, i, overrideMorningByteArr.length);
			i += overrideMorningByteArr.length;
			System.arraycopy(overrideDayByteArr, 0, profileData, i, overrideDayByteArr.length);
			i += overrideDayByteArr.length;
			System.arraycopy(overrideEveningByteArr, 0, profileData, i, overrideEveningByteArr.length);
			i += overrideEveningByteArr.length;
			System.arraycopy(overrideNightByteArr, 0, profileData, i, overrideNightByteArr.length);
			i += overrideNightByteArr.length;			
			
			return profileData;
			
		} //end of method profileChecksum
		
		public byte[] getByteArray() {
			
			byte[] profileData = getProfileData();
			byte[] dataArr = new byte[profileData.length + 1];
			
			checksum = ServerUtil.computeChecksum(profileData);			
			int i = 0;
			dataArr[i++] = checksum;
			System.arraycopy(profileData, 0, dataArr, i, profileData.length);
			i += profileData.length;			
			return dataArr;		
			
		} //end of method getByteArray
		
	} //end of class ScheduledProfileReq
	
	class ProfileData {
		
		byte schedBehavior;
		int inOccTimeLimit;
		int manualStateTimeLimit;
		byte[] spare;
		
		public ProfileData(PlugloadProfile profile) {
					
			this.schedBehavior = (byte)profile.getMode();
			this.inOccTimeLimit = profile.getActiveMotion();
			this.manualStateTimeLimit = profile.getManualOverrideTime();
			
		} //end of constructor ProfileData
		
		public byte[] getByteArray() {
			
			byte reqPkt[] = new byte[16];
			int i = 0;			
			reqPkt[i++] = schedBehavior;
			byte[] tempShortArr = ServerUtil.shortToByteArray(inOccTimeLimit);
			System.arraycopy(tempShortArr, 0, reqPkt, i, tempShortArr.length);
			i += 2;
			tempShortArr = ServerUtil.shortToByteArray(manualStateTimeLimit);
			System.arraycopy(tempShortArr, 0, reqPkt, i, tempShortArr.length);
			i += 2;
			for(int j = 0; j < 11; j++) {
				reqPkt[i++] = 0;
			}
			return reqPkt;
			
		} //end of method getByteArray
 		
	} //end of class ProfileData

	private byte getWeekDaysAndWeekEnds(PlugloadProfileConfiguration prConfg) {
		
    char days[] = { '0', '0', '0', '1', '1', '1', '1', '1' };
    Set<WeekdayPlugload> oWeekDays = prConfg.getWeekDays();
    // Count starts from 1.
    if (oWeekDays != null) {
    	for (WeekdayPlugload weekDay : oWeekDays) {
    		if (weekDay.getDay().equals("Sunday")) {
    			if (weekDay.getType().equals("weekday")) {
    				days[1] = '1';
    			} else {
    				days[1] = '0';
    			}
    		} else if (weekDay.getDay().equals("Saturday")) {
    			if (weekDay.getType().equals("weekday")) {
    				days[2] = '1';
    			} else {
    				days[2] = '0';
    			}
    		} else if (weekDay.getDay().equals("Friday")) {
    			if (weekDay.getType().equals("weekend")) {
    				days[3] = '0';
    			} else {
    				days[3] = '1';
    			}
    		} else if (weekDay.getDay().equals("Thursday")) {
    			if (weekDay.getType().equals("weekend")) {
    				days[4] = '0';
    			} else {
    				days[4] = '1';
    			}
    		} else if (weekDay.getDay().equals("Wednesday")) {
    			if (weekDay.getType().equals("weekend")) {
    				days[5] = '0';
    			} else {
    				days[5] = '1';
    			}
    		} else if (weekDay.getDay().equals("Tuesday")) {
    			if (weekDay.getType().equals("weekend")) {
    				days[6] = '0';
    			} else {
    				days[6] = '1';
    			}
    		} else if (weekDay.getDay().equals("Monday")) {
    			if (weekDay.getType().equals("weekend")) {
    				days[7] = '0';
    			} else {
    				days[7] = '1';
    			}
    		}
    	}
    }
    String strDays = String.valueOf(days);
    return Byte.parseByte(strDays, 2);
    
	} //end of method getWeekDaysAndWeekEnds
	
	class GlobalProfileData {
		
		short morningStart;
		short dayStart;
		short eveningStart;
		short nightStart;
		byte weekDayBits;
		byte profileGroupId;
		byte flags;
		short initialOnTime;
		int drMap;
		byte safetyMode;
		short motionGrpHeartBeatFreq;
		byte motionGrpHbMisses;
		byte holidayLevel;
				
		public GlobalProfileData(PlugloadProfileHandler prHand) {
					
			morningStart = ServerUtil.convertProfileTimeToShort(prHand.getPlugloadProfileConfiguration().getMorningTime());
			dayStart = ServerUtil.convertProfileTimeToShort(prHand.getPlugloadProfileConfiguration().getDayTime());
			eveningStart = ServerUtil.convertProfileTimeToShort(prHand.getPlugloadProfileConfiguration().getEveningTime());
			nightStart = ServerUtil.convertProfileTimeToShort(prHand.getPlugloadProfileConfiguration().getNightTime());
			
			weekDayBits = getWeekDaysAndWeekEnds(prHand.getPlugloadProfileConfiguration());
			profileGroupId = prHand.getProfileGroupId().byteValue();
			flags = prHand.getProfileFlag();
			initialOnTime = prHand.getInitialOnTime().shortValue();
		
      drMap |= prHand.getDrHighLevel().intValue() << 13;
      drMap |= prHand.getDrModerateLevel().intValue() << 10;
      drMap |= prHand.getDrLowLevel().intValue() << 7;
      drMap |= prHand.getDrSpecialLevel().intValue() << 4;
      
      safetyMode = prHand.getSafetyMode().byteValue();
      motionGrpHeartBeatFreq = prHand.getHeartbeatInterval().shortValue();
      motionGrpHbMisses = prHand.getNoOfMissedHeartbeats().byteValue();
      holidayLevel = prHand.getHolidayLevel();
      					
		} //end of constructor ProfileData
		
		public byte[] getByteArray() {
						
      byte[] packet = new byte[32];
      int i = 0;      
      byte[] tempArr = ServerUtil.shortToByteArray(morningStart);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      tempArr = ServerUtil.shortToByteArray(dayStart);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      tempArr = ServerUtil.shortToByteArray(eveningStart);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      tempArr = ServerUtil.shortToByteArray(nightStart);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;

      packet[i++] = weekDayBits;
      packet[i++] = profileGroupId;
      packet[i++] = flags;
      tempArr = ServerUtil.shortToByteArray(initialOnTime);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      tempArr = ServerUtil.shortToByteArray(drMap);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      packet[i++] = safetyMode;
      tempArr = ServerUtil.shortToByteArray(motionGrpHeartBeatFreq);
      System.arraycopy(tempArr, 0, packet, i, tempArr.length);      
      i += 2;
      packet[i++] = motionGrpHbMisses;
      packet[i++] = holidayLevel;
         
      // pad with 15 bytes
      for (int j = 0; j < 15; j++) {
          packet[i++] = 0;
      }      
      return packet;
			
		} //end of method getByteArray
 		
	} //end of class GlobalProfileData
		
	public void setGlobalProfile(long plugloadId) {

    Plugload plugload = plugloadMgr.getPlugloadById(plugloadId);
    if (plugload == null) {        
        logger.error(plugloadId + ": There is no Plugload");
        return;
    }
    // Update the Global Profile flag
    plugloadMgr.enablePushGlobalProfileForPlugload(plugload);
    PlugloadProfileHandler pProfHand = plugloadMgr.getProfileHandlerByPlugloadId(plugloadId);
    GlobalProfileData gProfileData = new GlobalProfileData(pProfHand);

    byte[] gPrByteArr = gProfileData.getByteArray();
    byte[] profileByteArr = new byte[gPrByteArr.length + 1];
    System.arraycopy(gPrByteArr, 0, profileByteArr, 1, gPrByteArr.length);
    byte gChecksum = ServerUtil.computeChecksum(gPrByteArr);
    profileByteArr[0] = gChecksum;
    if(profileLogger.isDebugEnabled()) {
      profileLogger.debug(plugloadId + ": Sending Global profile, PFHID: " + pProfHand.getId() + " - chksum: ("
            + pProfHand.getGlobalProfileChecksum() + ") => " + ServerUtil.getLogPacket(profileByteArr));
    }
    CommandScheduler.getInstance().addCommand(plugload, profileByteArr, ServerConstants.SET_PROFILE_ADV_MSG_TYPE,
            true, DeviceServiceImpl.UNICAST_PKTS_DELAY);
    // sendPacket(fixture, ServerConstants.SET_PROFILE_ADV_MSG_TYPE, profileByteArr, true);
    // ServerUtil.sleepMilli(UNICAST_PKTS_DELAY);

} // end of method setGlobalProfile
	
	private void sendProfileToPlugload(long plugloadId) {
		
		if(profileLogger.isDebugEnabled()) {
			profileLogger.debug(plugloadId + ": inside the sendProfileToPlugload");
		}
    Plugload plugload = PlugloadCache.getInstance().getCachedPlugload(plugloadId);
    if (plugload == null) {
    	profileLogger.error(plugloadId + ": There is no Plugload");
    	return;
    }
    PlugloadProfileHandler prHand = plugloadMgr.getProfileHandlerByPlugloadId(plugloadId);
    plugloadMgr.enablePushProfileForPlugload(plugload);   
        
    PlugloadProfile override5 = prHand.getOverride5();
    PlugloadProfile override6 = prHand.getOverride6();
    PlugloadProfile override7 = prHand.getOverride7();
    PlugloadProfile override8 = prHand.getOverride8();

    PlugloadInfo device = PlugloadCache.getInstance().getDevice(plugload);
    //scheduled profile
    ScheduledProfileReq schedProfReq = new ScheduledProfileReq(prHand);
    byte[] dataPacket = schedProfReq.getByteArray();
    
    if(profileLogger.isDebugEnabled()) {
      profileLogger.debug(plugload.getId() + ": Sending scheduled profile, PFHID: " + prHand.getId()
            + " - chksum: (" + prHand.getProfileChecksum() + ") => " + ServerUtil.getLogPacket(dataPacket));
    }
    
    long seqNo = CommandScheduler.getInstance().addCommand(plugload, dataPacket, ServerConstants.SET_PROFILE_MSG_TYPE, 
    		true, DeviceServiceImpl.UNICAST_PKTS_DELAY);
    if(device != null) {
    	device.clearPushProfileFlag();
    	device.addProfileSeqNo(seqNo);
    }            
    ServerUtil.sleepMilli(DeviceServiceImpl.UNICAST_PKTS_DELAY);

    // override profile
    OverrideProfileReq overrideReq = new OverrideProfileReq(override5, override6, override7, override8);
    byte[] overrideByteArr = overrideReq.getByteArray();
        
    if(profileLogger.isDebugEnabled()) {
      profileLogger.debug(plugload.getId() + ": Sending override profile, PFHID: " + prHand.getId()
            + " - chksum: (" + prHand.getProfileChecksum() + ") => " + ServerUtil.getLogPacket(dataPacket));
    }
    
    seqNo = CommandScheduler.getInstance().addCommand(plugload, overrideByteArr, ServerConstants.SET_PROFILE_MSG_TYPE, 
    		true, DeviceServiceImpl.UNICAST_PKTS_DELAY);
    if(device != null) {              
    	device.addProfileSeqNo(seqNo);
    } 
    ServerUtil.sleepMilli(DeviceServiceImpl.UNICAST_PKTS_DELAY);
        
	} // end of method sendProfileToPlugload
	
	public void setApplyWirelessParams(Plugload plugload, Gateway gw) {
		      
	
		WirelessParamsReq req = new WirelessParamsReq(3, gw.getChannel(), gw.getWirelessRadiorate(), 
				gw.getWirelessNetworkId(), gw.getWirelessEncryptKey(), (short)15, gw.getWirelessEncryptType());
		byte[] dataPkt = req.getByteArray();
		CommandScheduler.getInstance().addCommand(plugload, dataPkt, ServerConstants.SU_SET_APPLY_WIRELESS_CMD, true, 
				DeviceServiceImpl.UNICAST_PKTS_DELAY);
		      
	} //end of method setApplyWirelessParams
	
	public void sendMulticastPacket(ArrayList<Plugload> plugloadsList, int msgType, byte[] dataPacket, 
			boolean retryReq, int sleepMillis) {

  	long gwId = plugloadsList.get(0).getSecGwId();
  	Gateway gw = gwMgr.loadGateway(gwId);
  	if (gw == null) {
  		// System.out.println(gwId + ": there is no gateway with the id");
  		logger.error(gwId + ": There is no Gateway");
  		return;
  	}

  	int noOfPlugloads = plugloadsList.size();
  	byte[] snapAddr = new byte[noOfPlugloads * 3];
  	Plugload plugload = null;
  	for (int i = 0; i < noOfPlugloads; i++) {
      plugload = plugloadsList.get(i);
      System.arraycopy(ServerUtil.getSnapAddr(plugload.getMacAddress()), 0, snapAddr, i * 3, 3);
  	}

  	byte[] header = DeviceServiceImpl.getInstance().getMulticastHeader(dataPacket.length, msgType, snapAddr);

  	byte[] packet = new byte[header.length + dataPacket.length + 1];
  	System.arraycopy(header, 0, packet, 0, header.length);
  	System.arraycopy(dataPacket, 0, packet, header.length, dataPacket.length);
  	packet[packet.length - 1] = ServerConstants.FRAME_END_MARKER;
  	if(logger.isDebugEnabled()) {
  		logger.debug("multicast packet: " + ServerUtil.getLogPacket(packet));
  	}
  	GatewayComm.getInstance().sendNodeDataToGateway(gwId, gw.getIpAddress(), packet);   
  	ServerUtil.sleepMilli(sleepMillis);

  } // end of method sendMulticastPacket
	
	public void nodeBootInfo(Plugload plugload, byte[] packet, long gwId) {

    if(logger.isDebugEnabled()) {
      logger.debug(plugload.getId() + ":node boot info packet - " + ServerUtil.getLogPacket(packet));
    }
    /*
     * typedef struct node_info { unsigned char g_profile_checksum; unsigned char s_profile_checksum; unsigned char
     * boot_loader_version_MJ; unsigned char boot_loader_version_MN; version_info_t version; unsigned char
     * is_bypass_on; resetReason reset_reason; plc_info_t plc_info; unsigned char zgwIP[3]; // zigbee gateway ID
     * 00.01.0c etc will modify PING to send it short calibValue; }node_info_t;
     */
      
    PlugloadInfo device = PlugloadCache.getInstance().getDevice(plugload);
    if (device != null) {
    	device.setBootTime(new Date());
    	device.setUptime(1);
    	//to be consistent with sensors, last seq no. is set to -2.
    	device.setLastStatsSeqNo(-2); 
    }
    //TODO setCurrentTime(plugload, 0);
    GatewayInfo gwInfo = ServerMain.getInstance().getGatewayInfo(gwId);
    if(gwInfo != null) {
    	Date lastUtcTime = gwInfo.getLastUtcTimeSent();
    	if((System.currentTimeMillis() - lastUtcTime.getTime() > 1 * 60 * 1000)) {
    		//TODO sendUTCTime(gwInfo.getGw());
    		gwInfo.setLastUtcTimeSent(new Date());
    	}
    }
    int pktIndex = ServerConstants.RES_CMD_PKT_MSG_START_POS;
      
    byte gProfileChecksum = packet[pktIndex++]; // 4th
    byte profileChecksum = packet[pktIndex++]; // 5th

    byte bootLoaderMajorVer = packet[pktIndex++]; // 6th
    byte bootLoaderMinorVer = packet[pktIndex++]; // 7th
    String bootLoaderVer = bootLoaderMajorVer + "." + bootLoaderMinorVer;
    
    if(logger.isDebugEnabled()) {
    	logger.debug(plugload.getId() + ": boot loader ver -- " + bootLoaderVer);
    }
    // if version mismatch schedule an image upload
    // 8 to 13 are version related
    byte releaseNo = packet[pktIndex++];
    byte[] revisionArr = { packet[pktIndex++], packet[pktIndex++] };
    short currRevision = (short) ServerUtil.byteArrayToShort(revisionArr);

    byte major = packet[pktIndex++];
    byte minor = packet[pktIndex++];
    byte appId = packet[pktIndex++];

    String currVersion = major + "." + minor + "." + releaseNo + " b" + currRevision;
    // pktIndex += 6;
    byte bypassOn = packet[pktIndex++]; // 14th
    byte resetReason = packet[pktIndex++]; // 15th
    // System.out.println("reset reason -- " + resetReason);
    if(logger.isInfoEnabled()) {
    	logger.info(plugload.getId() + ": reset reason -- " + resetReason);
    }
    // plc info
    pktIndex += 18;
    // zigbee network id
    pktIndex += 3;
    // calib value
    pktIndex += 2;
    String otherVersion = "Unknown";
    byte profileGroupId = 0;
    short otherRevision = 0;
    String cuVersion = "";
    int imgUpgrStatus = 0;
    byte otherMajorVer = packet[pktIndex++];
    byte otherMinorVer = packet[pktIndex++];
    otherVersion = otherMajorVer + "." + otherMinorVer;
    // cu version
    byte[] cuVerArr = { packet[pktIndex++], packet[pktIndex++] };
    cuVersion = "" + (short) ServerUtil.byteArrayToShort(cuVerArr);
    //image upgrade pending status
    imgUpgrStatus = packet[pktIndex++];
    // reserved
    pktIndex += 1;
    byte[] otherRevisionArr = { packet[pktIndex++], packet[pktIndex++] };
    otherRevision = (short) ServerUtil.byteArrayToShort(otherRevisionArr);
    // Fetch the profile group id from the SU.
    profileGroupId = packet[pktIndex++];
    // other version release no.
    otherVersion += "." + packet[pktIndex++];
    
    otherVersion += " b" + otherRevision;
    if(logger.isDebugEnabled()) {
    	logger.debug(plugload.getId() + ": other version -- " + otherVersion);
    }

    if(logger.isInfoEnabled()) {
    	logger.info(plugload.getId() + ": image(CurrVer= " + currVersion + ", OtherVer= " + otherVersion
    			+ ") booted to app" + appId);
    }
    byte[] tempShortByteArr = new byte[2];
    //byte 63, 64 groups checksum
    System.arraycopy(packet, pktIndex, tempShortByteArr, 0, tempShortByteArr.length);
    int groupsChecksum = ServerUtil.byteArrayToShort(tempShortByteArr);
    if(logger.isInfoEnabled()) {
    	logger.info(plugload.getId() + "(" + plugload.getGroupsSyncPending() + "): groups checksum -- " + groupsChecksum);
    }
    if(!plugload.getGroupsSyncPending()) {
    	//TODO DeviceServiceImpl.getInstance().handleGroupsSynchronization(fixture, groupsChecksum);
    }
    pktIndex += 2;
    
    //byte 65 is wireless test status
    pktIndex++;
    //byte 66 is hopper
    plugload.setIsHopper((int)packet[pktIndex++]);
    if(logger.isDebugEnabled()) {
    	logger.debug(plugload.getId() + ": hopper - " + plugload.getIsHopper());
    }
    String upgrStatus = ImageUpgradeSO.getInstance().nodeRebooted(plugload, currRevision, 
          otherRevision, appId, gwId, imgUpgrStatus, currVersion);
    String app2Version = currVersion;
    String app1Version = otherVersion;
     
    if (ServerUtil.compareVersion(currVersion, ServerMain.getInstance().getGemsVersion()) == 0) {
    	// reset the version synced flag.
    	plugload.setVersion(app2Version);
    	plugload.setVersionSynced(0);
    	plugloadMgr.updatePlugloadVersionSyncedState(plugload);
    } 
    /*
    try {
    	handleProfileMismatch(plugload, gProfileChecksum, profileChecksum, profileGroupId,
    			ServerConstants.NODE_INFO_MSG_TYPE);
    } catch (Exception e) {
    	profileLogger.error(fixture.getId() + ": error in handling profile mismatch");
    } 
    */
    //TODO plugload.setResetReason((short)resetReason);
    plugload.setVersion(app2Version);
    plugload.setBootLoaderVersion(bootLoaderVer);
    plugload.setFirmwareVersion(app1Version);
    plugload.setSecGwId(gwId);
    if(gwInfo != null) {
    	plugload.setGateway(gwInfo.getGw());
    }
    plugload.setCurrApp((short) appId);
    plugload.setCuVersion(cuVersion);        
    if(!upgrStatus.equals(ServerConstants.IMG_UP_STATUS_NOT_PENDING)) {
    	plugload.setUpgradeStatus(upgrStatus);
    }
    plugload.setLastConnectivityAt(new Date());
    plugloadMgr.updateBootInfo(plugload, upgrStatus);

  } // end of method nodeBootInfo
	
	/**
   * This will run in the context of the thread pool., this will handle the senarios where async events coming in
   * short burst are handled in controlled thread pool
   * 
   * @param plugload
   * @param gProfChecksum
   * @param sProfChecksum
   * @param profileGroupId
   * @param msgType
   */
  public void handleProfileMismatch(Plugload plugload, byte gProfChecksum, byte sProfChecksum, byte profileGroupId,
  		int msgType) {
  	
  	//if the profile sync work is submitted in the last 5 minutes, don't add now.
  	DeviceInfo device = PlugloadCache.getInstance().getDevice(plugload);    
  	if(device != null && (System.currentTimeMillis() - device.getLastProfileSyncTime().getTime() 
  			< PerfSO.FIVE_MINUTE_INTERVAL)) {
  		if(profileLogger.isInfoEnabled()) {
  			profileLogger.info(plugload.getId() + ": Ignoring the profile sync as it was scheduled in the last 5 minutes");
  	  }
  		return;
  	}
  	device.setLastProfileSyncTime(new Date(System.currentTimeMillis()));
  	PlugloadProfileSyncWork oProfileSyncWork = new PlugloadProfileSyncWork(plugload, gProfChecksum, sProfChecksum, profileGroupId,
              msgType);
  	DeviceServiceImpl.getInstance().addProfileSyncWork(oProfileSyncWork);  	
  	
  } //end of method handleProfileMismatch
  
  public byte calculateScheduledProfileChecksum(Plugload plugload) {
  	
    byte[] profileByteArr = null;    
    PlugloadProfileHandler prHand = plugloadMgr.getProfileHandlerByPlugloadId(plugload.getId());     
   	profileByteArr = new ScheduledProfileReq(prHand).getProfileData();         
    byte profileChecksum = ServerUtil.computeChecksum(profileByteArr);
    return profileChecksum;
    
  } // end of method calculateScheduledProfileChecksum
  
  public byte calculateGlobalProfileChecksum(Plugload plugload) {
  	
    byte[] profileByteArray = null;
    PlugloadProfileHandler prHand = plugloadMgr.getProfileHandlerByPlugloadId(plugload.getId());    
    profileByteArray = new GlobalProfileData(prHand).getByteArray();
    byte profileChecksum = ServerUtil.computeChecksum(profileByteArray);    
    return profileChecksum;
    
  } // end of method calculateGlobalProfileChecksum
  
  /**
   * Puts profile sync work in thread pool. Rather than in individual thread.
   */
  public class PlugloadProfileSyncWork implements Runnable {
  	
  	private Plugload plugload;
  	private byte gProfChecksum;
  	private byte sProfChecksum;
  	private byte profileGroupId;
  	private int msgType;

  	public PlugloadProfileSyncWork(Plugload plugload, byte gProfChecksum, byte sProfileChecksum, byte profileGroupId, int msgType) {
  		
  		this.plugload = plugload;
  		this.gProfChecksum = gProfChecksum;
  		this.sProfChecksum = sProfileChecksum;
  		this.profileGroupId = profileGroupId;
  		this.msgType = msgType;
  		
  	} //end of constructor
  	  	
  	public void run() {
    
  		try {
  			Thread.sleep(50);
  			initiateProfileSyncActivity(plugload, gProfChecksum, sProfChecksum, profileGroupId, msgType);
  		} catch (InterruptedException ie) {
  			if(profileLogger.isDebugEnabled()) {
  				profileLogger.debug("interrupted activity.");
  			}
  		}
  		
  	} //end of method run
  	
  	public void initiateProfileSyncActivity(Plugload oPlugload, byte gProfChecksum, byte sProfChecksum, byte profileGroupId,
  			int msgType) {
  	
  		// Plugload cache may get invalidated between the PMstat event and actually
  		// this function been called, we need to ensure that we get the latest updated plugload object in the cache. 
  		Plugload plugload =  PlugloadCache.getInstance().getDevicePlugload(oPlugload.getSnapAddress());
  		long plugloadId = plugload.getId();
  		String sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
  		String sMsgType = Integer.toHexString(msgType);
  		if (profileLogger.isDebugEnabled()) {
  			profileLogger.debug(plugloadId + ": (" + sMsgType + ") profile group Id from plugload -- " + profileGroupId);
  		}  		
  		
  		byte plugloadProfileNo = (byte) plugloadMgr.getProfileNoForPlugload(plugloadId);
  		if (profileGroupId != plugloadProfileNo) {
  			if (profileLogger.isDebugEnabled()) {
  				profileLogger.debug(plugloadId + ": (" + sMsgType + ") db groupId from plugload -- " + plugloadProfileNo);
  			}
  		}

  		if (profileLogger.isDebugEnabled()) {
  			profileLogger.debug(plugloadId + ": (" + sMsgType + ") profile checksum from plugload -- " + sProfChecksum);
  		}
  		byte calcSchedPrChecksum = calculateScheduledProfileChecksum(plugload);
  		if (sProfChecksum != calcSchedPrChecksum) {
  			if (profileLogger.isDebugEnabled()) {
  				profileLogger.debug(plugloadId + ": (" + sMsgType + ") calc schedvprofile checksum -- " + calcSchedPrChecksum);
  			}
  		}
  		if (profileLogger.isDebugEnabled()) {
  			profileLogger.debug(plugloadId + ": (" + sMsgType + ") global profile checksum from plugload -- " + gProfChecksum);
  		}
  		byte calcGlobalPrChecksum = calculateGlobalProfileChecksum(plugload);
  		if (gProfChecksum != calcGlobalPrChecksum) {
  			if (profileLogger.isDebugEnabled()) {
  				profileLogger.debug(plugloadId + ": (" + sMsgType + ") calculated global profile checksum -- " + calcGlobalPrChecksum);
  			}
  		}

  		// Check for the user action first, if this is still set then we have to
  		// push the profile to the SU.
  		if (plugload.isPushProfile() || plugload.isPushGlobalProfile()) { 			
  			if (profileLogger.isDebugEnabled()) {
  				profileLogger.debug(plugloadId + ": (" + sMsgType + ") Profile needs to be pushed to SU.");
  			}
  			sendProfileToPlugload(plugloadId);
  			setGlobalProfile(plugloadId);
  		} else {
  			// User push action is NOT set, now the verification begins...
  			// 1. Check Profile_no matches for the said fixture id  			
  			PlugloadGroups grps = groupMgr.getGroupById(plugload.getGroupId());

  			boolean bCheckProfileChecksum = true;
  			// profileGroupId != 0: indicates that the SU is currently
  			// associated with some group profile.
  			if (profileGroupId != 0) {
  				boolean bAssociationCheckRequired = true;
  				// If profile group id matches with fixture associated group's
  				// profile_no then next check for checksums
  				if (profileGroupId == grps.getProfileNo()) {
  					bAssociationCheckRequired = false;
  					bCheckProfileChecksum = false;
  					if ((sProfChecksum != calcSchedPrChecksum) && (gProfChecksum != calcGlobalPrChecksum)) {
  						if (profileLogger.isInfoEnabled()) {
  							profileLogger.info(plugloadId + ": (" + sMsgType + ") Group Id matches but checksum mismatches, needs user action on "
  											+ plugload.getName() + " - (" + plugload.getVersion() + ")");
  						}  						
  						if (ServerUtil.compareVersion(plugload.getVersion(), ServerMain.getInstance().getGemsVersion()) != 0) {
  							plugload.setVersionSynced(1);
  							plugloadMgr.updatePlugloadVersionSyncedState(plugload);
  						}
  					}
  				}

  				if (bAssociationCheckRequired) {  					
  					if (profileLogger.isDebugEnabled()) {
  						profileLogger.debug(plugloadId + ": (" + sMsgType + ") Check if Profile matches any of the groups profiles.");
  					}

  					Long groupId = plugloadMgr.assignGroupProfileToPlugload(plugloadId, profileGroupId);

  					if (groupId != 0L) {  						
  						if (profileLogger.isDebugEnabled()) {
  							profileLogger.debug(plugloadId + ": (" + sMsgType + ") associated with group. " + groupId.longValue());
  						}
  						bCheckProfileChecksum = false;
  					} else {
  						profileLogger.error(plugloadId + ": Profile should have matched one of the groups, but since it didn't we are " +
  								"going to download it based on matching the checksums with the fixture custom profile.");
  					}
  				}
  			}

  			// If profileGroupId==0 then it is custom profile. Now check whether
  			// the given profileGroupId is present in the
  			// custom_fixture_profile_group table
  			if (bCheckProfileChecksum) {
  				if ((sProfChecksum != calcSchedPrChecksum) || (gProfChecksum != calcGlobalPrChecksum)) {
  					if (profileLogger.isDebugEnabled()) {
  						profileLogger.debug(plugloadId + " *** Plugload profile checksum mismatched, needs download! *** ");
  					}  					
  				}
  			}
  		}
  		
  	} // end of method initiateProfileSyncActivity
  	
  } //end of class PlugloadProfileSyncWork
  
  public void setCurrentTime(Plugload plugload, int seqNo) {
    
    DeviceInfo device = null;
    try {            
    	byte[] dataPacket = new byte[0];
    	device = PlugloadCache.getInstance().getDevice(plugload);
    	if(device != null) {
    		device.setLastDateSyncSeqNo(seqNo);  
    		if(device.isLastDateSyncPending()) {
    			//there is already a time command pending on this fixture. so don't schedule
    			return;
    		}
    		device.setLastDateSyncPending(true);
    	}
    	CommandScheduler.getInstance().addCommand(plugload, dataPacket, ServerConstants.SET_CURRENT_TIME, true,
    			DeviceServiceImpl.UNICAST_PKTS_DELAY);
    } catch (Exception e) {
    	e.printStackTrace();
    }
    finally {
    	plugload = null;
    	device = null;
    }

  } // end of method setCurrentTime
  
  public int getPlugloadConfigChecksum(Plugload plugload) {
  	
  	int checksum = 0;
  	//get all the groups
  	//get the byte array of all the groups configuration
  	//calculate checksum for all the groups
  	List<GemsGroupPlugload> groupsList = gemsGrpMgr.getAllGroupsOfPlugload(plugload);
  	if(groupsList == null) {
  		return 0;
  	}
  	Iterator<GemsGroupPlugload> iter = groupsList.iterator();
  	GemsGroupPlugload gemsGrpPlugload = null;
  	GemsGroup group = null;
  	ByteArrayOutputStream output = new ByteArrayOutputStream();    
  	try {
  		while(iter.hasNext()) {
  			gemsGrpPlugload = iter.next();
  			group = gemsGrpPlugload.getGroup();
  			//System.out.println(fixture.getId() + ": group -- " + group.getGroupName());
  			SwitchGroup swGrp = switchMgr.getSwitchGroupByGemsGroupId(group.getId());    			
  			if(swGrp != null) { //it is a switch group
  				//get the group no
          int groupNo = Integer.parseInt(swGrp.getGroupNo().toString(), 16);    
          //System.out.println("inside getFixturecheck groupno-- " + ServerUtil.getLogPacket(ServerUtil.intToByteArray(groupNo)));
          output.write(ServerUtil.intToByteArray(groupNo));
  				Switch swObj = switchMgr.getSwitchByGemsGroupId(group.getId());
  				//get the switch configuration data
  				byte[] swConf = switchMgr.getSwitchGroupParamsData(swObj, plugload.getId());
  				//System.out.println("inside getFixturecheck switch -- " + ServerUtil.getLogPacket(swConf));
  				output.write(swConf);
  				//get the wds data
  				byte[] wdsConf = switchMgr.getSwitchGroupWdsParamsData(swObj);
  				//System.out.println("inside getFixturecheck wds-- " + ServerUtil.getLogPacket(wdsConf));
  				output.write(wdsConf);
  			} else { //not a switch group
  				MotionGroup motionGrp = motionGrpMgr.getMotionGroupByGemsGroupId(group.getId());
  				if(motionGrp != null) { //it is a motion group
  				//get the group no
  					int groupNo = Integer.parseInt(motionGrp.getGroupNo().toString(), 16);
	              //System.out.println("inside getFixturecheck motion groupno-- " + ServerUtil.getLogPacket(ServerUtil.intToByteArray(groupNo)));
	              output.write(ServerUtil.intToByteArray(groupNo));
	              MotionGroupPlugloadDetails mgfd = gemsGrpPlugload.getMotionGrpPlDetails();
	              if (mgfd != null) {
						if (mgfd.getUseEmValue().intValue() == MotionGroupOverrideType.EM
								.getType()) {
							output.write(mgfd.getType().byteValue());
							output.write(mgfd.getAmbientType().byteValue());
							output.write(mgfd.getUseEmValue().byteValue());
							output.write(ServerUtil.shortToByteArray(mgfd
									.getLoAmbValue()));
							output.write(ServerUtil.shortToByteArray(mgfd
									.getHiAmbValue()));
							output.write(ServerUtil.intToByteArray(mgfd
									.getTod()));
							output.write(mgfd.getLightLevel().byteValue());
						} else {
							output.write(mgfd.getType().byteValue());
							output.write(mgfd.getAmbientType().byteValue());
							output.write(mgfd.getUseEmValue().byteValue());
							output.write(ServerUtil.intToByteArray(0x00000000));
							output.write(mgfd.getLightLevel().byteValue());
						}
	              }
  				} else {
  					//TODO ignore motion bit groups to include in the checksum
//  					MotionBitsScheduler schedule = motionBitsMgr.loadMotionBitsScheduleByGemsGroupId(group.getId());
//  					byte[] motionBitsData = getMotionBitCommandData(schedule.getBitLevel().byteValue(), schedule
//                .getTransmitFreq().byteValue(), (byte) 1, getScheduleDate(schedule.getCaptureStart()), 
//                getScheduleDate(schedule.getCaptureEnd()) );
  				}
  			}
  		}
  		if(logger.isDebugEnabled()) {
  			logger.debug("groups data -- " + ServerUtil.getLogPacket(output.toByteArray()));
  		}
  		checksum = ServerUtil.checksum(0, output.toByteArray());
  	}
  	catch(IOException ioe) {
  		ioe.printStackTrace();
  	}    	
  	finally {
  		try {
  			output.close();
  		}
  		catch(Exception ex) { }
  	}
  	return checksum;
  	
  } //end of method getFixtureConfigChecksum
  
  public void handleGroupsSynchronization(Plugload plugload, int groupsChecksum) {
  	
  	int emChecksum = getPlugloadConfigChecksum(plugload);
  	//System.out.println("em checksum -- " + emChecksum);
  	if(logger.isDebugEnabled()) {
  		logger.debug(plugload.getId() + ": group checksum on plugload(" + groupsChecksum + ")" + " on em(" + emChecksum +")");
  	}
  	if(emChecksum == groupsChecksum) {
  		return;
  	}
  	//first get the detailed CRC checksums for all groups    	
  	try {
      byte[] dataPacket = new byte[0];        
      CommandScheduler.getInstance().addCommand(plugload, dataPacket, ServerConstants.SU_CMD_REQ_DETAIL_CONFIG_CRC_REQ, false, 
      		DeviceServiceImpl.UNICAST_PKTS_DELAY);        
  	} catch (Exception ex) {
  		//ex.printStackTrace();
      logger.error(plugload.getId() + ": error in detail crc cmd req- " + ex.getMessage());
  	}
  	
  } //end of method handleGroupsSynchronization
  
  public void receivedGroupChecksums(Plugload plugload, byte[] pkt) {
  	
  	if(logger.isDebugEnabled()) {
      logger.debug(plugload.getId() + ": groups checksum packet - " + ServerUtil.getLogPacket(pkt));
    }    	
  	PlugloadGroupsSyncWork groupsSyncWork = new PlugloadGroupsSyncWork(plugload, pkt);
  	groupsSyncProcessThPool.addWork(groupsSyncWork);
  	
  } //end of method receivedGroupChecksums
  
  /**
   * Puts groups sync work in thread pool. Rather than in individual thread.
   */
  public class PlugloadGroupsSyncWork implements Runnable {
  	
  	private Plugload plugload;
  	private byte[] checksumPkt;
 
  	public PlugloadGroupsSyncWork(Plugload plugload, byte[] pkt) {
  		
  		this.plugload = plugload;
  		this.checksumPkt = pkt;            
  	
  	} //end of constructor

  	public void run() {
    
  		try {
  			Thread.sleep(50);
  			//sync the groups
      	plugload.setGroupsSyncPending(true);
      	plugloadMgr.changeGroupsSyncPending(plugload);
  			initiateGroupsSynchronization(plugload, checksumPkt);
  		} catch (InterruptedException ie) {
  			if(logger.isDebugEnabled()) {
  				logger.debug("interrupted activity.");
  			}
  		}
  		catch(Exception ex) {
  			ex.printStackTrace();
  		}
  		finally {
  			plugload.setGroupsSyncPending(false);
      	plugloadMgr.changeGroupsSyncPending(plugload);
  		}
  		
  	} //end of method run
  	
  } //end of class GroupsSyncWork
  
  private int getSwitchGroupConfigChecksum(Integer groupNo, long plugloadId, SwitchGroup swGrp) {
  	
  	//System.out.println("group no. in switch group config checksum -- " + groupNo);    	    	
  	ByteArrayOutputStream output = new ByteArrayOutputStream();
  	try {
  		output.write(ServerUtil.intToByteArray(Integer.parseInt(groupNo.toString(), 16)));        
  		//System.out.println("inside switch group groupno-- " + ServerUtil.getLogPacket(ServerUtil.intToByteArray(groupNo)));
  		Switch swObj = switchMgr.getSwitchByGemsGroupId(swGrp.getGemsGroup().getId());   		
  		//get the switch configuration data
  		byte[] swConf = switchMgr.getSwitchGroupParamsData(swObj, plugloadId);
  		//System.out.println("inside switch group switch-- " + ServerUtil.getLogPacket(swConf));
  		output.write(swConf);
  		//get the wds data
  		byte[] wdsConf = switchMgr.getSwitchGroupWdsParamsData(swObj);
  		//System.out.println("inside switch group wds-- " + ServerUtil.getLogPacket(wdsConf));
  		output.write(wdsConf);
  		return ServerUtil.checksum(0, output.toByteArray());
  	}
  	catch(IOException ioe) {
  		ioe.printStackTrace();    		
  	}
  	finally {
  		try {
  			output.close();
  		}
  		catch(Exception ex) { }
  	}
  	return 0;
  	
  } //end of method getSwitchGroupConfigChecksum
  
  public void initiateGroupsSynchronization(Plugload plugload, byte[] pkt) {
  	
  	//System.out.println("initiated the group sync activity");
    int bytePos = ServerConstants.RES_CMD_PKT_MSG_START_POS;
    byte versionProtocol = pkt[bytePos++];
    byte status = pkt[bytePos++];
    int noOfBytes = pkt.length;
    while(bytePos < noOfBytes - 1) {
    	byte configType = pkt[bytePos++];
    	if(logger.isDebugEnabled()) {
    		logger.debug(plugload.getId() + ": group sync config type - " + configType);
    	}
    	switch(configType) {
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_GLOBAL_PR:
    		bytePos += 2;
    		break;
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_WEEKDAY_PR:
    		bytePos += 2;
    		break;
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_WEEKEND_PR:
    		bytePos += 2;
    		break;
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_HOLIDAY_PR:
    		bytePos += 2;
    		break;
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_MOTION_BITS:
    		bytePos += 2;
    		break;
    	case ServerConstants.GROUP_SYNC_CONFIG_TYPE_GROUPS:
    		List<GemsGroupPlugload> emGroupsList = gemsGrpMgr.getAllGroupsOfPlugload(plugload);
    		HashMap<Long, GemsGroupPlugload> groupsMap = new HashMap<Long, GemsGroupPlugload>();
    		
    		if (emGroupsList != null) {
    			Iterator<GemsGroupPlugload> groupsIter = emGroupsList.iterator();
    			while (groupsIter.hasNext()) {
    				GemsGroupPlugload plugGrp = groupsIter.next();
    				groupsMap.put(plugGrp.getGroup().getId(), plugGrp);
    			}
    		}
			
    		int noOfGroups = pkt[bytePos++];
    		for(int i = 0; i < noOfGroups; i++) {      			
    			int groupNo = Integer.parseInt(Integer.toHexString(ServerUtil.extractIntFromByteArray(pkt, bytePos)));      			
    			int suGroupNo = Integer.parseInt(Integer.toString(groupNo), 16);
    			//System.out.println("group no. -- " + groupNo);
    			bytePos += 4;
    			int checksum = ServerUtil.extractShortFromByteArray(pkt, bytePos);
    			bytePos += 2;
    			if(groupNo > 14000000 && groupNo < 15000000) {
    				//switch group      				
    				SwitchGroup swGrp = switchMgr.getSwitchGroupByGroupNo(groupNo);
    	    	if(swGrp == null) {
    	    		//there is no switch group for this fixture with this group no.
    	    		//so delete this group from SU
    	    		int[] plugArr = { plugload.getId().intValue() };
    	    		DeviceServiceImpl.getInstance().sendSUGroupCommand(plugArr, ServerConstants.SU_CMD_LEAVE_GRP, 
    	    				(byte)GGroupType.SwitchGroup.getId(), suGroupNo);
    	    		continue;
    	    	}
    	    	groupsMap.remove(swGrp.getGemsGroup().getId());
    				int swGrpChecksum = getSwitchGroupConfigChecksum(groupNo, plugload.getId(), swGrp);
    				if(logger.isDebugEnabled()) {
    					logger.debug(plugload.getId() + ": switch grp checksum on su(" + checksum + ") on em(" + swGrpChecksum + ")");
    				}
    				if(checksum != swGrpChecksum) {
    					//sync this group
    					long gemsGroupId = swGrp.getGemsGroup().getId();
    					long switchId = switchMgr.getSwitchByGemsGroupId(gemsGroupId).getId();
    					switchMgr.sendSwitchGroupParams(switchId, plugload.getId());
    					switchMgr.sendSwitchGroupWdsParams(switchId);
    				}
    			}
    			if(groupNo > 12000000 && groupNo < 13000000) {
    				//motion group      				
    				MotionGroup motionGrp = motionGrpMgr.getMotionGroupByGroupNo(groupNo);
    				if(motionGrp == null) {
    					//this is no motion group for this fixture with this group no
    					//so delete this group from SU
    					int[] fixArr = { plugload.getId().intValue() };
    	    		DeviceServiceImpl.getInstance().sendSUGroupCommand(fixArr, ServerConstants.SU_CMD_LEAVE_GRP, 
    	    				(byte)GGroupType.MotionGroup.getId(), suGroupNo);
    	    		continue;
    				}else
    				{
    				    boolean bLeaveGrp = false;
    					GemsGroupPlugload emGroup = gemsGrpMgr.getGemsGroupPlugload(motionGrp.getGemsGroup().getId(), plugload.getId());
    					if(emGroup!=null)
    					{
      					if(emGroup.getUserAction() == GemsGroupFixture.USER_ACTION_FIXTURE_DELETE) {
      					    bLeaveGrp = true;
      					}
    					}else {
    					    bLeaveGrp = true;
    					}
    					if (bLeaveGrp) {
    						if(logger.isInfoEnabled()) {
    							logger.info(plugload.getId() + ": motion group (" + groupNo + ") not available on EM");
    						}
    						//Motion group is present on EM but user_action is set to delete. so delete this group from SU
    						int[] plugArr = { plugload.getId().intValue() };
    						DeviceServiceImpl.getInstance().sendSUGroupCommand(plugArr, ServerConstants.SU_CMD_LEAVE_GRP, 
    								(byte)GGroupType.MotionGroup.getId(), suGroupNo);
    						//Also delete the group from EM Side
    						gemsGrpMgr.deleteGemsGroups(motionGrp.getGemsGroup().getId());
    						continue;
    					}
    				}
    				groupsMap.remove(motionGrp.getGemsGroup().getId());
    				int motionGrpChecksum = ServerUtil.checksum(0, ServerUtil.intToByteArray(Integer.parseInt(new Integer(groupNo).toString(), 16)));
    				if(checksum != motionGrpChecksum) {
    					logger.error(plugload.getId() + ": motion group (" + groupNo + ") checksum on su (" + checksum + "), on em(" 
    							+ motionGrpChecksum + ")");
    				}
    			}
    		}      		
    		//sync the groups in which sus is not part of
    		Iterator<Long> additionalGrpsIter = groupsMap.keySet().iterator();
    		Long gemsGrpId;
    		while(additionalGrpsIter.hasNext()) {
    			gemsGrpId = additionalGrpsIter.next();
    			if(logger.isDebugEnabled()) {
    				logger.debug(plugload.getId() + ": missing group on su " + gemsGrpId);
    			}
    			SwitchGroup swGrp = switchMgr.getSwitchGroupByGemsGroupId(gemsGrpId);
  	    	if(swGrp == null) {
  	    		//it is not a switch group try motion group
  	    		MotionGroup motionGrp = motionGrpMgr.getMotionGroupByGemsGroupId(gemsGrpId);
    				if(motionGrp == null) {
    					//it is not a motion group
    					//TODO cannot sync this      		
    					continue;
    				}
    				//join the group
    				if(logger.isDebugEnabled()) {
    					logger.debug(plugload.getId() + ": syncing the motion group - " + gemsGrpId);
    				}
    				int[] plugArr = { plugload.getId().intValue() };
    				DeviceServiceImpl.getInstance().sendSUGroupCommand(plugArr, ServerConstants.SU_CMD_JOIN_GRP, 
    						(byte)GGroupType.MotionGroup.getId(), Integer.parseInt(motionGrp.getGroupNo().toString(), 16));
    				continue;
  	    	}
  	    	if(logger.isDebugEnabled()) {
  	    		logger.debug(plugload.getId() + ": syncing the switch group - " + gemsGrpId);
  	    	}
  	    	//join the group
  	    	int iStatus = gemsGrpMgr.assignPlugloadToGroup(plugload, ServerConstants.SU_CMD_JOIN_GRP, (byte)GGroupType.SwitchGroup.getId(), 
  	    			Integer.parseInt(swGrp.getGroupNo().toString(), 16), gemsGrpId);    				
  				if (iStatus == ServerConstants.SU_ACK) { //got the ack           
            //send switch configuration command
    				//long gemsGroupId = swGrp.getGemsGroup().getId();
  					long switchId = switchMgr.getSwitchByGemsGroupId(gemsGrpId).getId();
  					switchMgr.sendSwitchGroupParams(switchId, plugload.getId());
  					//send wds configuration command
  					switchMgr.sendSwitchGroupWdsParams(switchId);   					
  				} else {
  					logger.error(plugload.getId() + " with gems group " + gemsGrpId + ": unable to send switch configuration");
  				}    	    	 	    	
    		}
    		break;
    	default:
    		//do nothing
    	}
    }
    
  } //end of method initiateGroupsSynchronization

} //end of class PlugLoadImpl
