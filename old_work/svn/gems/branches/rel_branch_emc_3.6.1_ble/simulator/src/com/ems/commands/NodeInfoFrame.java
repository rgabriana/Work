package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.ems.commands.profile.Profile;
import com.ems.utils.Utils;


public class NodeInfoFrame implements ICommandPktFrame {
    private Logger oLogger = Logger.getLogger(NodeInfoFrame.class.getName());

    /**
     * Warning :- If you are adding parameters to this class be sure you are making necessary changes to
     * CommandsUtils.getTotalLengthOfClassVariables() function. This function computes length for the classes. You might
     * want to add the types to this function for the variables you are adding.
     */
    private char frame_start_marker = 0x58;
    private char protocol_ver = 2;
    private short frame_length = 66;

    private int txnid = Utils.getNextSeqNo();

    private byte[] node_name = { 0, 0, 0 };

    private char msg_type = 0xd4;

    

    
    byte g_profile_checksum = 0;
    byte s_profile_checksum = 0;
    byte group_id = (byte) 1;
    byte boot_loader_major = 4;
    byte boot_loader_minor = 0;
    byte bugfix_version = 0;
    short svn_tag = 512;
    byte currentApp_major = 5;
    byte currentApp_minor = 0;
    byte appId = 2;
    byte bypassOn = 0;
    byte resetReason = 0;
    byte[] model = {'S','U','-','F','A','K','E',0,0,0,0,0};
    byte[] unused = {0,0,0,0,0,0};
    byte[] zigbeeGWid = {0,0,0};
    short calibvalue          = 0;
    byte  altApp_major        = 0;
    byte  altApp_minor        = 0;
    short cuversion           = 33;
    byte  imageUpgradePending = 0;
    byte  rsvd                = 0;
    short altsvntag           = 256;
    byte  groupId             = 9;
    byte  alt_bugfixversion   = 0;
    short config_checksum     = 0;
    byte  wiringstate         = 2;
    byte  hopper              = 0;
    short config_checksum_bmp = 0;
    byte  dalicnt               = 4;
    private char frame_end_marker = 0x5e;
    
    private int pmSeqNo = -1;
    
    private Profile oProfile = null;

    public NodeInfoFrame() {
        
    }
    
    public NodeInfoFrame(int pmSeq, Profile profile) {

        oProfile = profile;
      //  System.out.println("max light level is "+oProfile.getMaxLightLevel());
       
      
        this.pmSeqNo = pmSeq;
    }

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        g_profile_checksum = oProfile.getDefaultAdvanceProfileChecksum();
        s_profile_checksum = oProfile.getDefaultScheduleProfileChecksum();
        groupId = oProfile.getProfileNo();
        
        try {
            output.write((byte) frame_start_marker);
            output.write((byte) protocol_ver);
            output.write(Utils.shortToByteArray(frame_length));
            output.write(Utils.intToByteArray(pmSeqNo));
            node_name = Utils.getSnapAddr(nodeName);
            output.write(node_name);
            output.write((byte) msg_type);
            output.write((byte) g_profile_checksum);
            output.write((byte) s_profile_checksum);
            output.write((byte) boot_loader_major);
            output.write((byte) boot_loader_minor);
            output.write((byte) bugfix_version);
            output.write(Utils.shortToByteArray(svn_tag));
            output.write((byte) currentApp_major);
            output.write((byte) currentApp_minor);
            output.write((byte) appId);
            output.write((byte) bypassOn);
            output.write((byte) resetReason);
            output.write(model);
            output.write(unused);
            output.write(zigbeeGWid);
            output.write(Utils.shortToByteArray(calibvalue));
            output.write((byte) altApp_major);
            output.write((byte) altApp_minor);
            output.write(Utils.shortToByteArray(cuversion));
            output.write((byte) imageUpgradePending);
            output.write((byte) rsvd);
            output.write(Utils.shortToByteArray(altsvntag));
            output.write((byte) groupId);
            output.write((byte) alt_bugfixversion);
            output.write(Utils.shortToByteArray(config_checksum));
            output.write((byte) wiringstate);
            output.write((byte) hopper);
            output.write(Utils.shortToByteArray(config_checksum_bmp));
            output.write((byte)dalicnt);
            output.write((byte) frame_end_marker);

        } catch (IOException e) {
            oLogger.warning(e.getMessage());
        }
        oLogger.fine("NodeInfo: "+ DatatypeConverter.printHexBinary(output.toByteArray()));
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

  

   

    public byte getCurrApp() {
        return appId;
    }

    public void setCurrApp(byte currApp) {
        this.appId = currApp;
    }

    public byte getGlobal_profile_checksum() {
        return g_profile_checksum;
    }

    public void setGlobal_profile_checksum(byte global_profile_checksum) {
        this.g_profile_checksum = global_profile_checksum;
    }

    public byte getProfile_checksum() {
        return s_profile_checksum;
    }

    public void setProfile_checksum(byte profile_checksum) {
        this.s_profile_checksum = profile_checksum;
    }

    public byte getGroup_id() {
        return group_id;
    }

    public void setGroup_id(byte group_id) {
        this.group_id = group_id;
    }
    
    public byte getAppId() {
        return this.appId;
    }

    public void setAppId(byte app_id) {
        this.appId = app_id;
    }
    
   
}
