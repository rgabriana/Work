package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;
import javax.swing.tree.TreeNode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.EmInstanceDao;
import com.emscloud.model.EmInstance;
import com.emscloud.types.FacilityType;
import com.emscloud.utils.scripts.DatabaseUtil;

@Service("emInstanceManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceManager {
	
	@Resource
	private EmInstanceDao		emInstanceDao;

	public List<EmInstance> loadallEmInstances() {
		
		return emInstanceDao.loadAllEmInstances() ;
	}

	public EmInstance loadEmInstanceById(long id) {
		
		return emInstanceDao.loadEmInstanceById(id) ;
	}
		public EmInstance loadEmInstanceByMac(String mac) {
		
		return emInstanceDao.loadEmInstanceByMac(mac) ;
	}
	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		
		return emInstanceDao.loadEmInstancesByCustomerId(id) ;
	}

	public void saveOrUpdate(EmInstance instance) {		
		emInstanceDao.saveOrUpdate(instance) ;		
		DatabaseUtil.createDatabase(instance.getDatabaseName());
	}
	
	public void delete(Long id)
	{
		emInstanceDao.deleteById(id);
	}

	
}
