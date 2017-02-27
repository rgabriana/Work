package com.ems.su;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.LampCaliberationFrame;
import com.ems.commands.MotionBitPkt;
import com.ems.commands.PMHeartbeatStatFrame;
import com.ems.commands.PMStatFrame;
import com.ems.commands.NodeInfoFrame;
import com.ems.commands.SUAckFrame;
import com.ems.commands.SUDiscoveryFrame;
import com.ems.commands.SUEventPkt;
import com.ems.commands.SURealTimeFrame;

import com.ems.commands.imgupgrade.SuImageUpgrade;
import com.ems.commands.profile.DownloadProfileFrame;
import com.ems.commands.profile.Profile;
import com.ems.constants.EnergyStatsConstant;
import com.ems.db.SimDBHelper;
import com.ems.gw.GWInterface;
import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;

/**
 * @author SAMEER SURJIKAR
 * 
 * 
 */
public class SensorUnit {
    public Logger oLogger = Logger.getLogger(SensorUnit.class.getName());
    String sName;
    int delay = Utils.getDelay();
    int mbitDelay = 1000 * 60;
    private boolean keepRunning = false;
    private CommonQueue commonqueue;
    long m_lastTxnId = 0;
    private int pmStatsSeqNo = 1;
    Random randomGenerator = new Random();
    Thread t;
    long startTime; 
    byte currentApp;
    public volatile Thread blinker, mbitThread, fxHBThread, pmHBThread;
    
    private Profile oProfile = null;

    // mode
    private byte mode = 2;
    private byte hbTriggerMask = 1;
    
    // bad cu reading
    private boolean badCUReading = false;
    
    public Timer badCUTimer = null;
    
    // light level
    int lightLevel = 75;
    int manualModeTime = 0;
    
    // lamp calibration curve response
    int lccdelay = 140; // seconds
    
    // Num of groups
    int iNumOfGroups = 0;
    
    private SimDBHelper m_dbHelperInstance = SimDBHelper.getInstance();
    
    private ImageUpgradeSU imageUpgrade = new ImageUpgradeSU(this);

	private int utc_time;
	private int localMins;
	private long setTime;
    
    public SensorUnit(String name) {
        sName = name;
        oProfile = new Profile(sName);
        m_dbHelperInstance.addSU(this);
        startTime = System.currentTimeMillis();
        currentApp = 2;
    }

    public String getsName() {
        return sName;
    }

