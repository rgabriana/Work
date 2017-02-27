package com.emscloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "replica_server", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ReplicaServer {

	private Long id;
	private String ip;
	private String name;
	private String uid;
	private String internalIp;
	private String macId;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "replica_server_seq")
	@SequenceGenerator(name = "replica_server_seq", sequenceName = "replica_server_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ip", unique = true, nullable = false)
	@XmlElement(name = "ip")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	public void setInternalIp(String internalIp) {
		this.internalIp = internalIp;
	}

	@Column(name = "internal_ip")
	@XmlElement(name = "internalIp")
	public String getInternalIp() {
		return internalIp;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Column(name = "uid")
	@XmlElement(name = "uid")
	public String getUid() {
		return uid;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}

	@Column(name = "mac_id")
	@XmlElement(name = "macId")
	public String getMacId() {
		return macId;
	}
	
}
