package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmTasksDao;
import com.emscloud.model.EmStatsList;
import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksList;

@Service("emTasksManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmTasksManager {
	
	@Resource
	EmTasksDao emTasksDao;
	
	public EmTasks saveObject(EmTasks emTask) {
		return (EmTasks)emTasksDao.saveObject(emTask);
	}
	
	public List<EmTasks> getEmTasksByEmInstanceId(Long emInstanceId) {
		return emTasksDao.getEmTasksByEmInstanceId(emInstanceId);
	}
	
	public List<EmTasks> getEmTasksList(){
		return emTasksDao.getEmTasksList();
		
	}
	
	public EmTasksList loadEmTasks(String orderway, int offset, int limit){
		return emTasksDao.loadEmTaksList(orderway, offset, limit);
	}
	
	public EmTasksList loadEmTasksByEmInstanceId(String orderway, int offset, int limit,long emInstanceId){
		return emTasksDao.loadEmTaksListByEmInstanceId(orderway, offset, limit,emInstanceId);
	}
	
	public EmTasks getEmTasksById(Long id) {
		Object o = emTasksDao.getObject(EmTasks.class, id);
		if(o != null) {
			return (EmTasks)o;
		}
		else {
			return null;
		}
	}
	
	public List<EmTasks> getActiveEmTasksByEmInstanceId(Long emInstanceId) {
		return emTasksDao.getActiveEmTasksByEmInstanceId(emInstanceId);
	}

}
