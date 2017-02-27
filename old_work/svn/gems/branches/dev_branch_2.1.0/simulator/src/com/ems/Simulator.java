package com.ems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ems.gw.GW10;
import com.ems.su.SensorUnit;
import com.ems.utils.Utils;

/**
 * @author Sameer Surjikar
 * 
 */
public class Simulator {

    public static void main(String[] args) throws IOException {
        System.out.println(Constants.simulatorStarted);
        try {
            start(args);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            start(args);
        }

    }

    public static void start(String[] args) throws IOException {

        ArrayList<SensorUnit> oSensorList = new ArrayList<SensorUnit>();
        ArrayList<GW10> oGatewayList = new ArrayList<GW10>();
        if (args != null && args[0].equalsIgnoreCase("-f")) {
            loadFromConfigurationFile(args[1], oSensorList, oGatewayList);
            SimulatorCommands.doSenosrGatewayAssignment(oSensorList, oGatewayList);
        }
        if (args != null && args[0].equalsIgnoreCase("-c")) {
            if (args.length < 4) {
                System.out.println(Constants.cmdLineParameters);
                System.exit(0);
            }

            try {
                int pktCount = 0;
                String gwInterface = args[1];
                String gemsIP = args[2];
                Utils.setDelay(Integer.parseInt(args[3]));
                String strSUList = args[4];
                String ipAddress = gemsIP;
                String gwIPAddress = Utils.getIpAddress(gwInterface);
                oGatewayList.add(new GW10(pktCount, gwInterface, Utils.setupGWIP(gwIPAddress), ipAddress));
                createSensor(strSUList, oSensorList);
                // Need this so that we have list of sensor unit for implementing observer pattern on updating
                // ems about ack package...
                Utils.setSensorUnits(oSensorList);
                int iJitter = Utils.getJitter();
                try {
                    String jitter = args[5];
                    iJitter = Integer.parseInt(jitter);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } finally {
                    Utils.setJitter(iJitter);
                }
                System.out.println("GW: " + gwIPAddress + ", GEMS: " + gemsIP + ", Sensor Units " + strSUList + ", Jitter: " + iJitter);
                SimulatorCommands.doSenosrGatewayAssignment(oSensorList, oGatewayList);
                SimulatorCommands.turnAllGatewayOn(oGatewayList);
                SimulatorCommands.turnAllSensorOn(oSensorList);
                SimulatorCommands.currentState(oSensorList, oGatewayList);
                //Thread cliThread = new Thread(new CLIHandler(oSensorList, oGatewayList));
                //cliThread.start();
                // cliThread.join();
            } catch (ArrayIndexOutOfBoundsException aiob) {

            } catch (NumberFormatException nfe) {
                Utils.setDelay(500000);
            } /*
               * catch (InterruptedException e) { // TODO Auto-generated catch block e.printStackTrace(); }
               */
        }

    }

    public static void loadFromConfigurationFile(String arg, ArrayList<SensorUnit> su, ArrayList<GW10> gw) {
        try {

            File fXmlFile = new File(arg);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            String gwip = "";
            String gemip = "";
            String sensorList = "";
            String delay = "500000";
            String jitter = String.valueOf(Utils.getJitter());

            NodeList nList = doc.getElementsByTagName("config");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    gwip = getTagValue("gatewayinterface", eElement);
                    gemip = getTagValue("gemip", eElement);
                    sensorList = getTagValue("sensorList", eElement);
                    delay = getTagValue("delay", eElement);
                    jitter = getTagValue("jitter", eElement);
                }
            }
            String args[] = new String[6];
            args[0] = "-c";
            args[1] = gwip;
            args[2] = gemip;
            args[3] = delay;
            args[4] = sensorList;
            args[5] = jitter;
            start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    private static void createSensor(String strSUList, ArrayList<SensorUnit> su) {
        String[] suList;
        suList = strSUList.split(",");
        for (int count = 0; count < suList.length; count++) {
            SensorUnit oSensor = new SensorUnit(suList[count]);
            su.add(oSensor);
        }
    }
}
