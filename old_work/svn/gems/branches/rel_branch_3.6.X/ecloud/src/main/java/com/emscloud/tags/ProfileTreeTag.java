package com.emscloud.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

public class ProfileTreeTag extends TagSupport {
	
	protected TreeNode<FacilityType> profileTreeHierarchy;
	
	public TreeNode<FacilityType> getProfileTreeHierarchy() {
		return profileTreeHierarchy;
	}

	public void setProfileTreeHierarchy(TreeNode<FacilityType> profileTreeHierarchy) {
		this.profileTreeHierarchy = profileTreeHierarchy;
	}

	public int doStartTag() throws JspException {
	    JspWriter out = pageContext.getOut();
	    try {
	    	out.print(getProfileTree(this.profileTreeHierarchy));
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
		if(profileTreeHierarchy.getTreeNodeList().size() > 0){
			output.append("<ul>");
			for (TreeNode<FacilityType> childTreeNode : profileTreeHierarchy.getTreeNodeList()){
				createChildProfileTree(childTreeNode);
				if(childTreeNode.getTreeNodeList().size() > 0){
					createProfileTree(childTreeNode);
				}
				output.append("</li>");
			}
			output.append("</ul>");
		}
	}
	
	
	private void createChildProfileTree(TreeNode<FacilityType> childTreeNode){
		output.append("<li rel='"+childTreeNode.getNodeType().getLowerCaseName()+"' id='"+childTreeNode.getNodeType().getLowerCaseName()+"_"+childTreeNode.getNodeId()+"'><a href='#'>"+childTreeNode.getName()+"</a>");
	}
	
	public int doEndTag() {
        return EVAL_PAGE;
    }
}
