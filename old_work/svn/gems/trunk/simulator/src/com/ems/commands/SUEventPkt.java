/**
 * 
 */
package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.utils.Utils;

/**
 * @author enlighted
 *
 */
public class SUEventPkt implements ICommandPktFrame {
    
    private Logger oLogger = Logger.getLogger(SUEventPkt.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 16;

    private int txnid = Utils.getNextSeqNo();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xb5;
    
    private int event_no = 12;
    
    private byte[] msg_body = null;

    public SUEventPkt(int event_no, byte[] msgBody) {
        this.event_no = event_no;
        this.msg_body=msgBody;
    }
    
    /* (non-Javadoc)
     * @see com.ems.commands.ICommandPktFrame#toByte(java.lang.String)
     */
    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write((byte)frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray((int)getLength()));
            output.write(Utils.intToByteArray(txnid));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            output.write(Utils.intToByteArray(event_no));
            if (msg_body != null)
                output.write(msg_body);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    /* (non-Javadoc)
     * @see com.ems.commands.ICommandPktFrame#getLength()
     */
    @Override
    public long getLength() {
        return 16 + (msg_body != null ? msg_body.length : 0);
    }

}
