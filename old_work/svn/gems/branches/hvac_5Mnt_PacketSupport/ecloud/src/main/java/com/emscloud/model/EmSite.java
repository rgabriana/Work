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
@Table(name = "em_site", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmSite implements java.io.Serializable {

	private static final long serialVersionUID = -5404694993653717365L;
	
	private long id;
	private Long siteId;
	private Long emId;
		
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="em_site_seq")
	@SequenceGenerator(name="em_site_seq", sequenceName="em_site_seq",allocationSize=1,initialValue=1)
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "em_id")
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
	
	@Column(name = "site_id")
	@XmlElement(name = "siteId")
	public Long getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

} //end of class SiteEms
