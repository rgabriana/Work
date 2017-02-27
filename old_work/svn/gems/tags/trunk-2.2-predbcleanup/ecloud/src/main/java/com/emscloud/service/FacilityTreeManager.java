package com.emscloud.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.security.provisioning.GroupManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.CustomerDao;
import com.emscloud.dao.UserDao;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.types.FacilityType;
import com.emscloud.utils.tree.TreeNode;

@Service("facilityTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityTreeManager {

	Map<Long, TreeNode<FacilityType>> facilityTreeMap = new HashMap<Long, TreeNode<FacilityType>>();

	@Resource
	private CustomerDao customerDao;

	public TreeNode<FacilityType> loadCustomerHierarchy() {

		if (facilityTreeMap.containsKey(0L)) {
			return facilityTreeMap.get(0L);
		}

		TreeNode<FacilityType> companyHierachy = customerDao
				.loadCustomerHierarchy();
		facilityTreeMap.put(0L, companyHierachy);
		return companyHierachy;
	}

	@SuppressWarnings("unused")
	private void assignNode(TreeNode<FacilityType> parentNode,
			TreeNode<FacilityType> topHierarchy, long userId,
			Set<String> userLocationsSet) {
		for (TreeNode<FacilityType> itrNode : topHierarchy.getTreeNodeList()) {
			if (userLocationsSet.contains(itrNode.getNodeType().getName()
					+ itrNode.getNodeId())) {
				TreeNode<FacilityType> newNode = itrNode.getShallowCopy();
				parentNode.addTreeNode(newNode);
				assignNode(newNode, itrNode, userId, userLocationsSet);
			} else {
				assignNode(parentNode, itrNode, userId, userLocationsSet);
			}

		}
	}

	public void inValidateFacilitiesTreeCache() {
		facilityTreeMap.clear();
		//Let' build the new cache
		loadCustomerHierarchy();

	}

	public void inValidateFacilitiesTreeCacheForTenant(long tenantId) {
		if (facilityTreeMap.containsKey(tenantId)) {
			facilityTreeMap.remove(tenantId);
		}
	}

}
