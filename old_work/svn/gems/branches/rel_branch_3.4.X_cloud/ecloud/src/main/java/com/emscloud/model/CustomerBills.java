/**
 * 
 */
package com.emscloud.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sharad Mahajan
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CustomerBills implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5129040236930610997L;
	public static final int DEFAULT_ROWS = 20;
	@XmlElement(name = "page")
    private int page;
    @XmlElement(name = "records")
    private long records;
    @XmlElement(name = "total")
    private long total;
	
	@XmlElement(name = "customerSppaBill")
	List<CustomerSppaBill> customerSppaBill;
	
	/**
	 * 
	 */
	public CustomerBills() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the records
	 */
	public long getRecords() {
		return records;
	}

	/**
	 * @param records the records to set
	 */
	public void setRecords(long records) {
		this.records = records;
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(long total) {
		this.records = total;
        this.total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
        if (this.total == 0) {
            this.total = 1;
        }
	}

	/**
	 * @return the customerSppaBill
	 */
	public List<CustomerSppaBill> getCustomerSppaBill() {
		return customerSppaBill;
	}

	/**
	 * @param customerSppaBill the customerSppaBill to set
	 */
	public void setCustomerSppaBill(List<CustomerSppaBill> customerSppaBill) {
		this.customerSppaBill = customerSppaBill;
	}


} //end of class CustomerBills
