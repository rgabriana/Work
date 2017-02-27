/**
 * 
 */
package com.ems.server.device;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;

import com.ems.server.EmsShutdownObserver;
import com.ems.server.ServerMain;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;

/**
 * @author
 * 
 */
public class DeviceListener {

    // private int noOfThreads = 15;

    private EmsThreadPool threadPool = null;
    private GwListenerDaemon gwListenerDaemon = null;

    // private GatewayManager gwMgr = null;

    static final Logger logger = Logger.getLogger("CommLog");
    private static Logger timingLogger = Logger.getLogger("TimingLogger");

    /**
   * 
   */
    public DeviceListener() {

        // TODO Auto-generated constructor stub
        int noOfThreads = ServerMain.getInstance().getNoOfCmdRespListenerThreads();
        threadPool = new EmsThreadPool(noOfThreads, "DeviceListener");
        gwListenerDaemon = new GwListenerDaemon();
        gwListenerDaemon.start();
        ServerMain.getInstance().addShutdownObserver(gwListenerDaemon);

        // gwMgr = (GatewayManager)SpringContext.getBean("gatewayManager");

    } // end of constructor DeviceListener

    // this is the work class for handling packets received from enlighted gateway
    public class GwResponseWork implements Runnable {

        byte[] packet = null;
        String gwIp = null;

        public GwResponseWork(byte[] pkt, String ip) {

            this.packet = pkt;
            this.gwIp = ip;

        } // end of method GwResponseWork

        public void run() {

            try {
                long startTime = System.currentTimeMillis();
                new GatewayResponsePacket(packet, gwIp).processResponse();
                timingLogger
                        .debug("Time taken to process gateway packet : " + (System.currentTimeMillis() - startTime));
                return;
            } catch (Exception e) {
                // e.printStackTrace();
                logger.error(gwIp + ": error in processing response packet- " + e.getMessage());
            }

        } // end of method run

    } // end of class GwResponseWork

    // this method is used to add the response packet from the gateway to the thread pool
    // it will be called from this class and SSL
    public void addGatewayResponse(byte[] pkt, String ip) {

        GwResponseWork work = new GwResponseWork(pkt, ip);
        threadPool.addWork(work);

    } // end of method addGatewatResponse

    // this is the daemon class listening for packets from enlighted gateway
    public class GwListenerDaemon extends Thread implements EmsShutdownObserver {

        byte[] rcvByteArr = new byte[128];
        private DatagramSocket serverSocket = null;

        public GwListenerDaemon() {
        }

        public void run() {

            try {
                serverSocket = new DatagramSocket(8084);

                serverSocket.setReceiveBufferSize(2048);
                logger.info("gateway listener socket started");
                while (ServerMain.getInstance().isRunning()) {
                    try {
                        long startTime = System.currentTimeMillis();
                        // byte[] rcvByteArr = new byte[128];
                        DatagramPacket dp = new DatagramPacket(rcvByteArr, rcvByteArr.length);
                        serverSocket.receive(dp);
                        byte[] pktLenArr = { rcvByteArr[2], rcvByteArr[3] };
                        int pktLen = ServerUtil.byteArrayToShort(pktLenArr);
                        byte[] pkt = new byte[pktLen];
                        System.arraycopy(rcvByteArr, 0, pkt, 0, pktLen);
                        addGatewayResponse(pkt, dp.getAddress().getHostAddress());
                        timingLogger.debug("Packet Received at Gems : " + (System.currentTimeMillis() - startTime));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of method run

        public void closeSocket() {

            try {
                // serverSocket.disconnect();
                serverSocket.close();
                logger.info("CLEANUP GwListenerDaemon thread pool stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void cleanUp() {
            logger.info("Device Listener cleanup");
            gwListenerDaemon.closeSocket();

        }

    } // end of class GwListenerDaemon

} // end of class DeviceListener
