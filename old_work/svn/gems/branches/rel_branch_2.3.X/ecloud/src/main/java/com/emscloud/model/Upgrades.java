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
@Table(name = "upgrades", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Upgrades implements java.io.Serializable {

	private long id;
	private String type;
	private String name;
	private String location;
	
	public Upgrades() {
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="upgrades_seq")
    @SequenceGenerator(name="upgrades_seq", sequenceName="upgrades_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "name", nullable = false)
	@XmlElement(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type", nullable = false)
	@XmlElement(name = "type")
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "location", nullable = false)
	@XmlElement(name = "location")
	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}
}
