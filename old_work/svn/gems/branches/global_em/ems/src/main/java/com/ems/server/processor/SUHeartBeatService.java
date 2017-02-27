/**
 * 
 */
package com.ems.server.processor;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.Gateway;
import com.ems.server.EmsShutdownObserver;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.device.GatewayImpl;
import com.ems.server.util.HeartbeatMapQueue;
import com.ems.server.util.ServerUtil;
import com.ems.service.GatewayManager;

/**
 * @author yogesh
 * 
 */
public class SUHeartBeatService extends Thread implements EmsShutdownObserver {
	private static SUHeartBeatService m_instance = null;
	private static Logger fixtureLogger = Logger.getLogger("FixtureLogger");
	private boolean isProcessing = false;
	private HeartbeatMapQueue hbQueue = null;
	private Long iCount = 0L;
	private GatewayManager gwMgr = null;
	
	private Gateway gw = null;

	/**
	 * @return the gw
	 */
	public Gateway getGw() {
		return gw;
	}

	/**
	 * @param gw the gw to set
	 */
	public void setGw(Gateway gw) {
		this.gw = gw;
	}

	private SUHeartBeatService() {

	}

	public static SUHeartBeatService getInstance() {
		if (m_instance == null) {
			m_instance = new SUHeartBeatService();
		}
		return m_instance;
	}

	@Override
	public void run() {
		if (gwMgr == null) {
			gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
		}
		gw = gwMgr.getUEMGateway();
		while (gw == null) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gw = gwMgr.getUEMGateway();
		}
		if (gw != null) {
			if (isProcessing == false) {
				isProcessing = true;
				fixtureLogger.info("Starting HeartBeat Service...");
				hbQueue = DeviceServiceImpl.getInstance().getSUHeartBeatQueue();
				byte[] data = null;

				while (isProcessing) {
					try {
						data = hbQueue.getRandom();
						if( data == null) {
							Thread.sleep(2000);
							continue;
						}
						String sName = ServerUtil.getSnapAddr(data[8], data[9], data[10]);

						if (data != null) {
							fixtureLogger
									.info("Forwarding Heartbeat to UEM engine.");
							GatewayImpl.getInstance().forwardPkt(gw, data);
						}

						iCount = iCount + 1;
						if ((iCount % 1000) == 0) {
							 fixtureLogger.info("SUHB Processed: " + iCount + " " + sName);
							Thread.sleep(5);
						}
						if (iCount >= Long.MAX_VALUE)
							iCount = 0L;
					} catch (InterruptedException e) {
						fixtureLogger
								.error("Error fetching pkt to su heart beat queue! "
										+ iCount);
					} catch (Exception e) {
						fixtureLogger.error("Error sending su heart beat! "
								+ iCount);
					} finally {
						data = null;
					}
				}
			}
		}
	}

	@Override
	public void cleanUp() {
		isProcessing = false;
	}

}
