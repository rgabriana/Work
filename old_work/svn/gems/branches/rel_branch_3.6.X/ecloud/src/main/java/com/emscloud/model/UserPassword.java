/**
 * 
 */
package com.emscloud.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sampath Akula
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserPassword implements Serializable    {

    /**
     * 
     */
    private static final long serialVersionUID = 5923168136919379079L;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

     
    
    @XmlElement(name = "oldPassword")
    private String oldPassword;
    
    @XmlElement(name = "newPassword")
    private String newPassword;
    
   

}
