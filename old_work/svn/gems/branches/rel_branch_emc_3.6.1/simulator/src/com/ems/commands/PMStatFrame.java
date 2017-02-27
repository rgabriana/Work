package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.profile.Profile;
import com.ems.utils.Utils;

/**
 * Created by IntelliJ IDEA. User: yogesh Date: 5/1/12 Time: 10:01 PM To change this template use File | Settings | File
 * Templates.
 */
public class PMStatFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(PMStatFrame.class.getName());

    /**
     * Warning :- If you are adding parameters to this class be sure you are making necessary changes to
     * CommandsUtils.getTotalLengthOfClassVariables() function. This function computes length for the classes. You might
     * want to add the types to this function for the variables you are adding.
     */
    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 86;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xd9;

    char min_voltage_step = '0';
    char max_voltage_step = '0';
    short avg_voltage_step = '0';
    char last_voltage_step = '0';

    short min_ambient_light = 0;
    short max_ambient_light = 0;
    int avg_ambient_light = 0;

    char min_temp = '2';
    char max_temp = '4';
    short avg_temp = 2;
    char last_temp = '2';

    short energyCalibValue = (short) 36282;
    // short energyCalibValue = (short) new Random().nextInt(36283) ;
    short energyTicks = 241;
    int energy_consumption = 0;

    short light_on_sec = 0;
    short light_on_to_off = 0;
    short light_off_to_on = 0;

    short motion_off_to_on = 0;
    short motion_on_to_off = 0;

    int motion_mask_word_1 = 0;
    int motion_mask_word_2 = 0;

    byte savings = (byte)1;

    byte current_state = 2;
    int sysUpTimeSecs = '0';
    short lastResetReason = 0;
    short cuStats = 0;
    short numResetsByCU = 0;
    byte currApp = 2;
    byte global_profile_checksum = 0;
    byte profile_checksum = 0;
    byte group_id = (byte) 1;
    short config_chksum = 0;
    int utc_time = (int) (System.currentTimeMillis()/1000);
    short duration=300;
    byte tenths_precision_temp = -4;
    short ambient_calib = 0xbb8;
    short config_chksum_bmap = 0;

    private char frame_end_marker = 0x5e;
    
    private int pmSeqNo = -1;
    
    private Profile oProfile = null;

    private PMStatFrame() {
        
    }
    
    public PMStatFrame(int pmSeq, Profile profile) {
        Random randomGenerator = new Random();
        oProfile = profile;
      //  System.out.println("max light level is "+oProfile.getMaxLightLevel());
        int max_volt = oProfile.getMaxLightLevel();
        if (max_volt > 0) {
            min_voltage_step = (char)randomGenerator.nextInt(max_volt);
        } else { 
            min_voltage_step = 0;
            max_volt = 0;
        }
        max_voltage_step = (char)max_volt;
        avg_voltage_step = (short)max_volt;
        last_voltage_step = max_voltage_step;
        this.pmSeqNo = pmSeq;
    }

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        global_profile_checksum = oProfile.getDefaultAdvanceProfileChecksum();
        profile_checksum = oProfile.getDefaultScheduleProfileChecksum();
        group_id = oProfile.getProfileNo();
        
        try {
            output.write((byte) frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray(frame_length));
            output.write(Utils.intToByteArray(pmSeqNo));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            output.write((byte) min_voltage_step);
            output.write((byte) max_voltage_step);
            output.write(Utils.shortToByteArray(avg_voltage_step));
            output.write((byte) last_voltage_step);
            output.write(Utils.shortToByteArray(min_ambient_light));
            output.write(Utils.shortToByteArray(max_ambient_light));
            output.write(Utils.intToByteArray(avg_ambient_light));
            output.write((byte) min_temp);
            output.write((byte) max_temp);
            output.write(Utils.shortToByteArray(avg_temp));
            output.write((byte) last_temp);
            output.write(Utils.shortToByteArray(energyCalibValue));
            output.write(Utils.shortToByteArray(energyTicks));
            output.write(Utils.intToByteArray(energy_consumption));
            output.write(Utils.shortToByteArray(light_on_sec));
            output.write(Utils.shortToByteArray(light_on_to_off));
            output.write(Utils.shortToByteArray(light_off_to_on));
            output.write(Utils.shortToByteArray(motion_off_to_on));
            output.write(Utils.shortToByteArray(motion_on_to_off));
            output.write(Utils.intToByteArray(motion_mask_word_1));
            output.write(Utils.intToByteArray(motion_mask_word_2));
            output.write((byte) savings);
            output.write(current_state);
            output.write(Utils.intToByteArray(sysUpTimeSecs));
            output.write(Utils.shortToByteArray(lastResetReason));
            output.write(Utils.shortToByteArray(cuStats));
            output.write(Utils.shortToByteArray(numResetsByCU));
            output.write((byte) currApp);
            output.write((byte) global_profile_checksum);
            output.write((byte) profile_checksum);
            output.write((byte) group_id);
            output.write(Utils.shortToByteArray(this.config_chksum));
            output.write(Utils.intToByteArray(this.utc_time));
            output.write(Utils.shortToByteArray(this.duration));
            output.write((byte) this.tenths_precision_temp);
            output.write(Utils.shortToByteArray(this.ambient_calib));
            output.write(Utils.shortToByteArray(this.config_chksum_bmap));
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return this.frame_length;
    }

    /*
     * Getter Setter Methods for changing the PMStat data dynamically in postToSU() function .
     */

    public int getTxnid() {
        return txnid;
    }

    public void setTxnid(int txnid) {
        this.txnid = txnid;
    }

    public short getFrame_length() {
        return frame_length;
    }

    public void setFrame_length(short frame_length) {
        this.frame_length = frame_length;
    }

    public byte[] getNode_name() {
        return node_name;
    }

    public void setNode_name(byte[] node_name) {
        this.node_name = node_name;
    }

    public char getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(char msg_type) {
        this.msg_type = msg_type;
    }

    public char getMin_voltage_step() {
        return min_voltage_step;
    }

    public void setMin_voltage_step(char min_voltage_step) {
        this.min_voltage_step = min_voltage_step;
    }

    public char getMax_voltage_step() {
        return max_voltage_step;
    }

    public void setMax_voltage_step(char max_voltage_step) {
        this.max_voltage_step = max_voltage_step;
    }

    public short getAvg_voltage_step() {
        return avg_voltage_step;
    }

    public void setAvg_voltage_step(short avg_voltage_step) {
        this.avg_voltage_step = avg_voltage_step;
    }

    public char getLast_voltage_step() {
        return last_voltage_step;
    }

    public void setLast_voltage_step(char last_voltage_step) {
        this.last_voltage_step = last_voltage_step;
    }

    public short getMin_ambient_light() {
        return min_ambient_light;
    }

    public void setMin_ambient_light(short min_ambient_light) {
        this.min_ambient_light = min_ambient_light;
    }

    public short getMax_ambient_light() {
        return max_ambient_light;
    }

    public void setMax_ambient_light(short max_ambient_light) {
        this.max_ambient_light = max_ambient_light;
    }

    public int getAvg_ambient_light() {
        return avg_ambient_light;
    }

    public void setAvg_ambient_light(int avg_ambient_light) {
        this.avg_ambient_light = avg_ambient_light;
    }

    public char getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(char min_temp) {
        this.min_temp = min_temp;
    }

    public char getMax_temp() {
        return max_temp;
    }

    public void setMax_temp(char max_temp) {
        this.max_temp = max_temp;
    }

    public short getAvg_temp() {
        return avg_temp;
    }

    public void setAvg_temp(short avg_temp) {
        this.avg_temp = avg_temp;
    }

    public char getLast_temp() {
        return last_temp;
    }

    public void setLast_temp(char last_temp) {
        this.last_temp = last_temp;
    }

    public short getEnergyCalibValue() {
        return energyCalibValue;
    }

    public void setEnergyCalibValue(short energyCalibValue) {
        this.energyCalibValue = energyCalibValue;
    }

    public short getEnergyTicks() {
        return energyTicks;
    }

    public void setEnergyTicks(short energyTicks) {
        this.energyTicks = energyTicks;
    }

    public int getEnergy_consumption() {
        return energy_consumption;
    }

    public void setEnergy_consumption(int energy_consumption) {
        this.energy_consumption = energy_consumption;
    }

    public short getLight_on_sec() {
        return light_on_sec;
    }

    public void setLight_on_sec(short light_on_sec) {
        this.light_on_sec = light_on_sec;
    }

    public short getLight_on_to_off() {
        return light_on_to_off;
    }

    public void setLight_on_to_off(short light_on_to_off) {
        this.light_on_to_off = light_on_to_off;
    }

    public short getLight_off_to_on() {
        return light_off_to_on;
    }

    public void setLight_off_to_on(short light_off_to_on) {
        this.light_off_to_on = light_off_to_on;
    }

    public short getMotion_on_to_off() {
        return motion_on_to_off;
    }

    public void setMotion_on_to_off(short motion_on_to_off) {
        this.motion_on_to_off = motion_on_to_off;
    }

    public int getMotion_mask_word_1() {
        return motion_mask_word_1;
    }

    public void setMotion_mask_word_1(int motion_mask_word_1) {
        this.motion_mask_word_1 = motion_mask_word_1;
    }

    public int getMotion_mask_word_2() {
        return motion_mask_word_2;
    }

    public void setMotion_mask_word_2(int motion_mask_word_2) {
        this.motion_mask_word_2 = motion_mask_word_2;
    }

    public byte getSavings() {
        return savings;
    }

    public void setSavings(byte savings) {
        this.savings = savings;
    }

    public byte getCurrent_state() {
        return current_state;
    }

    public void setCurrent_state(byte current_state) {
        this.current_state = current_state;
    }

    public int getSysUpTimeSecs() {
        return sysUpTimeSecs;
    }

    public void setSysUpTimeSecs(int sysUpTimeSecs) {
        this.sysUpTimeSecs = sysUpTimeSecs;
    }

    public short getLastResetReason() {
        return lastResetReason;
    }

    public void setLastResetReason(short lastResetReason) {
        this.lastResetReason = lastResetReason;
    }

    public short getCuStats() {
        return cuStats;
    }

    public void setCuStats(short cuStats) {
        this.cuStats = cuStats;
    }

    public short getNumResetsByCU() {
        return numResetsByCU;
    }

    public void setNumResetsByCU(short numResetsByCU) {
        this.numResetsByCU = numResetsByCU;
    }

    public byte getCurrApp() {
        return currApp;
    }

    public void setCurrApp(byte currApp) {
        this.currApp = currApp;
    }

    public byte getGlobal_profile_checksum() {
        return global_profile_checksum;
    }

    public void setGlobal_profile_checksum(byte global_profile_checksum) {
        this.global_profile_checksum = global_profile_checksum;
    }

    public byte getProfile_checksum() {
        return profile_checksum;
    }

    public void setProfile_checksum(byte profile_checksum) {
        this.profile_checksum = profile_checksum;
    }

    public byte getGroup_id() {
        return group_id;
    }

    public void setGroup_id(byte group_id) {
        this.group_id = group_id;
    }
    
    public void setUTC(int utc) {
        this.utc_time = utc;
    }

}
