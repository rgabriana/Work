/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author Sreedhar
 */
public class OutageBasePower implements Serializable {

    /**
   * 
   */
    private static final long serialVersionUID = 400389916484540777L;

    private Long id;
    private Long fixtureId;
    private Short voltLevel;
    private Double basePower;

    public OutageBasePower() {
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return
     */
    public Long getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(Long fixtureId) {
        this.fixtureId = fixtureId;
    }

    /**
     * @return
     */
    public Short getVoltLevel() {
        return voltLevel;
    }

    public void setVoltLevel(Short voltLevel) {
        this.voltLevel = voltLevel;
    }

    /**
     * @return
     */
    public Double getBasePower() {
        return basePower;
    }

    public void setBasePower(Double basePower) {
        this.basePower = basePower;
    }

}