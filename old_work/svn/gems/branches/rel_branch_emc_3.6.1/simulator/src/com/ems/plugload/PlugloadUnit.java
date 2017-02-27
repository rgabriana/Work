/**
 * 
 */
package com.ems.plugload;

import java.util.Calendar;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.PLAckFrame;
import com.ems.commands.PLRealTimeFrame;
import com.ems.commands.PlugloadDiscoveryFrame;
import com.ems.commands.PlugloadPMStatFrame;
import com.ems.commands.imgupgrade.PlugloadImageUpgrade;
import com.ems.commands.plprofile.Profile;
import com.ems.commands.profile.DownloadProfileFrame;
import com.ems.constants.EnergyStatsConstant;
import com.ems.db.SimPlDBHelper;
import com.ems.gw.GWInterface;
import com.ems.su.Packets;
import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class PlugloadUnit {
    public Logger oLogger = Logger.getLogger(PlugloadUnit.class.getName());
    String sName;
    int delay = Utils.getDelay();
    int mbitDelay = 1000 * 60;
    private boolean keepRunning = false;
    private CommonQueue commonqueue;
    long m_lastTxnId = 0;
    private int pmStatsSeqNo = 1;
    Random randomGenerator = new Random();
    private Profile oProfile = null;
    private SimPlDBHelper m_dbHelperInstance = SimPlDBHelper.getInstance();
    private volatile Thread blinker;
    private ImageUpgradePlugload imageUpgrade = new ImageUpgradePlugload(this);
 // mode
    private boolean autoMode = true;
    public PlugloadUnit(String name) {
        sName = name;
        oProfile = new Profile(sName);
        m_dbHelperInstance.addPL(this);
    }

    
    
    
    public boolean isKeepRunning() {
        return keepRunning;
    }




    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
    
    public Thread getKeepRunning() {
        return this.blinker;
    }




    public Profile getProfile() {
        return oProfile;
    }
    
    public void assignPlugloadtoGatewy(GWInterface gw) {
        commonqueue = gw.getGatewayQueue();
    }

    /**
     * @return the sName
     */
    public String getsName() {
        return sName;
    }

    /**
     * @param sName
     *            the sName to set
     */
    public void setsName(String sName) {
        this.sName = sName;
    }

    public void startPL() {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, this.commonqueue), "PM_" + sName);
        blinker.start();
    }

    public void stopPL() {
        keepRunning = false;
    }

    public void on() {
        startPL();
    }

    public void postToPlugload(int txnId, int msgType, byte[] args) {
        if (msgType != CommandsConstants.IMAGE_UPGRADE_MSG_TYPE) {
            if (m_lastTxnId == txnId)
                return;
        }

        m_lastTxnId = txnId;
        oLogger.finest(sName + " received " + Utils.getPacket(args));
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        StringBuffer oBuf = new StringBuffer();
        switch (msgType) {
        case CommandsConstants.GET_STATUS_MSG_TYPE:
            this.sendRealTimer((byte)msgType, this.sName);
            oBuf.append("Real Time");
            break;
            
        case CommandsConstants.SU_SET_APPLY_WIRELESS_CMD:
            iPriority = Packets.PRIORITY_LEVEL_FOUR;
            oBuf.append("Change Wireless");
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
            sendPLProfile((int) txnId, iPriority, profileType, sName, data);
            return;     

        default:
            oBuf.append("Unknown:-"+msgType);
        }
        // sending SU ack.
        sendPLAck((int) txnId, iPriority, msgType, sName);
        oLogger.finest(oBuf.toString());
        args = null;
    }
    
    
    /**
     * 
     * @param txnId
     * @param iPriority
     * @param ackType
     * @param snapAddress
     * @param data
     */
    private void sendPLProfile(int txnId, int iPriority, int ackType, String snapAddress, byte[] data) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new DownloadProfileFrame(txnId, (char) ackType, data));
        commonqueue.push(new Packets("Download Profile", "", Packets.DOWNLOAD_PROFILE_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }
    
    
    
    public class SendPlugloadUpgradeDataReceivedAckTask extends TimerTask {

        public SendPlugloadUpgradeDataReceivedAckTask() {
            
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
            gatewayPkt.setCommandPkt(new PlugloadImageUpgrade(ackNo));
            commonqueue.push(new Packets("Upgrade Ack", "", Packets.SU_IMG_UPGRADE_PKT, Packets.PRIORITY_LEVEL_EQUAL, gatewayPkt.toBytes(sName)));
        }
    }

    public void postDiscovery(long txnId) {
        if (m_lastTxnId == txnId)
            return;

        oLogger.fine(sName + " received discovery message");
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new PlugloadDiscoveryFrame());
        commonqueue.push(new Packets("Discovery", "", Packets.SU_DISCOVERY_ACK_PKT, Packets.PRIORITY_LEVEL_ONE,
                gatewayPkt.toBytes(sName)));
    }

    private void sendPLAck(int txnId, int iPriority, int msgType, String snapAddress) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new PLAckFrame(txnId, (char) msgType));
        commonqueue.push(new Packets("Ack", "", Packets.SU_ACK_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

    private void sendPLNack(int txnId, int iPriority, int msgType, String snapAddress) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new PLAckFrame(txnId, (char) CommandsConstants.NACK_TO_MSG, (char) msgType));
        commonqueue.push(new Packets("Nack", "", Packets.SU_ACK_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

    
    public byte[] getPMData() {
        GatewayPkt gatewayPkt = new GatewayPkt();
        PlugloadPMStatFrame pmStatFrame = new PlugloadPMStatFrame(getNextPmStatsSeqNo(), oProfile); 
        if (this.autoMode) {
            pmStatFrame.setCurrState((byte)2);
        } else {
            pmStatFrame.setCurrState((byte)7);
        }
        gatewayPkt.setCommandPkt(pmStatFrame);
        oLogger.info("PM stat packet for plugload is "+Utils.getPacket(gatewayPkt.toBytes(sName)));
        return gatewayPkt.toBytes(sName);

    }
    
    public int getNextPmStatsSeqNo() {
        return pmStatsSeqNo++;
    }
    
    protected void sendRealTimer(byte msgType, String sName) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        PLRealTimeFrame plRealTimeFrame = new PLRealTimeFrame(msgType, oProfile, sName);
        if(plRealTimeFrame.getMotionSecAgo() > 0) { 
            if (this.autoMode) {
                plRealTimeFrame.setState((byte)2);
            } else {
                plRealTimeFrame.setState((byte)7);
            }
            plRealTimeFrame.setEnergyTicks((short) (EnergyStatsConstant.ENERGY_TICKS*100));
            plRealTimeFrame.setVoltage((byte)100);
            gatewayPkt.setCommandPkt(plRealTimeFrame);
            int iPacketType = Packets.SU_REAL_TIME_PKT;
            if (msgType == (byte)CommandsConstants.SU_CMD_HEART_BEAT_MSG_TYPE)
                iPacketType = Packets.SU_REAL_TIME_HB_PKT;
            commonqueue.push(new Packets("Ack", "", iPacketType, iPriority, gatewayPkt.toBytes(sName)));
        }
    }
    
    public boolean postToCommonQueue(CommonQueue cq) {

        byte[] pmPkt = getPMData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;
        oLogger.fine("==========plugload pmpkt in posttocommonqueue is "+Utils.getPacket(pmPkt));
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
    
    class GenerateData implements Runnable {

        CommonQueue cq;
        PlugloadUnit pu;

        public GenerateData(PlugloadUnit pu, CommonQueue cq) {

            this.cq = cq;
            this.pu = pu;
        }

        @Override
        public void run() {

            byte[] snapByteArr = Utils.getSnapAddr(pu.sName);
            Random rand = new Random(Utils.extractShortFromByteArray(snapByteArr, 1));
            int pmStatsJitter = rand.nextInt(Utils.getJitter());
            // sleeping for the jitter timer for the first time
            try {
                // randomTime = su.randomGenerator.nextInt(Utils.getJitter());
                Thread.sleep(pmStatsJitter);
            } catch (InterruptedException ie) {
                pu.oLogger.warning(ie.getMessage());
            }
            while (pu.getKeepRunning() == Thread.currentThread()) {                
                    pu.postToCommonQueue(cq);                
                // su.oLogger.fine(su.getsName() + ": posted");
                try {
                    Thread.sleep(pu.delay);
                } catch (InterruptedException ie) {
                    pu.oLogger.warning(ie.getMessage());
                }
            }

        }

    }
}

class ImageUpgradePlugload {
    private boolean imageUpgradeInProgress = false;
    private long fileSize = 0L;
    private long receivedChuck = 0L;
    private int ackCount = -1;
    private PlugloadUnit pu;
    
    public ImageUpgradePlugload(PlugloadUnit pu) {
        this.pu = pu;
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
            pu.oLogger.finest(pu.sName + " received imgupgrade datachuck (" + getChunckSize() + ")");
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
