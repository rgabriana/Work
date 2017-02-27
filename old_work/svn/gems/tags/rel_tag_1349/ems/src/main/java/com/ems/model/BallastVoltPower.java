/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author EMS
 */
public class BallastVoltPower implements Serializable {

    /**
   * 
   */
    private static final long serialVersionUID = -8910774561083084850L;

    private Long id;
    private Long ballastId;
    private Long voltPowerMapId;
    private Double volt;
    private Double power;

    public BallastVoltPower() {
    }

    /**
     * @return
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
    public Long getBallastId() {
        return ballastId;
    }

    public void setBallastId(Long ballastId) {
        this.ballastId = ballastId;
    }

    /**
     * @return
     */
    public Long getVoltPowerMapId() {
        return voltPowerMapId;
    }

    public void setVoltPowerMapId(Long voltPowerMapId) {
        this.voltPowerMapId = voltPowerMapId;
    }

    /**
     * @return
     */
    public Double getVolt() {
        return volt;
    }

    public void setVolt(Double volt) {
        this.volt = volt;
    }

    /**
     * @return
     */
    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

}
