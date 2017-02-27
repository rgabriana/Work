package com.ems.util;

public class OutageReportVO {

	protected Long nodeId;
	protected String nodeType;
	/*protected Date fromDate;
	protected Date toDate;*/
	
	public OutageReportVO(Long nodeId, String nodeType){
		this.nodeId=nodeId;
		this.nodeType=nodeType;
		/*this.fromDate=fromDate;
		this.toDate=toDate;*/
	}
	public OutageReportVO(){
		
	}
	public Long getNodeId() {
		return nodeId;
	}
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
}
