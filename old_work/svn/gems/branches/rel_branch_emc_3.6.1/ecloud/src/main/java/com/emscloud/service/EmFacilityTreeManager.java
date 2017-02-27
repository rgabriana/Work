package com.emscloud.service;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.ResponseWrapper;
import com.emscloud.model.EmInstance;
import com.emscloud.types.FacilityType;
import com.emscloud.types.GlemModeType;
import com.emscloud.util.tree.TreeNode;

@Service("emFacilityTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmFacilityTreeManager {

	@Resource
	EmInstanceManager emInstanceManager;
	
	@Resource
	private GlemManager glemManager;

	static final Logger logger = Logger.getLogger(EmFacilityTreeManager.class
			.getName());

	public TreeNode<FacilityType> loadEmInstanceCompanyHierarchy(
			long emInstanceId) {
		TreeNode<FacilityType> result = new TreeNode<FacilityType>();
		try {
			EmInstance emInstance = emInstanceManager
					.getEmInstance(emInstanceId);
			String strServiceCall = "/services/org/facilitytree/getEmFacilityTree/";
			if (glemManager.getGLEMMode() == GlemModeType.UEM.getMode()) {
				strServiceCall = "/api/uem/getEmFacilityTree";
			}
			if(emInstance != null){
				ResponseWrapper<TreeNode> response = glemManager.getAdapter()
						.executeGet(
								emInstance,
								glemManager.getAdapter().getContextUrl() + strServiceCall,
								MediaType.APPLICATION_XML, TreeNode.class);

				if (response.getStatus() == Response.Status.OK.getStatusCode()) {
					result = (TreeNode<FacilityType>) response.getItems()
							.getTreeNodeList().get(0);
				} else {
					logger.error("Not able to get Facility Tree from EM:- "
							+ response.getEm().getMacId() + " reason :- "
							+ response.getStatus());
				}				
			}
		
		} catch (Exception e) {
			result = null;
			logger.error(e.getMessage());
		}
		return result;
	}
}
