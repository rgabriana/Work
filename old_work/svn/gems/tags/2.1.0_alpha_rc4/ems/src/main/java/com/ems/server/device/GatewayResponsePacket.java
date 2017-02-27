package com.ems.server.device;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ems.server.GatewayInfo;
import com.ems.server.GwStatsSO;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.discovery.DiscoverySO;
import com.ems.server.upgrade.ImageUpgradeSO;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;

public class GatewayResponsePacket {

    private byte[] gwPkt;
    private String gwIp = null;

    private static final Logger logger = Logger.getLogger("CommLog");
    private static Logger timingLogger = Logger.getLogger("TimingLogger");

    private boolean isPacketDestinedForGEMS() {

        if (gwPkt.length < ServerConstants.GW_HEADER_FLAGS_POS + 1) {
            return false;
        }
        byte flags = gwPkt[ServerConstants.GW_HEADER_FLAGS_POS];
        if ((flags & 2) != 2) {
            // destination is not gems so ignore the packet
            return false;
        }
        return true;

    } // end of method isPacketDestinedForGEMS

    private boolean isSUPacket() {

        byte flags = gwPkt[ServerConstants.GW_HEADER_FLAGS_POS];
        if (flags == 2) {
            return true;
        }
        return false;

    } // end of method isSUPacket

    public GatewayResponsePacket(byte[] pkt, String ip) {

        this.gwPkt = pkt;
        this.gwIp = ip;

    } // end of constructor

    // this is the function to parse the packet received from the enlighted gateway
    public void processResponse() {

        GatewayInfo gwInfo = GatewayImpl.getInstance().getGatewayInfo(gwIp);
        if (gwInfo == null) {
            logger.error(gwIp + ": There is no gateway, ignoring the gw packet - " + ServerUtil.getLogPacket(gwPkt));
            return;
        }
        gwInfo.setLastConnectivityAt(new Date());
        // parse the gw header
        if (!isPacketDestinedForGEMS()) {
            // destination is not gems so ignore the packet
            logger.info(gwIp + ": destination is not gems, ignoring the gw packet - " + ServerUtil.getLogPacket(gwPkt));
            return;
        }

        byte[] pkt = new byte[gwPkt.length - 9];
        System.arraycopy(gwPkt, 9, pkt, 0, pkt.length);
        long gwId = gwInfo.getGw().getId();
        if (isSUPacket()) {
            // parse the node packet
            if (!gwInfo.getGw().isCommissioned()) {
                logger.error(gwIp + ": is not commissioned, ignoring the node packet - "
                        + ServerUtil.getLogPacket(gwPkt));
                gwPkt = null;
                return;
            }
            gwPkt = null;
            long startTime = System.currentTimeMillis();
            new SUResponsePacket(pkt, gwIp, gwId).processResponse();
            System.out.println("time taken to process su packet - " + (System.currentTimeMillis() - startTime));
            return;
        }

        try {
            // parse gw packet
            int msgType = (pkt[0] & 0xFF);
            switch (msgType) {
            case ServerConstants.GATEWAY_INFO_RESP:
                GwStatsSO.getInstance().parseGwInfo(gwId, pkt, gwIp);
                break;

            case ServerConstants.GATEWAY_CMD_STATUS:
                logger.debug(gwIp + ": gw cmd status packet - " + ServerUtil.getLogPacket(gwPkt));
                ServerMain.getInstance().ackGatewayMessage(pkt, gwId);
                // if the fixture discovery is pending, continue it
                DiscoverySO.getInstance().receivedGwWirelessChangeAck(gwId);
                // if the gateway commission is pending, set it
                GatewayManager.receivedGwWirelessChangeAck(gwId);
                break;
            case ServerConstants.GATEWAY_FILE_XFER_STATUS:
                ImageUpgradeSO.getInstance().ackRadioFileTransfer(gwId, pkt);
                break;
            case ServerConstants.GATEWAY_PMG_RADIO_STATUS:
                ImageUpgradeSO.getInstance().ackRadioProgram(gwId, pkt);
                break;
            case ServerConstants.GATEWAY_UPGRADE_STATUS:
                logger.debug(gwIp + ": gw upgrade status packet - " + ServerUtil.getLogPacket(gwPkt));
                ServerMain.getInstance().ackGatewayMessage(pkt, gwId);
                ImageUpgradeSO.getInstance().ackGatewayImageUpgrade(gwId, pkt);
                break;
            default:
                if (msgType == ServerConstants.FRAME_NEW_START_MARKER) {
                    // there is no gateway command msg type so probably it is a image upgrade command
                    int cmdMsgTypePos = ServerConstants.RES_CMD_PKT_MSG_TYPE_POS;
                    cmdMsgTypePos -= 3; // as gw packet will not have node id
                    int cmdMsgType = (pkt[cmdMsgTypePos] & 0xFF);
                    switch (cmdMsgType) {
                    case ServerConstants.RESEND_REQUEST:
                        ImageUpgradeSO.getInstance().gwMissingPacketRequest(gwInfo.getGw(), pkt);
                        break;
                    case ServerConstants.ABORT_ISP_OPCODE:
                        ImageUpgradeSO.getInstance().gwCancelFileUpload(gwInfo.getGw(), pkt);
                        break;
                    case ServerConstants.ACK_TO_MSG:
                        int msgStartPos = cmdMsgTypePos + 1;
                        if ((pkt[msgStartPos] & 0xFF) == ServerConstants.ISP_INIT_ACK_OPCODE) {
                            ImageUpgradeSO.getInstance().gwAckImageUploadStart(gwInfo.getGw());
                        }
                        break;
                    }
                } else {
                    logger.error(gwIp + ": Unknow message(" + msgType + ")");
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gwPkt = null;
        }

    } // end of method parseGwPacket

} // end of class GatewayResponsePacket
