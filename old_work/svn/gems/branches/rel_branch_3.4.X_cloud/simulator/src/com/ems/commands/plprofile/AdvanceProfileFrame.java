/**
 * 
 */
package com.ems.commands.plprofile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class AdvanceProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(AdvanceProfileFrame.class.getName());
    private short[] paher_start = new short[4];
    private short standaloneMotionOverride;
    private byte drReactivity;
    private int toOffLinger;
    private byte initialOnLevel;
    private short profileGroupId;
    private int initialOnTime;        
    private short drLevels = 0;
    private int heartbeatInterval;
    private int heartbeatLingerPeriod;
    private int noOfMissedHeartbeats;
    private int safetyMode;      
    private byte holidayLevel = 0;
    private byte[] spare_1 = new byte[15];

    public AdvanceProfileFrame() {
        short[] paher_start = {360, 540, 1080, 1260};
        setPaher_start(paher_start);
        standaloneMotionOverride = 0;
        drReactivity = 0;
        profileGroupId = 1;
        initialOnTime = 10;
        initialOnLevel  = 0;
        drLevels = 0;
        toOffLinger = 0;
        heartbeatInterval = 30;
        heartbeatLingerPeriod = 30;
        noOfMissedHeartbeats = 3;
        safetyMode = 1;
        holidayLevel = 0;
        for (int i = 0; i < 15; i++) {
            spare_1[i] = 0;
        }
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
            for (int i = 0; i < 4; i++) {
                output.write(Utils.shortToByteArray(paher_start[i]));
            }
            output.write(Utils.shortToByteArray(standaloneMotionOverride));
            output.write(drReactivity);
            output.write(Utils.intToByteArray(toOffLinger));
            output.write(initialOnLevel);
            output.write((byte)profileGroupId);
            output.write(Utils.intToByteArray(initialOnTime));
            output.write(Utils.shortToByteArray(drLevels));
            output.write(Utils.intToByteArray(heartbeatInterval));
            output.write(Utils.intToByteArray(heartbeatLingerPeriod));
            output.write(Utils.intToByteArray(noOfMissedHeartbeats));
            output.write(Utils.intToByteArray(safetyMode));
            output.write(holidayLevel);
            output.write(spare_1);
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
        return 57;
    }

    /**
     * @return the paher_start
     */
    public short[] getPaher_start() {
        return paher_start;
    }

    /**
     * @param paher_start
     *            the paher_start to set
     */
    public void setPaher_start(short[] paher_start) {
        this.paher_start = paher_start;
    }

    /**
     * @return the standaloneMotionOverride
     */
    public short getStandaloneMotionOverride() {
        return standaloneMotionOverride;
    }
    /**
     * @param standaloneMotionOverride the standaloneMotionOverride to set
     */
    public void setStandaloneMotionOverride(short standaloneMotionOverride) {
        this.standaloneMotionOverride = standaloneMotionOverride;
    }
    /**
     * @return the drReactivity
     */
    public byte getDrReactivity() {
        return drReactivity;
    }
    /**
     * @param drReactivity the drReactivity to set
     */
    public void setDrReactivity(byte drReactivity) {
        this.drReactivity = drReactivity;
    }
    /**
     * @return the toOffLinger
     */
    public int getToOffLinger() {
        return toOffLinger;
    }
    /**
     * @param toOffLinger the toOffLinger to set
     */
    public void setToOffLinger(int toOffLinger) {
        this.toOffLinger = toOffLinger;
    }
    /**
     * @return the initialOnLevel
     */
    public byte getInitialOnLevel() {
        return initialOnLevel;
    }
    /**
     * @param initialOnLevel the initialOnLevel to set
     */
    public void setInitialOnLevel(byte initialOnLevel) {
        this.initialOnLevel = initialOnLevel;
    }
    /**
     * @return the profileGroupId
     */
    public short getProfileGroupId() {
        return profileGroupId;
    }
    /**
     * @param profileGroupId the profileGroupId to set
     */
    public void setProfileGroupId(short profileGroupId) {
        this.profileGroupId = profileGroupId;
    }

    /**
     * @return the initialOnTime
     */
    public int getInitialOnTime() {
        return initialOnTime;
    }
    /**
     * @param initialOnTime the initialOnTime to set
     */
    public void setInitialOnTime(int initialOnTime) {
        this.initialOnTime = initialOnTime;
    }
    /**
     * @return the drLevels
     */
    public short getDrLevels() {
        return drLevels;
    }
    /**
     * @param drLevels the drLevels to set
     */
    public void setDrLevels(short drLevels) {
        this.drLevels = drLevels;
    }
    /**
     * @return the heartbeatInterval
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    /**
     * @param heartbeatInterval the heartbeatInterval to set
     */
    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    /**
     * @return the heartbeatLingerPeriod
     */
    public int getHeartbeatLingerPeriod() {
        return heartbeatLingerPeriod;
    }
    /**
     * @param heartbeatLingerPeriod the heartbeatLingerPeriod to set
     */
    public void setHeartbeatLingerPeriod(int heartbeatLingerPeriod) {
        this.heartbeatLingerPeriod = heartbeatLingerPeriod;
    }
    /**
     * @return the noOfMissedHeartbeats
     */
    public int getNoOfMissedHeartbeats() {
        return noOfMissedHeartbeats;
    }
    /**
     * @param noOfMissedHeartbeats the noOfMissedHeartbeats to set
     */
    public void setNoOfMissedHeartbeats(int noOfMissedHeartbeats) {
        this.noOfMissedHeartbeats = noOfMissedHeartbeats;
    }
    /**
     * @return the safetyMode
     */
    public int getSafetyMode() {
        return safetyMode;
    }
    /**
     * @param safetyMode the safetyMode to set
     */
    public void setSafetyMode(int safetyMode) {
        this.safetyMode = safetyMode;
    }
    /**
     * @return the spare_1
     */
    public byte[] getSpare_1() {
        return spare_1;
    }

    /**
     * @param spare_1 the spare_1 to set
     */
    public void setSpare_1(byte[] spare_1) {
        this.spare_1 = spare_1;
    }
    /**
     * @return the holidayLevel
     */
    public byte getHolidayLevel() {
        return holidayLevel;
    }
    /**
     * @param holidayLevel the holidayLevel to set
     */
    public void setHolidayLevel(byte holidayLevel) {
        this.holidayLevel = holidayLevel;
    }

}
