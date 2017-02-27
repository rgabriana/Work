package com.ems.model;

import java.sql.Blob;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * 
 @author Abhishek sinha
 * 
 */

public class PlanMap {

    private static final long serialVersionUID = -8346640146041015941L;
    private Long id;
    private Blob plan;
    private CommonsMultipartFile fileData;

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
     * @return the name
     */
    public Blob getPlan() {
        return plan;
    }

    /**
     * @param plan
     *            the plan to set
     */
    public void setPlan(Blob plan) {
        this.plan = plan;
    }

    public CommonsMultipartFile getFileData() {
        return fileData;
    }

    public void setFileData(CommonsMultipartFile fileData) {
        this.fileData = fileData;
    }

}
