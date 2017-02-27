package com.emscloud.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmStateDao;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStateList;

@Service("emStatusManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmStateManager {

	@Resource 
	EmStateDao emStateDao ;

	public EmState loadLastEmStatsByEmInstanceId(long id) {
		return emStateDao.loadLastEmStatsByEmInstanceId(id);
	}

	public EmState saveOrUpdate(EmState emState) {
		
		return emStateDao.saveOrUpdate(emState);
	}

	public EmState loadEmStateById(Long latestEmStateId) {
		return emStateDao.loadEmStateById(latestEmStateId); 
	}
	
	public EmState loadBeforeSyncFailedEmStateByEmInstanceId(long id) {
		return emStateDao.loadBeforeSyncFailedEmStateByEmInstanceId(id);
	}
	
	public EmState resetPreviousFlagByEmInstanceId(Long id) {
			return emStateDao.resetPreviousFlag(id); 
	}
	
	public EmStateList getLatestEmStateListByEmInstanceId(String orderway, int offset, int limit, Long emInstanceId){
    	return emStateDao.loadEmStateListByEmInstanceId(orderway, offset, limit, emInstanceId);
	}
}
