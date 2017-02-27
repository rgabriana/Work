package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.CustomerDao;
import com.emscloud.dao.UpgradesDao;
import com.emscloud.model.Customer;
import com.emscloud.model.UpgradeFilesList;
import com.emscloud.model.Upgrades;



@Service("upgradesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UpgradesManager {	
	
	
	@Resource
	private UpgradesDao upgradesDao;	
	
	public UpgradeFilesList loadUpgradeFilesList(String orderway, int offset, int limit) {
		return upgradesDao.loadUpgradeFilesList(orderway, offset, limit);
	}

	public List<Upgrades> loadallUpgrades() {		
		return upgradesDao.loadAllUpgrades();
	}

	public void saveOrUpdate(Upgrades upgrade) {
		upgradesDao.saveOrUpdate(upgrade);		
	}
	
	public Upgrades loadDebianById(Long id)
	{		
		return upgradesDao.loadUpgradesById(id);
	}
}
