package com.ems;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.ems.gw.GW10;
import com.ems.su.SensorUnit;

/**
 * @author Sameer Surjikar
 * 
 */
public class CLIHandler implements Runnable {

    ArrayList<SensorUnit> oSensorList;
    ArrayList<GW10> oGatewayList;

    public CLIHandler() {
        super();

    }

    public CLIHandler(ArrayList<SensorUnit> oSensorList, ArrayList<GW10> oGatewayList) {
        super();
        this.oSensorList = oSensorList;
        this.oGatewayList = oGatewayList;
    }

    @Override
    public void run() {

        int count = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        SimulatorCommands.setReader(reader);

        System.out.println(Constants.help);
        while (true) {
            String choice = "";
            try {
                choice = reader.readLine();
                if (choice.equals(""))
                    continue;
                if (choice.charAt(0) == 'h') {
                    if (count == 1)
                        System.out.println(Constants.secondRoundQuestion);
                    else {
                        System.out.println(Constants.StartQues);
                        count++;
                    }
                    System.out.println(Constants.menu);
                    System.out.println(Constants.enter);
                } else {
                    switch (Integer.parseInt(choice)) {
                    case 1:
                        SimulatorCommands.turnAllSensorOn(oSensorList);
                        break;
                    case 2:
                        SimulatorCommands.turnAllGatewayOn(oGatewayList);
                        break;
                    case 3:
                        SimulatorCommands.turnAllSensorOff(oSensorList);
                        break;
                    case 4:
                        SimulatorCommands.turnAllGatewayOff(oGatewayList);
                        break;
                    case 5:
                        SimulatorCommands.turnGatewayRangeOff(oGatewayList);
                        break;
                    case 6:
                        SimulatorCommands.turnGatewayRangeOn(oGatewayList);
                        break;
                    case 7:
                        SimulatorCommands.turnSensorRangeOn(oSensorList);
                        break;
                    case 8:
                        SimulatorCommands.turnSensorRangeOff(oSensorList);
                        break;
                    case 9:
                        SimulatorCommands.doSenosrGatewayAssignment(oSensorList, oGatewayList);
                        break;
                    case 10:
                        SimulatorCommands.currentState(oSensorList, oGatewayList);
                        break;
                    case 11:
                        // Close all sensors and gateway then exit
                        System.out.println(Constants.shuttingDown);
                        SimulatorCommands.turnAllSensorOff(oSensorList);
                        SimulatorCommands.turnAllGatewayOff(oGatewayList);
                        System.exit(0);
                        break;
                    default:
                        System.out.println(Constants.wrongChoice);
                        break;

                    }
                }
            } catch (NullPointerException ex) {
                System.out.println(Constants.invalidInput);

            } catch (NumberFormatException ex) {
                System.out.println(Constants.invalidInput);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println(Constants.someException);
                SimulatorCommands.turnAllSensorOff(oSensorList);
                SimulatorCommands.turnAllGatewayOff(oGatewayList);
                e.printStackTrace();
                System.exit(0);
            }

        }

    }

}
