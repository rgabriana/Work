package com.ems.commands;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.logging.Logger;

import com.ems.commands.plprofile.Profile;
import com.ems.utils.Utils;

public class PLRealTimeFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(PLRealTimeFrame.class.getName());

    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 44;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xdc;

    byte state = 1; // 4th
    byte voltage = 30; // 5th
    byte temperature = 35; // 6
    short ambientLight = 34; // 7,8
    short motionSecAgo = 78; // 9,10
    int currentTime = ((Long) Calendar.getInstance().getTimeInMillis()).intValue(); // 11-14
    int upTme = 456; // 15-18
    byte globalProfileChecksum = 4;// 19
    byte profileChecksum = 4;// 20
    short offTimer = 5;// 21-22
    short energyTicks = 7889;// 23,24

    int time = ((Long) Calendar.getInstance().getTimeInMillis()).intValue(); // 25-28
    short calibValue = 5667;// 29-30
    byte isHopper = 0; // 31
    byte group_id = (byte) 1;

    private char frame_end_marker = 0x5e;
    
    private Profile oProfile;
    
    
    public PLRealTimeFrame(byte msgType, Profile oProfile2, String sName) {
        this.msg_type = (char)msgType;
        oProfile = oProfile2;
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
                        motionSecAgo = Short.parseShort(output);
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
        globalProfileChecksum = oProfile.getDefaultAdvanceProfileChecksum();
        profileChecksum = oProfile.getDefaultScheduleProfileChecksum();
        group_id = oProfile.getProfileNo();

        try {
            output.write((byte) frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray(frame_length));
            output.write(Utils.intToByteArray(txnid));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            output.write((byte) state);
            output.write((byte) voltage);
            output.write((byte) temperature);
            output.write(Utils.shortToByteArray(ambientLight));
            output.write(Utils.shortToByteArray(motionSecAgo));
            output.write(Utils.intToByteArray(currentTime));
            output.write(Utils.intToByteArray(upTme));
            output.write((byte) globalProfileChecksum);
            output.write((byte) profileChecksum);
            output.write(Utils.shortToByteArray(offTimer));
            output.write(Utils.shortToByteArray(energyTicks));
            output.write(Utils.intToByteArray(time));
            output.write(Utils.shortToByteArray(calibValue));
            output.write((byte) isHopper);
            output.write((byte) group_id);
            output.write((byte) frame_end_marker);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        // TODO Auto-generated method stub
        return 42;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getVoltage() {
        return voltage;
    }

    public void setVoltage(byte voltage) {
        this.voltage = voltage;
    }

    public byte getTemperature() {
        return temperature;
    }

    public void setTemperature(byte temperature) {
        this.temperature = temperature;
    }

    public short getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(short ambientLight) {
        this.ambientLight = ambientLight;
    }

    public short getMotionSecAgo() {
        return motionSecAgo;
    }

    public void setMotionSecAgo(short motionSecAgo) {
        this.motionSecAgo = motionSecAgo;
    }

    public short getEnergyTicks() {
        return energyTicks;
    }

    public void setEnergyTicks(short energyTicks) {
        this.energyTicks = energyTicks;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public short getCalibValue() {
        return calibValue;
    }

    public void setCalibValue(short calibValue) {
        this.calibValue = calibValue;
    }

    public byte getIsHopper() {
        return isHopper;
    }

    public void setIsHopper(byte isHopper) {
        this.isHopper = isHopper;
    }
    
    

}
