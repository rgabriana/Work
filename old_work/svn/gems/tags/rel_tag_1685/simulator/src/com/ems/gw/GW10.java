package com.ems.gw;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GWAckFrame;
import com.ems.commands.GatewayPkt;
import com.ems.su.Packets;
import com.ems.su.SensorUnit;
import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;

/**
 * @author SAMEER SURJIKAR
 */
public class GW10 {
    byte[] gwPacket = null;
    private String strIPAddress;
    private String strGwInterface;
    private String strGWIPAddress;
    InetAddress m_gwIPAddress;
    public Logger oLogger = Logger.getLogger(GW10.class.getName());
    private int iPktCount = 0;
    private static int iCount = 0;
    private static int iPMStatsCount = 0;
    private String tempData;
    private int commonQueueNo;
    public boolean isKeepRunning = true;
    InetAddress IPAddress;
    public HashMap<String, SensorUnit> suList;
    Thread suCmdProcessor;
    Thread suLowPriorityCmdProcessor;
    Thread gwReceiver;
    Thread gwTransmitter;
    volatile Thread blinker;

    public GW10(int pktCount, String gwInterface, InetAddress gwIPAddress, String ipAddress, byte[] data) {
        if (pktCount == 0)
            iCount = 1;
        iPktCount = pktCount;
        strGwInterface = gwInterface;
        strIPAddress = ipAddress;
        m_gwIPAddress = gwIPAddress;
        Utils.setCommonQueue(new CommonQueue());
        commonQueueNo = Utils.getCommonQueueArrayCount() - 1;
    }

    public InetAddress getM_gwIPAddress() {
        return m_gwIPAddress;
    }

    public void setM_gwIPAddress(InetAddress m_gwIPAddress) {
        this.m_gwIPAddress = m_gwIPAddress;
    }

