package com.emscloud.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Entity
@Table(name = "organization_energy_consumption_hourly", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OrganizationEnergyConsumptionHourly extends AggregatedStatsData implements Serializable {

	private static final long serialVersionUID = 1029630101450441670L;
	
}