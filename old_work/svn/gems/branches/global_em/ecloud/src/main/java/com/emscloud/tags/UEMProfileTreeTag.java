package com.emscloud.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

public class UEMProfileTreeTag extends TagSupport {
	
	protected TreeNode<FacilityType> uemProfileTreeHierarchy;
	
	public TreeNode<FacilityType> getUemProfileTreeHierarchy() {
		return uemProfileTreeHierarchy;
	}

	public void setUemProfileTreeHierarchy(
			TreeNode<FacilityType> uemProfileTreeHierarchy) {
		this.uemProfileTreeHierarchy = uemProfileTreeHierarchy;
	}

	public int doStartTag() throws JspException {
	    JspWriter out = pageContext.getOut();
	    try {
	    	out.print(getProfileTree(this.uemProfileTreeHierarchy));
	    } catch (IOException e) {
		}
	    return (SKIP_BODY);
	}
	
	private StringBuffer output;
	
	private StringBuffer getProfileTree(TreeNode<FacilityType> profileTreeHierarchy){
		output = new StringBuffer("");
		createProfileTree(profileTreeHierarchy);
		return output;
	}
	
	private void createProfileTree(TreeNode<FacilityType> profileTreeHierarchy){
		if(profileTreeHierarchy!=null && profileTreeHierarchy.getTreeNodeList().size() > 0){
			output.append("<ul>");
			for (TreeNode<FacilityType> childTreeNode : profileTreeHierarchy.getTreeNodeList()){
				if(childTreeNode!=null)
				{
					createChildProfileTree(childTreeNode);
				if(childTreeNode.getTreeNodeList().size() > 0){
					createProfileTree(childTreeNode);
				}
				}
				output.append("</li>");
			}
			output.append("</ul>");
		}
	}
	
	private void createChildProfileTree(TreeNode<FacilityType> childTreeNode){
		output.append("<li rel='"+childTreeNode.getNodeType().getLowerCaseName()+"' id='"+childTreeNode.getNodeType().getLowerCaseName()+"_"+childTreeNode.getNodeId()+"_"+childTreeNode.getParentNodeId()+"'><a href='#'>"+childTreeNode.getName()+"</a>");
	}
	
	public int doEndTag() {
        return EVAL_PAGE;
    }
}
