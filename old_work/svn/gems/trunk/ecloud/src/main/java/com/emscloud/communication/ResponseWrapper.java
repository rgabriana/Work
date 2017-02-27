package com.emscloud.communication;

import java.util.List;

import com.emscloud.model.EmInstance;



public class ResponseWrapper<T> {
	
	private EmInstance em ;
	private Integer status;
	private T items;
	
	public T getItems() {
		return items;
	}
	public void setItems(T items) {
		this.items = items;
	}
	public EmInstance getEm() {
		return em;
	}
	public void setEm(EmInstance em) {
		this.em = em;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer i) {
		this.status = i;
	}
	


}
