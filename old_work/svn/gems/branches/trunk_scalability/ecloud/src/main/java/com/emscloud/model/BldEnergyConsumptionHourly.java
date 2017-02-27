package com.emscloud.model;

import java.io.Serializable;

import javax.persistence.Entity;

import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Entity
@Table(name = "bld_energy_consumption_hourly", schema = "public")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BldEnergyConsumptionHourly extends AggregatedStatsData implements Serializable {

	private static final long serialVersionUID = 1029630101450441670L;
	
}
