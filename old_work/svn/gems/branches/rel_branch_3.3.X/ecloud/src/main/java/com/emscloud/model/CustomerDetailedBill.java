/**
 * 
 */
package com.emscloud.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author sreedhar.kamishetti
 *
 */
public class CustomerDetailedBill {

	@XmlElement(name = "billInvoice")
	private CustomerSppaBill billInvoice;
	@XmlElement(name = "emSppaBills")
	private List<SppaBill> emBills;
	
	/**
	 * 
	 */
	public CustomerDetailedBill() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the billInvoice
	 */	
	public CustomerSppaBill getBillInvoice() {
		return billInvoice;
	}

	/**
	 * @param billInvoice the billInvoice to set
	 */
	public void setBillInvoice(CustomerSppaBill billInvoice) {
		this.billInvoice = billInvoice;
	}

	/**
	 * @return the emBills
	 */
	public List<SppaBill> getEmBills() {
		return emBills;
	}

	/**
	 * @param emBills the emBills to set
	 */
	public void setEmBills(List<SppaBill> emBills) {
		this.emBills = emBills;
	}
	
}
