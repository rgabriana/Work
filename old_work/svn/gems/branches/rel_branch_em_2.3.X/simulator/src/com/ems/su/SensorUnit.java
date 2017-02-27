package com.ems.su;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.MotionBitPkt;
import com.ems.commands.PMStatFrame;
import com.ems.commands.SUAckFrame;
import com.ems.commands.SUDiscoveryFrame;
import com.ems.commands.SURealTimeFrame;
import com.ems.commands.profile.DownloadProfileFrame;
import com.ems.commands.profile.Profile;
import com.ems.constants.EnergyStatsConstant;
import com.ems.db.SimDBHelper;
import com.ems.gw.GW10;
import com.ems.profile.ProfileConstants;
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
    public volatile Thread blinker, mbitThread;
    
    private Profile oProfile = null;

    // mode
    private boolean autoMode = true;
    
    // bad cu reading
    private boolean badCUReading = false;
    
    public Timer badCUTimer = null;
    
    // light level
    int lightLevel = 75;
    int manualModeTime = 0;
    
    // Num of groups
    int iNumOfGroups = 0;
    
    private SimDBHelper m_dbHelperInstance = SimDBHelper.getInstance();

    public SensorUnit(String name) {
        sName = name;
        oProfile = new Profile(sName);
        m_dbHelperInstance.addSU(this);
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

    public void assignSensortoGatewy(GW10 gw) {
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
    }

    public void startSensor() {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, this.commonqueue), "PM_" + sName);
        blinker.start();
        if (Utils.getiEnabledMBits() == 1) {
            mbitThread = new Thread(new GenerateMotionBitData(this, this.commonqueue), "MBIT_"+ sName);
            mbitThread.start();
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

    public void stopSensor() {
        keepRunning = false;
        blinker = null;
        mbitThread = null;

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
        if (m_lastTxnId == txnId)
            return;

        m_lastTxnId = txnId;
        oLogger.finest(sName + " received " + Utils.getPacket(args));
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        StringBuffer oBuf = new StringBuffer();
        switch (msgType) {
        case CommandsConstants.SET_LIGHT_LEVEL_MSG_TYPE: {
            int level = Utils.extractIntFromByteArray(args, 1);
            this.manualModeTime = Utils.extractIntFromByteArray(args, 5);
            this.lightLevel = (this.lightLevel * level) / 100;
            this.autoMode = false;
            resetAutoModeTimer();
            oBuf.append("Relative Dim");
        }
            break;
        case CommandsConstants.GET_STATUS_MSG_TYPE:
            this.sendRealTimer();
            oBuf.append("Real Time");
            break;
        case CommandsConstants.SET_CURRENT_TIME:
            oBuf.append("Set Time");
            break;
        case CommandsConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE: {
            int level = Utils.extractIntFromByteArray(args, 1);
            this.manualModeTime = Utils.extractIntFromByteArray(args, 5);
            if (this.manualModeTime == 97 && badCUReading == false) {
                oLogger.fine(getsName() + ": badCU simulation triggered.");
                badCUReading = true;
            }
            this.autoMode = false;
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
            iPriority = Packets.PRIORITY_LEVEL_TWO;
            oBuf.append("Apply Wireless");
            break;
        case CommandsConstants.SU_SET_WIRELESS_CMD:
            iPriority = Packets.PRIORITY_LEVEL_TWO;
            oBuf.append("Change Wireless");
            break;
        case CommandsConstants.REBOOT_MSG_TYPE:
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
        default:
            oBuf.append("Unknown");
        }
        // sending SU ack.
        sendSUAck((int) txnId, iPriority, msgType, sName);
        oLogger.finest(oBuf.toString());
        args = null;
    }

    private void sendRealTimer() {
        GatewayPkt gatewayPkt = new GatewayPkt();
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        SURealTimeFrame suRealTimeFrame = new SURealTimeFrame(oProfile);
        int percentageDimLevel = 100;
        if (this.autoMode) {
            percentageDimLevel = oProfile.getMaxLightLevel();
            suRealTimeFrame.setState((byte)2);
        } else {
            percentageDimLevel = this.lightLevel;
            suRealTimeFrame.setState((byte)7);
        }
        
        suRealTimeFrame.setEnergyTicks((short) (EnergyStatsConstant.ENERGY_TICKS * percentageDimLevel / 100));
//        suRealTimeFrame
//        .setCalibValue((short) (EnergyStatsConstant.ENERGY_CALIB_VALUE * percentageDimLevel / 100));
        suRealTimeFrame.setVoltage((byte)percentageDimLevel);
        gatewayPkt.setCommandPkt(suRealTimeFrame);
        commonqueue.push(new Packets("Ack", "", Packets.SU_REAL_TIME_PKT, iPriority, gatewayPkt.toBytes(sName)));
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
            autoMode = true;
            
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
        if (this.autoMode) {
            percentageDimLevel = oProfile.getMaxLightLevel();
            pmStatFrame.setCurrent_state('2');
        } else {
            percentageDimLevel = this.lightLevel;
            pmStatFrame.setCurrent_state('7');
        }
        
        pmStatFrame.setEnergy_consumption(EnergyStatsConstant.ENERGY_CONSUMPTION * percentageDimLevel / 100);
//        pmStatFrame
//                .setEnergyCalibValue((short) (EnergyStatsConstant.ENERGY_CALIB_VALUE * percentageDimLevel / 100));
        pmStatFrame.setEnergyTicks((short) (EnergyStatsConstant.ENERGY_TICKS * percentageDimLevel / 100));
        pmStatFrame.setAvg_voltage_step((short)percentageDimLevel);
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

    public boolean postToCommonQueue(CommonQueue cq) {

        byte[] pmPkt = getPMData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;

        cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
        
        // Simulate duplicate PM stats packets
        Random random = new Random();
        int genDuplicateStats = random.nextInt(2);
        if (genDuplicateStats == 1) {
            try {
                Thread.sleep(5);
                cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
                Thread.sleep(5);
                cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, pmPkt));
            } catch (InterruptedException ie) {
    
            }
        }

        return true;
    }

    public boolean postMBitToCommonQueue(CommonQueue cq) {

        byte[] oPkt = getMBitData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + oPkt;

        cq.push(new Packets(sName, message, Packets.MBIT_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, oPkt));

        return true;
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
