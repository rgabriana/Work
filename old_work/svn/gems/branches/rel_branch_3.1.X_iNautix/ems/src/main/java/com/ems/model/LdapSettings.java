package com.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sameer Surjikar
 *
 */

@XmlRootElement (name = "ldapsettings")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapSettings implements java.io.Serializable {
	  
	  @XmlElement(name = "id")
	  Long id ;
	  
	  @XmlElement(name = "name")
	  String name;
	  
	  @XmlElement(name = "server")
	  String server; 
	 
	  @XmlElement(name = "port")
	  Integer port; 
	  
	  @XmlElement(name = "tls")
	  boolean tls; 
	 
	  @XmlElement(name = "passwordEncrypType")
	  String passwordEncrypType;
	  
	  @XmlElement(name = "baseDns")
	  String baseDns;
	 
	  @XmlElement(name = "userAttribute")
	  String userAttribute;
	  
	  @XmlElement(name = "allowAnonymous")
	  boolean allowAnonymous;
	 
	  @XmlElement(name = "nonAnonymousDn")
	  String nonAnonymousDn ;
	  
	  @XmlElement(name = "nonAnonymousPassword")
	  String nonAnonymousPassword;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}
	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	/**
	 * @return the tls
	 */
	public boolean isTls() {
		return tls;
	}
	/**
	 * @param tls the tls to set
	 */
	public void setTls(boolean tls) {
		this.tls = tls;
	}
	/**
	 * @return the passwordEncrypType
	 */
	public String getPasswordEncrypType() {
		return passwordEncrypType;
	}
	/**
	 * @param passwordEncrypType the passwordEncrypType to set
	 */
	public void setPasswordEncrypType(String passwordEncrypType) {
		this.passwordEncrypType = passwordEncrypType;
	}
	/**
	 * @return the baseDns
	 */
	public String getBaseDns() {
		return baseDns;
	}
	/**
	 * @param baseDns the baseDns to set
	 */
	public void setBaseDns(String baseDns) {
		this.baseDns = baseDns;
	}
	/**
	 * @return the userAtribute
	 */
	public String getUserAttribute() {
		return userAttribute;
	}
	/**
	 * @param userAtribute the userAtribute to set
	 */
	public void setUserAttribute(String userAttribute) {
		this.userAttribute = userAttribute;
	}
	/**
	 * @return the allowAnonymous
	 */
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}
	/**
	 * @param allowAnonymous the allowAnonymous to set
	 */
	public void setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
	}
	/**
	 * @return the nonAnonymousDn
	 */
	public String getNonAnonymousDn() {
		return nonAnonymousDn;
	}
	/**
	 * @param nonAnonymousDn the nonAnonymousDn to set
	 */
	public void setNonAnonymousDn(String nonAnonymousDn) {
		this.nonAnonymousDn = nonAnonymousDn;
	}
	/**
	 * @return the nonAnonymousPassword
	 */
	public String getNonAnonymousPassword() {
		return nonAnonymousPassword;
	}
	/**
	 * @param nonAnonymousPassword the nonAnonymousPassword to set
	 */
	public void setNonAnonymousPassword(String nonAnonymousPassword) {
		this.nonAnonymousPassword = nonAnonymousPassword;
	}

    
}
