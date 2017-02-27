package com.communicator.util;

public class Globals {

	public static int state;
	
	// communication error state
	final public static int state_comm_success				=	100;	
	final public static int state_comm_url_failed			=	101;
	final public static int state_comm_connect_failed		=	102;
	final public static int state_comm_init_failed			=	103;
	final public static int state_comm_send_failed			=	104;
	final public static int state_comm_read_failed			=	105;

	public static String buffer			= "";
	
	public static String propFile = "" ;
	public static String  loginUrl="https://localhost/ems/wsaction.action" ;
	
	public static String updateSystemConfig = "https://localhost/ems/services/systemconfig/edit";
	
	public static Communicator communicator = new Communicator();
	
	public static void updateSystemConfig(String name, String value) {
		communicator.webServicePostRequest("<systemConfiguration><name>" + name + "</name><value>" +
												value +"</value></systemConfiguration>", 
											updateSystemConfig, 30000);
	}
}
