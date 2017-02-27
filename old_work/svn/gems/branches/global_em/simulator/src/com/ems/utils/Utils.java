package com.ems.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.ems.su.SensorUnit;
import com.ems.wds.WDSUnit;

/**
 * Created by IntelliJ IDEA. User: yogesh Date: 5/1/12 Time: 10:02 PM To change this template use File | Settings | File
 * Templates.
 */
public final class Utils {
    private static int nextSeq = 1;
    private static int delay = 1000;
    private static int iJitter = 240000;
    private static int iEnabledMBits = 0;
    private static ArrayList<CommonQueue> queueArray = new ArrayList();
    private static int commonQueueArrayCount;
    private static Logger oLogger = Logger.getLogger(Utils.class.getName());
    private static SUMsgFromGemsQueue oSUMsgQueue = new SUMsgFromGemsQueue();
    private static SUMsgFromGemsQueueLP oSUMsgQueueLP = new SUMsgFromGemsQueueLP();
    private static HashMap<String, SensorUnit> sensorUnits = new HashMap<String, SensorUnit>();
    private static HashMap<String, WDSUnit> wdsUnits = new HashMap<String, WDSUnit>();
    private static int iNoOfSensors = 0;
    private static int iNoOfWDS = 0;
    private static int iGWType = 0;
    private static int iFxStatHB = 0;
    private static int iFxStatHBJitter = 25000;
    private static int ipmstathb = 0;
    
    private static boolean customMotion = false;
    private static boolean randomTemp = false;
    private static boolean randomPowerLight = false;

    public static HashMap<String, SensorUnit> getSensorUnits() {
        return sensorUnits;
    }

    public static HashMap<String, WDSUnit> getWDSUnits() {
        return wdsUnits;
    }

    public static void setSensorUnits(ArrayList sensorUnit) {
        Iterator<SensorUnit> i = sensorUnit.iterator();
        while (i.hasNext()) {
            SensorUnit su = (SensorUnit) i.next();
            sensorUnits.put(su.getsName(), su);
        }
        iNoOfSensors = sensorUnit.size();
    }

    public static int getNoOfSensorUnits() {
        return iNoOfSensors;
    }

    public static void setWDSUnits(ArrayList wdsUnit) {
        Iterator<WDSUnit> i = wdsUnit.iterator();
        while (i.hasNext()) {
            WDSUnit wds = (WDSUnit) i.next();
            wdsUnits.put(wds.getsName(), wds);
        }
        iNoOfWDS = wdsUnit.size();
    }

    public static int getNoOfWDSUnits() {
        return iNoOfWDS;
    }

    public static void printPacket(byte[] packet) {
        StringBuffer oBuffer = new StringBuffer();
        int noOfBytes = packet.length;
        for (int i = 0; i < noOfBytes; i++) {
            oBuffer.append(String.format("%x ", packet[i]));
        }
        oLogger.fine(oBuffer.toString());
    } // end of method printPacket

    public static String getPacket(byte[] packet) {
        StringBuffer oBuffer = new StringBuffer();
        int noOfBytes = packet.length;
        for (int i = 0; i < noOfBytes; i++) {
            oBuffer.append(String.format("%x ", packet[i]));
        }
        return oBuffer.toString();
    }

    // it extracts 2 bytes from the start position
    public static final int extractShortFromByteArray(byte[] b, int startPos) {

        int l = 0;
        l |= b[startPos] & 0xff;
        l = (l << 8) | b[startPos + 1] & 0xff;
        return l;

    } // end of method extractShortFromByteArray

    public static final byte[] shortToByteArray(int value) {

        return new byte[] { (byte) ((value & 0xFF00) >> 8), (byte) (value & 0xFF) };

    }

    public static final byte[] intToByteArray(int value) {

        return new byte[] { (byte) ((value & 0xFF000000) >> 24), (byte) ((value & 0xFF0000) >> 16),
                (byte) ((value & 0xFF00) >> 8), (byte) (value & 0xFF) };

    }

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(int delay) {
        Utils.delay = delay;
    }

