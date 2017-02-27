package com.enlightedinc.dashboard.server;

public class Globals {
	public int state;
	
	// communication error state
	final public int state_comm_success				=	100;	
	final public int state_comm_url_failed			=	101;
	final public int state_comm_connect_failed		=	102;
	final public int state_comm_init_failed			=	103;
	final public int state_comm_send_failed			=	104;
	final public int state_comm_read_failed			=	105;

	public String buffer			= "";
	
	public Globals() {
		
	}
}
