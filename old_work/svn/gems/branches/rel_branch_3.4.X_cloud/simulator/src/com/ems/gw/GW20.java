/**
 * 
 */
package com.ems.gw;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.ems.commands.CommandsConstants;
import com.ems.commands.GWAckFrame;
import com.ems.commands.GatewayPkt;
import com.ems.plugload.PlugloadUnit;
import com.ems.su.Packets;
import com.ems.su.SensorUnit;
import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;
import com.ems.wds.WDSUnit;

/**
 * @author yogesh
 * 
 */
public class GW20 implements GWInterface {

    byte[] gwPacket = null;
    private String strIPAddress;
    private String strGwInterface;
    private String strGWIPAddress;
    InetAddress m_gwIPAddress;
    private String m_gwMac;
    public Logger oLogger = Logger.getLogger(GW20.class.getName());
    private int iPktCount = 0;
    private static int iCount = 0;
    private static int iPMStatsCount = 0;
    private static long iSUHBStatsCount = 0;
    private String tempData;
    private int commonQueueNo;
    public boolean isKeepRunning = true;
    InetAddress IPAddress;
    public HashMap<String, SensorUnit> suList;
    public HashMap<String, WDSUnit> wdsList;
    public HashMap<String, PlugloadUnit> plList;
    Thread suCmdProcessor;
    Thread suLowPriorityCmdProcessor;
    Thread gw20Server;
    Thread gwReceiver;
    Thread gwTransmitter;
    volatile Thread blinker;
    private static SSLServerSocketFactory tlsSrvSocketFactory = null;
    private boolean bHandshakeDone = false;
    private CommonQueue cQueue = null;
    public DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public GW20(int pktCount, String gwInterface, InetAddress gwIPAddress, String ipAddress) {
        if (pktCount == 0)
            iCount = 1;
        iPktCount = pktCount;
        strGwInterface = gwInterface;
        strIPAddress = ipAddress;
        m_gwIPAddress = gwIPAddress;
        Utils.setCommonQueue(new CommonQueue());
        commonQueueNo = Utils.getCommonQueueArrayCount() - 1;
        cQueue = Utils.getCommonQueue(getCommonQueueNo()); 
    }

    public InetAddress getM_gwIPAddress() {
        return m_gwIPAddress;
    }

    public void setM_gwIPAddress(InetAddress m_gwIPAddress) {
        this.m_gwIPAddress = m_gwIPAddress;
    }