    public static synchronized int getNextSeqNo() {
        if (nextSeq == 1) {
            Random randomGenerator = new Random(System.currentTimeMillis());
            nextSeq = randomGenerator.nextInt(10000);
        }
        return nextSeq++ % 10000 + 1;
    }

    public static final long intByteArrayToLong(byte[] b) {
        long l = 0;
        l |= b[0] & 0xff;
        l = (l << 8) | b[1] & 0xff;
        l = (l << 8) | b[2] & 0xff;
        l = (l << 8) | b[3] & 0xff;
        return l;
    }

    public static byte[] getSnapAddr(String addr) {

        try {
            byte[] addrArr = new byte[3];
            StringTokenizer st = new StringTokenizer(addr, ":");
            int i = 0;
            short tempShort = 0;
            if (st.countTokens() == 3) {
                while (st.hasMoreTokens()) {
                    tempShort = Short.parseShort(st.nextToken(), 16);
                    if (tempShort > 127) {
                        tempShort = (short) (tempShort - 256);
                    }
                    addrArr[i++] = (byte) tempShort;
                    // addrArr[i++] = Byte.parseByte(st.nextToken());
                }
                return addrArr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    } // end of method getSnapAddr

    public static String getSnapAddr(byte[] addr) {
        int len = addr.length;
        if (len < 3) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        short tempShort = 0;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(":");
            }
            if (addr[i] < 0) {
                tempShort = (short) (256 + addr[i]);
            } else {
                tempShort = addr[i];
            }
            sb.append(Integer.toString(tempShort, 16));
        }
        return sb.toString();
    } // end of method getSnapAddr

