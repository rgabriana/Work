package com.emscloud.service;

 import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmLastEcSynctimeDao;
import com.emscloud.model.EmLastEcSynctime;


@Service("EmLastEcSynctimeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmLastEcSynctimeManager {
	static final Logger logger = Logger.getLogger(EmLastEcSynctimeManager.class
			.getName());

	@Resource
	EmLastEcSynctimeDao emLastEcSynctimeDao;

	public EmLastEcSynctime saveOrUpdate(EmLastEcSynctime instance) {
		return emLastEcSynctimeDao.saveOrUpdate(instance);
	}
	
	public EmLastEcSynctime getEmLastEcSynctimeForEmId(Long emId)
	{
		return emLastEcSynctimeDao.getEmLastEcSynctimeForEmId(emId);
	}
	
	public void deleteEmLastEcSynctimeByEmId(Long emId) {
		emLastEcSynctimeDao.deleteEmLastEcSynctimeByEmId(emId);
	}

}
