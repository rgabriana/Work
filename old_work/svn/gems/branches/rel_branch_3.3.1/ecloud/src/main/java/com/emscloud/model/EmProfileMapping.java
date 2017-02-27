package com.emscloud.model;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "em_profile_mapping", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmProfileMapping implements Serializable{

	/**
	 * @author SharadM
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private Long id;
	private Long emId;
	private Long emGroupId;
	private Short emProfileNo;
	private Long uemProfileId;
	private Long templateId;
	private Integer syncStatus;
	private Date syncStartTime;
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_profile_mapping_seq")
    @SequenceGenerator(name="em_profile_mapping_seq", sequenceName="em_profile_mapping_seq", allocationSize=1, initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return the emId
	 */
	@Column(name = "em_id", nullable = false)
	@XmlElement(name = "emId")
	public Long getEmId() {
		return emId;
	}
	/**
	 * @param emId the emId to set
	 */
	public void setEmId(Long emId) {
		this.emId = emId;
	}
    /**
     * @return the emGroupId
     */
	@Column(name = "em_group_id", nullable = false)
    public Long getEmGroupId() {
        return emGroupId;
    }
    /**
     * @param emGroupId the emGroupId to set
     */
    public void setEmGroupId(Long emGroupId) {
        this.emGroupId = emGroupId;
    }
    /**
     * @return the emProfileNo
     */
    @Column(name = "em_profile_no", nullable = false)
    public Short getEmProfileNo() {
        return emProfileNo;
    }
    /**
     * @param emProfileNo the emProfileNo to set
     */
    public void setEmProfileNo(Short emProfileNo) {
        this.emProfileNo = emProfileNo;
    }
    /**
     * @return the uemProfileId
     */
    @Column(name = "uem_profile_id", nullable = false)
    public Long getUemProfileId() {
        return uemProfileId;
    }
    /**
     * @param uemProfileId the uemProfileId to set
     */
    public void setUemProfileId(Long uemProfileId) {
        this.uemProfileId = uemProfileId;
    }
    /**
     * @return the templateId
     */
    @Column(name = "template_id", nullable = false)
    public Long getTemplateId() {
        return templateId;
    }
    /**
     * @param templateId the templateId to set
     */
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    /**
     * @return the syncStatus
     */
    @Column(name = "sync_status")
    public Integer getSyncStatus() {
        return syncStatus;
    }
    /**
     * @param syncStatus the syncStatus to set
     */
    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }
    /**
     * @return the syncStartTime
     */
    @Column(name = "sync_start_time")
    public Date getSyncStartTime() {
        return syncStartTime;
    }
    /**
     * @param syncStartTime the syncStartTime to set
     */
    public void setSyncStartTime(Date syncStartTime) {
        this.syncStartTime = syncStartTime;
    }
   
}
