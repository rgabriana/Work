/**
 * 
 */
package com.emscloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.FaultDao;
import com.emscloud.model.EmInstance;

/**
 * @author sreedhar.kamishetti
 *
 */
@Service("faultManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FaultManager {
	
	@Resource
	EmInstanceManager emInstanceManger;
	
	@Resource
	private FaultDao faultDao;

	/**
	 * 
	 */
	public FaultManager() {
		// TODO Auto-generated constructor stub		
	}
	
	public ArrayList<String> getAllAlarms(Date fromDate, Date toDate) {
		
		//get all the em instances
		List<EmInstance> emInstances = emInstanceManger.loadallEmInstances();
		Iterator<EmInstance> emIter = emInstances.iterator();
		ArrayList<String> allAlarms = new ArrayList<String>();
		while(emIter.hasNext()) {
			EmInstance emInst = emIter.next();
			String dbName = emInst.getDatabaseName();
			allAlarms.addAll(faultDao.getEmAlarms(dbName, fromDate, toDate));
		}
		return allAlarms;
		
	} //end of method getAllAlarms
		
} //end of method FaultManager
