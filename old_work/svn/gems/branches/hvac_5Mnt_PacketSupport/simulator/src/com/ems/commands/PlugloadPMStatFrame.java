package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import com.ems.commands.plprofile.Profile;
import com.ems.utils.Utils;

public class PlugloadPMStatFrame implements ICommandPktFrame{
    
    private Logger oLogger = Logger.getLogger(PlugloadPMStatFrame.class.getName());
    
    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 72;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xd9;
    private int pmSeqNo = -1;
   
    
    char min_voltage_step = '0';
    char max_voltage_step = '0';
    short avg_voltage_step = '0';
    char last_voltage_step = '0';
    
    short managedOnSecs = 0;
    short managedOnToOffSec = 0;
    short managedOffToOnSec = 0;
    
    int lastMotionSecsAgo = 0;
    
    char min_temp = '2';
    char max_temp = '4';
    short avg_temp = 2;
    char last_temp = '2';
    
    byte savingType;    
    byte currState;    
    int energy_consumption = 0;
    byte currBehavior;
    
    long uptime;
    byte noOfLoadChanges;
    byte noOfPeersHeardFrom;
    
    int managedEnergy;
    long managedEnergyCum;
    int unmanagedEnergy;
    long unmanagedEnergyCum;
    
    int managedPower;
    int managedCurrent;
    int managedPowerFactor;
    int unmanagedPower;
    int unmanagedCurrent;
    int unmanagedPowerFactor;
    
    byte cuCmdStatus;
    int cuStatus;
    int noCuResets;
    
    byte globalProfileChecksum;
    byte schedProfileChecksum;
    byte profileGrpId;
    
    int configChecksum;
    long utcTimeSecs;
    int intervalDur;

    
    char currApp = '2';
    
    byte global_profile_checksum = 0;
    byte profile_checksum = 0;
    byte group_id = (byte) 1;
    
