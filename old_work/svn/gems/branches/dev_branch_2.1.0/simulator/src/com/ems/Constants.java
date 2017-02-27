package com.ems;

public final class Constants {

    public static String StartQues = "What do you want to do?";
    public static String secondRoundQuestion = "What do you want to do next?";
    public static String enter = "Please enter the value from option given above. ";
    public static String menu = "1>  Turn all sensor on \n" + "2>  Turn all gateway on \n"
            + "3>  Turn all sensor off \n" + "4>  Turn all gateway off \n" + "5>  Turn gateway range of \n"
            + "6>  Turn gateway range on \n" + "7>  Turn sensor range on \n" + "8> Turn sensor range off\n"
            + "9> Assign sensor to gateways \n" + "10> Current Status of Simulator \n" + "11> Exit\n";
    public static String flow = "General flow of use \n" + "1> First create gateway\n" + "2> Then sensor\n"
            + "3> Assign sensors to gateway \n" + "4> Now you can do On/Off in the way you like...";
    public static String cmdLineParameters = "java -jar Simulator.jar [switch (-f/-c)] [gateway interface] [gems ip] [delay] [comma seperated SU, e,g 0:0:1,0:0:2]";
    public static String help = "Type h to get menu";
    public static String shuttingDown = "Simulator is shutting down....";
    public static String wrongChoice = "Wrong choice please try again. Integer is not listed as choice in the given list";
    public static String someException = "Some Exception happend Simulator is shutting down.... ";
    public static String invalidInput = "Input is not valid. Please try again.";
    public static String simulatorStarted = "Simulator Started";
}
