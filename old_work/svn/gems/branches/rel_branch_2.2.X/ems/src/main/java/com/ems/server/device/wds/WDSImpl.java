/**
 * 
 */
package com.ems.server.device.wds;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Gateway;
import com.ems.model.GemsGroup;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.server.device.CommandScheduler;
import com.ems.server.device.DeviceListener;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayComm;
import com.ems.server.device.McastPacket;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;
import com.ems.service.GemsGroupManager;
import com.ems.service.WdsManager;
import com.ems.types.GGroupType;

/**
 * @author yogesh
 * 
 */
public class WDSImpl {
    private static Logger logger = Logger.getLogger("SwitchLogger");

    private static WDSImpl instance = null;
    private DeviceListener deviceListener = new DeviceListener();
    private GatewayManager gwMgr = null;
    private WdsManager wdsMgr = null;
    private GemsGroupManager gemsGroupMgr = null;
    private static Logger discLogger = Logger.getLogger("Discovery");

    private WDSImpl() {
        gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
        wdsMgr = (WdsManager) SpringContext.getBean("wdsManager");
        gemsGroupMgr = (GemsGroupManager) SpringContext.getBean("gemsGroupManager");
    }

    public static WDSImpl getInstance() {
        if (instance == null) {
            synchronized (WDSImpl.class) {
                if (instance == null) {
                    instance = new WDSImpl();
                }
            }
        }
        return instance;
    }

    public DeviceListener getDeviceListener() {
        return deviceListener;
    }

    public void discover(long floorId, long gatewayId) {
        try {
            Gateway gw = gwMgr.loadGateway(gatewayId);
            if (gw != null) {
                // if the gateway is not commissioned, don't allow to discover the nodes
                if (!gw.isCommissioned()) {
                    return;
                }
                byte[] header = DeviceServiceImpl.getInstance().getHeader(3, ServerConstants.CMD_SWITCH_DISCOVER,
                        "1.2", null);
                byte[] pkt = new byte[header.length + 4];
                System.arraycopy(header, 0, pkt, 0, header.length);
                pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;

                byte[] snapAddr = ServerUtil.getSnapAddr(gw.getSnapAddress());
                System.arraycopy(snapAddr, 0, pkt, header.length, snapAddr.length);
                // if (discLogger.isDebugEnabled()) {
                discLogger.debug("wds discover req: " + ServerUtil.getLogPacket(pkt));
                // }
                // ServerUtil.logPacket("discover req -- ", pkt, discLogger);
                GatewayComm.getInstance().sendWDSDataToGateway(gatewayId, gw.getIpAddress(), pkt);
                // if (discLogger.isDebugEnabled()) {
                discLogger.debug("after mcast of wds discovery");
                // }
            }
        } catch (Exception ex) {
            discLogger.debug(ex.getMessage());
        }

    } // end of method discover

    ////////////////////////////////////////////////////////////////////////////////
    // TODO: CommandScheduler short circuited here, need to have a generic device
    //       interface for this to function
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * sets wireless params on the target WDS (currently, without retry) 
     * @param wdsId
     * @param gatewayId
     */
    public int setWdsWirelessParams(long wdsId, long gatewayId) {
        Wds oWds = wdsMgr.getWdsSwitchById(wdsId);
        if (oWds == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            logger.error(wdsId + ": There is no EWS");
            return 1;
        }
        // Send details to WDS
        Gateway gw = gwMgr.loadGateway(gatewayId);
        byte[] dataPkt = new byte[26];
        int i = 0;
        // Wds Id
        int wdsno =  Integer.parseInt(oWds.getWdsNo().toString(), 16); // Group no has version and type included.
        logger.info("Configuring EWS: " + oWds.getName() + " with wdsID: " + wdsno);
        byte[] switchGroupIdArr = ServerUtil.intToByteArray(wdsno);
        System.arraycopy(switchGroupIdArr, 0, dataPkt, i, switchGroupIdArr.length);
        i += 4;
        // Channel no
        dataPkt[i++] = gw.getChannel().byteValue();
        // System.out.println("gw channel -- " + gw.getChannel().byteValue());
        if (logger.isDebugEnabled()) {
            logger.debug(wdsId + ": gw channel -- " + gw.getChannel().byteValue());
        }
        // radio rate
        dataPkt[i++] = gw.getWirelessRadiorate().byteValue();
        // network id
        byte[] tempShortArr = ServerUtil.shortToByteArray(gw.getWirelessNetworkId());
        System.arraycopy(tempShortArr, 0, dataPkt, i, tempShortArr.length);
        i += 2;
        // enc key
        String key = gw.getWirelessEncryptKey();
        byte[] keyArr = new byte[17];
        System.arraycopy(key.getBytes(), 0, keyArr, 0, key.length());
        keyArr[key.length()] = 0;
        System.arraycopy(keyArr, 0, dataPkt, i, keyArr.length);
        i += 17;
        if (logger.isDebugEnabled()) {
            logger.debug("encryption type == " + gw.getWirelessEncryptType().byteValue());
        }
        // enc type
        byte keyType = gw.getWirelessEncryptType().byteValue();
        if (keyType == 1) {
            keyType = 2;
        }
        dataPkt[i++] = keyType;
        
		if (logger.isDebugEnabled()) {
            logger.debug(wdsId + ": " + ServerUtil.getLogPacket(dataPkt));
        }
        CommandScheduler.getInstance().addCommand(oWds, dataPkt, 
        		ServerConstants.CMD_SET_SWITCH_PARAMS, true, DeviceServiceImpl.UNICAST_PKTS_DELAY);
        
        /*
        McastPacket oPacket = new McastPacket();
        oPacket.setTransactionId(DeviceServiceImpl.getNextSeqNo());
        oPacket.addMcastTarget(ServerUtil.getSnapAddr(oWds.getMacAddress()));
        oPacket.setCommand((byte)ServerConstants.CMD_SET_SWITCH_PARAMS, dataPkt);
        if (logger.isDebugEnabled()) {
            logger.debug(wdsId + ": " + ServerUtil.getLogPacket(oPacket.getPacket()));
        }
        GatewayComm.getInstance().sendWDSDataToGateway(gatewayId, gw.getIpAddress(), oPacket.getPacket());
        oPacket = null;
        */
        return 0;
    }

