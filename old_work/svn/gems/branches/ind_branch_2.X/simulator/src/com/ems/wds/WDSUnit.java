/**
 * 
 */
package com.ems.wds;

import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.commands.wds.WDSAckFrame;
import com.ems.commands.wds.WDSDiscoverPkt;
import com.ems.gw.GW10;
import com.ems.su.Packets;
import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class WDSUnit {
    public Logger oLogger = Logger.getLogger(WDSUnit.class.getName());
    private boolean keepRunning = false;
    private CommonQueue commonqueue;
    long m_lastTxnId = 0;

    String sName;

    public WDSUnit(String name) {
        sName = name;
    }

    public String getsName() {
        return sName;
    }
    
    public void assignWDStoGatewy(GW10 gw) {
        commonqueue = gw.getGatewayQueue();
    }

    public CommonQueue getCommonqueue() {
        return commonqueue;
    }

    public void setCommonqueue(CommonQueue commonqueue) {
        this.commonqueue = commonqueue;
    }

    public void startWDS() {
        keepRunning = true;
    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public void postDiscovery(long txnId) {
        if (m_lastTxnId == txnId)
            return;

        //m_lastTxnId = txnId;
        oLogger.fine(sName + " received WDS discovery message");
        GatewayPkt gatewayPkt = new GatewayPkt();
        gatewayPkt.setRouteFlags(GatewayPkt.PKT_WDS);
        gatewayPkt.setCommandPkt(new WDSDiscoverPkt());
        commonqueue.push(new Packets("WDS Discovery", "", Packets.WDS_DISCOVERY_ACK_PKT, Packets.PRIORITY_LEVEL_ONE,
                gatewayPkt.toBytes(sName)));
    }

    public void postToWDS(int txnId, int msgType, byte[] args) {
        if (m_lastTxnId == txnId)
            return;

        m_lastTxnId = txnId;
        oLogger.finest(sName + " received " + Utils.getPacket(args));
        int iPriority = Packets.PRIORITY_LEVEL_EQUAL;
        StringBuffer oBuf = new StringBuffer();
        switch (msgType) {
        case CommandsConstants.CMD_SET_SWITCH_PARAMS:
            oBuf.append("Set switch params");
            break;
        case CommandsConstants.CMD_APPLY_SWITCH_PARAMS:
            oBuf.append("Apply switch params");
            break;
        default:
            oBuf.append("Unknown");
        }

        // sending WDS ack.
        sendWDSAck((int) txnId, iPriority, msgType, sName);
        oLogger.finest(oBuf.toString());
        args = null;
    }

    private void sendWDSAck(int txnId, int iPriority, int msgType, String snapAddress) {
        GatewayPkt gatewayPkt = new GatewayPkt();
        //WDS to EM
        gatewayPkt.setRouteFlags(GatewayPkt.PKT_WDS);
        gatewayPkt.setCommandPkt(new WDSAckFrame(txnId, (char) msgType));
        commonqueue.push(new Packets("WDS Ack", "", Packets.WDS_ACK_PKT, iPriority, gatewayPkt.toBytes(snapAddress)));
    }

}