    private Profile oProfile;
    private char frame_end_marker = 0x5e;
    
    
    public PlugloadPMStatFrame(int pmSeq, Profile profile) {
        Random randomGenerator = new Random();
        oProfile = profile;        
        frame_length = (short) getLength();
        this.pmSeqNo = pmSeq;
    }
    
    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        global_profile_checksum = oProfile.getDefaultAdvanceProfileChecksum();
        profile_checksum = oProfile.getDefaultScheduleProfileChecksum();
        /*group_id = oProfile.getProfileNo();*/
        
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
            output.write(Utils.shortToByteArray(managedOnSecs));
            output.write(Utils.shortToByteArray(managedOnToOffSec));
            output.write(Utils.shortToByteArray(managedOffToOnSec));
            output.write((byte) min_temp);
            output.write((byte) max_temp);
            output.write(Utils.shortToByteArray(avg_temp));
            output.write((byte) last_temp);            
            output.write((byte) savingType);
            output.write((byte) currState);
            output.write((byte) currBehavior);           
            output.write(Utils.longToByteArray(uptime));  
            output.write((byte) noOfLoadChanges);
            output.write((byte) noOfPeersHeardFrom);
            output.write(Utils.intToByteArray(managedEnergy));
            output.write(Utils.longToByteArray(managedEnergyCum));
            output.write(Utils.intToByteArray(unmanagedEnergy));
            output.write(Utils.longToByteArray(unmanagedEnergyCum));
            output.write(Utils.intToByteArray(managedPower));
            output.write(Utils.intToByteArray(managedCurrent));
            output.write(Utils.intToByteArray(managedPowerFactor));
            output.write(Utils.intToByteArray(unmanagedPower));
            output.write(Utils.intToByteArray(unmanagedCurrent));
            output.write(Utils.intToByteArray(unmanagedPowerFactor));
            output.write((byte) cuCmdStatus);
            output.write(Utils.intToByteArray(cuStatus));
            output.write(Utils.intToByteArray(noCuResets));
            output.write((byte) currApp);            
            output.write((byte) global_profile_checksum);
            output.write((byte) profile_checksum);
            output.write((byte) group_id);
            output.write(Utils.intToByteArray(configChecksum));        
            output.write(Utils.longToByteArray(utcTimeSecs));
            output.write(Utils.intToByteArray(intervalDur));          
            output.write((byte) frame_end_marker);       
            
           

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        return output.toByteArray();
    }



    @Override
    public long getLength() {
        // TODO Auto-generated method stub
        return 72;
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



    public int getLastMotionSecsAgo() {
        return lastMotionSecsAgo;
    }



    public void setLastMotionSecsAgo(int lastMotionSecsAgo) {
        this.lastMotionSecsAgo = lastMotionSecsAgo;
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



    public int getEnergy_consumption() {
        return energy_consumption;
    }



    public void setEnergy_consumption(int energy_consumption) {
        this.energy_consumption = energy_consumption;
    }



    public byte getCurrBehavior() {
        return currBehavior;
    }



    public void setCurrBehavior(byte currBehavior) {
        this.currBehavior = currBehavior;
    }



    public long getUptime() {
        return uptime;
    }



    public void setUptime(long uptime) {
        this.uptime = uptime;
    }



    public byte getNoOfLoadChanges() {
        return noOfLoadChanges;
    }



    public void setNoOfLoadChanges(byte noOfLoadChanges) {
        this.noOfLoadChanges = noOfLoadChanges;
    }



    public byte getNoOfPeersHeardFrom() {
        return noOfPeersHeardFrom;
    }



    public void setNoOfPeersHeardFrom(byte noOfPeersHeardFrom) {
        this.noOfPeersHeardFrom = noOfPeersHeardFrom;
    }



    public int getManagedEnergy() {
        return managedEnergy;
    }



    public void setManagedEnergy(int managedEnergy) {
        this.managedEnergy = managedEnergy;
    }



    public long getManagedEnergyCum() {
        return managedEnergyCum;
    }



    public void setManagedEnergyCum(long managedEnergyCum) {
        this.managedEnergyCum = managedEnergyCum;
    }



    public int getUnmanagedEnergy() {
        return unmanagedEnergy;
    }



    public void setUnmanagedEnergy(int unmanagedEnergy) {
        this.unmanagedEnergy = unmanagedEnergy;
    }



    public long getUnmanagedEnergyCum() {
        return unmanagedEnergyCum;
    }



    public void setUnmanagedEnergyCum(long unmanagedEnergyCum) {
        this.unmanagedEnergyCum = unmanagedEnergyCum;
    }



    public int getManagedPower() {
        return managedPower;
    }



    public void setManagedPower(int managedPower) {
        this.managedPower = managedPower;
    }



    public int getManagedCurrent() {
        return managedCurrent;
    }



    public void setManagedCurrent(int managedCurrent) {
        this.managedCurrent = managedCurrent;
    }



    public int getManagedPowerFactor() {
        return managedPowerFactor;
    }



    public void setManagedPowerFactor(int managedPowerFactor) {
        this.managedPowerFactor = managedPowerFactor;
    }



    public int getUnmanagedPower() {
        return unmanagedPower;
    }



    public void setUnmanagedPower(int unmanagedPower) {
        this.unmanagedPower = unmanagedPower;
    }



    public int getUnmanagedCurrent() {
        return unmanagedCurrent;
    }



    public void setUnmanagedCurrent(int unmanagedCurrent) {
        this.unmanagedCurrent = unmanagedCurrent;
    }



    public int getUnmanagedPowerFactor() {
        return unmanagedPowerFactor;
    }



    public void setUnmanagedPowerFactor(int unmanagedPowerFactor) {
        this.unmanagedPowerFactor = unmanagedPowerFactor;
    }



    public byte getCuCmdStatus() {
        return cuCmdStatus;
    }



    public void setCuCmdStatus(byte cuCmdStatus) {
        this.cuCmdStatus = cuCmdStatus;
    }



    public int getCuStatus() {
        return cuStatus;
    }



    public void setCuStatus(int cuStatus) {
        this.cuStatus = cuStatus;
    }



    public int getNoCuResets() {
        return noCuResets;
    }



    public void setNoCuResets(int noCuResets) {
        this.noCuResets = noCuResets;
    }



    public byte getGlobalProfileChecksum() {
        return globalProfileChecksum;
    }



    public void setGlobalProfileChecksum(byte globalProfileChecksum) {
        this.globalProfileChecksum = globalProfileChecksum;
    }



    public byte getSchedProfileChecksum() {
        return schedProfileChecksum;
    }



    public void setSchedProfileChecksum(byte schedProfileChecksum) {
        this.schedProfileChecksum = schedProfileChecksum;
    }



    public byte getProfileGrpId() {
        return profileGrpId;
    }



    public void setProfileGrpId(byte profileGrpId) {
        this.profileGrpId = profileGrpId;
    }



    public int getConfigChecksum() {
        return configChecksum;
    }



    public void setConfigChecksum(int configChecksum) {
        this.configChecksum = configChecksum;
    }



    public long getUtcTimeSecs() {
        return utcTimeSecs;
    }



    public void setUtcTimeSecs(long utcTimeSecs) {
        this.utcTimeSecs = utcTimeSecs;
    }



    public int getIntervalDur() {
        return intervalDur;
    }



    public void setIntervalDur(int intervalDur) {
        this.intervalDur = intervalDur;
    }



    public char getCurrApp() {
        return currApp;
    }



    public void setCurrApp(char currApp) {
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



    public char getFrame_end_marker() {
        return frame_end_marker;
    }



    public void setFrame_end_marker(char frame_end_marker) {
        this.frame_end_marker = frame_end_marker;
    }


    
}
