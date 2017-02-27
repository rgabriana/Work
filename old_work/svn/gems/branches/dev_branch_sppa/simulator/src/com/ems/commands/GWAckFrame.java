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
public class GWAckFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(GWAckFrame.class.getName());

    private byte ack_cmd = 0x1;
    private int txnid;
    private byte success;
    private byte acked_cmd;
    
    public GWAckFrame(int txId, byte success, byte acked_cmd) {
        this.txnid = txId;
        this.success = success;
        this.acked_cmd = acked_cmd;
    }

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write((byte) ack_cmd);
            output.write(Utils.intToByteArray(txnid));
            output.write((byte) success);
            output.write((byte) acked_cmd);
        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 7;
    }

}
