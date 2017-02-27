package com.emscloud.service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.BillPaymentDao;
import com.emscloud.model.CustomerBillPayment;

@Service("billPaymentManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BillPaymentManager {
	
	@Resource
	private BillPaymentDao billPaymentDao;

	public CustomerBillPayment getAllBillsPerCustomer(Long customerId, String orderway, int offset, int defaultRows) {
		return billPaymentDao.getAllBillPaymentPerCustomer(customerId,orderway,offset,defaultRows);
	}

	public void updateCustomerBillPayment(long customerId, BigDecimal paymentAmount) {
		billPaymentDao.updateCustomerBillPayment(customerId,paymentAmount);
	}
}
