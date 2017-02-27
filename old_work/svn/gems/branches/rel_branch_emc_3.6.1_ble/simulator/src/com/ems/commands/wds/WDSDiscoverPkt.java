/**
 * 
 */
package com.ems.commands.wds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class WDSDiscoverPkt implements ICommandPktFrame {

    private Logger oLogger = Logger.getLogger(WDSDiscoverPkt.class.getName());
    private static final int SWITCH_MODEL_SZ = 12;
    private static final int SWITCH_ASSEMBLY_SZ = 14;

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 39;
    private int txnid = Utils.getNextSeqNo();
    private byte[] node_name = { 0, 0, 0 };
    private char msg_type = 0x61;
    private int switchid = 0;
    private short firmware_major_version = 2;
    private short firmware_minor_version = 2;
    private short firmware_bugfix_version = 0;
    private short svn_tag = 342;
    //KCUB05124100 
    private char[] model = "KCUB05124100".toCharArray();
    short battey_level = 100;
    short req_lqi = 0;
    private char frame_end_marker = 0x5e;

    public WDSDiscoverPkt() {
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
            output.write(Utils.intToByteArray(switchid));
            output.write(Utils.shortToByteArray(firmware_major_version));
            output.write(Utils.shortToByteArray(firmware_minor_version));
            output.write(Utils.shortToByteArray(firmware_bugfix_version));
            output.write(Utils.shortToByteArray(svn_tag));
            byte[] bModelArr = new String(model).getBytes();
            output.write(bModelArr);
            output.write(Utils.shortToByteArray(battey_level));
            output.write(Utils.shortToByteArray(req_lqi));
            output.write((byte) frame_end_marker);
        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 50;
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
     * @return the firmware_major_version
     */
    public short getFirmware_major_version() {
        return firmware_major_version;
    }

    /**
     * @param firmware_major_version
     *            the firmware_major_version to set
     */
    public void setFirmware_major_version(short firmware_major_version) {
        this.firmware_major_version = firmware_major_version;
    }

    /**
     * @return the firmware_minor_version
     */
    public short getFirmware_minor_version() {
        return firmware_minor_version;
    }

    /**
     * @param firmware_minor_version
     *            the firmware_minor_version to set
     */
    public void setFirmware_minor_version(short firmware_minor_version) {
        this.firmware_minor_version = firmware_minor_version;
    }

    /**
     * @return the firmware_bugfix_version
     */
    public short getFirmware_bugfix_version() {
        return firmware_bugfix_version;
    }

    /**
     * @param firmware_bugfix_version
     *            the firmware_bugfix_version to set
     */
    public void setFirmware_bugfix_version(short firmware_bugfix_version) {
        this.firmware_bugfix_version = firmware_bugfix_version;
    }

    /**
     * @return the svn_tag
     */
    public short getSvn_tag() {
        return svn_tag;
    }

    /**
     * @param svn_tag
     *            the svn_tag to set
     */
    public void setSvn_tag(short svn_tag) {
        this.svn_tag = svn_tag;
    }

    /**
     * @return the model
     */
    public char[] getModel() {
        return model;
    }

    /**
     * @param model
     *            the model to set
     */
    public void setModel(char[] model) {
        this.model = model;
    }
}