    public static String convertByteArrToIp(byte[] ipArr) {

        StringBuffer sb = new StringBuffer();
        short tempShort = 0;
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                sb.append(".");
            }
            if (ipArr[i] < 0) {
                tempShort = (short) (256 + ipArr[i]);
            } else {
                tempShort = ipArr[i];
            }
            sb.append(tempShort);
        }
        return sb.toString();

    }

    /**
     * Fetch the IP address given the interface.
     * 
     * @param sIface
     * @return
     */
    public static String getIpAddress(String sIface) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                if (ni.getName().equals(sIface)) {
                    Enumeration<InetAddress> e2 = ni.getInetAddresses();

                    while (e2.hasMoreElements()) {
                        InetAddress ip = (InetAddress) e2.nextElement();
                        if (ip instanceof Inet4Address)
                            return convertByteArrToIp(ip.getAddress());
                    }
                }
                Enumeration<NetworkInterface> se = ni.getSubInterfaces();
                while (se.hasMoreElements()) {
                    NetworkInterface sni = (NetworkInterface) se.nextElement();
                    if (sni.getName().equals(sIface)) {
                        Enumeration<InetAddress> e2 = sni.getInetAddresses();

                        while (e2.hasMoreElements()) {
                            InetAddress ip = (InetAddress) e2.nextElement();
                            if (ip instanceof Inet4Address)
                                return convertByteArrToIp(ip.getAddress());
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    public static InetAddress setupGWIP(String IPaddress) {
        InetAddress gwIPAddress = null;
        try {
            gwIPAddress = InetAddress.getByName(IPaddress);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return gwIPAddress;
    }

    /*
     * @author Sameer Surjikar
     */
    public static synchronized CommonQueue getCommonQueue(int i) {
        return queueArray.get(i);
    }

    public static synchronized void setCommonQueue(CommonQueue cq) {
        queueArray.add(cq);
        commonQueueArrayCount++;
    }

    public static int getCommonQueueArrayCount() {
        return commonQueueArrayCount;
    }

    public static void setCommonQueueArrayCount(int commonQueueArrayCount) {
        Utils.commonQueueArrayCount = commonQueueArrayCount;
    }

    // yet to be implemented
    public static List<String> range2GatewayIPlist(String startIp, String endIp) {
        List<String> str = new ArrayList<String>();
        str.add("127.0.0.1");
        str.add("127.0.0.2");
        str.add("127.0.0.3");
        return str;
    }

    // Yet to be implemented
    public static List<String> range2SensorIPlist(String startIp, String endIp) {
        List<String> str = new ArrayList<String>();
        str.add("98:89:97");
        str.add("98:89:56");
        str.add("98:89:72");
        return str;
    }

    public static synchronized SUMsgFromGemsQueue getSUMsgQueue() {
        return oSUMsgQueue;
    }

    /**
     * @deprecated Use the priority field in the Packet instead
     * @return Low priority queue
     */
    private static synchronized SUMsgFromGemsQueueLP getSUMsgQueueLP() {
        return oSUMsgQueueLP;
    }

    /**
     * @return the iJitter
     */
    public static int getJitter() {
        return iJitter;
    }

    /**
     * @param iJitter
     *            the iJitter to set
     */
    public static void setJitter(int jitter) {
        iJitter = jitter;
    }

    public static final int extractIntFromByteArray(byte[] b, int startPos) {

        int value = 0;
        value |= b[startPos] & 0xff;
        value = (value << 8) | b[startPos + 1] & 0xff;
        value = (value << 8) | b[startPos + 2] & 0xff;
        value = (value << 8) | b[startPos + 3] & 0xff;
        // l = ((b[startPos] & 0xff) << 24) | ((b[startPos+1] & 0xff) << 16) | ((b[startPos+2] & 0xff) << 8) |
        // (b[startPos+3] & 0xff);
        return value;

    }

    /**
     * @return the iEnabledMBits
     */
    public static int getiEnabledMBits() {
        return iEnabledMBits;
    }

    /**
     * @param iEnabledMBits
     *            the iEnabledMBits to set
     */
    public static void setiEnabledMBits(int iEnabledMBits) {
        Utils.iEnabledMBits = iEnabledMBits;
    }

    public static byte computeChecksum(byte[] array) {

        int noOfBytes = array.length;
        byte checksum = 0;
        for (int i = 0; i < noOfBytes; i++) {
            checksum ^= array[i];
        }
        return checksum;
    }

    public static int byteArrayToShort(byte[] b) {
        int l = 0;
        l |= b[0] & 0xff;
        l = (l << 8) | b[1] & 0xff;
        return l;
    }

    /**
     * @return the iGWType
     */
    public static int getiGWType() {
        return iGWType;
    }

    /**
     * @param iGWType the iGWType to set
     */
    public static void setiGWType(int iGWType) {
        Utils.iGWType = iGWType;
    }

    /**
     * @return the iFxStatHB
     */
    public static int getiFxStatHB() {
        return iFxStatHB;
    }

    /**
     * @param iFxStatHB the iFxStatHB to set
     */
    public static void setiFxStatHB(int iFxStatHB) {
        Utils.iFxStatHB = iFxStatHB;
    }

    /**
     * @return the iFxStatHBJitter
     */
    public static int getiFxStatHBJitter() {
        return iFxStatHBJitter;
    }

    /**
     * @param iFxStatHBJitter the iFxStatHBJitter to set
     */
    public static void setiFxStatHBJitter(int iFxStatHBJitter) {
        Utils.iFxStatHBJitter = iFxStatHBJitter;
    }

    /**
     * @return the ipmstathb
     */
    public static int getIpmstathb() {
        return ipmstathb;
    }

    /**
     * @param ipmstathb the ipmstathb to set
     */
    public static void setIpmstathb(int ipmstathb) {
        Utils.ipmstathb = ipmstathb;
    }

    /**
     * @return the customMotion
     */
    public static boolean isCustomMotion() {
        return customMotion;
    }

    /**
     * @param customMotion the customMotion to set
     */
    public static void setCustomMotion(boolean customMotion) {
        Utils.customMotion = customMotion;
    }

    /**
     * @return the randomTemp
     */
    public static boolean isRandomTemp() {
        return randomTemp;
    }

    /**
     * @param randomTemp the randomTemp to set
     */
    public static void setRandomTemp(boolean randomTemp) {
        Utils.randomTemp = randomTemp;
    }

    /**
     * @return the randomPowerLight
     */
    public static boolean isRandomPowerLight() {
        return randomPowerLight;
    }

    /**
     * @param randomPowerLight the randomPowerLight to set
     */
    public static void setRandomPowerLight(boolean randomPowerLight) {
        Utils.randomPowerLight = randomPowerLight;
    } 
}