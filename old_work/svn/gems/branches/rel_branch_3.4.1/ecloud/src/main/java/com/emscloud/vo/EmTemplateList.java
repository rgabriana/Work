package com.emscloud.vo;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.model.EmInstance;
import com.emscloud.model.ProfileTemplate;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmTemplateList implements Serializable {

	/**
	 * @author SharadM
	 */
	private static final long serialVersionUID = 431776385828817164L;
	@XmlElement(name = "em")
    private EmInstance em ;
	@XmlElementWrapper(name = "emTemplates")
    @XmlElement(name = "emTemplate")
    List<ProfileTemplate> emTemplate;
	
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
     * @return the emTemplate
     */
    public List<ProfileTemplate> getEmTemplate() {
        return emTemplate;
    }
    /**
     * @param emTemplate the emTemplate to set
     */
    public void setEmTemplate(List<ProfileTemplate> emTemplate) {
        this.emTemplate = emTemplate;
    }
}
