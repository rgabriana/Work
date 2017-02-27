package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ems.utils.Utils;

/**
 * @author Sameer Surjikar
 * 
 */
public class GatewayPkt {
    public static final byte PKT_SU = 0x2;
    public static final byte PKT_GW = 0x6;
    public static final byte PKT_WDS = 0xe;

    public static int POS_TXID = 13;
    public static int POS_MCAST_CMD = POS_TXID + 4;
    public static int POS_MCAST_TARGETS_NO = POS_MCAST_CMD + 1;
    public static int POS_MCAST_TARGETS = POS_MCAST_TARGETS_NO + 1;

    private byte[] magic = { 'e', 's' };
    private short frame_length;
    private byte route_flags = (byte) 2;
    private int MD5_sum = 0;

    private ICommandPktFrame oFrame;

    public GatewayPkt() {
    }

    public GatewayPkt(byte route_flags) {
        this.route_flags = route_flags;
    }

    public void setCommandPkt(ICommandPktFrame framepkt) {
        oFrame = framepkt;
    }

    public void setRouteFlags(byte flags) {
        route_flags = flags;
    }
    
    public ICommandPktFrame getCommandPkt() {
        return oFrame;
    }

    public byte[] toBytes(final String nodeName) {
        frame_length = (short) (getLength() + oFrame.getLength());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            output.write(magic);
            output.write(Utils.shortToByteArray(frame_length));
            output.write((byte) route_flags);
            output.write(Utils.intToByteArray(MD5_sum));
            output.write(oFrame.toByte(nodeName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    public int getLength() {
        return 9;
    }
}
