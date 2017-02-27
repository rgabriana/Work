package com.ems.crontask.hourtask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;

import com.ems.Communicator;
import com.ems.Globals;
import com.ems.util.DateTimeUtils;

/**
 * @author SAMEER SURJIKAR
 * 
 *         This class will send Aggregated Dash board detail data of past one
 *         hour to Master gems.
 * 
 */
public class SendDashBoardDetailsHourlyData implements HourTask {
	private Properties properties = new Properties();
	Logger log = Globals.log ;
	String masterGemTimeStamp = null;
	String miniGemTimeStamp = null;
	String status ;
	String masterServerLogoutUrl ="" ;
	String miniServerLogoutUrl = "" ;

	@Override
	public void run() {
		readPropertyFile();
		createLogoutUrls() ;
		syncUp();
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
	public void logout(String url , String sessionId)
	{
		Communicator c = new Communicator() ;
		c.logout(url, sessionId) ;
	}

	/**
	 * Starts the sync up process.
	 */
	public void syncUp() {
		Globals.log.info("Started Sync up with master gems");

		// Login and get the response for getting TimeStamp which will be used
		// for sync up process
		setserverTimeStampsForSyncUp();
		if(status.equalsIgnoreCase("I"))
		{
			Globals.log.info("Gems is not activated on the Master gems. Please do so");
			return ;
		}
		
		
		// Logic for deciding whether to do full syncup or just the difference
		// or just the hourly syncup
		if (masterGemTimeStamp.equalsIgnoreCase("-1")) {
			// perform a full syncup
			Globals.log.info("Doing a full sync up with master gems. Communication is never done before. This will take time");
			intialSyncUp();
			
		} else {
			
			 DateFormat formatter , simpleFormatter;
			 Calendar today = Calendar.getInstance();
			 Date tempDate = null ;
			 Calendar temp = Calendar.getInstance() ;
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");			
	        try {
				tempDate = formatter.parse(masterGemTimeStamp);
				temp.setTime(tempDate) ;
			
			} catch (ParseException e) {
				Globals.log
				.info("Parsing issue :-" + e.getMessage());
			}
	        miniGemTimeStamp =simpleFormatter.format(temp.getTime());
			masterGemTimeStamp = simpleFormatter.format(today.getTime());
			
				Globals.log
						.info("Sync up on hourly aggregated data for the difference has been started. ");
				doSyncUpForDifference();
			
		}

		Globals.log.info("End of Sync up.");
	}

	/**
	 * This funtion sets the timestamp for mini and master gems. These timestamp
	 * are what make it possible for us to sync with the server. Make sure you
	 * can this function before doing any sync up activity.
	 */
	public void setserverTimeStampsForSyncUp() {
		Communicator syncUp = new Communicator();
		Properties properties = new Properties();
		try {

			InputStream is = new FileInputStream(Globals.propFile);

			properties.load(is);
		} catch (IOException e) {
		}

		// Getting server time stamp....
		String loginUrl = properties.getProperty("ServerLoginUrl");
		String apiKey = properties.getProperty("ApiKey");
		String ip = properties.getProperty("GemsIp");
		String timeZone = properties.getProperty("TimeZone");

		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><apiKey>"
				+ apiKey
				+ "</apiKey><gemsIp>"
				+ ip
				+ "</gemsIp>" +
				"<timeZone>"
				+ timeZone
				+ "</timeZone></loginRequest></body></request></root>";
		String loginResponse = "";
		try {
			loginResponse = syncUp.login(loginXML, loginUrl);
			
			
			masterGemTimeStamp = syncUp.getTagValue(loginResponse,
					"enl:lastCommunicationTimeStamp");
			if(!masterGemTimeStamp.equalsIgnoreCase("-1"))
			{  
				 //Converting master gem timestamp from GMT to local Server timezone.
				Calendar cal = Calendar.getInstance() ;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
				cal.setTime(sdf.parse(masterGemTimeStamp)) ;
				masterGemTimeStamp = sdf.format(DateTimeUtils.convTimeZoneFromGMTtoLocal(cal).getTime());
				
			}
			status = syncUp.getTagValue(loginResponse, "enl:gemsStatus") ;
			String masterSessionId = syncUp.getSessionID(loginResponse) ;
			logout(masterServerLogoutUrl, masterSessionId) ;
		} catch (Exception e) {
			Globals.log.info("Login error " + loginResponse);

		}
		// Getting Mini Gems time stamp....
		loginUrl = properties.getProperty("GemsLoginUrl");
		loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		
		String miniGemLoginResponse = syncUp.login(loginXML, loginUrl);
		
		try {
			
			miniGemTimeStamp = syncUp.getTagValue(miniGemLoginResponse,
					"enl:lastCommunicationTimeStamp");
			//System.out.println(miniGemTimeStamp);
			String miniSessionId =  syncUp.getSessionID(miniGemLoginResponse) ;
			logout(miniServerLogoutUrl, miniSessionId) ;
		} catch (Exception e) {
			Globals.log.info("Login error " + loginResponse);
		}

	}

	/**
	 * Does an Historic sync up for all the aggregated data. This happens when
	 * the gems first get connected.
	 */
	public void intialSyncUp() {
		try {
			
			 DateFormat formatter ;
	         formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			 Calendar temp = Calendar.getInstance();
			 Calendar minTemp = Calendar.getInstance();
	         Date tempDate = DateTimeUtils.parseString(miniGemTimeStamp, DateTimeUtils.SQL_FORMAT) ;
			 Date minTempDate = DateTimeUtils.parseString(miniGemTimeStamp, DateTimeUtils.SQL_FORMAT) ; 
			// Find difference between current date and date at with first ec record was detected by mini Gems
			 long difference =  dayDifference(formatter.format(temp.getTime()),formatter.format(tempDate.getTime()));
			
			 temp.setTime(tempDate);
			 minTemp.setTime(minTempDate) ;
			for (int i = 0; i <= difference; i++) {
				// Change master gems time stamp, to send data from the first
				// date of the ec table to 1 month forward and iterate till
				// current date.
				temp.add(Calendar.DATE, i);
				minTemp.add(Calendar.DATE, (i+1));
				// Minigems timestamp should be less than master gems as mini gems will be trying to fill data till current time.
				miniGemTimeStamp = formatter.format(temp.getTime());
				masterGemTimeStamp = formatter.format(minTemp.getTime());
				doSyncUpForDifference();
				//Need to reset the date to earlier month for proper working of the for loop after being used in doSyncUpForDifference().
				temp.add(Calendar.DATE, -i);
				minTemp.add(Calendar.DATE, -(i+1));
			}
			
		} catch (Exception ex) {
			Globals.log.info("Error while doing Historic Sync up \n "
					+ ex.getMessage());
			
		}
	}

	/**
	 * Does an hourly sync up with master gems.
	 */
	public void doHourlySyncUp() {
		try {
			StringBuilder data = getData();
			sendData(data.toString());
		} catch (Exception ex) {
			Globals.log.info("Error while doing hourly sync up \n "
					+ ex.getMessage());
		}

	}

	/**
	 * Does an hourly sync up for time there was communication barrier between
	 * Master server and mini gem
	 */
	public void doSyncUpForDifference() {
		try {
			String url = properties
					.getProperty("GetHourlyDashBoardDetailsData");
			 DateFormat formatter ;
			formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date to = formatter.parse(masterGemTimeStamp) ;	
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			Date from = formatter.parse(miniGemTimeStamp) ;
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			String finalUrl = url
					+ "/"
					+ new SimpleDateFormat("yyyyMMddHHmmss").format(cFrom
							.getTime())
					+ "/"
					+ new SimpleDateFormat("yyyyMMddHHmmss").format(cTo
							.getTime());
			Globals.log.info(finalUrl);
			// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
			// Make calls to the url to get and post data
			Communicator getdata = new Communicator();
			StringBuilder sb = getdata.webServiceGetRequest(finalUrl);
			//logout
			logout(miniServerLogoutUrl, getdata.getCurrentSessionId() ) ;
			Communicator senddata = new Communicator();
			senddata.webServicePostRequest(sb.toString(),
					properties.getProperty("SendHourlyDashBoardDetailsData"));
			//logout
			logout(masterServerLogoutUrl , senddata.getCurrentSessionId()) ;

		} catch (Exception ex) {
			Globals.log
					.info("Error while doing  hourly sync up for difference \n "
							+ ex.getMessage());
			
		}

	}

	

	/*
	 * @param mgTimeStamp : - time stamp from server
	 * 
	 * @param miniTimeStamp :- time stamp from mini gems
	 * 
	 * @return difference in day
	 */
	private long dayDifference(String mgTimeStamp, String miniTimeStamp) {
		long difference = 0;
		DateFormat formatter ;
		formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		Date from = null;
		try {
			from = formatter.parse(miniTimeStamp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date to=null;
		try {
			to = formatter.parse(mgTimeStamp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		difference = DateTimeUtils.dateDiffInDays(to,from);
		return difference;
	}
	
	public void readPropertyFile() {
		try {
			
			InputStream is = new FileInputStream(Globals.propFile);
			properties.load(is);
		} catch (IOException e) {
			Globals.log.info("Error while reading properties file , Please check the file path");
		}
		

	}
	public void createLogoutUrls()
	{
		masterServerLogoutUrl = properties.getProperty("ServerLoginUrl").substring(0, properties.getProperty("ServerLoginUrl").lastIndexOf("/")) +"/j_spring_security_check"  ;
		miniServerLogoutUrl = properties.getProperty("GemsLoginUrl").substring(0, properties.getProperty("GemsLoginUrl").lastIndexOf("/")) +"/j_spring_security_check"  ;
	}



}
