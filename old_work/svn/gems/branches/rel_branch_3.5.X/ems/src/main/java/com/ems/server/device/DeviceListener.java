/**
 * 
 */
package com.ems.server.device;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        boolean poolPkt = true;

        public GwResponseWork(byte[] pkt, String ip, boolean poolPkt) {

            this.packet = pkt;
            this.gwIp = ip;
            this.poolPkt = poolPkt;

        } // end of method GwResponseWork

        public void run() {

          GatewayResponsePacket gwRespPkt = null;
            try {
                long startTime = System.currentTimeMillis();
                gwRespPkt = new GatewayResponsePacket(packet, gwIp);
                gwRespPkt.processResponse(poolPkt);
                if(timingLogger.isDebugEnabled()) {
                  timingLogger.debug("Time taken to process gateway packet : " + 
                      (System.currentTimeMillis() - startTime));
                }
                return;
            } catch (Exception e) {
                // e.printStackTrace();
                logger.error(gwIp + ": error in processing response packet- " + e.getMessage());
            }
            finally {
              packet = null;
              gwIp = null;
              gwRespPkt.finalize();
            }

        } // end of method run

    } // end of class GwResponseWork

    // this method is used to add the response packet from the gateway to the thread pool
    // it will be called from this class and SSL
    public void addGatewayResponse(byte[] pkt, String ip, boolean poolPkt) {

        GwResponseWork work = new GwResponseWork(pkt, ip, poolPkt);
        threadPool.addWork(work);
        logger.debug(ip + ": queue size - " + threadPool.getQueueSize());
        work = null;

    } // end of method addGatewatResponse
    
    public static void returnReceivePacket(byte[] pkt) {
      
      pktPool.returnPacket(pkt);
      
    }
    
    public static byte[] getPacket() {
      
      return pktPool.getPacket();
      
    }
 
    //assumption is never we will have 500 packets at the receiver
    //TODO need to correct this logic to make it more adaptive to the situation 
    static class ReceivePacketPool {
      
      private ConcurrentLinkedQueue<byte[]> pool = new ConcurrentLinkedQueue<byte[]>();
      
      public ReceivePacketPool() {
	
	for(int i = 0; i < 500; i++) {
	  byte[] pkt = new byte[128];
	  pool.add(pkt);
	}
	
      }
      
      public byte[] getPacket() {
	
	if(logger.isDebugEnabled()) {
	  logger.debug("pkts avail -- " + pool.size());
	}
	return pool.poll();
	
      } //end of method getPacket
      
      public void returnPacket(byte[] pkt) {
	
	pool.add(pkt);
	
      } //end of method returnPacket
      
    } //end of class ReceivePacketPool
    
    private static ReceivePacketPool pktPool = new ReceivePacketPool();
    
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
                DatagramPacket dp = new DatagramPacket(rcvByteArr, rcvByteArr.length);
                if(logger.isInfoEnabled()) {
                  logger.info("gateway listener socket started");
                }
                while (ServerMain.getInstance().isRunning()) {
                  byte[] pkt = null;
                    try {                     
                        long startTime = System.currentTimeMillis();
                        // byte[] rcvByteArr = new byte[128];                       
                        serverSocket.receive(dp);
                        byte[] pktLenArr = { rcvByteArr[2], rcvByteArr[3] };
                        int pktLen = ServerUtil.byteArrayToShort(pktLenArr);
                        if(pktLen > rcvByteArr.length) {
                          //received a garbage packet. normally packets from gateway/sensors does not 
                          //be of size 128
                          logger.error(dp.getAddress() + ": rcvd pkt(garb) - " + 
                              ServerUtil.getLogPacket(rcvByteArr));
                          continue;
                        }
                        //pkt = new byte[pktLen];
                        boolean poolPkt = true;
                        pkt = pktPool.getPacket();
                        if(pkt == null) {
                          logger.error("Packet pool is not sufficient");
                          //ServerUtil.sleepMilli(10);
                          //continue;
                          pkt = new byte[pktLen];
                          poolPkt = false;
                        }
                        System.arraycopy(rcvByteArr, 0, pkt, 0, pktLen);
                        addGatewayResponse(pkt, dp.getAddress().getHostAddress(), poolPkt);
                        if(timingLogger.isDebugEnabled()) {
                          timingLogger.debug("Packet Received at Gems : " + 
                              (System.currentTimeMillis() - startTime));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } 
                    finally {
                      pkt = null;
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
                if(logger.isInfoEnabled()) {
                  logger.info("CLEANUP GwListenerDaemon thread pool stopped");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void cleanUp() {
          if(logger.isInfoEnabled()) {
            logger.info("Device Listener cleanup");
          }
            gwListenerDaemon.closeSocket();

        }

    } // end of class GwListenerDaemon

} // end of class DeviceListener
