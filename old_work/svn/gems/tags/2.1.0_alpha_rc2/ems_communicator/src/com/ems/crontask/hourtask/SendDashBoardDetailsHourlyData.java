package com.ems.crontask.hourtask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.ems.Communicator;
import com.ems.Globals;

/**
 * @author SAMEER SURJIKAR
 * 
 *         This class will send Aggregated Dash board detail data of past one
 *         hour to Master gems.
 * 
 */
public class SendDashBoardDetailsHourlyData implements HourTask {
	private Properties properties = new Properties();
	Logger log = Logger.getLogger("Send Dash Borad Details");

	@Override
	public void run() {
		readPropertyFile();
		StringBuilder data = getData();
		sendData(data.toString());

	}

	public StringBuilder getData() {
		Communicator getdata = new Communicator();
		StringBuilder sb = getdata.webServiceGetRequest(properties
				.getProperty("GetHourlyDashBoardDetailsData"));
		return sb;
	}

	public String sendData(String data) {
		Communicator senddata = new Communicator();
		return senddata.webServicePostRequest(data,
				properties.getProperty("SendHourlyDashBoardDetailsData"));

	}

	public void readPropertyFile() {
		try {
		//	 InputStream is = this.getClass()
				       // .getResourceAsStream("/com/ems/resources/urls.properties");
			 InputStream is = new FileInputStream(Globals.propFile);	
			properties.load(is);
		} catch (IOException e) {
			log.info("Error while reading properties file , Please check the file path");
		}

	}

}
