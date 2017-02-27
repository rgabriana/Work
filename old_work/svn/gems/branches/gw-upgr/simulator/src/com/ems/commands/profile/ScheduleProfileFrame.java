/**
 * 
 */
package com.ems.commands.profile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ems.commands.ICommandPktFrame;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class ScheduleProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(ScheduleProfileFrame.class.getName());
    private byte min_light_level;
    private byte max_light_level;
    private short motion_detection_duration;
    private short manual_state_time_limit;
    private byte occ_sensitivity;
    private byte ramp_time;
    private byte ambient_light_sensitivity;
    private byte[] spare_1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 9 spare

    public ScheduleProfileFrame(int iType, int paher) {
        if (iType == Profile.WEEKDAY) {
            switch (paher) {
            case Profile.MORNING:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 5 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 5;
                manual_state_time_limit = 60 * 60;
                break;
            case Profile.DAY:
                min_light_level = 20;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 15 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 5;
                manual_state_time_limit = 60 * 60;
                break;
            case Profile.EVENING:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 5  * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 5;
                manual_state_time_limit = 60 * 60;
                break;
            case Profile.NIGHT:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 5  * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 0;
                manual_state_time_limit = 60  * 60;
                break;
            }
        } else if (iType == Profile.WEEKEND) {
            switch (paher) {
            case Profile.NIGHT:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 3 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 0;
                manual_state_time_limit = 60  * 60;
                break;
            default:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 3 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 5;
                manual_state_time_limit = 60 * 60;
                break;
            }
        } else if (iType == Profile.HOLIDAY) {
            switch (paher) {
            case Profile.NIGHT:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 3 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 0;
                manual_state_time_limit = 60 * 60;
                break;
            default:
                min_light_level = 0;
                max_light_level = 100;
                ramp_time = 0;
                motion_detection_duration = 3 * 60;
                occ_sensitivity = 1;
                ambient_light_sensitivity = 5;
                manual_state_time_limit = 60 * 60;
                break;
            }
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
            output.write((byte) min_light_level);
            output.write((byte) max_light_level);
            output.write(Utils.shortToByteArray(motion_detection_duration));
            output.write(Utils.shortToByteArray(manual_state_time_limit));
            output.write((byte) occ_sensitivity);
            output.write((byte) ramp_time);
            output.write((byte) ambient_light_sensitivity);
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
     * @return the min_light_level
     */
    public byte getMin_light_level() {
        return min_light_level;
    }

    /**
     * @param min_light_level
     *            the min_light_level to set
     */
    public void setMin_light_level(byte min_light_level) {
        this.min_light_level = min_light_level;
    }

    /**
     * @return the max_light_level
     */
    public byte getMax_light_level() {
        return max_light_level;
    }

    /**
     * @param max_light_level
     *            the max_light_level to set
     */
    public void setMax_light_level(byte max_light_level) {
        this.max_light_level = max_light_level;
    }

    /**
     * @return the motion_detection_duration
     */
    public short getMotion_detection_duration() {
        return motion_detection_duration;
    }

    /**
     * @param motion_detection_duration
     *            the motion_detection_duration to set
     */
    public void setMotion_detection_duration(short motion_detection_duration) {
        this.motion_detection_duration = motion_detection_duration;
    }

    /**
     * @return the manual_state_time_limit
     */
    public short getManual_state_time_limit() {
        return manual_state_time_limit;
    }

    /**
     * @param manual_state_time_limit
     *            the manual_state_time_limit to set
     */
    public void setManual_state_time_limit(short manual_state_time_limit) {
        this.manual_state_time_limit = manual_state_time_limit;
    }

    /**
     * @return the occ_sensitivity
     */
    public byte getOcc_sensitivity() {
        return occ_sensitivity;
    }

    /**
     * @param occ_sensitivity
     *            the occ_sensitivity to set
     */
    public void setOcc_sensitivity(byte occ_sensitivity) {
        this.occ_sensitivity = occ_sensitivity;
    }

    /**
     * @return the ramp_time
     */
    public byte getRamp_time() {
        return ramp_time;
    }

    /**
     * @param ramp_time
     *            the ramp_time to set
     */
    public void setRamp_time(byte ramp_time) {
        this.ramp_time = ramp_time;
    }

    /**
     * @return the ambient_light_sensitivity
     */
    public byte getAmbient_light_sensitivity() {
        return ambient_light_sensitivity;
    }

    /**
     * @param ambient_light_sensitivity
     *            the ambient_light_sensitivity to set
     */
    public void setAmbient_light_sensitivity(byte ambient_light_sensitivity) {
        this.ambient_light_sensitivity = ambient_light_sensitivity;
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
