package com.ems.commands;

import java.awt.Component.BaselineResizeBehavior;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.plprofile.Profile;
import com.ems.utils.Utils;

public class PlugloadPMStatFrame implements ICommandPktFrame {

    short min_ambient_light = 0;
    short max_ambient_light = 0;
    int avg_ambient_light = 0;

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

    byte savings = (byte) 1;

    char current_state = '2';
    int sysUpTimeSecs = '0';
    short lastResetReason = 0;
    short cuStats = 0;
    short numResetsByCU = 0;

    private Logger oLogger = Logger.getLogger(PlugloadPMStatFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 0x42;
    private short frame_length = 94;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xd8;
    private int pmSeqNo = -1;

    byte min_voltage_step = (byte) 0;
    byte max_voltage_step = (byte) 0;
    short avg_voltage_step = 80;
    byte last_voltage_step = (byte) 0;

    short min_temp = 2;
    short max_temp = 4;
    int avg_temp = 2;
    short last_temp = 2;

    short managedEnergy = 5000;
    int managedEnergyCum = 500;
    short unmanagedEnergy = 5000;
    int unmanagedEnergyCum = 500;

    short managedOnSecs = 0;
    short managedOnToOffSec = 0;
    short managedOffToOnSec = 0;

    short motionOffToOn; // zero or sec in the period the last time when went from
    short motionOnToOff; // zero or sec in the period the last
    short motionTimerExpired;

    byte savingType = (byte) 3;
    byte currState;
    int uptime;

    short cuCmdStatus;
    short cuStatus;
    byte currApp = (byte) 2;
    byte global_profile_checksum = 0;
    byte profile_checksum = 0;
    byte group_id = (byte) 1;

    short configChecksum;
    int utcTimeSecs;
    short intervalDur;
    short configChksumBmap;

    byte currMode = (byte) 1;
    byte currProfType;
    byte currPeriodType;
    int locMotionSecsAgo;
    int remMotionSecsAgo;

    byte currBehavior;
    byte occRptorCnt;
    byte noOfLoadChanges;
    int loadBaseline = 5 * 12;
    short managedCurrent, lastCurrent;

    int lastMotionSecsAgo = 0;

    private Profile oProfile;
    private char frame_end_marker = 0x5e;

    public PlugloadPMStatFrame(int pmSeq, Profile profile) {
        Random randomGenerator = new Random();        oProfile = profile;

        frame_length = (short) getLength();
        this.pmSeqNo = pmSeq;
    }

    
   
    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (oProfile != null) {
            global_profile_checksum = oProfile.getDefaultAdvanceProfileChecksum();
            profile_checksum = oProfile.getDefaultScheduleProfileChecksum();
            oLogger.fine("profile checksum is " + profile_checksum);
            oLogger.fine("global profile checksum is " + global_profile_checksum);
            oLogger.fine("---mode for " + nodeName + " is " + oProfile.getModeBaseOnProfile());

            
            if (oProfile.getModeBaseOnProfile() == 2) {
                managedEnergy = 0;
                unmanagedEnergy = 0;
                loadBaseline = 5 * 12;
                avg_voltage_step = 0;
                min_voltage_step = 0;
                max_voltage_step = 0;
                savingType = CommandsConstants.PLUGLOAD_TUNE_UP_SAVING;

            } else if (oProfile.getModeBaseOnProfile() == 0) {
               oLogger.fine("plugload: "+nodeName+" is full on");
                managedEnergy = 5000;
                unmanagedEnergy = 5000;
                loadBaseline = 5 * 12;
                avg_voltage_step = 100;
                managedEnergyCum += 500;
                unmanagedEnergyCum += 500;
                savingType = CommandsConstants.PLUGLOAD_NONE_SAVING;
                
            } else {
                if (oProfile.getOccModeBaseOnProfile()) {
                    oLogger.fine("creating occupancy");
                    managedEnergy = 5000;
                    unmanagedEnergy = 5000;
                    loadBaseline = 5 * 12;
                    avg_voltage_step = 100;
                    managedEnergyCum += 500;
                    unmanagedEnergyCum += 500;
                    savingType = CommandsConstants.PLUGLOAD_OCC_SAVING;

                } else {
                    oLogger.fine("plugload: "+nodeName+" is off");
                    managedEnergy = 0;
                    unmanagedEnergy = 0;
                    loadBaseline = 5 * 12;
                    avg_voltage_step = 0;
                    managedEnergyCum += 0;
                    unmanagedEnergyCum += 0;
                    savingType = CommandsConstants.PLUGLOAD_OCC_SAVING;
                }
            }
        }

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

            output.write(Utils.shortToByteArray(min_temp));
            output.write(Utils.shortToByteArray(max_temp));
            output.write(Utils.intToByteArray(avg_temp));
            output.write(Utils.shortToByteArray(last_temp));

            output.write(Utils.shortToByteArray(managedEnergy));
            oLogger.fine("managed energy byte is " + Utils.getPacket(Utils.shortToByteArray(managedEnergy)));
            output.write(Utils.intToByteArray(managedEnergyCum));
            output.write(Utils.shortToByteArray(unmanagedEnergy));
            output.write(Utils.intToByteArray(unmanagedEnergyCum));

            output.write(Utils.shortToByteArray(managedOnSecs));
            output.write(Utils.shortToByteArray(managedOnToOffSec));
            output.write(Utils.shortToByteArray(managedOffToOnSec));

            output.write(Utils.shortToByteArray(motionOffToOn));
            output.write(Utils.shortToByteArray(motionOnToOff));
          //  output.write(Utils.shortToByteArray(motionTimerExpired));

            output.write((byte) savingType);
            output.write((byte) currState);
            output.write(Utils.intToByteArray(uptime));

            output.write(Utils.shortToByteArray(cuCmdStatus)); 
            output.write(Utils.shortToByteArray(cuStatus));
            output.write((byte) currApp);

            output.write((byte) global_profile_checksum);
            output.write((byte) profile_checksum);
            output.write((byte) group_id);

            output.write(Utils.shortToByteArray(configChecksum));
            output.write(Utils.intToByteArray(utcTimeSecs));
            output.write(Utils.shortToByteArray(intervalDur));
            output.write(Utils.shortToByteArray(configChksumBmap));

            output.write((byte) currMode);
            output.write((byte) currProfType);
            output.write((byte) currPeriodType);
            output.write(Utils.intToByteArray(locMotionSecsAgo));
            output.write(Utils.intToByteArray(remMotionSecsAgo));

            output.write((byte) currBehavior);
            output.write((byte) occRptorCnt);
            output.write((byte) noOfLoadChanges);
            output.write(Utils.shortToByteArray(loadBaseline));
            output.write(Utils.shortToByteArray(managedCurrent));
            output.write(Utils.shortToByteArray(lastCurrent));
            oLogger.fine("baseline energy byte is " + Utils.getPacket(Utils.intToByteArray(loadBaseline))+" and value is "+loadBaseline);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        // TODO Auto-generated method stub
        return 94;
    }

