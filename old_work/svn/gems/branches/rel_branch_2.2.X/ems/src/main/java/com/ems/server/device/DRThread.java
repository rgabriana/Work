package com.ems.server.device;

import com.ems.model.DRTarget;
import com.ems.server.util.ServerUtil;
import com.ems.service.DRTargetManager;

/**
 * The Class DRThread, to perform DR execution present in the <code>DRTargetManager</code> class
 * @author Shiv Mohan
 */
public class DRThread extends Thread {

	private DRTargetManager drTargetManager;
	private DRTarget drTarget;
	private static DRThread instance;
	private int interval = 0;

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * Private constructor
	 */
	private DRThread() {
	}

	/**
	 * Gets the single instance of DRThread
	 *
	 * @return single instance of DRThread
	 */
	public synchronized static DRThread getInstance() {
		if (instance == null || !instance.isAlive()) {
			instance = new DRThread();
		}
		return instance;
	}
	
	/**
	 * 
	 * @return if instance exists
	 */
	public static boolean instanceExists() {
		if (instance == null || !instance.isAlive()) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the dr target manager
	 *
	 * @param drTargetManager, the DR target manager to set
	 */
	public void setDrTargetManager(DRTargetManager drTargetManager) {
		this.drTargetManager = drTargetManager;
	}

	/**
	 * Sets the dr target
	 *
	 * @param drTarget, the drTarget to set
	 */
	public void setDrTarget(DRTarget drTarget) {
		this.drTarget = drTarget;
	}

	/**
	 * Gets the dr target
	 *
	 * @return the dr target
	 */
	public DRTarget getDrTarget() {
		return drTarget;
	}

	/**
	 * Executes the DRThread
	 */
	public void run() {
	  try {
		if(interval > 0) {
			int sleep = interval;
			setInterval(0);
			ServerUtil.sleep(sleep);
		}
		else if(interval < 0) {
			setInterval(0);
		}
		else {
		    drTarget.initiate();
		    drTargetManager.updateAttributes(drTarget);
		    drTargetManager.executeDR(drTarget);	
		    ServerUtil.sleep(drTarget.getDuration() * 60);
		}

	  } catch (InterruptedException ie) {
	    // Ignore exception
	  } finally {
	    drTarget.setEnabled(DRTarget.DISABLED);
	    drTargetManager.updateAttributes(drTarget);
	    drTarget = null;
	    drTargetManager = null;
	    interval = 0;
	  }
	}

	/**
	 * Interrupts and terminates the thread
	 */
	public void cancelDR() {
	  this.interrupt();
	  drTargetManager.cancelDR(drTarget);	  
	}

}
