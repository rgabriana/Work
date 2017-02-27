package com.ems.su;

import java.util.Calendar;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.PMStatFrame;
import com.ems.commands.SUAckFrame;
import com.ems.commands.SUDiscoveryFrame;
import com.ems.gw.GW10;
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
    private boolean keepRunning = false;
    private CommonQueue commonqueue;
    long m_lastTxnId = 0;
    Random randomGenerator = new Random();
    Thread t;
    public volatile Thread blinker;
    
    public SensorUnit(String name) {
        sName = name;
    }

    public String getsName() {
        return sName;
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

    // Two ways to start sensors.
    public void startSensor(CommonQueue cq) {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, cq));
        blinker.start();
    }

    public void startSensor() {
        keepRunning = true;
        blinker = new Thread(new GenerateData(this, this.commonqueue));
        blinker.start();

    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public Thread getKeepRunning() {
        return this.blinker;
    }

    public void stopSensor() {
        keepRunning = false;
        blinker = null;

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
        commonqueue.push(new Packets("Discovery", "", Packets.SU_DISCOVERY_ACK_PKT, Packets.PRIORITY_LEVEL_ONE, gatewayPkt.toBytes(sName)));
    }

    public void postToSU(int txnId, int msgType, byte[] args) {
        if (m_lastTxnId == txnId)
            return;

        m_lastTxnId = txnId;
        oLogger.finest(sName + " received " + Utils.getPacket(args));
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        StringBuffer oBuf = new StringBuffer();
        switch (msgType) {
        case CommandsConstants.SET_LIGHT_LEVEL_MSG_TYPE:
            oBuf.append("Relative Dim");
            break;
        case CommandsConstants.SET_CURRENT_TIME:
            oBuf.append("Set Time");
            break;
        case CommandsConstants.SET_ABS_LIGHT_LEVEL_MSG_TYPE:
            oBuf.append("Absolute Dim");
            break;
        case CommandsConstants.SET_PROFILE_ADV_MSG_TYPE:
            oBuf.append("Advanced Profile");
            break;
        case CommandsConstants.SET_PROFILE_MSG_TYPE:
            oBuf.append("Schedued Profile");
            break;
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
        default:
            oBuf.append("Unknown");
        }
        // sending SU ack.
        sendSUAck((int) txnId, iPriority, msgType, sName);
        oLogger.finest(oBuf.toString());
        args = null;
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

    public byte[] getPMData() {
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setCommandPkt(new PMStatFrame());
        return gatewayPkt.toBytes(sName);

    }

    public boolean postToCommonQueue(CommonQueue cq) {
        String message = "Posted Time : " + Calendar.getInstance().getTime().toString() + " Data : " + getPMData();

        cq.push(new Packets(sName, message, Packets.PM_STAT_PKT, Packets.PRIORITY_LEVEL_TWO, getPMData()));

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
        while (su.getKeepRunning() == Thread.currentThread()) {
         // randomly sleep for time between zero to three minutes 
            try {
                Thread.sleep(su.randomGenerator.nextInt(Utils.getJitter()));
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
            
            su.postToCommonQueue(cq);
            // su.oLogger.fine(su.getsName() + ": posted");
            try {
                Thread.sleep(su.delay);
            } catch (InterruptedException ie) {
                su.oLogger.warning(ie.getMessage());
            }
        }

    }

}
