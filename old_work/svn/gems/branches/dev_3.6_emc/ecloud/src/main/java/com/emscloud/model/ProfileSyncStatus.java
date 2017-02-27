package com.emscloud.model;

import java.io.Serializable;

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
@Table(name = "profile_sync_status", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileSyncStatus implements Serializable{

	/**
	 * @author SharadM
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private Long id;
	private Long emId;
	private Boolean profileDownloadSync = false;
	private Boolean templateDownloadSync =false;
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_sync_status_seq")
    @SequenceGenerator(name="profile_sync_status_seq", sequenceName="profile_sync_status_seq", allocationSize=1, initialValue=1)
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
     * @return the profileDownloadSync
     */
    @Column(name = "profile_download_sync")
    public Boolean getProfileDownloadSync() {
        return profileDownloadSync;
    }
    /**
     * @param profileDownloadSync the profileDownloadSync to set
     */
    public void setProfileDownloadSync(Boolean profileDownloadSync) {
        this.profileDownloadSync = profileDownloadSync;
    }
    /**
     * @return the templateDownloadSync
     */
    @Column(name = "template_download_sync")
    public Boolean getTemplateDownloadSync() {
        return templateDownloadSync;
    }
    /**
     * @param templateDownloadSync the templateDownloadSync to set
     */
    public void setTemplateDownloadSync(Boolean templateDownloadSync) {
        this.templateDownloadSync = templateDownloadSync;
    }
}
