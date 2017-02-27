/**
 * 
 */
package com.communicator.em;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yogesh
 * 
 */
public class EMManager implements EMManagerMBean {
	public static EMManager m_instance = new EMManager();
	private static Map<String, EMInstance> emMap = new HashMap<String, EMInstance>();
	private String sPPAHostServer = "sppa.enlightedinc.com";
	private long totalHealthCheckCount = 0;
	private long totalCallHomeCount = 0;
	private long totalsPPAMigrationCount = 0;
	private long totalsPPASyncCount = 0;
	
	private EMManager() {

	}

	public static EMManager getInstance() {
		return m_instance;
	}

	@Override
	public int getEMCount() {
		return emMap.size();
	}

	public void addEM(EMInstance oInstance) {
		emMap.put(oInstance.getsMAC(), oInstance);
	}

	public synchronized EMInstance getEM(String sMAC) {
		return emMap.get(sMAC);
	}

	public Collection<EMInstance> getEMList() {
		return emMap.values();
	}

	public Set<String> getEMKeys() {
		return emMap.keySet();
	}

	public synchronized void setReplicaServer(EMInstance em) {
		EMInstance oInstance = emMap.get(em.getsMAC());
		oInstance.setsReplicaServerIP(em.getsReplicaServerIP());
	}

	public String toString() {
		return emMap.keySet().toString();
	}

	/**
	 * @return the sPPAHostServer
	 */
	public String getsPPAHostServer() {
		return sPPAHostServer;
	}

	/**
	 * @param sPPAHostServer
	 *            the sPPAHostServer to set
	 */
	public void setsPPAHostServer(String sPPAHostServer) {
		this.sPPAHostServer = sPPAHostServer;
	}

	@Override
	public String getEMDetails(String mac) {
		String sDetails = "";
		EMInstance oInstance = getEM(mac);
		if (oInstance != null)
			sDetails = oInstance.toString();
		return sDetails;
	}

	/**
	 * @return the totalHealthCheckCount
	 */
	public long getTotalHealthCheckCount() {
		return totalHealthCheckCount;
	}

	/**
	 * @param totalHealthCheckCount the totalHealthCheckCount to set
	 */
	public void incTotalHealthCheckCount() {
		this.totalHealthCheckCount++;
	}

	/**
	 * @return the totalCallHomeCount
	 */
	public long getTotalCallHomeCount() {
		return totalCallHomeCount;
	}

	/**
	 * @param totalCallHomeCount the totalCallHomeCount to set
	 */
	public void incTotalCallHomeCount() {
		this.totalCallHomeCount++;
	}

	/**
	 * @return the totalsPPAMigrationCount
	 */
	public long getTotalsPPAMigrationCount() {
		return totalsPPAMigrationCount;
	}

	/**
	 * @param totalsPPAMigrationCount the totalsPPAMigrationCount to set
	 */
	public void incTotalsPPAMigrationCount() {
		this.totalsPPAMigrationCount++;
	}

	/**
	 * @return the totalsPPASyncCount
	 */
	public long getTotalsPPASyncCount() {
		return totalsPPASyncCount;
	}

	/**
	 * @param totalsPPASyncCount the totalsPPASyncCount to set
	 */
	public void incTotalsPPASyncCount() {
		this.totalsPPASyncCount++;
	}
}
