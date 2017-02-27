package com.ems;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.ems.gw.GW10;
import com.ems.su.SensorUnit;

import com.ems.utils.Utils;
import com.ems.wds.WDSUnit;

public final class SimulatorCommands {

    private static BufferedReader reader;

    public static BufferedReader getReader() {
        return reader;
    }

    public static void setReader(BufferedReader reader) {
        SimulatorCommands.reader = reader;
    }

    public static SensorUnit createSensor() {
        System.out.println("Pass the sensor IP");
        String ip = "";
        try {
            ip = reader.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SensorUnit su = new SensorUnit(ip);
        System.out.println("Sensor Created.. ");

        return su;
    }

    public static GW10 createGateway() {

        int pktCount = 0;
        String gwInterface = "";
        String gwIPAddress = "";
        String ipAddress = "";
        try {
            System.out.println("Pass Packet count");
            pktCount = Integer.parseInt(reader.readLine());

            System.out.println("Pass Gateway Interface");
            gwInterface = reader.readLine();
            System.out.println("Pass Gateway IP");
            gwIPAddress = reader.readLine();
            System.out.println("Pass Gem IP");
            ipAddress = reader.readLine();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Gateway Created");
        return new GW10(pktCount, gwInterface, Utils.setupGWIP(gwIPAddress), ipAddress);
    }

    public static void turnSensorRangeOff(ArrayList<SensorUnit> c) throws NumberFormatException, IOException {

        System.out.println("Total no of sensor : " + c.size());
        System.out.println("specify the range ");
        System.out.println("Start from : ");
        int start = Integer.parseInt(reader.readLine());
        System.out.println("End on : ");
        int end = Integer.parseInt(reader.readLine());
        for (int i = start - 1; i < end; i++) {
            c.get(i).off();
        }
        System.out.println("Sensors between range " + start + " and " + end + " are off");
    }

    public static void turnSensorRangeOn(ArrayList<SensorUnit> c) throws NumberFormatException, IOException {

        System.out.println("Total no of sensor : " + c.size());
        System.out.println("specify the range ");
        System.out.println("Start from : ");
        int start = Integer.parseInt(reader.readLine());
        System.out.println("End on : ");
        int end = Integer.parseInt(reader.readLine());
        for (int i = start - 1; i < end; i++) {
            c.get(i).on();
        }
        System.out.println("Sensors between range " + start + " and " + end + " are on");
    }

    public static void turnGatewayRangeOff(ArrayList<GW10> c) throws NumberFormatException, IOException {
        System.out.println("Total no of Gateway : " + c.size());
        System.out.println("specify the range ");
        System.out.println("Start from : ");
        int start = Integer.parseInt(reader.readLine());
        System.out.println("End on : ");
        int end = Integer.parseInt(reader.readLine());
        for (int i = start - 1; i < end; i++) {
            c.get(i).off();
        }
        System.out.println("Gateway between range " + start + " and " + end + " are off");
    }

    public static void turnGatewayRangeOn(ArrayList<GW10> c) throws NumberFormatException, IOException {

        System.out.println("Total no of Gateway : " + c.size());
        System.out.println("specify the range ");
        System.out.println("Start from : ");
        int start = Integer.parseInt(reader.readLine());
        System.out.println("End on : ");
        int end = Integer.parseInt(reader.readLine());
        for (int i = start - 1; i < end; i++) {

            c.get(i).on();
        }
        System.out.println("Gateway between range " + start + " and " + end + " are on");
    }

    public static ArrayList<SensorUnit> CreateSensorRange() throws IOException {
        ArrayList<SensorUnit> oSensor = new ArrayList<SensorUnit>();
        String startRange;
        String endRange;

        System.out.println("Enter the Ip range for sensor");
        System.out.println("Start from :");
        startRange = reader.readLine();
        System.out.println("End on :");
        endRange = reader.readLine();
        ArrayList<String> strSUList = (ArrayList<String>) Utils.range2SensorIPlist(startRange, endRange);

        for (int count = 0; count < strSUList.size(); count++) {
            System.out.println(strSUList.get(count));

            oSensor.add(new SensorUnit(strSUList.get(count)));
        }
        return oSensor;
    }

    public static ArrayList<GW10> CreateGatewayRange() throws IOException {
        int pktCount = 0;
        String gwInterface = "";
        String ipAddress = "";
        String startRange;
        String endRange;
        System.out.println("Enter the Ip range for Gateway IP");
        System.out.println("Start from :");
        startRange = reader.readLine();
        System.out.println("End on :");
        endRange = reader.readLine();

        System.out.println("Pass Packet count");
        pktCount = Integer.parseInt(reader.readLine());
        System.out.println("Pass Gateway Interface");
        gwInterface = reader.readLine();
        System.out.println("Pass Gem IP");
        ipAddress = reader.readLine();
        ArrayList<String> gatewayIpList = (ArrayList<String>) Utils.range2GatewayIPlist(startRange, endRange);
        ArrayList<GW10> oGateway = new ArrayList<GW10>();

        for (int count = 0; count < gatewayIpList.size(); count++) {

            oGateway.add(new GW10(pktCount, gwInterface, Utils.setupGWIP(gatewayIpList.get(count)), ipAddress));
        }
        return oGateway;
    }

    public static synchronized void turnAllSensorOn(ArrayList<SensorUnit> su) {
        Iterator<SensorUnit> i = su.iterator();
        while (i.hasNext()) {
            i.next().on();
        }
        System.out.println("All sensors are switched on");
    }

    public static void turnAllSensorOff(ArrayList<SensorUnit> su) {
        Iterator<SensorUnit> i = su.iterator();
        while (i.hasNext()) {
            i.next().off();
        }
        System.out.println("All sesors are off");
    }

    public static synchronized void turnAllGatewayOn(ArrayList<GW10> su) {
        Iterator<GW10> i = su.iterator();
        while (i.hasNext()) {
            i.next().on();
        }
        System.out.println("All gateways are on");

    }

    public static void turnAllGatewayOff(ArrayList<GW10> su) {
        Iterator<GW10> i = su.iterator();
        while (i.hasNext()) {
            i.next().off();
        }
        System.out.println("All gateways are off");
    }

    public static void doSenosrGatewayAssignment(ArrayList<SensorUnit> su, ArrayList<GW10> gu) {
        int j = 0;
        for (int i = 0; i < su.size(); i++) {
            if (j == gu.size() && gu.size() != 1) {
                j = 0;
            }
            su.get(i).assignSensortoGatewy(gu.get(j));
            if (gu.size() == 1)
                continue;
            else
                j++;
        }
        System.out.println("Sensors are Assigned and ready to start....");
    }
    
    public static void doWDSGatewayAssignment(ArrayList<WDSUnit> wds, ArrayList<GW10> gu) {
        int j = 0;
        for (int i = 0; i < wds.size(); i++) {
            if (j == gu.size() && gu.size() != 1) {
                j = 0;
            }
            wds.get(i).assignWDStoGatewy(gu.get(j));
            if (gu.size() == 1)
                continue;
            else
                j++;
        }
        System.out.println("WDS are Assigned and ready to start....");
    }

    public static void currentState(ArrayList<SensorUnit> su, ArrayList<GW10> gw) {
        int count = 0;
        System.out.println("Current Number of Sensors Loaded in Simulator : " + su.size());
        System.out.println("Current Number of Gateway Loaded in Simulator : " + gw.size());
        System.out.println("Gateway running :");
        Iterator<GW10> iterator = gw.iterator();
        while (iterator.hasNext()) {
            GW10 gateway = iterator.next();
            if (gateway.getKeepRunningreciever().isAlive())
                System.out.println(gateway.getM_gwIPAddress().getHostName());
        }
        System.out.println("Sensors running :");
        Iterator<SensorUnit> sIterator = su.iterator();
        while (sIterator.hasNext()) {
            SensorUnit sensor = sIterator.next();
            if (count % 5 == 0) {
                System.out.println();
            }
            if (sensor.isKeepRunning()) {
                System.out.print(sensor.getsName() + "   ");
                count++;
            } else
                continue;

        }
        System.out.println();
    }

}
