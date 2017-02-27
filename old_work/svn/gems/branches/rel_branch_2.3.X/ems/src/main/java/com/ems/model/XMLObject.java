package com.ems.model;

import java.util.HashSet;
import java.util.Set;

public class XMLObject {
	
	private String name;
	private String address;
	private String contact;
	private Set<Campus> campusSet;
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
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * @return the campusSet
	 */
	public Set<Campus> getCampusSet() {
		return campusSet;
	}
	/**
	 * @param campusSet the campusSet to set
	 */
	public void setCampusSet(Set<Campus> campusSet) {
		this.campusSet = campusSet;
	}
	
	public void addCampus(Campus campus){
		if(campusSet == null){
			campusSet = new HashSet<Campus>();
		}
		campusSet.add(campus);
	}
}
