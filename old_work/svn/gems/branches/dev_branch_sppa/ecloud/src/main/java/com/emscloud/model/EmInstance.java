package com.emscloud.model;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
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

	private static final long serialVersionUID = -5404694993653717365L;
	private long id;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String timeZone;
	private Date lastConnectivityAt;
	private String databaseName;
	


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_instance_seq")
    @SequenceGenerator(name="em_instance_seq", sequenceName="em_instance_seq")
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

	@Column(name = "database_name", length = 29)
	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}



}