    public int getNextPmStatsSeqNo() {
        return pmStatsSeqNo++;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public void assignSensortoGatewy(GWInterface gw) {
        commonqueue = gw.getGatewayQueue();
    }

    public CommonQueue getCommonqueue() {
        return commonqueue;
    }

    public void setCommonqueue(CommonQueue commonqueue) {
        this.commonqueue = commonqueue;
    }
    
    public Profile getProfile() {
        return oProfile;
    }

    // Two ways to start sensors.
    public void startSensor(CommonQueue cq) {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, cq), "PM_" + sName);
        blinker.start();
        if (Utils.getiEnabledMBits() == 1) {
            mbitThread = new Thread(new GenerateMotionBitData(this, cq), "MBIT_"+ sName);
            mbitThread.start();
        }
        if (Utils.getiFxStatHB() > 0) {
            fxHBThread = new Thread(new GenerateFxStatsHB(this), "FXHB_"+ sName);
            fxHBThread.start();
        }
        if (Utils.getIpmstathb() > 0) {
            pmHBThread = new Thread(new GeneratePmStatsHB(this), "PMHB_"+ sName);
            pmHBThread.start();
        }
    }

    public void startSensor() {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, this.commonqueue), "PM_" + sName);
        blinker.start();
        if (Utils.getiEnabledMBits() == 1) {
            mbitThread = new Thread(new GenerateMotionBitData(this, this.commonqueue), "MBIT_"+ sName);
            mbitThread.start();
        }
        if (Utils.getiFxStatHB() > 0) {
            fxHBThread = new Thread(new GenerateFxStatsHB(this), "FXHB_"+ sName);
            fxHBThread.start();
        }
        if (Utils.getIpmstathb() > 0) {
            pmHBThread = new Thread(new GeneratePmStatsHB(this), "PMHB_"+ sName);
            pmHBThread.start();
        }
    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public Thread getKeepRunning() {
        return this.blinker;
    }

    public Thread getKeepMBitRunning() {
        return this.mbitThread;
    }

    public Thread getFxHBRunning() {
        return this.fxHBThread;
    }
    
    public Thread getPmHBRunning() {
        return this.pmHBThread;
    }

    public void stopSensor() {
        keepRunning = false;
        blinker = null;
        mbitThread = null;
        fxHBThread = null;
    }

    public void on() {
        startSensor();
    }

    public void off() {
        stopSensor();

    }

    public void postDiscovery(long txnId) {
        if (m_lastTxnId == txnId)
            return;

        oLogger.fine(sName + " received discovery message");
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new SUDiscoveryFrame());
        commonqueue.push(new Packets("Discovery", "", Packets.SU_DISCOVERY_ACK_PKT, Packets.PRIORITY_LEVEL_ONE,
                gatewayPkt.toBytes(sName)));
    }

    public void postToSU(int txnId, int msgType, byte[] args) {
        if (msgType != CommandsConstants.IMAGE_UPGRADE_MSG_TYPE) {
            if (m_lastTxnId == txnId)
                return;
        }
      //  System.out.println("message type is "+msgType);
        m_lastTxnId = txnId;
        oLogger.finest(sName + " received " + Utils.getPacket(args));
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        StringBuffer oBuf = new StringBuffer();
        switch (msgType) {
        case CommandsConstants.SET_LIGHT_LEVEL_MSG_TYPE: {
            int level = Utils.extractIntFromByteArray(args, 1);
            this.manualModeTime = Utils.extractIntFromByteArray(args, 5);
            this.lightLevel = (this.lightLevel * level) / 100;
            this.mode = 7;
            resetAutoModeTimer();
            oBuf.append("Relative Dim");
        }
            break;
        case CommandsConstants.GET_STATUS_MSG_TYPE:
            this.sendRealTimer((byte)msgType, this.sName);
            oBuf.append("Real Time");
            break;
        case CommandsConstants.SET_CURRENT_TIME:
        	int  currentTime = Utils.extractIntFromByteArray(args, 1);
        	int  GMToff = Utils.extractIntFromByteArray(args, 5);
        	int  DaylightSavingTime = Utils.extractIntFromByteArray(args, 9);
        	this.utc_time = currentTime*60;
        	this.localMins = currentTime + GMToff +DaylightSavingTime;
        	this.setTime =   System.currentTimeMillis()/1000;
        	oLogger.fine("UTC:"+this.utc_time);
            oLogger.fine("localMins:"+this.localMins);
            oLogger.fine("currentTime:"+currentTime);
            oLogger.fine("GMToff:"+GMToff);
            oLogger.fine("DST:"+DaylightSavingTime);
            oBuf.append("Set Time");
            break;
        case CommandsConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE: {
            int level = Utils.extractIntFromByteArray(args, 1);
            this.manualModeTime = Utils.extractIntFromByteArray(args, 5);
            if (this.manualModeTime == 97 && badCUReading == false) {
                oLogger.fine(getsName() + ": badCU simulation triggered.");
                badCUReading = true;
            }
            this.mode = 7;
            resetAutoModeTimer();
            if (level > 0) {
                this.lightLevel = level;
            }
            oBuf.append("Absolute Dim");
        }
            break;
        case CommandsConstants.SET_PROFILE_ADV_MSG_TYPE:
            int profileType = args[2] & 0xFF;
            oBuf.append("Advanced Profile").append(" ").append(profileType);
            oProfile.setAdvanceProfile(args, 2, true);
            break;
        case CommandsConstants.SET_PROFILE_MSG_TYPE:
            profileType = args[2] & 0xFF;
            oBuf.append("Schedued Profile").append(" ").append(profileType);
            switch(profileType) {
            case CommandsConstants.WEEK_DAY_PROFILE:
                oBuf.append(" Weekday ");
                break;
            case CommandsConstants.WEEK_END_PROFILE:
                oBuf.append(" Weekend ");
                break;
            case CommandsConstants.HOLIDAY_PROFILE:
                oBuf.append(" Holiday ");
                break;
            }
            System.out.println("profile type is "+args[2]);
            oProfile.setScheduleProfile(args, profileType, 3, true);
            break;
        case CommandsConstants.PROFILE_DOWNLOAD_MSG_TYPE:
            profileType = args[1] & 0xFF;
            oBuf.append("Download Profile").append(" ").append(profileType);
            byte[] data = null;
            if(profileType == 1) {
                // global profile
                data = oProfile.getAdvanceProfile();
            }else {
                // weekday, weekend, holiday
                data = oProfile.getScheduleProfile(profileType);
            }
            sendSUProfile((int) txnId, iPriority, profileType, sName, data);
            return;
        case CommandsConstants.SET_VALIDATION_MSG_TYPE:
            oBuf.append("Commission");
            break;
        case CommandsConstants.SET_LIGHT_STATE_MSG_TYPE:
            oBuf.append("Fixture State");
            break;
        case CommandsConstants.SET_DISC_MODE_MSG_TYPE:
            oBuf.append("Discovery");
            break;
        case CommandsConstants.SU_APPLY_WIRELESS_CMD:
            iPriority = Packets.PRIORITY_LEVEL_FOUR;
            oBuf.append("Apply Wireless");
            break;
        case CommandsConstants.SU_SET_WIRELESS_CMD:
            iPriority = Packets.PRIORITY_LEVEL_FOUR;
            oBuf.append("Change Wireless");
            break;
        case CommandsConstants.REBOOT_MSG_TYPE:
        	//TODO:  Added ability to fail restore, when the database state for fail resotre is true
        	if(args[1] !=0) {
        		this.currentApp =args[1];
        	} else {
        		this.currentApp = 2;
        	}
        	
        	oLogger.fine("Booting App#" +this.currentApp);
        	try {
                //Sleep 15seconds to simulate reboot
                Thread.sleep(15000);
            } catch (InterruptedException ie) {
                oLogger.warning(ie.getMessage());
            }
        	this.postNodeInfoToCommonQueue(this.commonqueue,this.currentApp);
        	this.startTime = System.currentTimeMillis();
            oBuf.append("Reboot");
            break;
        case CommandsConstants.MANUAL_CALIB_MSG_TYPE:
            oBuf.append("Manual Calibration");
            break;
        case CommandsConstants.SU_CMD_JOIN_GRP:
            if(iNumOfGroups == 10) {
                // sending SU nack.
                sendSUNack((int) txnId, iPriority, msgType, sName);
                return;
            }
            iNumOfGroups++;
            break;
        case CommandsConstants.SU_CMD_LEAVE_GRP:
            if (iNumOfGroups == 0) {
                // sending SU nack.
                sendSUNack((int) txnId, iPriority, msgType, sName);
                return;
            }
            iNumOfGroups--;
            break;
        case CommandsConstants.CMD_LAMP_CALIBRATION_REQ:
            Timer timer = new Timer();
            timer.schedule(new SendLampCaliberationCurveTask(), this.lccdelay*1000);
            break;
        case CommandsConstants.IMAGE_UPGRADE_MSG_TYPE:
            int submsg = args[1] & 0xFF;
            //oLogger.info(sName + " >> " + submsg);
            if ((submsg == CommandsConstants.ISP_APP2_INIT_OPCODE) || (submsg == CommandsConstants.ISP_APP1_INIT_OPCODE)) {
                imageUpgrade.setImageUpgradeInProgress(true);
                int filesize = Utils.extractIntFromByteArray(args, 6);
                imageUpgrade.setFileSize(filesize);
                oLogger.info(sName + " received image upgrade init request " + filesize);
                sendSUAck((int) txnId, iPriority, CommandsConstants.ISP_INIT_ACK_OPCODE, sName);
            }else if ((submsg == CommandsConstants.ISP_DATA_OPCODE) && imageUpgrade.isImageUpgradeInProgress()) {
                int chucksize = Utils.extractIntFromByteArray(args, 2);
                imageUpgrade.setChunckSize(chucksize);
                
                if (((imageUpgrade.getChunckSize() % 8192) == 0) || (imageUpgrade.getChunckSize() >= imageUpgrade.getFileSize())) {
                    Timer imgtimer = new Timer();
                    imgtimer.schedule(new SendUpgradeDataReceivedAckTask(), 2000);
                }
            }
            return;
        default:
            oBuf.append("Unknown");
        }
        // sending SU ack.
        if ((msgType != CommandsConstants.GET_STATUS_MSG_TYPE) && (msgType != CommandsConstants.REBOOT_MSG_TYPE) ) {
        	sendSUAck((int) txnId, iPriority, msgType, sName);
        }
        oLogger.finest(oBuf.toString());
        args = null;
    }

    protected void sendRealTimer(byte msgType, String sName) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        SURealTimeFrame suRealTimeFrame = new SURealTimeFrame(msgType, oProfile, sName);
        if(suRealTimeFrame.getMotionSecAgo() > 0) { 
            int percentageDimLevel = 100;
            suRealTimeFrame.setState(this.mode);
            if (this.mode == 2) {
                percentageDimLevel = oProfile.getMaxLightLevel();
            } else {
                percentageDimLevel = this.lightLevel;
            }
            
            suRealTimeFrame.setEnergyTicks((short) (EnergyStatsConstant.ENERGY_TICKS * percentageDimLevel / 100));
    //        suRealTimeFrame
    //        .setCalibValue((short) (EnergyStatsConstant.ENERGY_CALIB_VALUE * percentageDimLevel / 100));
            suRealTimeFrame.setVoltage((byte)percentageDimLevel);
            long now = System.currentTimeMillis()/100;
            int delta = (int) (now - this.setTime); 
            suRealTimeFrame.setUTC(this.utc_time +delta);
            suRealTimeFrame.setLocalMinutes(this.localMins + delta/60);
            suRealTimeFrame.setupTime((int)(System.currentTimeMillis()-startTime)/1000);
            oLogger.finest("StartTime:"+startTime);
            oLogger.finest("CurrTime:"+System.currentTimeMillis());
            oLogger.finest("diff:"+(int)(System.currentTimeMillis()-startTime)/1000);
            gatewayPkt.setCommandPkt(suRealTimeFrame);
            int iPacketType = Packets.SU_REAL_TIME_PKT;
            if (msgType == (byte)CommandsConstants.SU_CMD_HEART_BEAT_MSG_TYPE)
                iPacketType = Packets.SU_REAL_TIME_HB_PKT;
            commonqueue.push(new Packets("Ack", "", iPacketType, iPriority, gatewayPkt.toBytes(sName)));
        }
        // Simulate sending SU Event after PM stat for testing...
        sendBLEEvent(sName);
    }
    
    
       
       
    

    private void resetAutoModeTimer() {
        Timer timer = new Timer();
        timer.schedule(new AutoModeSettingTask(), this.manualModeTime*1000);
        
    }
    
    private void simulateBadCUReading() {
        badCUTimer = new Timer();
        badCUTimer.schedule(new BadCUTask(), 1000*60*20);
    }
    
    public class AutoModeSettingTask extends TimerTask{

        @Override
        public void run() {
            mode = 2;
            
        }
    }
    
    public class BadCUTask extends TimerTask{

        @Override
        public void run() {
            badCUReading = false;
            badCUTimer.cancel();
            badCUTimer = null;
            
        }
    }

    public class SendLampCaliberationCurveTask extends TimerTask{

        public SendLampCaliberationCurveTask() {
            
        }
        @Override
        public void run() {
            GatewayPkt gatewayPkt = new GatewayPkt();
            gatewayPkt.setCommandPkt(new LampCaliberationFrame());
            commonqueue.push(new Packets("Lamp Curve", "", Packets.SU_LAMP_CURVE_PKT, Packets.PRIORITY_LEVEL_EQUAL, gatewayPkt.toBytes(sName)));
        }
    }

    public class SendUpgradeDataReceivedAckTask extends TimerTask {

        public SendUpgradeDataReceivedAckTask() {
            
        }
        @Override
        public void run() {
            byte ackNo = (byte)imageUpgrade.getAckCount();
            if (imageUpgrade.getChunckSize() >= imageUpgrade.getFileSize()) {
                oLogger.info(sName + " sending imgupgrade datachuck ack (" + imageUpgrade.getChunckSize() + ", " + ackNo + ")");
            }else {
                oLogger.finest(sName + " sending imgupgrade datachuck ack (" + imageUpgrade.getChunckSize() + ", " + ackNo + ")");
            }
            GatewayPkt gatewayPkt = new GatewayPkt();
            gatewayPkt.setCommandPkt(new SuImageUpgrade(ackNo));
            commonqueue.push(new Packets("Upgrade Ack", "", Packets.SU_IMG_UPGRADE_PKT, Packets.PRIORITY_LEVEL_EQUAL, gatewayPkt.toBytes(sName)));
        }
    }
    
    
    
    /**
     * 
     * @param txnId
     * @param iPriority
     * @param ackType
     * @param snapAddress
     * @param data
     */
    private void sendSUProfile(int txnId, int iPriority, int ackType, String snapAddress, byte[] data) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new DownloadProfileFrame(txnId, (char) ackType, data));
        commonqueue.push(new Packets("Download Profile", "", Packets.DOWNLOAD_PROFILE_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

    /**
     * Short circuited method, Send SU Ack should come from individual SU's. Currently Gateway directly is doing their
     * job in the interest of getting the simulator feature functional quickly.
     * 
     * @param txnId
     * @param iPriority
     * @param msgType
     * @param snapAddress
     */
    private void sendSUAck(int txnId, int iPriority, int msgType, String snapAddress) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new SUAckFrame(txnId, (char) msgType));
        commonqueue.push(new Packets("Ack", "", Packets.SU_ACK_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

    private void sendSUNack(int txnId, int iPriority, int msgType, String snapAddress) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new SUAckFrame(txnId, (char)CommandsConstants.NACK_TO_MSG, (char) msgType));
        commonqueue.push(new Packets("Nack", "", Packets.SU_ACK_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

    public byte[] getPMData() {
        GatewayPkt gatewayPkt = new GatewayPkt();

        PMStatFrame pmStatFrame = new PMStatFrame(getNextPmStatsSeqNo(), oProfile);
        
        int percentageDimLevel = 100;
        if (this.mode == 2) {
            percentageDimLevel = oProfile.getMaxLightLevel();
            pmStatFrame.setCurrent_state(this.mode);
        } else {
            percentageDimLevel = this.lightLevel;
            pmStatFrame.setCurrent_state(this.mode);
        }
        
        pmStatFrame.setEnergy_consumption(EnergyStatsConstant.ENERGY_CONSUMPTION * percentageDimLevel / 100);
//        pmStatFrame
//                .setEnergyCalibValue((short) (EnergyStatsConstant.ENERGY_CALIB_VALUE * percentageDimLevel / 100));
        pmStatFrame.setEnergyTicks((short) (EnergyStatsConstant.ENERGY_TICKS * percentageDimLevel / 100));
        pmStatFrame.setAvg_voltage_step((short)percentageDimLevel);
        pmStatFrame.setSysUpTimeSecs((int)(System.currentTimeMillis()-startTime)/1000);
        long now = System.currentTimeMillis()/100;
        int delta = (int) (now - this.setTime); 
        pmStatFrame.setUTC(this.utc_time + delta);
        if (badCUReading) {
            pmStatFrame.setEnergy_consumption(-1);
            simulateBadCUReading();
        }
        gatewayPkt.setCommandPkt(pmStatFrame);
        return gatewayPkt.toBytes(sName);

    }

    public byte[] getMBitData() {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new MotionBitPkt());
        return gatewayPkt.toBytes(sName);

    }
    
    public byte[] getNodeInfo(byte appId) {
        GatewayPkt gatewayPkt = new GatewayPkt();

        NodeInfoFrame nodeInfoFrame = new NodeInfoFrame(getNextPmStatsSeqNo(), oProfile);
        nodeInfoFrame.setCurrApp(appId);
        gatewayPkt.setCommandPkt(nodeInfoFrame);
        return gatewayPkt.toBytes(sName);

    }
    
    public boolean postNodeInfoToCommonQueue(CommonQueue cq,byte appId) {

        byte[] pmPkt = getNodeInfo(appId);
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;

        cq.push(new Packets(sName, message, Packets.NODE_INFO_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
        
        // Simulate duplicate PM stats packets
        Random random = new Random();
        int genDuplicateStats = random.nextInt(4);
        if (genDuplicateStats == 1) {
            try {
                Thread.sleep(10);
                cq.push(new Packets(sName, message, Packets.NODE_INFO_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
            } catch (InterruptedException ie) {
    
            }
        }

        return true;
    }

    public boolean postToCommonQueue(CommonQueue cq) {

        byte[] pmPkt = getPMData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;

        cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
        
        // Simulate duplicate PM stats packets
        Random random = new Random();
        int genDuplicateStats = random.nextInt(4);
        if (genDuplicateStats == 1) {
            try {
                Thread.sleep(10);
                cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
            } catch (InterruptedException ie) {
    
            }
        }

        return true;
    }

    public boolean postMBitToCommonQueue(CommonQueue cq) {

        byte[] oPkt = getMBitData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + oPkt;

        cq.push(new Packets(sName, message, Packets.MBIT_STAT_PKT, Packets.PRIORITY_LEVEL_EQUAL, oPkt));

        return true;
    }
    
    public boolean sendPMHeartbeat(byte msgType, String sName) {
        
        GatewayPkt gatewayPkt = new GatewayPkt();
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;

        PMHeartbeatStatFrame pmHbStatFrame = new PMHeartbeatStatFrame(getNextPmStatsSeqNo(), oProfile, sName);
        
        if(pmHbStatFrame.getMotionSecsAgo() > 0) {
            int percentageDimLevel = 100;
            if (this.mode == 2) {
                percentageDimLevel = oProfile.getMaxLightLevel();
            } else {
                percentageDimLevel = this.lightLevel;
            }
            pmHbStatFrame.setCurr_load((short) (72 * percentageDimLevel / 100));
           
            if (badCUReading) {
                simulateBadCUReading();
            }
            long now = System.currentTimeMillis()/100;
            int delta = (int) (now - this.setTime); 
            pmHbStatFrame.setUtc_time_secs(this.utc_time + delta);
            
            if((this.hbTriggerMask & 0x1) == 0x1) { 
            	pmHbStatFrame.setHb_trigger_type((byte) 0x0);
                gatewayPkt.setCommandPkt(pmHbStatFrame);
                int iPacketType = Packets.SU_REAL_TIME_HB_PKT;
                byte[] hbPkt = gatewayPkt.toBytes(sName);
                String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + hbPkt;
                commonqueue.push(new Packets(sName, message, iPacketType, iPriority, hbPkt));
                return true;
            }
            this.hbTriggerMask = this.m_dbHelperInstance.getHBTrigger(this.sName);
            if((this.hbTriggerMask & 0x8) == 0x8) { 
            	pmHbStatFrame.setHb_trigger_type((byte) 0x1);
                gatewayPkt.setCommandPkt(pmHbStatFrame);
                int iPacketType = Packets.SU_REAL_TIME_HB_PKT;
                byte[] hbPkt = gatewayPkt.toBytes(sName);
                String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + hbPkt;
                commonqueue.push(new Packets(sName, message, iPacketType, iPriority, hbPkt));
                return true;
            }
            byte[] pmPkt = gatewayPkt.toBytes(sName);
            String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;
    
            
            return commonqueue.push(new Packets(sName, message, Packets.SU_PM_HB_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
        }
        return true;

    }
    
    public void sendBLEEvent(String sName) {
        oLogger.info(sName + " sending BLE Event");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] tagId = {0, 0, 0, 0, 0, 0};
        Random rand = new Random();
        try {
            for (int count = 0; count < 16; count++) {
                output.write(tagId);
                output.write((byte)rand.nextInt(255));
            }
        }catch(IOException ioe) {
            
        }
        SUEventPkt event = new SUEventPkt(12, output.toByteArray());
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(event);
        int iPacketType = Packets.SU_EVENT_PKT;
        byte[] oPkt = gatewayPkt.toBytes(sName);
        oLogger.info(sName + " " + Utils.getPacket(oPkt));
        commonqueue.push(new Packets(sName, "", iPacketType, Packets.PRIORITY_LEVEL_EQUAL, oPkt));

    }
}

class GenerateData implements Runnable {

    CommonQueue cq;
    SensorUnit su;

    public GenerateData(SensorUnit su, CommonQueue cq) {

        this.cq = cq;
        this.su = su;
    }

    @Override
    public void run() {

        byte[] snapByteArr = Utils.getSnapAddr(su.sName);
        Random rand = new Random(Utils.extractShortFromByteArray(snapByteArr, 1));
        int pmStatsJitter = rand.nextInt(Utils.getJitter());
        // sleeping for the jitter timer for the first time
        try {
            //wait 30 seconds after Sensor start then send NodeInfo
        	Thread.sleep(30000);
        	su.postNodeInfoToCommonQueue(cq,su.currentApp);
            // randomTime = su.randomGenerator.nextInt(Utils.getJitter());
            Thread.sleep(pmStatsJitter);
        } catch (InterruptedException ie) {
            su.oLogger.warning(ie.getMessage());
        }
        
        while (su.getKeepRunning() == Thread.currentThread()) {
            if (su.badCUTimer == null) {
                su.postToCommonQueue(cq);
            }else {
                su.oLogger.fine(su.getsName() + ": skipped...");
            }
            // su.oLogger.fine(su.getsName() + ": posted");
            try {
                Thread.sleep(su.delay);
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
        }

    }

}

/**
 * Starts based on configuration settings
 * @author yogesh
 *
 */
class GenerateMotionBitData implements Runnable {

    CommonQueue cq;
    SensorUnit su;

    public GenerateMotionBitData(SensorUnit su, CommonQueue cq) {

        this.cq = cq;
        this.su = su;
    }

    @Override
    public void run() {
        byte[] snapByteArr = Utils.getSnapAddr(su.sName);
        Random rand = new Random(Utils.extractShortFromByteArray(snapByteArr, 1));
        int mbitStatsJitter = rand.nextInt(Utils.getJitter());
        // sleeping for the jitter timer for the first time
        try {
            Thread.sleep(mbitStatsJitter);
        } catch (InterruptedException ie) {
            su.oLogger.warning(ie.getMessage());
        }
        while (su.getKeepMBitRunning() == Thread.currentThread()) {
            su.postMBitToCommonQueue(cq);
            // su.oLogger.fine(su.getsName() + ": posted");
            try {
                Thread.sleep(su.mbitDelay);
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
        }
    }
}

class GenerateFxStatsHB implements Runnable {
    SensorUnit su;

    public GenerateFxStatsHB(SensorUnit su) {
        this.su = su;
    }

    @Override
    public void run() {
        byte[] snapByteArr = Utils.getSnapAddr(su.sName);
        // sleeping for the jitter timer for the first time
        Random rand = new Random(Utils.extractShortFromByteArray(snapByteArr, 1));
        int iJitter = rand.nextInt(Utils.getiFxStatHBJitter());
        try {
            Thread.sleep(iJitter);
        } catch (InterruptedException ie) {
            su.oLogger.warning(ie.getMessage());
        }
        while (su.getFxHBRunning() == Thread.currentThread()) {
            su.sendRealTimer((byte)CommandsConstants.SU_CMD_HEART_BEAT_MSG_TYPE, su.sName);
            try {
                Thread.sleep(Utils.getiFxStatHB());
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
        }
    }
}


class GeneratePmStatsHB implements Runnable {
    SensorUnit su;

    public GeneratePmStatsHB(SensorUnit su) {
        this.su = su;
    }

    @Override
    public void run() {
        byte[] snapByteArr = Utils.getSnapAddr(su.sName);
        // sleeping for the jitter timer for the first time
        Random rand = new Random(Utils.extractShortFromByteArray(snapByteArr, 1));
        int iJitter = rand.nextInt(60*1000);
        try {
            Thread.sleep(iJitter);
        } catch (InterruptedException ie) {
            su.oLogger.warning(ie.getMessage());
        }
        while (su.getPmHBRunning() == Thread.currentThread()) {
            su.sendPMHeartbeat((byte)CommandsConstants.SU_CMD_HB_STATS_MSG_TYPE, su.sName);
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
        }
    }
}

class ImageUpgradeSU {
    private boolean imageUpgradeInProgress = false;
    private long fileSize = 0L;
    private long receivedChuck = 0L;
    private int ackCount = -1;
    private SensorUnit su;
    
    public ImageUpgradeSU(SensorUnit su) {
        this.su = su;
    }

    public boolean isImageUpgradeInProgress() {
        return imageUpgradeInProgress;
    }

    public void setImageUpgradeInProgress(boolean imageUpgradeInProgress) {
        this.imageUpgradeInProgress = imageUpgradeInProgress;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
        receivedChuck = 0;
        ackCount = -1;
    }

    public void setChunckSize(int chuckSize) {
        if (receivedChuck != (chuckSize+64)) { // duplicates are handled here
            receivedChuck = chuckSize+64;
            su.oLogger.finest(su.sName + " received imgupgrade datachuck (" + getChunckSize() + ")");
            if ((receivedChuck % 8192) == 0 || receivedChuck >= fileSize) {
                ackCount++;
            }
        }
    }
    
    public long getChunckSize() {
        return receivedChuck;
    }

    public int getAckCount() {
        return ackCount;
    }
}
