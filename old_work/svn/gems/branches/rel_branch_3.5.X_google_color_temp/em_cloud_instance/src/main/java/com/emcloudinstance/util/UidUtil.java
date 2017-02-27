package com.emcloudinstance.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;


import org.springframework.stereotype.Component;


@Component("uidUtil")
public class UidUtil {
	
	public String macId = "";
	
	public String uid;
	
	@PostConstruct
	public void init(){
		
		try {
				Process pr = null;
				String[] cmdArr = { "/bin/bash", "/var/lib/tomcat6/webapps/em_cloud_instance/adminscripts/getMac.sh" };
				BufferedReader br = null;
				pr = Runtime.getRuntime().exec(cmdArr);
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				
				while (true) {
					macId = br.readLine().trim();
					if (macId != null && !"".equals(macId)) {
						break;
					}
				}
			
			} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
