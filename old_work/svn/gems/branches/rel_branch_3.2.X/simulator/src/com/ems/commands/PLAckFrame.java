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
public class PLAckFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(PLAckFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 18;
    private short frame_length = 18;
    private int txnid = Utils.getNextSeqNo();
    private byte[] node_name = { 0, 0, 0 };
    private char ack_cmd = 0xbb;
    private char msg_type;
    private int ack_txnid;
    private char frame_end_marker = 0x5e;

    public PLAckFrame(long ackTxId, char ackMsg) {
        msg_type = ackMsg;
        ack_txnid = (int)ackTxId;
    }

    public PLAckFrame(long ackTxId, char ackMsg, char msgType) {
        ack_cmd = ackMsg;
        msg_type = msgType;
        ack_txnid = (int)ackTxId;
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
            output.write((byte) ack_cmd);
            output.write((byte) msg_type);
            output.write(Utils.intToByteArray(ack_txnid));
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
        return 18;
    }

}