    /**
     * Applies wireless params on the WDS (currently without retry)
     * @param wdsId
     * @param gatewayId
     */
    public void applyWireless(long wdsId, long gatewayId) {
        Wds oWds = wdsMgr.getWdsSwitchById(wdsId);
        if (oWds == null) {
            // System.out.println("There is no Fixture with the fixture Id: " + fixtureId);
            logger.error(wdsId + ": There is no EWS");
            return;
        }
        byte[] dataPkt = new byte[1];
        dataPkt[0] = (byte) 15; // default delay of 15 sec
        Gateway gw = gwMgr.loadGateway(gatewayId);
        
        CommandScheduler.getInstance().addCommand(oWds, dataPkt, 
        		ServerConstants.CMD_APPLY_SWITCH_PARAMS, true, DeviceServiceImpl.UNICAST_PKTS_DELAY);
        
        /*
        McastPacket oPacket = new McastPacket();
        oPacket.setTransactionId(DeviceServiceImpl.getNextSeqNo());
        oPacket.addMcastTarget(ServerUtil.getSnapAddr(oWds.getMacAddress()));
        oPacket.setCommand((byte)ServerConstants.CMD_APPLY_SWITCH_PARAMS, dataPkt);
        GatewayComm.getInstance().sendWDSDataToGateway(gatewayId, gw.getIpAddress(), oPacket.getPacket());
        oPacket = null;
        */
    }
    
    public void sendMulticastPacket(ArrayList<Wds> wdsList, int msgType, byte[] dataPacket, boolean retryReq,
        int sleepMillis) {

    	long gwId = wdsList.get(0).getGatewayId();
    	Gateway gw = gwMgr.loadGateway(gwId);
    	if (gw == null) {
    		// System.out.println(gwId + ": there is no gateway with the id");
    		logger.error(gwId + ": There is no Gateway");
    		return;
    	}

    	int noOfWdss = wdsList.size();
    	byte[] snapAddr = new byte[noOfWdss * 3];
    	Wds wds = null;
    	for (int i = 0; i < noOfWdss; i++) {
        wds = wdsList.get(i);
        System.arraycopy(ServerUtil.getSnapAddr(wds.getMacAddress()), 0, snapAddr, i * 3, 3);
    	}

    	byte[] header = DeviceServiceImpl.getInstance().getMulticastHeader(dataPacket.length, msgType, snapAddr);

    	byte[] packet = new byte[header.length + dataPacket.length + 1];
    	System.arraycopy(header, 0, packet, 0, header.length);
    	System.arraycopy(dataPacket, 0, packet, header.length, dataPacket.length);
    	packet[packet.length - 1] = ServerConstants.FRAME_END_MARKER;
    	if(logger.isDebugEnabled()) {
    		logger.debug("multicast packet: " + ServerUtil.getLogPacket(packet));
    	}
    	GatewayComm.getInstance().sendWDSDataToGateway(gwId, gw.getIpAddress(), packet);   
    	ServerUtil.sleepMilli(sleepMillis);

    } // end of method sendMulticastPacket
    
    // this is for broadcast image upgrade data packet for wds
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
    			byte[] header = DeviceServiceImpl.getInstance().getMulticastHeader(dataPacket.length, msgType, snapAddr);
    			byte[] pkt = new byte[header.length + dataPacket.length + 1];
    			System.arraycopy(header, 0, pkt, 0, header.length);
    			pkt[pkt.length - 1] = ServerConstants.FRAME_END_MARKER;
    			
    			System.arraycopy(dataPacket, 0, pkt, header.length, dataPacket.length);
    			if(logger.isDebugEnabled()) {
    				logger.debug("image upgrade data packet: " + ServerUtil.getLogPacket(pkt));
    			}
    			//ServerUtil.logPacket("image upgrade data packet", pkt, fixtureLogger);
    			GatewayComm.getInstance().sendWDSDataToGateway(gwId, gw.getIpAddress(), pkt);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}

    } // end of method broadcastImageUprade
    
}
