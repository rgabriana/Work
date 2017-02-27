/**
 * 
 */
package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EmailConfiguration implements Serializable {

    private static final long serialVersionUID = 3417517523423672780L;
    @XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "host")
    private String host;
    @XmlElement(name = "port")
    private String port="";
    @XmlElement(name = "user")
    private String user;
    @XmlElement(name = "pass")
    private String pass;
    @XmlElement(name = "protocol")
    private String protocol;
    @XmlElement(name = "flagAuth")
    private String flagAuth;
    @XmlElement(name = "flagTls")
    private String flagTls;
    private String prevPort="";
    
	public String getPrevPort() {
		return prevPort;
	}
	public void setPrevPort(String prevPort) {
		this.prevPort = prevPort;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getFlagAuth() {
		return flagAuth;
	}
	public void setFlagAuth(String flagAuth) {
		this.flagAuth = flagAuth;
	}
	public String getFlagTls() {
		return flagTls;
	}
	public void setFlagTls(String flagTls) {
		this.flagTls = flagTls;
	}

    
}
