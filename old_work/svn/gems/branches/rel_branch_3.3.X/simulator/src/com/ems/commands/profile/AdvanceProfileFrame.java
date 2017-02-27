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
public class AdvanceProfileFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(AdvanceProfileFrame.class.getName());
    private short[] paher_start = new short[4];
    private short dark_intensity;
    private short neighbor_intensity;
    private short envelop_on_intensity;
    private byte acceptable_drop_percentage;
    private byte acceptable_rise_percentage;
    private byte relays_connected;
    private byte dim_back_off_mins;
    private short intensity_norm_time_limit;
    private byte minimum_level_before_off;
    private byte stand_alone_motion_override;
    private byte DR_reactivity;
    private short to_off_linger;
    private byte weekDayBits;
    private byte InitialOnLevel;
    private byte profile_group_id;
    private byte flags;
    private byte[] initial_on_time = new byte[2];
    private byte theBayType;
    private short motionThresholdGain;
    private byte[] spare_0 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private short on_ambient_light_level;
    private short on_ambient_light_level_low_thresh;
    private short on_ambient_light_level_high_thresh;
    private byte last_state;
    private byte last_mode;
    private byte location;
    private byte calib_done;
    private byte[] spare_1 = { 0, 0, 0, 0, 0, 0, 0 };

    public AdvanceProfileFrame() {
        setDark_intensity((short)20);
        setNeighbor_intensity((short)200);
        setEnvelop_on_intensity((short)50);
        setAcceptable_drop_percentage((byte)10);
        setAcceptable_rise_percentage((byte)20);
        setDim_back_off_mins((byte)10);
        setIntensity_norm_time_limit((short)10);
        setOn_ambient_light_level((short)0); // extended
        setMinimum_level_before_off((byte)20);
        setRelays_connected((byte)1);
        setStand_alone_motion_override((byte)0);
        setDR_reactivity((byte)0);
        setTo_off_linger((short)30);
        setInitialOnLevel((byte)50);
        short[] paher_start = {360, 540, 1080, 1260};
        setPaher_start(paher_start);
        setInitial_on_time(new byte[]{0, 5});
        setWeekDayBits((byte)31);
        setProfile_group_id((byte)1);
        setFlags((byte)0);
        setTheBayType((byte)0);
        setMotionThresholdGain((short)0);
        // extended
        on_ambient_light_level_low_thresh = 0;
        on_ambient_light_level_high_thresh = 0;
        last_state = 0;
        last_mode = 0;
        location = 0;
        calib_done = 2;
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
            output.write(Utils.shortToByteArray(dark_intensity));
            output.write(Utils.shortToByteArray(neighbor_intensity));
            output.write(Utils.shortToByteArray(envelop_on_intensity));
            output.write(acceptable_drop_percentage);
            output.write(acceptable_rise_percentage);
            output.write(relays_connected);
            output.write(dim_back_off_mins);
            output.write(Utils.shortToByteArray(intensity_norm_time_limit));
            output.write(minimum_level_before_off);
            output.write(stand_alone_motion_override);
            output.write(DR_reactivity);
            output.write(Utils.shortToByteArray(to_off_linger));
            output.write(weekDayBits);
            output.write(InitialOnLevel);
            output.write(profile_group_id);
            output.write(flags);
            output.write(initial_on_time);
            output.write(theBayType);
            output.write(Utils.shortToByteArray(motionThresholdGain));
            output.write(spare_0);
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
        return 53;
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
     * @return the dark_intensity
     */
    public short getDark_intensity() {
        return dark_intensity;
    }

    /**
     * @param dark_intensity
     *            the dark_intensity to set
     */
    public void setDark_intensity(short dark_intensity) {
        this.dark_intensity = dark_intensity;
    }

    /**
     * @return the neighbor_intensity
     */
    public short getNeighbor_intensity() {
        return neighbor_intensity;
    }

    /**
     * @param neighbor_intensity
     *            the neighbor_intensity to set
     */
    public void setNeighbor_intensity(short neighbor_intensity) {
        this.neighbor_intensity = neighbor_intensity;
    }

    /**
     * @return the envelop_on_intensity
     */
    public short getEnvelop_on_intensity() {
        return envelop_on_intensity;
    }

    /**
     * @param envelop_on_intensity
     *            the envelop_on_intensity to set
     */
    public void setEnvelop_on_intensity(short envelop_on_intensity) {
        this.envelop_on_intensity = envelop_on_intensity;
    }

    /**
     * @return the acceptable_drop_percentage
     */
    public byte getAcceptable_drop_percentage() {
        return acceptable_drop_percentage;
    }

    /**
     * @param acceptable_drop_percentage
     *            the acceptable_drop_percentage to set
     */
    public void setAcceptable_drop_percentage(byte acceptable_drop_percentage) {
        this.acceptable_drop_percentage = acceptable_drop_percentage;
    }

    /**
     * @return the acceptable_rise_percentage
     */
    public byte getAcceptable_rise_percentage() {
        return acceptable_rise_percentage;
    }

    /**
     * @param acceptable_rise_percentage
     *            the acceptable_rise_percentage to set
     */
    public void setAcceptable_rise_percentage(byte acceptable_rise_percentage) {
        this.acceptable_rise_percentage = acceptable_rise_percentage;
    }

    /**
     * @return the relays_connected
     */
    public byte getRelays_connected() {
        return relays_connected;
    }

    /**
     * @param relays_connected
     *            the relays_connected to set
     */
    public void setRelays_connected(byte relays_connected) {
        this.relays_connected = relays_connected;
    }

    /**
     * @return the dim_back_off_mins
     */
    public byte getDim_back_off_mins() {
        return dim_back_off_mins;
    }

    /**
     * @param dim_back_off_mins
     *            the dim_back_off_mins to set
     */
    public void setDim_back_off_mins(byte dim_back_off_mins) {
        this.dim_back_off_mins = dim_back_off_mins;
    }

    /**
     * @return the intensity_norm_time_limit
     */
    public short getIntensity_norm_time_limit() {
        return intensity_norm_time_limit;
    }

    /**
     * @param intensity_norm_time_limit
     *            the intensity_norm_time_limit to set
     */
    public void setIntensity_norm_time_limit(short intensity_norm_time_limit) {
        this.intensity_norm_time_limit = intensity_norm_time_limit;
    }

    /**
     * @return the minimum_level_before_off
     */
    public byte getMinimum_level_before_off() {
        return minimum_level_before_off;
    }

    /**
     * @param minimum_level_before_off
     *            the minimum_level_before_off to set
     */
    public void setMinimum_level_before_off(byte minimum_level_before_off) {
        this.minimum_level_before_off = minimum_level_before_off;
    }

    /**
     * @return the stand_alone_motion_override
     */
    public byte getStand_alone_motion_override() {
        return stand_alone_motion_override;
    }

    /**
     * @param stand_alone_motion_override
     *            the stand_alone_motion_override to set
     */
    public void setStand_alone_motion_override(byte stand_alone_motion_override) {
        this.stand_alone_motion_override = stand_alone_motion_override;
    }

    /**
     * @return the dR_reactivity
     */
    public byte getDR_reactivity() {
        return DR_reactivity;
    }

    /**
     * @param dR_reactivity
     *            the dR_reactivity to set
     */
    public void setDR_reactivity(byte dR_reactivity) {
        DR_reactivity = dR_reactivity;
    }

    /**
     * @return the to_off_linger
     */
    public short getTo_off_linger() {
        return to_off_linger;
    }

    /**
     * @param to_off_linger
     *            the to_off_linger to set
     */
    public void setTo_off_linger(short to_off_linger) {
        this.to_off_linger = to_off_linger;
    }

    /**
     * @return the weekDayBits
     */
    public byte getWeekDayBits() {
        return weekDayBits;
    }

    /**
     * @param weekDayBits
     *            the weekDayBits to set
     */
    public void setWeekDayBits(byte weekDayBits) {
        this.weekDayBits = weekDayBits;
    }

    /**
     * @return the initialOnLevel
     */
    public byte getInitialOnLevel() {
        return InitialOnLevel;
    }

    /**
     * @param initialOnLevel
     *            the initialOnLevel to set
     */
    public void setInitialOnLevel(byte initialOnLevel) {
        InitialOnLevel = initialOnLevel;
    }

    /**
     * @return the profile_group_id
     */
    public byte getProfile_group_id() {
        return profile_group_id;
    }

    /**
     * @param profile_group_id
     *            the profile_group_id to set
     */
    public void setProfile_group_id(byte profile_group_id) {
        this.profile_group_id = profile_group_id;
    }

    /**
     * @return the flags
     */
    public byte getFlags() {
        return flags;
    }

    /**
     * @param flags
     *            the flags to set
     */
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    /**
     * @return the initial_on_time
     */
    public byte[] getInitial_on_time() {
        return initial_on_time;
    }

    /**
     * @param initial_on_time
     *            the initial_on_time to set
     */
    public void setInitial_on_time(byte[] initial_on_time) {
        this.initial_on_time = initial_on_time;
    }

    /**
     * @return the theBayType
     */
    public byte getTheBayType() {
        return theBayType;
    }

    /**
     * @param theBayType
     *            the theBayType to set
     */
    public void setTheBayType(byte theBayType) {
        this.theBayType = theBayType;
    }

    /**
     * @return the motionThresholdGain
     */
    public short getMotionThresholdGain() {
        return motionThresholdGain;
    }

    /**
     * @param motionThresholdGain
     *            the motionThresholdGain to set
     */
    public void setMotionThresholdGain(short motionThresholdGain) {
        this.motionThresholdGain = motionThresholdGain;
    }

    /**
     * @return the spare_0
     */
    public byte[] getSpare_0() {
        return spare_0;
    }

    /**
     * @param spare_0
     *            the spare_0 to set
     */
    public void setSpare_0(byte[] spare_0) {
        this.spare_0 = spare_0;
    }

    /**
     * @return the on_ambient_light_level
     */
    public short getOn_ambient_light_level() {
        return on_ambient_light_level;
    }

    /**
     * @param on_ambient_light_level the on_ambient_light_level to set
     */
    public void setOn_ambient_light_level(short on_ambient_light_level) {
        this.on_ambient_light_level = on_ambient_light_level;
    }

    /**
     * @return the on_ambient_light_level_low_thresh
     */
    public short getOn_ambient_light_level_low_thresh() {
        return on_ambient_light_level_low_thresh;
    }

    /**
     * @param on_ambient_light_level_low_thresh the on_ambient_light_level_low_thresh to set
     */
    public void setOn_ambient_light_level_low_thresh(short on_ambient_light_level_low_thresh) {
        this.on_ambient_light_level_low_thresh = on_ambient_light_level_low_thresh;
    }

    /**
     * @return the on_ambient_light_level_high_thresh
     */
    public short getOn_ambient_light_level_high_thresh() {
        return on_ambient_light_level_high_thresh;
    }

    /**
     * @param on_ambient_light_level_high_thresh the on_ambient_light_level_high_thresh to set
     */
    public void setOn_ambient_light_level_high_thresh(short on_ambient_light_level_high_thresh) {
        this.on_ambient_light_level_high_thresh = on_ambient_light_level_high_thresh;
    }

    /**
     * @return the last_state
     */
    public byte getLast_state() {
        return last_state;
    }

    /**
     * @param last_state the last_state to set
     */
    public void setLast_state(byte last_state) {
        this.last_state = last_state;
    }

    /**
     * @return the last_mode
     */
    public byte getLast_mode() {
        return last_mode;
    }

    /**
     * @param last_mode the last_mode to set
     */
    public void setLast_mode(byte last_mode) {
        this.last_mode = last_mode;
    }

    /**
     * @return the location
     */
    public byte getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(byte location) {
        this.location = location;
    }

    /**
     * @return the calib_done
     */
    public byte getCalib_done() {
        return calib_done;
    }

    /**
     * @param calib_done the calib_done to set
     */
    public void setCalib_done(byte calib_done) {
        this.calib_done = calib_done;
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

}
