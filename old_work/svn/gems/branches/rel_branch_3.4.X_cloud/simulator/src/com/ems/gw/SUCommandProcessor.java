package com.ems.gw;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GatewayPkt;
import com.ems.plugload.PlugloadUnit;
import com.ems.su.SensorUnit;
import com.ems.utils.Utils;
import com.ems.wds.WDSUnit;

public class SUCommandProcessor implements Runnable {
    private Logger oLogger = Logger.getLogger(SUCommandProcessor.class.getName());
    private GWInterface gw;
    private HashMap<String, SensorUnit> suList;
    private HashMap<String, WDSUnit> wdsList;
    private HashMap<String, PlugloadUnit> plList;


    public SUCommandProcessor(GWInterface gw) {
        Thread.currentThread().setName("SUCommandProcesssor");
        this.gw = gw;
    }

    public void run() {
        try {
            while (gw.getKeepRunningSuCmdProcessor() == Thread.currentThread()) {
                synchronized (Utils.getSUMsgQueue().getWaitNotifyLock()) {
                    Utils.getSUMsgQueue().getWaitNotifyLock().wait();
                }
                while (!Utils.getSUMsgQueue().isQueueEmpty()) {
                    processSUMsg();
                    try {
                        Thread.sleep(5);
                    }catch(InterruptedException ie) {
                        
                    }
                }
            }
        }

        catch (SocketException e) {
            oLogger.warning(e.getMessage());
        } catch (UnknownHostException e) {
            oLogger.warning(e.getMessage());
        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        } catch (InterruptedException e) {
            oLogger.warning(e.getMessage());
        } finally {
        }
    }

    public synchronized void processSUMsg() throws IOException {
        suList = Utils.getSensorUnits();
        wdsList = Utils.getWDSUnits();
        plList = Utils.getPlugLoadUnits();
        long iStart = System.currentTimeMillis();
        byte[] suPkt = Utils.getSUMsgQueue().pop();
        StringBuffer oBuf = new StringBuffer();
        oBuf.append("SU Pkt: ");
        int txPos = GatewayPkt.POS_TXID;
        byte[] txId = { suPkt[txPos], suPkt[txPos + 1], suPkt[txPos + 2], suPkt[txPos + 3] };
        long txnId = Utils.intByteArrayToLong(txId);
        int gwMsgType = suPkt[GatewayPkt.POS_MCAST_CMD] & 0xFF;
        switch (gwMsgType) {
        case CommandsConstants.RESP_INIT_OPCODE:
            int iTargets = suPkt[GatewayPkt.POS_MCAST_TARGETS_NO] & 0xff;
            int addrPos = GatewayPkt.POS_MCAST_TARGETS;
            int msgType = (suPkt[GatewayPkt.POS_MCAST_TARGETS + iTargets * 3] & 0xFF);
            byte[] msgArgs = new byte[suPkt.length - (GatewayPkt.POS_MCAST_TARGETS + iTargets * 3)];
            System.arraycopy(suPkt, (GatewayPkt.POS_MCAST_TARGETS + (iTargets * 3)), msgArgs, 0, msgArgs.length);
            byte bh1 = suPkt[GatewayPkt.POS_MCAST_TARGETS_NO+1];
            byte bh2 = suPkt[GatewayPkt.POS_MCAST_TARGETS_NO+2];
            if (iTargets == 2 && bh1 == CommandsConstants.BORDCAST_HEADER_BIT1 && bh2 == CommandsConstants.BORDCAST_HEADER_BIT2) {
                Iterator<SensorUnit> oSUUnitsItr = suList.values().iterator();
                while (oSUUnitsItr.hasNext()) {
                    SensorUnit oSU = oSUUnitsItr.next();
                    oSU.postToSU((int) txnId, msgType, msgArgs);
                }
                //TODO for Plugload image upgrade
            }else {
                oBuf.append(" (Multicast targets - ").append(iTargets).append("), ");
                String snapAddress;
                for (int count = 0; count < iTargets; count++) {
                    byte[] snapAddrArr = { suPkt[addrPos], suPkt[addrPos + 1], suPkt[addrPos + 2] };
                    addrPos += 3;
                    snapAddress = Utils.getSnapAddr(snapAddrArr);
                    oBuf.append("(").append(snapAddress).append("), "); 
                    switch(msgType) {
                    case CommandsConstants.CMD_SET_SWITCH_PARAMS:
                    case CommandsConstants.CMD_APPLY_SWITCH_PARAMS:
                        if (wdsList.get(snapAddress) != null)
                            wdsList.get(snapAddress).postToWDS((int) txnId, msgType, msgArgs);
                        break;
                    default:
                        if (suList.get(snapAddress) != null)
                            suList.get(snapAddress).postToSU((int) txnId, msgType, msgArgs);
                        else if (plList.get(snapAddress) != null) 
                            plList.get(snapAddress).postToPlugload((int) txnId, msgType, msgArgs);
                        break;
                    }
                }
                oBuf.append(" MsgType: ").append(String.format("%x ", msgType)).append(", Processing time: ")
                        .append(System.currentTimeMillis() - iStart).append(" ms");
            }
            break;
        case CommandsConstants.ZIGBEE_DISCOVERY_REQUEST:
            oBuf.append(" Discovery MsgType: ")
                    .append(String.format("%x ", CommandsConstants.ZIGBEE_DISCOVERY_REQUEST))
                    .append(", Processing time: ").append(System.currentTimeMillis() - iStart).append(" ms");
            Iterator<SensorUnit> oSUUnitsItr = suList.values().iterator();
            while (oSUUnitsItr.hasNext()) {
                SensorUnit oSU = oSUUnitsItr.next();
                oSU.postDiscovery(txnId);
            }
            Iterator<PlugloadUnit> oPLItr = plList.values().iterator();
            while (oPLItr.hasNext()) {
                PlugloadUnit oPL = oPLItr.next();
                oPL.postDiscovery(txnId);
            }
            break;
        case CommandsConstants.CMD_WDS_DISCOVERY_REQUEST:
            oBuf.append(" WDS Discovery MsgType: ")
                    .append(String.format("%x ", CommandsConstants.CMD_WDS_DISCOVERY_REQUEST))
                    .append(", Processing time: ").append(System.currentTimeMillis() - iStart).append(" ms");
            Iterator<WDSUnit> oWDSUnitsItr = wdsList.values().iterator();
            while (oWDSUnitsItr.hasNext()) {
                WDSUnit oWDS = oWDSUnitsItr.next();
                oWDS.postDiscovery(txnId);
            }
            break;
        default:
            break;
        }
        oLogger.fine(oBuf.toString());
        suPkt = null;
    }
}