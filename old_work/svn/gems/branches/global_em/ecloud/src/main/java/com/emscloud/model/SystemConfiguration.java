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
@Table(name = "system_configuration", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SystemConfiguration implements Serializable {

	private static final long serialVersionUID = -2357612034504415642L;
	private Long id;
    private String name;
    private String value;

    /**
     * @return the id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="system_configuration_seq")
    @SequenceGenerator(name="system_configuration_seq", sequenceName="system_configuration_seq")
	@Column(name = "id", unique = true, nullable = false)
	@XmlElement(name = "id")
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    @Column(name = "name", nullable = false)
	 @XmlElement(name = "name")
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the Name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the Value
     */
    @Column(name = "value", nullable = false)
	 @XmlElement(name = "value")
    public String getValue() {
        return this.value;
    }

    /**
     * @param name
     *            the Value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
