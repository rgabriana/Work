/**
 * 
 */
package com.communicator.em;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;

/**
 * @author yogesh
 * 
 */
public class EMInstance {
	private String sMAC;
	private String sReplicaServerIP;
	private String sVersion;
	private int iStatus = 0;
	private EmStatus emStatus;
	private DatabaseState databaseState;

	/**
	 * @return the sMAC
	 */
	public String getsMAC() {
		return sMAC;
	}

	/**
	 * @param sMAC
	 *            the sMAC to set
	 */
	public void setsMAC(String sMAC) {
		this.sMAC = sMAC;
	}

	/**
	 * @return the sReplicaServerIP
	 */
	public String getsReplicaServerIP() {
		return sReplicaServerIP;
	}

	/**
	 * @param sReplicaServerIP
	 *            the sReplicaServerIP to set
	 */
	public void setsReplicaServerIP(String sReplicaServerIP) {
		this.sReplicaServerIP = sReplicaServerIP;
	}

	/**
	 * @return the sVersion
	 */
	public String getsVersion() {
		return sVersion;
	}

	/**
	 * @param sVersion
	 *            the sVersion to set
	 */
	public void setsVersion(String sVersion) {
		this.sVersion = sVersion;
	}

	/**
	 * @return the iStatus
	 */
	public int getiStatus() {
		return iStatus;
	}

	/**
	 * @param iStatus
	 *            the iStatus to set
	 */
	public void setiStatus(int iStatus) {
		this.iStatus = iStatus;
	}

	public String toString() {
		return "[" + sMAC + ", " + sVersion + ", " + sReplicaServerIP + ", "
				+ iStatus + "]";
	}

	public void setCurrentEmStatus(EmStatus emStatus) {
		this.emStatus = emStatus;

	}

	public EmStatus getCurrentEmStatus() {
		return this.emStatus;
	}

	public void setCurrentMigrationState(DatabaseState valueOf) {
		this.databaseState = valueOf;
	}

	public DatabaseState getCurrentMigrationState() {
		return this.databaseState;
	}

}
