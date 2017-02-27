/**
 * 
 */
package com.ems.plugload;

import java.util.Calendar;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.PLAckFrame;
import com.ems.commands.PlugloadDiscoveryFrame;
import com.ems.commands.PlugloadPMStatFrame;
import com.ems.commands.plprofile.Profile;
import com.ems.db.SimPlDBHelper;
import com.ems.gw.GWInterface;
import com.ems.su.Packets;
import com.ems.su.SensorUnit;
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
    Profile oProfile = null;
    private SimPlDBHelper m_dbHelperInstance = SimPlDBHelper.getInstance();
    private volatile Thread blinker;
    
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
        case CommandsConstants.SU_SET_APPLY_WIRELESS_CMD:
            iPriority = Packets.PRIORITY_LEVEL_FOUR;
            oBuf.append("Change Wireless");
            break;

        default:
            oBuf.append("Unknown");
        }
        // sending SU ack.
        sendPLAck((int) txnId, iPriority, msgType, sName);
        oLogger.finest(oBuf.toString());
        args = null;
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
        gatewayPkt.setCommandPkt(pmStatFrame);
        oLogger.info("PM stat packet for plugload is "+Utils.getPacket(gatewayPkt.toBytes(sName)));
        return gatewayPkt.toBytes(sName);

    }
    
    public int getNextPmStatsSeqNo() {
        return pmStatsSeqNo++;
    }
    
    public boolean postToCommonQueue(CommonQueue cq) {

        byte[] pmPkt = getPMData();
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + pmPkt;
        oLogger.info("==========plugload pmpkt in posttocommonqueue is "+Utils.getPacket(pmPkt));
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
