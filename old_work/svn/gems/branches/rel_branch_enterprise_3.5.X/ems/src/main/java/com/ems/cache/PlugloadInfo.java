/**
 * 
 */
package com.ems.cache;

import java.util.Date;

import com.ems.action.SpringContext;
import com.ems.model.Fixture;
import com.ems.model.Plugload;
import com.ems.service.PlugloadManager;

/**
 * @author sreedhar.kamishetti
 *
 */
public class PlugloadInfo extends DeviceInfo {

	private Plugload plugload = null;
	private PlugloadManager plugloadMgr = null;
	
	/**
	 * 
	 */
	public PlugloadInfo() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param fixture
	 */
	public PlugloadInfo(Plugload plugload) {

		this.plugload = plugload;
    this.deviceName = plugload.getName();
    this.dbId = plugload.getId();    
    this.deviceState = plugload.getState();
    if(plugload.getLastStatsRcvdTime() != null) {
      this.lastStatsRcvdTime = plugload.getLastStatsRcvdTime();
    } else {
      this.lastStatsRcvdTime = new Date();
    }
		
		
	}
	
	public Plugload getPlugload() {
    
    return plugload;
    
  } //end of method getPlugload
  
  public void setPlugload(Plugload plugload) {
    
    this.plugload = plugload;
    
  } //end of method setPlugload
  
  public void resetPushProfileForPlugload(long seqNo) {
    
    profileSeqList.remove(seqNo);    
    if(profileSeqList.size() == 0) {  
      if(plugloadMgr == null) {
      	plugloadMgr = (PlugloadManager)SpringContext.getBean("plugloadManager");
      }
      plugloadMgr.resetPushProfileForPlugload(plugload.getId());
    }
    
  } //end of method resetPushProfileForPlugload


} //end of class PlugloadInfo
