package com.communicator.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.communicator.util.CommonUtils;
import com.communicator.util.CommunicatorConstant;

@Service("tunnelingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class TunnelingManager {
	
	@Resource 
	ServerInfoManager serverInfoManager ;
	@Resource
	SecureCloudConnectionTemplate secureCloudConnectionTemplate ;

	static final Logger logger = Logger.getLogger(TunnelingManager.class
			.getName());
	private String localPort=null;
	private String keyPath = null;
	
	@PostConstruct
	public void init()
	{
		localPort = CommonUtils.getPropertyWithName("localTunnelingPort", CommunicatorConstant.configFilePath);
		keyPath = CommonUtils.getPropertyWithName("keyPath", CommunicatorConstant.configFilePath);
		
	}

	public void StartTunnel(String port)
	{
		localPort = CommonUtils.getPropertyWithName("localTunnelingPort", CommunicatorConstant.configFilePath);
		keyPath = CommonUtils.getPropertyWithName("keyPath", CommunicatorConstant.configFilePath);
		if(!monitorTunnel(serverInfoManager.getHost()))
		{
		try{ 
			if(!port.equalsIgnoreCase("0"))
		    {
				logger.info("---------------ESTABLISHING TUNNEL TO MASTER SERVER AT "+ Calendar.getInstance().getTime().toString() +"----------------");
				ArrayList<String> cmdList = new ArrayList<String>();
				String cmd = "ssh -o \"StrictHostKeyChecking no\" -i "+ keyPath+" -N -f -q -R "+port+":localhost:"+ localPort + " enlighted@"+serverInfoManager.getHost();
				logger.info("starting tunneling command :- " + cmd);
				cmdList.add(cmd);
				CommonUtils.executeListOfShellCmd(cmdList) ;
				if(!toggleEmBrowsability("true"))
				{
					// clean up on failure
					logger.info("Stopping tunnel as a clean up process due to failure on server side.....") ;
					StopTunnel(port);
				}
		    }else
		    {
		    	logger.info("Port cannot be 0 (default). Tunnel cannot be established.");
		    }
		}catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		}
	}


	public void StopTunnel(String port) {
	
		localPort = CommonUtils.getPropertyWithName("localTunnelingPort", CommunicatorConstant.configFilePath);
		keyPath = CommonUtils.getPropertyWithName("keyPath", CommunicatorConstant.configFilePath);
		if(monitorTunnel(serverInfoManager.getHost()))
		{
			logger.info("---------------CLOSING TUNNEL TO MASTER SERVER AT "+ Calendar.getInstance().getTime().toString()+"----------------");
			try{
				ArrayList<String> cmdList = new ArrayList<String>();
				String cmd = "\":localhost:"+  localPort+ " enlighted@"+serverInfoManager.getHost()+"\"";
				String killCmd =  "pkill -f "+ cmd ;
				logger.info("Ending tunneling command for :- " + cmd);
				cmdList.add(killCmd);
				CommonUtils.executeListOfShellCmd(cmdList) ;
				if(!toggleEmBrowsability("false"))
				{
					logger.info("Tunnel is closed but may be browsability of EM is still on due to error on server side. Please contact administrator.") ;
				}
			}catch(Exception e)
			{
				logger.error(e.getMessage());
			}
		} 
	}
	
	private Boolean toggleEmBrowsability(String value)
	{	Boolean flag = false ; 
		CloudHttpResponse cloudResponse = secureCloudConnectionTemplate.executePost(CommunicatorConstant.updateEmBrowsability+serverInfoManager.getMacAddress(), value, 
				serverInfoManager.getHost(), MediaType.TEXT_PLAIN);
		if(cloudResponse.getStatus()==200)
		{
			logger.info("EM browsability updated to :-" + value) ;
			flag = true ;
		}else 
		{
			logger.info("Failed to update EM Browsability. Reason :-" + cloudResponse.getResponse() + " and status is :- "+ cloudResponse.getStatus() );
			flag = false ;
		}
		return flag ;
	}
	
	private boolean monitorTunnel(String cloudServerName)
	{
		Boolean flag = false ;
		 Runtime run = Runtime.getRuntime();
			try {
				Process pr = run.exec("sh /opt/enLighted/communicator/tunnelMonitoringScript.sh " + cloudServerName);
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";
				while ((line=buf.readLine())!=null) {
					if(line.contains("SSH tunnel is up"))
					{
						flag=true;
					}
				}
			    pr.waitFor();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}catch (Exception e)
			{
				logger.error(e.getMessage());
			}
		return flag ;
		
	}
	
}
