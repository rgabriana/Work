package com.ems.server.processor;

/**
 * @author 
 *
 */
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

import java.lang.management.ThreadMXBean;

import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.ems.action.SpringContext;
import com.ems.model.EmStats;
import com.ems.service.EmStatsManager;
//
//import org.hyperic.sigar.CpuPerc;
//import org.hyperic.sigar.Sigar;
//import org.hyperic.sigar.SigarProxy;
//import org.hyperic.sigar.SigarProxyCache;

public class EmHealthStatsThread extends Thread {
  
  private static EmHealthStatsThread instance = null;
  
  private long emStatsInterval = 5 * 60 * 1000;  
  private Timer emHealthStatsTimer = new Timer("EM Health StatsTimer", true);
  
  private EmStatsManager emStatsMgr = null;
  
  private ThreadMXBean threadBean = null;
  private GarbageCollectorMXBean gcBean = null;
  private MemoryMXBean memoryBean = null;  
  private OperatingSystemMXBean osBean = null;
  
  private long uptime = 0;
  
  private EmHealthStatsThread() {

    emStatsMgr = (EmStatsManager)SpringContext.getBean("emStatsManager");
    EMHealthStatsTask emTask = new EMHealthStatsTask();
    emHealthStatsTimer.scheduleAtFixedRate(emTask, emStatsInterval, emStatsInterval);
    
    threadBean = ManagementFactory.getThreadMXBean();
    gcBean = ManagementFactory.getGarbageCollectorMXBeans().get(0);
    memoryBean = ManagementFactory.getMemoryMXBean();
    osBean = ManagementFactory.getOperatingSystemMXBean();
     
  } //end of constructor
  
  public static EmHealthStatsThread getInstance() {
    
    if(instance == null) {
      synchronized(EmHealthStatsThread.class) {
	if(instance == null) {
	  instance = new EmHealthStatsThread();
	}
      }
    }
    return instance;
    
  } //end of method getInstance

  public void stopThread() {

    emHealthStatsTimer.cancel();
    
  } //end of method stopThread
  
  public class EMHealthStatsTask extends TimerTask{
    
    public void run() {

      dumpEmStats();
	
    } //end of method run
 
  } //end of class GwPingTask
   
  private float getCPUPercentage() {
    
    Process process = null;
    try {
      process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "ps wauxf | grep java | grep -v grep" });
      Scanner scanner = new Scanner(process.getInputStream(), "UTF-8");
      scanner.next(); //user
      scanner.nextInt(); //pid;
      float cpuPercentage = Float.parseFloat(scanner.next().replace(",", "."));
      return cpuPercentage;
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      if(process != null) {
	try {
	process.getInputStream().close();
	process.getOutputStream().close();
	process.getErrorStream().close();
	process.destroy();
	}
	catch(Exception e) {	  
	}
	process = null;
      }
    }
    return -1;
    
  } //end of method getCPUPercentage

/*  
  private static String getSigarCpuPercentage() {
    
    Sigar sigarImpl = new Sigar();    
    SigarProxy sigar = SigarProxyCache.newInstance(sigarImpl, 5);
    try {   
      return CpuPerc.format(sigar.getCpuPerc().getUser());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return "N/A";
    
  } //end of method getSigarCpuPercentage
  */
  
  private void dumpEmStats() {

    EmStats stats = new EmStats();
    try {      
      int noOfThreads = threadBean.getThreadCount();
      stats.setActiveThreadCount(noOfThreads);
           
      long noOfGcs = gcBean.getCollectionCount();
      stats.setGcCount(noOfGcs);
      long gcTime = gcBean.getCollectionTime();
      stats.setGcTime(gcTime);
            
      double heapUsed = (double)(memoryBean.getHeapMemoryUsage().getUsed()) / 1024 / 1024;
      stats.setHeapUsed(heapUsed);
      double nonHeapUsed = (double)(memoryBean.getNonHeapMemoryUsage().getUsed()) / 1024 / 1204;
      stats.setNonHeapUsed(nonHeapUsed);
            
      double sysLoad = osBean.getSystemLoadAverage();
      stats.setSysLoad(sysLoad);
      
      float cpuPer = getCPUPercentage();
      stats.setCpuPercentage(cpuPer);
      stats.setCaptureAt(new Date());
      
      //System.out.println("cpu - " + cpuPer); // + ", sigar-" + getSigarCpuPercentage());
      
      StringBuffer sb = new StringBuffer();
      sb.append("emStats(");
      sb.append(new Date());
      sb.append(")-");
      sb.append(noOfThreads);
      sb.append(",");
      sb.append(noOfGcs);
      sb.append(",");
      sb.append(gcTime);
      sb.append(",");
      sb.append(heapUsed);
      sb.append(" MB,");
      sb.append(nonHeapUsed);
      sb.append(" MB,");
      sb.append(sysLoad);
      sb.append(",");
      sb.append(cpuPer);
      sb.append("%");
      //System.out.println(sb.toString());
      
      emStatsMgr.save(stats);
           
    } catch (Exception ex) {
      //Let's catch the exception here so that our next batch works fine
      ex.printStackTrace();
    }
    
  } //end of method dumpEmStats

  public static void main(String args[]) {
    
    //System.out.println(getSigarCpuPercentage());
  }
} //end of class EmHealthStatsThread