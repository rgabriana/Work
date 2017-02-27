package com.ems.commands;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.profile.Profile;
import com.ems.utils.Utils;

public class PMHeartbeatStatFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(PMHeartbeatStatFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 45;

    private int pmSeqNo = -1;

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xa9;
    
    byte hb_trigger_type = 0;
    short hb_trigger_value = 60;
    short motionSecsAgo = 78;
    short curr_load = 32;
    
    char last_voltage_step = '0';

    short last_ambient_light = 0;

    short last_temp = 230;

    int motion_mask_word_1 = 0;
    int motion_mask_word_2 = 0;

    char currApp = '2';
    int utc_time_secs = 0;
    short interval_dura = 0;
    short analog_value_1 = 0;
    short analog_value_2 = 0;
    byte reserved = 0;

    private char frame_end_marker = 0x5e;
    
    private Profile oProfile = null;
    
    public PMHeartbeatStatFrame(int pmSeq, Profile profile, String sName) {
        oProfile = profile;
        int max_volt = oProfile.getMaxLightLevel();
        if (max_volt > 0) {
            max_volt = 0;
        }
        last_voltage_step = (char)max_volt;
        frame_length = (short) getLength();
        this.pmSeqNo = pmSeq;
        
        if(sName != null && !"".equals(sName)) {
            sName = sName.replaceAll(":", "");
            if(Utils.isCustomMotion()) {
                Runtime rt = Runtime.getRuntime();
                Process proc;
                try {
                    //Need file with name sName without : under directory /home/enlighted/sensormotions 
                    //to get user defined motion secs ago value. else it uses the default value.
                    proc = rt.exec(new String[]{"./getLastMotionForSensor.sh",sName});
                    BufferedReader outputStream = new BufferedReader(
                            new InputStreamReader(proc.getInputStream()));
                    String output = outputStream.readLine();
                    if (output != null && !"".equals(output)) {
                        motionSecsAgo = Short.parseShort(output);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        }
    }

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Random r = new Random();
        try {
            output.write((byte) frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray(frame_length));
            output.write(Utils.intToByteArray(pmSeqNo));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            //if there is no occupancy for more than 90 seconds, trigger type will be 2 (i.e. unoccupied)
            if(motionSecsAgo < 90)
                hb_trigger_type = 1;
            else
                hb_trigger_type = 2;
            output.write(hb_trigger_type);
            output.write(Utils.shortToByteArray(hb_trigger_value));
            output.write(Utils.shortToByteArray(motionSecsAgo));
            if(Utils.isRandomPowerLight()) {
                output.write(Utils.shortToByteArray((short)r.nextInt(200)));
                output.write((byte) r.nextInt(100));    
            }
            else {
                output.write(Utils.shortToByteArray(curr_load));
                output.write((byte) last_voltage_step);
            }
            output.write(Utils.shortToByteArray(last_ambient_light));
            if(Utils.isRandomTemp()) {
                output.write(Utils.shortToByteArray((short)((20 + r.nextInt(10))*10)));
            }
            else {
                output.write(Utils.shortToByteArray(last_temp));
            }
            output.write(Utils.intToByteArray(motion_mask_word_1));
            output.write(Utils.intToByteArray(motion_mask_word_2));
            output.write((byte) currApp);
            output.write(Utils.intToByteArray(utc_time_secs));
            output.write(Utils.shortToByteArray(interval_dura));
            output.write(Utils.shortToByteArray(analog_value_1));
            output.write(Utils.shortToByteArray(analog_value_2));
            output.write((byte)reserved);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }
    
    @Override
    public long getLength() {
        return 45;
    }

    /**
     * @return the frame_start_marker
     */
    public char getFrame_start_marker() {
        return frame_start_marker;
    }

    /**
     * @param frame_start_marker the frame_start_marker to set
     */
    public void setFrame_start_marker(char frame_start_marker) {
        this.frame_start_marker = frame_start_marker;
    }

    /**
     * @return the protocol_ver
     */
    public char getProtocol_ver() {
        return protocol_ver;
    }

    /**
     * @param protocol_ver the protocol_ver to set
     */
    public void setProtocol_ver(char protocol_ver) {
        this.protocol_ver = protocol_ver;
    }

    /**
     * @return the frame_length
     */
    public short getFrame_length() {
        return frame_length;
    }

    /**
     * @param frame_length the frame_length to set
     */
    public void setFrame_length(short frame_length) {
        this.frame_length = frame_length;
    }

    /**
     * @return the node_name
     */
    public byte[] getNode_name() {
        return node_name;
    }

    /**
     * @param node_name the node_name to set
     */
    public void setNode_name(byte[] node_name) {
        this.node_name = node_name;
    }

    /**
     * @return the msg_type
     */
    public char getMsg_type() {
        return msg_type;
    }

    /**
     * @param msg_type the msg_type to set
     */
    public void setMsg_type(char msg_type) {
        this.msg_type = msg_type;
    }

    /**
     * @return the hb_trigger_value
     */
    public short getHb_trigger_value() {
        return hb_trigger_value;
    }

    /**
     * @param hb_trigger_value the hb_trigger_value to set
     */
    public void setHb_trigger_value(short hb_trigger_value) {
        this.hb_trigger_value = hb_trigger_value;
    }
    
    /**
     * @return the hb_trigger_type
     */
    public byte getHb_trigger_type() {
        return hb_trigger_type;
    }

    /**
     * @param hb_trigger_value the hb_trigger_value to set
     */
    public void setHb_trigger_type(byte hb_trigger_type) {
        this.hb_trigger_type = hb_trigger_type;
    }

    /**
     * @return the motionSecsAgo
     */
    public short getMotionSecsAgo() {
        return motionSecsAgo;
    }

    /**
     * @param motionSecsAgo the motionSecsAgo to set
     */
    public void setMotionSecsAgo(short motionSecsAgo) {
        this.motionSecsAgo = motionSecsAgo;
    }

    /**
     * @return the curr_load
     */
    public short getCurr_load() {
        return curr_load;
    }

    /**
     * @param curr_load the curr_load to set
     */
    public void setCurr_load(short curr_load) {
        this.curr_load = curr_load;
    }

    /**
     * @return the last_voltage_step
     */
    public char getLast_voltage_step() {
        return last_voltage_step;
    }

    /**
     * @param last_voltage_step the last_voltage_step to set
     */
    public void setLast_voltage_step(char last_voltage_step) {
        this.last_voltage_step = last_voltage_step;
    }

    /**
     * @return the last_ambient_light
     */
    public short getLast_ambient_light() {
        return last_ambient_light;
    }

    /**
     * @param last_ambient_light the last_ambient_light to set
     */
    public void setLast_ambient_light(short last_ambient_light) {
        this.last_ambient_light = last_ambient_light;
    }

    /**
     * @return the last_temp
     */
    public short getLast_temp() {
        return last_temp;
    }

    /**
     * @param last_temp the last_temp to set
     */
    public void setLast_temp(short last_temp) {
        this.last_temp = last_temp;
    }

    /**
     * @return the motion_mask_word_1
     */
    public int getMotion_mask_word_1() {
        return motion_mask_word_1;
    }

    /**
     * @param motion_mask_word_1 the motion_mask_word_1 to set
     */
    public void setMotion_mask_word_1(int motion_mask_word_1) {
        this.motion_mask_word_1 = motion_mask_word_1;
    }

    /**
     * @return the motion_mask_word_2
     */
    public int getMotion_mask_word_2() {
        return motion_mask_word_2;
    }

    /**
     * @param motion_mask_word_2 the motion_mask_word_2 to set
     */
    public void setMotion_mask_word_2(int motion_mask_word_2) {
        this.motion_mask_word_2 = motion_mask_word_2;
    }

    /**
     * @return the currApp
     */
    public char getCurrApp() {
        return currApp;
    }

    /**
     * @param currApp the currApp to set
     */
    public void setCurrApp(char currApp) {
        this.currApp = currApp;
    }

    /**
     * @return the utc_time_secs
     */
    public int getUtc_time_secs() {
        return utc_time_secs;
    }

    /**
     * @param utc_time_secs the utc_time_secs to set
     */
    public void setUtc_time_secs(int utc_time_secs) {
        this.utc_time_secs = utc_time_secs;
    }

    /**
     * @return the interval_dura
     */
    public short getInterval_dura() {
        return interval_dura;
    }

    /**
     * @param interval_dura the interval_dura to set
     */
    public void setInterval_dura(short interval_dura) {
        this.interval_dura = interval_dura;
    }

    /**
     * @return the analog_value_1
     */
    public short getAnalog_value_1() {
        return analog_value_1;
    }

    /**
     * @param analog_value_1 the analog_value_1 to set
     */
    public void setAnalog_value_1(short analog_value_1) {
        this.analog_value_1 = analog_value_1;
    }

    /**
     * @return the analog_value_2
     */
    public short getAnalog_value_2() {
        return analog_value_2;
    }

    /**
     * @param analog_value_2 the analog_value_2 to set
     */
    public void setAnalog_value_2(short analog_value_2) {
        this.analog_value_2 = analog_value_2;
    }

    /**
     * @return the reserved
     */
    public byte getReserved() {
        return reserved;
    }

    /**
     * @param reserved the reserved to set
     */
    public void setReserved(byte reserved) {
        this.reserved = reserved;
    }

    /**
     * @return the frame_end_marker
     */
    public char getFrame_end_marker() {
        return frame_end_marker;
    }

    /**
     * @param frame_end_marker the frame_end_marker to set
     */
    public void setFrame_end_marker(char frame_end_marker) {
        this.frame_end_marker = frame_end_marker;
    }

    /**
     * @return the pmSeqNo
     */
    public int getPmSeqNo() {
        return pmSeqNo;
    }

    /**
     * @param pmSeqNo the pmSeqNo to set
     */
    public void setPmSeqNo(int pmSeqNo) {
        this.pmSeqNo = pmSeqNo;
    }

    /**
     * @return the oProfile
     */
    public Profile getoProfile() {
        return oProfile;
    }

    /**
     * @param oProfile the oProfile to set
     */
    public void setoProfile(Profile oProfile) {
        this.oProfile = oProfile;
    }

}
