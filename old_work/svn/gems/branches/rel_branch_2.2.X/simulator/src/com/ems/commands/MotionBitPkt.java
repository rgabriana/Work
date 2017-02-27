/**
 * 
 */
package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class MotionBitPkt implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(MotionBitPkt.class.getName());
    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 18;
    private int txnid = Utils.getNextSeqNo();
    private byte[] node_name = { 0, 0, 0 };
    private char msg_type = 0xda;
    private byte motion_bit_config;
    private byte bitmask;
    private byte[] bitmask_arr; // stream of data
    private char frame_end_marker = 0x5e;

    public MotionBitPkt() {
        motion_bit_config = (byte) ((1 << 4) + 1);
        bitmask = 1;
        bitmask_arr = new byte[bitmask * 4];
        for (int count = 0; count < bitmask_arr.length; count++) {
            bitmask_arr[count] = 0;
        }
        bitmask_arr[0] |= (1 << 26);
        bitmask_arr[0] |= 1;
    }

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            output.write((byte) frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray(frame_length));
            output.write(Utils.intToByteArray(txnid));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            output.write(motion_bit_config);
            output.write(bitmask);
            output.write(bitmask_arr);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 15 + (bitmask * 4);
    }

    /**
     * @return the frame_start_marker
     */
    public char getFrame_start_marker() {
        return frame_start_marker;
    }

    /**
     * @param frame_start_marker
     *            the frame_start_marker to set
     */
    public void setFrame_start_marker(char frame_start_marker) {
        this.frame_start_marker = frame_start_marker;
    }

    /**
     * @return the protocol_ver
     */
    public char getProtocol_ver() {
        return protocol_ver;
    }

    /**
     * @param protocol_ver
     *            the protocol_ver to set
     */
    public void setProtocol_ver(char protocol_ver) {
        this.protocol_ver = protocol_ver;
    }

    /**
     * @return the frame_length
     */
    public short getFrame_length() {
        return frame_length;
    }

    /**
     * @param frame_length
     *            the frame_length to set
     */
    public void setFrame_length(short frame_length) {
        this.frame_length = frame_length;
    }

    /**
     * @return the txnid
     */
    public int getTxnid() {
        return txnid;
    }

    /**
     * @param txnid
     *            the txnid to set
     */
    public void setTxnid(int txnid) {
        this.txnid = txnid;
    }

    /**
     * @return the node_name
     */
    public byte[] getNode_name() {
        return node_name;
    }

    /**
     * @param node_name
     *            the node_name to set
     */
    public void setNode_name(byte[] node_name) {
        this.node_name = node_name;
    }

    /**
     * @return the msg_type
     */
    public char getMsg_type() {
        return msg_type;
    }

    /**
     * @param msg_type
     *            the msg_type to set
     */
    public void setMsg_type(char msg_type) {
        this.msg_type = msg_type;
    }

    /**
     * @return the motion_bit_config
     */
    public byte getMotion_bit_config() {
        return motion_bit_config;
    }

    /**
     * @param motion_bit_config
     *            the motion_bit_config to set
     */
    public void setMotion_bit_config(byte motion_bit_config) {
        this.motion_bit_config = motion_bit_config;
    }

    /**
     * @return the bitmask
     */
    public byte getBitmask() {
        return bitmask;
    }

    /**
     * @param bitmask
     *            the bitmask to set
     */
    public void setBitmask(byte bitmask) {
        this.bitmask = bitmask;
    }

    /**
     * @return the bitmask_arr
     */
    public byte[] getBitmask_arr() {
        return bitmask_arr;
    }

    /**
     * @param bitmask_arr
     *            the bitmask_arr to set
     */
    public void setBitmask_arr(byte[] bitmask_arr) {
        this.bitmask_arr = bitmask_arr;
    }

    /**
     * @return the frame_end_marker
     */
    public char getFrame_end_marker() {
        return frame_end_marker;
    }

    /**
     * @param frame_end_marker
     *            the frame_end_marker to set
     */
    public void setFrame_end_marker(char frame_end_marker) {
        this.frame_end_marker = frame_end_marker;
    }

}
