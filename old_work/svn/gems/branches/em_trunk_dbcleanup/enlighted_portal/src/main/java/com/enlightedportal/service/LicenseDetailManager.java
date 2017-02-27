package com.enlightedportal.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enlightedportal.dao.LicenseDetailDao;
import com.enlightedportal.dao.UserDao;
import com.enlightedportal.model.LicenseDetails;
import com.enlightedportal.model.LicensePanel;
import com.enlightedportal.model.User;


@Service("licenseDetailManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LicenseDetailManager {
	
	@Resource
	private LicenseDetailDao licenseDetailDao;

	/**
	 * load license details for a given customer
	 * 
	 * @param customerId
	 *            Customer id. 
	 * @return License Details acquired by the given customer
	 */
	public List<LicenseDetails> loadLicenseDetailsByCustomerId(Long customerId) {
		return licenseDetailDao.loadLicenseDetailsByCustomerId(customerId);
	}

public void saveOrUpdate(LicenseDetails licenseDetails) {
		
		licenseDetailDao.saveOrUpdate(licenseDetails) ;
	}

public List<LicenseDetails> loadAllLicenseDetails() {
	
	return licenseDetailDao.loadAllLicenseDetails();
}

public byte[] loadApiKeyWRTMac(String mac) {
	// TODO Auto-generated method stub
	return licenseDetailDao.loadApiKeyWRTMac(mac) ;
}

}
