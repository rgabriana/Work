package com.emcloudinstance.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.FacilityTreeDao;
import com.emcloudinstance.util.tree.TreeNode;



@Service("facilityTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityTreeManager {
	
	@Resource
	FacilityTreeDao facilityTreeDao;
	
	
	public TreeNode<String> getEmFacilityTree(String emMac) {
		TreeNode<String> tree= facilityTreeDao.loadCompanyHierarchyForEmInstance(emMac);
		return tree;
		
	}

}
