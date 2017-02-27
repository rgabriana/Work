package com.emscloud.vo;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.communication.vos.EMProfile;
import com.emscloud.model.EmInstance;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmProfileList implements Serializable {

	/**
	 * @author SharadM
	 */
	private static final long serialVersionUID = 431776385828817164L;
	@XmlElement(name = "em")
	private EmInstance em ;
	@XmlElementWrapper(name = "emProfiles")
	@XmlElement(name = "emProfile")
    List<EMProfile> emProfile;
    /**
     * @return the em
     */
    public EmInstance getEm() {
        return em;
    }
    /**
     * @param em the em to set
     */
    public void setEm(EmInstance em) {
        this.em = em;
    }
    /**
     * @return the profileList
     */
    public List<EMProfile> getProfileList() {
        return emProfile;
    }
    /**
     * @param profileList the profileList to set
     */
    public void setProfileList(List<EMProfile> profileList) {
        this.emProfile = profileList;
    }
}
