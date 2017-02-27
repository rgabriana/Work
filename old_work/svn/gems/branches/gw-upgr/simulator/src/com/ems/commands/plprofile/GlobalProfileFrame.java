package com.ems.commands.plprofile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;

import com.ems.utils.Utils;

public class GlobalProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(ScheduleProfileFrame.class.getName());
    byte motionGrpHbMisses, weekDayBits, profileGroupId, flags, safetyMode, holidayLevel;
    
    private byte[] spare_1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 9 spare
    short morningStart, dayStart, eveningStart, nightStart, motionGrpHeartBeatFreq, initialOnTime, drMap;

    public GlobalProfileFrame() {

    }

    public byte[] getByteArray() {

        byte[] packet = new byte[53];
        int i = 0;
        // morning start
        byte[] tempShortArr = Utils.shortToByteArray(morningStart);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // day start
        tempShortArr = Utils.shortToByteArray(dayStart);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // evening start
        tempShortArr = Utils.shortToByteArray(eveningStart);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // night start
        tempShortArr = Utils.shortToByteArray(nightStart);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;

        // unused dark lux
        tempShortArr = Utils.shortToByteArray(0);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // unused neighbor lux
        tempShortArr = Utils.shortToByteArray(0);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // unused envelope on level
        tempShortArr = Utils.shortToByteArray(0);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // unused drop percentage
        packet[i++] = 0;
        // unsed rise percentage
        packet[i++] = 0;
        // unused relays connected
        packet[i++] = 0;
        // unused dim backoff time
        packet[i++] = 0;
        // unused intensity norm time
        tempShortArr = Utils.shortToByteArray(0);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // pl_motion_hb_max_attempts (min level before off in sensor)
        packet[i++] = motionGrpHbMisses;
        // unused stand alone motion override
        packet[i++] = 0;
        // unused dr reactivity
        packet[i++] = 0;
        // pl_motion_hb_interval (to off linger in sensor)
        tempShortArr = Utils.shortToByteArray(motionGrpHeartBeatFreq);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // week days
        packet[i++] = weekDayBits;
        // unused initial on level
        packet[i++] = 0;
        // group id
        packet[i++] = profileGroupId;
        // flags
        packet[i++] = flags;
        // initial on time
        tempShortArr = Utils.shortToByteArray(initialOnTime);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // bay type in the sensor profile here it is safety behavior
        packet[i++] = safetyMode;
        // unused motion threshold gain
        tempShortArr = Utils.shortToByteArray(0);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // dr levels map
        tempShortArr = Utils.shortToByteArray(drMap);
        System.arraycopy(tempShortArr, 0, packet, i, tempShortArr.length);
        i += 2;
        // unused daylight harvesting flags
        packet[i++] = 0;
        // holiday level
        packet[i++] = holidayLevel;
        // pad with 15 bytes
        for (int j = 0; j < 15; j++) {
            packet[i++] = 0;
        }

        return packet;

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
            output.write(Utils.shortToByteArray(morningStart));
            output.write(Utils.shortToByteArray(dayStart));
            output.write(Utils.shortToByteArray(eveningStart));
            output.write(Utils.shortToByteArray(nightStart));           
            
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

    /**
     * @return the spare_1
     */
    public byte[] getSpare_1() {
        return spare_1;
    }

    /**
     * @param spare_1
     *            the spare_1 to set
     */
    public void setSpare_1(byte[] spare_1) {
        this.spare_1 = spare_1;
    }

}