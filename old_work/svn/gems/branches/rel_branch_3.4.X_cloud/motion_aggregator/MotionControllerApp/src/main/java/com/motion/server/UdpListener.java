package com.motion.server;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.stereotype.Service;

import com.motion.dao.FixtureDao;
import com.motion.dao.FloorDao;
import com.motion.dao.SystemConfigDao;
import com.motion.utils.MotionAppConstants;
import com.motion.utils.SchedulerManager;

@Service("udpListener")
public class UdpListener {
	
	public static final Logger logger = Logger.getLogger(UdpListener.class
			.getName());
	private  Integer port ;
	private DatagramSocket datagramSocket = null;
	@Resource 
	SystemConfigDao systemConfigDao;
	@Resource 
	FixtureDao fixtureDao;
	@Resource 
	FloorDao floorDao;
	private static Long processed = 0l ;
	private static Long incrementCount = 1l ;
	// purpose of blocking queue was to give consumer/producer functionality to queue.
	private static BlockingQueue<byte[]> workQueue = new LinkedBlockingQueue<byte[]>();
	Scheduler sched = null ;
	public UdpListener() {
		
	}
	public  void startListening()
	{	
		floorDao.loadFloorFromConfig(MotionAppConstants.FLOOR_DETAILS) ;
		fixtureDao.loadFixturesFromConfig(MotionAppConstants.FIXTURE_DETAILS);
		this.port = systemConfigDao.getUdpPort();
		try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			logger.error(e.getMessage());
		}

		byte[] buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		logger.info("Listening on port " + port) ;
		//Job which do transformation of motion packets
		createAndFireMotionPacketTransformJob(MotionAppConstants.MOTION_PACKET_TRANSFORMER_JOB_NAME) ;
		createAndFireDisplayDataSendJob(MotionAppConstants.DISPLAY_DATA_SEND_JOB_NAME) ;
		logger.info("Udp Listener Started....");
		while (true) {	
			try {
				
				if(!datagramSocket.isClosed() && datagramSocket.isBound())
				{
					datagramSocket.receive(packet);
					buffer = packet.getData();	
					workQueue.put(buffer) ;
					processed = processed + incrementCount ;
					logger.debug("Motion packet Receieved till now:-" + processed.toString()  );
					logger.debug("Packet Receieved is :- " + new String(buffer));
							
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}catch (IOException e) {
				logger.error(e.getMessage());
			}catch (Exception e){
				logger.error(e.getMessage());
			}
			
		}
		 
	}
	@PreDestroy
	 public void cleanUp() {
		  try {
			  logger.info("Cleaning Resources for UdpListener....");
			  logger.info("Stopping Udp Listner....");
			  if(!datagramSocket.isClosed())
				  datagramSocket.close() ;
			  sched.deleteJob(new JobKey(MotionAppConstants.MOTION_PACKET_TRANSFORMER_JOB_NAME));
			  sched.deleteJob(new JobKey(MotionAppConstants.DISPLAY_DATA_SEND_JOB_NAME));
          } catch (Exception e) {
        	  logger.error(e.getMessage());
          }

       }
	public BlockingQueue<byte[]> getWorkQueue() {
		return workQueue;
	}
	
	private void createAndFireMotionPacketTransformJob(String jobName )
	{
		sched = SchedulerManager.getInstance().getScheduler();
		try {
			if (!sched.checkExists(new JobKey(jobName, sched.getSchedulerName()))) {
				createNewMotionPacketTransformJob(jobName);	
			}
		} catch (SchedulerException e) {
			logger.error(e.getMessage());
		}
		
	}
	private void createNewMotionPacketTransformJob(String jobName) throws SchedulerException {
		JobDetail BulkJob = newJob(PacketTransformer.class)
								.withIdentity(jobName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName())
				.build();
		SimpleTrigger BulkTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(
						jobName+"_trigger",
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).startNow().build();

		SchedulerManager.getInstance().getScheduler()
				.scheduleJob(BulkJob, BulkTrigger);
	}
	private void createAndFireDisplayDataSendJob(String jobName )
	{
		sched = SchedulerManager.getInstance().getScheduler();
		try {
			if (!sched.checkExists(new JobKey(jobName, sched.getSchedulerName()))) {
				createNewDisplayDataSendJob(jobName);	
			}
		} catch (SchedulerException e) {
			logger.error(e.getMessage());
		}
		
	}
	private void createNewDisplayDataSendJob(String jobName) throws SchedulerException {
		JobDetail BulkJob = newJob(DisplayDataSendJob.class)
								.withIdentity(jobName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName())
				.build();
		SimpleTrigger BulkTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(
						jobName+"_trigger",
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).startNow().build();

		SchedulerManager.getInstance().getScheduler()
				.scheduleJob(BulkJob, BulkTrigger);
	}
}
