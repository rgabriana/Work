package com.communicator.em;

public interface EMManagerMBean {

	public int getEMCount();

	public String getEMDetails(String mac);

	public long getTotalHealthCheckCount();

	public long getTotalCallHomeCount();

	public long getTotalsPPAMigrationCount();

	public long getTotalsPPASyncCount();

}
