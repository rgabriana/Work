package com.emscloud.tags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

public class FacilityTreeTag extends TagSupport {
	
	protected TreeNode<FacilityType> facilityTreeHierarchy;
	
	public TreeNode<FacilityType> getFacilityTreeHierarchy() {
		return facilityTreeHierarchy;
	}

	public void setFacilityTreeHierarchy(
			TreeNode<FacilityType> facilityTreeHierarchy) {
		this.facilityTreeHierarchy = facilityTreeHierarchy;
	}

	public int doStartTag() throws JspException {
	    JspWriter out = pageContext.getOut();
	    try {
	    	out.print(getFacilityTree(this.facilityTreeHierarchy));
	    } catch (IOException e) {
		}
	    return (SKIP_BODY);
	}
	
	private StringBuffer output;
	
	private StringBuffer getFacilityTree(TreeNode<FacilityType> facilityTreeHierarchy){
		output = new StringBuffer("");
		output.append("<ul>");
		createChildFacilityTree(facilityTreeHierarchy);
		createFaciltyTree(facilityTreeHierarchy);
		output.append("</li>");
		output.append("</ul>");
		return output;
	}
	
	private void createFaciltyTree(TreeNode<FacilityType> facilityTreeHierarchy){
		if(facilityTreeHierarchy.getTreeNodeList().size() > 0){
			output.append("<ul>");
			for (TreeNode<FacilityType> childTreeNode : facilityTreeHierarchy.getTreeNodeList()){
				createChildFacilityTree(childTreeNode);
				if(childTreeNode.getTreeNodeList().size() > 0){
					createFaciltyTree(childTreeNode);
				}
				output.append("</li>");
			}
			output.append("</ul>");
		}
	}
	
	private void createChildFacilityTree(TreeNode<FacilityType> childTreeNode){
		if(childTreeNode.getNodeType() == FacilityType.FLOOR){
			if(childTreeNode.isMapped()){
				output.append("<li rel='"+childTreeNode.getNodeType().getLowerCaseName()+"_mapped' id='"+childTreeNode.getNodeType().getLowerCaseName()+"_"+childTreeNode.getNodeId()+"'><a href='#'>"+childTreeNode.getName()+"</a>");
			}else{
				output.append("<li rel='"+childTreeNode.getNodeType().getLowerCaseName()+"_unmapped' id='"+childTreeNode.getNodeType().getLowerCaseName()+"_"+childTreeNode.getNodeId()+"'><a href='#'>"+childTreeNode.getName()+"</a>");
			}
		}else{
			output.append("<li rel='"+childTreeNode.getNodeType().getLowerCaseName()+"' id='"+childTreeNode.getNodeType().getLowerCaseName()+"_"+childTreeNode.getNodeId()+"'><a href='#'>"+childTreeNode.getName()+"</a>");
		}
	}
	    
    public int doEndTag() {
        return EVAL_PAGE;
    }
}