    public Logger getoLogger() {
        return oLogger;
    }

    public void setoLogger(Logger oLogger) {
        this.oLogger = oLogger;
    }

    public char getFrame_start_marker() {
        return frame_start_marker;
    }

    public void setFrame_start_marker(char frame_start_marker) {
        this.frame_start_marker = frame_start_marker;
    }

    public char getProtocol_ver() {
        return protocol_ver;
    }

    public void setProtocol_ver(char protocol_ver) {
        this.protocol_ver = protocol_ver;
    }

    public short getFrame_length() {
        return frame_length;
    }

    public void setFrame_length(short frame_length) {
        this.frame_length = frame_length;
    }

    public int getTxnid() {
        return txnid;
    }

    public void setTxnid(int txnid) {
        this.txnid = txnid;
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

    public int getPmSeqNo() {
        return pmSeqNo;
    }

    public void setPmSeqNo(int pmSeqNo) {
        this.pmSeqNo = pmSeqNo;
    }

    public byte getMin_voltage_step() {
        return min_voltage_step;
    }

    public void setMin_voltage_step(byte min_voltage_step) {
        this.min_voltage_step = min_voltage_step;
    }

    public byte getMax_voltage_step() {
        return max_voltage_step;
    }

    public void setMax_voltage_step(byte max_voltage_step) {
        this.max_voltage_step = max_voltage_step;
    }

    public short getAvg_voltage_step() {
        return avg_voltage_step;
    }

    public void setAvg_voltage_step(short avg_voltage_step) {
        this.avg_voltage_step = avg_voltage_step;
    }

    public byte getLast_voltage_step() {
        return last_voltage_step;
    }

    public void setLast_voltage_step(byte last_voltage_step) {
        this.last_voltage_step = last_voltage_step;
    }

    public short getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(short min_temp) {
        this.min_temp = min_temp;
    }

    public short getMax_temp() {
        return max_temp;
    }

    public void setMax_temp(short max_temp) {
        this.max_temp = max_temp;
    }

    public int getAvg_temp() {
        return avg_temp;
    }

    public void setAvg_temp(int avg_temp) {
        this.avg_temp = avg_temp;
    }

    public short getLast_temp() {
        return last_temp;
    }

    public void setLast_temp(short last_temp) {
        this.last_temp = last_temp;
    }

    public short getManagedEnergy() {
        return managedEnergy;
    }

    public void setManagedEnergy(short managedEnergy) {
        this.managedEnergy = managedEnergy;
    }

    public int getManagedEnergyCum() {
        return managedEnergyCum;
    }

    public void setManagedEnergyCum(int managedEnergyCum) {
        this.managedEnergyCum = managedEnergyCum;
    }

    public short getUnmanagedEnergy() {
        return unmanagedEnergy;
    }

    public void setUnmanagedEnergy(short unmanagedEnergy) {
        this.unmanagedEnergy = unmanagedEnergy;
    }

    public int getUnmanagedEnergyCum() {
        return unmanagedEnergyCum;
    }

    public void setUnmanagedEnergyCum(int unmanagedEnergyCum) {
        this.unmanagedEnergyCum = unmanagedEnergyCum;
    }

    public short getManagedOnSecs() {
        return managedOnSecs;
    }

    public void setManagedOnSecs(short managedOnSecs) {
        this.managedOnSecs = managedOnSecs;
    }

    public short getManagedOnToOffSec() {
        return managedOnToOffSec;
    }

    public void setManagedOnToOffSec(short managedOnToOffSec) {
        this.managedOnToOffSec = managedOnToOffSec;
    }

    public short getManagedOffToOnSec() {
        return managedOffToOnSec;
    }

    public void setManagedOffToOnSec(short managedOffToOnSec) {
        this.managedOffToOnSec = managedOffToOnSec;
    }

    public short getMotionOffToOn() {
        return motionOffToOn;
    }

    public void setMotionOffToOn(short motionOffToOn) {
        this.motionOffToOn = motionOffToOn;
    }

    public short getMotionOnToOff() {
        return motionOnToOff;
    }

    public void setMotionOnToOff(short motionOnToOff) {
        this.motionOnToOff = motionOnToOff;
    }

    public short getMotionTimerExpired() {
        return motionTimerExpired;
    }

    public void setMotionTimerExpired(short motionTimerExpired) {
        this.motionTimerExpired = motionTimerExpired;
    }

    public byte getSavingType() {
        return savingType;
    }

    public void setSavingType(byte savingType) {
        this.savingType = savingType;
    }

    public byte getCurrState() {
        return currState;
    }

    public void setCurrState(byte currState) {
        this.currState = currState;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }

    public short getCuCmdStatus() {
        return cuCmdStatus;
    }

    public void setCuCmdStatus(short cuCmdStatus) {
        this.cuCmdStatus = cuCmdStatus;
    }

    public short getCuStatus() {
        return cuStatus;
    }

    public void setCuStatus(short cuStatus) {
        this.cuStatus = cuStatus;
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

    public short getConfigChecksum() {
        return configChecksum;
    }

    public void setConfigChecksum(short configChecksum) {
        this.configChecksum = configChecksum;
    }

    public int getUtcTimeSecs() {
        return utcTimeSecs;
    }

    public void setUtcTimeSecs(int utcTimeSecs) {
        this.utcTimeSecs = utcTimeSecs;
    }

    public short getIntervalDur() {
        return intervalDur;
    }

    public void setIntervalDur(short intervalDur) {
        this.intervalDur = intervalDur;
    }

    public short getConfigChksumBmap() {
        return configChksumBmap;
    }

    public void setConfigChksumBmap(short configChksumBmap) {
        this.configChksumBmap = configChksumBmap;
    }

    public byte getCurrMode() {
        return currMode;
    }

    public void setCurrMode(byte currMode) {
        this.currMode = currMode;
    }

    public byte getCurrProfType() {
        return currProfType;
    }

    public void setCurrProfType(byte currProfType) {
        this.currProfType = currProfType;
    }

    public byte getCurrPeriodType() {
        return currPeriodType;
    }

    public void setCurrPeriodType(byte currPeriodType) {
        this.currPeriodType = currPeriodType;
    }

    public int getLocMotionSecsAgo() {
        return locMotionSecsAgo;
    }

    public void setLocMotionSecsAgo(int locMotionSecsAgo) {
        this.locMotionSecsAgo = locMotionSecsAgo;
    }

    public int getRemMotionSecsAgo() {
        return remMotionSecsAgo;
    }

    public void setRemMotionSecsAgo(int remMotionSecsAgo) {
        this.remMotionSecsAgo = remMotionSecsAgo;
    }

    public byte getCurrBehavior() {
        return currBehavior;
    }

    public void setCurrBehavior(byte currBehavior) {
        this.currBehavior = currBehavior;
    }

    public byte getOccRptorCnt() {
        return occRptorCnt;
    }

    public void setOccRptorCnt(byte occRptorCnt) {
        this.occRptorCnt = occRptorCnt;
    }

    public byte getNoOfLoadChanges() {
        return noOfLoadChanges;
    }

    public void setNoOfLoadChanges(byte noOfLoadChanges) {
        this.noOfLoadChanges = noOfLoadChanges;
    }

    public int getLoadBaseline() {
        return loadBaseline;
    }

    public void setLoadBaseline(int loadBaseline) {
        this.loadBaseline = loadBaseline;
    }

    public int getLastMotionSecsAgo() {
        return lastMotionSecsAgo;
    }

    public void setLastMotionSecsAgo(int lastMotionSecsAgo) {
        this.lastMotionSecsAgo = lastMotionSecsAgo;
    }

    public Profile getoProfile() {
        return oProfile;
    }

    public void setoProfile(Profile oProfile) {
        this.oProfile = oProfile;
    }

    public char getFrame_end_marker() {
        return frame_end_marker;
    }

    public void setFrame_end_marker(char frame_end_marker) {
        this.frame_end_marker = frame_end_marker;
    }

}
