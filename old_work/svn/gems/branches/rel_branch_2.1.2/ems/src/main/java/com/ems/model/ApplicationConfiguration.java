package com.ems.model;

import java.io.Serializable;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class ApplicationConfiguration implements Serializable {

    private static final long serialVersionUID = 8661463002499134221L;
    private Long id;
    private boolean selfLogin;
    private String validDomain;
    private Float price;

    /**
     * @return the id
     */
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
     * @return the selfLogin
     */
    public boolean isSelfLogin() {
        return selfLogin;
    }

    /**
     * @param selfLogin
     *            the selfLogin to set
     */
    public void setSelfLogin(boolean selfLogin) {
        this.selfLogin = selfLogin;
    }

    /**
     * @return the validDomain
     */
    public String getValidDomain() {
        return validDomain;
    }

    /**
     * @param validDomain
     *            the validDomain to set
     */
    public void setValidDomain(String validDomain) {
        this.validDomain = validDomain;
    }

    /**
     * @return the price
     */
    public Float getPrice() {
        return price;
    }

    /**
     * @param price
     *            the price to set
     */
    public void setPrice(Float price) {
        this.price = price;
    }
}
