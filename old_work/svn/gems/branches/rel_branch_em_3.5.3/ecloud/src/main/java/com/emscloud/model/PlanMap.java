/**
 * 
 */
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

/**
 * @author Admin
 *
 */
@Entity
@Table(name = "plan_map")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PlanMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6067602248448358189L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="plan_map_seq")
    @SequenceGenerator(name="plan_map_seq", sequenceName="plan_map_seq", allocationSize=1, initialValue=1)
    @Column(name = "id")
	@XmlElement(name = "id")
    private Long id;
	
	@Column(name = "name")
	@XmlElement(name = "name")
    private String name;
	
	@Column(name = "plan_map")
	private byte[] plan_map;

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

	/**
	 * @return the plan_map
	 */
	public byte[] getPlan_map() {
		return plan_map;
	}

	/**
	 * @param plan_map the plan_map to set
	 */
	public void setPlan_map(byte[] plan_map) {
		this.plan_map = plan_map;
	}
	
	
}