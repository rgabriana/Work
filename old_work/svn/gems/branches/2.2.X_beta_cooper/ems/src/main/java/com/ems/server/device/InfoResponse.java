package com.ems.server.device;

public final class InfoResponse {
	
    //The version this instance of SNAPconnect is running
    public String version;
    //Whether SNAPconnect is connected to a bridge node
    public boolean connected;
    //The type of port to use for the connection (RS232=1, USB=2)
    public int portType;
    //The port number of the specified type starting at 1
    public int portNum;
    
} //end of class InfoResponse
