package com.communicator;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.types.DatabaseState;
import com.communicator.dao.SystemConfigDao;
import com.communicator.manager.CallHomeManager;
import com.communicator.manager.MigrationManager;
import com.communicator.manager.ReplicaServerInfoManager;
import com.communicator.manager.ServerInfoManager;
import com.communicator.util.InitializeSecureConnection;
import com.communicator.util.SchedulerManager;
import com.communicator.util.SpringAppContext;

public class Communicator {

	private static long sleepTime = 300 * 1000;
	
	private static Boolean isCleanUpDoneWhenCloudDisabled = false ;
	public static final Logger logger = Logger.getLogger(Communicator.class.getName());
	private static ApplicationContext springContext = null ;
	
	   
	public static void main(String[] args) {
	
			// Some of the URL require certificate authentication, on which server issues a renegotiation request
			// Clients need to honor this renegotiation with the server.
			System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
			Random rndGenerator = new Random();
			try {
				Thread.sleep(rndGenerator.nextInt(new Integer((int) sleepTime)));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	            
			logger.info("--------------------COMMUNICATOR IS STARTING AT "+ new Date() + "----------------------------------");
			//Initialize the logging system
			BasicConfigurator.configure();
			
			//Initialize the Spring factory
			SpringAppContext.init();
			springContext = SpringAppContext.getContext();
			
			SecureCloudConnectionTemplate secureCloudConnectionTemplate = (SecureCloudConnectionTemplate)springContext.getBean("secureCloudConnectionTemplate");
			InitializeSecureConnection.init(secureCloudConnectionTemplate);
			
			//Initialize quartz scheduler
			SchedulerManager.getInstance();
			
			ServerInfoManager serverInfoManager = (ServerInfoManager)springContext.getBean("serverInfoManager");
	        CallHomeManager callHomeManager = (CallHomeManager)springContext.getBean("callHomeManager");
	        MigrationManager migrationManager = (MigrationManager)springContext.getBean("migrationManager");
	        ReplicaServerInfoManager replicaServerInfoManager = (ReplicaServerInfoManager)springContext.getBean("replicaServerInfoManager");
	        SystemConfigDao systemConfigDao = (SystemConfigDao)springContext.getBean("systemConfigDao");
	        while (true) {
				try {
					
					if(systemConfigDao.isCloudEnabled().equalsIgnoreCase("0"))
					{
						logger.info("EM is not cloud enabled. Contact Administrator");
						if(!isCleanUpDoneWhenCloudDisabled)
						{
							systemConfigDao.doBeforeMigrationcleanUp() ;
							isCleanUpDoneWhenCloudDisabled = true ;
						}
						
					}else {
						// If cloud communication is toggled
						isCleanUpDoneWhenCloudDisabled = false;
						serverInfoManager.init();
						String status = serverInfoManager.getCloudSyncType();
							if("1".equals(status)) {
								callHomeManager.sendDataCallHome();
							}
							else if ("2".equals(status)) {
								if(!InitializeSecureConnection.getIsSppaCertificateInitialized())
								{
									InitializeSecureConnection.init(secureCloudConnectionTemplate);
									
								}
								callHomeManager.sendDataCallHome();
								if(replicaServerInfoManager.getReplicaConnectivity())
								{
									migrationManager.runMigrationOrSync() ;
								}else
								{
									migrationManager.setReplicaMigrationFlagDirectECloud( DatabaseState.REPLICA_UNREACHABLE.getName());
								}
							}	
						 else {
							serverInfoManager.init();
						}
					}
				} catch (SecurityException e) {
					logger.error( e.getMessage());
				} catch(Exception e)
				{
					logger.error(e.getMessage());
				}
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					logger.error(e.getMessage()) ;
				}
			}
		}  
        
	}

	