    public GW20(int pktCount, String gwInterface, InetAddress gwIPAddress, String sMac, String ipAddress) {
        if (pktCount == 0)
            iCount = 1;
        iPktCount = pktCount;
        strGwInterface = gwInterface;
        strIPAddress = ipAddress;
        m_gwIPAddress = gwIPAddress;
        m_gwMac = sMac;
        Utils.setCommonQueue(new CommonQueue());
        commonQueueNo = Utils.getCommonQueueArrayCount() - 1;
        cQueue = Utils.getCommonQueue(getCommonQueueNo()); 
        try {
            IPAddress = InetAddress.getByName(strIPAddress);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void init() {

        SecureRandom secRand = new SecureRandom();
        try {
            SSLContext tlsSc = SSLContext.getInstance("TLSv1");

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = "enLighted".toCharArray();
            InputStream in = (new FileInputStream("server.jks"));
            ks.load(in, password);
            in.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            // System.out.println(">>" + KeyManagerFactory.getDefaultAlgorithm());
            // KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            tlsSc.init(kmf.getKeyManagers(), new TrustManager[] { new SSLTrustManager() }, secRand);
            // tlsSc.init(null, new TrustManager[] { new SSLTrustManager() }, secRand);
            tlsSrvSocketFactory = tlsSc.getServerSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGW20() {
        isKeepRunning = true;
        try {
            suCmdProcessor = new Thread(new SUCommandProcessor(this));
            suCmdProcessor.start();

            gw20Server = new Thread(new GW2Server(this));
            gw20Server.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void stopGW20() {

        isKeepRunning = false;
        gw20Server = null;
        gwReceiver = null;
        gwTransmitter = null;
        suCmdProcessor = null;
    }

    public void on() {
        startGW20();
    }

    public void off() {

        stopGW20();

    }

    public Thread getKeepRunningServer() {
        return this.gw20Server;
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
        return cQueue;
    }

    public void setGateWayQueue(CommonQueue cq) {
        Utils.setCommonQueue(cq);
        this.commonQueueNo = Utils.getCommonQueueArrayCount();
    }

    public boolean isHandshakeDone() {
        return bHandshakeDone;
    }
    
    public synchronized void sendData(OutputStream os) throws IOException {
        Packets oPacket = cQueue.pop();
        if (bHandshakeDone) {
            gwPacket = oPacket.getPacketData();
            os.write(gwPacket);
            Date oNow = new Date();
            if (oPacket.getiType() == Packets.PM_STAT_PKT)
                oLogger.fine(formatter.format(oNow) + " Sent PMstat: (" + (iPMStatsCount) + " pkts) " + Utils.getPacket(gwPacket));
            else if (oPacket.getiType() == Packets.SU_REAL_TIME_HB_PKT)
                oLogger.fine(formatter.format(oNow) + " Sent HB: (" + (iSUHBStatsCount++) + " pkts) " + Utils.getPacket(gwPacket));
            else
                oLogger.fine(formatter.format(oNow) + " Sent Other: (Type = " + oPacket.getiType() + ", Priority: " + oPacket.getiPriority()
                        + ", Total: " + iCount + " pkts) " + Utils.getPacket(gwPacket));
    
            gwPacket = null;
            // overflow protection
            if (iPMStatsCount == Utils.getNoOfSensorUnits())
                iPMStatsCount = 0;
    
            if (iCount == Integer.MAX_VALUE) {
                iPktCount = 0;
                iCount = 0;
            }
            if (iSUHBStatsCount == Long.MAX_VALUE) {
                iSUHBStatsCount = 0;
            }
        }else {
            if (oPacket.getiType() == Packets.GW_HANDSHAKE_PKT) {
                gwPacket = oPacket.getPacketData();
                os.write(gwPacket);
                oLogger.info("GW Handshake: " + Utils.getPacket(gwPacket));
                bHandshakeDone = true;
            }else {
                oLogger.finest("Handshake not done - Pkt discarded: " + oPacket.getiType());
            }
        }
    }

    public byte[] getSSLAuthKey(String authKey) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update("EnlightedAuthKey".getBytes());
            md.update(authKey.getBytes());
            md.update(strGwInterface.replaceAll(":", "").getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    class GW2Server implements Runnable {
        private GW20 gw;
        
        public GW2Server(GW20 gw) {
            this.gw = gw;
        }
        
        @Override
        public void run() {
            try {
                init();
                SSLServerSocket ssocket = (SSLServerSocket) tlsSrvSocketFactory.createServerSocket();
                ssocket.bind(new InetSocketAddress(m_gwIPAddress, 8085));

                String[] suites = ssocket.getSupportedCipherSuites();
                for (int i = 0; i < suites.length; i++) {
                    if (suites[i].startsWith("TLS_RSA")) {
                        System.out.println(suites[i]);
                    }
                }

                String[] protocols = ssocket.getSupportedProtocols();
                for (int i = 0; i < protocols.length; i++) {
                    System.out.println(protocols[i]);
                }
                ssocket.setEnableSessionCreation(true);
                ssocket.setWantClientAuth(false);
                ssocket.setNeedClientAuth(false);
                ssocket.setUseClientMode(false);
                ssocket.setEnabledProtocols(new String[] { "TLSv1" });
                ssocket.setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_AES_128_CBC_SHA" });
                // ssocket.setEnabledProtocols(ssocket.getEnabledProtocols());
                // ssocket.setEnabledCipherSuites(ssocket.getSupportedCipherSuites());

                // Listen for connections
                SSLSocket socket = null;
                while ((socket = (SSLSocket) ssocket.accept()) != null) {
                    try {
                        bHandshakeDone = false;
                        gwReceiver = null;
                        gwTransmitter = null;
                        cQueue.flush();
                        Thread.sleep(10);
                        System.out.println(new Date() + " got the client socket connection " + socket.toString()
                                + " Queue size: " + cQueue.size());
                        socket.startHandshake();
                        socket.setSoTimeout(360 * 1000);
                        socket.setKeepAlive(true);

                        gwReceiver = new Thread(new GW2Receiver(m_gwMac + "_R", gw, socket));
                        gwReceiver.start();

                        gwTransmitter = new Thread(new GW2Transmitter(m_gwMac + "_T", gw, socket));
                        gwTransmitter.start();
                        System.out.println(new Date() + " waiting for new connection");
                    } catch (SSLHandshakeException sslhse) {
                        System.out.println(sslhse.getMessage());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    } finally {
                        
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}

class GW2Receiver implements Runnable {
    SSLSocket clientSocket = null;
    GW20 gw;
    InputStream in = null;
    boolean bProcess = true;
    int rcvdHeaderLen = 0;
    int expLen = 0;
    int currLen = 0;
    byte firstLen = 0;
    byte[] rcvdPkt = null;  
    long packetFormationTime = 0L;


    public GW2Receiver(String sName, GW20 gw, SSLSocket socket) {
        Thread.currentThread().setName(sName);
        clientSocket = socket;
        this.gw = gw;
    }

    public void run() {
        try {

            int CMD_MARKER_POS = 9;
            int len = 0;
            in = clientSocket.getInputStream();
            byte[] rcvByteArr = null;
            while (gw.getKeepRunningreciever() == Thread.currentThread()) {
                rcvByteArr =  new byte[128];
                len = receiveCommand(in, rcvByteArr);
                if (gw.getKeepRunningreciever() == null) {
                    rcvByteArr = null;
                    rcvdPkt = null;
                    break;
                }
                if (gw.isHandshakeDone() == false) {
                    if (rcvByteArr[0] != CommandsConstants.GW_MAGIC_FIRST
                            && rcvByteArr[1] != CommandsConstants.GW_MAGIC_SECOND) {
                        // it is not a command. it is a authentication key
                        byte[] authAck = gw.getSSLAuthKey("enLightedWorkNow");
                        if (authAck != null) {
                            gw.oLogger.fine("GW Handshake: " + clientSocket.getLocalAddress().getHostAddress() + " "
                                    + Utils.getPacket(authAck) + ", " + Utils.getPacket(rcvByteArr));
                            gw.getGatewayQueue().push(
                                    new Packets("GW Handshake", "", Packets.GW_HANDSHAKE_PKT, Packets.PRIORITY_LEVEL_FOUR,
                                            rcvByteArr));
                        }
                        continue;
                    }
                }
                if(!processData(rcvByteArr, len)) {
                    continue;
                }
                if (rcvdPkt == null)
                    return;

                long iStart = System.currentTimeMillis();
                gw.oLogger.fine("Received from " + clientSocket.getLocalAddress().getHostAddress() + " " + Utils.getPacket(rcvdPkt));
                bProcess = true;
                if (rcvdPkt[0] == CommandsConstants.GW_MAGIC_FIRST
                        && rcvdPkt[1] == CommandsConstants.GW_MAGIC_SECOND) {
                    if (rcvdPkt[CMD_MARKER_POS] == CommandsConstants.FRAME_NEW_START_MARKER) {
                        // Process other su commands here
                        byte[] suPkt = new byte[128];
                        System.arraycopy(rcvdPkt, 0, suPkt, 0, rcvdPkt.length);
                        Utils.getSUMsgQueue().push(suPkt);
                    } else {
                        // Gateway Commands
                        if (rcvdPkt[CMD_MARKER_POS] == CommandsConstants.GATEWAY_CMD_INFO) {
                            gw.oLogger.fine("Gateway cmd info");
                        } else if (rcvdPkt[CMD_MARKER_POS] == CommandsConstants.GATEWAY_WIRELESS_CMD) {
                            gw.oLogger.info("Gateway set wireless params");
                            int txPos = CMD_MARKER_POS + 1;
                            byte[] txId = { rcvdPkt[txPos], rcvdPkt[txPos + 1], rcvdPkt[txPos + 2],
                                    rcvdPkt[txPos + 3] };
                            long txnId = Utils.intByteArrayToLong(txId);
                            GatewayPkt gatewayPkt = new GatewayPkt(GatewayPkt.PKT_GW);
                            gatewayPkt.setCommandPkt(new GWAckFrame((int) txnId, (byte) 0, rcvdPkt[CMD_MARKER_POS]));
                            gw.getGatewayQueue().push(
                                    new Packets("GW Ack", "", Packets.GW_ACK_PKT, Packets.PRIORITY_LEVEL_THREE,
                                            gatewayPkt.toBytes("")));
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                gw.oLogger.warning("Interrupted during wireless params switch!");
                            }
                        } else {
                            gw.oLogger.warning("Not implemented: " + rcvdPkt[CMD_MARKER_POS]);
                        }
                    }
                }
                rcvdPkt = null;
                gw.oLogger.finer("Processing time: " + (System.currentTimeMillis() - iStart));
            }
        } catch (IOException e) {
            gw.oLogger.severe(e.getMessage());
        } finally {
            gw.oLogger.info("Stopping GW Receiver");
            if (in != null) {
                gw.oLogger.info("Stopping GWR 1...");
                try {
                    in.close();
                    in = null;
                    clientSocket.close();
                    gw.oLogger.info("Stopping GWR 2...");
                } catch (IOException e) {
                    gw.oLogger.severe(e.getMessage());
                }finally {
                    in = null;
                    clientSocket = null;
                }
            }
        }
    }

    private int receiveCommand(InputStream in, byte[] rcvByteArr) {
        int len = 0;
        try {
            len = in.read(rcvByteArr);
            if (len != -1) {
                gw.oLogger.finest("Bytes read: " + len);
                return len;
            }
        } catch (Exception e) {
            gw.oLogger.severe(e.getMessage());
        } finally {
        }
        return 0;
    }
    
    private boolean processData(byte[] data, int len) {
        int currBuffPos = 0;
        gw.oLogger.finest("Process Pkt: (" + len + ", " + rcvdHeaderLen + ", " + expLen + ", " + currLen + ") - " + Utils.getPacket(data));

        for (; len > 0;) {
            if (rcvdHeaderLen == 4) {
                // header is received
                int left = expLen - currLen;
                int toCopy = len > left ? left : len;
                System.arraycopy(data, currBuffPos, rcvdPkt, currLen + 4, toCopy);
                len -= toCopy;
                currBuffPos += toCopy;
                currLen += toCopy;

                if (currLen == expLen) {
                    // got the full packet, send for processing it
                    rcvdHeaderLen = 0;
                    expLen = 0;
                    currLen = 0;
                    packetFormationTime = System.currentTimeMillis() - packetFormationTime;
                    gw.oLogger.fine("ASSEMBLE (" + packetFormationTime + ") " + Utils.getPacket(rcvdPkt));
                    packetFormationTime = 0;
                    return true;
                }
            } else {
                // we don't have the header yet
                byte c = data[currBuffPos++];
                //gw.oLogger.info("(" + c + ", " + rcvdHeaderLen + ", " + expLen + ")");
                len--;
                switch (rcvdHeaderLen) {
                case 0:
                    // first byte e
                    if (c == 0x65) {
                        rcvdHeaderLen = 1;
                        packetFormationTime = System.currentTimeMillis();
                    }
                    break;
                case 1:
                    // second byte s
                    if (c == 0x73) {
                        rcvdHeaderLen = 2;
                    } else {
                        rcvdHeaderLen = 0;
                    }
                    break;
                case 2:
                    // first byte of len
                    firstLen = c;
                    expLen = c << 8;
                    rcvdHeaderLen = 3;
                    break;
                case 3:
                    // second byte of len
                    expLen |= (c & 0xFF) - 4;
                    rcvdHeaderLen = 4;
                    currLen = 0;
                    rcvdPkt = new byte[expLen + 4];
                    rcvdPkt[0] = 0x65;
                    rcvdPkt[1] = 0x73;
                    rcvdPkt[2] = firstLen;
                    rcvdPkt[3] = c;
                    break;
                }
            }
        }
        return false;
    }
}

class GW2Transmitter implements Runnable {
    SSLSocket clientSocket = null;
    GW20 gw;
    OutputStream os = null;

    public GW2Transmitter(String sName, GW20 gw, SSLSocket socket) {
        Thread.currentThread().setName(sName);
        this.gw = gw;
        clientSocket = socket;
    }

    public void run() {
        try {
            os = clientSocket.getOutputStream();
            while (gw.getKeepRunningtransmitter() == Thread.currentThread()) {
                synchronized(gw.getGatewayQueue().getWaitNotifyLock()) {
                    gw.getGatewayQueue().getWaitNotifyLock().notify();
                    gw.oLogger.finest(gw.formatter.format(new Date()) +  " aquiring Lock");
                    gw.getGatewayQueue().getWaitNotifyLock().wait();
                    gw.oLogger.finest(gw.formatter.format(new Date()) +  " releasing Lock");
                }
                while (!gw.getGatewayQueue().isQueueEmpty()) {
                    if (gw.getKeepRunningtransmitter() == null)
                        break;
                    try {
                        gw.sendData(os);
                    }catch(Exception e) {
                        gw.oLogger.warning(e.getMessage());
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ie) {

                    }
                }
                gw.oLogger.fine("no packets in the queue");
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
            gw.oLogger.info("Stopping GW Transmitter");
            if (os != null) {
                gw.oLogger.info("Stopping GWT 1...");
                try {
                    os.close();
                    clientSocket.close();
                    gw.oLogger.info("Stopping GWT 2...");
                } catch (IOException e) {
                    gw.oLogger.warning(e.getMessage());
                } finally {
                    os = null;
                    clientSocket = null;
                }
            }
        }

    }
}

class SSLTrustManager implements X509TrustManager {

    /**
             * 
             */
    public SSLTrustManager() {
        // TODO Auto-generated constructor stub
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

} // end of class SSLTrustManager
