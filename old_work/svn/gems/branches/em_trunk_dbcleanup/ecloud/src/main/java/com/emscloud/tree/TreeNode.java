package com.emscloud.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<E> {

    private String nodeId;
    private String name;
    private boolean isLeaf = false;

    private E nodeType;
    private long tenantid;
    private long cloudFacilityId;
    
	private List<TreeNode<E>> treeNodeList = new ArrayList<TreeNode<E>>();

    public List<TreeNode<E>> getTreeNodeList() {
        return treeNodeList;
    }

    public void addTreeNode(TreeNode<E> node) {
        treeNodeList.add(node);
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTreeNodeList(List<TreeNode<E>> treeNodeList) {
        this.treeNodeList = treeNodeList;
    }

    public E getNodeType() {
        return nodeType;
    }

    public void setNodeType(E nodeType) {
        this.nodeType = nodeType;
    }

    public TreeNode<E> deepCopy() {
        TreeNode<E> treeNodeCopy = new TreeNode<E>();
        treeNodeCopy.nodeId = nodeId;
        treeNodeCopy.name = name;
        treeNodeCopy.nodeType = nodeType;
        treeNodeCopy.isLeaf = isLeaf;
        treeNodeCopy.tenantid = tenantid;
        treeNodeCopy.cloudFacilityId = cloudFacilityId;
        treeNodeCopy.treeNodeList = this.treeNodeList;
        
//        List<TreeNode<E>> copyTreeNodeList = new ArrayList<TreeNode<E>>();
//
//        for (TreeNode<E> node : this.treeNodeList) {
//            copyTreeNodeList.add(node.deepCopy());
//        }
        
        return treeNodeCopy;
    }
    
    public long getTenantid() {
		return tenantid;
	}

	public void setTenantid(long tenantid) {
		this.tenantid = tenantid;
	}
	
	public TreeNode<E> getLogicalSelection(){
	    TreeNode<E> selected = this;
	    //check if node has exactly 1 child, then set the node as selected-node.
	    if(this.treeNodeList.size() == 1){
	        TreeNode<E> node = this.treeNodeList.get(0);
	        if(!node.isLeaf()){
	            selected = node.getLogicalSelection();
	        }	        
	    }	    
	    return selected;	    
	}	
	
	public TreeNode<E> getShallowCopy(){
		TreeNode<E> treeNodeCopy = new TreeNode<E>();
		treeNodeCopy.nodeId = nodeId;
        treeNodeCopy.name = name;
        treeNodeCopy.nodeType = nodeType;
        treeNodeCopy.isLeaf = isLeaf;
        treeNodeCopy.tenantid = tenantid;
        treeNodeCopy.cloudFacilityId = cloudFacilityId;
        treeNodeCopy.treeNodeList = new ArrayList<TreeNode<E>>();
        return treeNodeCopy;
	}

	/**
	 * @return the cloudFacilityId
	 */
	public long getcloudFacilityId() {
		return cloudFacilityId;
	}

	/**
	 * @param cloudFacilityId the cloudFacilityId to set
	 */
	public void setcloudFacilityId(long cloudFacilityId) {
		this.cloudFacilityId = cloudFacilityId;
	}
}
