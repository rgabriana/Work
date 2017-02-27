package com.ems.crontask.hourtask;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.ems.Globals;
import com.ems.util.PropertyFileManager;
import com.ems.util.Utils;

/**
 * @author SAMEER SURJIKAR This will be the starting point for the hourly task.
 *         It will take all the task and communicate it to the given server on
 *         hourly basis. This class is called from the crontab on hourly basis
 * 
 */
public class RunHourTasks {

	private static final int NTHREDS = 10;

	/**
	 * @param args
	 *            :- name of the tasks you want to run in comma separated value
	 */
	public static void main(String[] args) {
		Globals.propFile = args[0];
		Logger log = Globals.log;
		ArrayList<HourTask> tasks = new ArrayList<HourTask>();
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		
		// This is the first step which sets up the urls used through out the code in the property file. 
		PropertyFileManager propManager = new PropertyFileManager() ;
		propManager.setUrls() ;
		// Generate and store apiKey in property file.
		Properties prop = new Properties();

		try {

			InputStream is = new FileInputStream(Globals.propFile);
			prop.load(is);
			String macGenerationIp = prop.getProperty("macGenerationIp");
			prop.put("ApiKey", Utils.getMacAddress(macGenerationIp));	
			prop.store(new FileOutputStream(Globals.propFile), null);
		} catch (IOException ex) {
			log.info("Error while reading/writing property file "
					+ Globals.propFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Error with reading/writing  property file "
					+ Globals.propFile);
		}
		for (int i = 1; i < args.length; i++) {
			HourTask instance = new GetInstance().getInstance(args[i]);
			if (instance == null) {
				log.info(args[i] + " Is not an hourly task");
				continue;
			} else {
				tasks.add(instance);
			}
		}
		if (!tasks.isEmpty()) {
			Iterator<HourTask> iterator = tasks.iterator();

			while (iterator.hasNext()) {
				HourTask task = (HourTask) iterator.next();

				executor.execute(task);

			}
			// This will make the executor accept no new threads
			// and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			while (!executor.isTerminated()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					log.info("Error in thread management");
				}
			}
		} else
			log.info("No task to run. Pass the name of the task you want run as a command line arguments");

	}

}
