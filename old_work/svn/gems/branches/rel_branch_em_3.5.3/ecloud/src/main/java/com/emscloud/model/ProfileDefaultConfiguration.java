package com.emscloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "profile_default_configuration")
public class ProfileDefaultConfiguration {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="profile_default_configuration_seq")
	@SequenceGenerator(name="profile_default_configuration_seq", sequenceName="profile_default_configuration_seq", allocationSize=1, initialValue=1)
    @Column(name = "id")	
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "value")
	private String value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String key) {
		this.name = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
