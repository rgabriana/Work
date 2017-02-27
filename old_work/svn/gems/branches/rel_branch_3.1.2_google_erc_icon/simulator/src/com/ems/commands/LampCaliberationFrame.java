/**
 * 
 */
package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class LampCaliberationFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(LampCaliberationFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 95;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0x41;

    private byte status = 0;
    private byte reserved = 0;
    // 2 bytes per entry (0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10)
    private short[] fxvoltpowermap = new short[20];
    // 2 bytes per entry
    private short[] fxvoltluxmap = new short[20];
    
    private char frame_end_marker = 0x5e;


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

            output.write((byte) status);
            output.write((byte) reserved);
            float powerUsed = 2.0f;
            float volt = 0.5f;
            short luxLevel = 100;
            for (int count = 0; count < 20; count++) {
                fxvoltpowermap[count] = (short) powerUsed;
                fxvoltluxmap[count] = (short) luxLevel;
                output.write(Utils.shortToByteArray(fxvoltpowermap[count]));
                output.write(Utils.shortToByteArray(fxvoltluxmap[count]));
                powerUsed += volt * 10;
                luxLevel += count;
                volt += 0.5;
            }
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
        return frame_length;
    }

}
