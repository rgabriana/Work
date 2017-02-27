/**
 * 
 */
package com.ems.commands.imgupgrade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;
import com.ems.utils.Utils;

/**
 * @author shrihari
 * 
 */
public class PlugloadImageUpgrade implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(PlugloadImageUpgrade.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 66;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0x87;

    private byte[] reserved = new byte[52];
    private byte chunckno = 0;
    private char frame_end_marker = 0x5e;

    public PlugloadImageUpgrade(byte chunckno) {
        this.chunckno = chunckno;
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

            for (int count = 0; count < 52; count++) {
                reserved[count] = 0;
            }
            output.write(reserved);
            output.write(chunckno);
            output.write((byte) frame_end_marker);
        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 66;
    }

}
