package com.ems.su;

/**
 * @author Sameer Surjikar
 * 
 */
public class Packets implements Comparable<Packets> {
    public static int PM_STAT_PKT = 1;
    public static int SU_ACK_PKT = 2;
    public static int GW_ACK_PKT = 3;
    public static int SU_DISCOVERY_ACK_PKT = 4;
    public static int MBIT_STAT_PKT = 5;
    public static int WDS_DISCOVERY_ACK_PKT = 6;
    public static int WDS_ACK_PKT = 7;
    public static int SU_REAL_TIME_PKT = 8;
    public static int DOWNLOAD_PROFILE_PKT = 9;
    public static final int GW_HANDSHAKE_PKT = 10;
    public static int SU_LAMP_CURVE_PKT = 11;
    public static int SU_REAL_TIME_HB_PKT = 12;
    public static int SU_PM_HB_PKT = 13;
    public static int SU_IMG_UPGRADE_PKT = 14;
    public static int SU_EVENT_PKT = 15;
    public static int NODE_INFO_PKT = 16;

    // Higher based on the level
    public static int PRIORITY_LEVEL_EQUAL = 0;
    public static int PRIORITY_LEVEL_ONE = 1; // Discovery
    public static int PRIORITY_LEVEL_TWO = 2; // PM stats
    public static int PRIORITY_LEVEL_THREE = 3; // GW Packets
    public static int PRIORITY_LEVEL_FOUR = 4; // GW handshake & set Wireless commands
    
    private String svName;
    private String message;
    private int iType;
    private int iPriority = PRIORITY_LEVEL_EQUAL;
    private byte[] packetData;

    public Packets(String svName, String message, int type, int priority, byte[] oPacket) {
        this.svName = svName;
        this.message = message;
        this.iType = type;
        this.iPriority = priority;
        this.packetData = oPacket;
    }

    public byte[] getPacketData() {
        return packetData;
    }

    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }

    public String getSvName() {
        return svName;
    }

    public void setSvName(String svName) {
        this.svName = svName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Packets [svName=" + svName + ", message=" + message + "]" + "Posted";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((svName == null) ? 0 : svName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Packets other = (Packets) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (svName == null) {
            if (other.svName != null)
                return false;
        } else if (!svName.equals(other.svName))
            return false;
        return true;
    }

    /**
     * @return the iType
     */
    public int getiType() {
        return iType;
    }

    /**
     * @param iType
     *            the iType to set
     */
    public void setiType(int iType) {
        this.iType = iType;
    }

    /**
     * @return the iPriority
     */
    public int getiPriority() {
        return iPriority;
    }

    /**
     * @param iPriority
     *            the iPriority to set
     */
    public void setiPriority(int iPriority) {
        this.iPriority = iPriority;
    }

    @Override
    public int compareTo(Packets oTo) {
        if (this.iPriority < oTo.iPriority)
            return -1;
        else if (this.iPriority > oTo.iPriority)
            return 1;
        return 0;
    }

}
