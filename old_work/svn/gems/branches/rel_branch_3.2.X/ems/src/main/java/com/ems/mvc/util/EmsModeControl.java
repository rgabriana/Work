package com.ems.mvc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EmsModeControl {
	
	public static String setMode(String state) {
		String output = "N";
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(new String[]{"checkandsetemmode.sh", state});
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = outputStream.readLine()) != null ) {
				if (!"".equals(line.trim())) {
					output = line.trim();
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return output;
	}
	
	public static boolean alreadyRunning() {
		String state = null;
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(new String[]{"head", "-n", "1", "/var/lib/tomcat6/Enlighted/emsmode"});
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String line = null;

			while ((line = outputStream.readLine()) != null ) {
				if (!"".equals(line.trim())) {
					state = line.trim();
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		if ("NORMAL".equals(state) || "TOMCAT_SHUTDOWN".equals(state)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static void resetToNormalIfImageUpgrade() {
		String state = null;
		for(int i = 0; i < 10000; i++) {
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(new String[]{"head", "-n", "1", "/var/lib/tomcat6/Enlighted/emsmode"});
				BufferedReader outputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line = null;
	
				while ((line = outputStream.readLine()) != null ) {
					if (!"".equals(line.trim())) {
						state = line.trim();
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			if(state != null && state.contains("IMAGE_UPGRADE")) {
				resetMode();
			}
			else {
				return;
			}
		
		}
	}
	
	public static String resetMode() {
		String output = "N";
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(new String[]{"checkandsetemmode.sh", "NORMAL"});
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = outputStream.readLine()) != null ) {
				if (!"".equals(line.trim())) {
					output = line.trim();
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return output;
	}
	

}
