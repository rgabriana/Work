/**
 * 
 */
package com.ems.cache;

import com.ems.model.Fixture;
import com.ems.model.Plugload;

/**
 * @author sreedhar.kamishetti
 *
 */
public class PlugloadInfo extends DeviceInfo {

	private Plugload plugload = null;
	
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
		
	}
	
	public Plugload getPlugload() {
    
    return plugload;
    
  } //end of method getPlugload
  
  public void setPlugload(Plugload plugload) {
    
    this.plugload = plugload;
    
  } //end of method setPlugload

} //end of class PlugloadInfo
