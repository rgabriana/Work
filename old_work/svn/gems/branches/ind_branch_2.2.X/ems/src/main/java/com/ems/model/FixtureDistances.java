/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

/**
 * @author EMS
 */
public class FixtureDistances implements Serializable {

    private static final long serialVersionUID = 2L;

    private Long id;
    private String srcFixture;
    private String dstFixture;
    private Integer lightLevel;

    /**
   * 
   */
    public FixtureDistances() {
        // TODO Auto-generated constructor stub
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
     * @return source fixture
     */
    public String getSrcFixture() {
        return srcFixture;
    }

    public void setSrcFixture(String srcFixture) {
        this.srcFixture = srcFixture;
    }

    /**
     * @return destination fixture
     */
    public String getDstFixture() {
        return dstFixture;
    }

    public void setDstFixture(String dstFixture) {
        this.dstFixture = dstFixture;
    }

    /**
     * @return light level
     */
    public Integer getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(Integer lightLevel) {
        this.lightLevel = lightLevel;
    }

} // end of class FixtureDistances
