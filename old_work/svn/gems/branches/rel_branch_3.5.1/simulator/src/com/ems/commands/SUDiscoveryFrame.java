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
public class SUDiscoveryFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(SUDiscoveryFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 39;
    private int txnid = Utils.getNextSeqNo();
    private byte[] node_name = { 0, 0, 0 };
    private byte msg_type = (byte) CommandsConstants.SU_DISCOVERY_TYPE;
    private short netID = 6854;
    private byte channel = 4;
    private byte frm_ver_mj = 1;
    private byte frm_ver_mn = 0;
    private byte[] frm_ver_build = { 0, 1 };
    private byte[] firmware_version = { 0, 4 };
    private byte[] applictaion_version = { 1, 4 };
    private byte[] bootloader_version = { 2, 1 };
    private int app_bugfix_version = 2;
    private int firmware_bugix_version = 1111;
    private short cu_version = 32;
    private byte[] bug_fix_number = { 2, 11 };
    private byte node_param = (byte) 2;
    private char frame_end_marker = 0x5e;

    public SUDiscoveryFrame() {
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
            output.write(Utils.shortToByteArray(netID));
            output.write((byte)channel);
            output.write((byte)frm_ver_mj);
            output.write(frm_ver_mn);
            output.write(frm_ver_build);
            output.write(firmware_version);
            if (Utils.getiGWType() == 1) {
              applictaion_version[0] = 2;  
            }
            output.write(applictaion_version);
            output.write(bootloader_version);
            output.write(Utils.intToByteArray(app_bugfix_version));
            output.write(Utils.intToByteArray(firmware_bugix_version));
            output.write(Utils.shortToByteArray(cu_version));
            output.write(bug_fix_number);
            output.write(node_param);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 39;
    }

}