    public GW10(int pktCount, String gwInterface, InetAddress gwIPAddress, String ipAddress) {
        if (pktCount == 0)
            iCount = 1;
        iPktCount = pktCount;
        strGwInterface = gwInterface;
        strIPAddress = ipAddress;
        m_gwIPAddress = gwIPAddress;
        Utils.setCommonQueue(new CommonQueue());
        commonQueueNo = Utils.getCommonQueueArrayCount() - 1;
        try {
            IPAddress = InetAddress.getByName(strIPAddress);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void startGW10() {
        isKeepRunning = true;
        try {
            suCmdProcessor = new Thread(new SUCommandProcessor(this));
            suCmdProcessor.start();
            
            gwReceiver = new Thread(new GWReceiver(strIPAddress + "_R", this));
            gwReceiver.start();
            
            gwTransmitter = new Thread(new GWTransmitter(strIPAddress + "_T", this));
            gwTransmitter.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void stopGW10() {

        isKeepRunning = false;
        gwReceiver = null;
        gwTransmitter = null;
        suCmdProcessor = null;
    }

    public void on() {
        startGW10();
    }

    public void off() {

        stopGW10();

    }

    public Thread getKeepRunningtransmitter() {
        return this.gwTransmitter;
    }

    public Thread getKeepRunningreciever() {
        return this.gwReceiver;
    }

    public Thread getKeepRunningSuCmdProcessor() {
        return this.suCmdProcessor;
    }
    
    public Thread getKeepRunningSuCmdProcessorLP() {
        return this.suLowPriorityCmdProcessor;
    }

    public int getCommonQueueNo() {
        return commonQueueNo;
    }

    public void setCommonQueueNo(int commonQueueNo) {
        this.commonQueueNo = commonQueueNo;
    }

    public CommonQueue getGatewayQueue() {
        return Utils.getCommonQueue(commonQueueNo);
    }

    public void setGateWayQueue(CommonQueue cq) {
        Utils.setCommonQueue(cq);
        this.commonQueueNo = Utils.getCommonQueueArrayCount();
    }

    public synchronized void sendData(DatagramSocket clientSocket, DatagramPacket sendPacket) throws IOException {
        Packets oPacket = Utils.getCommonQueue(commonQueueNo).pop(); 
        gwPacket = oPacket.getPacketData();
        sendPacket = new DatagramPacket(gwPacket, gwPacket.length, IPAddress, 8084);
        clientSocket.send(sendPacket);
        if (oPacket.getiType() == Packets.PM_STAT_PKT)
            oLogger.fine("Sent PMstat: (" + (++iPMStatsCount) + " pkts) " + Utils.getPacket(gwPacket));
        else
            oLogger.fine("Sent Other: (Type = " + oPacket.getiType() + ", Priority: " + oPacket.getiPriority() + ", Total: " + iCount++ + " pkts) " + Utils.getPacket(gwPacket));
        
        sendPacket = null;
        gwPacket = null;
        // overflow protection
        if (iPMStatsCount == Utils.getNoOfSensorUnits())
            iPMStatsCount = 0;
        
        if (iCount == Integer.MAX_VALUE) {
            iPktCount = 0;
            iCount = 0;
        }
    }
}

class GWReceiver implements Runnable {
    byte[] rcvByteArr = new byte[128];
    DatagramSocket serverSocket = null;
    DatagramPacket recievePacket = null;
    GW10 gw;

    public GWReceiver(String sName, GW10 gw) {
        Thread.currentThread().setName(sName);
        this.gw = gw;
    }

    public void run() {
        try {
            serverSocket = new DatagramSocket(8085, gw.m_gwIPAddress);
            serverSocket.setReceiveBufferSize(2048);
            int CMD_MARKER_POS = 9;
            boolean bProcess = true;
            while (gw.getKeepRunningreciever() == Thread.currentThread()) {

                rcvByteArr = new byte[128];
                DatagramPacket dp = new DatagramPacket(rcvByteArr, rcvByteArr.length);
                serverSocket.receive(dp);
                long iStart = System.currentTimeMillis();
                gw.oLogger.finest("Received from " + dp.getAddress());
                gw.oLogger.finest(Utils.getPacket(rcvByteArr));
                dp = null;
                bProcess = true;
                if (rcvByteArr[0] == CommandsConstants.GW_MAGIC_FIRST
                        && rcvByteArr[1] == CommandsConstants.GW_MAGIC_SECOND) {
                    if (rcvByteArr[CMD_MARKER_POS] == CommandsConstants.FRAME_NEW_START_MARKER) {
                        // Process other su commands here
                        byte[] suPkt = new byte[128];
                        System.arraycopy(rcvByteArr, 0, suPkt, 0, rcvByteArr.length);
                        Utils.getSUMsgQueue().push(suPkt);
                    } else {
                        // Gateway Commands
                        if (rcvByteArr[CMD_MARKER_POS] == CommandsConstants.GATEWAY_CMD_INFO) {
                            gw.oLogger.fine("Gateway cmd info");
                        } else if (rcvByteArr[CMD_MARKER_POS] == CommandsConstants.GATEWAY_WIRELESS_CMD) {
                            gw.oLogger.info("Gateway set wireless params");
                            int txPos = CMD_MARKER_POS + 1;
                            byte[] txId = { rcvByteArr[txPos], rcvByteArr[txPos + 1], rcvByteArr[txPos + 2],
                                    rcvByteArr[txPos + 3] };
                            long txnId = Utils.intByteArrayToLong(txId);
                            GatewayPkt gatewayPkt = new GatewayPkt((byte) 6);
                            gatewayPkt.setCommandPkt(new GWAckFrame((int) txnId, (byte) 0, rcvByteArr[CMD_MARKER_POS]));
                            Utils.getCommonQueue(gw.getCommonQueueNo()).push(
                                    new Packets("GW Ack", "", Packets.GW_ACK_PKT, Packets.PRIORITY_LEVEL_THREE, gatewayPkt.toBytes("")));
                        } else {
                            gw.oLogger.warning("Not implemented: " + rcvByteArr[CMD_MARKER_POS]);
                        }
                    }
                }
                rcvByteArr = null;
                gw.oLogger.finer("Processing time: " + (System.currentTimeMillis() - iStart));
            }
        } catch (IOException e) {
            gw.oLogger.severe(e.getMessage());
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        }
    }
}

class GWTransmitter implements Runnable {
    DatagramSocket clientSocket = null;
    DatagramPacket sendPacket = null;
    GW10 gw;

    public GWTransmitter(String sName, GW10 gw) {
        Thread.currentThread().setName(sName);
        this.gw = gw;
    }

    public void run() {
        try {
            clientSocket = new DatagramSocket(0, gw.m_gwIPAddress);
            while (gw.getKeepRunningtransmitter() == Thread.currentThread()) {

                synchronized (Utils.getCommonQueue(gw.getCommonQueueNo()).getWaitNotifyLock()) {
                    Utils.getCommonQueue(gw.getCommonQueueNo()).getWaitNotifyLock().wait();
                }
                while (!Utils.getCommonQueue(gw.getCommonQueueNo()).isQueueEmpty()) {
                    gw.sendData(clientSocket, sendPacket);
                    try {
                        Thread.sleep(5);
                    }catch(InterruptedException ie) {
                        
                    }
                }
            }
        }

        catch (SocketException e) {
            gw.oLogger.warning(e.getMessage());
        } catch (UnknownHostException e) {
            gw.oLogger.warning(e.getMessage());
        } catch (IOException e) {
            gw.oLogger.warning(e.getMessage());
        } catch (InterruptedException e) {
            gw.oLogger.warning(e.getMessage());
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
        }

    }
}

class SUCommandProcessor implements Runnable {
    GW10 gw;

    public SUCommandProcessor(GW10 gw) {
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
            gw.oLogger.warning(e.getMessage());
        } catch (UnknownHostException e) {
            gw.oLogger.warning(e.getMessage());
        } catch (IOException e) {
            gw.oLogger.warning(e.getMessage());
        } catch (InterruptedException e) {
            gw.oLogger.warning(e.getMessage());
        } finally {
        }
    }

    public synchronized void processSUMsg() throws IOException {
        gw.suList = Utils.getSensorUnits();
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
            oBuf.append(" (Multicast targets - ").append(iTargets).append("), ");
            int addrPos = GatewayPkt.POS_MCAST_TARGETS;
            int msgType = (suPkt[GatewayPkt.POS_MCAST_TARGETS + iTargets * 3] & 0xFF);
            byte[] msgArgs = new byte[suPkt.length - (GatewayPkt.POS_MCAST_TARGETS + iTargets * 3)];
            System.arraycopy(suPkt, (GatewayPkt.POS_MCAST_TARGETS + (iTargets * 3)), msgArgs, 0, msgArgs.length);
            String snapAddress;
            for (int count = 0; count < iTargets; count++) {
                byte[] snapAddrArr = { suPkt[addrPos], suPkt[addrPos + 1], suPkt[addrPos + 2] };
                addrPos += 3;
                snapAddress = Utils.getSnapAddr(snapAddrArr);
                oBuf.append("(").append(snapAddress).append("), ");
                if (gw.suList.get(snapAddress) != null)
                    gw.suList.get(snapAddress).postToSU((int) txnId, msgType, msgArgs);
            }
            oBuf.append(" MsgType: ").append(String.format("%x ", msgType)).append(", Processing time: ")
                    .append(System.currentTimeMillis() - iStart).append(" ms");
            break;
        case CommandsConstants.ZIGBEE_DISCOVERY_REQUEST:
            oBuf.append(" Discovery MsgType: ")
                    .append(String.format("%x ", CommandsConstants.ZIGBEE_DISCOVERY_REQUEST))
                    .append(", Processing time: ").append(System.currentTimeMillis() - iStart).append(" ms");
            Iterator<SensorUnit> oSUUnitsItr = gw.suList.values().iterator();
            while (oSUUnitsItr.hasNext()) {
                SensorUnit oSU = oSUUnitsItr.next();
                oSU.postDiscovery(txnId);
            }
            break;
        default:
            break;
        }
        gw.oLogger.fine(oBuf.toString());
        suPkt = null;
    }
}