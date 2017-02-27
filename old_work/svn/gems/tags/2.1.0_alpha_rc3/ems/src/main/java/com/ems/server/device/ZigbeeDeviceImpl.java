/**
 * 
 */
package com.ems.server.device;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Gateway;
import com.ems.server.ServerConstants;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;

/**
 * @author EMS
 * 
 */
public class ZigbeeDeviceImpl {

    private static ZigbeeDeviceImpl instance = null;

    private DeviceListener deviceListener = new DeviceListener();

    private GatewayManager gwMgr = null;

    private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
    private static Logger discLogger = Logger.getLogger("Discovery");

    /**
     * constructor
     */
    private ZigbeeDeviceImpl() {

        gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");

    } // end of constructor

    public static ZigbeeDeviceImpl getInstance() {

        if (instance == null) {
            synchronized (ZigbeeDeviceImpl.class) {
                if (instance == null) {
                    instance = new ZigbeeDeviceImpl();
                }
            }
        }
        return instance;

    } // end of method getInstance

    public DeviceListener getDeviceListener() {

        return deviceListener;

    } // end of method getDeviceListener

    // this is the discover method sent through enlighted gw
    public void discover(long floorId) {

        try {
            List<Gateway> gwList = gwMgr.loadFloorGateways(floorId);
            Iterator<Gateway> gwIter = gwList.iterator();
            while (gwIter.hasNext()) {
                Gateway gw = gwIter.next();
                discover(floorId, gw.getId());
            }
            fixtureLogger.debug("after mcast of discovery");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method discover

    // this is the discover method sent through enlighted gw
    public void discover(long floorId, long gatewayId) {

        try {
            Gateway gw = gwMgr.loadGateway(gatewayId);
            if (gw != null) {
                // if the gateway is not commissioned, don't allow to discover the nodes
                if (!gw.isCommissioned()) {
                    return;
                }
                byte[] header = DeviceServiceImpl.getInstance().getHeader(3, ServerConstants.ZIGBEE_DISCOVERY_REQUEST,
                        "1.2", null);
                byte[] pkt = new byte[header.length + 4];
                System.arraycopy(header, 0, pkt, 0, header.length);
                pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;

                byte[] snapAddr = ServerUtil.getSnapAddr(gw.getSnapAddress());
                System.arraycopy(snapAddr, 0, pkt, header.length, snapAddr.length);
                ServerUtil.logPacket("discover req -- ", pkt, discLogger);
                GatewayComm.getInstance().sendNodeDataToGateway(gatewayId, gw.getIpAddress(), pkt);
                discLogger.debug("after mcast of discovery");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method discover

    // this is for broadcast image upgrade data packet
    // this is a multicast packet where node addresses are not
    // put but special image upgrade marker 0x7f, 0x6f follwed
    // by file size is put and sent as 2 addresses
    public void broadcastImageUprade(long gwId, byte[] dataPacket, int msgType, int fileSize) {

        try {
            Gateway gw = gwMgr.loadGateway(gwId);
            if (gw != null) {
                byte[] snapAddr = new byte[6];
                snapAddr[0] = 0x7f;
                snapAddr[1] = 0x6f;
                // convert to little endian
                // ByteBuffer bb = ByteBuffer.allocate(4);
                // bb.order(ByteOrder.LITTLE_ENDIAN);
                // bb.putInt(fileSize);
                // byte[] fileSizeArr = bb.array();
                // System.arraycopy(fileSizeArr, 0, snapAddr, 2, fileSizeArr.length);

                ServerUtil.fillIntInByteArray(fileSize, snapAddr, 2);
                byte[] header = DeviceServiceImpl.getInstance()
                        .getMulticastHeader(dataPacket.length, msgType, snapAddr);
                byte[] pkt = new byte[header.length + dataPacket.length + 1];
                System.arraycopy(header, 0, pkt, 0, header.length);
                pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;

                System.arraycopy(dataPacket, 0, pkt, header.length, dataPacket.length);
                ServerUtil.logPacket("image upgrade data packet", pkt, fixtureLogger);
                GatewayComm.getInstance().sendNodeDataToGateway(gwId, gw.getIpAddress(), pkt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // end of method broadcastImageUprade

} // end of class ZigbeeDeviceImpl
