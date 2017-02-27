package com.emscloud.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.FacilityDao;
import com.emscloud.model.Facility;
import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

@Service("facilityTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityTreeManager {

	@Resource
	private FacilityDao facilityDao;
	
	@Resource
	private FacilityEmMappingManager facilityEmMappingManager;

	private TreeNode<FacilityType> companyNode;
	
	public TreeNode<FacilityType> loadCompanyHierarchyByCustomerId(long custId) {
		if(companyNode != null && !companyNode.getTreeNodeList().isEmpty())
			return companyNode;
		
		Facility facility = facilityDao.loadFacilityTreeByCustomer(custId);
		if(facility != null) {
        	companyNode = createNode(false, facility.getId(), 
        			facility.getName(), FacilityType.getFacilityType(facility.getType()));
        	findAndCreateChildNodes(companyNode, facility);
		}

        return companyNode;
	}

	@SuppressWarnings("unchecked")
	private void findAndCreateChildNodes(TreeNode treeNode, Facility facility) {
		Set<Facility> childFacilities = facility.getChildFacilities();
		
		List<Facility> childFacilitiesList = new ArrayList<Facility>(childFacilities);
		Collections.sort(childFacilitiesList, Facility.FacilityNameComparator);
				
		for (Facility childFacility : childFacilitiesList) {
			Set<Facility> grandChildren = childFacility.getChildFacilities();
			boolean bHasChildren = ((grandChildren!=null)&& !grandChildren.isEmpty());
			TreeNode childNode = createNode(!bHasChildren, childFacility.getId(), childFacility.getName(), FacilityType.getFacilityType(childFacility.getType()));
			treeNode.addTreeNode(childNode);
			if(bHasChildren)
				findAndCreateChildNodes(childNode, childFacility);
		}
	}
	
	private TreeNode<FacilityType> createNode(boolean isLeaf, Long id, String name, 
			   FacilityType type) {
			  TreeNode<FacilityType> node = new TreeNode<FacilityType>();
			  node.setLeaf(isLeaf);
			  node.setName(name);
			  node.setNodeId(id);
			  node.setNodeType(type);
			  if(type == FacilityType.FLOOR){
				  if( facilityEmMappingManager.getFacilityEmMappingOnFacilityId(id) == null){
						node.setMapped(false);  
					}else{
					    node.setMapped(true);  
					}
			  }else{
				  node.setMapped(false); 
			  }
			  
			  return node;
	}
	
	public void clearCache() {
		companyNode = null;
	}
	
	// Get bread crumb for given node id in facility tree.
	public String getNodePath(Long nodeID) {
		Facility facility = facilityDao.loadFacilityById(nodeID);
		
		if(facility.getParentId() != null)
			return ((getNodePath(facility.getParentId()) + " >> " + facility.getName()));
		else
			return (facility.getName());
	}


}
