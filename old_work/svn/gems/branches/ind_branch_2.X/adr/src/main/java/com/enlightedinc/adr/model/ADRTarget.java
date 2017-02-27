package com.enlightedinc.adr.model;

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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Kushal
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="adr_target")
public class ADRTarget {

    private Long id;
    private String drIdentifier;
    private String drStatus;
    private String operationMode;
    private Date startTime;
    private Date endTime;
    private Double loadAmount;
    private Double priceAbsolute;
    private Double priceRelative;
    
    public double currentTime;
    public String modeTimeSlot;
/*    public String loadAmountTimeOffset;
    public String priceRelativeTimeOffset;
    public String priceAbsoluteTimeOffset;*/


    public ADRTarget() {
    }

    public ADRTarget(Long id, String drIdentifier, String drStatus, String operationMode, Date startTime
    		, Date endTime, Double loadAmount, Double priceAbsolute, Double priceRelative) {
        super();
        this.id = id;
        this.drIdentifier = drIdentifier;
        this.drStatus = drStatus;
        this.operationMode = operationMode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.loadAmount = loadAmount;
        this.priceAbsolute = priceAbsolute;
        this.priceRelative = priceRelative;
    }

/*    public void initiate() {
        this.setEnabled(ENABLED);
        this.setStartTime(new Date());
    }*/

    @Column(name="DR_IDENTIFIER")
    public String getDrIdentifier() {
		return drIdentifier;
	}

	public void setDrIdentifier(String drIdentifier) {
		this.drIdentifier = drIdentifier;
	}

	@Column(name="DR_STATUS")
	public String getDrStatus() {
		return drStatus;
	}

	public void setDrStatus(String drStatus) {
		this.drStatus = drStatus;
	}

	@Column(name="OPERATION_MODE_VALUE")
	public String getOperationMode() {
		return operationMode;
	}

	public void setOperationMode(String operationMode) {
		this.operationMode = operationMode;
	}

	@Column(name="START_TIME")
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Column(name="END_TIME")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name="LOAD_AMOUNT")
	public Double getLoadAmount() {
		return loadAmount;
	}

	public void setLoadAmount(Double loadAmount) {
		this.loadAmount = loadAmount;
	}

	@Column(name="PRICE_ABSOLUTE")
	public Double getPriceAbsolute() {
		return priceAbsolute;
	}

	public void setPriceAbsolute(Double priceAbsolute) {
		this.priceAbsolute = priceAbsolute;
	}

	@Column(name="PRICE_RELATIVE")
	public Double getPriceRelative() {
		return priceRelative;
	}

	public void setPriceRelative(Double priceRelative) {
		this.priceRelative = priceRelative;
	}

	/**
     * @return unique identifier
     */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="AdrTargetSeq")
    @SequenceGenerator(name="AdrTargetSeq",sequenceName="ADR_TARGET_SEQ", allocationSize=1)
    @Column(name="ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
