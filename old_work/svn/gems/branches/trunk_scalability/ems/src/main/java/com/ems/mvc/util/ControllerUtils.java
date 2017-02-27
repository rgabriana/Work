package com.ems.mvc.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Synchronize;

import com.ems.action.SpringContext;
import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.service.FixtureManager;
import com.ems.utils.ArgumentUtils;

public class ControllerUtils {
	
	 
	 private static Long VALIDATION_INACTIVITY_TIME = 15 * 60 * 1000l; //15 minutes it is in sec
	 private  static Boolean validGroupSync = true ;
	 private static Boolean isTimerOn = false ;
	 private static Long LastActivityTime = null ;
	 private static Long activatedTime = null ;
	 static final Logger logger = Logger.getLogger("ControllerUtilsLogger");
	 public static String getTheLicenseFileName(String extension , String folderPath) throws ArrayIndexOutOfBoundsException
		{
		 	String fileName =null;
			GenericExtFilter filter = new GenericExtFilter(extension);
			File dir = new File(folderPath);
			String[] list = dir.list(filter);
			if(!(list.length==0))
			{
				fileName = list[0];
			}else
			{
				fileName = null ;
			}
			return fileName ;
		 	
		}
	 public static class GenericExtFilter implements FilenameFilter {
		 
			private String ext;
	 
			public GenericExtFilter(String ext) {
				this.ext = ext;
			}
	 
			public boolean accept(File dir, String name) {
				return (name.endsWith(ext));
			}
	 	}
	 public static void startTimerForFixtureGroupSyncFlag() 
	 {	
		 if(!isTimerOn)
		 {
			 new FixtureGroupSyncWatcher(5  * 60l ) ;
			 isTimerOn =true ;
		 }
	 }
	 public static  void timerFlagUpdate(Boolean GroupSyncFlag)
	 {
		 validGroupSync = GroupSyncFlag ;
		 activatedTime = LastActivityTime = System.currentTimeMillis() ;
	 }
	 
	 public static class FixtureGroupSyncWatcher {
		 
		    private static Timer timer = null ;
		   
		    
		    public FixtureGroupSyncWatcher(Long seconds) {		    	
		    		timer = new Timer();
		    		timer.schedule(new RemindTask(), 0,seconds*1000);
			}

		    class RemindTask extends TimerTask {
		        public void run() {
		        	
		        	if(!validGroupSync)
		        	{
		        		timerCleanUp() ; 
		        		
		        		
		        	}
		        	else 
		        	{
		        		if(LastActivityTime==null)
		            	{
		            		LastActivityTime = System.currentTimeMillis();
		            		activatedTime =  System.currentTimeMillis();
		            	}
		        		if(((System.currentTimeMillis()) - LastActivityTime.longValue()) < VALIDATION_INACTIVITY_TIME.longValue())
		        		{
		        			logger.debug("Last Activity Time :- " + activatedTime);
		        		}
		        		else
		        		{
		        			timerCleanUp() ;
		        			logger.info("Timer sensed user ideal for more than 15 min resetting all the fixture group sync flag to false ");
		        			
		        		}
		        		
		        	}
		        			           
		        }
		        private void timerCleanUp()
		        {
		        	isTimerOn = false ;
        			LastActivityTime= null ;
        			restFlags() ;
        			timer.cancel();
        			
		        }
		        public void restFlags()
		        {
		        	FixtureManager fixtureManager =(FixtureManager)SpringContext.getBean("fixtureManager");
		        	ArrayList<Long> fixtureIdList = (ArrayList<Long>) fixtureManager.loadFixturesIdWithGroupSynchFlagTrue();		        	
		        	if(!ArgumentUtils.isNullOrEmpty(fixtureIdList) )
		        	{
		        		for(Long id : fixtureIdList)
		        		{
		        		    logger.debug("Fixture with pending Flag true" + id);
		        			fixtureManager.changeGroupsSyncPending(fixtureManager.getFixtureById(id), false) ;
		        			FixtureCache.getInstance().invalidateDeviceCache(id);
		        		}
		        	}
		        }
		    }
		}
	 
	 
}
