package com.emscloud.model;



import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "em_instance", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmInstance implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5404694993653717365L;
	private long id;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String securityKey;
	private String timeZone;
	private Date lastConnectivityAt;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private Set<EmCampus> emCampuses = new HashSet<EmCampus>(0);

	public EmInstance() {
	}

	public EmInstance(long id, Customer customer, String name, String macId) {
		this.id = id;
		this.customer = customer;
		this.name = name;
		this.macId = macId;
	}

	public EmInstance(long id, Customer customer, String name, String macId,
			String securityKey, String timeZone, Date lastConnectivityAt,
			BigDecimal latitude, BigDecimal longitude, Set<EmCampus> emCampuses) {
		this.id = id;
		this.customer = customer;
		this.name = name;
		this.macId = macId;
		this.securityKey = securityKey;
		this.timeZone = timeZone;
		this.lastConnectivityAt = lastConnectivityAt;
		this.latitude = latitude;
		this.longitude = longitude;
		this.emCampuses = emCampuses;
	}

	@Id
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
	@JoinColumn(name = "customer_id", nullable = false)
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Column(name = "name", nullable = false)
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

	@Column(name = "security_key")
	@XmlElement(name = "securityKey")
	public String getSecurityKey() {
		return this.securityKey;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}

	@Column(name = "time_zone")
	@XmlElement(name = "timeZone")
	public String getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_connectivity_at", length = 29)
	@XmlElement(name = "lastConnectivityAt")
	public Date getLastConnectivityAt() {
		return this.lastConnectivityAt;
	}

	public void setLastConnectivityAt(Date lastConnectivityAt) {
		this.lastConnectivityAt = lastConnectivityAt;
	}

	@Column(name = "latitude", precision = 4)
	@XmlElement(name = "latitude")
	public BigDecimal getLatitude() {
		return this.latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	@Column(name = "longitude", precision = 4)
	@XmlElement(name = "longitude")
	public BigDecimal getLongitude() {
		return this.longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "emInstance")
	public Set<EmCampus> getEmCampuses() {
		return this.emCampuses;
	}

	public void setEmCampuses(Set<EmCampus> emCampuses) {
		this.emCampuses = emCampuses;
	}

}
