package com.emscloud.util.tree;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.emscloud.types.FacilityType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TreeNode<E> {

	@XmlElement(name = "nodeId")
	private Long nodeId;

	@XmlElement(name = "parentNodeId")
	private Long parentNodeId;

	@XmlElement(name = "name")
    private String name;
	
	@XmlElement(name = "isLeaf")
    private boolean isLeaf = false;
	
	@XmlElement(name = "isMapped")
    private boolean isMapped = false;

	@XmlElement(name = "nodeType")
	private FacilityType nodeType;
    
	@XmlElement(name = "treeNodeList")
	private List<TreeNode<E>> treeNodeList = new ArrayList<TreeNode<E>>();
	
	@XmlElement(name = "isSelected")
	private boolean isSelected=true;
	
	@XmlElement(name = "count")
	private int count = 0;
	
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

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(Long parentNodeId) {
		this.parentNodeId = parentNodeId;
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

    public FacilityType getNodeType() {
        return nodeType;
    }

    public void setNodeType(FacilityType nodeType) {
        this.nodeType = nodeType;
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
    public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

    /**
     * @return the isSelected
     */
    public boolean getIsSelected() {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

	public void setMapped(boolean isMapped) {
		this.isMapped = isMapped;
	}

	public boolean isMapped() {
		return isMapped;
	}
}
