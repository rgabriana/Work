package com.emscloud.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.persistence.CascadeType;

@Entity
@Table(name = "facilityType", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FacilityType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private long id;	
	private String name;
			
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="facilityType_seq")
    @SequenceGenerator(name="facilityType_seq", sequenceName="facilityType_seq", allocationSize=1, initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
			
	@Column(name = "name", nullable = false)
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public static String getTypeString(long type) {
		return "";
	}
	
}
