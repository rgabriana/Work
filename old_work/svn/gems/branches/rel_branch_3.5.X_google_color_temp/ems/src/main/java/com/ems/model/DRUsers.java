/**
 * DR User maintains list of openADR user credentials required for communication with openADR server.
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author yogesh
 * @hibernate.class table="dr_users"
 * 
 */
public class DRUsers implements Serializable {

	private static final long serialVersionUID = -1702580838407770311L;
	private Long id;
	private String name;
	private String password;
	private String server;
	
	private String venId;
	private Long timeInterval;
	private String marketcontext1;
	private String marketcontext2;
	private String marketcontext3;
	private String vtnId1;
	private String vtnId2;
	private String vtnId3;
	private String version;
	private String keystoreFileName;
	private String truststoreFileName;
	private String keystorePassword;
	private String truststorePassword;
	private String prefix;
	private String servicepath;

	/**
	 * @return unique identifier
	 * @hibernate.id generator-class="native" unsaved-value="null"
	 * @hibernate.generator-param name="sequence" value="dr_users_seq"
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 * @hibernate.property column="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 * @hibernate.property column="password"
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

	/**
	 * @return the venId
	 */
	public String getVenId() {
		return venId;
	}

	/**
	 * @param venId the venId to set
	 */
	public void setVenId(String venId) {
		this.venId = venId;
	}

	/**
	 * @return the timeInterval
	 */
	public Long getTimeInterval() {
		return timeInterval;
	}

	/**
	 * @param timeInterval the timeInterval to set
	 */
	public void setTimeInterval(Long timeInterval) {
		this.timeInterval = timeInterval;
	}

	/**
	 * @return the marketcontext1
	 */
	public String getMarketcontext1() {
		return marketcontext1;
	}

	/**
	 * @param marketcontext1 the marketcontext1 to set
	 */
	public void setMarketcontext1(String marketcontext1) {
		this.marketcontext1 = marketcontext1;
	}

	/**
	 * @return the marketcontext2
	 */
	public String getMarketcontext2() {
		return marketcontext2;
	}

	/**
	 * @param marketcontext2 the marketcontext2 to set
	 */
	public void setMarketcontext2(String marketcontext2) {
		this.marketcontext2 = marketcontext2;
	}

	/**
	 * @return the marketcontext3
	 */
	public String getMarketcontext3() {
		return marketcontext3;
	}

	/**
	 * @param marketcontext3 the marketcontext3 to set
	 */
	public void setMarketcontext3(String marketcontext3) {
		this.marketcontext3 = marketcontext3;
	}

	/**
	 * @return the vtnId1
	 */
	public String getVtnId1() {
		return vtnId1;
	}

	/**
	 * @param vtnId1 the vtnId1 to set
	 */
	public void setVtnId1(String vtnId1) {
		this.vtnId1 = vtnId1;
	}

	/**
	 * @return the vtnId2
	 */
	public String getVtnId2() {
		return vtnId2;
	}

	/**
	 * @param vtnId2 the vtnId2 to set
	 */
	public void setVtnId2(String vtnId2) {
		this.vtnId2 = vtnId2;
	}

	/**
	 * @return the vtnId3
	 */
	public String getVtnId3() {
		return vtnId3;
	}

	/**
	 * @param vtnId3 the vtnId3 to set
	 */
	public void setVtnId3(String vtnId3) {
		this.vtnId3 = vtnId3;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setKeystoreFileName(String keystoreFileName) {
		this.keystoreFileName = keystoreFileName;
	}

	public String getKeystoreFileName() {
		return keystoreFileName;
	}

	public void setTruststoreFileName(String truststoreFileName) {
		this.truststoreFileName = truststoreFileName;
	}

	public String getTruststoreFileName() {
		return truststoreFileName;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setServicepath(String servicepath) {
		this.servicepath = servicepath;
	}

	public String getServicepath() {
		return servicepath;
	}
    

}
