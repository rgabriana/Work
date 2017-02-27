/**
 * 
 */
package com.ems.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author sreedhar
 */
public class GwStats implements Serializable {

    private static final long serialVersionUID = 7323703683116646247L;

    private Long Id;
    private Long gwId;
    private Date captureAt;
    private Long uptime;
    private Long noPktsFromGems;
    private Long noPktsToGems;
    private Long noPktsToNodes;
    private Long noPktsFromNodes;

    /**
   * 
   */
    public GwStats() {
        // TODO Auto-generated constructor stub
    }

    public GwStats(Long gwId, Long uptime, Long noPktsFromGems, Long noPktsToGems, Long noPktsFromNodes,
            Long noPktsToNodes) {

        this.gwId = gwId;
        this.uptime = uptime;
        this.noPktsFromGems = noPktsFromGems;
        this.noPktsToGems = noPktsToGems;
        this.noPktsFromNodes = noPktsFromNodes;
        this.noPktsToNodes = noPktsToNodes;

    } // end of constructor

    /**
     * @return the id
     */
    public Long getId() {
        return Id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        Id = id;
    }

    /**
     * @return the gwId
     */
    public Long getGwId() {
        return gwId;
    }

    /**
     * @param gwId
     *            the gwId to set
     */
    public void setGwId(Long gwId) {
        this.gwId = gwId;
    }

    /**
     * @return the captureAt
     */
    public Date getCaptureAt() {
        return captureAt;
    }

    /**
     * @param captureAt
     *            the captureAt to set
     */
    public void setCaptureAt(Date captureAt) {
        this.captureAt = captureAt;
    }

    /**
     * @return the uptime
     */
    public Long getUptime() {
        return uptime;
    }

    /**
     * @param uptime
     *            the uptime to set
     */
    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    /**
     * @return the noPktsFromGems
     */
    public Long getNoPktsFromGems() {
        return noPktsFromGems;
    }

    /**
     * @param noPktsFromGems
     *            the noPktsFromGems to set
     */
    public void setNoPktsFromGems(Long noPktsFromGems) {
        this.noPktsFromGems = noPktsFromGems;
    }

    /**
     * @return the noPktsToGems
     */
    public Long getNoPktsToGems() {
        return noPktsToGems;
    }

    /**
     * @param noPktsToGems
     *            the noPktsToGems to set
     */
    public void setNoPktsToGems(Long noPktsToGems) {
        this.noPktsToGems = noPktsToGems;
    }

    /**
     * @return the noPktsToNodes
     */
    public Long getNoPktsToNodes() {
        return noPktsToNodes;
    }

    /**
     * @param noPktsToNodes
     *            the noPktsToNodes to set
     */
    public void setNoPktsToNodes(Long noPktsToNodes) {
        this.noPktsToNodes = noPktsToNodes;
    }

    /**
     * @return the noPktsFromNodes
     */
    public Long getNoPktsFromNodes() {
        return noPktsFromNodes;
    }

    /**
     * @param noPktsFromNodes
     *            the noPktsFromNodes to set
     */
    public void setNoPktsFromNodes(Long noPktsFromNodes) {
        this.noPktsFromNodes = noPktsFromNodes;
    }

} // end of class GwStats
