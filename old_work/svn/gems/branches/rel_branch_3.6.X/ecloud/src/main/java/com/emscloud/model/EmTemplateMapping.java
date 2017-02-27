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
@Table(name = "em_template_mapping", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmTemplateMapping implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5240387337612171412L;
	private Long id;
	private Long emId;
	private Long emTemplateId;
	private String emTemplateName;
	private Long uemTemplateId;	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_template_mapping_seq")
    @SequenceGenerator(name="em_template_mapping_seq", sequenceName="em_template_mapping_seq", allocationSize=1, initialValue=1)
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
     * @return the emTemplateId
     */
	@Column(name = "em_template_id", nullable = false)
    public Long getEmTemplateId() {
        return emTemplateId;
    }
    /**
     * @param emTemplateId the emTemplateId to set
     */
    public void setEmTemplateId(Long emTemplateId) {
        this.emTemplateId = emTemplateId;
    }
    /**
     * @return the emTemplateName
     */
    @Column(name = "em_template_name", nullable = false)
    public String getEmTemplateName() {
        return emTemplateName;
    }
    /**
     * @param emTemplateName the emTemplateName to set
     */
    public void setEmTemplateName(String emTemplateName) {
        this.emTemplateName = emTemplateName;
    }
    /**
     * @return the uemTemplateId
     */
    @Column(name = "uem_template_id", nullable = false)
    public Long getUemTemplateId() {
        return uemTemplateId;
    }
    /**
     * @param uemTemplateId the uemTemplateId to set
     */
    public void setUemTemplateId(Long uemTemplateId) {
        this.uemTemplateId = uemTemplateId;
    }
}
