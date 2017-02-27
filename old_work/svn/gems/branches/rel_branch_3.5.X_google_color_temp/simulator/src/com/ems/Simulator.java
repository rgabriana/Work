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
import com.ems.gw.GW20;
import com.ems.gw.GWInterface;
import com.ems.plugload.PlugloadUnit;
import com.ems.su.SensorUnit;
import com.ems.utils.Utils;
import com.ems.wds.WDSUnit;

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
        ArrayList<GWInterface> oGatewayList = new ArrayList<GWInterface>();
        ArrayList<WDSUnit> oWDSList = new ArrayList<WDSUnit>();
        ArrayList<PlugloadUnit> oPlugloadList = new ArrayList<PlugloadUnit>();
        if (args != null && args[0].equalsIgnoreCase("-f")) {
            loadFromConfigurationFile(args[1], oSensorList, oGatewayList);
            //TODO: This seems wrong.  The last line of LoadFromConfig executes Start with the arguments.
            //      The start with arguments does the 'do*Assignments', why do they get called again?
            SimulatorCommands.doSenosrGatewayAssignment(oSensorList, oGatewayList);
            SimulatorCommands.doWDSGatewayAssignment(oWDSList, oGatewayList);
            SimulatorCommands.doPlugloadGatewayAssignment(oPlugloadList, oGatewayList);
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
                String strWDSList = args[6];
                String ipAddress = gemsIP;
                String gwIPAddress = Utils.getIpAddress(gwInterface);
                int iJitter = Utils.getJitter();
                try {
                    String jitter = args[5];
                    iJitter = Integer.parseInt(jitter);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } finally {
                    Utils.setJitter(iJitter);
                }
                int iEnableMBits = 0;
                try {
                    String enableMBits = args[7];
                    iEnableMBits = Integer.parseInt(enableMBits);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } finally {
                    Utils.setiEnabledMBits(iEnableMBits);
                }

                int iGWType = 0;
                try {
                    iGWType = Integer.parseInt(args[8]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setiGWType(iGWType);
                }

                if (iGWType == 0)
                    oGatewayList.add(new GW10(pktCount, gwInterface, Utils.setupGWIP(gwIPAddress), ipAddress));
                else
                    oGatewayList.add(new GW20(pktCount, gwInterface, Utils.setupGWIP(gwIPAddress), ipAddress));

                int ifxStatHB = 0;
                try {
                    ifxStatHB = Integer.parseInt(args[9]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setiFxStatHB(ifxStatHB);
                }
                
                int ifxStatHBJitter = 25000;
                try {
                    ifxStatHBJitter = Integer.parseInt(args[10]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setiFxStatHBJitter(ifxStatHBJitter);
                }
                
                int ipmstathb = 0;
                try {
                    ipmstathb = Integer.parseInt(args[11]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setIpmstathb(ipmstathb);
                }
                
                int customMotion = 0;
                try {
                    customMotion = Integer.parseInt(args[12]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setCustomMotion(customMotion != 0);
                }
                
                int randomTemp = 0;
                try {
                    randomTemp = Integer.parseInt(args[13]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setRandomTemp(randomTemp != 0);
                }
                
                int randomPowerLight = 0;
                try {
                    randomPowerLight = Integer.parseInt(args[14]);
                } catch (ArrayIndexOutOfBoundsException aiob) {
                } catch(NumberFormatException nfe) {
                } catch (NullPointerException npe) {
                } finally {
                    Utils.setRandomPowerLight(randomPowerLight != 0);
                }
                String strPLList = args[15];

                createSensor(strSUList, oSensorList);
                createWDS(strWDSList, oWDSList);
                createPlugload(strPLList, oPlugloadList);
                // Need this so that we have list of sensor unit for implementing observer pattern on updating
                // ems about ack package...
                Utils.setSensorUnits(oSensorList);
                Utils.setWDSUnits(oWDSList);
                Utils.setPlugloadUnits(oPlugloadList);
                System.out.println("GW: " + gwIPAddress + ", GEMS: " + gemsIP + ", Sensor Units " + strSUList
                        + ", Jitter: " + iJitter + ", WDS: " + strWDSList + ", MBits: " + iEnableMBits + ", FXHB: " + ifxStatHB +
                        ", pmstathb: " + ipmstathb + ", Plugload Units " + strPLList);
                SimulatorCommands.doSenosrGatewayAssignment(oSensorList, oGatewayList);
                SimulatorCommands.doWDSGatewayAssignment(oWDSList, oGatewayList);
                SimulatorCommands.doPlugloadGatewayAssignment(oPlugloadList, oGatewayList);
                SimulatorCommands.turnAllGatewayOn(oGatewayList);
                SimulatorCommands.turnAllSensorOn(oSensorList);
                SimulatorCommands.turnAllPlugloadsOn(oPlugloadList);
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

    public static void loadFromConfigurationFile(String arg, ArrayList<SensorUnit> su, ArrayList<GWInterface> gw) {
        try {

            File fXmlFile = new File(arg);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            int i;
            int index;
            int[] startval = {0,0,0};
            String gwip = "";
            String gemip = "";
            String sensorList = "";
            String sensorListStartId = "";
            String sensorListCnt = "";
            String wdsList = "";
            String wdsListStartId = "";
            String wdsListCnt = "";
            String plugloadList = "";
            String plugloadListStartId = "";
            String plugloadListCnt = "";
            String enableMBits = "0";
            String delay = "500000";
            String jitter = String.valueOf(Utils.getJitter());
            String gwtype = "0";
            String fxstatHeartbeat = "0";
            String fxHBjitter = "25000";
            String pmstatHb = "0";
            String customMotion  = "0";
            String randomTemp = "0";
            String randomPowerLight = "0";

            NodeList nList = doc.getElementsByTagName("config");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    gwip = getTagValue("gatewayinterface", eElement);
                    gemip = getTagValue("gemip", eElement);
                    sensorListStartId = getTagValue("sensorListStartId", eElement);
                    sensorListCnt = getTagValue("sensorListCnt", eElement);      
                    sensorList = getTagValue("sensorList", eElement);
                    wdsList = getTagValue("wdsList", eElement);
                    wdsListStartId = getTagValue("wdsListStartId", eElement);
                    wdsListCnt = getTagValue("wdsListCnt", eElement);
                    enableMBits = getTagValue("enablembits", eElement);
                    delay = getTagValue("delay", eElement);
                    jitter = getTagValue("jitter", eElement);
                    String sTemp = getTagValue("type", eElement);
                    plugloadList = getTagValue("plugloadList", eElement);
                    plugloadListStartId = getTagValue("plugloadListStartId", eElement);
                    plugloadListCnt = getTagValue("plugloadListCnt", eElement);
                    if (sensorList == null && sensorListStartId != null && sensorListCnt != null) {
                    	sensorList="";
                    	index=0;
                    	for(String tempstr:sensorListStartId.split(":")) {
                    		startval[index] = Integer.parseInt(tempstr,16);
                    		index++;
                    	}
                    	int sensorCnt = Integer.parseInt(sensorListCnt);
                    	if (sensorCnt > 250) {
                    		System.out.println("SensorListCnt is greater than 250. Exiting");
                    		System.exit(1);
                    	}
                    	for(i=1;i<=sensorCnt;i++) {
                    		if(i==1){
                    			sensorList = sensorList.concat(Integer.toHexString(startval[0])+
 				                       ":" + Integer.toHexString(startval[1])+
 				                       ":" + Integer.toHexString(startval[2]));
                    		} else {
                    		   sensorList = sensorList.concat("," + Integer.toHexString(startval[0])+
                    				                       ":" + Integer.toHexString(startval[1])+
                    				                       ":" + Integer.toHexString(startval[2]));	
                    		}
                    		startval = incrArray(startval);
                    	}
                    }
                    if (plugloadList == null && plugloadListStartId != null && plugloadListCnt != null) {
                    	plugloadList="";
                    	index=0;
                    	for(String tempstr:plugloadListStartId.split(":")) {
                    		startval[index] = Integer.parseInt(tempstr,16);
                    		index++;
                    	}
                    	int plugloadCnt = Integer.parseInt(plugloadListCnt);
                    	if (plugloadCnt > 250) {
                    		System.out.println("plugloadListCnt is greater than 250. Exiting");
                    		System.exit(1);
                    	}
                    	for(i=1;i<=plugloadCnt;i++) {
                    		if(i==1){
                    		   plugloadList = plugloadList.concat(Integer.toHexString(startval[0])+
  				                       ":" + Integer.toHexString(startval[1])+
  				                       ":" + Integer.toHexString(startval[2]));	
                    		} else {
                    		   plugloadList = plugloadList.concat("," + Integer.toHexString(startval[0])+
                    				                       ":" + Integer.toHexString(startval[1])+
                    				                       ":" + Integer.toHexString(startval[2]));
                    		}
                    		startval = incrArray(startval);
                    	}
                    }
                    if (wdsList == null && wdsListStartId != null && wdsListCnt != null) {
                    	wdsList="";
                    	index=0;
                    	for(String tempstr:wdsListStartId.split(":")) {
                    		startval[index] = Integer.parseInt(tempstr,16);
                    		index++;
                    	}
                    	int wdsCnt = Integer.parseInt(wdsListCnt);
                    	if (wdsCnt > 250) {
                    		System.out.println("wdsListCnt is greater than 250. Exiting");
                    		System.exit(1);
                    	}
                    	for(i=1;i<=wdsCnt;i++) {
                    		if(i==1){
                    		   wdsList = wdsList.concat(Integer.toHexString(startval[0])+
  				                       ":" + Integer.toHexString(startval[1])+
  				                       ":" + Integer.toHexString(startval[2]));
                    		} else {
                    		   wdsList = wdsList.concat("," + Integer.toHexString(startval[0])+
                    				                       ":" + Integer.toHexString(startval[1])+
                    				                       ":" + Integer.toHexString(startval[2]));
                    		}
                    		startval = incrArray(startval);
                    	}
                    }
                    if (sTemp != null) {
                        gwtype = sTemp;
                    }
                    sTemp = getTagValue("fxstathb", eElement);
                    if (sTemp != null) {
                        fxstatHeartbeat = sTemp;
                    }
                    sTemp = getTagValue("fxstathbjitter", eElement);
                    if (sTemp != null) {
                        fxHBjitter = sTemp;
                    }
                    sTemp = getTagValue("pmstatshb", eElement);
                    if (sTemp != null) {
                        pmstatHb = sTemp;
                    }
                    sTemp = getTagValue("customMotion", eElement);
                    if (sTemp != null) {
                        customMotion = sTemp;
                    }
                    sTemp = getTagValue("randomTemp", eElement);
                    if (sTemp != null) {
                        randomTemp = sTemp;
                    }
                    sTemp = getTagValue("randomPowerLight", eElement);
                    if (sTemp != null) {
                        randomPowerLight = sTemp;
                    }
                }
            }
            String args[] = new String[16];
            args[0] = "-c";
            args[1] = gwip;
            args[2] = gemip;
            args[3] = delay;
            args[4] = sensorList;
            args[5] = jitter;
            args[6] = wdsList;
            args[7] = enableMBits;
            args[8] = gwtype;
            args[9] = fxstatHeartbeat;
            args[10] = fxHBjitter;
            args[11] = pmstatHb;
            args[12] = customMotion;
            args[13] = randomTemp;
            args[14] = randomPowerLight;
            args[15] = plugloadList;
            start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        try {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
            Node nValue = (Node) nlList.item(0);
            return nValue.getNodeValue();
        }catch(NullPointerException npe) {
            System.out.println("sTag: " + sTag + " does not exists!");
        }
        return null;
    }

    private static int[] incrArray(int[] startval) {
    	if(startval[2]!=0xff) {
			startval[2] = startval[2] +1;
		} else {
		    startval[2] = 0;
		    if(startval[1] != 0xff) {
		    	startval[1] = startval[1] +1;
		    } else {
    		    startval[1] = 0;
    		    if(startval[0] != 0xff) {
    		    	startval[0] = startval[0] +1;
    		    } else {
    		    	startval[0] = 0;
    		    }
		    }
		}
    	return startval;
    }
    
    private static void createSensor(String strSUList, ArrayList<SensorUnit> su) {
        String[] suList;
        suList = strSUList.split(",");
        for (int count = 0; count < suList.length; count++) {
            SensorUnit oSensor = new SensorUnit(suList[count]);
            su.add(oSensor);
        }
    }
    
    private static void createWDS(String strWDSList, ArrayList<WDSUnit> wds) {
        String[] wdsList;
        if (strWDSList != null && !strWDSList.equals("")) {
            wdsList = strWDSList.split(",");
            for (int count = 0; count < wdsList.length; count++) {
                WDSUnit oWDS = new WDSUnit(wdsList[count]);
                wds.add(oWDS);
            }
        }
    }
    
    private static void createPlugload(String strplList, ArrayList<PlugloadUnit> pl) {
        String[] plList;
        if (strplList != null && !strplList.equals("")) {
            plList = strplList.split(",");
            for (int count = 0; count < plList.length; count++) {
                PlugloadUnit oPL = new PlugloadUnit(plList[count]);
                pl.add(oPL);
            }
        }
    }

}
