package com.emscloud.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.model.Users;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UsersList {
	
	@XmlElement(name = "page")
	private int page;
	@XmlElement(name = "records")
	private long records;
	@XmlElement(name = "total")
	private long total;
	@XmlElement(name = "userlist")
	private List<Users> users;

	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public long getRecords() {
		return records;
	}
	public void setRecords(long records) {
		this.records = records;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<Users> getUsers() {
		return users;
	}
	public void setUsers(List<Users> users) {
		this.users = users;
	}
	public static final int DEFAULT_ROWS = 5;

	

}
