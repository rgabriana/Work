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
import java.util.logging.Logger;

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
	Logger log = Logger.getLogger("Send Dash Borad Details");
	String masterGemTimeStamp = null;
	String miniGemTimeStamp = null;
	String status ;

	@Override
	public void run() {
		readPropertyFile();
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

	/**
	 * Starts the sync up process.
	 */
	public void syncUp() {
		Globals.log.fine("Started Sync up with master gems");

		// Login and get the response for getting TimeStamp which will be used
		// for sync up process
		setserverTimeStampsForSyncUp();
		if(status.equalsIgnoreCase("I"))
		{
			Globals.log.fine("Gems is not activated on the Master gems. Please do so");
			return ;
		}
		//System.out.println(masterGemTimeStamp);
		
		// Logic for deciding whether to do full syncup or just the difference
		// or just the hourly syncup
		if (masterGemTimeStamp.equalsIgnoreCase("-1")) {
			// perform a full syncup
			Globals.log.fine("Doing a full sync up with master gems.");
			intialSyncUp();
			
		} else {
			//find hour difference between current time and the time when ems last communicated with the Master gems.
			long difference = 0;
			 DateFormat formatter , simpleFormatter;
			 Calendar today = Calendar.getInstance();
			 Date tempDate = null ;
			 Calendar temp = Calendar.getInstance() ;
			formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");			
	        try {
				tempDate = formatter.parse(masterGemTimeStamp);
				temp.setTime(tempDate) ;
				//to get time in 24 hours for finding difference.
				temp.add(Calendar.HOUR_OF_DAY, 12);
			} catch (ParseException e) {
				Globals.log
				.fine("Parsing issue :-" + e.getMessage());
			}
	        miniGemTimeStamp =simpleFormatter.format(temp.getTime());
			masterGemTimeStamp = simpleFormatter.format(today.getTime());
			try {
				difference = hourDifference(simpleFormatter.parse(masterGemTimeStamp), simpleFormatter.parse(miniGemTimeStamp)) ;
				//System.out.println(difference);
				//System.out.println("masterGemTimeStamp " + masterGemTimeStamp);
				// System.out.println("miniGemTimeStamp " + miniGemTimeStamp);
				// System.out.println(today.getTimeZone() + " " + temp.getTimeZone());
			} catch (ParseException e) {
				Globals.log
				.fine("Parsing issue :-" + e.getMessage());
			}
			 if (difference == 1l) {
				// send hourly aggregate data
				Globals.log.fine("Doing a hourly syncup with master gems");
				//System.out.println("Doing a hourly syncup with master gems");
				doHourlySyncUp();
			} else if (difference > 1l) {
				// perform sync up for the specific difference
				Globals.log
						.fine("Gems or Master gems was down a sync up on hourly aggregated data ");
				doSyncUpForDifference();
			}
		}

		Globals.log.fine("End of Sync up.");
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

		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><apiKey>"
				+ apiKey
				+ "</apiKey><gemsIp>"
				+ ip
				+ "</gemsIp></loginRequest></body></request></root>";
		String loginResponse = "";
		try {
			loginResponse = syncUp.login(loginXML, loginUrl);
			//System.out.println(loginResponse);
			
			masterGemTimeStamp = syncUp.getTagValue(loginResponse,
					"enl:lastCommunicationTimeStamp");
			status = syncUp.getTagValue(loginResponse, "enl:gemsStatus") ;
		} catch (Exception e) {
			Globals.log.fine("Login error " + loginResponse);

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
		} catch (Exception e) {
			Globals.log.fine("Login error " + loginResponse);
		}

	}

	/**
	 * Does an Historic sync up for all the aggregated data. This happens when
	 * the gems first get connected.
	 */
	public void intialSyncUp() {
		try {
			//System.out.println("Started Initial sync up");
			 DateFormat formatter ;
	         formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			 Calendar temp = Calendar.getInstance();
			 Calendar minTemp = Calendar.getInstance();
	         Date tempDate = DateTimeUtils.parseString(miniGemTimeStamp, DateTimeUtils.SQL_FORMAT) ;
			 Date minTempDate = DateTimeUtils.parseString(miniGemTimeStamp, DateTimeUtils.SQL_FORMAT) ; 
			// Find difference between current date and date at with first ec record was detected by mini Gems
			 long difference =  dayDifference(formatter.format(temp.getTime()),formatter.format(tempDate.getTime()));
			 //System.out.println(difference);
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
			//System.out.println("Done Initial sync up");
		} catch (Exception ex) {
			Globals.log.fine("Error while doing Historic Sync up \n "
					+ ex.getMessage());
			//System.out.println("ERROR" + ex.getMessage());
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
			Globals.log.fine("Error while doing hourly sync up \n "
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
			// ----------------------------------------------------------------------------------------------------------------------------------------------------------------
			// Make calls to the url to get and post data
			//System.out.println(finalUrl);
			Communicator getdata = new Communicator();
			StringBuilder sb = getdata.webServiceGetRequest(finalUrl);
			//System.out.println(sb.toString());
			Communicator senddata = new Communicator();
			senddata.webServicePostRequest(sb.toString(),
					properties.getProperty("SendHourlyDashBoardDetailsData"));

		} catch (Exception ex) {
			Globals.log
					.fine("Error while doing  hourly sync up for difference \n "
							+ ex.getMessage());
			//System.out.println("Error " +ex.getMessage());
		}

	}

	/**
	 * @param mgTimeStamp
	 *            : - time stamp from server
	 * @param miniTimeStamp
	 *            :- time stamp from mini gems
	 * @return timestamp difference in hour
	 */
	private long hourDifference(Date from , Date to) {
		
	        Calendar fDate = Calendar.getInstance();
	        fDate.setTime(from);
	        Calendar tDate = Calendar.getInstance();
	        tDate.setTime(to);

	        long diff = fDate.getTimeInMillis() - tDate.getTimeInMillis();
	        long diffhours = diff / ( 60 * 60 * 1000);
		return diffhours;
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
			// InputStream is = this.getClass()
			// .getResourceAsStream("/com/ems/resources/urls.properties");
			InputStream is = new FileInputStream(Globals.propFile);
			properties.load(is);
		} catch (IOException e) {
			log.info("Error while reading properties file , Please check the file path");
		}

	}

	

}
