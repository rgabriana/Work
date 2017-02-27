/**
 * 
 */
package com.ems.plugload;

import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.PLAckFrame;
import com.ems.commands.PlugloadDiscoveryFrame;
import com.ems.db.SimDBHelper;
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
    private SimDBHelper m_dbHelperInstance = SimDBHelper.getInstance();

    public PlugloadUnit(String name) {
        sName = name;
        // m_dbHelperInstance.addSU(this);
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

}
