package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.communication.types.TaskCode;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;

@Entity
@Table(name = "em_tasks_uuid", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmTasksUUID implements Serializable{
	
	private static final long serialVersionUID = -5404694233653717390L;	
	private long id;
	private String uuid;
	private boolean active;		
	
	/**
	 * @return the id
	 */
	@Id	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_tasks_uuid_seq")
    @SequenceGenerator(name="em_tasks_uuid_seq", sequenceName="em_tasks_uuid_seq", allocationSize=1, initialValue=1)
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
	
	/**
	 * @return the uuid
	 */
	@Column(name = "uuid", nullable = false)
	@XmlElement(name = "uuid")
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * @return the active
	 */
	@Column(name = "active", nullable = false)
	@XmlElement(name = "active")
	public boolean getActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}	
	
}
