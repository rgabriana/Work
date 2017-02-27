package com.emscloud.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmTasksUUIDDao;
import com.emscloud.model.EmTasksUUID;

@Service("emTasksUUIDManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmTasksUUIDManager {
static final Logger logger = Logger.getLogger(EmTasksUUIDManager.class.getName());
	
	@Resource
	EmTasksUUIDDao emTasksUUIDDao;
	
	public EmTasksUUID saveOrUpdate(EmTasksUUID emTasksUUID) {
		return emTasksUUIDDao.saveOrUpdate(emTasksUUID);
	}
	
	public EmTasksUUID getEmTasksUUIDById(Long id){
		return emTasksUUIDDao.getEmTasksUUIDById(id);
	}

}
