/**
 * 
 */
package com.ems.commands.plprofile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;
import com.ems.db.SimPlDBHelper;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class ScheduleProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(ScheduleProfileFrame.class.getName());
    byte mode;
    int activeMotion;
    int manualOverrideTime;
    private byte[] spare_1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 9 spare

    public ScheduleProfileFrame(int iType, int paher) {

        if (iType == Profile.WEEKDAY) {
            switch (paher) {
            case Profile.MORNING:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            case Profile.DAY:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            case Profile.EVENING:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            case Profile.NIGHT:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            }
        } else if (iType == Profile.WEEKEND) {
            switch (paher) {
            case Profile.NIGHT:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            default:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            }
        } else if (iType == Profile.HOLIDAY) {
            switch (paher) {
            case Profile.NIGHT:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            default:
                mode = 2;
                activeMotion = 30;
                manualOverrideTime = 60;
                break;
            }
        }
    }

    public ScheduleProfileFrame(byte[] profileArray, int iType, int paher) {

        switch (paher) {
        case Profile.MORNING:
            mode = profileArray[0];
            activeMotion = Utils.extractIntFromByteArray(profileArray, 1);
            manualOverrideTime = Utils.extractIntFromByteArray(profileArray, 5);
            break;
        case Profile.DAY:
            mode = profileArray[18];
            activeMotion = Utils.extractIntFromByteArray(profileArray, 19);
            manualOverrideTime = Utils.extractIntFromByteArray(profileArray, 23);
            break;
        case Profile.EVENING:
            mode = profileArray[36];
            activeMotion = Utils.extractIntFromByteArray(profileArray, 37);
            manualOverrideTime = Utils.extractIntFromByteArray(profileArray, 41);
            break;
        case Profile.NIGHT:
            mode = profileArray[54];
            activeMotion = Utils.extractIntFromByteArray(profileArray, 55);
            manualOverrideTime = Utils.extractIntFromByteArray(profileArray, 59);
            break;
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
            output.write((byte) mode);
            output.write(Utils.intToByteArray(activeMotion));
            output.write(Utils.intToByteArray(manualOverrideTime));
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
        return 18;
    }

    /**
     * @return the mode
     */
    public byte getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(byte mode) {
        this.mode = mode;
    }

    /**
     * @return the activeMotion
     */
    public int getActiveMotion() {
        return activeMotion;
    }

    /**
     * @param activeMotion
     *            the activeMotion to set
     */
    public void setActiveMotion(int activeMotion) {
        this.activeMotion = activeMotion;
    }

    /**
     * @return the manualOverrideTime
     */
    public int getManualOverrideTime() {
        return manualOverrideTime;
    }

    /**
     * @param manualOverrideTime
     *            the manualOverrideTime to set
     */
    public void setManualOverrideTime(int manualOverrideTime) {
        this.manualOverrideTime = manualOverrideTime;
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
