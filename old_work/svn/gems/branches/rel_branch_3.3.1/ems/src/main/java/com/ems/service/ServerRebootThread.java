package com.ems.service;

import com.ems.server.ServerMain;

public class ServerRebootThread implements Runnable {

	@Override
	public void run() {
		
		try {
			Thread.sleep(1000);
			Process pr = null;
			String[] cmdArr = { "/bin/bash", ServerMain.getInstance().getTomcatLocation() + "/adminscripts/serverreboot.sh" };
            pr = Runtime.getRuntime().exec(cmdArr);
            pr.waitFor();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}
