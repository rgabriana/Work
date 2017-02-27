/**
 * 
 */
package com.ems.commands.profile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.commands.ICommandPktFrame;
import com.ems.commands.SUAckFrame;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class DownloadProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(SUAckFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 18;
    private int txnid = Utils.getNextSeqNo();
    private byte[] node_name = { 0, 0, 0 };
    private char msg_type = CommandsConstants.PROFILE_DOWNLOAD_MSG_TYPE;
    private char ack_cmd;
    private byte[] data;
    private char frame_end_marker = 0x5e;

    public DownloadProfileFrame(int txnId, char ack_cmd, byte[] data) {
        this.txnid = txnId;
        this.ack_cmd = ack_cmd;
        this.data = data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.commands.ICommandPktFrame#toByte(java.lang.String)
     */
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
            output.write((byte) ack_cmd);
            output.write(data);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.commands.ICommandPktFrame#getLength()
     */
    @Override
    public long getLength() {
        return 18 + data.length;
    }

}
