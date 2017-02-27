package com.emscloud.model;



import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "app_instance", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AppInstance implements java.io.Serializable {

	private static final long serialVersionUID = -7345190884290061515L;
	private long id;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String timeZone;
	private Boolean active;
	private Date lastConnectivityAt;
	private String utcLastConnectivityAt;
	private Date appCommissionedDate;	
	private String appType;
	
	
	private boolean openTunnelToCloud;
	private Long tunnelPort;
	private String browsableLink ;
	
	private boolean openSshTunnelToCloud;
	private Long sshTunnelPort;
	
	private String ipAddress;
	


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="app_instance_seq")
    @SequenceGenerator(name="app_instance_seq", sequenceName="app_instance_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}


	@Column(name = "version")
	@XmlElement(name = "version")
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "mac_id", nullable = false)
	@XmlElement(name = "macId")
	public String getMacId() {
		return this.macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}
	
	@Column(name = "time_zone")
	@XmlElement(name = "timeZone")
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	@Column(name = "last_connectivity_at")
	@XmlElement(name = "lastConnectivityAt")
	public Date getLastConnectivityAt() {
		return this.lastConnectivityAt;
	}

	public void setLastConnectivityAt(Date lastConnectivityAt) {
		this.lastConnectivityAt = lastConnectivityAt;
	}
	
	@Transient
	@XmlElement(name = "utcLastConnectivityAt")
	public String getUtcLastConnectivityAt() {
		return utcLastConnectivityAt;
	}

	public void setUtcLastConnectivityAt(String utcLastConnectivityAt) {
		this.utcLastConnectivityAt = utcLastConnectivityAt;
	}

	/**	
	* @return the appCommissionedDate
	*/
	@Column(name = "app_commissioned_date")
	@XmlElement(name = "appCommissionedDate")
	public Date getAppCommissionedDate() {
		return appCommissionedDate;
	}
	
	/**
	* @param appCommissionedDate the appCommissionedDate to set
	*/
	public void setAppCommissionedDate(Date appCommissionedDate) {
		this.appCommissionedDate = appCommissionedDate;
	}
	
	@Column(name = "active")
	@XmlElement(name = "active")
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	@Column(name = "open_tunnel_to_cloud")
	@XmlElement(name = "openTunnelToCloud")
	public boolean getOpenTunnelToCloud() {
		return openTunnelToCloud;
	}
	
	public void setOpenTunnelToCloud(boolean openTunnelToCloud) {
		this.openTunnelToCloud = openTunnelToCloud;
	}
	@Column(name = "tunnel_port")
	@XmlElement(name = "tunnelPort")
	public Long getTunnelPort() {
		return tunnelPort;
	}

	public void setTunnelPort(Long tunnelPort) {
		this.tunnelPort = tunnelPort;
	}
	@Column(name = "browsable_link")
	@XmlElement(name = "browsableLink")
	public String getBrowsableLink() {
		return browsableLink;
	}

	public void setBrowsableLink(String browsableLink) {
		this.browsableLink = browsableLink;
	}
	
	@Column(name = "open_ssh_tunnel_to_cloud")
	@XmlElement(name = "openSshTunnelToCloud")
	public boolean getOpenSshTunnelToCloud() {
		return openSshTunnelToCloud;
	}

	public void setOpenSshTunnelToCloud(boolean openSshTunnelToCloud) {
		this.openSshTunnelToCloud = openSshTunnelToCloud;
	}
	@Column(name = "ssh_tunnel_port")
	@XmlElement(name = "sshTunnelPort")
	public Long getSshTunnelPort() {
		return sshTunnelPort;
	}

	public void setSshTunnelPort(Long sshTunnelPort) {
		this.sshTunnelPort = sshTunnelPort;
	}

	
	@Column(name = "ip_address")
	@XmlElement(name = "ipAddress")
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "app_type")
	@XmlElement(name = "app_type")
	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}	

}
