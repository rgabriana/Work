package com.emscloud.api.util;


/*@Component("request")
@Scope("request")*/
public class Request {
	
	private Long transactionId;
	private StringBuilder message;
	
	
	/**
	 * @return the transactionId
	 */
	public Long getTransactionId() {
		return transactionId;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	/**
	 * @return the message
	 */
	public StringBuilder getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(StringBuilder message) {
		this.message = message;
	}
	
	

}
