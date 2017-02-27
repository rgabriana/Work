package com.emsdashboard.model;

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
@Table(name = "gems")
@XmlRootElement (name = "gems")
@XmlAccessorType(XmlAccessType.NONE)
public class GemsServer {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator="gemsSeqGen")
	@SequenceGenerator(name="gemsSeqGen", sequenceName="gems_seq")
    @Column(name = "id")
	@XmlElement(name = "id")
    private Long id;

    @Column(name = "name")
    @XmlElement(name = "name")
    private String name;

    @Column(name = "gems_unique_address")
    @XmlElement(name = "gemsUniqueAddress")
    private String gemsUniqueAddress;
    
    @Column(name = "mac_id")
    @XmlElement(name = "macId")
    private String macId;
    
    @Column(name = "gems_ip_address")
    @XmlElement(name = "gemsIpAddress")
    private String gemsIpAddress;
    
    @Column(name = "port")
    @XmlElement(name = "port")
    private Long port;
    
    @Column(name = "version")
    @XmlElement(name = "version")
    private Long version;
    
    @Column(name = "api_key")
    @XmlElement(name = "apiKey")
    private String apiKey;
    
    @Column(name = "status")
    @XmlElement(name = "status")
    private char status;
    
    @Column(name = "timezone")
    @XmlElement(name = "timezone")
    private String timeZone ;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGemsUniqueAddress() {
		return gemsUniqueAddress;
	}

	public void setGemsUniqueAddress(String gemsUniqueAddress) {
		this.gemsUniqueAddress = gemsUniqueAddress;
	}

	public String getMacId() {
		return macId;
	}

	public void setMacId(String macId) {
		this.macId = macId;
	}

	public String getGemsIpAddress() {
		return gemsIpAddress;
	}

	public void setGemsIpAddress(String gemsIpAddress) {
		this.gemsIpAddress = gemsIpAddress;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long i) {
		this.port = i;
	}

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
	
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
