package com.ems.util.tree;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TreeNode<E> {

	@XmlElement(name = "nodeId")
	private String nodeId;
	
	@XmlElement(name = "name")
    private String name;
    
	@XmlElement(name = "isLeaf")
    private boolean isLeaf = false;
	
	@XmlElement(name = "nodeType")
    private E nodeType;
	
	@XmlElement(name = "tenantid")
    private long tenantid;
	
	@XmlElement(name = "sweepTimerId")
    private long sweepTimerId;
    
    @XmlElement(name = "isSelected")
    private boolean isSelected=true;
    
    @XmlElement(name = "count")
    private int count = 0;
    
    @XmlElement(name = "treeNodeList")
	private List<TreeNode<E>> treeNodeList = new ArrayList<TreeNode<E>>();

    public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

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
        treeNodeCopy.sweepTimerId = sweepTimerId;
        treeNodeCopy.isSelected = isSelected;
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
        treeNodeCopy.sweepTimerId = sweepTimerId;
        treeNodeCopy.isSelected = isSelected;
        treeNodeCopy.treeNodeList = new ArrayList<TreeNode<E>>();
        return treeNodeCopy;
	}

	/**
	 * @return the sweepTimerId
	 */
	public long getSweepTimerId() {
		return sweepTimerId;
	}

	/**
	 * @param sweepTimerId the sweepTimerId to set
	 */
	public void setSweepTimerId(long sweepTimerId) {
		this.sweepTimerId = sweepTimerId;
	}
	public boolean getIsSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}
